package com.example.cardiosense.ui.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.cardiosense.ui.home.HomeScreen
import com.example.cardiosense.ui.onboarding.PermissionsScreen
import com.example.cardiosense.ui.profile.ProfileScreen
import com.example.cardiosense.ui.scanner.ScannerScreen
import com.example.cardiosense.ui.settings.SettingsScreen
import com.example.cardiosense.ui.theme.CardiosenseTheme
import com.example.cardiosense.ui.onboarding.PermissionManager
import kotlinx.coroutines.launch

// 1. Make AppScreen public so MainActivity can see it
enum class AppScreen(val title: String) {
    Permissions("Startup"),
    Scanner("Scanner"),
    Home("Live"),
    Profile("Profile"),
    Settings("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    startDestination: AppScreen = AppScreen.Permissions // 2. Add this parameter
) {
    val context = LocalContext.current
    var selectedScreen by remember { mutableStateOf(startDestination) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (selectedScreen == AppScreen.Permissions) {
        PermissionsScreen(
            onPermissionsGranted = { selectedScreen = AppScreen.Home }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier
                        .height(12.dp)
                        .width(1.dp)
                    )
                    Text("Menu", modifier = Modifier.padding(16.dp), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Home") },
                        selected = selectedScreen == AppScreen.Home,
                        onClick = {
                            selectedScreen = AppScreen.Home
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.BluetoothSearching, null) },
                        label = { Text("Scanner") },
                        selected = selectedScreen == AppScreen.Scanner,
                        onClick = {
                            selectedScreen = AppScreen.Scanner
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, null) },
                        label = { Text("Profile") },
                        selected = selectedScreen == AppScreen.Profile,
                        onClick = {
                            selectedScreen = AppScreen.Profile
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("Settings") },
                        selected = selectedScreen == AppScreen.Settings,
                        onClick = {
                            selectedScreen = AppScreen.Settings
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(selectedScreen.title) },
                        colors = TopAppBarDefaults.topAppBarColors(),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(modifier = modifier.fillMaxSize().padding(innerPadding)) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        when (selectedScreen) {
                            AppScreen.Scanner -> ScannerScreen()
                            AppScreen.Home -> HomeScreen()
                            AppScreen.Profile -> ProfileScreen()
                            AppScreen.Settings -> SettingsScreen()
                            else -> {}
                        }
                    }
                }
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
