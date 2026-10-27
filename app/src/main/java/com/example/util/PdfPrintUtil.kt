package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.dao.InvoiceWithItems
import com.example.model.DiscountType
import com.example.model.InvoiceLayout
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

object PdfPrintUtil {

    private const val PAGE_WIDTH = 595 // A4 width in postscript points
    private const val PAGE_HEIGHT = 842 // A4 height in postscript points

    /**
     * Generates a native vector PDF file without using WebView or Chromium.
     * Prevents any Mesa / Chromium renderer process crashes on emulator or physical devices.
     */
    fun createPdfDocument(invoiceWithItems: InvoiceWithItems, isBlackAndWhite: Boolean = false): PdfDocument {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawInvoiceOnCanvas(canvas, invoiceWithItems, isBlackAndWhite)

        document.finishPage(page)
        return document
    }

    private fun drawInvoiceOnCanvas(
        canvas: Canvas,
        invoiceWithItems: InvoiceWithItems,
        isBlackAndWhite: Boolean
    ) {
        val inv = invoiceWithItems.invoice
        val items = invoiceWithItems.items
        val curr = inv.currency

        // Determine Theme Colors based on chosen layout
        val primaryHex = if (isBlackAndWhite) {
            0xFF222222.toInt()
        } else {
            when (inv.layout) {
                InvoiceLayout.CORPORATE -> 0xFF1E3A8A.toInt()
                InvoiceLayout.ELEGANT -> 0xFF92400E.toInt()
                InvoiceLayout.MODERN -> 0xFF2563EB.toInt()
                InvoiceLayout.SOFT -> 0xFFD97706.toInt()
                InvoiceLayout.BOLD -> 0xFF0F172A.toInt()
                InvoiceLayout.REMITTANCE -> 0xFF047857.toInt()
                InvoiceLayout.MINIMAL -> 0xFF475569.toInt()
                InvoiceLayout.FORMAL -> 0xFF1E293B.toInt()
            }
        }

        val textDark = 0xFF0F172A.toInt()
        val textMuted = 0xFF64748B.toInt()
        val bgBox = 0xFFF8FAFC.toInt()
        val borderBox = 0xFFE2E8F0.toInt()

        // Background
        val bgPaint = Paint().apply { color = AndroidColor.WHITE }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // Top decorative accent bar
        val barPaint = Paint().apply { color = primaryHex }
        canvas.drawRect(36f, 36f, PAGE_WIDTH - 36f, 42f, barPaint)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Title: "INVOICE"
        paint.color = primaryHex
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        canvas.drawText("INVOICE", 40f, 74f, paint)

        // Status badge
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = textMuted
        canvas.drawText("STATUS: ${inv.status.label.uppercase(Locale.US)}", 40f, 92f, paint)

        // Invoice Meta (Right-aligned)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        paint.color = textDark
        val invNoText = inv.invoiceNumber
        var xRight = (PAGE_WIDTH - 40).toFloat()
        canvas.drawText(invNoText, xRight - paint.measureText(invNoText), 72f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        paint.color = textMuted
        val dateText = "Date: ${inv.dateIso}"
        canvas.drawText(dateText, xRight - paint.measureText(dateText), 88f, paint)
        val dueText = "Due: ${inv.dueDateIso}"
        canvas.drawText(dueText, xRight - paint.measureText(dueText), 102f, paint)

        if (inv.poNumber.isNotBlank()) {
            val poText = "PO #: ${inv.poNumber}"
            canvas.drawText(poText, xRight - paint.measureText(poText), 116f, paint)
        }

        // Bill From & Bill To Info Boxes
        var startY = 130f
        val boxWidth = (PAGE_WIDTH - 72f - 16f) / 2f
        val boxHeight = 80f

        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgBox
            style = Paint.Style.FILL
        }
        val boxBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderBox
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        // From Box
        val fromRect = RectF(36f, startY, 36f + boxWidth, startY + boxHeight)
        canvas.drawRoundRect(fromRect, 6f, 6f, boxPaint)
        canvas.drawRoundRect(fromRect, 6f, 6f, boxBorderPaint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        paint.color = primaryHex
        canvas.drawText("FROM", 46f, startY + 16f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = textDark
        val fromLines = wrapText(inv.billFrom.ifBlank { "Business Sender" }, paint, boxWidth - 20f).take(4)
        var lineY = startY + 29f
        for (line in fromLines) {
            canvas.drawText(line, 46f, lineY, paint)
            lineY += 12f
        }

        // Bill To Box
        val toX = 36f + boxWidth + 16f
        val toRect = RectF(toX, startY, toX + boxWidth, startY + boxHeight)
        canvas.drawRoundRect(toRect, 6f, 6f, boxPaint)
        canvas.drawRoundRect(toRect, 6f, 6f, boxBorderPaint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        paint.color = primaryHex
        canvas.drawText("BILL TO", toX + 10f, startY + 16f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = textDark
        val toLines = wrapText(inv.billTo.ifBlank { "Valued Customer" }, paint, boxWidth - 20f).take(4)
        lineY = startY + 29f
        for (line in toLines) {
            canvas.drawText(line, toX + 10f, lineY, paint)
            lineY += 12f
        }

        // Items Table Header
        var tableY = startY + boxHeight + 20f
        val tableHeaderHeight = 22f
        val thRect = RectF(36f, tableY, PAGE_WIDTH - 36f, tableY + tableHeaderHeight)
        val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primaryHex }
        canvas.drawRoundRect(thRect, 4f, 4f, thPaint)

        paint.color = AndroidColor.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f

        canvas.drawText("DESCRIPTION", 46f, tableY + 15f, paint)

        val qtyX = 330f
        val rateX = 400f
        val taxX = 460f
        val totX = PAGE_WIDTH - 46f

        canvas.drawText("QTY", qtyX - paint.measureText("QTY"), tableY + 15f, paint)
        canvas.drawText("RATE", rateX - paint.measureText("RATE"), tableY + 15f, paint)
        canvas.drawText("TAXABLE", taxX - paint.measureText("TAXABLE"), tableY + 15f, paint)
        canvas.drawText("TOTAL", totX - paint.measureText("TOTAL"), tableY + 15f, paint)

        // Items Rows
        var rowY = tableY + tableHeaderHeight + 6f
        val rowLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderBox
            strokeWidth = 0.75f
        }

        for (item in items) {
            paint.color = textDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9.5f

            val desc = item.description.lines().firstOrNull() ?: "Item"
            val cleanDesc = if (desc.length > 40) desc.substring(0, 37) + "..." else desc
            canvas.drawText(cleanDesc, 46f, rowY + 13f, paint)

            val qtyStr = String.format(Locale.US, "%.1f", item.quantity)
            val rateStr = "$curr${String.format(Locale.US, "%.2f", item.rate)}"
            val taxStr = "$curr${String.format(Locale.US, "%.2f", item.taxableRate)}"
            val totStr = "$curr${String.format(Locale.US, "%.2f", item.total)}"

            canvas.drawText(qtyStr, qtyX - paint.measureText(qtyStr), rowY + 13f, paint)
            canvas.drawText(rateStr, rateX - paint.measureText(rateStr), rowY + 13f, paint)
            canvas.drawText(taxStr, taxX - paint.measureText(taxStr), rowY + 13f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(totStr, totX - paint.measureText(totStr), rowY + 13f, paint)

            rowY += 22f
            canvas.drawLine(36f, rowY, PAGE_WIDTH - 36f, rowY, rowLinePaint)
            rowY += 4f
        }

        // Totals Box (Right bottom)
        var totalBlockY = rowY + 12f
        val totBoxWidth = 210f
        val totBoxX = PAGE_WIDTH - 36f - totBoxWidth

        val totals = mutableListOf<Pair<String, String>>()
        totals.add(Pair("Subtotal:", "$curr${String.format(Locale.US, "%.2f", inv.subtotal)}"))
        if (inv.discountAmount > 0) {
            totals.add(Pair("Discount (${if (inv.discountType == DiscountType.PERCENT) "${inv.discountValue}%" else "Flat"}):", "-$curr${String.format(Locale.US, "%.2f", inv.discountAmount)}"))
        }
        if (inv.cgstAmount > 0) {
            totals.add(Pair("CGST (${inv.cgstPercent}%):", "$curr${String.format(Locale.US, "%.2f", inv.cgstAmount)}"))
        }
        if (inv.sgstAmount > 0) {
            totals.add(Pair("SGST (${inv.sgstPercent}%):", "$curr${String.format(Locale.US, "%.2f", inv.sgstAmount)}"))
        }

        for ((label, value) in totals) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9.5f
            paint.color = textMuted
            canvas.drawText(label, totBoxX, totalBlockY + 12f, paint)

            paint.color = textDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(value, PAGE_WIDTH - 46f - paint.measureText(value), totalBlockY + 12f, paint)
            totalBlockY += 16f
        }

        // Grand Total Divider & Row
        canvas.drawLine(totBoxX, totalBlockY + 2f, PAGE_WIDTH - 36f, totalBlockY + 2f, barPaint)
        totalBlockY += 16f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.color = primaryHex
        canvas.drawText("GRAND TOTAL:", totBoxX, totalBlockY, paint)

        val grandStr = "$curr${String.format(Locale.US, "%.2f", inv.grandTotal)}"
        canvas.drawText(grandStr, PAGE_WIDTH - 46f - paint.measureText(grandStr), totalBlockY, paint)

        // Notes & Payment Terms (Left side card with full word-wrapping and multi-line support)
        if (inv.notes.isNotBlank()) {
            val notesBoxX = 36f
            val notesBoxWidth = totBoxX - 36f - 16f
            val notesStartY = rowY + 12f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            val wrappedLines = wrapText(inv.notes, paint, notesBoxWidth - 20f)

            val notesLineHeight = 12f
            val contentHeight = 22f + (wrappedLines.size * notesLineHeight) + 10f
            val minNotesHeight = kotlin.math.max(65f, (totalBlockY - notesStartY + 10f))
            val notesBoxHeight = kotlin.math.max(contentHeight, minNotesHeight)

            // Background & Border card for Notes & Terms
            val notesRect = RectF(notesBoxX, notesStartY, notesBoxX + notesBoxWidth, notesStartY + notesBoxHeight)
            canvas.drawRoundRect(notesRect, 6f, 6f, boxPaint)
            canvas.drawRoundRect(notesRect, 6f, 6f, boxBorderPaint)

            // Card Header
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            paint.color = primaryHex
            canvas.drawText("NOTES & PAYMENT TERMS", notesBoxX + 10f, notesStartY + 16f, paint)

            val headerDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderBox
                strokeWidth = 0.5f
            }
            canvas.drawLine(notesBoxX + 10f, notesStartY + 21f, notesBoxX + notesBoxWidth - 10f, notesStartY + 21f, headerDividerPaint)

            // Render all wrapped note lines
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = textDark

            var nY = notesStartY + 33f
            for (line in wrappedLines) {
                if (nY < PAGE_HEIGHT - 36f) {
                    canvas.drawText(line, notesBoxX + 10f, nY, paint)
                    nY += notesLineHeight
                }
            }
        }
    }

    /**
     * Helper to wrap text into multiple lines fitting within a given width on Canvas.
     */
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            if (paragraph.isBlank()) {
                result.add("")
                continue
            }
            val words = paragraph.split(" ")
            var currentLine = StringBuilder()
            for (word in words) {
                val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(candidate) <= maxWidth) {
                    currentLine = StringBuilder(candidate)
                } else {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine.toString())
                    }
                    if (paint.measureText(word) > maxWidth) {
                        var subWord = word
                        while (paint.measureText(subWord) > maxWidth && subWord.length > 1) {
                            var endIdx = subWord.length - 1
                            while (endIdx > 0 && paint.measureText(subWord.substring(0, endIdx)) > maxWidth) {
                                endIdx--
                            }
                            result.add(subWord.substring(0, endIdx))
                            subWord = subWord.substring(endIdx)
                        }
                        currentLine = StringBuilder(subWord)
                    } else {
                        currentLine = StringBuilder(word)
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine.toString())
            }
        }
        return result
    }

    /**
     * Saves the PDF to internal cache and returns the shared File / Uri via FileProvider.
     */
    fun savePdfToCache(context: Context, invoiceWithItems: InvoiceWithItems, isBlackAndWhite: Boolean = false): File? {
        return try {
            val doc = createPdfDocument(invoiceWithItems, isBlackAndWhite)
            val dir = File(context.cacheDir, "invoices")
            if (!dir.exists()) dir.mkdirs()
            val safeName = invoiceWithItems.invoice.invoiceNumber.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val file = File(dir, "Invoice_${safeName}.pdf")
            val out = FileOutputStream(file)
            doc.writeTo(out)
            out.flush()
            out.close()
            doc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Shares the generated PDF document directly via system share sheet (WhatsApp, Email, Drive, etc.)
     */
    fun sharePdfFile(context: Context, invoiceWithItems: InvoiceWithItems, isBlackAndWhite: Boolean = false) {
        val file = savePdfToCache(context, invoiceWithItems, isBlackAndWhite)
        if (file != null && file.exists()) {
            val uri: Uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoiceWithItems.invoice.invoiceNumber}")
                putExtra(Intent.EXTRA_TEXT, "Please find attached invoice ${invoiceWithItems.invoice.invoiceNumber} for your records.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF Invoice"))
        } else {
            Toast.makeText(context, "Could not generate PDF file", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Prints the document using Android native PrintDocumentAdapter without Chromium/WebView.
     */
    fun printInvoice(context: Context, invoiceWithItems: InvoiceWithItems, isBlackAndWhite: Boolean = false) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val printAdapter = object : PrintDocumentAdapter() {
                    private var pdfFile: File? = null

                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }

                        pdfFile = savePdfToCache(context, invoiceWithItems, isBlackAndWhite)

                        val info = PrintDocumentInfo.Builder("${invoiceWithItems.invoice.invoiceNumber}.pdf")
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1)
                            .build()

                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out PageRange>?,
                        destination: ParcelFileDescriptor?,
                        cancellationSignal: CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        val file = pdfFile
                        if (file == null || destination == null) {
                            callback?.onWriteFailed("File creation error")
                            return
                        }

                        try {
                            val inStream = FileInputStream(file)
                            val outStream = FileOutputStream(destination.fileDescriptor)
                            val buffer = ByteArray(4096)
                            var read: Int
                            while (inStream.read(buffer).also { read = it } > 0) {
                                if (cancellationSignal?.isCanceled == true) {
                                    callback?.onWriteCancelled()
                                    inStream.close()
                                    outStream.close()
                                    return
                                }
                                outStream.write(buffer, 0, read)
                            }
                            inStream.close()
                            outStream.close()
                            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                        } catch (e: IOException) {
                            callback?.onWriteFailed(e.localizedMessage)
                        }
                    }
                }

                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setColorMode(if (isBlackAndWhite) PrintAttributes.COLOR_MODE_MONOCHROME else PrintAttributes.COLOR_MODE_COLOR)
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()

                printManager.print(
                    invoiceWithItems.invoice.invoiceNumber,
                    printAdapter,
                    printAttributes
                )
            } else {
                // Fallback to sharing the PDF file directly
                sharePdfFile(context, invoiceWithItems, isBlackAndWhite)
            }
        } catch (e: Exception) {
            // Safe fallback to PDF sharing
            sharePdfFile(context, invoiceWithItems, isBlackAndWhite)
        }
    }

    fun shareInvoiceText(context: Context, invoiceWithItems: InvoiceWithItems) {
        val inv = invoiceWithItems.invoice
        val curr = inv.currency
        val text = buildString {
            appendLine("📄 INVOICE: ${inv.invoiceNumber}")
            appendLine("Date: ${inv.dateIso} | Due: ${inv.dueDateIso}")
            if (inv.poNumber.isNotBlank()) appendLine("PO #: ${inv.poNumber}")
            appendLine("Status: ${inv.status.label}")
            appendLine()
            appendLine("--- FROM ---")
            appendLine(inv.billFrom)
            appendLine()
            appendLine("--- BILL TO ---")
            appendLine(inv.billTo)
            appendLine()
            appendLine("--- ITEMS ---")
            invoiceWithItems.items.forEachIndexed { idx, item ->
                appendLine("${idx + 1}. ${item.description}")
                appendLine("   ${item.quantity} x $curr${String.format(Locale.US, "%.2f", item.rate)} = $curr${String.format(Locale.US, "%.2f", item.total)}")
            }
            appendLine()
            appendLine("Subtotal: $curr${String.format(Locale.US, "%.2f", inv.subtotal)}")
            if (inv.discountAmount > 0) {
                appendLine("Discount: -$curr${String.format(Locale.US, "%.2f", inv.discountAmount)}")
            }
            if (inv.cgstAmount > 0) appendLine("CGST (${inv.cgstPercent}%): $curr${String.format(Locale.US, "%.2f", inv.cgstAmount)}")
            if (inv.sgstAmount > 0) appendLine("SGST (${inv.sgstPercent}%): $curr${String.format(Locale.US, "%.2f", inv.sgstAmount)}")
            appendLine("====================")
            appendLine("GRAND TOTAL: $curr${String.format(Locale.US, "%.2f", inv.grandTotal)}")
            appendLine("====================")
            if (inv.notes.isNotBlank()) {
                appendLine()
                appendLine("Notes & Terms:")
                appendLine(inv.notes)
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Invoice ${inv.invoiceNumber}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice ${inv.invoiceNumber}"))
    }
}
