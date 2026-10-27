package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.InvoiceViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: InvoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.appSettings.collectAsState()
            val isDark = settings.isDarkMode

            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                viewModel.toastMessage.collectLatest { message ->
                    snackbarHostState.showSnackbar(message)
                }
            }

            MyApplicationTheme(darkTheme = isDark) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ImmersiveBackground,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNewInvoice = {
                                    navController.navigate("editor")
                                },
                                onEditInvoice = {
                                    navController.navigate("editor")
                                },
                                onViewInvoice = { invoiceWithItems ->
                                    viewModel.loadInvoiceForEditing(invoiceWithItems)
                                    navController.navigate("preview")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onNavigateToCatalog = {
                                    navController.navigate("catalog")
                                },
                                onNavigateToBusinesses = {
                                    navController.navigate("businesses")
                                },
                                onNavigateToAdmin = {
                                    navController.navigate("admin")
                                }
                            )
                        }

                        composable("editor") {
                            InvoiceEditorScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onSavedNavigate = { navController.popBackStack() },
                                onPreview = { navController.navigate("preview") }
                            )
                        }

                        composable("preview") {
                            InvoicePreviewScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onEdit = { navController.navigate("editor") }
                            )
                        }

                        composable("catalog") {
                            CatalogItemsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("businesses") {
                            BusinessManagementScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToCatalog = { navController.navigate("catalog") },
                                onNavigateToBusinesses = { navController.navigate("businesses") },
                                onNavigateToAdmin = { navController.navigate("admin") }
                            )
                        }

                        composable("admin") {
                            AdminDashboardScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToCatalog = { navController.navigate("catalog") },
                                onNavigateToBusinesses = { navController.navigate("businesses") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                    }
                }
            }
        }
    }
}
