package com.adam.fitness.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberHasLocationPermission(): androidx.compose.runtime.MutableState<Boolean> {
    val context = LocalContext.current
    return remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
}
