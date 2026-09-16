package com.rakshacast.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rakshacast.ui.screens.*
import com.rakshacast.ui.theme.*
import com.rakshacast.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object RiskMap : Screen("risk_map", "Risk Map", Icons.Default.Map)
    object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications)
    object Emergency : Screen("emergency", "Emergency", Icons.Default.HealthAndSafety)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Critical : Screen("critical", "Critical Warning", Icons.Default.Warning)
}

val items = listOf(
    Screen.Home,
    Screen.RiskMap,
    Screen.Alerts,
    Screen.Emergency,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RakshaCastApp(startDestination: String = Screen.Home.route, criticalHazard: String = "", criticalProb: Int = 0, criticalLoc: String = "", criticalRiskLevel: String = "SEVERE", criticalExplanation: String? = null, criticalRelief: String? = null, criticalArea: String? = null) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceWhite,
                contentColor = MutedText
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = WeatherMoisture.copy(alpha = 0.2f),
                            unselectedIconColor = NeutralGrey,
                            unselectedTextColor = NeutralGrey
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(viewModel, onNavigateToMap = { navController.navigate(Screen.RiskMap.route) }) }
            composable(Screen.RiskMap.route) { RiskMapScreen(viewModel) }
            composable(Screen.Alerts.route) { AlertsScreen(viewModel) }
            composable(Screen.Emergency.route) { EmergencyScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
            composable(Screen.Critical.route) {
                CriticalWarningScreen(
                    hazardType = criticalHazard,
                    probability = criticalProb,
                    location = criticalLoc,
                    riskLevel = criticalRiskLevel,
                    onDismiss = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}

