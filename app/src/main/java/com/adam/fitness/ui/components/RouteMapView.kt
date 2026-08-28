package com.adam.fitness.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.adam.fitness.data.LocationPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Live/replay route map rendered with osmdroid (OpenStreetMap tiles — no API key).
 * GPS recording is fully independent of tile availability; this view simply
 * won't show basemap imagery when offline, while the polyline still renders.
 */
@Composable
fun RouteMapView(points: List<LocationPoint>, modifier: Modifier = Modifier, followLatest: Boolean = true) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            MapView(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(16.0)
            }
        },
        update = { map ->
            map.overlays.clear()
            if (points.isNotEmpty()) {
                val geoPoints = points.map { GeoPoint(it.lat, it.lon) }
                val line = Polyline().apply {
                    setPoints(geoPoints)
                    outlinePaint.strokeWidth = 10f
                }
                map.overlays.add(line)

                val startMarker = Marker(map).apply {
                    position = geoPoints.first()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Start"
                }
                map.overlays.add(startMarker)

                if (points.size > 1) {
                    val endMarker = Marker(map).apply {
                        position = geoPoints.last()
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Current"
                    }
                    map.overlays.add(endMarker)
                }

                if (followLatest) map.controller.animateTo(geoPoints.last())
            }
            map.invalidate()
        }
    )

    DisposableEffect(Unit) {
        onDispose { }
    }
}
