package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Challenge
import com.example.ui.TabunginViewModel
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPastel
import com.example.ui.theme.LemonSoft
import com.example.ui.theme.TextDark

@Composable
fun ChallengesScreen(
    viewModel: TabunginViewModel,
    modifier: Modifier = Modifier
) {
    val challenges by viewModel.allChallenges.collectAsState()
    val trackedGoal by viewModel.currentlyTrackedGoal.collectAsState()

    val activeList = remember(challenges) { challenges.filter { it.isActive } }
    val recommendedList = remember(challenges) { challenges.filter { !it.isActive } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Page HEADER
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "MISI TABUNGIN,",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = GreenDark,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tantangan 🎯",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )
            }
        }

        // --- Active Challenges Section ---
        item {
            Text(
                text = "Tantangan Aktif 🔥 (${activeList.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark
            )
        }

        if (activeList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌱", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum Ada Misi Aktif",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Lihat daftar rekomendasi di bawah dan aktifkan tantangan pertamamu!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(activeList) { challenge ->
                ActiveChallengeCard(
                    challenge = challenge,
                    activeTrackedGoalName = trackedGoal?.name,
                    onStepCompleted = {
                        val mainId = trackedGoal?.id
                        if (mainId != null) {
                            viewModel.completeChallengeStep(challenge.id, mainId)
                        }
                    },
                    onDeactivate = { viewModel.toggleChallengeActive(challenge.id, false) }
                )
            }
        }

        // --- Recommended / New Challenges Section ---
        item {
            Text(
                text = "Rekomendasi Tantangan Baru ✨",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (recommendedList.isEmpty()) {
            item {
                Text(
                    text = "Hebat! Kamu telah mengaktifkan seluruh tantangan yang tersedia! 🏆",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(recommendedList) { challenge ->
                RecommendedChallengeCard(
                    challenge = challenge,
                    onActivate = { viewModel.toggleChallengeActive(challenge.id, true) },
                    onReset = { viewModel.resetChallenge(challenge.id) }
                )
            }
        }
    }
}

@Composable
fun ActiveChallengeCard(
    challenge: Challenge,
    activeTrackedGoalName: String?,
    onStepCompleted: () -> Unit,
    onDeactivate: () -> Unit
) {
    val progressFraction = (challenge.completedSteps.toFloat() / challenge.totalSteps).coerceIn(0f, 1f)
    val percentageLabel = "${(progressFraction * 100).toInt()}%"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_callenge_${challenge.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Misi: ${challenge.durationDays} Hari".uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = GreenDark,
                        letterSpacing = 0.5.sp
                    )
                }
                
                IconButton(
                    onClick = onDeactivate,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh, 
                        contentDescription = "Hentikan Misi", 
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = challenge.description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Progress stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TERKUMPUL DARI TANTANGAN", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatRupiah(challenge.currentAmount)} / ${formatRupiah(challenge.targetAmount)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }
                Text(
                    text = "$percentageLabel Selesai",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = GreenDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Linear Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .clip(CircleShape)
                        .background(GreenDark)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom CTA row (Contribute Step)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Setoran Berikutnya", 
                        fontSize = 9.sp, 
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "+${formatRupiah(challenge.contributionPerStep)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = GreenDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Langkah ${challenge.completedSteps}/${challenge.totalSteps}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (activeTrackedGoalName != null) {
                    Button(
                        onClick = onStepCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = TextDark),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Setor Ke Target", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFECEFF1))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Pilih target di beranda", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedChallengeCard(
    challenge: Challenge,
    onActivate: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Durasi: ${challenge.durationDays} Hari • Selesai: ${challenge.completedSteps}/${challenge.totalSteps} langkah",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (challenge.completedSteps > 0) {
                    IconButton(
                        onClick = onReset, 
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Simpan Ulang", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = challenge.description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL TARGET", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatRupiah(challenge.targetAmount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }

                Button(
                    onClick = onActivate,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPastel.copy(alpha = 0.5f), contentColor = GreenDark),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (challenge.completedSteps > 0) "Mulai Lagi" else "Ikuti Misi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = GreenDark
                    )
                }
            }
        }
    }
}
