package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.ceil

class AppRepository(private val appDao: AppDao) {

    val allGoals: Flow<List<SavingGoal>> = appDao.getAllGoals()
    val primaryActiveGoal: Flow<SavingGoal?> = appDao.getPrimaryActiveGoal()
    val allTransactions: Flow<List<SavingTransaction>> = appDao.getAllTransactions()
    val allChallenges: Flow<List<Challenge>> = appDao.getAllChallenges()
    val activeChallenges: Flow<List<Challenge>> = appDao.getActiveChallenges()

    fun getGoalById(id: Int): Flow<SavingGoal?> = appDao.getGoalById(id)
    fun getTransactionsForGoal(goalId: Int): Flow<List<SavingTransaction>> = appDao.getTransactionsForGoal(goalId)

    suspend fun insertGoal(goal: SavingGoal): Long {
        return appDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: SavingGoal) {
        appDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: SavingGoal) {
        appDao.deleteTransactionsByGoalId(goal.id)
        appDao.deleteGoal(goal)
    }

    // Direct add savings to goal
    suspend fun addSavingToGoal(goalId: Int, amount: Double, note: String, type: String): ResultState {
        val targetGoal = appDao.getGoalById(goalId).firstOrNull() ?: return ResultState.Error("Tujuan tabungan tidak ditemukan.")
        if (targetGoal.isCompleted) {
            return ResultState.Error("Tujuan tabungan ini sudah tercapai! 🎉")
        }

        val newAmount = targetGoal.savedAmount + amount
        val isCompletedNow = newAmount >= targetGoal.price

        val updatedGoal = targetGoal.copy(
            savedAmount = if (isCompletedNow) targetGoal.price else newAmount,
            isCompleted = isCompletedNow
        )

        appDao.updateGoal(updatedGoal)

        // Log transaction
        appDao.insertTransaction(
            SavingTransaction(
                goalId = goalId,
                goalName = targetGoal.name,
                amount = amount,
                note = note,
                timestamp = System.currentTimeMillis(),
                type = type
            )
        )

        return if (isCompletedNow) {
            ResultState.GoalCompleted(targetGoal.name)
        } else {
            ResultState.Success("Berhasil menabung Rp ${String.format("%,.0f", amount)}!")
        }
    }

    // AutoSave from income
    suspend fun applyAutoSave(goalId: Int, incomeAmount: Double, percentage: Double): ResultState {
        val autoSaveSavings = incomeAmount * (percentage / 100.0)
        if (autoSaveSavings <= 0) return ResultState.Error("Nominal pemasukan tidak valid.")
        val note = "AutoSave ${percentage.toInt()}% dari pemasukan Rp ${String.format("%,.0f", incomeAmount)}"
        return addSavingToGoal(goalId, autoSaveSavings, note, "autosave")
    }

    // Round-Up Saving
    suspend fun applyRoundUp(goalId: Int, expenseAmount: Double, roundOption: Int): ResultState {
        if (expenseAmount <= 0) return ResultState.Error("Nominal belanja tidak valid.")
        
        // Custom round options:
        // 1: Round to nearest Rp1.000
        // 2: Round to nearest Rp5.000
        // 3: Round to nearest Rp10.000
        val divisor = when (roundOption) {
            1 -> 1000.0
            2 -> 5000.0
            3 -> 10000.0
            else -> 1000.0
        }

        val roundedAmount = ceil(expenseAmount / divisor) * divisor
        val gap = roundedAmount - expenseAmount

        return if (gap > 0) {
            val note = "Pembulatan Belanja Rp ${String.format("%,.0f", expenseAmount)} -> Rp ${String.format("%,.0f", roundedAmount)}"
            addSavingToGoal(goalId, gap, note, "roundup")
        } else {
            ResultState.Success("Belanja kamu sudah bulat, tidak ada selisih untuk ditabung. 🪙")
        }
    }

    // Challenge action / step completion
    suspend fun completeChallengeStep(challengeId: Int, mainGoalId: Int): ResultState {
        val challengesList = appDao.getAllChallenges().firstOrNull() ?: emptyList()
        val challenge = challengesList.find { it.id == challengeId } ?: return ResultState.Error("Tantangan tidak ditemukan.")

        if (challenge.completedSteps >= challenge.totalSteps) {
            return ResultState.Error("Tantangan ini sudah selesai! 🏆")
        }

        val amountToSave = challenge.contributionPerStep
        val newCompletedSteps = challenge.completedSteps + 1
        val isChallengeFinishedNow = newCompletedSteps >= challenge.totalSteps
        val newCurrentAmount = challenge.currentAmount + amountToSave

        val updatedChallenge = challenge.copy(
            currentAmount = newCurrentAmount,
            completedSteps = newCompletedSteps,
            isActive = !isChallengeFinishedNow // Deactivate if done
        )

        appDao.updateChallenge(updatedChallenge)

        // Save contribution into the user's primary savings goal
        val note = "Misi '${challenge.title}' (Langkah $newCompletedSteps/${challenge.totalSteps})"
        val result = addSavingToGoal(mainGoalId, amountToSave, note, "challenge")

        return when (result) {
            is ResultState.GoalCompleted -> {
                ResultState.ChallengeStepSuccess(
                    challengeTitle = challenge.title,
                    isChallengeCompleted = isChallengeFinishedNow,
                    msg = "Misi selesai! Dan barang impianmu '${result.goalName}' TELAH TERCAPAI! 🎉🏆"
                )
            }
            else -> {
                ResultState.ChallengeStepSuccess(
                    challengeTitle = challenge.title,
                    isChallengeCompleted = isChallengeFinishedNow,
                    msg = "Yey! Tambah Rp ${String.format("%,.0f", amountToSave)} dari tantangan '${challenge.title}'!"
                )
            }
        }
    }

    // Toggle challenge status
    suspend fun toggleChallengeActive(challengeId: Int, isActive: Boolean) {
        val challengesList = appDao.getAllChallenges().firstOrNull() ?: emptyList()
        val challenge = challengesList.find { it.id == challengeId } ?: return
        
        val updated = challenge.copy(
            isActive = isActive,
            startDate = if (isActive) System.currentTimeMillis() else null,
            currentAmount = if (isActive) challenge.currentAmount else 0.0,
            completedSteps = if (isActive) challenge.completedSteps else 0
        )
        appDao.updateChallenge(updated)
    }

    // Reset challenge progress so they can repeat it
    suspend fun resetChallenge(challengeId: Int) {
        val challengesList = appDao.getAllChallenges().firstOrNull() ?: emptyList()
        val challenge = challengesList.find { it.id == challengeId } ?: return

        val reset = challenge.copy(
            isActive = false,
            startDate = null,
            currentAmount = 0.0,
            completedSteps = 0
        )
        appDao.updateChallenge(reset)
    }
}

sealed class ResultState {
    data class Success(val msg: String) : ResultState()
    data class Error(val errorMsg: String) : ResultState()
    data class GoalCompleted(val goalName: String) : ResultState()
    data class ChallengeStepSuccess(
        val challengeTitle: String,
        val isChallengeCompleted: Boolean,
        val msg: String
    ) : ResultState()
}
