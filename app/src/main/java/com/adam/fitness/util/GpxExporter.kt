package com.adam.fitness.util

import android.content.Context
import androidx.core.content.FileProvider
import com.adam.fitness.data.WorkoutEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object GpxExporter {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun export(context: Context, workout: WorkoutEntity): android.net.Uri {
        val points = RouteCodec.decode(workout.routeJson)
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"ADAM\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
        sb.append("<trk><name>${workout.activityType}</name><trkseg>\n")
        for (p in points) {
            sb.append("<trkpt lat=\"${p.lat}\" lon=\"${p.lon}\">")
            sb.append("<time>${isoFormat.format(java.util.Date(p.timestamp))}</time>")
            if (p.altitude != null) sb.append("<ele>${p.altitude}</ele>")
            sb.append("</trkpt>\n")
        }
        sb.append("</trkseg></trk></gpx>")

        val dir = File(context.cacheDir, "gpx").apply { mkdirs() }
        val file = File(dir, "adam_workout_${workout.id}.gpx")
        file.writeText(sb.toString())
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
