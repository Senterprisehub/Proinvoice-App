package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessProfileEntity
import com.example.ui.theme.*
import com.example.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessManagementScreen(
    viewModel: InvoiceViewModel,
    onBack: () -> Unit
) {
    val profiles by viewModel.businessProfiles.collectAsState()

    var editingProfile by remember { mutableStateOf<BusinessProfileEntity?>(null) }
    var nameInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var detailsInput by remember { mutableStateOf("") }
    var profileToDelete by remember { mutableStateOf<BusinessProfileEntity?>(null) }

    fun resetForm() {
        editingProfile = null
        nameInput = ""
        addressInput = ""
        detailsInput = ""
    }

    Scaffold(
        containerColor = ImmersiveBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Business Profiles", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ImmersiveTextPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("business_back_btn")) {
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
            // Add / Edit Business Card Form
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
                            text = if (editingProfile != null) "EDIT BUSINESS PROFILE" else "ADD NEW BUSINESS PROFILE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = ImmersivePrimary
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Business Name *") },
                            placeholder = { Text("e.g. Acme Technologies Ltd.", color = ImmersiveTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveOutline,
                                focusedTextColor = ImmersiveTextPrimary,
                                unfocusedTextColor = ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_business_name_input")
                        )

                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("Business Address") },
                            placeholder = { Text("123 Main Street, Suite 400, City, State, ZIP", color = ImmersiveTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveOutline,
                                focusedTextColor = ImmersiveTextPrimary,
                                unfocusedTextColor = ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_business_address_input"),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = detailsInput,
                            onValueChange = { detailsInput = it },
                            label = { Text("Tax ID / GSTIN / Contact Info") },
                            placeholder = { Text("GSTIN: 29AAAAA0000A1Z5\nPhone: +1 555-0199\nEmail: info@acme.com", color = ImmersiveTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ImmersivePrimary,
                                unfocusedBorderColor = ImmersiveOutline,
                                focusedTextColor = ImmersiveTextPrimary,
                                unfocusedTextColor = ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_business_details_input"),
                            minLines = 2
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (editingProfile != null) {
                                TextButton(onClick = { resetForm() }) {
                                    Text("Cancel", color = ImmersiveTextSecondary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Button(
                                onClick = {
                                    if (nameInput.isNotBlank()) {
                                        viewModel.saveBusinessProfile(
                                            name = nameInput.trim(),
                                            address = addressInput.trim(),
                                            details = detailsInput.trim(),
                                            editId = editingProfile?.id ?: 0L
                                        )
                                        resetForm()
                                    } else {
                                        viewModel.showToast("Please enter a business name.")
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ImmersivePrimary,
                                    contentColor = ImmersiveOnPrimary
                                ),
                                modifier = Modifier.testTag("save_business_btn")
                            ) {
                                Text(if (editingProfile != null) "Update Business" else "+ Add Business", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVED PROFILES (${profiles.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = ImmersiveTextSecondary
                    )
                }
            }

            if (profiles.isEmpty()) {
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
                                Icon(Icons.Default.Business, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No businesses saved yet", fontWeight = FontWeight.Bold, color = ImmersiveTextPrimary)
                            Text("Add your company profile to auto-fill invoices with one tap.", fontSize = 12.sp, color = ImmersiveTextSecondary)
                        }
                    }
                }
            } else {
                items(profiles, key = { it.id }) { profile ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                        border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.7f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = profile.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ImmersiveTextPrimary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            editingProfile = profile
                                            nameInput = profile.name
                                            addressInput = profile.address
                                            detailsInput = profile.details
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ImmersivePrimary)
                                    }
                                    IconButton(onClick = { profileToDelete = profile }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ImmersiveOverdue)
                                    }
                                }
                            }

                            if (profile.address.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(profile.address, fontSize = 12.sp, color = ImmersiveTextSecondary)
                            }

                            if (profile.details.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(profile.details, fontSize = 12.sp, color = ImmersiveTextSecondary, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    profileToDelete?.let { prof ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            containerColor = ImmersiveSurface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, ImmersiveOutline, RoundedCornerShape(24.dp)),
            title = { Text("Delete Business Profile", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${prof.name}'?", color = ImmersiveTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBusinessProfile(prof.id)
                        profileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveOverdue, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("Cancel", color = ImmersiveTextSecondary)
                }
            }
        )
    }
}
