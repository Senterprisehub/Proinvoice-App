package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InvoiceStatus
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.viewmodel.InvoiceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: InvoiceViewModel,
    onBack: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToBusinesses: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val invoices by viewModel.allInvoices.collectAsState()
    val catalogItems by viewModel.catalogItems.collectAsState()
    val businessProfiles by viewModel.businessProfiles.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    val curr = settings.currency

    // Analytics calculations
    val totalRevenue = remember(invoices) { invoices.sumOf { it.invoice.grandTotal } }
    val paidRevenue = remember(invoices) { invoices.filter { it.invoice.status == InvoiceStatus.PAID }.sumOf { it.invoice.grandTotal } }
    val pendingRevenue = remember(invoices) { invoices.filter { it.invoice.status == InvoiceStatus.SENT || it.invoice.status == InvoiceStatus.DRAFT }.sumOf { it.invoice.grandTotal } }
    val overdueRevenue = remember(invoices) { invoices.filter { it.invoice.status == InvoiceStatus.OVERDUE }.sumOf { it.invoice.grandTotal } }
    val avgInvoice = remember(invoices) { if (invoices.isNotEmpty()) totalRevenue / invoices.size else 0.0 }
    val collectionRate = remember(totalRevenue, paidRevenue) { if (totalRevenue > 0) (paidRevenue / totalRevenue) * 100 else 0.0 }

    Scaffold(
        containerColor = ImmersiveBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Analytics, contentDescription = null, tint = ImmersivePrimary)
                        Text("Executive Analytics Hub", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ImmersiveTextPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ImmersiveTextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("admin_theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (settings.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (settings.isDarkMode) ImmersivePending else ImmersivePrimary
                        )
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
            // Analytics Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = BorderStroke(1.dp, ImmersiveOutline)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ImmersivePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = ImmersiveOnPrimary, modifier = Modifier.size(26.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "BUSINESS INTELLIGENCE",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                    color = ImmersivePrimary
                                )
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ImmersivePaid))
                            }
                            Text(
                                text = "Financial Performance & Analytics",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ImmersiveTextPrimary
                            )
                            Text(
                                text = "Real-time metrics, collection rates, and catalog telemetry",
                                fontSize = 12.sp,
                                color = ImmersiveTextSecondary
                            )
                        }
                    }
                }
            }

            // Financial KPIs
            item {
                Text(
                    text = "FINANCIAL OVERVIEW",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.8.sp,
                    color = ImmersiveTextSecondary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Gross Invoiced",
                        value = "$curr${String.format(Locale.US, "%.2f", totalRevenue)}",
                        subtitle = "${invoices.size} Invoices",
                        icon = Icons.Default.MonetizationOn,
                        color = ImmersivePrimary,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Total Collected",
                        value = "$curr${String.format(Locale.US, "%.2f", paidRevenue)}",
                        subtitle = "${String.format(Locale.US, "%.1f", collectionRate)}% Paid Rate",
                        icon = Icons.Default.CheckCircle,
                        color = ImmersivePaid,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Pending (Draft/Sent)",
                        value = "$curr${String.format(Locale.US, "%.2f", pendingRevenue)}",
                        subtitle = "${invoices.count { it.invoice.status == InvoiceStatus.SENT || it.invoice.status == InvoiceStatus.DRAFT }} Pending",
                        icon = Icons.Default.HourglassTop,
                        color = ImmersivePending,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Overdue",
                        value = "$curr${String.format(Locale.US, "%.2f", overdueRevenue)}",
                        subtitle = "${invoices.count { it.invoice.status == InvoiceStatus.OVERDUE }} Overdue",
                        icon = Icons.Default.Warning,
                        color = ImmersiveOverdue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // System Metrics Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = BorderStroke(1.dp, ImmersiveOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CATALOG & SYSTEM HEALTH",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp,
                            color = ImmersivePrimary
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Average Invoice Value", color = ImmersiveTextSecondary, fontSize = 13.sp)
                            Text("$curr${String.format(Locale.US, "%.2f", avgInvoice)}", fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary)
                        }

                        HorizontalDivider(color = ImmersiveOutline.copy(alpha = 0.4f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Catalog Items Registered", color = ImmersiveTextSecondary, fontSize = 13.sp)
                            Text("${catalogItems.size} items", fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary)
                        }

                        HorizontalDivider(color = ImmersiveOutline.copy(alpha = 0.4f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Business Profiles Managed", color = ImmersiveTextSecondary, fontSize = 13.sp)
                            Text("${businessProfiles.size} profiles", fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary)
                        }
                    }
                }
            }

            // Quick Administration Shortcuts Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = BorderStroke(1.dp, ImmersiveOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ADMINISTRATIVE ACTIONS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp,
                            color = ImmersiveTextSecondary
                        )

                        Button(
                            onClick = onNavigateToBusinesses,
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersiveSecondaryContainer, contentColor = ImmersiveTextPrimary),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = ImmersivePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage Business Profiles", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onNavigateToCatalog,
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersiveSecondaryContainer, contentColor = ImmersiveTextPrimary),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = ImmersivePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage Catalog Items", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToSettings,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ImmersiveOutline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveTextPrimary),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = ImmersiveTextSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Global Billing & Tax Settings", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
