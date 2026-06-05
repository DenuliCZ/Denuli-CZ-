package com.example.util

import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.sin

object MidiSynthesizer {
    private const val TAG = "MidiSynthesizer"

    val NOTES = listOf("C4", "D4", "E4", "G4", "A4", "C5", "D5", "E5")
    val FREQS = doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25)
    val INSTRUMENTS = listOf("Sine Lead", "Chiptune Pulse", "Retro Saw", "Tri Pluck")

    /**
     * Synthesizes the active MIDI pattern into a 16-bit mono PCM .wav file.
     * The pattern is looped to fill the full trackDuration.
     */
    fun synthesizeMidiToWav(
        instrument: String,
        patternStr: String?,
        outputFile: File,
        trackDuration: Int = 15
    ) {
        val sampleRate = 22050
        // At 120 BPM, there are 2 beats per second. 
        // 4 steps per beat = 8 steps per second.
        // Each step is 125 ms (0.125s).
        // 16 steps = 2.0 seconds per loop.
        val stepDurationSec = 0.125
        val loopDurationSec = 16 * stepDurationSec // 2.0s
        val samplesPerStep = (sampleRate * stepDurationSec).toInt()
        val samplesPerLoop = samplesPerStep * 16

        // Parse grid [step][noteIndex]
        val activeGrid = Array(16) { BooleanArray(8) }
        if (!patternStr.isNullOrBlank()) {
            patternStr.split(",").forEach { cell ->
                val parts = cell.split("_")
                if (parts.size == 2) {
                    val step = parts[0].toIntOrNull() ?: -1
                    val noteIdx = parts[1].toIntOrNull() ?: -1
                    if (step in 0..15 && noteIdx in 0..7) {
                        activeGrid[step][noteIdx] = true
                    }
                }
            }
        }

        // Generate audio data for one single 16-step loop
        val loopSamples = ShortArray(samplesPerLoop)

        for (step in 0 until 16) {
            val stepStartSample = step * samplesPerStep
            val activeNotes = mutableListOf<Int>()
            for (noteIdx in 0 until 8) {
                if (activeGrid[step][noteIdx]) {
                    activeNotes.add(noteIdx)
                }
            }

            if (activeNotes.isNotEmpty()) {
                // Synthesize the overlapping active notes for this step
                for (s in 0 until samplesPerStep) {
                    val sampleIdx = stepStartSample + s
                    val t = s.toDouble() / sampleRate
                    
                    // Simple pluck decay envelope: plucks decay exponentially over step duration
                    val decay = Math.max(0.0, 1.0 - (s.toDouble() / samplesPerStep))
                    var mixedVal = 0.0

                    for (noteIdx in activeNotes) {
                        val freq = FREQS[noteIdx]
                        val phase = 2.0 * PI * freq * t
                        val wave = when (instrument) {
                            "Sine Lead" -> sin(phase)
                            "Chiptune Pulse" -> if (sin(phase) >= 0.0) 0.3 else -0.3 // Square wave with lower amplitude
                            "Retro Saw" -> {
                                val p = phase % (2.0 * PI)
                                (p / PI) - 1.0
                            }
                            "Tri Pluck" -> {
                                val p = (phase % (2.0 * PI)) / (2.0 * PI)
                                if (p < 0.5) {
                                    4.0 * p - 1.0
                                } else {
                                    3.0 - 4.0 * p
                                }
                            }
                            else -> sin(phase)
                        }
                        mixedVal += wave * decay
                    }

                    // Average mixed waveform to avoid digital clipping
                    val scaled = (mixedVal / activeNotes.size) * 16000.0 // Scale nicely to 16-bit Short range
                    loopSamples[sampleIdx] = scaled.toInt().coerceIn(-32768, 32767).toShort()
                }
            }
        }

        // Loop pattern to fill target project duration
        val totalSamplesCount = sampleRate * trackDuration
        val totalSamples = ShortArray(totalSamplesCount)

        for (i in 0 until totalSamplesCount) {
            val loopIndex = i % samplesPerLoop
            totalSamples[i] = loopSamples[loopIndex]
        }

        // Write WAV structure
        val dataSize = totalSamplesCount * 2
        val totalFileSize = 36 + dataSize
        val bitsPerSample = 16
        val numChannels = 1
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)

        try {
            BufferedOutputStream(FileOutputStream(outputFile)).use { out ->
                out.write("RIFF".toByteArray())
                out.write(intToByteArray(totalFileSize))
                out.write("WAVE".toByteArray())
                out.write("fmt ".toByteArray())
                out.write(intToByteArray(16))
                out.write(shortToByteArray(1))
                out.write(shortToByteArray(numChannels.toShort()))
                out.write(intToByteArray(sampleRate))
                out.write(intToByteArray(byteRate))
                out.write(shortToByteArray(blockAlign.toShort()))
                out.write(shortToByteArray(bitsPerSample.toShort()))
                out.write("data".toByteArray())
                out.write(intToByteArray(dataSize))

                val outBytes = ByteArray(totalSamples.size * 2)
                ByteBuffer.wrap(outBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(totalSamples)
                out.write(outBytes)
            }
            Log.d(TAG, "Midi sequence success generated to: ${outputFile.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error synthesizing WAV file: ${e.message}")
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xff).toByte(),
            (value ushr 8 and 0xff).toByte(),
            (value ushr 16 and 0xff).toByte(),
            (value ushr 24 and 0xff).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xff).toByte(),
            (value.toInt() ushr 8 and 0xff).toByte()
        )
    }
}
