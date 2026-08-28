package com.adam.fitness.util

import com.adam.fitness.data.LocationPoint
import org.json.JSONArray
import org.json.JSONObject

object RouteCodec {
    fun encode(points: List<LocationPoint>): String {
        val arr = JSONArray()
        for (p in points) {
            val o = JSONObject()
            o.put("lat", p.lat)
            o.put("lon", p.lon)
            o.put("t", p.timestamp)
            o.put("acc", p.accuracy)
            o.put("spd", p.speed)
            if (p.altitude != null) o.put("alt", p.altitude)
            arr.put(o)
        }
        return arr.toString()
    }

    fun decode(json: String): List<LocationPoint> {
        if (json.isBlank()) return emptyList()
        val arr = JSONArray(json)
        val out = ArrayList<LocationPoint>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                LocationPoint(
                    lat = o.getDouble("lat"),
                    lon = o.getDouble("lon"),
                    timestamp = o.getLong("t"),
                    accuracy = o.optDouble("acc", 0.0).toFloat(),
                    speed = o.optDouble("spd", 0.0).toFloat(),
                    altitude = if (o.has("alt")) o.getDouble("alt") else null
                )
            )
        }
        return out
    }
}
