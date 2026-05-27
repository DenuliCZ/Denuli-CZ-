package com.example.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object ExportFileHelper {
    suspend fun saveSampleExportFile(context: Context, fileName: String, format: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val extension = when (format) {
            "MP4_720", "MP4_4K" -> "mp4"
            "WAV" -> "wav"
            "FLAC" -> "flac"
            else -> "mp3"
        }
        
        // Clean file name
        val safeName = fileName.replace("[\\\\/:*?\"<>|]".toRegex(), "_").trim().ifEmpty { "My_Track" }
        val fullFileName = "$safeName.$extension"
        val mimeType = when (extension) {
            "mp4" -> "video/mp4"
            "wav" -> "audio/wav"
            "flac" -> "audio/flac"
            else -> "audio/mpeg"
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Modern Android Q+ MediaStore Storage directly to Download/
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fullFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StudioDenuli")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os ->
                        writeRealOrSampleData(os, extension)
                    }
                    return@withContext Pair(true, "Download/StudioDenuli/$fullFileName")
                }
            }

            // Fallback for older Android or direct File-based writes
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val subFolder = File(downloadsDir, "StudioDenuli")
            if (!subFolder.exists()) {
                subFolder.mkdirs()
            }
            val targetFile = File(subFolder, fullFileName)
            FileOutputStream(targetFile).use { fos ->
                writeRealOrSampleData(fos, extension)
            }
            return@withContext Pair(true, "Download/StudioDenuli/$fullFileName")
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Pair(false, e.message ?: "Unknown error")
        }
    }

    private fun writeRealOrSampleData(os: OutputStream, extension: String) {
        val urlsToTry = if (extension == "mp4") {
            listOf(
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "https://www.w3schools.com/html/mov_bbb.mp4"
            )
        } else {
            listOf(
                "https://ccmixter.org/content/snowflake/snowflake_-_I_ll_Be_Right_Here.mp3",
                "https://ccmixter.org/content/admiralbob77/admiralbob77_-_Two_Left_Feet.mp3",
                "https://ccmixter.org/content/snowflake/snowflake_-_Stay_with_Me.mp3",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            )
        }

        var success = false
        for (urlStr in urlsToTry) {
            if (downloadWithRedirects(urlStr, os)) {
                success = true
                break
            }
        }

        if (!success) {
            // Offline fallback - write a nice playable synthesized retro composition
            writeSynthesizedFallback(os, extension)
        }
    }

    private fun downloadWithRedirects(urlStr: String, os: OutputStream): Boolean {
        var currentUrl = urlStr
        var redirects = 0
        val maxRedirects = 5

        while (redirects < maxRedirects) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 12000
                connection.readTimeout = 12000
                connection.instanceFollowRedirects = true
                connection.requestMethod = "GET"
                
                // Add common User-Agent, because some CDNs (like Archive.org) reject empty user agents with 403 Forbidden!
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                
                connection.connect()

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || 
                    status == HttpURLConnection.HTTP_MOVED_PERM || 
                    status == HttpURLConnection.HTTP_SEE_OTHER ||
                    status == 307 || status == 308) {
                    
                    val newUrl = connection.getHeaderField("Location") ?: return false
                    currentUrl = newUrl
                    redirects++
                    continue
                }

                if (status == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { input ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            os.write(buffer, 0, bytesRead)
                        }
                    }
                    return true
                }
                return false
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            } finally {
                connection?.disconnect()
            }
        }
        return false
    }

    private fun writeSynthesizedFallback(os: OutputStream, extension: String) {
        if (extension == "wav" || extension == "mp3" || extension == "flac") {
            val sampleRate = 16000
            val durationSec = 15
            val numSamples = sampleRate * durationSec
            val numBytes = numSamples * 2 // 16-bit mono

            // 44-byte WAV header
            val header = ByteArray(44)
            header[0] = 'R'.code.toByte() // RIFF
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()

            val fileSize = 36 + numBytes
            header[4] = (fileSize and 0xff).toByte()
            header[5] = ((fileSize shr 8) and 0xff).toByte()
            header[6] = ((fileSize shr 16) and 0xff).toByte()
            header[7] = ((fileSize shr 24) and 0xff).toByte()

            header[8] = 'W'.code.toByte() // WAVE
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()

            header[12] = 'f'.code.toByte() // fmt
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()

            header[16] = 16 // Subchunk1Size (16 for PCM)
            header[17] = 0
            header[18] = 0
            header[19] = 0

            header[20] = 1 // AudioFormat = 1 (PCM)
            header[21] = 0

            header[22] = 1 // NumChannels = 1 (Mono)
            header[23] = 0

            header[24] = (sampleRate and 0xff).toByte() // SampleRate
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()

            val byteRate = sampleRate * 2
            header[28] = (byteRate and 0xff).toByte() // ByteRate
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()

            header[32] = 2 // BlockAlign
            header[33] = 0

            header[34] = 16 // BitsPerSample
            header[35] = 0

            header[36] = 'd'.code.toByte() // data
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()

            header[40] = (numBytes and 0xff).toByte() // Subchunk2Size
            header[41] = ((numBytes shr 8) and 0xff).toByte()
            header[42] = ((numBytes shr 16) and 0xff).toByte()
            header[43] = ((numBytes shr 24) and 0xff).toByte()

            os.write(header)

            val notes = doubleArrayOf(
                261.63, 293.66, 329.63, 392.00, 440.00,
                523.25, 587.33, 659.25, 783.99, 880.00,
                1046.50, 1174.66, 1318.51, 1567.98, 1760.00
            )

            val melodyPattern = intArrayOf(
                5, 7, 8, 9, 8, 7, 5, -1,
                7, 9, 10, 12, 10, 9, 7, -1,
                8, 10, 12, 13, 12, 10, 8, -1,
                9, 12, 14, 12, 9, 7, 5, -1
            )

            val bassPattern = doubleArrayOf(
                130.81, 130.81, 196.00, 196.00,
                110.00, 110.00, 164.81, 164.81,
                87.31, 87.31, 130.81, 130.81,
                98.00, 98.00, 146.83, 146.83
            )

            val buffer = ByteArray(2)
            val stepDurationSec = 0.25
            val beatDurationSec = 0.5
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val currentStep = (t / stepDurationSec).toInt()

                // Melody Note
                val melodyIndex = currentStep % melodyPattern.size
                val noteId = melodyPattern[melodyIndex]
                val melodyFreq = if (noteId != -1) notes[noteId % notes.size] else 0.0

                // Bass Note
                val bassIndex = currentStep % bassPattern.size
                val bassFreq = bassPattern[bassIndex]

                val timeInStep = t % stepDurationSec
                val envelope = if (timeInStep < 0.02) {
                    timeInStep / 0.02
                } else {
                    (1.0 - (timeInStep - 0.02) / (stepDurationSec - 0.02)).coerceIn(0.0, 1.0)
                }

                // Lead Melody Wave (Triangle Lead with vibrato)
                val vibrato = 1.0 + 0.015 * Math.sin(2.0 * Math.PI * 6.0 * t)
                val melodyWave = if (melodyFreq > 0.0) {
                    val period = 1.0 / (melodyFreq * vibrato)
                    val phase = (t % period) / period
                    val raw = if (phase < 0.25) {
                        phase * 4.0
                    } else if (phase < 0.75) {
                        2.0 - phase * 4.0
                    } else {
                        phase * 4.0 - 4.0
                    }
                    raw * envelope * 0.35
                } else {
                    0.0
                }

                // Chords/Harmony (Triad pads on top of the bass note): play root, major/minor third, and fifth
                val chordFreq1 = bassFreq * 2.0 // Root octave up
                val isMinor = (currentStep / 4) % 2 == 0
                val chordFreq2 = chordFreq1 * (if (isMinor) 1.1892 else 1.2599) // minor third or major third
                val chordFreq3 = chordFreq1 * 1.4983 // perfect fifth
                
                val chordWave = (Math.sin(2.0 * Math.PI * chordFreq1 * t) + 
                                 Math.sin(2.0 * Math.PI * chordFreq2 * t) + 
                                 Math.sin(2.0 * Math.PI * chordFreq3 * t)) * 0.12 * envelope

                // Sub-Bass Wave (Sine + Second Harmonic)
                val bassWave = (Math.sin(2.0 * Math.PI * bassFreq * t) * 0.45 + 
                                Math.sin(2.0 * Math.PI * (bassFreq * 2.0) * t) * 0.1)

                // DRUMS ENGINE
                // Kick Drum: sweeps down exponentially from 140Hz to 45Hz every beat (0.5s)
                val tFromBeat = t % beatDurationSec
                val kickFreq = 45.0 + 100.0 * Math.exp(-90.0 * tFromBeat)
                val kickVol = Math.exp(-12.0 * tFromBeat)
                val kickWave = Math.sin(2.0 * Math.PI * kickFreq * t) * kickVol * 0.55

                // Snare Drum: white noise on the counter-beat (every beat offset by 0.25s)
                val tFromSnare = (t + 0.25) % beatDurationSec
                val snareVol = Math.exp(-22.0 * tFromSnare)
                val snareNoise = Math.sin(t * 1e5) * Math.cos(t * 3.7e5)
                val snareWave = snareNoise * snareVol * 0.25

                // Hi-Hat: crisp click on every 0.25s step
                val hatVol = Math.exp(-110.0 * timeInStep)
                val hatNoise = Math.sin(t * 8.5e5) % 1.0
                val hatWave = hatNoise * hatVol * 0.08

                // Sum all tracks
                val mixed = (melodyWave + chordWave + bassWave + kickWave + snareWave + hatWave).coerceIn(-1.0, 1.0)
                val songVolume = when {
                    t < 0.5 -> t / 0.5
                    t > (durationSec - 0.5) -> (durationSec - t) / 0.5
                    else -> 1.0
                }

                val sampleVal = (mixed * 32767.0 * songVolume * 0.8).toInt()
                buffer[0] = (sampleVal and 0xff).toByte()
                buffer[1] = ((sampleVal shr 8) and 0xff).toByte()
                os.write(buffer)
            }
        } else {
            val mockData = "StudioDenuli Spark export format: $extension. Done."
            os.write(mockData.toByteArray())
        }
    }
}
