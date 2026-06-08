package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SavingGoal::class, SavingTransaction::class, Challenge::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tabunginaja_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.appDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: AppDao) {
                // Prepopulate 2 delightful Goal / Target items
                val laptopGoalId = dao.insertGoal(
                    SavingGoal(
                        id = 1,
                        name = "Laptop Gaming / Kerja",
                        price = 10000000.0,
                        savedAmount = 0.0, // Clear initial progress (0%)
                        priority = 1,
                        category = "Laptop",
                        imageUri = "laptop"
                    )
                )

                dao.insertGoal(
                    SavingGoal(
                        id = 2,
                        name = "Liburan ke Bali 🌴",
                        price = 4000000.0,
                        savedAmount = 0.0, // Clear initial progress (0%)
                        priority = 2,
                        category = "Liburan",
                        imageUri = "holiday"
                    )
                )

                // Prepopulate challenges from the brief
                dao.insertChallenge(
                    Challenge(
                        title = "Receh Harian 🪙",
                        description = "Menabung minimal Rp 2.000 setiap hari secara konsisten selama sebulan.",
                        targetAmount = 60000.0,
                        currentAmount = 0.0,
                        durationDays = 30,
                        isActive = false,
                        contributionPerStep = 2000.0,
                        completedSteps = 0,
                        totalSteps = 30,
                        codeKey = "receh"
                    )
                )

                dao.insertChallenge(
                    Challenge(
                        title = "Tantangan 52 Minggu ⏳",
                        description = "Menabung bertahap setiap minggu (Minggu 1: Rp 10.000, Minggu 2: Rp 20.000, dst).",
                        targetAmount = 1378000.0,
                        currentAmount = 0.0,
                        durationDays = 365,
                        isActive = false,
                        contributionPerStep = 25000.0, // average representation
                        completedSteps = 0,
                        totalSteps = 52,
                        codeKey = "52_weeks"
                    )
                )

                dao.insertChallenge(
                    Challenge(
                        title = "No Jajan 3 Hari 🚫🍔",
                        description = "Tahan diri dari jajan selama 3 hari. Uang jajan dialihkan langsung ke tabungan.",
                        targetAmount = 75000.0,
                        currentAmount = 0.0, // Clear starting amount
                        durationDays = 3,
                        isActive = true, // Set active by default to showcase challenges!
                        startDate = System.currentTimeMillis(),
                        contributionPerStep = 25000.0,
                        completedSteps = 0, // Clear completed steps
                        totalSteps = 3,
                        codeKey = "no_jajan"
                    )
                )

                dao.insertChallenge(
                    Challenge(
                        title = "Kopi Kilat ☕💨",
                        description = "Kurangi satu pembelian kopi kafe setiap minggunya selama 4 minggu berturut-turut.",
                        targetAmount = 100000.0,
                        currentAmount = 0.0,
                        durationDays = 28,
                        isActive = false,
                        contributionPerStep = 25000.0,
                        completedSteps = 0,
                        totalSteps = 4,
                        codeKey = "coffee_skip"
                    )
                )

                dao.insertChallenge(
                    Challenge(
                        title = "Weekend Saving 🌴💳",
                        description = "Tantangan menabung konsisten setiap akhir pekan (Sabtu & Minggu) selama sebulan.",
                        targetAmount = 200000.0,
                        currentAmount = 0.0, // Clear starting amount
                        durationDays = 30,
                        isActive = true, // Active by default!
                        startDate = System.currentTimeMillis() - 86400000L * 3,
                        contributionPerStep = 25000.0,
                        completedSteps = 0, // Clear completed steps
                        totalSteps = 8,
                        codeKey = "weekend"
                    )
                )

                dao.insertChallenge(
                    Challenge(
                        title = "Tantangan Rp10.000 Sehari 🏷️",
                        description = "Konsisten menyisihkan uang pas Rp10.000 setiap hari berturut-turut selama 30 hari.",
                        targetAmount = 300000.0,
                        currentAmount = 0.0,
                        durationDays = 30,
                        isActive = false,
                        contributionPerStep = 10000.0,
                        completedSteps = 0,
                        totalSteps = 30,
                        codeKey = "daily_10k"
                    )
                )

                dao.insertChallenge(
                    Challenge(
                        title = "Sprint Target 🏃‍♂️⚡",
                        description = "Fokus ekstra menambah saldo tabungan untuk target barang impian utamamu selama 14 hari.",
                        targetAmount = 500000.0,
                        currentAmount = 0.0,
                        durationDays = 14,
                        isActive = false,
                        contributionPerStep = 35000.0,
                        completedSteps = 0,
                        totalSteps = 14,
                        codeKey = "sprint"
                    )
                )
            }
        }
    }
}
