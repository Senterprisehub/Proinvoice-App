package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.InvoiceWithItems
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.model.DiscountType
import com.example.model.InvoiceLayout
import com.example.ui.components.LiveTemplateGalleryDialog
import com.example.ui.components.LiveTemplateSelectorBar
import com.example.ui.components.RenderDocumentTemplate
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.PdfPrintUtil
import com.example.viewmodel.InvoiceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePreviewScreen(
    viewModel: InvoiceViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val editorState by viewModel.editorState.collectAsState()
    var isBlackAndWhite by remember { mutableStateOf(false) }
    var showTemplateGallery by remember { mutableStateOf(false) }

    val invEntity = remember(editorState) {
        InvoiceEntity(
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
    }

    val itemEntities = remember(editorState.items) {
        editorState.items.map {
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
    }

    val invoiceWithItems = remember(invEntity, itemEntities) {
        InvoiceWithItems(invEntity, itemEntities)
    }

    val primaryColor = Color(editorState.layout.primaryColorHex)

    Scaffold(
        containerColor = ImmersiveBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Live Invoice Preview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = "${editorState.layout.icon} ${editorState.layout.label}",
                            fontSize = 12.sp,
                            color = primaryColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("preview_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ImmersiveTextPrimary)
                    }
                },
                actions = {
                    // Color / BW toggle
                    IconButton(onClick = { isBlackAndWhite = !isBlackAndWhite }) {
                        Icon(
                            if (isBlackAndWhite) Icons.Default.Palette else Icons.Outlined.Contrast,
                            contentDescription = "Toggle Color Mode",
                            tint = ImmersiveTextPrimary
                        )
                    }

                    // Share PDF File
                    IconButton(
                        onClick = { PdfPrintUtil.sharePdfFile(context, invoiceWithItems, isBlackAndWhite) },
                        modifier = Modifier.testTag("preview_share_pdf_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Share PDF", tint = ImmersivePrimary)
                    }

                    // Share Text
                    IconButton(
                        onClick = { PdfPrintUtil.shareInvoiceText(context, invoiceWithItems) },
                        modifier = Modifier.testTag("preview_share_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = ImmersiveTextPrimary)
                    }

                    // Native Print / PDF
                    IconButton(
                        onClick = { PdfPrintUtil.printInvoice(context, invoiceWithItems, isBlackAndWhite) },
                        modifier = Modifier.testTag("preview_print_btn")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print / PDF", tint = ImmersiveTextPrimary)
                    }

                    // Edit
                    IconButton(onClick = onEdit, modifier = Modifier.testTag("preview_edit_btn")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ImmersivePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ImmersiveBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ImmersiveBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Template Switcher Strip with Real-time feedback
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = BorderStroke(1.dp, ImmersiveOutline)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    LiveTemplateSelectorBar(
                        currentLayout = editorState.layout,
                        onSelectLayout = { layout -> viewModel.updateEditor { it.copy(layout = layout) } },
                        onOpenFullGallery = { showTemplateGallery = true }
                    )
                }
            }

            // Status Badge & Layout indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ImmersiveSurfaceElevated,
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(editorState.layout.icon)
                        Text(
                            text = "${editorState.layout.label} Style",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }

                StatusBadge(status = editorState.status)
            }

            // Live Rendered Document Sheet Paper Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = if (isBlackAndWhite) ImmersiveOutline else primaryColor.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.padding(18.dp)) {
                    RenderDocumentTemplate(
                        invoice = invEntity,
                        items = itemEntities,
                        isBlackAndWhite = isBlackAndWhite,
                        isCompact = false
                    )
                }
            }

            // Quick Print & Share Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { PdfPrintUtil.shareInvoiceText(context, invoiceWithItems) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ImmersiveOutline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveTextPrimary),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Text", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { PdfPrintUtil.sharePdfFile(context, invoiceWithItems, isBlackAndWhite) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ImmersivePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersivePrimary),
                    modifier = Modifier.weight(1.2f).height(48.dp).testTag("action_share_pdf_btn")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { PdfPrintUtil.printInvoice(context, invoiceWithItems, isBlackAndWhite) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersivePrimary,
                        contentColor = ImmersiveOnPrimary
                    ),
                    modifier = Modifier.weight(1.1f).height(48.dp).testTag("action_print_btn")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    if (showTemplateGallery) {
        LiveTemplateGalleryDialog(
            currentLayout = editorState.layout,
            sampleInvoice = invEntity,
            sampleItems = itemEntities,
            onSelectLayout = { layout -> viewModel.updateEditor { it.copy(layout = layout) } },
            onDismiss = { showTemplateGallery = false }
        )
    }
}

