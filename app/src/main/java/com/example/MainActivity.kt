package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ResultState
import com.example.ui.TabunginViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.GreenDark
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TabunginAppShell()
            }
        }
    }
}

@Composable
fun TabunginAppShell() {
    val viewModel: TabunginViewModel = viewModel()
    var currentTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to ViewModel actions (Saving messages, goal milestones)
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ResultState.Success -> {
                    snackbarHostState.showSnackbar(
                        message = event.msg,
                        duration = SnackbarDuration.Short
                    )
                }
                is ResultState.Error -> {
                    snackbarHostState.showSnackbar(
                        message = "Oops! ${event.errorMsg}",
                        duration = SnackbarDuration.Short
                    )
                }
                is ResultState.GoalCompleted -> {
                    snackbarHostState.showSnackbar(
                        message = "Yeay! Kamu telah mencapai target '${event.goalName}'! 🎉 Kemas barang impianmu!",
                        duration = SnackbarDuration.Long
                    )
                }
                is ResultState.ChallengeStepSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = if (event.isChallengeCompleted) {
                            "🏆 Tantangan '${event.challengeTitle}' selesai! Luar biasa konsisten! 💚"
                        } else {
                            event.msg
                        },
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GreenDark,
                        indicatorColor = GreenDark
                    ),
                    modifier = Modifier.testTag("nav_home_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Wishlist") },
                    label = { Text("Wishlist", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GreenDark,
                        indicatorColor = GreenDark
                    ),
                    modifier = Modifier.testTag("nav_wishlist_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Tantangan") },
                    label = { Text("Tantangan", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GreenDark,
                        indicatorColor = GreenDark
                    ),
                    modifier = Modifier.testTag("nav_challenges_tab")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Alat Hemat") },
                    label = { Text("Alat Hemat", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GreenDark,
                        indicatorColor = GreenDark
                    ),
                    modifier = Modifier.testTag("nav_tools_tab")
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(viewModel = viewModel)
                1 -> WishlistScreen(viewModel = viewModel)
                2 -> ChallengesScreen(viewModel = viewModel)
                3 -> ToolsScreen(viewModel = viewModel)
            }
        }
    }
}
