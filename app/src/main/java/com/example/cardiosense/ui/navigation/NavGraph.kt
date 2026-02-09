package com.example.cardiosense.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cardiosense.ui.home.HomeScreen
import com.example.cardiosense.ui.onboarding.PermissionsScreen
import com.example.cardiosense.ui.profile.ProfileScreen
import com.example.cardiosense.ui.scanner.ScannerScreen
import com.example.cardiosense.ui.theme.CardiosenseTheme

private enum class AppScreen(val title: String) {
    Permissions("Startup"),
    Scanner("Scanner"),
    Home("Live"),
    Profile("Profile")
}

// Navigation host and routes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    var selectedScreen by remember { mutableStateOf(AppScreen.Permissions) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "CardioSense") },
            colors = TopAppBarDefaults.topAppBarColors()
        )
        TabRow(selectedTabIndex = selectedScreen.ordinal) {
            AppScreen.values().forEachIndexed { index, screen ->
                Tab(
                    selected = selectedScreen.ordinal == index,
                    onClick = { selectedScreen = screen },
                    text = { Text(text = screen.title) }
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedScreen) {
                AppScreen.Permissions -> PermissionsScreen(
                    onPermissionsGranted = { selectedScreen = AppScreen.Scanner }
                )
                AppScreen.Scanner -> ScannerScreen()
                AppScreen.Home -> HomeScreen()
                AppScreen.Profile -> ProfileScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavGraphPreview() {
    CardiosenseTheme {
        NavGraph(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NavGraphDarkPreview() {
    CardiosenseTheme {
        NavGraph(modifier = Modifier.fillMaxSize())
    }
}
