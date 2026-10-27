package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InvoiceStatus
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: InvoiceStatus,
    modifier: Modifier = Modifier
) {
    val (bg, text, border) = when (status) {
        InvoiceStatus.DRAFT -> Triple(ImmersiveDraftContainer, ImmersiveDraftText, ImmersiveOutline)
        InvoiceStatus.SENT -> Triple(ImmersivePendingContainer, ImmersivePendingText, ImmersivePending.copy(alpha = 0.5f))
        InvoiceStatus.PAID -> Triple(ImmersivePaidContainer, ImmersivePaidText, ImmersivePaid.copy(alpha = 0.5f))
        InvoiceStatus.OVERDUE -> Triple(ImmersiveOverdueContainer, ImmersiveOverdueText, ImmersiveOverdue.copy(alpha = 0.5f))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, border),
        modifier = modifier.testTag("status_badge_${status.name.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(text)
            )
            Text(
                text = status.label,
                color = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Boolean
) {
    var username by remember { mutableStateOf("Admin2026") }
    var password by remember { mutableStateOf("Password@0") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurface,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.border(1.dp, ImmersiveOutline, RoundedCornerShape(28.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ImmersivePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Security,
                        contentDescription = "Admin",
                        tint = ImmersiveOnPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Admin Portal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ImmersiveTextPrimary
                    )
                    Text(
                        text = "IMMERSIVE GATEWAY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = ImmersivePrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info Box with preconfigured credentials matching Immersive style
                Card(
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ImmersiveOutline.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ImmersivePrimary))
                            Text(
                                text = "CREDENTIALS REPOSITORY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = ImmersivePrimary
                            )
                        }
                        Text(
                            text = "• Admin User ID: Admin2026\n• Default Password: Password@0",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = "Standard users can access all billing tools with zero login required.",
                            fontSize = 10.sp,
                            color = ImmersiveTextSecondary
                        )
                    }
                }

                // Styled User ID
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorText = null },
                    label = { Text("USER ID", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ImmersivePrimary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersivePrimary,
                        unfocusedBorderColor = ImmersiveOutline,
                        focusedLabelColor = ImmersivePrimary,
                        unfocusedLabelColor = ImmersiveTextSecondary,
                        focusedTextColor = ImmersiveTextPrimary,
                        unfocusedTextColor = ImmersiveTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_username_input")
                )

                // Styled Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorText = null },
                    label = { Text("PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ImmersivePrimary) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = ImmersiveTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersivePrimary,
                        unfocusedBorderColor = ImmersiveOutline,
                        focusedLabelColor = ImmersivePrimary,
                        unfocusedLabelColor = ImmersiveTextSecondary,
                        focusedTextColor = ImmersiveTextPrimary,
                        unfocusedTextColor = ImmersiveTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_password_input")
                )

                errorText?.let {
                    Text(
                        text = it,
                        color = ImmersiveOverdue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val success = onLogin(username, password)
                    if (success) {
                        onDismiss()
                    } else {
                        errorText = "Invalid Admin User ID or Password"
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImmersivePrimary,
                    contentColor = ImmersiveOnPrimary
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("admin_login_submit_btn")
            ) {
                Text("Authorize Administrator", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ImmersiveTextSecondary)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color = ImmersivePrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = ImmersiveTextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.18f))
                        .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ImmersiveTextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = color.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
