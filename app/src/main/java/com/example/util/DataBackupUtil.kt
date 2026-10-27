package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.dao.InvoiceWithItems
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.CatalogItemEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.model.DiscountType
import com.example.model.InvoiceLayout
import com.example.model.InvoiceStatus
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataBackupUtil {

    fun generateBackupJson(
        invoices: List<InvoiceWithItems>,
        catalogItems: List<CatalogItemEntity>,
        businesses: List<BusinessProfileEntity>,
        settings: AppSettingsEntity
    ): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("timestamp", System.currentTimeMillis())
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        root.put("app", "Invoice Billing Manager")

        // Settings
        val settingsObj = JSONObject().apply {
            put("currency", settings.currency)
            put("defaultCgst", settings.defaultCgst)
            put("defaultSgst", settings.defaultSgst)
            put("defaultNotes", settings.defaultNotes)
            put("defaultLayout", settings.defaultLayout.name)
            put("isDarkMode", settings.isDarkMode)
            put("companyLogoBase64", settings.companyLogoBase64 ?: "")
            put("adminUsername", settings.adminUsername)
            put("adminPassword", settings.adminPassword)
        }
        root.put("settings", settingsObj)

        // Businesses
        val bizArray = JSONArray()
        for (biz in businesses) {
            val obj = JSONObject().apply {
                put("name", biz.name)
                put("address", biz.address)
                put("details", biz.details)
                put("createdAt", biz.createdAt)
            }
            bizArray.put(obj)
        }
        root.put("businesses", bizArray)

        // Catalog Items
        val catArray = JSONArray()
        for (item in catalogItems) {
            val obj = JSONObject().apply {
                put("name", item.name)
                put("description", item.description)
                put("rate", item.rate)
            }
            catArray.put(obj)
        }
        root.put("catalogItems", catArray)

        // Invoices with items
        val invArray = JSONArray()
        for (invWithItems in invoices) {
            val inv = invWithItems.invoice
            val invObj = JSONObject().apply {
                put("invoiceNumber", inv.invoiceNumber)
                put("dateIso", inv.dateIso)
                put("dueDateIso", inv.dueDateIso)
                put("poNumber", inv.poNumber)
                put("billFrom", inv.billFrom)
                put("billTo", inv.billTo)
                put("notes", inv.notes)
                put("discountValue", inv.discountValue)
                put("discountType", inv.discountType.name)
                put("status", inv.status.name)
                put("layout", inv.layout.name)
                put("currency", inv.currency)
                put("cgstPercent", inv.cgstPercent)
                put("sgstPercent", inv.sgstPercent)
                put("subtotal", inv.subtotal)
                put("discountAmount", inv.discountAmount)
                put("cgstAmount", inv.cgstAmount)
                put("sgstAmount", inv.sgstAmount)
                put("grandTotal", inv.grandTotal)
                put("createdAt", inv.createdAt)
                put("updatedAt", inv.updatedAt)

                val itemsArray = JSONArray()
                for (it in invWithItems.items) {
                    val itObj = JSONObject().apply {
                        put("description", it.description)
                        put("quantity", it.quantity)
                        put("rate", it.rate)
                        put("taxableRate", it.taxableRate)
                        put("total", it.total)
                    }
                    itemsArray.put(itObj)
                }
                put("items", itemsArray)
            }
            invArray.put(invObj)
        }
        root.put("invoices", invArray)

        return root.toString(2)
    }

    data class ParsedBackupData(
        val invoices: List<Pair<InvoiceEntity, List<InvoiceItemEntity>>>,
        val catalogItems: List<CatalogItemEntity>,
        val businesses: List<BusinessProfileEntity>,
        val settings: AppSettingsEntity?
    )

    fun parseBackupJson(jsonStr: String): ParsedBackupData {
        val root = JSONObject(jsonStr)

        val settings: AppSettingsEntity? = if (root.has("settings")) {
            val s = root.getJSONObject("settings")
            AppSettingsEntity(
                currency = s.optString("currency", "$"),
                defaultCgst = s.optDouble("defaultCgst", 0.0),
                defaultSgst = s.optDouble("defaultSgst", 0.0),
                defaultNotes = s.optString("defaultNotes", "Payment terms: Net 15\nThank you for your business!"),
                defaultLayout = try { InvoiceLayout.valueOf(s.optString("defaultLayout", "FORMAL")) } catch (_: Exception) { InvoiceLayout.FORMAL },
                isDarkMode = s.optBoolean("isDarkMode", false),
                companyLogoBase64 = s.optString("companyLogoBase64").takeIf { it.isNotBlank() },
                adminUsername = s.optString("adminUsername", "Admin2026"),
                adminPassword = s.optString("adminPassword", "Password@0")
            )
        } else null

        val businesses = mutableListOf<BusinessProfileEntity>()
        if (root.has("businesses")) {
            val arr = root.getJSONArray("businesses")
            for (i in 0 until arr.length()) {
                val b = arr.getJSONObject(i)
                businesses.add(
                    BusinessProfileEntity(
                        name = b.optString("name", "Business"),
                        address = b.optString("address", ""),
                        details = b.optString("details", ""),
                        createdAt = b.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        val catalog = mutableListOf<CatalogItemEntity>()
        if (root.has("catalogItems")) {
            val arr = root.getJSONArray("catalogItems")
            for (i in 0 until arr.length()) {
                val c = arr.getJSONObject(i)
                catalog.add(
                    CatalogItemEntity(
                        name = c.optString("name", "Item"),
                        description = c.optString("description", ""),
                        rate = c.optDouble("rate", 0.0)
                    )
                )
            }
        }

        val invoices = mutableListOf<Pair<InvoiceEntity, List<InvoiceItemEntity>>>()
        if (root.has("invoices")) {
            val arr = root.getJSONArray("invoices")
            for (i in 0 until arr.length()) {
                val invObj = arr.getJSONObject(i)
                val statusStr = invObj.optString("status", "DRAFT")
                val layoutStr = invObj.optString("layout", "FORMAL")
                val discountTypeStr = invObj.optString("discountType", "PERCENT")

                val invoice = InvoiceEntity(
                    invoiceNumber = invObj.optString("invoiceNumber", "INV-0001"),
                    dateIso = invObj.optString("dateIso", "2026-08-13"),
                    dueDateIso = invObj.optString("dueDateIso", "2026-08-27"),
                    poNumber = invObj.optString("poNumber", ""),
                    billFrom = invObj.optString("billFrom", ""),
                    billTo = invObj.optString("billTo", ""),
                    notes = invObj.optString("notes", ""),
                    discountValue = invObj.optDouble("discountValue", 0.0),
                    discountType = try { DiscountType.valueOf(discountTypeStr) } catch (_: Exception) { DiscountType.PERCENT },
                    status = try { InvoiceStatus.valueOf(statusStr) } catch (_: Exception) { InvoiceStatus.DRAFT },
                    layout = try { InvoiceLayout.valueOf(layoutStr) } catch (_: Exception) { InvoiceLayout.FORMAL },
                    currency = invObj.optString("currency", "$"),
                    cgstPercent = invObj.optDouble("cgstPercent", 0.0),
                    sgstPercent = invObj.optDouble("sgstPercent", 0.0),
                    subtotal = invObj.optDouble("subtotal", 0.0),
                    discountAmount = invObj.optDouble("discountAmount", 0.0),
                    cgstAmount = invObj.optDouble("cgstAmount", 0.0),
                    sgstAmount = invObj.optDouble("sgstAmount", 0.0),
                    grandTotal = invObj.optDouble("grandTotal", 0.0),
                    createdAt = invObj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = invObj.optLong("updatedAt", System.currentTimeMillis())
                )

                val items = mutableListOf<InvoiceItemEntity>()
                if (invObj.has("items")) {
                    val itemsArr = invObj.getJSONArray("items")
                    for (j in 0 until itemsArr.length()) {
                        val itObj = itemsArr.getJSONObject(j)
                        items.add(
                            InvoiceItemEntity(
                                invoiceId = 0,
                                description = itObj.optString("description", "Item"),
                                quantity = itObj.optDouble("quantity", 1.0),
                                rate = itObj.optDouble("rate", 0.0),
                                taxableRate = itObj.optDouble("taxableRate", 0.0),
                                total = itObj.optDouble("total", 0.0)
                            )
                        )
                    }
                }
                invoices.add(Pair(invoice, items))
            }
        }

        return ParsedBackupData(invoices, catalog, businesses, settings)
    }

    fun generateCsv(invoices: List<InvoiceWithItems>): String {
        val sb = StringBuilder()
        sb.appendLine("Invoice Number,Date,Due Date,Status,Layout,Bill From,Bill To,Subtotal,Discount,CGST,SGST,Grand Total,Currency")
        for (invWithItems in invoices) {
            val inv = invWithItems.invoice
            val subtotal = String.format(Locale.US, "%.2f", inv.subtotal)
            val discount = String.format(Locale.US, "%.2f", inv.discountAmount)
            val cgst = String.format(Locale.US, "%.2f", inv.cgstAmount)
            val sgst = String.format(Locale.US, "%.2f", inv.sgstAmount)
            val grand = String.format(Locale.US, "%.2f", inv.grandTotal)

            fun escapeCsv(s: String): String {
                val clean = s.replace("\"", "\"\"").replace("\n", " ")
                return "\"$clean\""
            }

            sb.appendLine("${inv.invoiceNumber},${inv.dateIso},${inv.dueDateIso},${inv.status.name},${inv.layout.name},${escapeCsv(inv.billFrom)},${escapeCsv(inv.billTo)},$subtotal,$discount,$cgst,$sgst,$grand,${inv.currency}")
        }
        return sb.toString()
    }

    fun shareBackupFile(context: Context, jsonContent: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "invoices_full_backup_$timeStamp.json"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(jsonContent.toByteArray(Charsets.UTF_8))
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "Invoice Application Data Backup")
            putExtra(Intent.EXTRA_TEXT, "Here is the complete invoice application data backup file ($fileName). You can upload this file into the Invoice app on any device to restore all invoices, items, and styles.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Download / Transfer Backup File"))
        return file
    }

    fun writeStringToUri(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content.toByteArray(Charsets.UTF_8))
            outputStream.flush()
        }
    }

    fun readStringFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } ?: throw IllegalStateException("Unable to read from selected file")
    }

    fun shareTextData(context: Context, title: String, content: String, mimeType: String = "text/plain") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}

