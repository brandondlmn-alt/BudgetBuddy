package com.example.budgetbuddy.utils

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.budgetbuddy.data.entity.Expense
import java.io.File
import java.io.FileOutputStream

class PdfExporter(private val context: Context) {

    fun createPdf(expenses: List<Expense>): File? {
        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            textSize = 12f
        }
        val subTextPaint = Paint().apply {
            textSize = 10f
            alpha = 150
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var y = 50f

        canvas.drawText("Budget Buddy - Expense Report", 20f, y, titlePaint)
        y += 40f

        for (expense in expenses) {
            // Check if we need a new page (text + image buffer)
            if (y > 700f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }

            // Draw text details
            canvas.drawText("${expense.date} - ${expense.description}", 20f, y, textPaint)
            y += 20f
            canvas.drawText("Amount: R ${"%.2f".format(expense.amount)} | Time: ${expense.startTime} - ${expense.endTime}", 20f, y, subTextPaint)
            y += 25f

            // Draw receipt image if it exists
            if (!expense.photoPath.isNullOrEmpty()) {
                val file = File(expense.photoPath)
                if (file.exists()) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            val maxWidth = 200f
                            val maxHeight = 200f
                            val ratio = Math.min(maxWidth / bitmap.width, maxHeight / bitmap.height)
                            val finalWidth = (bitmap.width * ratio).toInt()
                            val finalHeight = (bitmap.height * ratio).toInt()

                            val destRect = Rect(20, y.toInt(), 20 + finalWidth, (y + finalHeight).toInt())
                            canvas.drawBitmap(bitmap, null, destRect, null)
                            y += finalHeight + 30f
                            bitmap.recycle()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            y += 20f // Spacing between items
        }

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "ExpenseReport_${System.currentTimeMillis()}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun sharePdf(file: File) {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}