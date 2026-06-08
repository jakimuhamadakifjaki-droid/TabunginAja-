package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TabunginViewModel
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPastel
import com.example.ui.theme.LemonSoft
import com.example.ui.theme.TextDark
import kotlin.math.ceil

@Composable
fun ToolsScreen(
    viewModel: TabunginViewModel,
    modifier: Modifier = Modifier
) {
    val trackedGoal by viewModel.currentlyTrackedGoal.collectAsState()

    // Autosave state
    var incomeStr by remember { mutableStateOf("") }
    var autoSavePercentage by remember { mutableDoubleStateOf(10.0) }
    var autoSaveCalculated by remember { mutableDoubleStateOf(0.0) }

    // Round-up state
    var expenseStr by remember { mutableStateOf("") }
    var roundOptionIndex by remember { mutableIntStateOf(2) } // 1: nearest Rp1.000, 2: nearest Rp5.000, 3: nearest Rp10.000. Default 5.000
    var roundUpCalculated by remember { mutableDoubleStateOf(0.0) }
    var roundedTotalState by remember { mutableDoubleStateOf(0.0) }

    // Live calculations wrapper
    LaunchedEffect(incomeStr, autoSavePercentage) {
        val inc = incomeStr.toDoubleOrNull() ?: 0.0
        autoSaveCalculated = inc * (autoSavePercentage / 100.0)
    }

    LaunchedEffect(expenseStr, roundOptionIndex) {
        val exp = expenseStr.toDoubleOrNull() ?: 0.0
        val divisor = when (roundOptionIndex) {
            1 -> 1000.0
            2 -> 5000.0
            3 -> 10000.0
            else -> 1000.0
        }
        val rounded = ceil(exp / divisor) * divisor
        roundedTotalState = rounded
        roundUpCalculated = rounded - exp
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Page Header
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "STRATEGI OTOMATIS,",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = GreenDark,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tabung Otomatis ⚙️",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = TextDark
                )
            }
        }

        // Selected Target Tracker Card (Warning/Notice)
        item {
            val goal = trackedGoal
            if (goal == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Belum ada target utama terpilih. Silakan buat atau pilih target di tab Wishlist untuk menggunakan strategi kalkulator ini.",
                            fontSize = 11.sp,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Black,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GreenPastel.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎯", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "STRATEGI TABUNGAN UNTUK TARGET UTAMA:",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                goal.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TextDark
                            )
                        }
                    }
                }
            }
        }

        // --- STRATEGY 1: AutoSave from Income ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("autosave_config_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "1. AutoSave Pemasukan ⚡",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sisihkan porsi tabungan secara otomatis setiap kali kamu mendapat bonus, gaji, atau pemasukan baru.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input monthly payout
                    OutlinedTextField(
                        value = incomeStr,
                        onValueChange = { incomeStr = it },
                        label = { Text("Jumlah Nominal Pemasukan Baru (Rp)") },
                        placeholder = { Text("Contoh: 1500000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("autosave_income_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ratio selectors selector: 1%, 5%, 10%, 20%, 30%
                    Text(
                        text = "Persentase AutoSave", 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val percentageOptions = listOf(1.0, 5.0, 10.0, 20.0, 30.0)
                        percentageOptions.forEach { pct ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (autoSavePercentage == pct) GreenDark else Color(0xFFF1F5F9))
                                    .clickable { autoSavePercentage = pct }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${pct.toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (autoSavePercentage == pct) Color.White else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live preview section
                    if (autoSaveCalculated > 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(LemonSoft.copy(alpha = 0.5f))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Penyisihan Virtual:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(
                                    text = formatRupiah(autoSaveCalculated),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GreenDark
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Dihitung otomatis (${autoSavePercentage.toInt()}%) dari pemasukan Rp " + String.format("%,.0f", (incomeStr.toDoubleOrNull() ?: 0.0)),
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val targetGoal = trackedGoal
                        Button(
                            onClick = {
                                if (targetGoal != null) {
                                    viewModel.addAutoSave(targetGoal.id, incomeStr.toDoubleOrNull() ?: 0.0, autoSavePercentage)
                                    incomeStr = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TextDark),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Terapkan AutoSave Ke Target", fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }

        // --- STRATEGY 2: Round-Up Change Strategy ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("roundup_config_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "2. Round-Up Saving 🪙",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Bulatkan pembelanjaan harian kamu ke atas kelipatan terdekat dan tabung uang selisih kembaliannya secara instan.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input shopping bill
                    OutlinedTextField(
                        value = expenseStr,
                        onValueChange = { expenseStr = it },
                        label = { Text("Jumlah Pembayaran Belanja (Rp)") },
                        placeholder = { Text("Contoh: 18500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenDark),
                        modifier = Modifier.fillMaxWidth().testTag("roundup_expense_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Round-up Options selectors
                    Text(
                        text = "Bulatkan Ke Kelipatan Terdekat:", 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val options = listOf(
                            Triple(1, "Rp 1.000 🏷️", "Ribuan"),
                            Triple(2, "Rp 5.000 ⚡", "Lima Ribu"),
                            Triple(3, "Rp 10.000 🪙", "Sepuluh Ribu")
                        )
                        options.forEach { opt ->
                            val isSelected = roundOptionIndex == opt.first
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) GreenDark else Color(0xFFF1F5F9))
                                    .clickable { roundOptionIndex = opt.first }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = opt.second,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) Color.White else Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = opt.third,
                                        fontSize = 8.sp,
                                        color = if (isSelected) LemonSoft else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live calculation layout
                    if (roundUpCalculated > 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(LemonSoft.copy(alpha = 0.5f))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "Beralih: Belanja Rp " + String.format("%,.0f", (expenseStr.toDoubleOrNull() ?: 0.0)) + " dibulatkan menjadi Rp " + String.format("%,.0f", roundedTotalState),
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kembalian Ditabung:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text(
                                    text = formatRupiah(roundUpCalculated),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GreenDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val targetGoal = trackedGoal
                        Button(
                            onClick = {
                                if (targetGoal != null) {
                                    viewModel.addRoundUp(targetGoal.id, expenseStr.toDoubleOrNull() ?: 0.0, roundOptionIndex)
                                    expenseStr = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("roundup_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TextDark),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Asosiasikan Kembalian Ke Tabungan", fontWeight = FontWeight.Black, color = Color.White)
                        }
                    } else if (!expenseStr.isNullOrBlank() && roundUpCalculated <= 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3CD))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nominal belanja pas, tidak ada pembulatan ke atas.",
                                fontSize = 10.sp,
                                color = Color(0xFF856404),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
