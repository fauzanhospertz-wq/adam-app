package com.adam.fitness.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.adam.fitness.data.ActivityType
import com.adam.fitness.data.LocationPoint
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.data.WorkoutEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/** Generates an original 1080x1920 share card. Design is not derived from any third-party app. */
object ShareCardGenerator {

    fun generate(context: Context, workout: WorkoutEntity, units: UnitSystem, route: List<LocationPoint>): android.net.Uri {
        val w = 1080
        val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Background gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.parseColor("#151517"), Color.parseColor("#0A0A0B"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val accent = Color.parseColor("#FF5A31")
        val white = Color.WHITE
        val grey = Color.parseColor("#9A9A9E")

        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        canvas.drawText("ADAM", 72f, 140f, brandPaint)

        val typePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = grey
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.15f
        }
        canvas.drawText(workout.activityType.name, 72f, 200f, typePaint)

        // Route drawing area (card)
        val routeRect = RectF(72f, 260f, w - 72f, 1080f)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1D1D20") }
        canvas.drawRoundRect(routeRect, 40f, 40f, cardPaint)
        drawRoute(canvas, route, routeRect, accent)

        // Big distance
        val distMeters = workout.distanceMeters
        val distStr = PaceFormatter.formatDistance(distMeters, units)
        val bigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = white
            textSize = 148f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        canvas.drawText(distStr.uppercase(), 72f, 1230f, bigPaint)

        // Stat row: time / pace / kcal
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = grey; textSize = 32f; letterSpacing = 0.1f }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = white; textSize = 56f; typeface = Typeface.DEFAULT_BOLD }

        val colWidth = (w - 144) / 3f
        val statsY = 1330f
        val valuesY = 1400f

        canvas.drawText("TIME", 72f, statsY, labelPaint)
        canvas.drawText(PaceFormatter.formatDuration(workout.durationMs), 72f, valuesY, valuePaint)

        canvas.drawText("PACE", 72f + colWidth, statsY, labelPaint)
        canvas.drawText(PaceFormatter.formatPace(workout.avgPaceSecPerKm, units), 72f + colWidth, valuesY, valuePaint)

        canvas.drawText("KCAL", 72f + colWidth * 2, statsY, labelPaint)
        canvas.drawText("${workout.calories}", 72f + colWidth * 2, valuesY, valuePaint)

        // Date footer
        val df = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = grey; textSize = 34f }
        canvas.drawText(df.format(java.util.Date(workout.startTime)), 72f, h - 90f, footerPaint)
        val genPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent; textSize = 34f; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("Tracked with ADAM", 72f, h - 50f, genPaint)

        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "adam_share_${workout.id}.png")
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun drawRoute(canvas: Canvas, route: List<LocationPoint>, bounds: RectF, color: Int) {
        if (route.size < 2) return
        val minLat = route.minOf { it.lat }
        val maxLat = route.maxOf { it.lat }
        val minLon = route.minOf { it.lon }
        val maxLon = route.maxOf { it.lon }
        val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
        val lonSpan = (maxLon - minLon).coerceAtLeast(0.0001)
        val pad = 80f
        val drawW = bounds.width() - pad * 2
        val drawH = bounds.height() - pad * 2
        val scale = minOf(drawW / lonSpan, drawH / latSpan)

        val path = Path()
        route.forEachIndexed { i, p ->
            val x = bounds.left + pad + ((p.lon - minLon) * scale).toFloat()
            val y = bounds.bottom - pad - ((p.lat - minLat) * scale).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val routePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 10f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(path, routePaint)

        // start/end markers
        val start = route.first(); val end = route.last()
        val startX = bounds.left + pad + ((start.lon - minLon) * scale).toFloat()
        val startY = bounds.bottom - pad - ((start.lat - minLat) * scale).toFloat()
        val endX = bounds.left + pad + ((end.lon - minLon) * scale).toFloat()
        val endY = bounds.bottom - pad - ((end.lat - minLat) * scale).toFloat()
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = Color.WHITE }
        canvas.drawCircle(startX, startY, 14f, dotPaint)
        dotPaint.color = color
        canvas.drawCircle(endX, endY, 14f, dotPaint)
    }
}
