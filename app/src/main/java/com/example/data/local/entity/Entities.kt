package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DiscountType
import com.example.model.InvoiceLayout
import com.example.model.InvoiceStatus

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val dateIso: String,
    val dueDateIso: String,
    val poNumber: String = "",
    val billFrom: String = "",
    val billTo: String = "",
    val notes: String = "",
    val discountValue: Double = 0.0,
    val discountType: DiscountType = DiscountType.PERCENT,
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val layout: InvoiceLayout = InvoiceLayout.FORMAL,
    val currency: String = "$",
    val cgstPercent: Double = 0.0,
    val sgstPercent: Double = 0.0,
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoice_items")
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val description: String,
    val quantity: Double,
    val rate: Double,
    val taxableRate: Double,
    val total: Double
)

@Entity(tableName = "catalog_items")
data class CatalogItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val rate: Double = 0.0
)

@Entity(tableName = "business_profiles")
data class BusinessProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String = "",
    val details: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val currency: String = "$",
    val defaultCgst: Double = 0.0,
    val defaultSgst: Double = 0.0,
    val defaultNotes: String = "Payment terms: Net 15\nThank you for your business!",
    val defaultLayout: InvoiceLayout = InvoiceLayout.FORMAL,
    val isDarkMode: Boolean = false,
    val companyLogoBase64: String? = null,
    val adminUsername: String = "Admin2026",
    val adminPassword: String = "Password@0"
)
