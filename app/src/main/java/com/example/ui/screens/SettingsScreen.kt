package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InvoiceLayout
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.LiveTemplateGalleryDialog
import com.example.ui.theme.*
import com.example.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: InvoiceViewModel,
    onBack: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToBusinesses: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val invoices by viewModel.allInvoices.collectAsState()
    val catalogItems by viewModel.catalogItems.collectAsState()
    val businesses by viewModel.businessProfiles.collectAsState()

    var currency by remember(settings) { mutableStateOf(settings.currency) }
    var defaultCgst by remember(settings) { mutableStateOf(settings.defaultCgst.toString()) }
    var defaultSgst by remember(settings) { mutableStateOf(settings.defaultSgst.toString()) }
    var defaultNotes by remember(settings) { mutableStateOf(settings.defaultNotes) }
    var defaultLayout by remember(settings) { mutableStateOf(settings.defaultLayout) }
    var isDarkMode by remember(settings) { mutableStateOf(settings.isDarkMode) }

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showTemplateGallery by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showRestoreSuccessDialog by remember { mutableStateOf(false) }
    var restoreSuccessSummary by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }

    // File Pickers for Device-to-Device Migration
    val uploadFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importBackupFromUri(context, uri) { success, summary ->
                if (success) {
                    restoreSuccessSummary = summary
                    showRestoreSuccessDialog = true
                }
            }
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.saveBackupToUri(context, uri)
        }
    }

    val currencies = listOf("$", "₹", "€", "£", "¥", "C$", "A$", "CHF", "AED", "SAR")

    Scaffold(
        containerColor = ImmersiveBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Settings & Preferences", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ImmersiveTextPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ImmersiveTextPrimary)
                    }
                },
                actions = {
                    // Quick Theme Toggle in Top Bar
                    IconButton(
                        onClick = {
                            val newMode = !isDarkMode
                            isDarkMode = newMode
                            viewModel.setDarkMode(newMode)
                        },
                        modifier = Modifier.testTag("settings_theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkMode) ImmersivePending else ImmersivePrimary
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.updateSettings(
                                currency = currency,
                                defaultCgst = defaultCgst.toDoubleOrNull() ?: 0.0,
                                defaultSgst = defaultSgst.toDoubleOrNull() ?: 0.0,
                                defaultNotes = defaultNotes,
                                defaultLayout = defaultLayout,
                                isDarkMode = isDarkMode
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImmersivePrimary,
                            contentColor = ImmersiveOnPrimary
                        ),
                        modifier = Modifier.padding(end = 8.dp).testTag("save_settings_btn")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ImmersiveBackground)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ImmersiveBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme & Visual Appearance Chooser Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("theme_picker_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = BorderStroke(1.dp, ImmersiveOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = ImmersivePrimary)
                                Text(
                                    text = "THEME & VISUAL APPEARANCE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp,
                                    color = ImmersivePrimary
                                )
                            }

                            Surface(
                                color = if (isDarkMode) ImmersivePrimaryContainer else ImmersivePaidContainer,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, (if (isDarkMode) ImmersivePrimary else ImmersivePaid).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = if (isDarkMode) "🌙 DARK THEME" else "☀️ LIGHT THEME",
                                    color = if (isDarkMode) ImmersiveOnPrimaryContainer else ImmersivePaidText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Select your preferred appearance mode for comfortable billing and document creation.",
                            fontSize = 12.sp,
                            color = ImmersiveTextSecondary
                        )

                        // Dedicated Interactive Theme Buttons (Light Theme & Dark Theme)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Light Theme Button
                            OutlinedButton(
                                onClick = {
                                    isDarkMode = false
                                    viewModel.setDarkMode(false)
                                },
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(
                                    width = if (!isDarkMode) 2.dp else 1.dp,
                                    color = if (!isDarkMode) ImmersivePrimary else ImmersiveOutline
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!isDarkMode) ImmersivePrimaryContainer.copy(alpha = 0.25f) else ImmersiveSurfaceElevated,
                                    contentColor = ImmersiveTextPrimary
                                ),
                                modifier = Modifier.weight(1f).height(64.dp).testTag("choose_light_theme_btn"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (!isDarkMode) ImmersivePending.copy(alpha = 0.2f) else ImmersiveOutline.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.LightMode,
                                            contentDescription = null,
                                            tint = if (!isDarkMode) ImmersivePending else ImmersiveTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            "Light Theme",
                                            fontWeight = if (!isDarkMode) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = ImmersiveTextPrimary
                                        )
                                        Text(
                                            if (!isDarkMode) "Active ✓" else "Crisp & Clean",
                                            fontSize = 11.sp,
                                            color = if (!isDarkMode) ImmersivePrimary else ImmersiveTextSecondary
                                        )
                                    }
                                }
                            }

                            // Dark Theme Button
                            OutlinedButton(
                                onClick = {
                                    isDarkMode = true
                                    viewModel.setDarkMode(true)
                                },
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(
                                    width = if (isDarkMode) 2.dp else 1.dp,
                                    color = if (isDarkMode) ImmersivePrimary else ImmersiveOutline
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isDarkMode) ImmersivePrimaryContainer.copy(alpha = 0.25f) else ImmersiveSurfaceElevated,
                                    contentColor = ImmersiveTextPrimary
                                ),
                                modifier = Modifier.weight(1f).height(64.dp).testTag("choose_dark_theme_btn"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isDarkMode) ImmersivePrimary.copy(alpha = 0.2f) else ImmersiveOutline.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.DarkMode,
                                            contentDescription = null,
                                            tint = if (isDarkMode) ImmersivePrimary else ImmersiveTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            "Dark Theme",
                                            fontWeight = if (isDarkMode) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = ImmersiveTextPrimary
                                        )
                                        Text(
                                            if (isDarkMode) "Active ✓" else "Immersive Dark",
                                            fontSize = 11.sp,
                                            color = if (isDarkMode) ImmersivePrimary else ImmersiveTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Full Backup & Device-to-Device Data Migration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("backup_migration_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = BorderStroke(1.dp, ImmersiveOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.SyncAlt, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "DEVICE-TO-DEVICE BACKUP & RESTORE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp,
                                    color = ImmersivePrimary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (networkStatus.isOnline) ImmersivePaidContainer.copy(alpha = 0.5f) else ImmersivePrimaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (networkStatus.isOnline) ImmersivePaid.copy(alpha = 0.4f) else ImmersivePrimary.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (networkStatus.isOnline) ImmersivePaid else ImmersivePending)
                                    )
                                    Text(
                                        text = if (networkStatus.isOnline) "Online (${networkStatus.connectionType})" else "Offline Mode",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (networkStatus.isOnline) ImmersivePaidText else ImmersiveOnPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Informational summary banner
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ImmersiveSurfaceElevated,
                            border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Storage, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(16.dp))
                                    Text("Complete App Data & Styling Export", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ImmersiveTextPrimary)
                                }
                                Text(
                                    text = "Export your entire database (${invoices.size} Invoices, ${catalogItems.size} Items, ${businesses.size} Businesses) as a portable backup file. Upload this file on any new device to instantly restore all invoices with identical layouts, calculations, and dashboard styles.",
                                    fontSize = 11.sp,
                                    color = ImmersiveTextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        // Action 1: Download / Export Full Backup File
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("1. Download from this device:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ImmersiveTextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.exportAndDownloadBackupFile(context) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ImmersivePrimary,
                                        contentColor = ImmersiveOnPrimary
                                    ),
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("download_backup_btn")
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                        saveFileLauncher.launch("invoices_backup_$timeStamp.json")
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, ImmersiveOutline),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveTextPrimary),
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("save_to_storage_btn")
                                ) {
                                    Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save to Storage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Action 2: Upload / Restore Backup File on New Device
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("2. Restore / Upload on new device:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ImmersiveTextPrimary)
                            Button(
                                onClick = { uploadFileLauncher.launch("*/*") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ImmersivePaid,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("upload_backup_file_btn")
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload & Restore Backup File (.json)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Secondary Options: Manual Paste JSON & CSV Export
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    importJsonText = ""
                                    showImportDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ImmersiveOutline),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveTextPrimary),
                                modifier = Modifier.weight(1f).testTag("paste_json_btn")
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(15.dp), tint = ImmersivePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paste JSON", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }

                            OutlinedButton(
                                onClick = { viewModel.exportAndShareCsv(context) },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ImmersiveOutline),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveTextPrimary),
                                modifier = Modifier.weight(1f).testTag("export_csv_btn")
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp), tint = ImmersivePending)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Quick Data & Hub Navigation Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = BorderStroke(1.dp, ImmersiveOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "DATA MANAGEMENT & HUBS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = ImmersivePrimary
                        )

                        Button(
                            onClick = onNavigateToBusinesses,
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersiveSecondaryContainer, contentColor = ImmersiveTextPrimary),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = ImmersivePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🏢 Business Profiles Management", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onNavigateToCatalog,
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersiveSecondaryContainer, contentColor = ImmersiveTextPrimary),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = ImmersivePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📦 Items Catalog Management", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onNavigateToAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersiveSecondaryContainer, contentColor = ImmersiveTextPrimary),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = ImmersivePaid)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📊 Executive Analytics & Overview", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // General Preferences (Currency, Layout, Taxes)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = BorderStroke(1.dp, ImmersiveOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "BILLING DEFAULTS & LOCALIZATION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = ImmersivePrimary
                        )

                        // Currency Selector
                        Text("Active Currency Symbol", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ImmersiveTextPrimary)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(currencies) { c ->
                                val selected = currency == c
                                FilterChip(
                                    selected = selected,
                                    onClick = { currency = c },
                                    label = { Text(c, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ImmersivePrimary,
                                        selectedLabelColor = ImmersiveOnPrimary,
                                        containerColor = ImmersiveSurfaceElevated,
                                        labelColor = ImmersiveTextSecondary
                                    ),
                                    border = BorderStroke(1.dp, if (selected) ImmersivePrimary else ImmersiveOutline)
                                )
                            }
                        }

                        // Default Tax Percentages
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = defaultCgst,
                                onValueChange = { defaultCgst = it },
                                label = { Text("Default CGST %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImmersivePrimary,
                                    unfocusedBorderColor = ImmersiveOutline,
                                    focusedTextColor = ImmersiveTextPrimary,
                                    unfocusedTextColor = ImmersiveTextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).testTag("default_cgst_input")
                            )

                            OutlinedTextField(
                                value = defaultSgst,
                                onValueChange = { defaultSgst = it },
                                label = { Text("Default SGST %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImmersivePrimary,
                                    unfocusedBorderColor = ImmersiveOutline,
                                    focusedTextColor = ImmersiveTextPrimary,
                                    unfocusedTextColor = ImmersiveTextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).testTag("default_sgst_input")
                            )
                        }

                        // Default Layout Header with Live Preview Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Default Invoice Template", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ImmersiveTextPrimary)
                            TextButton(
                                onClick = { showTemplateGallery = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp), tint = ImmersivePrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Live Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ImmersivePrimary)
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(InvoiceLayout.entries) { layout ->
                                val selected = defaultLayout == layout
                                FilterChip(
                                    selected = selected,
                                    onClick = { defaultLayout = layout },
                                    label = { Text("${layout.icon} ${layout.label}") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ImmersivePrimary,
                                        selectedLabelColor = ImmersiveOnPrimary,
                                        containerColor = ImmersiveSurfaceElevated,
                                        labelColor = ImmersiveTextSecondary
                                    ),
                                    border = BorderStroke(1.dp, if (selected) ImmersivePrimary else ImmersiveOutline)
                                )
                            }
                        }

                        // Default Notes & Terms
                        Text("Default Terms & Notes", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ImmersiveTextPrimary)
                        OutlinedTextField(
                            value = defaultNotes,
                            onValueChange = { defaultNotes = it },
                            placeholder = { Text("Default payment terms...", color = ImmersiveTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveOutline,
                                focusedTextColor = ImmersiveTextPrimary,
                                unfocusedTextColor = ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("default_notes_input"),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
            }

            // Reset / Demo Data Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = BorderStroke(1.dp, ImmersiveOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "RESET & DEMO DATA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = ImmersiveOverdue
                        )
                        Text(
                            text = "Reset the local database to clean initial sample businesses, items catalog, and invoices.",
                            fontSize = 12.sp,
                            color = ImmersiveTextSecondary
                        )

                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveOverdue),
                            border = BorderStroke(1.dp, ImmersiveOverdue.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("reset_demo_data_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset to Initial Demo Data", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, ImmersiveOutline, RoundedCornerShape(24.dp)),
            title = { Text("Reset Database?", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will clear all current records and restore standard sample invoices, items, and businesses.", color = ImmersiveTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToSampleData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveOverdue, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Reset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = ImmersiveTextSecondary)
                }
            }
        )
    }

    if (showTemplateGallery) {
        LiveTemplateGalleryDialog(
            currentLayout = defaultLayout,
            onSelectLayout = { selected -> defaultLayout = selected },
            onDismiss = { showTemplateGallery = false }
        )
    }

    // Success Restore Summary Dialog
    if (showRestoreSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreSuccessDialog = false },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, ImmersivePaid.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ImmersivePaid,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    "Data Restored Successfully! 🎉",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = ImmersiveTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        restoreSuccessSummary,
                        fontSize = 13.sp,
                        color = ImmersiveTextPrimary
                    )
                    Text(
                        "All invoices, items, business profiles, and template styles have been imported. Your dashboard is now updated with the exact same data and visual layout.",
                        fontSize = 12.sp,
                        color = ImmersiveTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreSuccessDialog = false
                        onBack() // Navigate to dashboard to see updated data immediately
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersivePaid,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("view_dashboard_after_restore_btn")
                ) {
                    Text("Go to Dashboard", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreSuccessDialog = false }) {
                    Text("Stay in Settings", color = ImmersiveTextSecondary)
                }
            }
        )
    }

    // JSON Restore / Import Dialog (Manual text paste option)
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, ImmersiveOutline, RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ImmersivePrimary)
                    Text("Restore Database from JSON", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Paste your JSON backup payload below. This will safely restore all invoices, catalog items, and business profiles.",
                        fontSize = 12.sp,
                        color = ImmersiveTextSecondary
                    )
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("{\n  \"version\": 2,\n  \"invoices\": [...]\n}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("import_json_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ImmersivePrimary,
                            unfocusedBorderColor = ImmersiveOutline,
                            focusedTextColor = ImmersiveTextPrimary,
                            unfocusedTextColor = ImmersiveTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            val ok = viewModel.importBackupFromJson(importJsonText.trim())
                            if (ok) {
                                showImportDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary, contentColor = ImmersiveOnPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("confirm_import_btn")
                ) {
                    Text("Restore Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = ImmersiveTextSecondary)
                }
            }
        )
    }
}

