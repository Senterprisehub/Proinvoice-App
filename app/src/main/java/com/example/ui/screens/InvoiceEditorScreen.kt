package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.InvoiceWithItems
import com.example.data.local.entity.CatalogItemEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.model.AutoSaveStatus
import com.example.model.DiscountType
import com.example.model.InvoiceItem
import com.example.model.InvoiceLayout
import com.example.model.InvoiceStatus
import com.example.ui.components.LiveTemplateGalleryDialog
import com.example.ui.components.LiveTemplateSelectorBar
import com.example.ui.theme.*
import com.example.util.PdfPrintUtil
import com.example.viewmodel.InvoiceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceEditorScreen(
    viewModel: InvoiceViewModel,
    onBack: () -> Unit,
    onSavedNavigate: () -> Unit,
    onPreview: () -> Unit
) {
    val context = LocalContext.current
    val editorState by viewModel.editorState.collectAsState()
    val autoSaveStatus by viewModel.autoSaveStatus.collectAsState()
    val catalogItems by viewModel.catalogItems.collectAsState()
    val businessProfiles by viewModel.businessProfiles.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    val curr = editorState.currency

    var showBusinessDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showTemplateGallery by remember { mutableStateOf(false) }

    // Intercept hardware and gesture back to ensure latest progress is saved as draft
    BackHandler {
        viewModel.autoSaveDraftNow()
        onBack()
    }

    Scaffold(
        containerColor = ImmersiveBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (editorState.invoiceId == 0L) "New Invoice" else "Edit Invoice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = ImmersiveTextPrimary
                            )
                            // Auto-Save Status Pill
                            Surface(
                                onClick = { viewModel.autoSaveDraftNow() },
                                shape = RoundedCornerShape(8.dp),
                                color = when (autoSaveStatus) {
                                    is AutoSaveStatus.Saving -> ImmersivePending.copy(alpha = 0.18f)
                                    is AutoSaveStatus.Saved -> ImmersivePaid.copy(alpha = 0.18f)
                                    is AutoSaveStatus.Error -> ImmersiveOverdue.copy(alpha = 0.18f)
                                    AutoSaveStatus.Idle -> ImmersiveOutline.copy(alpha = 0.3f)
                                },
                                border = BorderStroke(
                                    0.5.dp,
                                    when (autoSaveStatus) {
                                        is AutoSaveStatus.Saving -> ImmersivePending.copy(alpha = 0.6f)
                                        is AutoSaveStatus.Saved -> ImmersivePaid.copy(alpha = 0.6f)
                                        is AutoSaveStatus.Error -> ImmersiveOverdue.copy(alpha = 0.6f)
                                        AutoSaveStatus.Idle -> ImmersiveOutline.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier.testTag("auto_save_status_pill")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    when (val status = autoSaveStatus) {
                                        is AutoSaveStatus.Saving -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(8.dp),
                                                strokeWidth = 1.5.dp,
                                                color = ImmersivePending
                                            )
                                            Text("Saving...", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = ImmersivePending)
                                        }
                                        is AutoSaveStatus.Saved -> {
                                            Icon(
                                                Icons.Default.CloudDone,
                                                contentDescription = null,
                                                tint = ImmersivePaid,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text("Draft Saved", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = ImmersivePaid)
                                        }
                                        is AutoSaveStatus.Error -> {
                                            Icon(
                                                Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = ImmersiveOverdue,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text("Retry Draft", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = ImmersiveOverdue)
                                        }
                                        AutoSaveStatus.Idle -> {
                                            Icon(
                                                Icons.Default.CloudQueue,
                                                contentDescription = null,
                                                tint = ImmersiveTextSecondary,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text("Auto-Save", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = ImmersiveTextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            text = when (val s = autoSaveStatus) {
                                is AutoSaveStatus.Saved -> "${editorState.invoiceNumber} • Draft saved at ${s.timeFormatted}"
                                is AutoSaveStatus.Saving -> "${editorState.invoiceNumber} • Auto-saving changes..."
                                else -> "${editorState.invoiceNumber} • Auto-save enabled"
                            },
                            fontSize = 11.sp,
                            color = ImmersiveTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.autoSaveDraftNow()
                            onBack()
                        },
                        modifier = Modifier.testTag("editor_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ImmersiveTextPrimary)
                    }
                },
                actions = {
                    // Preview action
                    IconButton(onClick = onPreview, modifier = Modifier.testTag("editor_preview_btn")) {
                        Icon(Icons.Outlined.Visibility, contentDescription = "Preview", tint = ImmersiveTextPrimary)
                    }
                    // Print action
                    IconButton(
                        onClick = {
                            val invEntity = InvoiceEntity(
                                id = editorState.invoiceId,
                                invoiceNumber = editorState.invoiceNumber,
                                dateIso = editorState.dateIso,
                                dueDateIso = editorState.dueDateIso,
                                poNumber = editorState.poNumber,
                                billFrom = editorState.billFrom,
                                billTo = editorState.billTo,
                                notes = editorState.notes,
                                discountValue = editorState.discountValue,
                                discountType = editorState.discountType,
                                status = editorState.status,
                                layout = editorState.layout,
                                currency = editorState.currency,
                                cgstPercent = editorState.cgstPercent,
                                sgstPercent = editorState.sgstPercent,
                                subtotal = editorState.subtotal,
                                discountAmount = editorState.discountAmount,
                                cgstAmount = editorState.cgstAmount,
                                sgstAmount = editorState.sgstAmount,
                                grandTotal = editorState.grandTotal
                            )
                            val itemEntities = editorState.items.map {
                                InvoiceItemEntity(
                                    id = it.id,
                                    invoiceId = editorState.invoiceId,
                                    description = it.description,
                                    quantity = it.quantity,
                                    rate = it.rate,
                                    taxableRate = it.taxable,
                                    total = it.total
                                )
                            }
                            PdfPrintUtil.printInvoice(context, InvoiceWithItems(invEntity, itemEntities))
                        },
                        modifier = Modifier.testTag("editor_print_btn")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print", tint = ImmersiveTextPrimary)
                    }
                    // Save action
                    Button(
                        onClick = {
                            viewModel.saveCurrentInvoice {
                                onSavedNavigate()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImmersivePrimary,
                            contentColor = ImmersiveOnPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp).testTag("editor_save_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
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
            // Header Info Card (Invoice #, Date, Due Date, Status, PO #)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = BorderStroke(1.dp, ImmersiveOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "INVOICE DETAILS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = ImmersivePrimary
                            )

                            // Status Dropdown
                            Box {
                                OutlinedButton(
                                    onClick = { showStatusDropdown = true },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, ImmersiveOutline),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveTextPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("status_select_btn")
                                ) {
                                    Text("Status: ${editorState.status.label}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = ImmersivePrimary)
                                }
                                DropdownMenu(
                                    expanded = showStatusDropdown,
                                    onDismissRequest = { showStatusDropdown = false }
                                ) {
                                    InvoiceStatus.entries.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status.label) },
                                            onClick = {
                                                viewModel.updateEditor { it.copy(status = status) }
                                                showStatusDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editorState.invoiceNumber,
                                onValueChange = { num -> viewModel.updateEditor { it.copy(invoiceNumber = num) } },
                                label = { Text("Invoice #") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImmersivePrimary,
                                    unfocusedBorderColor = ImmersiveOutline,
                                    focusedTextColor = ImmersiveTextPrimary,
                                    unfocusedTextColor = ImmersiveTextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).testTag("inv_number_input")
                            )

                            OutlinedTextField(
                                value = editorState.poNumber,
                                onValueChange = { po -> viewModel.updateEditor { it.copy(poNumber = po) } },
                                label = { Text("PO # (Optional)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImmersivePrimary,
                                    unfocusedBorderColor = ImmersiveOutline,
                                    focusedTextColor = ImmersiveTextPrimary,
                                    unfocusedTextColor = ImmersiveTextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).testTag("inv_po_input")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editorState.dateIso,
                                onValueChange = { d -> viewModel.updateEditor { it.copy(dateIso = d) } },
                                label = { Text("Invoice Date") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImmersivePrimary,
                                    unfocusedBorderColor = ImmersiveOutline,
                                    focusedTextColor = ImmersiveTextPrimary,
                                    unfocusedTextColor = ImmersiveTextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).testTag("inv_date_input")
                            )

                            OutlinedTextField(
                                value = editorState.dueDateIso,
                                onValueChange = { due -> viewModel.updateEditor { it.copy(dueDateIso = due) } },
                                label = { Text("Due Date") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImmersivePrimary,
                                    unfocusedBorderColor = ImmersiveOutline,
                                    focusedTextColor = ImmersiveTextPrimary,
                                    unfocusedTextColor = ImmersiveTextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).testTag("inv_due_input")
                            )
                        }
                    }
                }
            }

            // From & Bill To Cards
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = BorderStroke(1.dp, ImmersiveOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // From Header + Business Profile Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FROM (SENDER DETAILS)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = ImmersivePrimary
                            )

                            if (businessProfiles.isNotEmpty()) {
                                Box {
                                    FilledTonalButton(
                                        onClick = { showBusinessDropdown = true },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = ImmersiveSecondaryContainer,
                                            contentColor = ImmersiveTextPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.testTag("select_business_btn")
                                    ) {
                                        Text("🏢 Autofill Business", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = ImmersivePrimary)
                                    }

                                    DropdownMenu(
                                        expanded = showBusinessDropdown,
                                        onDismissRequest = { showBusinessDropdown = false }
                                    ) {
                                        businessProfiles.forEach { biz ->
                                            DropdownMenuItem(
                                                text = { Text(biz.name, fontWeight = FontWeight.Medium) },
                                                onClick = {
                                                    viewModel.applyBusinessToFrom(biz)
                                                    showBusinessDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = editorState.billFrom,
                            onValueChange = { from -> viewModel.updateEditor { it.copy(billFrom = from) } },
                            placeholder = { Text("Company name, address, tax ID, email, phone...", color = ImmersiveTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveOutline,
                                focusedTextColor = ImmersiveTextPrimary,
                                unfocusedTextColor = ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("bill_from_input"),
                            minLines = 3,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "BILL TO (CLIENT DETAILS)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = ImmersivePrimary
                        )

                        OutlinedTextField(
                            value = editorState.billTo,
                            onValueChange = { to -> viewModel.updateEditor { it.copy(billTo = to) } },
                            placeholder = { Text("Client name, address, email, contact info...", color = ImmersiveTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveOutline,
                                focusedTextColor = ImmersiveTextPrimary,
                                unfocusedTextColor = ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("bill_to_input"),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
            }

            // Items List Card Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LINE ITEMS (${editorState.items.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = ImmersiveTextSecondary
                    )

                    Button(
                        onClick = { viewModel.addItem() },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImmersivePrimary,
                            contentColor = ImmersiveOnPrimary
                        ),
                        modifier = Modifier.testTag("add_item_line_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Line Items
            itemsIndexed(editorState.items) { index, item ->
                LineItemEditorCard(
                    index = index,
                    item = item,
                    currency = curr,
                    catalogItems = catalogItems,
                    onItemChange = { transform -> viewModel.updateItem(index, transform) },
                    onSelectCatalogItem = { catItem -> viewModel.selectCatalogItemForLine(index, catItem) },
                    onRemove = { viewModel.removeItem(index) },
                    cgst = editorState.cgstPercent,
                    sgst = editorState.sgstPercent
                )
            }

            // Calculations & Discounts Card
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
                            text = "TAXES & SUMMARY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = ImmersivePrimary
                        )

                        // Subtotal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", color = ImmersiveTextSecondary)
                            Text(
                                "$curr${String.format(Locale.US, "%.2f", editorState.subtotal)}",
                                fontWeight = FontWeight.SemiBold,
                                color = ImmersiveTextPrimary
                            )
                        }

                        // Discount Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Discount:", color = ImmersiveTextSecondary)
                                OutlinedTextField(
                                    value = if (editorState.discountValue == 0.0) "" else editorState.discountValue.toString(),
                                    onValueChange = { v ->
                                        val d = v.toDoubleOrNull() ?: 0.0
                                        viewModel.updateEditor { it.copy(discountValue = d) }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ImmersivePrimary,
                                        unfocusedBorderColor = ImmersiveOutline,
                                        focusedTextColor = ImmersiveTextPrimary,
                                        unfocusedTextColor = ImmersiveTextPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.width(75.dp).height(48.dp).testTag("discount_val_input")
                                )
                                // Discount Type Toggle
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = ImmersiveSecondaryContainer,
                                    border = BorderStroke(1.dp, ImmersiveOutline),
                                    modifier = Modifier.clickable {
                                        val newType = if (editorState.discountType == DiscountType.PERCENT) DiscountType.FLAT else DiscountType.PERCENT
                                        viewModel.updateEditor { it.copy(discountType = newType) }
                                    }
                                ) {
                                    Text(
                                        text = editorState.discountType.symbol,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = ImmersivePrimary
                                    )
                                }
                            }

                            Text(
                                "-$curr${String.format(Locale.US, "%.2f", editorState.discountAmount)}",
                                color = ImmersivePaid,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // CGST & SGST Percent inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = if (editorState.cgstPercent == 0.0) "" else editorState.cgstPercent.toString(),
                                onValueChange = { v ->
                                    val cgst = v.toDoubleOrNull() ?: 0.0
                                    viewModel.recalculateTaxablesOnTaxChange(cgst, editorState.sgstPercent)
                                },
                                label = { Text("CGST %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImmersivePrimary,
                                    unfocusedBorderColor = ImmersiveOutline,
                                    focusedTextColor = ImmersiveTextPrimary,
                                    unfocusedTextColor = ImmersiveTextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).testTag("cgst_input")
                            )

                            OutlinedTextField(
                                value = if (editorState.sgstPercent == 0.0) "" else editorState.sgstPercent.toString(),
                                onValueChange = { v ->
                                    val sgst = v.toDoubleOrNull() ?: 0.0
                                    viewModel.recalculateTaxablesOnTaxChange(editorState.cgstPercent, sgst)
                                },
                                label = { Text("SGST %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImmersivePrimary,
                                    unfocusedBorderColor = ImmersiveOutline,
                                    focusedTextColor = ImmersiveTextPrimary,
                                    unfocusedTextColor = ImmersiveTextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).testTag("sgst_input")
                            )
                        }

                        // Tax Amounts
                        if (editorState.cgstAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CGST (${editorState.cgstPercent}%)", color = ImmersiveTextSecondary, fontSize = 13.sp)
                                Text("$curr${String.format(Locale.US, "%.2f", editorState.cgstAmount)}", fontSize = 13.sp, color = ImmersiveTextPrimary)
                            }
                        }

                        if (editorState.sgstAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SGST (${editorState.sgstPercent}%)", color = ImmersiveTextSecondary, fontSize = 13.sp)
                                Text("$curr${String.format(Locale.US, "%.2f", editorState.sgstAmount)}", fontSize = 13.sp, color = ImmersiveTextPrimary)
                            }
                        }

                        HorizontalDivider(color = ImmersiveOutline.copy(alpha = 0.5f))

                        // Grand Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Grand Total",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = ImmersiveTextPrimary
                            )
                            Text(
                                "$curr${String.format(Locale.US, "%.2f", editorState.grandTotal)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = ImmersivePrimary
                            )
                        }
                    }
                }
            }

            // Template Chooser Card with Live Mini Previews
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
                        LiveTemplateSelectorBar(
                            currentLayout = editorState.layout,
                            onSelectLayout = { layout -> viewModel.updateEditor { it.copy(layout = layout) } },
                            onOpenFullGallery = { showTemplateGallery = true }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "NOTES & PAYMENT TERMS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = ImmersivePrimary
                        )

                        OutlinedTextField(
                            value = editorState.notes,
                            onValueChange = { notes -> viewModel.updateEditor { it.copy(notes = notes) } },
                            placeholder = { Text("Payment terms, bank account details, wire info...", color = ImmersiveTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveOutline,
                                focusedTextColor = ImmersiveTextPrimary,
                                unfocusedTextColor = ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("inv_notes_input"),
                            minLines = 3,
                            maxLines = 6
                        )
                    }
                }
            }

            // Bottom Save & Action Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onPreview,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ImmersiveOutline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveTextPrimary),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Outlined.Visibility, contentDescription = null, tint = ImmersivePrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            viewModel.saveCurrentInvoice {
                                onSavedNavigate()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImmersivePrimary,
                            contentColor = ImmersiveOnPrimary
                        ),
                        modifier = Modifier.weight(1f).height(50.dp).testTag("bottom_save_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Invoice", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showTemplateGallery) {
        val currentInv = InvoiceEntity(
            id = editorState.invoiceId,
            invoiceNumber = editorState.invoiceNumber,
            dateIso = editorState.dateIso,
            dueDateIso = editorState.dueDateIso,
            poNumber = editorState.poNumber,
            billFrom = editorState.billFrom,
            billTo = editorState.billTo,
            notes = editorState.notes,
            discountValue = editorState.discountValue,
            discountType = editorState.discountType,
            status = editorState.status,
            layout = editorState.layout,
            currency = editorState.currency,
            cgstPercent = editorState.cgstPercent,
            sgstPercent = editorState.sgstPercent,
            subtotal = editorState.subtotal,
            discountAmount = editorState.discountAmount,
            cgstAmount = editorState.cgstAmount,
            sgstAmount = editorState.sgstAmount,
            grandTotal = editorState.grandTotal
        )
        val currentItms = editorState.items.map {
            InvoiceItemEntity(
                id = it.id,
                invoiceId = editorState.invoiceId,
                description = it.description,
                quantity = it.quantity,
                rate = it.rate,
                taxableRate = it.taxable,
                total = it.total
            )
        }

        LiveTemplateGalleryDialog(
            currentLayout = editorState.layout,
            sampleInvoice = currentInv,
            sampleItems = currentItms,
            onSelectLayout = { layout -> viewModel.updateEditor { it.copy(layout = layout) } },
            onDismiss = { showTemplateGallery = false }
        )
    }
}

@Composable
fun LineItemEditorCard(
    index: Int,
    item: InvoiceItem,
    currency: String,
    catalogItems: List<CatalogItemEntity>,
    onItemChange: ((InvoiceItem) -> InvoiceItem) -> Unit,
    onSelectCatalogItem: (CatalogItemEntity) -> Unit,
    onRemove: () -> Unit,
    cgst: Double,
    sgst: Double
) {
    var showCatalogDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ITEM #${index + 1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = ImmersivePrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (catalogItems.isNotEmpty()) {
                        Box {
                            TextButton(
                                onClick = { showCatalogDropdown = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("📦 Pick Item", fontSize = 11.sp, color = ImmersivePrimary)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = ImmersivePrimary)
                            }

                            DropdownMenu(
                                expanded = showCatalogDropdown,
                                onDismissRequest = { showCatalogDropdown = false }
                            ) {
                                catalogItems.forEach { cat ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(cat.name, fontWeight = FontWeight.Medium)
                                                Text("$currency${cat.rate}", fontSize = 11.sp, color = ImmersivePrimary)
                                            }
                                        },
                                        onClick = {
                                            onSelectCatalogItem(cat)
                                            showCatalogDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp).testTag("remove_item_${index}_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove Item", tint = ImmersiveOverdue, modifier = Modifier.size(18.dp))
                    }
                }
            }

            OutlinedTextField(
                value = item.description,
                onValueChange = { desc -> onItemChange { it.copy(description = desc) } },
                label = { Text("Description") },
                placeholder = { Text("Product / service details...", color = ImmersiveTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ImmersivePrimary,
                    unfocusedBorderColor = ImmersiveOutline,
                    focusedTextColor = ImmersiveTextPrimary,
                    unfocusedTextColor = ImmersiveTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("item_desc_${index}_input"),
                minLines = 2,
                maxLines = 4
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (item.quantity == 0.0) "" else item.quantity.toString(),
                    onValueChange = { q ->
                        val qty = q.toDoubleOrNull() ?: 1.0
                        onItemChange { it.copy(quantity = qty) }
                    },
                    label = { Text("Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersivePrimary,
                        unfocusedBorderColor = ImmersiveOutline,
                        focusedTextColor = ImmersiveTextPrimary,
                        unfocusedTextColor = ImmersiveTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.8f).testTag("item_qty_${index}_input")
                )

                OutlinedTextField(
                    value = if (item.rate == 0.0) "" else item.rate.toString(),
                    onValueChange = { r ->
                        val rate = r.toDoubleOrNull() ?: 0.0
                        val taxMultiplier = 1.0 + ((cgst + sgst) / 100.0)
                        val taxable = if (taxMultiplier > 0) rate / taxMultiplier else rate
                        onItemChange { it.copy(rate = rate, taxable = taxable) }
                    },
                    label = { Text("Rate ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersivePrimary,
                        unfocusedBorderColor = ImmersiveOutline,
                        focusedTextColor = ImmersiveTextPrimary,
                        unfocusedTextColor = ImmersiveTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f).testTag("item_rate_${index}_input")
                )

                OutlinedTextField(
                    value = if (item.taxable == 0.0) "" else String.format(Locale.US, "%.2f", item.taxable),
                    onValueChange = { t ->
                        val taxVal = t.toDoubleOrNull() ?: 0.0
                        onItemChange { it.copy(taxable = taxVal) }
                    },
                    label = { Text("Taxable ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersivePrimary,
                        unfocusedBorderColor = ImmersiveOutline,
                        focusedTextColor = ImmersiveTextPrimary,
                        unfocusedTextColor = ImmersiveTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f).testTag("item_taxable_${index}_input")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Line Total: ",
                    fontSize = 12.sp,
                    color = ImmersiveTextSecondary
                )
                Text(
                    text = "$currency${String.format(Locale.US, "%.2f", item.total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ImmersivePrimary
                )
            }
        }
    }
}
