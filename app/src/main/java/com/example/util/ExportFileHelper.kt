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
import java.io.OutputStream
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.view.Surface
import com.example.data.database.Project

object ExportFileHelper {
    
    suspend fun saveSampleExportFile(
        context: Context, 
        project: Project?, 
        format: String
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val extension = when (format) {
            "MP4_720", "MP4_4K" -> "mp4"
            "WAV" -> "wav"
            "FLAC" -> "flac"
            else -> "mp3"
        }
        
        val p = project ?: Project(title = "My_Custom_Song")
        val safeName = p.title.replace("[\\\\/:*?\"<>|]".toRegex(), "_").trim().ifEmpty { "My_Track" }
        val fullFileName = "$safeName.$extension"
        val mimeType = when (extension) {
            "mp4" -> "video/mp4"
            "wav" -> "audio/wav"
            "flac" -> "audio/flac"
            else -> "audio/mpeg"
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fullFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StudioDenuli")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os ->
                        if (extension == "mp4") {
                            // Create a temporary local file to encode video first, then write it to MediaStore stream
                            val tempVideoFile = File(context.cacheDir, "temp_video_render.mp4")
                            createCustomVideo(tempVideoFile.absolutePath, p)
                            tempVideoFile.inputStream().use { input ->
                                input.copyTo(os)
                            }
                            tempVideoFile.delete()
                        } else {
                            writeCustomSynthesizedData(os, extension, p)
                        }
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
                if (extension == "mp4") {
                    createCustomVideo(targetFile.absolutePath, p)
                } else {
                    writeCustomSynthesizedData(fos, extension, p)
                }
            }
            return@withContext Pair(true, "Download/StudioDenuli/$fullFileName")
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Pair(false, e.message ?: "Unknown error")
        }
    }

    /**
     * Synthesizes and caches the custom project preview WAV audio on-the-fly for real-time playbacks.
     */
    suspend fun generateProjectAudioCache(context: Context, project: Project?): File = withContext(Dispatchers.IO) {
        val cacheFile = File(context.cacheDir, "active_project_preview.wav")
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
        FileOutputStream(cacheFile).use { fos ->
            writeCustomSynthesizedData(fos, "wav", project)
        }
        return@withContext cacheFile
    }

    /**
     * Direct mathematical procedural synthesizer generating beautiful customized music matching project's variables 100% offline.
     */
    fun writeCustomSynthesizedData(os: OutputStream, extension: String, project: Project?) {
        val sampleRate = 22050
        val durationSec = 15 // A crisp 15 seconds custom procedural render
        val numSamples = sampleRate * durationSec
        val numBytes = numSamples * 2 // 16-bit Mono

        val p = project ?: Project(title = "Procedural Vibe")
        val bpm = p.bpm.coerceIn(60, 200)
        val genre = p.genre.trim().lowercase()
        val lyrics = p.lyrics
        val vocalDelayMs = p.vocalDelayMs

        // 1. Write standard 44-byte WAV header container
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

        header[16] = 16 // Subchunk1Size
        header[17] = 0
        header[18] = 0
        header[19] = 0

        header[20] = 1 // PCM
        header[21] = 0

        header[22] = 1 // Mono channel
        header[23] = 0

        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()

        val byteRate = sampleRate * 2
        header[28] = (byteRate and 0xff).toByte()
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

        header[40] = (numBytes and 0xff).toByte()
        header[41] = ((numBytes shr 8) and 0xff).toByte()
        header[42] = ((numBytes shr 16) and 0xff).toByte()
        header[43] = ((numBytes shr 24) and 0xff).toByte()

        os.write(header)

        // Notes table representing a harmonious scale (A Minor Pentatonic)
        val notes = doubleArrayOf(
            130.81, 146.83, 164.81, 196.00, 220.00, // C3..A3
            261.63, 293.66, 329.63, 392.00, 440.00, // C4..A4
            523.25, 587.33, 659.25, 783.99, 880.00  // C5..A5
        )

        // Text-to-Melody Engine: Create a unique melody seed purely out of the active lyrics characters!
        val cleanLyrics = lyrics.replace(Regex("[^a-zA-Z]"), "")
        val melodyPattern = if (cleanLyrics.isEmpty()) {
            intArrayOf(5, 7, 8, 9, 8, 7, 5, 2, 4, 5, 8, -1)
        } else {
            cleanLyrics.map { it.code % 15 }.toIntArray()
        }

        // Chord progression derived from genre
        val bassPatterns = when {
            genre.contains("rock") || genre.contains("metal") -> doubleArrayOf(73.42, 65.41, 55.00, 48.99) // D2, C2, A1, G1
            genre.contains("jazz") || genre.contains("ambient") -> doubleArrayOf(130.81, 146.83, 110.00, 87.31) // C3, D3, A2, F2
            else -> doubleArrayOf(110.00, 87.31, 130.81, 196.00) // Am, F, C, G
        }

        val beatDurationSec = 60.0 / bpm
        val stepDurationSec = beatDurationSec / 2.0 

        // Vocal Delay buffer lines
        val delaySamples = (sampleRate * (vocalDelayMs / 1000.0)).toInt().coerceIn(100, 11025)
        val delayBuffer = DoubleArray(delaySamples)
        var delayIdx = 0

        val outputBuffer = ByteArray(2)
        var noiseSeed = 458693021L

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val currentStep = (t / stepDurationSec).toInt()

            // A. LEAD INSTRUMENT (Procedurally mapped from Lyrics!)
            val melodyIdx = currentStep % melodyPattern.size
            val noteId = melodyPattern[melodyIdx]
            val melodyFreq = if (noteId != -1) notes[noteId % notes.size] else 0.0

            val timeInStep = t % stepDurationSec
            val leadEnvelope = if (genre.contains("ambient") || p.voiceEffect.contains("cave", ignoreCase = true)) {
                (1.0 - (timeInStep / stepDurationSec)).coerceIn(0.0, 1.0)
            } else {
                Math.exp(-9.0 * timeInStep)
            }

            val vibrato = 1.0 + 0.02 * Math.sin(2.0 * Math.PI * 6.0 * t)
            var leadWave = if (melodyFreq > 0.0) {
                if (genre.contains("rock") || genre.contains("metal")) {
                    val sinVal = Math.sin(2.0 * Math.PI * melodyFreq * vibrato * t)
                    val rawSquare = if (sinVal > 0.0) 0.4 else -0.4
                    rawSquare * leadEnvelope * 0.22
                } else if (genre.contains("hip") || genre.contains("rap") || genre.contains("r&b")) {
                    // Soft sine with slight third-harmonic distortion
                    val basic = Math.sin(2.0 * Math.PI * melodyFreq * vibrato * t)
                    val harmonic = Math.sin(6.0 * Math.PI * melodyFreq * vibrato * t)
                    (basic * 0.8 + harmonic * 0.2) * leadEnvelope * 0.32
                } else {
                    // Modern synth sawtooth-like triangle
                    val period = 1.0 / (melodyFreq * vibrato)
                    val phase = (t % period) / period
                    val rawTri = if (phase < 0.25) phase * 4.0 else if (phase < 0.75) 2.0 - phase * 4.0 else phase * 4.0 - 4.0
                    rawTri * leadEnvelope * 0.35
                }
            } else {
                0.0
            }

            // B. DIGITAL VOICE EFFECTS (Robot, Reverb, Cave, Echo, Pitch Up)
            if (p.voiceEffect.contains("robot", ignoreCase = true)) {
                leadWave *= Math.sin(2.0 * Math.PI * 55.0 * t) * 1.6
            }

            // Feed vocal delay line
            val delayedValue = delayBuffer[delayIdx]
            delayBuffer[delayIdx] = leadWave + delayedValue * 0.38
            delayIdx = (delayIdx + 1) % delaySamples
            
            // Mix delay signals
            leadWave = leadWave * 0.65 + delayedValue * 0.45

            if (p.voiceEffect.contains("pitch", ignoreCase = true)) {
                leadWave = Math.sin(2.0 * Math.PI * (melodyFreq * 1.5) * t) * leadEnvelope * 0.25
            } else if (p.voiceEffect.contains("cave", ignoreCase = true) || p.voiceEffect.contains("reverb", ignoreCase = true)) {
                leadWave = leadWave * 0.48 + delayedValue * 0.65
            }

            // C. SYMPHONIC CHORDS & BACKGROUND ARPEGGIATION
            val bassIdx = (currentStep / 4) % bassPatterns.size
            val bassFreq = bassPatterns[bassIdx]
            val isMinor = (currentStep / 8) % 2 == 0
            
            val chord1 = bassFreq * 2.0
            val chord2 = chord1 * (if (isMinor) 1.1892 else 1.2599)
            val chord3 = chord1 * 1.4983

            val chordWave = (Math.sin(2.0 * Math.PI * chord1 * t) + 
                             Math.sin(2.0 * Math.PI * chord2 * t) + 
                             Math.sin(2.0 * Math.PI * chord3 * t)) * 0.08 * leadEnvelope

            // Warm deep bass notes
            val bassWave = Math.sin(2.0 * Math.PI * bassFreq * t) * (if (genre.contains("metal")) 0.6 else 0.42)

            // D. DRUM BEAT GENERATION
            var drumWave = 0.0
            if (!genre.contains("ambient") && !genre.contains("classical")) {
                val tFromBeat = t % beatDurationSec
                
                // Thick analog-style punchy Kick
                val kickFreq = 38.0 + 130.0 * Math.exp(-95.0 * tFromBeat)
                val kickEnvelope = Math.exp(-14.0 * tFromBeat)
                val kickWave = Math.sin(2.0 * Math.PI * kickFreq * t) * kickEnvelope * 0.58

                // Noise-sculpted snappy Snare drums
                var snareWave = 0.0
                val snOn = (currentStep % 4 == 2)
                if (snOn) {
                    val tFromSn = (t + beatDurationSec / 2.0) % beatDurationSec
                     val snareEnv = Math.exp(-22.0 * tFromSn)
                     noiseSeed = (noiseSeed * 1103515245L + 12345L)
                     val noiseVal = ((noiseSeed shr 16) and 0xFFFF).toDouble() / 65535.0 - 0.5
                     snareWave = noiseVal * snareEnv * 0.30
                }

                // Crisp hi-hat ticking on every step
                val hatEnv = Math.exp(-110.0 * timeInStep)
                noiseSeed = (noiseSeed * 1103515245L + 12345L)
                val hatNoise = ((noiseSeed shr 16) and 0xFFFF).toDouble() / 65535.0 - 0.5
                val hatWave = hatNoise * hatEnv * 0.07

                drumWave = kickWave + snareWave + hatWave
            }

            // E. LOCAL PROCEDURAL NATURE ATMOSPHERE (Rain, Birds, Ocean, Thunder)
            var natureWave = 0.0
            if (!"None".equals(p.natureSound, ignoreCase = true)) {
                noiseSeed = (noiseSeed * 1103515245L + 12345L)
                val whiteNoise = ((noiseSeed shr 16) and 0xFFFF).toDouble() / 65535.0 - 0.5
                
                when {
                    p.natureSound.contains("rain", ignoreCase = true) || p.natureSound.contains("water", ignoreCase = true) -> {
                        natureWave = whiteNoise * 0.09
                    }
                    p.natureSound.contains("ocean", ignoreCase = true) || p.natureSound.contains("wave", ignoreCase = true) -> {
                        val tideMod = 0.5 + 0.5 * Math.sin(2.0 * Math.PI * 0.22 * t)
                        natureWave = whiteNoise * tideMod * 0.14
                    }
                    p.natureSound.contains("bird", ignoreCase = true) || p.natureSound.contains("forest", ignoreCase = true) -> {
                        val chirpTimer = (t.toInt() % 4 == 0)
                        val chirpEnv = if (chirpTimer) Math.exp(-14.0 * (t % 1.0)) else 0.0
                        val chirpFreq = 1900.0 + 700.0 * Math.sin(2.0 * Math.PI * 35.0 * (t % 1.0))
                        val chirpWave = Math.sin(2.0 * Math.PI * chirpFreq * t) * chirpEnv * 0.11
                        natureWave = whiteNoise * 0.02 + chirpWave
                    }
                    p.natureSound.contains("thunder", ignoreCase = true) -> {
                        val thVal = t % 8.0
                        val rawEnv = if (thVal < 2.0) Math.exp(-1.5 * thVal) else 0.0
                        natureWave = whiteNoise * rawEnv * 0.28
                    }
                }
            }

            // F. MIX LOGIC & FINAL FADE BUFFER
            val mixedSignal = (leadWave + chordWave + bassWave + drumWave + natureWave).coerceIn(-1.0, 1.0)
            val fadeVol = when {
                t < 1.0 -> t / 1.0
                t > (durationSec - 1.2) -> (durationSec - t) / 1.2
                else -> 1.0
            }

            val finalSample = (mixedSignal * 32767.0 * fadeVol * 0.85).toInt()
            outputBuffer[0] = (finalSample and 0xff).toByte()
            outputBuffer[1] = ((finalSample shr 8) and 0xff).toByte()
            os.write(outputBuffer)
        }
    }

    /**
     * Renders a real-time customized lyric video clip matching user's templates and active lyrics database.
     */
    private fun createCustomVideo(outputFilePath: String, project: Project) {
        val width = 640
        val height = 480
        val bitRate = 1400000
        val frameRate = 12
        val durationSec = 10L // Fast and fluid 10-second lyric video render
        val totalFrames = durationSec * frameRate
        val mime = "video/avc"

        val file = File(outputFilePath)
        if (file.exists()) {
            file.delete()
        }

        var mediaCodec: MediaCodec? = null
        var inputSurface: Surface? = null
        var muxer: MediaMuxer? = null
        var isMuxerStarted = false

        try {
            muxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val format = MediaFormat.createVideoFormat(mime, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            // Force software AVC fallback to be completely safe in all container configurations
            mediaCodec = try {
                MediaCodec.createByCodecName("c2.android.avc.encoder")
            } catch (e: Exception) {
                try {
                    MediaCodec.createByCodecName("OMX.google.h264.encoder")
                } catch (ee: Exception) {
                    MediaCodec.createEncoderByType(mime)
                }
            }

            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = mediaCodec.createInputSurface()
            mediaCodec.start()

            var videoTrackIndex = -1
            val bufferInfo = MediaCodec.BufferInfo()
            var framesEncoded = 0

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 21f
                color = AndroidColor.WHITE
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 28f
                color = AndroidColor.parseColor("#00FFCC")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            // Split active lyrics sheet into distinct lines for visual rendering alignment
            val lyricsLines = project.lyrics.split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("[") }
                .ifEmpty { listOf("Denuli Spark AI Masterpiece", "Beautiful custom lyrics", "Music & Video aligned", "Generated on-device") }

            while (framesEncoded < totalFrames) {
                // Synchronous frame-by-frame lock and paint
                val canvas: Canvas? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    inputSurface.lockHardwareCanvas()
                } else {
                    inputSurface.lockCanvas(null)
                }

                if (canvas != null) {
                    val frameTime = framesEncoded.toDouble() / frameRate

                    // 1. CHOOSE BACKGROUND STYLING BASED ON USER TEMPLATE
                    val template = project.videoTemplate.lowercase()
                    if (template.contains("neon") || template.contains("cyberpunk") || template.contains("retro")) {
                        // Cyberpunk Purple deep world + neon gridlines
                        canvas.drawColor(AndroidColor.parseColor("#080313"))
                        
                        // Drawn glowing wireframe grid sliding downwards
                        paint.color = AndroidColor.parseColor("#338B5BF3")
                        paint.strokeWidth = 2.5f
                        val horizonY = height * 0.40f
                        
                        // Sync animation speed with BPM/Time!
                        val speedFactor = frameTime * (project.bpm / 120.0) * 45.0
                        val gridOffset = (speedFactor % 40.0).toFloat()

                        for (y in 0..12) {
                            val ratio = y.toFloat() / 12f
                            val screenY = horizonY + (height - horizonY) * (ratio * ratio) + gridOffset
                            if (screenY <= height) {
                                canvas.drawLine(0f, screenY, width.toFloat(), screenY, paint)
                            }
                        }

                        // Converging visual wires
                        for (x in -5..5) {
                            val startX = width / 2f + (x * 40f)
                            val endX = width / 2f + (x * 160f)
                            canvas.drawLine(startX, horizonY, endX, height.toFloat(), paint)
                        }

                        // Pulsing Synthwave sun on central background horizon
                        paint.color = AndroidColor.parseColor("#FFAE00")
                        val sunRadius = 55f + 4f * Math.sin(2.0 * Math.PI * 1.5 * frameTime).toFloat()
                        canvas.drawCircle(width / 2f, horizonY - 15f, sunRadius, paint)

                    } else if (template.contains("space") || template.contains("voyage") || template.contains("liquid")) {
                        // Ambient cosmic voyage: starfield drifting outwards
                        canvas.drawColor(AndroidColor.parseColor("#020108"))
                        paint.color = AndroidColor.WHITE
                        for (star in 0..40) {
                            val seed = star * 71
                            val angle = (seed % 360).toDouble()
                            val speed = (seed % 3) + 1.5
                            val drift = ((frameTime * speed * 85) + (seed % 250)) % (width / 2.0)
                            val sx = (width / 2.0) + Math.cos(Math.toRadians(angle)) * drift
                            val sy = (height / 2.0) + Math.sin(Math.toRadians(angle)) * drift
                            val size = (drift / (width / 2.0) * 3.5f).toFloat().coerceAtLeast(1f)
                            canvas.drawCircle(sx.toFloat(), sy.toFloat(), size, paint)
                        }
                    } else if (template.contains("sunset") || template.contains("scenic")) {
                        // Golden sunset gradient
                        val sunsetLinearGrad = LinearGradient(0f, 0f, 0f, height.toFloat(), AndroidColor.RED, AndroidColor.YELLOW, Shader.TileMode.CLAMP)
                        paint.shader = sunsetLinearGrad
                        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                        paint.shader = null

                        // VHS vintage scanning bar interference
                        paint.color = AndroidColor.WHITE
                        paint.alpha = 30
                        val staticBarY = ((frameTime * 155f) % height).toFloat()
                        canvas.drawRect(0f, staticBarY, width.toFloat(), staticBarY + 18f, paint)
                        paint.alpha = 255
                    } else {
                        // Calm nature blue mountains gradient
                        val blueGrad = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), AndroidColor.parseColor("#0D1B2A"), AndroidColor.parseColor("#1B263B"), Shader.TileMode.CLAMP)
                        paint.shader = blueGrad
                        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                        paint.shader = null

                        // Tiny atmospheric forest fireflies rising up
                        paint.color = AndroidColor.parseColor("#44FFE600")
                        for (part in 0..12) {
                            val px = (width / 2f) + (200f * Math.sin(frameTime + part)).toFloat()
                            val py = height - ((frameTime * 45f + part * 75f) % height).toFloat()
                            canvas.drawCircle(px, py, 7f, paint)
                        }
                    }

                    // 2. EQUALIZER SPECTRUM REPRESENTING THE PROCEDURE SYNTH
                    paint.shader = null
                    val totalBars = 20
                    val barWidth = width / 26f
                    val gap = width / 90f
                    for (bar in 0 until totalBars) {
                        val leftX = gap + bar * (barWidth + gap)
                        val multiplier = 2.5 + (bar % 5)
                        val reactiveHeight = 25f + 130f * (0.5 + 0.5 * Math.sin(2.0 * Math.PI * multiplier * frameTime) * Math.cos(frameTime)).toFloat()
                        
                        paint.color = if (template.contains("neon") || template.contains("cyberpunk")) {
                            AndroidColor.parseColor("#00FFCC")
                        } else {
                            AndroidColor.parseColor("#FF6B8B")
                        }
                        canvas.drawRect(leftX, height - reactiveHeight, leftX + barWidth, height - 15f, paint)
                    }

                    // 3. TITLE & ACTIVE SCROLLING LYRICS OVERLAY BOX
                    val timelineStep = durationSec.toDouble() / lyricsLines.size.toDouble()
                    val lyricIdx = (frameTime / timelineStep).toInt().coerceIn(0, lyricsLines.size - 1)
                    val activeLyric = lyricsLines[lyricIdx]

                    val lightBg = template.contains("sunset")
                    textPaint.color = if (lightBg) AndroidColor.BLACK else AndroidColor.WHITE
                    titlePaint.color = if (lightBg) AndroidColor.parseColor("#7A1C25") else AndroidColor.parseColor("#00FFCC")

                    // Drawn text backing card container
                    paint.color = if (lightBg) AndroidColor.argb(135, 255, 255, 255) else AndroidColor.argb(150, 12, 6, 26)
                    canvas.drawRoundRect(40f, height * 0.12f, width - 40f, height * 0.36f, 15f, 15f, paint)

                    canvas.drawText("⚡ ${project.title} ⚡", width / 2f, height * 0.22f, titlePaint)
                    canvas.drawText(activeLyric, width / 2f, height * 0.31f, textPaint)

                    // Track BPM / Details stamp text
                    textPaint.textSize = 13.5f
                    textPaint.color = if (lightBg) AndroidColor.DKGRAY else AndroidColor.LTGRAY
                    val metadataString = "ENGINE: SPARK AI PROCEDURE | GENRE: ${project.genre.uppercase()} | BPM: ${project.bpm}"
                    canvas.drawText(metadataString, width / 2f, height * 0.41f, textPaint)
                    textPaint.textSize = 21f

                    inputSurface.unlockCanvasAndPost(canvas)
                }

                // Retrieve buffer from codec and write into MP4 container
                var outIdx = mediaCodec.dequeueOutputBuffer(bufferInfo, 2500)
                while (outIdx >= 0) {
                    val encodedBuffer = mediaCodec.getOutputBuffer(outIdx)
                    if (encodedBuffer != null) {
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufferInfo.size = 0
                        }
                        if (bufferInfo.size != 0) {
                            if (videoTrackIndex == -1) {
                                videoTrackIndex = muxer.addTrack(mediaCodec.outputFormat)
                                muxer.start()
                                isMuxerStarted = true
                            }
                            encodedBuffer.position(bufferInfo.offset)
                            encodedBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(videoTrackIndex, encodedBuffer, bufferInfo)
                        }
                    }
                    mediaCodec.releaseOutputBuffer(outIdx, false)
                    outIdx = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                }

                framesEncoded++
            }

            mediaCodec.signalEndOfInputStream()

            var outIdx = mediaCodec.dequeueOutputBuffer(bufferInfo, 4000)
            while (outIdx >= 0) {
                val encodedBuffer = mediaCodec.getOutputBuffer(outIdx)
                if (encodedBuffer != null && bufferInfo.size != 0) {
                    encodedBuffer.position(bufferInfo.offset)
                    encodedBuffer.limit(bufferInfo.offset + bufferInfo.size)
                    muxer.writeSampleData(videoTrackIndex, encodedBuffer, bufferInfo)
                }
                mediaCodec.releaseOutputBuffer(outIdx, false)
                outIdx = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                mediaCodec?.stop()
                mediaCodec?.release()
                inputSurface?.release()
                if (isMuxerStarted) {
                    muxer?.stop()
                }
                muxer?.release()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
