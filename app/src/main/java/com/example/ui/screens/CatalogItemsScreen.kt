package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CatalogItemEntity
import com.example.ui.theme.*
import com.example.viewmodel.InvoiceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogItemsScreen(
    viewModel: InvoiceViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.catalogItems.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val curr = settings.currency

    var editingItem by remember { mutableStateOf<CatalogItemEntity?>(null) }
    var nameInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<CatalogItemEntity?>(null) }

    fun resetForm() {
        editingItem = null
        nameInput = ""
        descInput = ""
        rateInput = ""
    }

    Scaffold(
        containerColor = ImmersiveBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Items Management", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ImmersiveTextPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("catalog_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ImmersiveTextPrimary)
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
            // Add / Edit Item Form Card
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
                        Text(
                            text = if (editingItem != null) "EDIT CATALOG ITEM" else "ADD NEW CATALOG ITEM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = ImmersivePrimary
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Item Name *") },
                            placeholder = { Text("e.g. Afrezza 8U Inhaler", color = ImmersiveTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveOutline,
                                focusedTextColor = ImmersiveTextPrimary,
                                unfocusedTextColor = ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_item_name_input")
                        )

                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text("Description (Optional)") },
                            placeholder = { Text("e.g. With inhaler cartridges pack", color = ImmersiveTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveOutline,
                                focusedTextColor = ImmersiveTextPrimary,
                                unfocusedTextColor = ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_item_desc_input"),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = rateInput,
                            onValueChange = { rateInput = it },
                            label = { Text("Rate ($curr) *") },
                            placeholder = { Text("e.g. 4599.00", color = ImmersiveTextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveOutline,
                                focusedTextColor = ImmersiveTextPrimary,
                                unfocusedTextColor = ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_item_rate_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (editingItem != null) {
                                TextButton(onClick = { resetForm() }) {
                                    Text("Cancel", color = ImmersiveTextSecondary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Button(
                                onClick = {
                                    val r = rateInput.toDoubleOrNull()
                                    if (nameInput.isNotBlank() && r != null && r >= 0) {
                                        viewModel.saveCatalogItem(
                                            name = nameInput.trim(),
                                            description = descInput.trim(),
                                            rate = r,
                                            editId = editingItem?.id ?: 0L
                                        )
                                        resetForm()
                                    } else {
                                        viewModel.showToast("Please enter a valid item name and rate.")
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ImmersivePrimary,
                                    contentColor = ImmersiveOnPrimary
                                ),
                                modifier = Modifier.testTag("save_item_btn")
                            ) {
                                Text(if (editingItem != null) "Update Item" else "+ Add Item", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Items List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVED ITEMS (${items.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = ImmersiveTextSecondary
                    )
                }
            }

            if (items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                        border = BorderStroke(1.dp, ImmersiveOutline)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(ImmersiveSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No items saved yet", fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary)
                            Text("Add items above for fast line item insertion in invoices.", fontSize = 12.sp, color = ImmersiveTextSecondary)
                        }
                    }
                }
            } else {
                items(items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                        border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ImmersiveTextPrimary)
                                if (item.description.isNotBlank()) {
                                    Text(item.description, fontSize = 12.sp, color = ImmersiveTextSecondary)
                                }
                                Text(
                                    text = "$curr${String.format(Locale.US, "%.2f", item.rate)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ImmersivePrimary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        editingItem = item
                                        nameInput = item.name
                                        descInput = item.description
                                        rateInput = item.rate.toString()
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ImmersivePrimary)
                                }
                                IconButton(onClick = { itemToDelete = item }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ImmersiveOverdue)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, ImmersiveOutline, RoundedCornerShape(24.dp)),
            title = { Text("Delete Item", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${item.name}' from saved items?", color = ImmersiveTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCatalogItem(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveOverdue, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = ImmersiveTextSecondary)
                }
            }
        )
    }
}
