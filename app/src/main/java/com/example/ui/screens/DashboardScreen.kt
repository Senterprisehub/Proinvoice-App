package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.InvoiceWithItems
import com.example.model.InvoiceStatus
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.PdfPrintUtil
import com.example.viewmodel.InvoiceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: InvoiceViewModel,
    onNewInvoice: () -> Unit,
    onEditInvoice: (InvoiceWithItems) -> Unit,
    onViewInvoice: (InvoiceWithItems) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToBusinesses: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val invoices by viewModel.allInvoices.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    var showNetworkInfoDialog by remember { mutableStateOf(false) }
    var invoiceToDelete by remember { mutableStateOf<InvoiceWithItems?>(null) }

    val curr = settings.currency

    // Filter invoices
    val filteredInvoices = remember(invoices, statusFilter, searchQuery) {
        invoices.filter { item ->
            val inv = item.invoice
            val matchesStatus = statusFilter == null || inv.status == statusFilter
            val matchesQuery = searchQuery.isBlank() ||
                    inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                    inv.billTo.contains(searchQuery, ignoreCase = true) ||
                    inv.billFrom.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesQuery
        }
    }

    // Totals for summary cards
    val totalRevenue = remember(invoices) { invoices.sumOf { it.invoice.grandTotal } }
    val paidRevenue = remember(invoices) { invoices.filter { it.invoice.status == InvoiceStatus.PAID }.sumOf { it.invoice.grandTotal } }

    Scaffold(
        containerColor = ImmersiveBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ImmersivePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = ImmersiveOnPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ProInvoice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ImmersiveTextPrimary
                            )
                            Text(
                                text = "INVOICE & BILLING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = ImmersivePrimary
                            )
                        }
                    }
                },
                actions = {
                    // Live Network Status Badge (Offline & Online indicator)
                    Surface(
                        onClick = { showNetworkInfoDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = if (networkStatus.isOnline) ImmersivePaidContainer.copy(alpha = 0.6f) else ImmersivePrimaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(
                            1.dp,
                            if (networkStatus.isOnline) ImmersivePaid.copy(alpha = 0.5f) else ImmersivePrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(end = 4.dp).testTag("network_status_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (networkStatus.isOnline) ImmersivePaid else ImmersivePending)
                            )
                            Text(
                                text = if (networkStatus.isOnline) "Online" else "Offline",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (networkStatus.isOnline) ImmersivePaidText else ImmersiveOnPrimaryContainer
                            )
                        }
                    }

                    // Quick Theme Toggle Button (Light / Dark Mode)
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("toggle_theme_btn")
                    ) {
                        Icon(
                            imageVector = if (settings.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (settings.isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = if (settings.isDarkMode) ImmersivePending else ImmersivePrimary
                        )
                    }

                    // Direct Executive / Analytics Hub
                    IconButton(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.testTag("admin_dashboard_btn")
                    ) {
                        Icon(
                            Icons.Filled.Analytics,
                            contentDescription = "Executive Analytics Hub",
                            tint = ImmersivePrimary
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_btn")
                    ) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = ImmersiveTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ImmersiveBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.startNewInvoice()
                    onNewInvoice()
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Invoice", fontWeight = FontWeight.Bold) },
                containerColor = ImmersivePrimary,
                contentColor = ImmersiveOnPrimary,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.testTag("create_invoice_fab")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ImmersiveBackground),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Stats Header Cards
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Total Invoiced",
                            value = "$curr${String.format(Locale.US, "%.2f", totalRevenue)}",
                            subtitle = "${invoices.size} Invoices",
                            icon = Icons.Default.Receipt,
                            color = ImmersivePrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Collected",
                            value = "$curr${String.format(Locale.US, "%.2f", paidRevenue)}",
                            subtitle = "${invoices.count { it.invoice.status == InvoiceStatus.PAID }} Paid",
                            icon = Icons.Default.CheckCircle,
                            color = ImmersivePaid,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Management Navigation Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToBusinesses,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ImmersiveSurface,
                                contentColor = ImmersiveTextPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("quick_biz_btn"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Businesses", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToCatalog,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ImmersiveSurface,
                                contentColor = ImmersiveTextPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("quick_items_btn"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Catalog", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToAdmin,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ImmersiveSurface,
                                contentColor = ImmersiveTextPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("quick_analytics_btn"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = ImmersivePaid, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analytics", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Seamless Offline & Online Capability Pill
                    Surface(
                        onClick = { showNetworkInfoDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = ImmersiveSurfaceElevated,
                        border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth().testTag("offline_online_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (networkStatus.isOnline) Icons.Default.CloudDone else Icons.Default.OfflineBolt,
                                    contentDescription = null,
                                    tint = if (networkStatus.isOnline) ImmersivePaid else ImmersivePending,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = if (networkStatus.isOnline) "🟢 Online (${networkStatus.connectionType})" else "⚡ Offline Mode Active",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = ImmersiveTextPrimary
                                    )
                                    Text(
                                        text = "100% Offline-First Database • Tap to View Sync & Backup Info",
                                        fontSize = 10.sp,
                                        color = ImmersiveTextSecondary
                                    )
                                }
                            }

                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Information",
                                tint = ImmersiveTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search by invoice # or client...", color = ImmersiveTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ImmersiveTextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = ImmersiveTextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ImmersiveSurface,
                            unfocusedContainerColor = ImmersiveSurface,
                            focusedBorderColor = ImmersivePrimary,
                            unfocusedBorderColor = ImmersiveOutline,
                            focusedTextColor = ImmersiveTextPrimary,
                            unfocusedTextColor = ImmersiveTextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("search_invoices_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isSelected = statusFilter == null
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setStatusFilter(null) },
                                label = { Text("All (${invoices.size})", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(18.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ImmersivePrimary,
                                    selectedLabelColor = ImmersiveOnPrimary,
                                    containerColor = ImmersiveSurface,
                                    labelColor = ImmersiveTextSecondary
                                ),
                                border = BorderStroke(1.dp, if (isSelected) ImmersivePrimary else ImmersiveOutline)
                            )
                        }
                        items(InvoiceStatus.entries) { status ->
                            val count = invoices.count { it.invoice.status == status }
                            val isSelected = statusFilter == status
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setStatusFilter(if (isSelected) null else status) },
                                label = { Text("${status.label} ($count)", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(18.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ImmersivePrimary,
                                    selectedLabelColor = ImmersiveOnPrimary,
                                    containerColor = ImmersiveSurface,
                                    labelColor = ImmersiveTextSecondary
                                ),
                                border = BorderStroke(1.dp, if (isSelected) ImmersivePrimary else ImmersiveOutline)
                            )
                        }
                    }
                }
            }

            // Invoice List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT INVOICES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp,
                        color = ImmersiveTextSecondary
                    )
                    Text(
                        text = "${filteredInvoices.size} found",
                        fontSize = 11.sp,
                        color = ImmersiveTextSecondary
                    )
                }
            }

            // Invoices or Empty State
            if (filteredInvoices.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                        border = BorderStroke(1.dp, ImmersiveOutline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(ImmersiveSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = ImmersivePrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (invoices.isEmpty()) "No invoices created yet" else "No matching invoices found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ImmersiveTextPrimary
                            )
                            Text(
                                text = "Create your first professional invoice with line items, tax, and PDF print.",
                                fontSize = 13.sp,
                                color = ImmersiveTextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = {
                                    viewModel.startNewInvoice()
                                    onNewInvoice()
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ImmersivePrimary,
                                    contentColor = ImmersiveOnPrimary
                                )
                            ) {
                                Text("+ Create Invoice Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredInvoices, key = { it.invoice.id }) { invoiceWithItems ->
                    InvoiceListItemCard(
                        invoiceWithItems = invoiceWithItems,
                        currency = curr,
                        onView = { onViewInvoice(invoiceWithItems) },
                        onEdit = {
                            viewModel.loadInvoiceForEditing(invoiceWithItems)
                            onEditInvoice(invoiceWithItems)
                        },
                        onPrint = {
                            PdfPrintUtil.printInvoice(context, invoiceWithItems)
                        },
                        onSharePdf = {
                            PdfPrintUtil.sharePdfFile(context, invoiceWithItems)
                        },
                        onShare = {
                            PdfPrintUtil.shareInvoiceText(context, invoiceWithItems)
                        },
                        onDelete = {
                            invoiceToDelete = invoiceWithItems
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    invoiceToDelete?.let { inv ->
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, ImmersiveOutline, RoundedCornerShape(24.dp)),
            title = { Text("Delete Invoice", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete invoice ${inv.invoice.invoiceNumber}? This action cannot be undone.", color = ImmersiveTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(inv.invoice.id)
                        invoiceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveOverdue, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) {
                    Text("Cancel", color = ImmersiveTextSecondary)
                }
            }
        )
    }

    // Offline & Online Architecture Info Dialog
    if (showNetworkInfoDialog) {
        AlertDialog(
            onDismissRequest = { showNetworkInfoDialog = false },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, ImmersiveOutline, RoundedCornerShape(24.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (networkStatus.isOnline) Icons.Default.CloudDone else Icons.Default.OfflineBolt,
                        contentDescription = null,
                        tint = if (networkStatus.isOnline) ImmersivePaid else ImmersivePending
                    )
                    Text("Offline & Online Modes", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = ImmersiveSurfaceElevated,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (networkStatus.isOnline) ImmersivePaid else ImmersivePending)
                            )
                            Column {
                                Text(
                                    text = "Current State: " + if (networkStatus.isOnline) "ONLINE (${networkStatus.connectionType})" else "OFFLINE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ImmersiveTextPrimary
                                )
                                Text(
                                    text = if (networkStatus.isOnline) "Full network features and cloud sharing enabled." else "100% features available offline without internet.",
                                    fontSize = 11.sp,
                                    color = ImmersiveTextSecondary
                                )
                            }
                        }
                    }

                    Text("⚡ What works OFFLINE:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ImmersivePrimary)
                    Text("• Creating, editing, duplicating & calculating invoices\n• On-device PDF generation & document rendering\n• Full access to Businesses & Catalog Items\n• Instant persistence to encrypted local SQLite/Room database", fontSize = 11.sp, color = ImmersiveTextPrimary)

                    Text("🌐 What works ONLINE:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ImmersivePaid)
                    Text("• Direct PDF & invoice sharing via Email, WhatsApp, Drive & Cloud\n• Exporting data backups to cloud destinations\n• Remote wireless network printing", fontSize = 11.sp, color = ImmersiveTextPrimary)

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.exportAndShareBackup(context)
                                showNetworkInfoDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Backup JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.exportAndShareCsv(context)
                                showNetworkInfoDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNetworkInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary, contentColor = ImmersiveOnPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun InvoiceListItemCard(
    invoiceWithItems: InvoiceWithItems,
    currency: String,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onPrint: () -> Unit,
    onSharePdf: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val inv = invoiceWithItems.invoice

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onView() }
            .testTag("invoice_card_${inv.invoiceNumber}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inv.invoiceNumber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = ImmersiveTextPrimary
                    )
                    Text(
                        text = "Due: ${inv.dueDateIso}",
                        fontSize = 11.sp,
                        color = ImmersiveTextSecondary
                    )
                }
                StatusBadge(status = inv.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Client billTo preview
            if (inv.billTo.isNotBlank()) {
                Text(
                    text = inv.billTo.lines().firstOrNull() ?: "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ImmersiveTextPrimary
                )
            }

            Text(
                text = "${invoiceWithItems.items.size} item(s) • ${inv.layout.label}",
                fontSize = 11.sp,
                color = ImmersiveTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ImmersiveOutline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currency${String.format(Locale.US, "%.2f", inv.grandTotal)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = ImmersivePrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onSharePdf, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = ImmersivePrimary, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onPrint, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Print, contentDescription = "Print", tint = ImmersiveTextSecondary, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = ImmersiveTextSecondary, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ImmersivePrimary, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ImmersiveOverdue, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}
