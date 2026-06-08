package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TabunginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    // Database state flows
    val allGoals: StateFlow<List<SavingGoal>>
    val primaryActiveGoal: StateFlow<SavingGoal?>
    val allTransactions: StateFlow<List<SavingTransaction>>
    val allChallenges: StateFlow<List<Challenge>>
    val activeChallenges: StateFlow<List<Challenge>>

    // Selected Goal ID for visual tracking (Defaults to primary active goal, fallback to first available)
    private val _selectedGoalId = MutableStateFlow<Int?>(null)
    val selectedGoalId: StateFlow<Int?> = _selectedGoalId.asStateFlow()

    // Observable stream for the currently active goal for progress visualizers
    val currentlyTrackedGoal: StateFlow<SavingGoal?>

    // UI Message state
    private val _uiEvent = MutableSharedFlow<ResultState>()
    val uiEvent: SharedFlow<ResultState> = _uiEvent.asSharedFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        val dao = database.appDao()
        repository = AppRepository(dao)

        allGoals = repository.allGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        primaryActiveGoal = repository.primaryActiveGoal.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allChallenges = repository.allChallenges.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeChallenges = repository.activeChallenges.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Derive active tracked goal: either the user-selected goal, or the primary active goal, or null
        currentlyTrackedGoal = combine(allGoals, selectedGoalId, primaryActiveGoal) { goals, selectedId, primary ->
            if (goals.isEmpty()) return@combine null
            val match = goals.find { it.id == selectedId }
            match ?: primary ?: goals.firstOrNull { !it.isCompleted } ?: goals.first()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun selectTrackedGoal(goalId: Int) {
        _selectedGoalId.value = goalId
    }

    // --- Actions ---

    fun insertGoal(name: String, price: Double, category: String, priority: Int, targetDate: Long? = null, imageUri: String? = null) {
        viewModelScope.launch {
            val newGoal = SavingGoal(
                name = name,
                price = price,
                category = category,
                priority = priority,
                targetDate = targetDate,
                imageUri = imageUri ?: getCategoryDefaultImage(category)
            )
            repository.insertGoal(newGoal)
            _uiEvent.emit(ResultState.Success("Berhasil membuat target '${name}'! 🌱"))
        }
    }

    fun deleteGoal(goal: SavingGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            _uiEvent.emit(ResultState.Success("Berhasil menghapus target '${goal.name}'."))
        }
    }

    fun addSaving(goalId: Int, amount: Double, note: String) {
        viewModelScope.launch {
            val finalNote = note.ifBlank { "Setoran manual" }
            val result = repository.addSavingToGoal(goalId, amount, finalNote, "manual")
            _uiEvent.emit(result)
        }
    }

    fun addAutoSave(goalId: Int, incomeAmount: Double, percentage: Double) {
        viewModelScope.launch {
            val result = repository.applyAutoSave(goalId, incomeAmount, percentage)
            _uiEvent.emit(result)
        }
    }

    fun addRoundUp(goalId: Int, expenseAmount: Double, roundOption: Int) {
        viewModelScope.launch {
            val result = repository.applyRoundUp(goalId, expenseAmount, roundOption)
            _uiEvent.emit(result)
        }
    }

    fun completeChallengeStep(challengeId: Int, mainGoalId: Int) {
        viewModelScope.launch {
            val result = repository.completeChallengeStep(challengeId, mainGoalId)
            _uiEvent.emit(result)
        }
    }

    fun toggleChallengeActive(challengeId: Int, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleChallengeActive(challengeId, isActive)
            val msg = if (isActive) "Tantangan telah dimulai! Semangat! 💚" else "Tantangan dinonaktifkan."
            _uiEvent.emit(ResultState.Success(msg))
        }
    }

    fun resetChallenge(challengeId: Int) {
        viewModelScope.launch {
            repository.resetChallenge(challengeId)
            _uiEvent.emit(ResultState.Success("Progress tantangan disetel ulang."))
        }
    }

    // --- Gemini Financial Tips ---
    private val _geminiTip = MutableStateFlow<String>("")
    val geminiTip: StateFlow<String> = _geminiTip.asStateFlow()

    private val _isGeneratingTip = MutableStateFlow<Boolean>(false)
    val isGeneratingTip: StateFlow<Boolean> = _isGeneratingTip.asStateFlow()

    fun refreshGeminiTip(goal: SavingGoal?) {
        viewModelScope.launch {
            _isGeneratingTip.value = true
            try {
                val tip = GeminiRepository.generateFinancialTip(goal)
                _geminiTip.value = tip
            } catch (e: Exception) {
                _geminiTip.value = "Semoga harimu menyenangkan! Mari terus konsisten menabung untuk mewujudkan impianmu! 🌱"
            } finally {
                _isGeneratingTip.value = false
            }
        }
    }

    private fun getCategoryDefaultImage(category: String): String {
        return when (category.lowercase()) {
            "smartphone" -> "smartphone"
            "laptop" -> "laptop"
            "motor" -> "motor"
            "kamera" -> "kamera"
            "liburan" -> "holiday"
            "pendidikan" -> "education"
            "hobi" -> "hobby"
            "konser" -> "concert"
            else -> "lainnya"
        }
    }
}
