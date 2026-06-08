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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavingGoal
import com.example.ui.TabunginViewModel
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPastel
import com.example.ui.theme.LemonSoft
import com.example.ui.theme.TextDark
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    viewModel: TabunginViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.allGoals.collectAsState()
    val trackedGoal by viewModel.currentlyTrackedGoal.collectAsState()
    var showCreateGoalDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Section Header
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
                        text = "DAFTAR IMPIAN,",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = GreenDark,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Wishlist 🌱",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }

                Button(
                    onClick = { showCreateGoalDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TextDark),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_tambah_target")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buat Baru", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }

        if (goals.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📦", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Wishlist Masih Kosong",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ayo tambahkan barang impian pertamamu dengan menekan tombol 'Buat Baru' di kanan atas!",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            items(goals) { goal ->
                val isTracked = goal.id == trackedGoal?.id
                WishlistGoalCard(
                    goal = goal,
                    isTracked = isTracked,
                    onSelectAsTracked = { viewModel.selectTrackedGoal(goal.id) },
                    onDelete = { viewModel.deleteGoal(goal) }
                )
            }
        }
    }

    if (showCreateGoalDialog) {
        CreateGoalDialog(
            onDismiss = { showCreateGoalDialog = false },
            onConfirm = { name, price, category, priority, deadline ->
                viewModel.insertGoal(name, price, category, priority, deadline)
                showCreateGoalDialog = false
            }
        )
    }
}

@Composable
fun WishlistGoalCard(
    goal: SavingGoal,
    isTracked: Boolean,
    onSelectAsTracked: () -> Unit,
    onDelete: () -> Unit
) {
    val progressFraction = (goal.savedAmount / goal.price).toFloat().coerceIn(0f, 1f)
    val formattedProgress = "${(progressFraction * 100).toInt()}%"
    val remaining = goal.price - goal.savedAmount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wish_item_card_${goal.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Category Emoji and Title, with Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GreenPastel.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getCategoryIcon(goal.category), fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = goal.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = goal.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Actions: Delete Target
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Hapus Target",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Savings and Price Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Terkumpul", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatRupiah(goal.savedAmount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = GreenDark
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Harga Barang", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatRupiah(goal.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
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
                        .background(if (goal.isCompleted) GreenDark else GreenPastel)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badges & Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Priority Badge
                    val priorityColorLabel = getPriorityColorAndLabel(goal.priority)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(priorityColorLabel.first.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = priorityColorLabel.second,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColorLabel.first
                        )
                    }

                    // Deadline Badge (if set)
                    if (goal.targetDate != null) {
                        val formattedDate = remember(goal.targetDate) {
                            val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                            formatter.format(Date(goal.targetDate))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = formattedDate,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Select Tracked Target Button
                if (isTracked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GreenDark)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Utama", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                } else if (!goal.isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.2.dp, GreenDark, RoundedCornerShape(12.dp))
                            .clickable { onSelectAsTracked() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Pantau 🎯",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = GreenDark
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LemonSoft)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Tercapai 🎉",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF6F6300)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, Int, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Laptop") }
    var priority by remember { mutableIntStateOf(1) } // 1: Tinggi, 2: Sedang, 3: Santai
    var deadlineDays by remember { mutableStateOf("") } // Deadline in future days
    var errorMsg by remember { mutableStateOf("") }

    val categories = listOf(
        "Laptop", "Smartphone", "Motor", "Kamera", "Liburan", 
        "Pendidikan", "Hobi", "Konser", "Furniture", "Lainnya"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mulai Target Impian Baru 🏆", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenDark) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMsg = "" },
                        label = { Text("Nama Barang Impian") },
                        placeholder = { Text("Contoh: Smart TV 42\"") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenDark),
                        modifier = Modifier.fillMaxWidth().testTag("target_name_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it; errorMsg = "" },
                        label = { Text("Harga Sasaran (Rp)") },
                        placeholder = { Text("Contoh: 3500000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenDark),
                        modifier = Modifier.fillMaxWidth().testTag("target_price_input"),
                        singleLine = true
                    )
                }

                item {
                    Text("Kategori Barang", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            var expandedCatMenu by remember { mutableStateOf(false) }
                            Button(
                                onClick = { expandedCatMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPastel.copy(alpha = 0.3f), contentColor = GreenDark)
                            ) {
                                Text("${category} ${getCategoryIcon(category)}")
                            }
                            DropdownMenu(
                                expanded = expandedCatMenu,
                                onDismissRequest = { expandedCatMenu = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text("$cat ${getCategoryIcon(cat)}") },
                                        onClick = {
                                            category = cat
                                            expandedCatMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Prioritas Pembelian", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val prioritasList = listOf(Triple(1, "Utama 🔥", Color(0xFFD9534F)), Triple(2, "Menengah ⭐️", Color(0xFF428BCA)), Triple(3, "Santai 🕒", Color(0xFF5CB85C)))
                        prioritasList.forEach { p ->
                            Button(
                                onClick = { priority = p.first },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (priority == p.first) p.third else Color(0xFFF0F4F1),
                                    contentColor = if (priority == p.first) Color.White else Color.DarkGray
                                )
                            ) {
                                Text(p.second, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = deadlineDays,
                        onValueChange = { deadlineDays = it },
                        label = { Text("Tenggat Waktu Opsional (Hari dari sekarang)") },
                        placeholder = { Text("Contoh: 90") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenDark),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (errorMsg.isNotEmpty()) {
                    item {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorMsg = "Nama barang impian tidak boleh kosong!"
                    } else if (price == null || price <= 0) {
                        errorMsg = "Masukkan nominal harga target barang yang valid!"
                    } else {
                        val offsetDays = deadlineDays.toIntOrNull()
                        val deadlineTimestamp = if (offsetDays != null && offsetDays > 0) {
                            System.currentTimeMillis() + (offsetDays * 86400000L)
                        } else null
                        onConfirm(name, price, category, priority, deadlineTimestamp)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark),
                modifier = Modifier.testTag("target_submit_button")
            ) {
                Text("Buat Target")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}

fun getPriorityColorAndLabel(priority: Int): Pair<Color, String> {
    return when (priority) {
        1 -> Pair(Color(0xFFD9534F), "Prioritas: UTAMA")
        2 -> Pair(Color(0xFF2B80BC), "Prioritas: MENENGAH")
        else -> Pair(Color(0xFF2E8231), "Prioritas: SANTAI")
    }
}

fun getCategoryIcon(category: String): String {
    return when (category.lowercase()) {
        "smartphone" -> "📱"
        "laptop" -> "💻"
        "motor" -> "🏍️"
        "kamera" -> "📷"
        "liburan" -> "✈️"
        "pendidikan" -> "🎓"
        "hobi" -> "🎮"
        "konser" -> "🎟️"
        "furniture" -> "🛏️"
        else -> "🎁"
    }
}
