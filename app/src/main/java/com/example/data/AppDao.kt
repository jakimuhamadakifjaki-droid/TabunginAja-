package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Saving Goals ---
    @Query("SELECT * FROM saving_goals ORDER BY priority ASC, createdAt DESC")
    fun getAllGoals(): Flow<List<SavingGoal>>

    @Query("SELECT * FROM saving_goals WHERE id = :id LIMIT 1")
    fun getGoalById(id: Int): Flow<SavingGoal?>

    @Query("SELECT * FROM saving_goals WHERE isCompleted = 0 ORDER BY priority ASC, createdAt DESC LIMIT 1")
    fun getPrimaryActiveGoal(): Flow<SavingGoal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingGoal)

    @Query("UPDATE saving_goals SET savedAmount = savedAmount + :amount WHERE id = :goalId")
    suspend fun incrementGoalAmount(goalId: Int, amount: Double)

    @Query("UPDATE saving_goals SET isCompleted = 1 WHERE id = :goalId")
    suspend fun markGoalAsCompleted(goalId: Int)


    // --- Saving Transactions ---
    @Query("SELECT * FROM saving_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<SavingTransaction>>

    @Query("SELECT * FROM saving_transactions WHERE goalId = :goalId ORDER BY timestamp DESC")
    fun getTransactionsForGoal(goalId: Int): Flow<List<SavingTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SavingTransaction)

    @Query("DELETE FROM saving_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    @Query("DELETE FROM saving_transactions WHERE goalId = :goalId")
    suspend fun deleteTransactionsByGoalId(goalId: Int)


    // --- Challenges ---
    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE isActive = 1")
    fun getActiveChallenges(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE codeKey = :codeKey LIMIT 1")
    suspend fun getChallengeByCode(codeKey: String): Challenge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Query("UPDATE challenges SET currentAmount = currentAmount + :amount, completedSteps = completedSteps + 1 WHERE id = :challengeId")
    suspend fun incrementChallengeStep(challengeId: Int, amount: Double)
}
