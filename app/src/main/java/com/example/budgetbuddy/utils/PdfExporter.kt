package com.example.budgetbuddy.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.budgetbuddy.data.entity.Expense
import java.io.File
import java.io.FileOutputStream

class PdfExporter(private val context: Context) {

    fun createPdf(expenses: List<Expense>): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        paint.textSize = 18f
        canvas.drawText("Expense Report", 20f, 40f, paint)

        paint.textSize = 12f
        var y = 80f
        for (expense in expenses) {
            canvas.drawText("${expense.date}: ${expense.description} - R ${"%.2f".format(expense.amount)}", 20f, y, paint)
            y += 20f
            if (y > 800) break // Simple pagination limit for demo
        }

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "ExpenseReport.pdf")
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
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}