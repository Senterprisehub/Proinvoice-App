package com.example.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.model.DiscountType
import com.example.model.InvoiceItem
import com.example.model.InvoiceLayout
import com.example.model.InvoiceStatus
import com.example.ui.theme.*
import java.util.Locale

object SampleTemplateData {
    val sampleInvoice = InvoiceEntity(
        id = 1L,
        invoiceNumber = "INV-2026-0042",
        dateIso = "2026-03-15",
        dueDateIso = "2026-03-30",
        poNumber = "PO-88412",
        billFrom = "Apex Creative Studio\n100 Tech Boulevard, Suite 400\ncontact@apexstudio.io",
        billTo = "Acme Global Industries\n450 Innovation Way\nbilling@acmeglobal.com",
        notes = "Thank you for your business! Payment is due within 15 days via wire transfer.",
        discountValue = 10.0,
        discountType = DiscountType.PERCENT,
        status = InvoiceStatus.SENT,
        layout = InvoiceLayout.MODERN,
        currency = "$",
        cgstPercent = 9.0,
        sgstPercent = 9.0,
        subtotal = 1450.0,
        discountAmount = 145.0,
        cgstAmount = 117.45,
        sgstAmount = 117.45,
        grandTotal = 1539.90
    )

    val sampleItems = listOf(
        InvoiceItemEntity(
            id = 1L,
            invoiceId = 1L,
            description = "UI/UX Brand Design & Mobile App Prototyping",
            quantity = 1.0,
            rate = 950.0,
            taxableRate = 950.0,
            total = 950.0
        ),
        InvoiceItemEntity(
            id = 2L,
            invoiceId = 1L,
            description = "Cloud Infrastructure & Database Deployment",
            quantity = 1.0,
            rate = 500.0,
            taxableRate = 500.0,
            total = 500.0
        )
    )
}

@Composable
fun LiveTemplateSelectorBar(
    currentLayout: InvoiceLayout,
    onSelectLayout: (InvoiceLayout) -> Unit,
    onOpenFullGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "INVOICE TEMPLATE STYLE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = ImmersivePrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ImmersivePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${InvoiceLayout.entries.size} STYLES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImmersivePrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            TextButton(
                onClick = onOpenFullGallery,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("open_template_gallery_btn")
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp), tint = ImmersivePrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Live Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ImmersivePrimary)
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
        ) {
            items(InvoiceLayout.entries) { layout ->
                val selected = currentLayout == layout
                val primaryColor = Color(layout.primaryColorHex)
                val secColor = Color(layout.secondaryColorHex)

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) ImmersivePrimaryContainer else ImmersiveSurfaceElevated,
                    border = BorderStroke(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) primaryColor else ImmersiveOutline.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .width(135.dp)
                        .clickable { onSelectLayout(layout) }
                        .testTag("template_chip_${layout.name.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Mini Live Visual Swatch
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(primaryColor.copy(alpha = 0.85f), secColor.copy(alpha = 0.7f))
                                    )
                                )
                                .padding(6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(layout.icon, fontSize = 16.sp)
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }

                        Text(
                            text = layout.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (selected) ImmersiveOnPrimaryContainer else ImmersiveTextPrimary
                        )

                        Text(
                            text = layout.subtitle,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (selected) ImmersiveOnPrimaryContainer.copy(alpha = 0.8f) else ImmersiveTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTemplateGalleryDialog(
    currentLayout: InvoiceLayout,
    sampleInvoice: InvoiceEntity = SampleTemplateData.sampleInvoice,
    sampleItems: List<InvoiceItemEntity> = SampleTemplateData.sampleItems,
    onSelectLayout: (InvoiceLayout) -> Unit,
    onDismiss: () -> Unit
) {
    var previewLayout by remember { mutableStateOf(currentLayout) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ImmersiveOutline) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Invoice Template Gallery",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = ImmersiveTextPrimary
                    )
                    Text(
                        text = "Live preview modern layouts with real document rendering",
                        fontSize = 12.sp,
                        color = ImmersiveTextSecondary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = ImmersiveTextSecondary)
                }
            }

            // Live Template Card Carousel / Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(InvoiceLayout.entries) { layout ->
                    val isCurrent = previewLayout == layout
                    val primaryColor = Color(layout.primaryColorHex)

                    FilterChip(
                        selected = isCurrent,
                        onClick = { previewLayout = layout },
                        leadingIcon = { Text(layout.icon) },
                        label = { Text(layout.label, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor.copy(alpha = 0.25f),
                            selectedLabelColor = ImmersiveTextPrimary,
                            containerColor = ImmersiveSurfaceElevated,
                            labelColor = ImmersiveTextSecondary
                        ),
                        border = BorderStroke(if (isCurrent) 1.5.dp else 1.dp, if (isCurrent) primaryColor else ImmersiveOutline)
                    )
                }
            }

            // Description info bar for active layout
            Card(
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(previewLayout.primaryColorHex).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(previewLayout.icon, fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${previewLayout.label} • ${previewLayout.subtitle}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(previewLayout.primaryColorHex)
                        )
                        Text(
                            text = previewLayout.description,
                            fontSize = 11.sp,
                            color = ImmersiveTextSecondary
                        )
                    }
                }
            }

            // LIVE MINI PREVIEW OF THE DOCUMENT
            Text(
                text = "LIVE DOCUMENT PREVIEW",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = ImmersiveTextSecondary
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = BorderStroke(1.5.dp, Color(previewLayout.primaryColorHex).copy(alpha = 0.6f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            RenderDocumentTemplate(
                                invoice = sampleInvoice.copy(layout = previewLayout),
                                items = sampleItems,
                                isCompact = true
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ImmersiveOutline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ImmersiveTextSecondary),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        onSelectLayout(previewLayout)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(previewLayout.primaryColorHex),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1.5f).height(48.dp).testTag("apply_template_btn")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply ${previewLayout.label}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Universal Composable to render the invoice document in various modern styles
 */
@Composable
fun RenderDocumentTemplate(
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    isBlackAndWhite: Boolean = false,
    isCompact: Boolean = false
) {
    val layout = invoice.layout
    val curr = invoice.currency
    val primaryColor = if (isBlackAndWhite) Color(0xFFE2E8F0) else Color(layout.primaryColorHex)
    val secColor = if (isBlackAndWhite) Color(0xFF94A3B8) else Color(layout.secondaryColorHex)

    when (layout) {
        InvoiceLayout.MODERN -> ModernSlateTemplate(invoice, items, curr, primaryColor, secColor, isCompact)
        InvoiceLayout.CORPORATE -> CorporateProTemplate(invoice, items, curr, primaryColor, secColor, isCompact)
        InvoiceLayout.ELEGANT -> LuxeAtelierTemplate(invoice, items, curr, primaryColor, secColor, isCompact)
        InvoiceLayout.MINIMAL -> NordicMinimalTemplate(invoice, items, curr, primaryColor, secColor, isCompact)
        InvoiceLayout.BOLD -> BauhausNeoTemplate(invoice, items, curr, primaryColor, secColor, isCompact)
        InvoiceLayout.SOFT -> PastelStudioTemplate(invoice, items, curr, primaryColor, secColor, isCompact)
        InvoiceLayout.REMITTANCE -> FintechReceiptTemplate(invoice, items, curr, primaryColor, secColor, isCompact)
        InvoiceLayout.FORMAL -> FormalGstTemplate(invoice, items, curr, primaryColor, secColor, isCompact)
    }
}

// 1. Modern Slate Style
@Composable
private fun ModernSlateTemplate(
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    curr: String,
    primaryColor: Color,
    secColor: Color,
    isCompact: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp)) {
        // Modern Gradient Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = primaryColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isCompact) 10.dp else 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(primaryColor))
                        Text(
                            text = "INVOICE",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isCompact) 16.sp else 22.sp,
                            letterSpacing = 1.sp,
                            color = primaryColor
                        )
                    }
                    Text(
                        text = invoice.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isCompact) 12.sp else 15.sp,
                        color = ImmersiveTextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Date: ${invoice.dateIso}", fontSize = if (isCompact) 10.sp else 12.sp, color = ImmersiveTextSecondary)
                    Text("Due: ${invoice.dueDateIso}", fontSize = if (isCompact) 10.sp else 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    if (invoice.poNumber.isNotBlank()) {
                        Text("PO: ${invoice.poNumber}", fontSize = 10.sp, color = ImmersiveTextSecondary)
                    }
                }
            }
        }

        // Address Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AddressBox("FROM", invoice.billFrom, primaryColor, Modifier.weight(1f), isCompact)
            AddressBox("BILL TO", invoice.billTo, primaryColor, Modifier.weight(1f), isCompact)
        }

        // Table
        ItemsTable(items, curr, primaryColor, isCompact)

        // Summary
        TotalsBlock(invoice, curr, primaryColor, isCompact)

        // Notes
        if (invoice.notes.isNotBlank() && !isCompact) {
            NotesBlock(invoice.notes, primaryColor)
        }
    }
}

// 2. Corporate Pro Style
@Composable
private fun CorporateProTemplate(
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    curr: String,
    primaryColor: Color,
    secColor: Color,
    isCompact: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 14.dp)) {
        // Deep Navy Solid Header
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, primaryColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(if (isCompact) 10.dp else 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("COMMERCIAL INVOICE", fontWeight = FontWeight.Black, fontSize = if (isCompact) 14.sp else 20.sp, color = Color.White, letterSpacing = 1.sp)
                        Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = if (isCompact) 11.sp else 13.sp, color = primaryColor)
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = primaryColor) {
                        Text("EXECUTIVE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Issue Date: ${invoice.dateIso}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text("Payment Due: ${invoice.dueDateIso}", fontSize = 10.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Addresses
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AddressBox("ISSUER DETAILS", invoice.billFrom, primaryColor, Modifier.weight(1f), isCompact)
            AddressBox("CLIENT RECIPIENT", invoice.billTo, primaryColor, Modifier.weight(1f), isCompact)
        }

        // Table
        ItemsTable(items, curr, Color(0xFF334155), isCompact)

        // Summary
        TotalsBlock(invoice, curr, primaryColor, isCompact)

        // Signatory box
        if (!isCompact) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (invoice.notes.isNotBlank()) {
                    Box(modifier = Modifier.weight(1.2f)) { NotesBlock(invoice.notes, primaryColor) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .border(1.dp, ImmersiveOutline, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Authorized Signatory", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ImmersiveTextSecondary)
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = ImmersiveOutline)
                    Text("Seal & Signature", fontSize = 9.sp, color = ImmersiveTextSecondary)
                }
            }
        }
    }
}

// 3. Luxe Atelier Style
@Composable
private fun LuxeAtelierTemplate(
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    curr: String,
    primaryColor: Color,
    secColor: Color,
    isCompact: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(if (isCompact) 10.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ATELIER INVOICE",
                    fontWeight = FontWeight.Light,
                    fontSize = if (isCompact) 16.sp else 22.sp,
                    letterSpacing = 2.sp,
                    color = primaryColor
                )
                Text(text = "№ ${invoice.invoiceNumber}", fontSize = if (isCompact) 11.sp else 13.sp, color = ImmersiveTextPrimary)
            }
            Text("✦", fontSize = 20.sp, color = primaryColor)
        }

        HorizontalDivider(color = primaryColor.copy(alpha = 0.4f), thickness = 1.dp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AddressBox("ORIGIN", invoice.billFrom, primaryColor, Modifier.weight(1f), isCompact)
            AddressBox("CLIENTELE", invoice.billTo, primaryColor, Modifier.weight(1f), isCompact)
        }

        ItemsTable(items, curr, primaryColor, isCompact)
        TotalsBlock(invoice, curr, primaryColor, isCompact)

        if (invoice.notes.isNotBlank() && !isCompact) {
            NotesBlock(invoice.notes, primaryColor)
        }
    }
}

// 4. Nordic Minimal Style
@Composable
private fun NordicMinimalTemplate(
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    curr: String,
    primaryColor: Color,
    secColor: Color,
    isCompact: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("invoice", fontWeight = FontWeight.Normal, fontSize = if (isCompact) 18.sp else 26.sp, color = ImmersiveTextPrimary)
                Text("#${invoice.invoiceNumber}", fontSize = 12.sp, color = primaryColor)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(invoice.dateIso, fontSize = 11.sp, color = ImmersiveTextSecondary)
                Text("Due: ${invoice.dueDateIso}", fontSize = 11.sp, color = ImmersiveTextPrimary, fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(color = ImmersiveOutline.copy(alpha = 0.4f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AddressBox("FROM", invoice.billFrom, primaryColor, Modifier.weight(1f), isCompact)
            AddressBox("TO", invoice.billTo, primaryColor, Modifier.weight(1f), isCompact)
        }

        ItemsTable(items, curr, primaryColor, isCompact)
        TotalsBlock(invoice, curr, primaryColor, isCompact)

        if (invoice.notes.isNotBlank() && !isCompact) {
            NotesBlock(invoice.notes, primaryColor)
        }
    }
}

// 5. Bauhaus Neo Style
@Composable
private fun BauhausNeoTemplate(
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    curr: String,
    primaryColor: Color,
    secColor: Color,
    isCompact: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)) {
        Surface(
            color = Color.Black,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(2.dp, primaryColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("INVOICE // ${invoice.invoiceNumber}", color = Color.White, fontWeight = FontWeight.Black, fontSize = if (isCompact) 12.sp else 16.sp)
                Text(invoice.dateIso, color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AddressBox("SENDER", invoice.billFrom, primaryColor, Modifier.weight(1f), isCompact)
            AddressBox("RECIPIENT", invoice.billTo, primaryColor, Modifier.weight(1f), isCompact)
        }

        ItemsTable(items, curr, primaryColor, isCompact)
        TotalsBlock(invoice, curr, primaryColor, isCompact)

        if (invoice.notes.isNotBlank() && !isCompact) {
            NotesBlock(invoice.notes, primaryColor)
        }
    }
}

// 6. Pastel Studio Style
@Composable
private fun PastelStudioTemplate(
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    curr: String,
    primaryColor: Color,
    secColor: Color,
    isCompact: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(primaryColor.copy(alpha = 0.08f))
            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(if (isCompact) 10.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("🌸 Studio Invoice", fontWeight = FontWeight.Bold, fontSize = if (isCompact) 15.sp else 20.sp, color = primaryColor)
                Text(invoice.invoiceNumber, fontSize = 12.sp, color = ImmersiveTextPrimary)
            }
            Surface(shape = RoundedCornerShape(12.dp), color = primaryColor) {
                Text("PASTEL", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AddressBox("FROM", invoice.billFrom, primaryColor, Modifier.weight(1f), isCompact)
            AddressBox("TO", invoice.billTo, primaryColor, Modifier.weight(1f), isCompact)
        }

        ItemsTable(items, curr, primaryColor, isCompact)
        TotalsBlock(invoice, curr, primaryColor, isCompact)

        if (invoice.notes.isNotBlank() && !isCompact) {
            NotesBlock(invoice.notes, primaryColor)
        }
    }
}

// 7. Fintech Receipt Style
@Composable
private fun FintechReceiptTemplate(
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    curr: String,
    primaryColor: Color,
    secColor: Color,
    isCompact: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(if (isCompact) 10.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("PAYMENT RECEIPT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = if (isCompact) 13.sp else 16.sp, color = primaryColor)
                Text("TX: ${invoice.invoiceNumber}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = ImmersiveTextSecondary)
            }
            Text("💳 FINTECH", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryColor)
        }

        Text("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = ImmersiveOutline, maxLines = 1)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AddressBox("MERCHANT", invoice.billFrom, primaryColor, Modifier.weight(1f), isCompact)
            AddressBox("CUSTOMER", invoice.billTo, primaryColor, Modifier.weight(1f), isCompact)
        }

        ItemsTable(items, curr, primaryColor, isCompact)
        TotalsBlock(invoice, curr, primaryColor, isCompact)

        if (invoice.notes.isNotBlank() && !isCompact) {
            NotesBlock(invoice.notes, primaryColor)
        }
    }
}

// 8. Formal GST Tax Template
@Composable
private fun FormalGstTemplate(
    invoice: InvoiceEntity,
    items: List<InvoiceItemEntity>,
    curr: String,
    primaryColor: Color,
    secColor: Color,
    isCompact: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text("TAX INVOICE", fontWeight = FontWeight.ExtraBold, fontSize = if (isCompact) 16.sp else 22.sp, color = primaryColor)
                Text("Original for Recipient", fontSize = 10.sp, color = ImmersiveTextSecondary, fontStyle = FontStyle.Italic)
                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = if (isCompact) 11.sp else 13.sp, color = ImmersiveTextPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Date: ${invoice.dateIso}", fontSize = 11.sp, color = ImmersiveTextSecondary)
                Text("Due: ${invoice.dueDateIso}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = primaryColor)
                if (invoice.poNumber.isNotBlank()) Text("PO: ${invoice.poNumber}", fontSize = 10.sp, color = ImmersiveTextSecondary)
            }
        }

        HorizontalDivider(color = primaryColor, thickness = 2.dp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AddressBox("SELLER (DETAILS & GSTIN)", invoice.billFrom, primaryColor, Modifier.weight(1f), isCompact)
            AddressBox("BUYER (CONSIGNEE)", invoice.billTo, primaryColor, Modifier.weight(1f), isCompact)
        }

        ItemsTable(items, curr, primaryColor, isCompact)
        TotalsBlock(invoice, curr, primaryColor, isCompact)

        if (invoice.notes.isNotBlank() && !isCompact) {
            NotesBlock(invoice.notes, primaryColor)
        }
    }
}

// Reusable Subcomponents for Document Layout
@Composable
private fun AddressBox(
    title: String,
    content: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isCompact: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = ImmersiveSurfaceElevated,
        border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(if (isCompact) 6.dp else 10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 0.6.sp, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content.ifBlank { "—" },
                fontSize = if (isCompact) 10.sp else 11.sp,
                lineHeight = if (isCompact) 13.sp else 15.sp,
                color = ImmersiveTextPrimary,
                maxLines = if (isCompact) 3 else 6,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ItemsTable(
    items: List<InvoiceItemEntity>,
    curr: String,
    accentColor: Color,
    isCompact: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = ImmersiveSurfaceElevated,
        border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.6f))
    ) {
        Column {
            // Header
            Surface(color = accentColor.copy(alpha = 0.25f), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = ImmersiveTextPrimary, modifier = Modifier.weight(2f))
                    Text("Qty", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = ImmersiveTextPrimary, modifier = Modifier.weight(0.6f))
                    Text("Rate", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = ImmersiveTextPrimary, modifier = Modifier.weight(1f))
                    Text("Total", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = ImmersiveTextPrimary, modifier = Modifier.weight(1f))
                }
            }

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = if (isCompact) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.description.ifBlank { "Line Item" }, fontSize = if (isCompact) 10.sp else 11.sp, color = ImmersiveTextPrimary, modifier = Modifier.weight(2f), maxLines = 2)
                    Text("${item.quantity}", fontSize = if (isCompact) 10.sp else 11.sp, color = ImmersiveTextSecondary, modifier = Modifier.weight(0.6f))
                    Text("$curr${String.format(Locale.US, "%.2f", item.rate)}", fontSize = if (isCompact) 10.sp else 11.sp, color = ImmersiveTextSecondary, modifier = Modifier.weight(1f))
                    Text("$curr${String.format(Locale.US, "%.2f", item.total)}", fontWeight = FontWeight.Bold, fontSize = if (isCompact) 10.sp else 11.sp, color = ImmersiveTextPrimary, modifier = Modifier.weight(1f))
                }
                HorizontalDivider(color = ImmersiveOutline.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
private fun TotalsBlock(
    invoice: InvoiceEntity,
    curr: String,
    accentColor: Color,
    isCompact: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.End)
            .width(if (isCompact) 180.dp else 220.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal:", fontSize = 11.sp, color = ImmersiveTextSecondary)
            Text("$curr${String.format(Locale.US, "%.2f", invoice.subtotal)}", fontSize = 11.sp, color = ImmersiveTextPrimary)
        }

        if (invoice.discountAmount > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Discount:", fontSize = 11.sp, color = ImmersivePaid)
                Text("-$curr${String.format(Locale.US, "%.2f", invoice.discountAmount)}", fontSize = 11.sp, color = ImmersivePaid)
            }
        }

        if (invoice.cgstAmount > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CGST (${invoice.cgstPercent}%):", fontSize = 10.sp, color = ImmersiveTextSecondary)
                Text("$curr${String.format(Locale.US, "%.2f", invoice.cgstAmount)}", fontSize = 10.sp, color = ImmersiveTextPrimary)
            }
        }

        if (invoice.sgstAmount > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SGST (${invoice.sgstPercent}%):", fontSize = 10.sp, color = ImmersiveTextSecondary)
                Text("$curr${String.format(Locale.US, "%.2f", invoice.sgstAmount)}", fontSize = 10.sp, color = ImmersiveTextPrimary)
            }
        }

        HorizontalDivider(thickness = 1.dp, color = ImmersiveOutline)

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TOTAL:", fontWeight = FontWeight.Black, fontSize = if (isCompact) 11.sp else 13.sp, color = ImmersiveTextPrimary)
                Text(
                    "$curr${String.format(Locale.US, "%.2f", invoice.grandTotal)}",
                    fontWeight = FontWeight.Black,
                    fontSize = if (isCompact) 13.sp else 15.sp,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun NotesBlock(notes: String, accentColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = ImmersiveSurfaceElevated,
        border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text("NOTES & PAYMENT TERMS", fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 0.6.sp, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(notes, fontSize = 10.sp, lineHeight = 14.sp, color = ImmersiveTextPrimary)
        }
    }
}
