package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.example.localization.AppStrings
import com.example.model.AppThemeSpec
import com.example.model.QuickRequest
import com.example.model.UserPreferences
import com.example.ui.components.resolveStampMeta
import com.example.ui.components.splitRequestTextLines
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object DocumentImageExporter {

    fun renderDocumentBitmap(
        req: QuickRequest,
        prefs: UserPreferences,
        theme: AppThemeSpec
    ): Bitmap {
        val width = 1080
        val outerPad = 48f
        val innerPad = 56f
        val contentWidth = (width - outerPad * 2f - innerPad * 2f).roundToInt()

        val primaryColor = theme.primary.toArgb()
        val textDark = theme.textDark.toArgb()
        val textMuted = theme.textMuted.toArgb()
        val primarySoft = theme.primarySoft.toArgb()
        val cardBgColor = theme.cardBg.toArgb()
        val borderColor = when (req.docType) {
            "cheque" -> 0xFFC4A47C.toInt()
            "card" -> primaryColor
            else -> theme.borderSoft.toArgb()
        }

        val lines = splitRequestTextLines(req.body, req.splitMode)
        val lang = prefs.lang

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val customerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val linePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 35f
            typeface = Typeface.DEFAULT
        }
        val mutedPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textMuted
            textSize = 28f
        }

        fun buildRtlLayout(text: String, paint: TextPaint, maxW: Int): StaticLayout =
            StaticLayout.Builder.obtain(text, 0, text.length, paint, maxW)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setTextDirection(TextDirectionHeuristics.ANYRTL_LTR)
                .setLineSpacing(6f, 1.05f)
                .build()

        val customerText = if (prefs.reqShowCustomer && req.customerName.isNotBlank()) {
            if (req.docType == "cheque") "يُصرف / للسيد: ${req.customerName}" else "العميل: ${req.customerName}"
        } else ""
        val customerLayout = if (customerText.isNotEmpty()) buildRtlLayout(customerText, customerPaint, contentWidth) else null
        val titleLayout = if (req.title.isNotBlank()) buildRtlLayout(req.title, titlePaint, contentWidth) else null
        val lineLayouts = lines.mapIndexed { idx, l ->
            val txt = if (req.splitMode != "full" && lines.size > 1) "${idx + 1}. $l" else l
            buildRtlLayout(txt, linePaint, contentWidth)
        }

        // Measure vertical height accurately
        var y = outerPad + innerPad + 80f // top badge row
        if (customerLayout != null) y += customerLayout.height + 16f
        if (titleLayout != null) y += titleLayout.height + 24f
        if (req.docType == "invoice" || req.docType == "cheque") y += 28f
        if (lineLayouts.isNotEmpty()) {
            lineLayouts.forEach { y += it.height + 14f }
            y += 20f
        }
        val stamp = resolveStampMeta(req.stampId)
        val hasFooter = (prefs.reqShowStamps && stamp.id != "none") || (prefs.reqShowSignature && req.isSigned)
        val footerHeight = if (hasFooter) 240f else 48f
        val totalHeight = max(620, (y + footerHeight + outerPad + innerPad).roundToInt())

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Outer canvas backdrop
        canvas.drawColor(0xFFF3F6F9.toInt())

        val cardRect = RectF(outerPad, outerPad, width - outerPad, totalHeight - outerPad)
        val bgGrad = when (req.docType) {
            "cheque" -> intArrayOf(0xFFFAF6EE.toInt(), 0xFFF3ECE0.toInt())
            "card" -> intArrayOf(primarySoft, cardBgColor)
            else -> intArrayOf(0xFFFFFFFF.toInt(), 0xFFF8FAFC.toInt())
        }
        val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                cardRect.left, cardRect.top,
                cardRect.right, cardRect.bottom,
                bgGrad[0], bgGrad[1],
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(cardRect, 42f, 42f, cardFillPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = if (req.docType == "cheque") 5f else 3f
            color = borderColor
        }
        canvas.drawRoundRect(cardRect, 42f, 42f, borderPaint)

        // Header row (Badge, Amount, Date)
        var curY = outerPad + innerPad
        val leftX = outerPad + innerPad
        val rightX = width - outerPad - innerPad

        if (prefs.reqShowHeaderBadge) {
            val badgeText = when (req.docType) {
                "cheque" -> if (lang == "en") "OFFICIAL CHEQUE" else "شيك رسمي"
                "card" -> if (lang == "en") "ORDER CARD" else "كارت أوردر"
                else -> if (lang == "en") "INVOICE" else "فاتورة طلب"
            }
            val badgePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryColor
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bw = badgePaint.measureText(badgeText) + 46f
            val badgeRect = RectF(rightX - bw, curY, rightX, curY + 54f)
            val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = primarySoft }
            canvas.drawRoundRect(badgeRect, 27f, 27f, badgeBgPaint)
            canvas.drawText(badgeText, badgeRect.left + 23f, badgeRect.top + 37f, badgePaint)
        }

        if (prefs.reqShowAmount && req.amountText.isNotBlank()) {
            val amtPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textDark
                textSize = 31f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val aw = amtPaint.measureText(req.amountText) + 42f
            val amtRect = RectF(leftX, curY, leftX + aw, curY + 54f)
            val amtBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
            val amtStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                color = borderColor
            }
            canvas.drawRoundRect(amtRect, 18f, 18f, amtBg)
            canvas.drawRoundRect(amtRect, 18f, 18f, amtStroke)
            canvas.drawText(req.amountText, amtRect.left + 21f, amtRect.top + 38f, amtPaint)
        }

        if (prefs.reqShowDate) {
            val dateStr = AppStrings.todayStr()
            canvas.drawText(dateStr, leftX, curY + 86f, mutedPaint)
        }

        curY += 108f

        if (customerLayout != null) {
            canvas.save()
            canvas.translate(leftX, curY)
            customerLayout.draw(canvas)
            canvas.restore()
            curY += customerLayout.height + 16f
        }

        if (titleLayout != null) {
            canvas.save()
            canvas.translate(leftX, curY)
            titleLayout.draw(canvas)
            canvas.restore()
            curY += titleLayout.height + 24f
        }

        if (req.docType == "invoice" || req.docType == "cheque") {
            val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderColor
                strokeWidth = 3.5f
                pathEffect = DashPathEffect(floatArrayOf(20f, 14f), 0f)
            }
            canvas.drawLine(leftX, curY, rightX, curY, dashPaint)
            curY += 28f
        }

        lineLayouts.forEach { layout ->
            canvas.save()
            canvas.translate(leftX, curY)
            layout.draw(canvas)
            canvas.restore()
            curY += layout.height + 14f
        }

        // Big Round Rubber Ink Stamp (Right / Bottom area)
        val footerY = totalHeight - outerPad - innerPad - 110f
        if (prefs.reqShowStamps && stamp.id != "none") {
            val inkColor = stamp.tint.toArgb()
            val cx = rightX - 125f
            val cy = footerY
            val radius = 105f

            canvas.save()
            canvas.rotate(-14f, cx, cy)

            val stampFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = (inkColor and 0x00FFFFFF) or 0x16000000
            }
            canvas.drawCircle(cx, cy, radius, stampFill)

            val outerStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 8f
                color = inkColor
            }
            canvas.drawCircle(cx, cy, radius, outerStroke)

            val innerStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3.5f
                color = inkColor
            }
            canvas.drawCircle(cx, cy, radius * 0.78f, innerStroke)

            val dashStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                color = (inkColor and 0x00FFFFFF) or 0x88000000.toInt()
                pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
            }
            canvas.drawCircle(cx, cy, radius * 0.68f, dashStroke)

            val stampTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = inkColor
                textSize = if (stamp.id == "done") 52f else 38f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val starPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = inkColor
                textSize = 20f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("★ ★ ★", cx, cy - 48f, starPaint)
            if (lang == "en") {
                canvas.drawText(stamp.labelEn, cx, cy + 12f, stampTextPaint)
            } else {
                canvas.drawText(stamp.line1Ar, cx, cy - 6f, stampTextPaint)
                val subPaint = TextPaint(stampTextPaint).apply {
                    textSize = if (stamp.id == "done") 34f else 30f
                }
                if (stamp.line2Ar.isNotBlank()) {
                    canvas.drawText(stamp.line2Ar, cx, cy + 32f, subPaint)
                }
            }
            canvas.drawText("★ ★ ★", cx, cy + 66f, starPaint)
            canvas.restore()
        }

        // Signature on Left
        val sigText = req.signatureName.ifBlank { prefs.reqSignatureName }
        if (prefs.reqShowSignature && req.isSigned && sigText.isNotBlank()) {
            val sigPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1E3A8A.toInt()
                textSize = 44f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
            }
            canvas.drawText(sigText, leftX + 10f, footerY + 18f, sigPaint)
            val lineSigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x991E3A8A.toInt()
                strokeWidth = 3.5f
            }
            canvas.drawLine(leftX + 6f, footerY + 36f, leftX + 240f, footerY + 36f, lineSigPaint)
        }

        return bitmap
    }

    fun saveDocumentToGallery(
        context: Context,
        bitmap: Bitmap,
        docId: String
    ): Result<String> = runCatching {
        val filename = "MNotes_${docId.take(8)}_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/MNotes")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Could not create MediaStore entry")
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: error("Could not open output stream")
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            filename
        } else {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        }
    }

    fun prepareShareIntent(
        context: Context,
        bitmap: Bitmap,
        docId: String,
        chooserTitle: String
    ): Result<Intent> = runCatching {
        val dir = File(context.cacheDir, "shared_docs")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "mnotes_doc_${docId.take(10)}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        Intent.createChooser(shareIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
