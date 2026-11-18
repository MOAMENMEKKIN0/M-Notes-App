package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Icon
import com.example.MainActivity
import com.example.model.MNotesCatalog

object LauncherIconManager {
    fun applyLauncherIcon(context: Context, iconId: String): Boolean {
        val targetSpec = MNotesCatalog.launcherIconById(iconId)
        val pm = context.packageManager
        val pkgName = context.packageName
        return runCatching {
            // Enable the target alias first so there is always at least one active launcher entry.
            pm.setComponentEnabledSetting(
                ComponentName(pkgName, targetSpec.aliasClassName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            MNotesCatalog.launcherIcons.forEach { spec ->
                if (spec.id != targetSpec.id) {
                    pm.setComponentEnabledSetting(
                        ComponentName(pkgName, spec.aliasClassName),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
            true
        }.getOrDefault(false)
    }

    fun pinCustomColoredShortcut(
        context: Context,
        shapeId: String,
        symbolId: String,
        bgColorArgb: Int,
        fgColorArgb: Int,
        badgeColorArgb: Int,
        shortLabel: String = "M Notes"
    ): Boolean {
        val sm = context.getSystemService(ShortcutManager::class.java) ?: return false
        if (!sm.isRequestPinShortcutSupported) return false

        val bmp = renderCustomIconBitmap(
            sizePx = 256,
            shapeId = shapeId,
            symbolId = symbolId,
            bgColorArgb = bgColorArgb,
            fgColorArgb = fgColorArgb,
            badgeColorArgb = badgeColorArgb
        )

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val shortcut = ShortcutInfo.Builder(context, "mnotes_custom_${System.currentTimeMillis()}")
            .setShortLabel(shortLabel.ifBlank { "M Notes" })
            .setIcon(Icon.createWithBitmap(bmp))
            .setIntent(launchIntent)
            .build()

        return sm.requestPinShortcut(shortcut, null)
    }

    fun renderCustomIconBitmap(
        sizePx: Int,
        shapeId: String,
        symbolId: String,
        bgColorArgb: Int,
        fgColorArgb: Int,
        badgeColorArgb: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val s = sizePx.toFloat()

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = bgColorArgb
        }
        val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fgColorArgb
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = s * 0.44f
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = s * 0.045f
            strokeCap = Paint.Cap.ROUND
            color = badgeColorArgb
        }
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = badgeColorArgb
        }

        // Outer shape
        when (shapeId) {
            "blob" -> {
                val p = Path().apply {
                    moveTo(s * 0.56f, s * 0.06f)
                    cubicTo(s * 0.88f, s * 0.06f, s * 0.96f, s * 0.28f, s * 0.94f, s * 0.53f)
                    cubicTo(s * 0.92f, s * 0.82f, s * 0.74f, s * 0.95f, s * 0.48f, s * 0.95f)
                    cubicTo(s * 0.20f, s * 0.95f, s * 0.06f, s * 0.76f, s * 0.07f, s * 0.48f)
                    cubicTo(s * 0.08f, s * 0.21f, s * 0.28f, s * 0.06f, s * 0.56f, s * 0.06f)
                    close()
                }
                canvas.drawPath(p, bgPaint)
            }
            "ios" -> {
                val rect = RectF(s * 0.06f, s * 0.06f, s * 0.94f, s * 0.94f)
                canvas.drawRoundRect(rect, s * 0.24f, s * 0.24f, bgPaint)
            }
            else -> {
                canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.46f, bgPaint)
            }
        }

        // Subtle inner border if background is very bright white
        val lum = Color.luminance(bgColorArgb)
        if (lum > 0.85f) {
            val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = s * 0.03f
                color = badgeColorArgb
                alpha = 90
            }
            if (shapeId == "ios") {
                canvas.drawRoundRect(RectF(s * 0.08f, s * 0.08f, s * 0.92f, s * 0.92f), s * 0.22f, s * 0.22f, ring)
            } else {
                canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.43f, ring)
            }
        }

        // Symbol inside
        when (symbolId) {
            "bell_m" -> {
                // Reminder bell chime top + bold M
                val bellRect = RectF(s * 0.40f, s * 0.20f, s * 0.60f, s * 0.34f)
                canvas.drawArc(bellRect, 180f, 180f, true, badgePaint)
                canvas.drawLine(s * 0.36f, s * 0.34f, s * 0.64f, s * 0.34f, strokePaint)
                canvas.drawText("M", s * 0.5f, s * 0.75f, fgPaint)
            }
            "clock_m" -> {
                // Reminder arc + M
                val arcRect = RectF(s * 0.26f, s * 0.20f, s * 0.74f, s * 0.48f)
                canvas.drawArc(arcRect, 200f, 140f, false, strokePaint)
                canvas.drawText("M", s * 0.5f, s * 0.75f, fgPaint)
            }
            "gym_m" -> {
                // Dumbbell bar + M
                canvas.drawLine(s * 0.32f, s * 0.28f, s * 0.68f, s * 0.28f, strokePaint)
                canvas.drawRect(s * 0.29f, s * 0.22f, s * 0.35f, s * 0.34f, badgePaint)
                canvas.drawRect(s * 0.65f, s * 0.22f, s * 0.71f, s * 0.34f, badgePaint)
                canvas.drawText("M", s * 0.5f, s * 0.76f, fgPaint)
            }
            else -> {
                // M + Reminder Dot
                fgPaint.textSize = s * 0.50f
                canvas.drawText("M", s * 0.5f, s * 0.68f, fgPaint)
                canvas.drawCircle(s * 0.74f, s * 0.26f, s * 0.065f, badgePaint)
            }
        }
        return bitmap
    }
}
