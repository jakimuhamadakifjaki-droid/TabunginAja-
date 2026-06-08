package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavingGoal
import com.example.data.SavingTransaction
import com.example.data.ResultState
import com.example.ui.TabunginViewModel
import com.example.ui.components.ProgressVisualizerSelector
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPastel
import com.example.ui.theme.LemonSoft
import com.example.ui.theme.LightGray
import com.example.ui.theme.TextDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Rupiah Formatter
fun formatRupiah(amount: Double): String {
    return "Rp " + String.format("%,.0f", amount).replace(",", ".")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TabunginViewModel,
    modifier: Modifier = Modifier
) {
    val trackedGoal by viewModel.currentlyTrackedGoal.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val activeChallenges by viewModel.activeChallenges.collectAsState()
    val allGoals by viewModel.allGoals.collectAsState()

    var showSavingDialog by remember { mutableStateOf(false) }
    var showGoalPicker by remember { mutableStateOf(false) }
    var selectedVisualizerStyle by remember { mutableIntStateOf(1) } // Default to Plant Growth!

    val geminiTip by viewModel.geminiTip.collectAsState()
    val isGeneratingTip by viewModel.isGeneratingTip.collectAsState()

    LaunchedEffect(trackedGoal) {
        if (geminiTip.isEmpty() && trackedGoal != null) {
            viewModel.refreshGeminiTip(trackedGoal)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // App header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SELAMAT MENABUNG,",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = GreenDark,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sahabat! 🌱",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }
                
                // Beautiful Profile Avatar Badge (Mocked fajar emoji avatar from Sleek template)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GreenPastel)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🦊", fontSize = 22.sp)
                }
            }
        }

        // --- SECTION 1: Active Tracked Goal Card ---
        item {
            val goal = trackedGoal
            if (goal != null) {
                val progressFraction = (goal.savedAmount / goal.price).toFloat().coerceIn(0f, 1f)
                val remainingAmount = goal.price - goal.savedAmount

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("primary_target_card"),
                    colors = CardDefaults.cardColors(containerColor = GreenPastel),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title header of target
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TARGET UTAMA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GreenDark,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = goal.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextDark
                                )
                            }
                            // Sleek Priority / Fast Switcher Badge
                            val priorityLabel = when (goal.priority) {
                                1 -> "Prioritas Tinggi"
                                2 -> "Prioritas Sedang"
                                else -> "Prioritas Rendah"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.45f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = priorityLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual progress display
                        ProgressVisualizerSelector(
                            visualizerType = selectedVisualizerStyle,
                            progress = progressFraction,
                            category = goal.category,
                            modifier = Modifier
                                .testTag("visualizer_canvas")
                                .clip(RoundedCornerShape(24.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visualizer Switcher Bar (Highly integrated to match theme)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val styleNames = listOf("Pemberian 🎁", "Pohon 🌱", "Roket 🚀", "Misteri 🔒")
                            styleNames.forEachIndexed { index, name ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selectedVisualizerStyle == index) GreenDark else Color.Transparent)
                                        .clickable { selectedVisualizerStyle = index }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedVisualizerStyle == index) Color.White else GreenDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sleek Quick Stats (Semi-Transparent White Grid Boxes)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.65f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "TERKUMPUL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GreenDark.copy(alpha = 0.8f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatRupiah(goal.savedAmount),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextDark
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.65f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "KEKURANGAN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GreenDark.copy(alpha = 0.8f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatRupiah(remainingAmount),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (goal.isCompleted) GreenDark else Color(0xFFD94D41)
                                    )
                                }
                            }
                        }

                        // Switch Target Button (if there's more than 1 goal available)
                        if (allGoals.size > 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showGoalPicker = true }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ganti Target", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenDark)
                                    Icon(
                                        Icons.Filled.KeyboardArrowDown, 
                                        contentDescription = "Ganti Target",
                                        tint = GreenDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Simulated Time and Motivation Message
                        val avgWeeklySaving = 85000.0 // Realistic base weekly average
                        val weeksNeeded = if (avgWeeklySaving > 0 && remainingAmount > 0) {
                            kotlin.math.ceil(remainingAmount / avgWeeklySaving).toInt()
                        } else 0
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(LemonSoft.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = Color(0xFF7F7205),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (goal.isCompleted) {
                                    "Selamat! Dana 100% terkumpul untuk barang impianmu! 💚🎉"
                                } else {
                                    "Dengan rata-rata tabunganmu, target tercapai dalam kisaran $weeksNeeded minggu lagi!"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF5E5404),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sleek Black / Contrast Full-Width Action Button
                        Button(
                            onClick = { showSavingDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_tambah_tabungan"),
                            colors = ButtonDefaults.buttonColors(containerColor = TextDark),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("Tambah Setor Tabungan", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+", fontWeight = FontWeight.Light, fontSize = 18.sp, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                // Empty state tracker
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💡", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum Ada Target Aktif",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ayo buat target barang impianmu terlebih dulu di tab Wishlist untuk mulai mencatat tabungan virtual!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // --- SECTION 1B: Gemini Daily Financial Tips ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = GreenDark.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("gemini_tips_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GreenPastel.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✨", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GEMINI TIPS HARIAN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GreenDark,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Tips Pintar Keuangan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                        }

                        if (isGeneratingTip) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = GreenDark
                            )
                        } else {
                            IconButton(
                                onClick = { viewModel.refreshGeminiTip(trackedGoal) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Perbarui Tips",
                                    tint = GreenDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isGeneratingTip && geminiTip.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Menganalisis progress menabung...",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        val displayTip = geminiTip.ifEmpty {
                            "Mulai tentukan target impianmu dan tabung sedikit demi sedikit setiap hari secara konsisten! 🌱"
                        }
                        Text(
                            text = displayTip,
                            fontSize = 12.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // --- SECTION 2: Active Challenge Banner ---
        item {
            Text(
                text = "Tantangan Aktif 🔥",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (activeChallenges.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { /* Switch to challenges tab */ },
                    colors = CardDefaults.cardColors(containerColor = LightGray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌱", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tidak Ada Tantangan Aktif",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Ikuti tantangan untuk mempercepat pencapaian barang impianmu!",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        } else {
            items(activeChallenges.take(2)) { challenge ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Square Lemon Icon Badge (matches html mock beautifully)
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(LemonSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏆", fontSize = 20.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = challenge.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Langkah ${challenge.completedSteps}/${challenge.totalSteps} • Bonus +${formatRupiah(challenge.contributionPerStep)}",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { (challenge.completedSteps.toFloat() / challenge.totalSteps).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth(0.95f)
                                            .height(5.dp)
                                            .clip(CircleShape),
                                        color = GreenDark,
                                        trackColor = Color(0xFFF1F5F9)
                                    )
                                }
                            }
                            
                            // Sleek Play/Complete Step Action Button
                            IconButton(
                                onClick = {
                                    val trackedId = trackedGoal?.id
                                    if (trackedId != null) {
                                        viewModel.completeChallengeStep(challenge.id, trackedId)
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(GreenDark.copy(alpha = 0.15f))
                                    .size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow, 
                                    contentDescription = "Selesaikan Langkah",
                                    tint = GreenDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        // Sleek Bottom border-b-4 accent mimicking the html mockup
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(LemonSoft)
                        )
                    }
                }
            }
        }

        // --- SECTION 3: Recent Transactions Ledger ---
        item {
            Text(
                text = "Riwayat Menabung Terbaru 📝",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "Belum banyak riwayat tabungan. Masukkan setoran pertamamu!",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(transactions.take(10)) { tx ->
                TransactionItemRow(tx)
            }
        }
    }

    // Modal dialog for Tambah Tabungan
    if (showSavingDialog) {
        val selectedGoal = trackedGoal
        if (selectedGoal != null) {
            AddSavingDialog(
                goalName = selectedGoal.name,
                onDismiss = { showSavingDialog = false },
                onConfirm = { amount, note ->
                    viewModel.addSaving(selectedGoal.id, amount, note)
                    showSavingDialog = false
                }
            )
        }
    }

    // Dialog to pick quick tracked target
    if (showGoalPicker) {
        AlertDialog(
            onDismissRequest = { showGoalPicker = false },
            title = { Text("Pilih Target Utama 🎯", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allGoals.forEach { g ->
                        val selected = g.id == trackedGoal?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) GreenPastel.copy(alpha = 0.3f) else Color.Transparent)
                                .clickable {
                                    viewModel.selectTrackedGoal(g.id)
                                    showGoalPicker = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(g.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Terkumpul: ${formatRupiah(g.savedAmount)}", fontSize = 11.sp, color = Color.Gray)
                            }
                            if (selected) {
                                Icon(Icons.Filled.Check, contentDescription = "Selected", tint = GreenDark)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGoalPicker = false }) {
                    Text("Tutup", color = GreenDark)
                }
            }
        )
    }
}

@Composable
fun TransactionItemRow(tx: SavingTransaction) {
    val dateString = remember(tx.timestamp) {
        val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
        formatter.format(Date(tx.timestamp))
    }

    val iconType = when (tx.type) {
        "autosave" -> "⚡"
        "roundup" -> "🪙"
        "challenge" -> "🏆"
        else -> "💰"
    }

    val typeLabel = when (tx.type) {
        "autosave" -> "AutoSave"
        "roundup" -> "Round-Up"
        "challenge" -> "Tantangan"
        else -> "Manual"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Modern Rounded Corner Icon Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (tx.type) {
                                "autosave" -> Color(0xFFE3F2FD)
                                "roundup" -> LemonSoft.copy(alpha = 0.6f)
                                "challenge" -> GreenPastel.copy(alpha = 0.5f)
                                else -> Color(0xFFF1F8E9)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconType, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (tx.note.isNotBlank()) tx.note else "Tabungan ${tx.goalName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$typeLabel • $dateString",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = "+${formatRupiah(tx.amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = GreenDark
            )
        }
    }
}

@Composable
fun AddSavingDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var noteStr by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Tambah Tabungan Baru 💰", 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                color = GreenDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Menabung untuk: $goalName",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        errorMsg = ""
                    },
                    label = { Text("Jumlah Uang (Rp)") },
                    placeholder = { Text("Contoh: 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenDark,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_savings_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = { Text("Contoh: Hasil potong jajan") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenDark,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_savings_note_input"),
                    singleLine = true
                )

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMsg = "Masukkan jumlah nominal menabung yang valid!"
                    } else {
                        onConfirm(amount, noteStr)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark),
                modifier = Modifier.testTag("add_savings_submit_button")
            ) {
                Text("Simpan Tabungan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}
