package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saving_goals")
data class SavingGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val savedAmount: Double = 0.0,
    val targetDate: Long? = null, // Deadline epoch milliseconds
    val imageUri: String? = null, // String or local drawable descriptor
    val isCompleted: Boolean = false,
    val priority: Int = 1, // 1: Utama (High), 2: Menengah, 3: Santai
    val category: String = "Lainnya", // e.g., Smartphone, Laptop, Motor, Kamera, Liburan, Hobi, dll.
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saving_transactions")
data class SavingTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int, // Refers to SavingGoal.id (or 0 for general buffer)
    val goalName: String = "", // Redundant for easy query reporting
    val amount: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String // "manual", "autosave", "roundup", "challenge"
)

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val durationDays: Int = 30,
    val isActive: Boolean = false,
    val startDate: Long? = null,
    val contributionPerStep: Double = 0.0, // Contribution per milestone action
    val completedSteps: Int = 0,
    val totalSteps: Int = 30,
    val codeKey: String = "" // "52_weeks", "no_jajan", "coffee_skip", "receh", "weekend", "daily_10k", "sprint"
)
