package com.adam.fitness.util

import android.content.Context
import android.speech.tts.TextToSpeech
import com.adam.fitness.data.UnitSystem
import java.util.Locale

class VoiceAnnouncer(context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                ready = true
            }
        }
    }

    fun announceDistanceMilestone(km: Int) {
        speak("Distance, $km kilometer${if (km == 1) "" else "s"}.")
    }

    fun announceTimeMilestone(minutes: Int) {
        speak("Time, $minutes minute${if (minutes == 1) "" else "s"}.")
    }

    fun announceAveragePace(secPerKm: Double, units: UnitSystem) {
        val m = (secPerKm / 60).toInt()
        val s = (secPerKm % 60).toInt()
        speak("Average pace, $m minute${if (m == 1) "" else "s"} $s second${if (s == 1) "" else "s"} per kilometer.")
    }

    private fun speak(text: String) {
        if (ready) tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
