package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.RadialGradient
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import kotlin.math.*
import kotlin.random.Random

object ExportFileHelper {
    private const val TAG = "ExportFileHelper"

    /**
     * Synthesizes a real, fully playable .wav file depending on the genre.
     * Combines dynamic synth, chords, bass, and beat.
     */
    suspend fun generateRealAudioTrack(
        context: Context,
        genre: String,
        durationSec: Int,
        projectBpm: Int? = null,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val cacheDir = context.cacheDir
        val audioFile = File(cacheDir, "spark_voice_composition_${System.currentTimeMillis()}.wav")

        val sampleRate = 22050 // Save memory/time with 22kHz
        val bitsPerSample = 16
        val numChannels = 1
        val numSamples = sampleRate * durationSec
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)
        val dataSize = numSamples * blockAlign
        val totalFileSize = 36 + dataSize

        BufferedOutputStream(FileOutputStream(audioFile)).use { out ->
            // 1. Write WAV Header
            out.write("RIFF".toByteArray())
            out.write(intToByteArray(totalFileSize))
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(intToByteArray(16)) // Subchunk1Size (16 for PCM)
            out.write(shortToByteArray(1)) // AudioFormat (1 for PCM)
            out.write(shortToByteArray(numChannels.toShort()))
            out.write(intToByteArray(sampleRate))
            out.write(intToByteArray(byteRate))
            out.write(shortToByteArray(blockAlign.toShort()))
            out.write(shortToByteArray(bitsPerSample.toShort()))
            out.write("data".toByteArray())
            out.write(intToByteArray(dataSize))

            // 2. Synthesize Samples
            val bpm = if (projectBpm != null && projectBpm > 0) {
                projectBpm.toDouble()
            } else {
                when (genre.lowercase()) {
                    "pop" -> 120.0
                    "rock" -> 100.0
                    "hiphop" -> 85.0
                    "cinematic" -> 70.0
                    "synthwave" -> 115.0
                    "lo-fi" -> 75.0
                    "metal" -> 138.0
                    "edm" -> 128.0
                    "country" -> 92.0
                    else -> 95.0
                }
            }

            val beatDurationSec = 60.0 / bpm
            val r = java.util.Random(System.currentTimeMillis())

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                var sampleVal = 0.0

                // Current chord progression index (4 bars progression)
                val barIndex = (t / (beatDurationSec * 4)).toInt() % 4
                val chordBaseFreq = when (barIndex) {
                    0 -> 220.0  // A3
                    1 -> 165.0  // E3
                    2 -> 196.0  // G3
                    3 -> 146.83 // D3
                    else -> 220.0
                }

                // A. Layer 1: Chords (Synthesizer Backing / Pads / Keys)
                val chordOsc1 = sin(2 * PI * chordBaseFreq * t)
                val chordOsc2 = sin(2 * PI * (chordBaseFreq * 1.2) * t)
                val chordOsc3 = sin(2 * PI * (chordBaseFreq * 1.5) * t)
                var chordsMix = (chordOsc1 + chordOsc2 + chordOsc3) / 3.0

                // Apply genre-specific synthesis
                when (genre.lowercase()) {
                    "rock" -> {
                        // Hard clipped rock guitar distortion
                        chordsMix = sign(chordsMix) * (1.0 - exp(-3.0 * abs(chordsMix)))
                    }
                    "hiphop" -> {
                        // Muted ambient electric piano chords
                        val subFilter = cos(2 * PI * 1.2 * t)
                        chordsMix *= (0.4 + 0.3 * subFilter)
                    }
                    "synthwave" -> {
                        // Retro analog saw chords with high filtering sweep
                        var saw = 0.0
                        for (harm in 1..3) {
                            saw += sin(2 * PI * (chordBaseFreq * harm) * t) / harm
                        }
                        val sweep = 0.6 + 0.4 * sin(2 * PI * 0.2 * t)
                        chordsMix = saw * sweep
                    }
                    "lo-fi" -> {
                        // Warm Rhodes keys with vinyl noise and tremolo flutter
                        val flutter = 0.85 + 0.15 * sin(2 * PI * 5.5 * t)
                        chordsMix *= flutter * 0.7
                    }
                    "metal" -> {
                        // Heavy, aggressive thrash metal industrial fuzz
                        var fuzz = 0.0
                        for (harm in 1..5) {
                            fuzz += sin(2 * PI * (chordBaseFreq * harm) * t)
                        }
                        chordsMix = sign(fuzz) * 0.9
                    }
                    "edm" -> {
                        // Sidechained EDM Supersaw chords
                        var supersaw = 0.0
                        for (harm in 1..4) {
                            supersaw += sin(2 * PI * (chordBaseFreq * harm) * t)
                        }
                        // Sidechain compression synchronized with EDM kick beat
                        val beatRel = (t % beatDurationSec) / beatDurationSec
                        val sidechainDucking = 0.15 + 0.85 * (1.0 - exp(-15.0 * beatRel))
                        chordsMix = supersaw * sidechainDucking * 0.6
                    }
                    "country" -> {
                        // Gently strummed bright guitars
                        val strumIndex = (t % beatDurationSec * 12.0).toInt().coerceIn(1, 12)
                        chordsMix *= exp(-1.2 * (t % (beatDurationSec / 3.0))) * (1.2 / strumIndex)
                    }
                    "cinematic" -> {
                        // Slow lush orchestral backing strings
                        val slowLfo = 0.5 + 0.5 * sin(2 * PI * 0.1 * t)
                        chordsMix *= slowLfo
                    }
                }

                sampleVal += chordsMix * 0.24

                // B. Layer 2: Kick & Snare Drums (Rhythmic backing)
                val beatTime = t % beatDurationSec
                val totalBeats = (t / beatDurationSec).toInt()

                when (genre.lowercase()) {
                    "edm", "synthwave" -> {
                        // Four-on-the-floor kick on every single beat!
                        val kickEnv = exp(-32.0 * beatTime)
                        val kickOsc = sin(2 * PI * 56.0 * exp(-18.0 * beatTime) * beatTime)
                        sampleVal += kickOsc * kickEnv * 0.44

                        // Giant gated 80s / Club snare on beat 2 and 4
                        if (totalBeats % 2 == 1) {
                            val snareEnv = exp(-12.0 * beatTime)
                            val noise = r.nextGaussian()
                            sampleVal += noise * snareEnv * 0.18
                        }
                    }
                    "metal" -> {
                        // Double-bass drumrolls (extreme fast rolls on eighth steps)
                        val metalStep = beatDurationSec / 4.0 // 16th notes
                        val stepTime = t % metalStep
                        val kickEnv = exp(-45.0 * stepTime)
                        val kickOsc = sin(2 * PI * 65.0 * exp(-20.0 * stepTime) * stepTime)
                        sampleVal += kickOsc * kickEnv * 0.38

                        // Fast blasting snare on beat 2 and 4
                        if (totalBeats % 2 == 1) {
                            val snareEnv = exp(-22.0 * beatTime)
                            sampleVal += r.nextGaussian() * snareEnv * 0.16
                        }
                    }
                    "lo-fi" -> {
                        // Soft dusty boom-bap kick and lazy vinyl crackles
                        if (totalBeats % 2 == 0) {
                            val kickEnv = exp(-20.0 * beatTime)
                            val kickOsc = sin(2 * PI * 50.0 * exp(-8.0 * beatTime) * beatTime)
                            sampleVal += kickOsc * kickEnv * 0.35
                        }
                        // Crackling vinyl background ambient overlay
                        sampleVal += r.nextGaussian() * 0.02

                        // Laidback rim-shot clap snare
                        if (totalBeats % 2 == 1) {
                            val snareEnv = exp(-16.0 * beatTime)
                            sampleVal += r.nextGaussian() * snareEnv * 0.08
                        }
                    }
                    "country" -> {
                        // Acoustic side-stick and shuffle brushes
                        if (totalBeats % 2 == 0) {
                            val kickEnv = exp(-25.0 * beatTime)
                            val kickOsc = sin(2 * PI * 55.0 * exp(-12.0 * beatTime) * beatTime)
                            sampleVal += kickOsc * kickEnv * 0.28
                        }
                        // Shuffle brushes
                        val brushEnv = exp(-40.0 * (t % (beatDurationSec / 2.0)))
                        sampleVal += r.nextGaussian() * brushEnv * 0.04
                    }
                    else -> {
                        // Default energetic pop/hiphop layout
                        if (totalBeats % 2 == 0) {
                            val kickEnv = exp(-35.0 * beatTime)
                            val kickOsc = sin(2 * PI * 55.0 * exp(-15.0 * beatTime) * beatTime)
                            sampleVal += kickOsc * kickEnv * 0.40
                        }
                        if (totalBeats % 2 == 1) {
                            val snareEnv = exp(-18.0 * beatTime)
                            sampleVal += r.nextGaussian() * snareEnv * 0.15
                        }
                    }
                }

                // C. Layer 3: Bass Line
                val bassFreq = chordBaseFreq / 2.0 // Root bass frequency (one octave down)
                val bassOsc = when (genre.lowercase()) {
                    "hiphop", "edm" -> {
                        // Sliding fat 808 sub-bass glides
                        val slide = 1.0 + 0.10 * sin(2 * PI * t / beatDurationSec)
                        sin(2 * PI * bassFreq * slide * t)
                    }
                    "synthwave" -> {
                        // Octave-jumping bubble bass (pumping 16th bassline)
                        val stepIndex = (t / (beatDurationSec / 4.0)).toInt() % 4
                        val octaveMult = if (stepIndex % 2 == 1) 2.0 else 1.0
                        sin(2 * PI * (bassFreq * octaveMult) * t)
                    }
                    "metal" -> {
                        // Growling bass with heavy third-order harmonic drive
                        var saw = 0.0
                        for (harmonic in 1..3) {
                            saw += sin(2 * PI * (bassFreq * harmonic) * t) / harmonic
                        }
                        saw * 0.6
                    }
                    "country" -> {
                        // Country walking bass line (root, then fifth octave)
                        val beatNum = totalBeats % 4
                        val countryFreq = when (beatNum) {
                            0, 2 -> bassFreq         // Root
                            else -> bassFreq * 1.5   // Fifth
                        }
                        sin(2 * PI * countryFreq * t)
                    }
                    else -> sin(2 * PI * bassFreq * t) // Sincere warm bass
                }
                sampleVal += bassOsc * 0.28

                // D. Layer 4: Lead solo and arpeggio
                val stepTime = beatDurationSec / 2.0
                val noteStep = (t / stepTime).toInt() % 8
                val scale = when (barIndex) {
                    0 -> doubleArrayOf(220.0, 246.94, 261.63, 293.66, 329.63, 349.23, 392.00, 440.0) // A minor
                    else -> doubleArrayOf(196.00, 220.00, 246.94, 261.63, 293.66, 329.63, 392.00, 523.25)
                }
                val leadFreq = scale[noteStep] * 2.0

                var leadOsc = sin(2 * PI * leadFreq * t)
                when (genre.lowercase()) {
                    "pop", "edm" -> {
                        // Vibrant detuned synthesizer leads
                        leadOsc = if (leadOsc > 0) 0.6 else -0.6
                        val popLfo = sin(2 * PI * 6.5 * t)
                        leadOsc *= (0.7 + 0.3 * popLfo)
                    }
                    "synthwave" -> {
                        // Analog arpeggiator patterns climbing fast
                        val arpStep = beatDurationSec / 4.0
                        val arpIndex = (t / arpStep).toInt() % 4
                        val arpFreq = chordBaseFreq * when (arpIndex) {
                            0 -> 1.0
                            1 -> 1.2
                            2 -> 1.5
                            3 -> 2.0
                            else -> 1.0
                        }
                        leadOsc = sin(2 * PI * (arpFreq * 2.0) * t)
                    }
                    "metal" -> {
                        // Rapid shredding metal guitar solos
                        val metalStep = beatDurationSec / 4.0
                        val soloIndex = (t / metalStep).toInt() % 8
                        val soloFreq = scale[soloIndex] * 4.0 // High pitch sweep screeching
                        leadOsc = sin(2 * PI * soloFreq * t)
                        leadOsc = sign(leadOsc) * (1.1 - exp(-4.0 * abs(leadOsc))) // Ultra saturation
                    }
                    "lo-fi" -> {
                        // Muted moody arpeggio
                        leadOsc = sin(2 * PI * leadFreq * t) * (0.5 + 0.3 * sin(2 * PI * 1.5 * t))
                    }
                }

                val leadEnvelope = exp(-4.5 * (t % stepTime))
                sampleVal += leadOsc * leadEnvelope * 0.16

                // Master gain and hard limiting clipper to avoid digital noise clipping
                var finalSample = sampleVal * 0.72
                if (finalSample > 1.0) finalSample = 1.0
                if (finalSample < -1.0) finalSample = -1.0

                // Convert to 16-bit PCM Short
                val sampleShort = (finalSample * 32767.0).toInt().toShort()
                out.write(shortToByteArray(sampleShort))

                if (i % 25000 == 0) {
                    onProgress(i.toFloat() / numSamples)
                }
            }
        }
        onProgress(1.0f)
        Log.d(TAG, "Audio successfully synthesized: ${audioFile.absolutePath}")
        audioFile
    }

    /**
     * Synthesizes a completely finished AI Song (Music Backing + Singing Vocals) using high-fidelity
     * additive wave synthesis, formant filters, portamento glides, and acoustic resonance effects.
     */
    suspend fun generateFinishedSongWav(
        context: Context,
        genre: String,
        lyrics: String,
        durationSec: Int = 15,
        projectBpm: Int = 120,
        promptStyle: String = "",
        negativePrompt: String = "",
        soundInfluencePercent: Float = 80f,
        styleInfluencePercent: Float = 75f,
        vocalVoice: String = "Human",
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val cacheDir = context.cacheDir
        val audioFile = File(cacheDir, "spark_ai_finished_song_${System.currentTimeMillis()}.wav")

        val sampleRate = 22050
        val bitsPerSample = 16
        val numChannels = 1
        val numSamples = sampleRate * durationSec
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)
        val dataSize = numSamples * blockAlign
        val totalFileSize = 36 + dataSize

        BufferedOutputStream(FileOutputStream(audioFile)).use { out ->
            // 1. Write WAV Header
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

            val bpm = projectBpm.coerceAtLeast(40).toDouble()
            val beatDurationSec = 60.0 / bpm
            val r = java.util.Random(System.currentTimeMillis())

            // Sizing influences
            val backingScale = (soundInfluencePercent / 100f).coerceIn(0f, 2f)
            val effectIntensity = (styleInfluencePercent / 75f).coerceIn(0f, 2.5f)

            // Initialize studio analog echo/delay line for vocaloid track
            val delayBufferSize = (sampleRate * 0.30).toInt() // 300ms feedback delay
            val delayBuffer = FloatArray(delayBufferSize) { 0f }
            var delayIndex = 0

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                var sampleVal = 0.0

                // 4-Bar Chord Progression Base Frequencies
                val barIndex = (t / (beatDurationSec * 4)).toInt() % 4
                val chordBaseFreq = when (barIndex) {
                    0 -> 220.0  // A3
                    1 -> 165.0  // E3
                    2 -> 196.0  // G3
                    3 -> 146.83 // D3
                    else -> 220.0
                }

                // A. Layer 1: Professional Instrumental Harmony Chords (Pads/Keys)
                var chordsMix = 0.0
                when (genre.lowercase()) {
                    "pop" -> {
                        // Shimmering FM synth bell-keys (FM synthesis)
                        val fMod = chordBaseFreq * 2.0
                        val bell1 = sin(2 * PI * chordBaseFreq * t + (0.40 * effectIntensity) * sin(2 * PI * fMod * t))
                        val bell2 = sin(2 * PI * (chordBaseFreq * 1.5) * t + (0.25 * effectIntensity) * sin(2 * PI * (fMod * 1.5) * t))
                        chordsMix = (bell1 + bell2) * 0.5 * (0.6 + 0.4 * sin(2 * PI * 0.35 * t))
                    }
                    "rock" -> {
                        // Gritty hand-crafted double-tracked electric guitar chords
                        var saw = 0.0
                        for (harm in 1..4) {
                            saw += sin(2 * PI * (chordBaseFreq * harm) * t) / harm
                        }
                        // Tube-compress and soft-clip saturated drive based on style influence
                        val driveVal = 1.8 * effectIntensity
                        val drive = saw * driveVal
                        chordsMix = sign(drive) * (1.0 - exp(-2.8 * abs(drive))) * 0.55
                    }
                    "edm" -> {
                        // sidechained EDM Super-Saw with adjustable detune intensity
                        var supersaw = 0.0
                        val detuneSpread = 0.015 * effectIntensity
                        for (detune in listOf(1.0 - detuneSpread, 1.0, 1.0 + detuneSpread)) {
                            val phase = 2 * PI * chordBaseFreq * detune * t
                            supersaw += ((phase % (2 * PI)) / PI) - 1.0
                        }
                        // Sidechain compression synchronized with EDM kick beat
                        val beatRel = (t % beatDurationSec) / beatDurationSec
                        val sidechain = 0.15 + 0.85 * (1.0 - exp(-15.0 * beatRel))
                        chordsMix = (supersaw / 3.0) * sidechain * 0.65
                    }
                    "synthwave" -> {
                        // Retro analog sweep sawtooth chords from the 80s
                        var saw = 0.0
                        val detuneSpread = 0.007 * effectIntensity
                        for (detune in listOf(1.0 - detuneSpread, 1.0, 1.0 + detuneSpread)) {
                            val phase = 2 * PI * chordBaseFreq * detune * t
                            saw += ((phase % (2 * PI)) / PI) - 1.0
                        }
                        // Slow high-frequency lowpass sweeping
                        val scaleSweep = 0.5 + 0.5 * sin(2 * PI * 0.25 * t)
                        chordsMix = (saw / 3.0) * scaleSweep * 0.70
                    }
                    "lo-fi" -> {
                        // Soft tape-warped vintage Rhodes electric piano with adjustable flutter
                        val flutterSpeed = 4.8 * effectIntensity
                        val flutter = 0.94 + 0.06 * sin(2 * PI * flutterSpeed * t)
                        val key1 = sin(2 * PI * chordBaseFreq * t)
                        val key2 = sin(2 * PI * (chordBaseFreq * 1.25) * t) * 0.3
                        val key3 = sin(2 * PI * (chordBaseFreq * 1.5) * t) * 0.15
                        chordsMix = (key1 + key2 + key3) * flutter * 0.55
                    }
                    "acoustic" -> {
                        // Beautiful steel-string resonant acoustic guitar arpeggios
                        val pluckTime = t % (beatDurationSec * 0.5)
                        val pluckEnv = exp(-4.5 * pluckTime)
                        val pluck1 = sin(2 * PI * chordBaseFreq * t) * pluckEnv
                        val pluck2 = sin(2 * PI * (chordBaseFreq * 1.5) * t) * exp(-6.5 * pluckTime) * 0.45
                        chordsMix = (pluck1 + pluck2) * 0.85
                    }
                    "metal" -> {
                        // Extremely brutal scooped industrial buzz guitar
                        var metalSaw = 0.0
                        for (harm in 1..6) {
                            metalSaw += sin(2 * PI * (chordBaseFreq * harm) * t) / harm
                        }
                        chordsMix = if (metalSaw >= 0.0) 0.52 else -0.52
                    }
                    else -> {
                        // Soft soothing ambient pad
                        val chordOsc1 = sin(2 * PI * chordBaseFreq * t)
                        val chordOsc2 = sin(2 * PI * (chordBaseFreq * 1.25) * t)
                        val chordOsc3 = sin(2 * PI * (chordBaseFreq * 1.5) * t)
                        chordsMix = ((chordOsc1 + chordOsc2 + chordOsc3) / 3.0) * (0.7 + 0.3 * sin(2 * PI * 0.2 * t))
                    }
                }
                sampleVal += chordsMix * 0.28 * backingScale

                // B. Layer 2: Custom Genre-tailored Basslines
                var bassLine = 0.0
                val bassFreq = chordBaseFreq * 0.5
                when (genre.lowercase()) {
                    "synthwave", "edm" -> {
                        // Pumping 8th note sequenced bass
                        val stepIndex = (t / (beatDurationSec / 2.0)).toInt() % 4
                        val octaveMult = if (stepIndex % 2 == 1) 2.0 else 1.0
                        val bassOsc = sin(2 * PI * (bassFreq * octaveMult) * t)
                        val bassTime = t % (beatDurationSec / 2.0)
                        val bassEnv = exp(-9.0 * bassTime)
                        bassLine = bassOsc * bassEnv * 0.34
                    }
                    "rock", "metal" -> {
                        // Saturated growling picked bass guitar
                        val subBass = sin(2 * PI * bassFreq * t)
                        val growl = subBass + 0.32 * sin(2 * PI * (bassFreq * 3.0) * t)
                        bassLine = growl * 0.25
                    }
                    "lo-fi", "hiphop" -> {
                        // Deep melting tape-sub bass
                        val smoothSub = sin(2 * PI * bassFreq * t)
                        val subLfo = 0.8 + 0.2 * sin(2 * PI * 0.85 * t)
                        bassLine = smoothSub * subLfo * 0.28
                    }
                    "pop" -> {
                        // Bouncy slap bass sequence
                        val popStep = (t / (beatDurationSec / 2.0)).toInt() % 8
                        val hasPluck = popStep in listOf(0, 2, 3, 5, 6)
                        val popBassEnv = if (hasPluck) exp(-11.0 * (t % (beatDurationSec / 2.0))) else 0.0
                        bassLine = sin(2 * PI * bassFreq * t) * popBassEnv * 0.26
                    }
                    "acoustic" -> {
                        // Tactile acoustic double-bass
                        val aTime = t % beatDurationSec
                        val acousticBassEnv = exp(-4.0 * aTime)
                        bassLine = sin(2 * PI * bassFreq * t) * acousticBassEnv * 0.24
                    }
                    else -> {
                        // Sincere deep warm bass waves
                        val softBass = sin(2 * PI * bassFreq * t)
                        val softEnv = 0.6 + 0.4 * sin(2 * PI * (bpm / 60.0) * t)
                        bassLine = softBass * softEnv * 0.20
                    }
                }
                sampleVal += bassLine * backingScale

                // C. Layer 3: Dynamic Genre-Specific Drum Machines
                val beatTime = t % beatDurationSec
                val totalBeats = (t / beatDurationSec).toInt()
                val beatInBar = totalBeats % 4
                var drumMix = 0.0

                when (genre.lowercase()) {
                    "pop" -> {
                        // Crisp punchy urban pop layout
                        // Kick on beat 1 and 3
                        if (beatInBar == 0 || beatInBar == 2) {
                            val kickEnv = exp(-28.0 * beatTime)
                            val kickOsc = sin(2 * PI * 58.0 * exp(-13.0 * beatTime) * beatTime)
                            drumMix += kickOsc * kickEnv * 0.38
                        }
                        // Snap/snare handclap on beat 2 and 4
                        if (beatInBar == 1 || beatInBar == 3) {
                            val snareEnv = exp(-18.0 * beatTime)
                            drumMix += r.nextGaussian() * snareEnv * 0.18
                        }
                        // Continuous high-pitch hi-hat ticks on every eighth-step
                        val subEight = t % (beatDurationSec / 2.0)
                        drumMix += r.nextGaussian() * exp(-55.0 * subEight) * 0.04
                    }
                    "rock" -> {
                        // Power double-head acoustic rock kit
                        // Kick on beat 1, 3 and offbeat of 3
                        val isKickTime = (beatInBar == 0 || beatInBar == 2 || (beatInBar == 3 && beatTime > beatDurationSec * 0.5))
                        if (isKickTime) {
                            val kt = if (beatInBar == 3 && beatTime > beatDurationSec * 0.5) beatTime - beatDurationSec * 0.5 else beatTime
                            val kickEnv = exp(-23.0 * kt)
                            val kickOsc = sin(2 * PI * 66.0 * exp(-10.0 * kt) * kt)
                            drumMix += kickOsc * kickEnv * 0.40
                        }
                        // Massive high-stick snare on 2 and 4
                        if (beatInBar == 1 || beatInBar == 3) {
                            val snareEnv = exp(-13.0 * beatTime)
                            drumMix += r.nextGaussian() * snareEnv * 0.21
                        }
                        // Closed hi-hats sizzling on 8th steps
                        drumMix += r.nextGaussian() * exp(-42.0 * (t % (beatDurationSec / 2.0))) * 0.05
                        // Splash crash crash-cymbal on bar lines
                        if (totalBeats % 16 == 0) {
                            drumMix += r.nextGaussian() * exp(-2.2 * beatTime) * 0.15
                        }
                    }
                    "edm" -> {
                        // Heavy, chest-pumping electronic club layout
                        // Gigantic club sub-kick on every single beat!
                        val kickEnv = exp(-35.0 * beatTime)
                        val kickOsc = sin(2 * PI * 60.0 * exp(-17.0 * beatTime) * beatTime)
                        drumMix += kickOsc * kickEnv * 0.46
                        // Stereo electronic white-noise clap on 2 and 4
                        if (beatInBar == 1 || beatInBar == 3) {
                            val clapEnv = exp(-15.0 * beatTime)
                            drumMix += r.nextGaussian() * clapEnv * 0.21
                        }
                        // Sharp sizzling open hi-hat ticking on the offbeats
                        val isOffbeat = beatTime > beatDurationSec * 0.45 && beatTime < beatDurationSec * 0.75
                        if (isOffbeat) {
                            val hatEnv = exp(-24.0 * (beatTime - beatDurationSec * 0.5))
                            drumMix += r.nextGaussian() * hatEnv * 0.075
                        }
                    }
                    "synthwave" -> {
                        // Warm retro LinnDrum style layout
                        // Retro four-on-the-floor kick
                        val kickEnv = exp(-32.0 * beatTime)
                        val kickOsc = sin(2 * PI * 55.0 * exp(-14.0 * beatTime) * beatTime)
                        drumMix += kickOsc * kickEnv * 0.42
                        // Gated 80s reverb snare on 2 and 4
                        if (beatInBar == 1 || beatInBar == 3) {
                            val snareEnv = exp(-8.5 * beatTime) // Extended wet decay
                            drumMix += r.nextGaussian() * snareEnv * 0.19
                        }
                        // 8th note rolling retro hi-hats
                        drumMix += r.nextGaussian() * exp(-38.0 * (t % (beatDurationSec / 2.0))) * 0.05
                    }
                    "lo-fi" -> {
                        // Cozy retro tape-record environment noise
                        val dustNoise = 0.026 * effectIntensity
                        sampleVal += r.nextGaussian() * dustNoise
                        // Lazy, unquantized dusty swing boom-bap kick
                        val swingOffset = beatDurationSec * 0.07
                        val isLofiKick = (beatInBar == 0 && beatTime < beatDurationSec * 0.5) ||
                                (beatInBar == 2 && beatTime > swingOffset && beatTime < swingOffset + 0.3)
                        if (isLofiKick) {
                            val kt = if (beatInBar == 2) beatTime - swingOffset else beatTime
                            val kickEnv = exp(-19.0 * kt)
                            val kickOsc = sin(2 * PI * 49.0 * exp(-9.0 * kt) * kt)
                            drumMix += kickOsc * kickEnv * 0.33
                        }
                        // Crisp wooden rimshot / lazy handclap on beat 2 and 4
                        if (beatInBar == 1 || beatInBar == 3) {
                            val snareEnv = exp(-21.0 * beatTime)
                            drumMix += r.nextGaussian() * snareEnv * 0.12
                        }
                        // Gentle swing shaker
                        drumMix += r.nextGaussian() * exp(-34.0 * (t % (beatDurationSec / 2.0))) * 0.03
                    }
                    "acoustic" -> {
                        // Intimate wooden acoustic cajon and handheld shaker (No digital beeps!)
                        // Decayed wooden cajon "thump" on beat 1 and 3
                        if (beatInBar == 0 || beatInBar == 2) {
                            val kickEnv = exp(-17.0 * beatTime)
                            val kickOsc = sin(2 * PI * 68.0 * exp(-11.0 * beatTime) * beatTime)
                            drumMix += kickOsc * kickEnv * 0.28
                        }
                        // Soft cajon edge-slap / acoustic brush tap on beat 2 and 4
                        if (beatInBar == 1 || beatInBar == 3) {
                            val brushEnv = exp(-28.0 * beatTime)
                            drumMix += r.nextGaussian() * brushEnv * 0.08
                        }
                        // Shimmering handheld shaker on 8th steps
                        drumMix += r.nextGaussian() * exp(-38.0 * (t % (beatDurationSec / 2.0))) * 0.03
                    }
                    else -> {
                        // Default energetic pop layout
                        if (totalBeats % 2 == 0) {
                            val kickEnv = exp(-35.0 * beatTime)
                            val kickOsc = sin(2 * PI * 55.0 * exp(-15.0 * beatTime) * beatTime)
                            drumMix += kickOsc * kickEnv * 0.36
                        }
                        if (totalBeats % 2 == 1) {
                            val snareEnv = exp(-18.0 * beatTime)
                            drumMix += r.nextGaussian() * snareEnv * 0.15
                        }
                    }
                }
                sampleVal += drumMix * backingScale

                // D. Layer 4: High-Fidelity Vocal synthesis (with adjustable style parameters and pitch styles)
                val vocalBeatIndex = (t / (beatDurationSec * 1.5)).toInt()
                val vocalBaseFreq = when (barIndex) {
                    0 -> when (vocalBeatIndex % 4) {
                        0 -> 440.0  // A4
                        1 -> 493.88 // B4
                        2 -> 523.25 // C5
                        3 -> 587.33 // D5
                        else -> 440.0
                    }
                    1 -> when (vocalBeatIndex % 4) {
                        0 -> 329.63 // E4
                        1 -> 392.00 // G4
                        2 -> 440.00 // A4
                        3 -> 493.88 // B4
                        else -> 329.63
                    }
                    2 -> when (vocalBeatIndex % 4) {
                        0 -> 392.00 // G4
                        1 -> 440.00 // A4
                        2 -> 493.88 // B4
                        3 -> 587.33 // D5
                        else -> 392.00
                    }
                    3 -> when (vocalBeatIndex % 4) {
                        0 -> 293.66 // D4
                        1 -> 329.63 // E4
                        2 -> 392.00 // G4
                        3 -> 440.00 // A4
                        else -> 293.66
                    }
                    else -> 440.0
                }

                // Portamento smooth slide
                val glideTime = t % (beatDurationSec * 1.5)
                val prevVocalFreq = when (barIndex) {
                    0 -> 392.0
                    1 -> 440.0
                    2 -> 329.63
                    3 -> 392.0
                    else -> 440.0
                }
                
                // Determine pitch attributes depending on voice style
                val autotuned = vocalVoice.lowercase() == "robot" || vocalVoice.lowercase() == "vocoder"
                val currentVocalFreq = if (autotuned) {
                    // Robot autotuned has no glide (instant pitch changes)
                    vocalBaseFreq
                } else {
                    prevVocalFreq + (vocalBaseFreq - prevVocalFreq) * (1.0 - exp(-15.0 * glideTime))
                }

                // Pitch vibrato
                val vibratoSwell = if (autotuned) 0.0 else 1.0 - exp(-3.0 * glideTime)
                val vibratoRate = 6.2
                val vibratoDepth = if (vocalVoice.lowercase() == "human") 0.019 else 0.013
                val vibrato = 1.0 + vibratoDepth * vibratoSwell * sin(2 * PI * vibratoRate * t)

                // 1. Generate core vocal glottal pulse 
                val vocalPhase = (currentVocalFreq * vibrato) * t
                val vp = vocalPhase % 1.0
                val glottalPulse = if (vp < 0.28) {
                    vp / 0.28
                } else if (vp < 0.78) {
                    (0.78 - vp) / 0.50
                } else {
                    0.0
                } - 0.50

                // 2. Formant filtering (modeling human vowel cavities)
                val vowelCycle = (t * 2.5).toInt() % 5
                val (formantF1, formantF2) = when (vowelCycle) {
                    0 -> Pair(730.0, 1090.0) // "Ah"
                    1 -> Pair(530.0, 1840.0) // "Eh"
                    2 -> Pair(270.0, 2290.0) // "Ee"
                    3 -> Pair(570.0, 840.0)  // "Oh"
                    4 -> Pair(300.0, 870.0)  // "Oo"
                    else -> Pair(500.0, 1000.0)
                }

                val formantTone1 = sin(2 * PI * formantF1 * t)
                val formantTone2 = sin(2 * PI * formantF2 * t)
                var vocalFiltered = glottalPulse * 0.35 + (formantTone1 * 0.42 + formantTone2 * 0.32) * abs(glottalPulse)

                // Give it advanced robot/vocoder ring modulation feel if enabled
                if (vocalVoice.lowercase() == "robot" || vocalVoice.lowercase() == "vocoder") {
                    val modOsc = sin(2 * PI * 85.0 * t)
                    vocalFiltered = (vocalFiltered * 0.40 + (vocalFiltered * modOsc) * 0.60) * 1.2
                }

                // Gate articulate between words
                val vocalGate = if (glideTime > (beatDurationSec * 1.35)) 0.0 else 1.0
                var originalVocalSample = vocalFiltered * vocalGate * 0.44

                // If "Duet" is selected, double tap with a rich harmony singer (a third higher/lower harmonizing)
                if (vocalVoice.lowercase() == "duet") {
                    val harmonizeFreq = currentVocalFreq * 1.25 // Major third harmony!
                    val vpHarm = (harmonizeFreq * vibrato) * t % 1.0
                    val glottalPulseHarm = if (vpHarm < 0.28) {
                        vpHarm / 0.28
                    } else if (vpHarm < 0.78) {
                        (0.78 - vpHarm) / 0.50
                    } else {
                        0.0
                    } - 0.50
                    val formantHarmTone1 = sin(2 * PI * formantF1 * 1.25 * t)
                    val formantHarmTone2 = sin(2 * PI * formantF2 * 1.25 * t)
                    val vocalFilteredHarm = glottalPulseHarm * 0.35 + (formantHarmTone1 * 0.42 + formantHarmTone2 * 0.32) * abs(glottalPulseHarm)
                    val originalVocalSampleHarm = vocalFilteredHarm * vocalGate * 0.32 // slightly softer harmony level
                    
                    originalVocalSample = originalVocalSample * 0.85 + originalVocalSampleHarm
                }

                // 3. Studio Delay (Echo) Line synthesis
                val delayedVocal = delayBuffer[delayIndex]
                val delayFeedback = 0.44f * (styleInfluencePercent / 75f).coerceIn(0f, 0.85f)
                delayBuffer[delayIndex] = (originalVocalSample + delayedVocal * delayFeedback).toFloat()
                delayIndex = (delayIndex + 1) % delayBufferSize

                // Master Vocals Mix (Raw singer + studio echo delay)
                val vocalsMix = originalVocalSample + delayedVocal * 0.33
                sampleVal += vocalsMix

                // E. Master dynamic compressor & clipping safeguard
                var finalSample = sampleVal * 0.65
                if (finalSample > 1.0) finalSample = 1.0
                if (finalSample < -1.0) finalSample = -1.0

                val sampleShort = (finalSample * 32767.0).toInt().toShort()
                out.write(shortToByteArray(sampleShort))

                if (i % 20000 == 0) {
                    onProgress(i.toFloat() / numSamples)
                }
            }
        }
        onProgress(1.0f)
        Log.d(TAG, "Completed AI Finished Song synthesis: ${audioFile.absolutePath}")
        audioFile
    }

    /**
     * Renders a real MP4 video with custom scrolling animations, particle storms, and graphics inside a
     * native encoder using MediaCodec and MediaMuxer.
     */
    suspend fun generateRealLyricVideo(
        context: Context,
        lyrics: String,
        genre: String,
        audioFile: File,
        durationSec: Int,
        onProgress: (Float) -> Unit
    ): File {
        return generateRealLyricVideo(context, lyrics, genre, audioFile, durationSec, "", onProgress)
    }

    suspend fun generateRealLyricVideo(
        context: Context,
        lyrics: String,
        genre: String,
        audioFile: File,
        durationSec: Int,
        visualMood: String,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val cacheDir = context.cacheDir
        val videoFile = File(cacheDir, "spark_composition_${System.currentTimeMillis()}.mp4")

        val width = 360
        val height = 640
        val frameRate = 15
        val bitRate = 800000 // 800 kbps
        val maxFrames = frameRate * durationSec

        // Setup MediaCodec Video Encoder
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3) // Key frame every 3s
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        // Setup MediaMuxer
        val muxer = MediaMuxer(videoFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        // Paint components for graphic render
        val bgPaint = Paint().apply { isAntiAlias = true }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val titlePaint = Paint().apply {
            color = Color.parseColor("#00FFC2") // Glowing turquoise cinematic text
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }

        // Clean lyrics into list of lines
        val lyricLines = lyrics.lines().filter { it.isNotBlank() }

        // Timing indices
        val bufferInfo = MediaCodec.BufferInfo()
        var frame = 0

        while (frame < maxFrames) {
            val ptsUs = (frame * 1_000_000L / frameRate)

            // 1. Draw frame to Surface Canvas
            val canvas: Canvas? = inputSurface.lockCanvas(null)
            if (canvas != null) {
                try {
                    val floatFrame = frame.toFloat()
                    
                    // A. Determine visual mood colors based on genre or visualMood analyzing
                    val moodLower = visualMood.lowercase()
                    val customColorStart = if (moodLower.contains("fialov") || moodLower.contains("purple")) {
                        Color.parseColor("#1C003B")
                    } else if (moodLower.contains("zelen") || moodLower.contains("green")) {
                        Color.parseColor("#002919")
                    } else if (moodLower.contains("modr") || moodLower.contains("blue")) {
                        Color.parseColor("#001B33")
                    } else if (moodLower.contains("zlat") || moodLower.contains("gold") || moodLower.contains("žlut")) {
                        Color.parseColor("#2A1E00")
                    } else if (moodLower.contains("červen") || moodLower.contains("red")) {
                        Color.parseColor("#2B0000")
                    } else {
                        null
                    }

                    // Rich Gradient Background corresponding to selected genre/vibe
                    val colorStart = customColorStart ?: when (genre.lowercase()) {
                        "pop" -> Color.parseColor("#140526")
                        "rock" -> Color.parseColor("#22070D")
                        "hiphop" -> Color.parseColor("#050818")
                        "synthwave" -> Color.parseColor("#060318")
                        "lo-fi" -> Color.parseColor("#0A0F1E")
                        "metal" -> Color.parseColor("#1D0404")
                        "edm" -> Color.parseColor("#03031C")
                        "country" -> Color.parseColor("#2B1501")
                        else -> Color.parseColor("#0C0F1E")
                    }
                    val colorEnd = Color.parseColor("#020104")
                    bgPaint.shader = LinearGradient(
                        0f, 0f, 0f, height.toFloat(),
                        colorStart, colorEnd, Shader.TileMode.CLAMP
                    )
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                    // Reset shader for vector shapes
                    bgPaint.shader = null

                    // CAMERA MOVEMENT - Cinematic subtle tilt and zoom-in
                    canvas.save()
                    val zoomScale = 1.0f + 0.04f * (floatFrame / maxFrames)
                    val panDeltaX = sin(floatFrame * 0.02f) * 6f
                    val panDeltaY = cos(floatFrame * 0.015f) * 4f
                    canvas.translate(width / 2f + panDeltaX, height / 2f + panDeltaY)
                    canvas.scale(zoomScale, zoomScale)
                    canvas.translate(-width / 2f, -height / 2f)

                    // 2. Draw 100% Real Procedural Cinematic Scenes based on selected genre (with silhouettes of people, landscapes, objects)
                    when (genre.lowercase()) {
                        "synthwave" -> {
                            // --- SUNSET RETRO HIGHWAY GRID LANDSCAPE ---
                            // A. Draw Neon Retro Sun
                            val sunX = width / 2f
                            val sunY = height * 0.38f
                            val sunRadius = 75f
                            val sunPaint = Paint().apply {
                                isAntiAlias = true
                                shader = LinearGradient(
                                    sunX, sunY - sunRadius, sunX, sunY + sunRadius,
                                    Color.parseColor("#FF007F"), Color.parseColor("#FFD700"), Shader.TileMode.CLAMP
                                )
                            }
                            canvas.drawCircle(sunX, sunY, sunRadius, sunPaint)

                            // Retro horizontal band slits over sun
                            val slatPaint = Paint().apply {
                                color = colorEnd
                                style = Paint.Style.FILL
                            }
                            var slatY = sunY - sunRadius + 15f
                            while (slatY < sunY + sunRadius) {
                                val thickness = 3.5f + (slatY - sunY) * 0.05f
                                if (thickness > 0) {
                                    canvas.drawRect(sunX - sunRadius - 5f, slatY, sunX + sunRadius + 5f, slatY + thickness, slatPaint)
                                }
                                slatY += 14f
                            }

                            // B. Futuristic Cyber Skyline Silhouette
                            val skyPaint = Paint().apply {
                                color = Color.parseColor("#150A26")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val sPath = android.graphics.Path()
                            sPath.moveTo(0f, height * 0.52f)
                            val buildingWidths = listOf(35f, 45f, 55f, 40f, 50f, 45f, 35f, 60f)
                            val buildingHeights = listOf(70f, 110f, 80f, 130f, 95f, 120f, 75f, 105f)
                            var curX = 0f
                            for (bi in buildingWidths.indices) {
                                val bW = buildingWidths[bi]
                                val bH = buildingHeights[bi] + sin(floatFrame * 0.04f + bi) * 5f
                                sPath.lineTo(curX, height * 0.52f - bH)
                                sPath.lineTo(curX + bW, height * 0.52f - bH)
                                curX += bW
                            }
                            sPath.lineTo(width.toFloat(), height * 0.52f)
                            sPath.close()
                            canvas.drawPath(sPath, skyPaint)

                            // Glowing futuristic windows in skyscrapers
                            val winPaint = Paint().apply {
                                color = Color.parseColor("#FFFF2E")
                                style = Paint.Style.FILL
                            }
                            for (wi in 0..11) {
                                val wx = (wi * 32) % width.toFloat()
                                val wy = (height * 0.35f + wi * 15f) % (height * 0.15f) + height * 0.32f
                                if (wy < height * 0.50f) {
                                    winPaint.alpha = (100 + 155 * abs(sin(floatFrame * 0.05f + wi))).toInt().coerceIn(0, 255)
                                    canvas.drawRect(wx + 2f, wy, wx + 6f, wy + 8f, winPaint)
                                    canvas.drawRect(wx + 10f, wy, wx + 14f, wy + 8f, winPaint)
                                }
                            }

                            // C. Draw scrolling perspective grid below horizon
                            val gridPaint = Paint().apply {
                                color = Color.parseColor("#FF007F")
                                strokeWidth = 1.8f
                                style = Paint.Style.STROKE
                                isAntiAlias = true
                            }
                            val horizonY = height * 0.52f
                            canvas.drawLine(0f, horizonY, width.toFloat(), horizonY, gridPaint)

                            // Perspective lines scaling out
                            val numLines = 12
                            for (li in 0..numLines) {
                                val xH = (width / 2f) + (li - numLines / 2f) * 6f
                                val xB = (width / 2f) + (li - numLines / 2f) * 75f
                                canvas.drawLine(xH, horizonY, xB, height.toFloat(), gridPaint)
                            }

                            // Horizontal scrolling lines
                            val gridSpacing = 30f
                            var gridY = horizonY + (floatFrame * 5.0f) % gridSpacing
                            while (gridY < height) {
                                val ratio = (gridY - horizonY) / (height - horizonY)
                                val actualY = horizonY + ratio * ratio * (height - horizonY)
                                gridPaint.alpha = (ratio * 255).toInt().coerceIn(0, 255)
                                canvas.drawLine(0f, actualY, width.toFloat(), actualY, gridPaint)
                                gridY += gridSpacing
                            }

                            // D. Sports Car Silhouette speeding on highway
                            val carPaint = Paint().apply {
                                color = Color.parseColor("#090214")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val carPath = android.graphics.Path()
                            val carBaseY = height * 0.88f + sin(floatFrame * 0.4f) * 1.5f // Engine vibration!
                            val carStartX = width * 0.28f
                            carPath.moveTo(carStartX, carBaseY)
                            carPath.lineTo(carStartX + 12f, carBaseY - 14f)
                            carPath.lineTo(carStartX + 52f, carBaseY - 15f)
                            carPath.lineTo(carStartX + 70f, carBaseY - 32f) // Roof slope
                            carPath.lineTo(carStartX + 115f, carBaseY - 30f)
                            carPath.lineTo(carStartX + 130f, carBaseY - 12f)
                            carPath.lineTo(carStartX + 155f, carBaseY - 10f)
                            carPath.lineTo(carStartX + 152f, carBaseY)
                            carPath.close()
                            canvas.drawPath(carPath, carPaint)

                            // Spinning neon wheels
                            val wheelPaint = Paint().apply {
                                color = Color.parseColor("#00FFC2")
                                style = Paint.Style.STROKE
                                strokeWidth = 3f
                                isAntiAlias = true
                            }
                            canvas.drawCircle(carStartX + 35f, carBaseY, 11f, carPaint)
                            canvas.drawCircle(carStartX + 115f, carBaseY, 11f, carPaint)
                            canvas.drawCircle(carStartX + 35f, carBaseY, 11f, wheelPaint)
                            canvas.drawCircle(carStartX + 115f, carBaseY, 11f, wheelPaint)

                            // Wheel spokes spinning
                            val angle = floatFrame * 18f
                            val sX1 = (carStartX + 35f) + 11f * cos(Math.toRadians(angle.toDouble())).toFloat()
                            val sY1 = carBaseY + 11f * sin(Math.toRadians(angle.toDouble())).toFloat()
                            val sX2 = (carStartX + 35f) - 11f * cos(Math.toRadians(angle.toDouble())).toFloat()
                            val sY2 = carBaseY - 11f * sin(Math.toRadians(angle.toDouble())).toFloat()
                            canvas.drawLine(carStartX + 35f, carBaseY, sX1, sY1, wheelPaint)
                            canvas.drawLine(carStartX + 35f, carBaseY, sX2, sY2, wheelPaint)
                        }

                        "lo-fi" -> {
                            // --- COZY WINDOW LOOKING OUT ON RAINY HARBOR SKYSCRAPERS ---
                            // Dark blue night scenery behind
                            val nightViewPaint = Paint().apply {
                                color = Color.parseColor("#0F1A30")
                                style = Paint.Style.FILL
                            }
                            // Window glass layout
                            canvas.drawRect(0f, 0f, width.toFloat(), height * 0.60f, nightViewPaint)

                            // Shimmering stars/city lights in distant dark background
                            val faintStarPaint = Paint().apply {
                                color = Color.parseColor("#80DDF2FF")
                                style = Paint.Style.FILL
                            }
                            for (si in 0..12) {
                                val sPhase = floatFrame * 0.05f + si * 1.4f
                                val sx = (si * 31) % width.toFloat()
                                val sy = (si * 23) % (height * 0.46f) + 30f
                                val starRadius = 1.0f + 1.2f * abs(sin(sPhase))
                                canvas.drawCircle(sx, sy, starRadius, faintStarPaint)
                            }

                            // Rain streaks hitting the glass/windowpane
                            val rainPaint = Paint().apply {
                                color = Color.parseColor("#3FAED8F2")
                                strokeWidth = 2.0f
                                isAntiAlias = true
                            }
                            for (ri in 0..18) {
                                val rSpeed = 14f
                                val rOffset = ri * 37f
                                val ry = ((floatFrame * rSpeed + rOffset) % (height * 0.52f)) + 15f
                                val rx = (ri * 23) % width.toFloat() - (ry * 0.15f)
                                canvas.drawLine(rx, ry, rx - 3f, ry + 12f, rainPaint)
                            }

                            // Window wooden panel frames overlay
                            val woodPaint = Paint().apply {
                                color = Color.parseColor("#20192B")
                                strokeWidth = 14f
                                style = Paint.Style.STROKE
                                isAntiAlias = true
                            }
                            // Border & division bar
                            canvas.drawRect(0f, 0f, width.toFloat(), height * 0.60f, woodPaint)
                            canvas.drawLine(width / 2f, 0f, width / 2f, height * 0.60f, woodPaint)
                            canvas.drawLine(0f, height * 0.30f, width.toFloat(), height * 0.30f, woodPaint)

                            // Table surface
                            val tablePaint = Paint().apply {
                                color = Color.parseColor("#14101F")
                                style = Paint.Style.FILL
                            }
                            canvas.drawRect(0f, height * 0.58f, width.toFloat(), height.toFloat(), tablePaint)

                            // Warm glowing vintage lamp radial gradient glow
                            val lampColor = Paint().apply {
                                isAntiAlias = true
                                shader = RadialGradient(
                                    width - 45f, height * 0.62f, 150f,
                                    Color.parseColor("#B8FFB833"), Color.TRANSPARENT, Shader.TileMode.CLAMP
                                )
                            }
                            canvas.drawCircle(width - 45f, height * 0.62f, 150f, lampColor)

                            // Sleeping Cat silhouette on table
                            val catPaint = Paint().apply {
                                color = Color.parseColor("#09070F")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val catX = width * 0.22f
                            val catBaseY = height * 0.82f
                            val catBreathing = abs(sin(floatFrame * 0.08f)) * 3f // Breathing animation!
                            // Draw cat body curl
                            canvas.drawOval(catX - 35f, catBaseY - 22f - catBreathing, catX + 35f, catBaseY, catPaint)
                            // Cat head
                            canvas.drawCircle(catX + 22f, catBaseY - 24f - catBreathing * 0.8f, 14f, catPaint)
                            // Cat ears
                            val earPath = android.graphics.Path()
                            earPath.moveTo(catX + 14f, catBaseY - 33f)
                            earPath.lineTo(catX + 17f, catBaseY - 45f)
                            earPath.lineTo(catX + 26f, catBaseY - 35f)
                            earPath.close()
                            canvas.drawPath(earPath, catPaint)

                            // Coffee Cup emitting cartoonish warm steam paths
                            val steamPaint = Paint().apply {
                                color = Color.parseColor("#3CD3C5EE")
                                strokeWidth = 3f
                                style = Paint.Style.STROKE
                                isAntiAlias = true
                            }
                            val cupX = width * 0.58f
                            val cupY = height * 0.78f
                            // Cup silhouette
                            canvas.drawRect(cupX - 14f, cupY - 14f, cupX + 14f, cupY + 6f, catPaint)
                            canvas.drawArc(cupX + 10f, cupY - 8f, cupX + 20f, cupY + 2f, -90f, 180f, false, catPaint) // Handle

                            // Loop warm curly steam trails
                            for (si in 0..2) {
                                val steamPath = android.graphics.Path()
                                val steamPhase = floatFrame * 0.12f + si * 2.1f
                                val sStartX = cupX - 6f + si * 6f
                                steamPath.moveTo(sStartX, cupY - 16f)
                                steamPath.cubicTo(
                                    sStartX + sin(steamPhase) * 6f, cupY - 30f,
                                    sStartX - sin(steamPhase) * 6f, cupY - 45f,
                                    sStartX + sin(steamPhase) * 10f, cupY - 60f
                                )
                                canvas.drawPath(steamPath, steamPaint)
                            }
                        }

                        "metal", "rock" -> {
                            // --- DARK VOLCANIC CHASM & GUITAR SOLOS SILHOUETTE ---
                            // Lava core background bottom
                            val corePaint = Paint().apply {
                                color = Color.parseColor("#FF3E00")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            canvas.drawRect(0f, height * 0.78f, width.toFloat(), height.toFloat(), corePaint)

                            // Rising flame embers sparks
                            val flameSpark = Paint().apply {
                                color = Color.parseColor("#FFA500")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            for (ei in 0..22) {
                                val ePhase = floatFrame * 0.12f + ei * 1.5f
                                val ey = height.toFloat() - ((floatFrame * 6.0f + ei * 35f) % 280f)
                                val ex = (ei * 20f + 110f * sin(ePhase)) % width.toFloat()
                                val esize = 2.0f + 4.0f * abs(sin(ePhase))
                                canvas.drawCircle(ex, ey, esize, flameSpark)
                            }

                            // Sharp craggy lava boulders/ridges
                            val boulderPaint = Paint().apply {
                                color = Color.parseColor("#150E0C")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val bPath = android.graphics.Path()
                            bPath.moveTo(0f, height * 0.82f)
                            bPath.lineTo(width * 0.22f, height * 0.58f)
                            bPath.lineTo(width * 0.42f, height * 0.72f)
                            bPath.lineTo(width * 0.65f, height * 0.55f)
                            bPath.lineTo(width * 0.80f, height * 0.68f)
                            bPath.lineTo(width.toFloat(), height * 0.78f)
                            bPath.lineTo(width.toFloat(), height.toFloat())
                            bPath.lineTo(0f, height.toFloat())
                            bPath.close()
                            canvas.drawPath(bPath, boulderPaint)

                            // Giant branching lightning bolt with screen flashes
                            if (frame % 25 < 5) {
                                val lPaint = Paint().apply {
                                    color = Color.parseColor("#EBF3FF")
                                    strokeWidth = 4.5f + (frame % 5) * 2f
                                    style = Paint.Style.STROKE
                                    isAntiAlias = true
                                }
                                val lPath = android.graphics.Path()
                                var boltY = 0f
                                var boltX = width * 0.52f
                                lPath.moveTo(boltX, boltY)
                                while (boltY < height * 0.55f) {
                                    val nextY = boltY + 35f
                                    val nextX = boltX + (sin(floatFrame + boltY) * 35f)
                                    lPath.lineTo(nextX, nextY)
                                    boltX = nextX
                                    boltY = nextY
                                }
                                canvas.drawPath(lPath, lPaint)

                                // Volcanic light overlay
                                val lightSky = Paint().apply {
                                    color = Color.parseColor("#3FA3B7FF")
                                    style = Paint.Style.FILL
                                }
                                canvas.drawPaint(lightSky)
                            }

                            // Detailed Rocker Guitarist silhouette headbanging on mountain top!
                            val rockerPaint = Paint().apply {
                                color = Color.parseColor("#060201")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val headX = width * 0.48f
                            val headY = height * 0.53f + sin(floatFrame * 0.9f) * 8f // High speed headbanging!
                            
                            // Head & flying long hair
                            canvas.drawCircle(headX, headY, 14f, rockerPaint)
                            // Spiky flying long hair paths
                            val hairPath = android.graphics.Path()
                            hairPath.moveTo(headX - 10f, headY - 8f)
                            hairPath.lineTo(headX - 35f - abs(sin(floatFrame * 0.8f)) * 15f, headY + 14f)
                            hairPath.lineTo(headX - 6f, headY + 12f)
                            hairPath.close()
                            canvas.drawPath(hairPath, rockerPaint)

                            // Torso / shoulders
                            canvas.drawRect(headX - 22f, headY + 16f, headX + 22f, headY + 65f, rockerPaint)

                            // Electric Guitar flying-V silhouette held diagonally
                            val guitarPaint = Paint().apply {
                                color = Color.parseColor("#260805")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val gPath = android.graphics.Path()
                            val gX = headX - 35f
                            val gY = headY + 38f
                            gPath.moveTo(gX, gY)
                            gPath.lineTo(gX + 66f, gY - 45f) // neck
                            gPath.lineTo(gX + 70f, gY - 42f)
                            gPath.lineTo(gX + 12f, gY + 18f)
                            gPath.lineTo(gX - 12f, gY + 45f) // V horn 1
                            gPath.lineTo(gX - 4f, gY + 10f)
                            gPath.lineTo(gX - 30f, gY + 8f)  // V horn 2
                            gPath.close()
                            canvas.drawPath(gPath, guitarPaint)
                        }

                        "edm", "pop", "hiphop" -> {
                            // --- STAGE Laser Lights Arena & Concert Crowd ---
                            // Glowing concert spotlight beams
                            val beamPaint = Paint().apply {
                                color = Color.parseColor("#3C00FFFF")
                                strokeWidth = 5f
                                isAntiAlias = true
                            }
                            val beamColors = listOf(
                                Color.parseColor("#2EFF00D6"),
                                Color.parseColor("#2E00FFC2"),
                                Color.parseColor("#2E0099FF"),
                                Color.parseColor("#2EFFFF00")
                            )
                            for (bi in 0..4) {
                                val bAngle = sin(floatFrame * 0.08f + bi * 1.5f) * 120f
                                beamPaint.color = beamColors[bi % beamColors.size]
                                canvas.save()
                                canvas.translate(width / 2f + (bi - 2) * 45f, height * 0.15f)
                                canvas.rotate(bAngle)
                                canvas.drawRect(-12f, 0f, 12f, height.toFloat() * 1.2f, beamPaint)
                                canvas.restore()
                            }

                            // DJ Booth center silhouette
                            val djPaint = Paint().apply {
                                color = Color.parseColor("#04020A")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            // DJ stands at the table
                            val tblX = width * 0.32f
                            val tblY = height * 0.58f
                            canvas.drawRect(tblX, tblY, tblX + 130f, tblY + 45f, djPaint) // table

                            // DJ raised hands silhouette bouncing to beat
                            val djHeadY = tblY - 32f + sin(floatFrame * 0.45f) * 4.5f // dancing!
                            val djHeadX = width / 2.0f
                            canvas.drawCircle(djHeadX, djHeadY, 12f, djPaint) // Head
                            canvas.drawRect(djHeadX - 14f, djHeadY + 12f, djHeadX + 14f, tblY, djPaint) // Body

                            // Raised hands paths
                            val handPath = android.graphics.Path()
                            handPath.moveTo(djHeadX - 14f, djHeadY + 15f)
                            handPath.lineTo(djHeadX - 32f, djHeadY - 14f + sin(floatFrame * 0.5f) * 8f) // Left arm raised
                            handPath.lineTo(djHeadX - 26f, djHeadY + 22f)
                            handPath.moveTo(djHeadX + 14f, djHeadY + 15f)
                            handPath.lineTo(djHeadX + 32f, djHeadY - 12f + cos(floatFrame * 0.5f) * 8f) // Right arm raised
                            handPath.lineTo(djHeadX + 26f, djHeadY + 22f)
                            canvas.drawPath(handPath, djPaint)

                            // Foreground dancing concert-crowd silhouette waving hands
                            for (ci in 0..11) {
                                val cx = (ci * 35f) % width.toFloat()
                                val cy = height.toFloat() - 40f + sin(floatFrame * 0.35f + ci) * 4f
                                val headRadius = 11f
                                canvas.drawCircle(cx, cy - 25f, headRadius, djPaint)
                                canvas.drawRect(cx - 15f, cy - 14f, cx + 15f, height.toFloat(), djPaint)

                                // Wave hand lines
                                val curArmY = cy - 25f - 14f + cos(floatFrame * 0.28f + ci) * 16f
                                canvas.drawLine(cx, cy - 14f, cx - 12f, curArmY, djPaint)
                                canvas.drawLine(cx, cy - 14f, cx + 12f, curArmY, djPaint)
                            }
                        }

                        "country" -> {
                            // --- OUTDOOR COUNTRY TEXAS DESERT SUNSET & CAMPFIRE CAMPING ---
                            // Background big setting sun
                            val countrySun = Paint().apply {
                                color = Color.parseColor("#FFA100")
                                isAntiAlias = true
                            }
                            canvas.drawCircle(width * 0.72f, height * 0.46f, 65f, countrySun)

                            // Deep brown layers of rolling scenic mountains
                            val ridge1 = Paint().apply {
                                color = Color.parseColor("#35220F")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val r1Path = android.graphics.Path()
                            r1Path.moveTo(0f, height * 0.65f)
                            r1Path.lineTo(width * 0.45f, height * 0.54f)
                            r1Path.lineTo(width.toFloat(), height * 0.62f)
                            r1Path.lineTo(width.toFloat(), height.toFloat())
                            r1Path.lineTo(0f, height.toFloat())
                            r1Path.close()
                            canvas.drawPath(r1Path, ridge1)

                            val ridge2 = Paint().apply {
                                color = Color.parseColor("#22140A")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val r2Path = android.graphics.Path()
                            r2Path.moveTo(0f, height * 0.74f)
                            r2Path.lineTo(width * 0.30f, height * 0.66f)
                            r2Path.lineTo(width.toFloat(), height * 0.71f)
                            r2Path.lineTo(width.toFloat(), height.toFloat())
                            r2Path.lineTo(0f, height.toFloat())
                            r2Path.close()
                            canvas.drawPath(r2Path, ridge2)

                            // Forest pine tree outlines
                            val pinePaint = Paint().apply {
                                color = Color.parseColor("#150A03")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            for (ti in 0..4) {
                                val tx = ti * 85f
                                val ty = height * 0.68f + ti * 10f
                                val pTreePath = android.graphics.Path()
                                pTreePath.moveTo(tx, ty - 60f)
                                pTreePath.lineTo(tx - 20f, ty - 10f)
                                pTreePath.lineTo(tx - 10f, ty - 12f)
                                pTreePath.lineTo(tx - 25f, ty + 15f)
                                pTreePath.lineTo(tx + 25f, ty + 15f)
                                pTreePath.lineTo(tx + 10f, ty - 12f)
                                pTreePath.lineTo(tx + 20f, ty - 10f)
                                pTreePath.close()
                                canvas.drawPath(pTreePath, pinePaint)
                            }

                            // Interactive glowing campfire with sparks flickering
                            val firePaint = Paint().apply {
                                color = Color.parseColor("#FF5500")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val fireX = width * 0.62f
                            val fireY = height * 0.88f
                            // Bouncing red/yellow flames
                            val fireScale = 0.8f + abs(sin(floatFrame * 0.3f)) * 0.4f
                            canvas.drawCircle(fireX, fireY, 18f * fireScale, firePaint)
                            firePaint.color = Color.parseColor("#FFAA00")
                            canvas.drawCircle(fireX, fireY + 4f, 11f * fireScale, firePaint)

                            // Rising fire smoke
                            val smokeCountry = Paint().apply {
                                color = Color.parseColor("#227D746D")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            for (si in 0..4) {
                                val sPhase = floatFrame * 0.08f + si * 1.5f
                                val sy = (fireY - 10f) - ((floatFrame * 1.8f + si * 25f) % 110f)
                                val sx = fireX + sin(sPhase) * 11f
                                canvas.drawCircle(sx, sy, 5f + si * 2.5f, smokeCountry)
                            }

                            // Acoustic Guitarist silhouette sitting on a log next to campfire
                            val countryGuy = Paint().apply {
                                color = Color.parseColor("#0C0603")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val guyX = width * 0.35f
                            val guyY = height * 0.86f
                            // Log
                            canvas.drawRect(guyX - 35f, guyY, guyX + 35f, guyY + 12f, countryGuy)
                            // Cowboy hat and body
                            canvas.drawCircle(guyX, guyY - 32f, 10f, countryGuy) // Head
                            canvas.drawRect(guyX - 12f, guyY - 22f, guyX + 12f, guyY, countryGuy) // Torso
                            // Cowboy hat brim brim oval
                            canvas.drawOval(guyX - 18f, guyY - 36f, guyX + 18f, guyY - 31f, countryGuy)
                            canvas.drawCircle(guyX, guyY - 37f, 6f, countryGuy) // crown

                            // Diagonal shape of dreadnought acoustic guitar
                            val guitarWood = Paint().apply {
                                color = Color.parseColor("#1C0F06")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            canvas.drawOval(guyX - 15f, guyY - 14f, guyX + 18f, guyY - 2f, guitarWood)
                        }

                        else -> {
                            // --- DEEP COSMIC GALACTIC SPACE & FLOATING ASTRONAUT ---
                            // Glowing nebula gas clouds using RadialGradients
                            val purpleC = Paint().apply {
                                isAntiAlias = true
                                shader = RadialGradient(
                                    width * 0.35f + sin(floatFrame * 0.03f) * 45f,
                                    height * 0.38f + cos(floatFrame * 0.02f) * 55f,
                                    170f,
                                    Color.parseColor("#5A651EB2"), Color.TRANSPARENT, Shader.TileMode.CLAMP
                                )
                            }
                            canvas.drawCircle(width / 2f, height / 2f, 210f, purpleC)

                            val tealC = Paint().apply {
                                isAntiAlias = true
                                shader = RadialGradient(
                                    width * 0.65f + cos(floatFrame * 0.04f) * 60f,
                                    height * 0.48f + sin(floatFrame * 0.03f) * 50f,
                                    160f,
                                    Color.parseColor("#4B00DFA0"), Color.TRANSPARENT, Shader.TileMode.CLAMP
                                )
                            }
                            canvas.drawCircle(width / 2f, height / 2f, 190f, tealC)

                            // Cosmic background stars shimmering
                            val spaceStar = Paint().apply {
                                color = Color.WHITE
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            for (si in 0..18) {
                                val sPhase = floatFrame * 0.06f + si * 1.6f
                                val sx = (si * 33) % width.toFloat()
                                val sy = (si * 29) % height.toFloat()
                                spaceStar.alpha = (90 + 165 * abs(sin(sPhase))).toInt().coerceIn(0, 255)
                                val sRadius = 0.8f + 1.5f * abs(sin(sPhase))
                                canvas.drawCircle(sx, sy, sRadius, spaceStar)
                            }

                            // Giant Planet with rings
                            val planetPaint = Paint().apply {
                                isAntiAlias = true
                                shader = LinearGradient(
                                    width * 0.22f, height * 0.22f, width * 0.32f, height * 0.32f,
                                    Color.parseColor("#FF5E7E"), Color.parseColor("#3B1D5F"), Shader.TileMode.CLAMP
                                )
                            }
                            val pCenterX = width * 0.22f
                            val pCenterY = height * 0.25f
                            canvas.drawCircle(pCenterX, pCenterY, 32f, planetPaint)

                            // Planetary ring oval (outline-only angled)
                            val ringPaint = Paint().apply {
                                color = Color.parseColor("#80DCD6FF")
                                style = Paint.Style.STROKE
                                strokeWidth = 3f
                                isAntiAlias = true
                            }
                            canvas.save()
                            canvas.translate(pCenterX, pCenterY)
                            canvas.rotate(-22f)
                            canvas.drawOval(-55f, -8f, 55f, 8f, ringPaint)
                            canvas.restore()

                            // Floating astronaut silhouette drifting slowly
                            val astroPaint = Paint().apply {
                                color = Color.parseColor("#06030C")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val astroX = width * 0.55f + sin(floatFrame * 0.04f) * 18f
                            val astroY = height * 0.48f + cos(floatFrame * 0.03f) * 22f
                            canvas.save()
                            canvas.translate(astroX, astroY)
                            canvas.rotate(sin(floatFrame * 0.02f) * 14f) // floating tilt!

                            // Helmet visor
                            canvas.drawCircle(0f, -22f, 15f, astroPaint) // helmet
                            val visorPaint = Paint().apply {
                                color = Color.parseColor("#00FFC2")
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            canvas.drawOval(-8f, -26f, 8f, -18f, visorPaint)

                            // Backpack and body
                            canvas.drawRect(-18f, -14f, -10f, 18f, astroPaint) // backpack
                            canvas.drawRoundRect(-12f, -10f, 12f, 20f, 6f, 6f, astroPaint) // body

                            // Floating curved hose paths
                            val wirePaint = Paint().apply {
                                color = Color.parseColor("#3C4E3D59")
                                strokeWidth = 2.5f
                                style = Paint.Style.STROKE
                                isAntiAlias = true
                            }
                            val wirePath = android.graphics.Path()
                            wirePath.moveTo(-8f, 18f)
                            wirePath.cubicTo(-25f, 35f, -44f, 22f, -65f, height * 0.15f)
                            canvas.drawPath(wirePath, wirePaint)

                            canvas.restore()
                        }
                    }

                    // RESTORE CAMERA transform to prevent modifying the overlays
                    canvas.restore()

                    // E. Modern Cinematic Letterbox black bars at the top & bottom
                    val letterboxPaint = Paint().apply {
                        color = Color.parseColor("#CC030106") // Semi translucent cinematic overlay bar
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(0f, 0f, width.toFloat(), 95f, letterboxPaint)
                    canvas.drawRect(0f, height.toFloat() - 105f, width.toFloat(), height.toFloat(), letterboxPaint)

                    // Draw thin golden glowing frame boundary
                    val goldBorder = Paint().apply {
                        color = Color.parseColor("#33FFC107")
                        strokeWidth = 2f
                        style = Paint.Style.STROKE
                    }
                    canvas.drawRect(4f, 4f, width.toFloat() - 4f, height.toFloat() - 4f, goldBorder)

                    // Render gorgeous movie header title
                    canvas.drawText("⚡ SPARK LABS V28 ⚡", width / 2.0f, 54f, titlePaint)

                    // Render camera REC recording symbol blinking to add movie realism
                    if (frame % 20 < 10) {
                        val recIndicator = Paint().apply {
                            color = Color.RED
                            style = Paint.Style.FILL
                            isAntiAlias = true
                        }
                        canvas.drawCircle(35f, 50f, 6f, recIndicator)
                        val recText = Paint().apply {
                            color = Color.WHITE
                            textSize = 12f
                            isAntiAlias = true
                        }
                        canvas.drawText("REC", 62f, 54f, recText)
                    }

                    // Render scrolling subtitles / lyrics nicely
                    if (lyricLines.isNotEmpty()) {
                        val progressPct = floatFrame / maxFrames
                        val activeIndex = (progressPct * lyricLines.size).toInt().coerceIn(0, lyricLines.lastIndex)

                        // Draw semi-translucent back plate for subtitle readability
                        val subBG = Paint().apply {
                            color = Color.parseColor("#77000000")
                            style = Paint.Style.FILL
                        }
                        canvas.drawRoundRect(25f, height * 0.73f, width.toFloat() - 25f, height * 0.89f, 10f, 10f, subBG)

                        // Render active lyric in center glowing cyan
                        textPaint.apply {
                            color = Color.parseColor("#00FFC2")
                            textSize = 21f
                            isFakeBoldText = true
                        }
                        canvas.drawText(lyricLines[activeIndex], width / 2.0f, height * 0.79f, textPaint)

                        // Render upcoming lyrics in smaller translucent grey below
                        textPaint.apply {
                            color = Color.parseColor("#8E8CA4")
                            textSize = 14f
                            isFakeBoldText = false
                        }
                        if (activeIndex + 1 < lyricLines.size) {
                            canvas.drawText(lyricLines[activeIndex + 1], width / 2.0f, height * 0.85f, textPaint)
                        }
                    } else {
                        canvas.drawText("Píseň se generuje v cloudu...", width / 2.0f, height * 0.80f, textPaint)
                    }

                    // Render progress timeline text at the bottom letterbox
                    val elapsedSec = (frame / frameRate)
                    val progressStr = String.format("%02d:%02d / %02d:%02d", elapsedSec / 60, elapsedSec % 60, durationSec / 60, durationSec % 60)
                    textPaint.apply {
                        color = Color.parseColor("#908DA1")
                        textSize = 13f
                        isFakeBoldText = false
                    }
                    canvas.drawText(progressStr, width / 2.0f, height.toFloat() - 45f, textPaint)

                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }
            }

            // 2. Read output from MediaCodec, write into MediaMuxer
            var outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10000)
            while (outputBufferId >= 0) {
                val encodedData = encoder.getOutputBuffer(outputBufferId)
                if (encodedData != null) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0
                    }

                    if (bufferInfo.size != 0) {
                        if (!muxerStarted) {
                            trackIndex = muxer.addTrack(encoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        bufferInfo.presentationTimeUs = ptsUs
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                }
                encoder.releaseOutputBuffer(outputBufferId, false)
                outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 0)
            }

            frame++
            if (frame % 15 == 0) {
                onProgress(frame.toFloat() / maxFrames)
            }
        }

        encoder.stop()
        encoder.release()
        inputSurface.release()

        if (muxerStarted) {
            muxer.stop()
        }
        muxer.release()

        Log.d(TAG, "Video successfully generated: ${videoFile.absolutePath}")
        videoFile
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            value.toByte(),
            (value ushr 8).toByte(),
            (value ushr 16).toByte(),
            (value ushr 24).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            value.toByte(),
            (value.toInt() ushr 8).toByte()
        )
    }

    private fun convertPcm(
        data: ByteArray,
        sourceSampleRate: Int,
        sourceChannels: Int,
        targetSampleRate: Int
    ): ByteArray {
        val bytesPerSample = 2
        val sourceFrameSize = sourceChannels * bytesPerSample
        val totalFrames = data.size / sourceFrameSize
        if (totalFrames <= 0) return ByteArray(0)

        val samplesMono = ShortArray(totalFrames)
        val buffer = ByteBuffer.wrap(data).order(java.nio.ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until totalFrames) {
            var sum = 0
            for (c in 0 until sourceChannels) {
                if (buffer.remaining() >= 2) {
                    sum += buffer.short
                }
            }
            samplesMono[i] = (sum / sourceChannels).toShort()
        }

        if (sourceSampleRate == targetSampleRate) {
            val outBytes = ByteArray(samplesMono.size * 2)
            val outBuffer = ByteBuffer.wrap(outBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            for (s in samplesMono) {
                outBuffer.putShort(s)
            }
            return outBytes
        }

        val ratio = sourceSampleRate.toDouble() / targetSampleRate
        val targetFrames = (totalFrames / ratio).toInt()
        if (targetFrames <= 0) return ByteArray(0)

        val resampled = ShortArray(targetFrames)
        for (i in 0 until targetFrames) {
            val srcIndexDouble = i * ratio
            val srcIndex = srcIndexDouble.toInt()
            val nextIndex = if (srcIndex + 1 < totalFrames) srcIndex + 1 else srcIndex
            val fract = srcIndexDouble - srcIndex

            val s1 = samplesMono[srcIndex].toInt()
            val s2 = samplesMono[nextIndex].toInt()
            val interpolated = s1 + (fract * (s2 - s1))
            resampled[i] = interpolated.toInt().coerceIn(-32768, 32767).toShort()
        }

        val outBytes = ByteArray(resampled.size * 2)
        val outBuffer = ByteBuffer.wrap(outBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        for (s in resampled) {
            outBuffer.putShort(s)
        }
        return outBytes
    }

    suspend fun decodeToPcm(inputFile: File, targetSampleRate: Int = 22050): ByteArray = withContext(Dispatchers.IO) {
        if (!inputFile.exists() || inputFile.length() == 0L) return@withContext ByteArray(0)

        val extractor = android.media.MediaExtractor()
        try {
            extractor.setDataSource(inputFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Extractor error setting datasource: ${e.message}")
            return@withContext ByteArray(0)
        }

        var trackIndex = -1
        var format: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val f = extractor.getTrackFormat(i)
            val mime = f.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio/")) {
                trackIndex = i
                format = f
                break
            }
        }

        if (trackIndex < 0 || format == null) {
            extractor.release()
            return@withContext ByteArray(0)
        }

        extractor.selectTrack(trackIndex)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
        val bos = java.io.ByteArrayOutputStream()

        try {
            val decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(format, null, null, 0)
            decoder.start()

            val bufferInfo = MediaCodec.BufferInfo()
            var isInputEOS = false
            var isOutputEOS = false

            var currentChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
            var currentSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE, 22050)

            while (!isOutputEOS) {
                if (!isInputEOS) {
                    val inputBufferId = decoder.dequeueInputBuffer(10000)
                    if (inputBufferId >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferId)!!
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isInputEOS = true
                        } else {
                            decoder.queueInputBuffer(inputBufferId, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outputBufferId = decoder.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferId >= 0) {
                    val outputBuffer = decoder.getOutputBuffer(outputBufferId)!!
                    val chunk = ByteArray(bufferInfo.size)
                    outputBuffer.get(chunk)
                    outputBuffer.clear()

                    if (chunk.isNotEmpty()) {
                        val converted = convertPcm(chunk, currentSampleRate, currentChannels, targetSampleRate)
                        bos.write(converted)
                    }

                    decoder.releaseOutputBuffer(outputBufferId, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isOutputEOS = true
                    }
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = decoder.outputFormat
                    currentChannels = newFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT, currentChannels)
                    currentSampleRate = newFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE, currentSampleRate)
                }
            }

            decoder.stop()
            decoder.release()
        } catch (e: Exception) {
            Log.e(TAG, "Codec decoding error: ${e.message}")
        } finally {
            extractor.release()
        }

        bos.toByteArray()
    }

    suspend fun mixActiveTracksToWav(
        context: Context,
        tracks: List<com.example.data.database.AudioTrack>,
        outputFile: File,
        onProgress: (Float) -> Unit
    ): Unit = withContext(Dispatchers.IO) {
        val sampleRate = 22050
        val bitsPerSample = 16
        val numChannels = 1

        val hasSolo = tracks.any { it.isSolo }
        val eligibleTracks = tracks.filter { track ->
            val hasFile = !track.filePath.isNullOrEmpty() && File(track.filePath).exists()
            val allowedBySoloMute = if (hasSolo) track.isSolo else !track.isMuted
            hasFile && allowedBySoloMute
        }

        if (eligibleTracks.isEmpty()) {
            throw IllegalArgumentException("Nebyly nalezeny žádné nahrané ani vygenerované zvukové stopy k exportu!")
        }

        onProgress(0.1f)

        val trackPcms = mutableListOf<ShortArray>()
        val volumes = mutableListOf<Float>()

        eligibleTracks.forEachIndexed { idx, track ->
            val file = File(track.filePath!!)
            val pcmBytes = decodeToPcm(file, sampleRate)
            if (pcmBytes.isNotEmpty()) {
                val shortCount = pcmBytes.size / 2
                val shortArr = ShortArray(shortCount)
                ByteBuffer.wrap(pcmBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArr)
                val processedArr = AudioEffectsProcessor.applyEffects(track, shortArr)
                trackPcms.add(processedArr)
                volumes.add(track.volume)
            }
            val progressVal = 0.1f + (0.4f * (idx + 1) / eligibleTracks.size)
            onProgress(progressVal)
        }

        if (trackPcms.isEmpty()) {
            throw IllegalStateException("Nepodařilo se úspěšně dekódovat žádnou zvukovou srpávnou stopu.")
        }

        val maxLength = trackPcms.maxOf { it.size }
        val mixedSamples = IntArray(maxLength)

        trackPcms.forEachIndexed { trackIdx, pcm ->
            val vol = volumes[trackIdx]
            for (i in pcm.indices) {
                mixedSamples[i] += (pcm[i] * vol).toInt()
            }
        }

        onProgress(0.7f)

        val finalSamples = ShortArray(maxLength)
        for (i in 0 until maxLength) {
            finalSamples[i] = mixedSamples[i].coerceIn(-32768, 32767).toShort()
        }

        onProgress(0.85f)

        val dataSize = maxLength * 2
        val totalFileSize = 36 + dataSize
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)

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

            val outBytes = ByteArray(finalSamples.size * 2)
            ByteBuffer.wrap(outBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(finalSamples)
            out.write(outBytes)
        }

        onProgress(1.0f)
    }

    fun saveWavToDownloads(context: Context, srcFile: File, title: String): File? {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "$title.wav")
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                    put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        srcFile.inputStream().use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    return srcFile
                }
            } else {
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val destFile = File(downloadsDir, "$title.wav")
                srcFile.copyTo(destFile, overwrite = true)
                return destFile
            }
        } catch (e: Exception) {
            Log.e("saveWavToDownloads", "Chyba při ukládání: ${e.message}")
        }
        return null
    }

    fun drawTimelineScene(
        canvas: Canvas,
        mood: String,
        frame: Float,
        width: Int,
        height: Int,
        text: String,
        speed: Float
    ) {
        val bgPaint = Paint().apply { isAntiAlias = true }
        
        when (mood) {
            "Neon Cyberpunk" -> {
                val colorStart = Color.parseColor("#060318")
                val colorEnd = Color.parseColor("#020104")
                bgPaint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colorStart, colorEnd, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
                bgPaint.shader = null

                val horizonY = height * 0.52f
                val sunX = width / 2f
                val sunY = height * 0.38f
                val sunRadius = 75f
                val sunPaint = Paint().apply {
                    isAntiAlias = true
                    shader = LinearGradient(
                        sunX, sunY - sunRadius, sunX, sunY + sunRadius,
                        Color.parseColor("#FF007F"), Color.parseColor("#FFD700"), Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(sunX, sunY, sunRadius, sunPaint)

                val slatPaint = Paint().apply {
                    color = colorEnd
                    style = Paint.Style.FILL
                }
                var slatY = sunY - sunRadius + 15f
                while (slatY < sunY + sunRadius) {
                    val thickness = 3.5f + (slatY - sunY) * 0.05f
                    if (thickness > 0) {
                        canvas.drawRect(sunX - sunRadius - 5f, slatY, sunX + sunRadius + 5f, slatY + thickness, slatPaint)
                    }
                    slatY += 14f
                }

                val gridPaint = Paint().apply {
                    color = Color.parseColor("#FF007F")
                    strokeWidth = 2.0f
                    style = Paint.Style.STROKE
                    isAntiAlias = true
                }
                canvas.drawLine(0f, horizonY, width.toFloat(), horizonY, gridPaint)
                
                val numLines = 10
                for (li in 0..numLines) {
                    val xH = (width / 2f) + (li - numLines / 2f) * 6f
                    val xB = (width / 2f) + (li - numLines / 2f) * 75f
                    canvas.drawLine(xH, horizonY, xB, height.toFloat(), gridPaint)
                }

                val gridSpacing = 30f
                var gridY = horizonY + (frame * 5.0f * speed) % gridSpacing
                while (gridY < height) {
                    val ratio = (gridY - horizonY) / (height - horizonY)
                    val actualY = horizonY + ratio * ratio * (height - horizonY)
                    gridPaint.alpha = (ratio * 255).toInt().coerceIn(0, 255)
                    canvas.drawLine(0f, actualY, width.toFloat(), actualY, gridPaint)
                    gridY += gridSpacing
                }

                val carPaint = Paint().apply {
                    color = Color.parseColor("#090214")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val carPath = android.graphics.Path()
                val carBaseY = height * 0.82f + sin(frame * 0.4f) * 1.5f
                val carStartX = width * 0.28f
                carPath.moveTo(carStartX, carBaseY)
                carPath.lineTo(carStartX + 12f, carBaseY - 14f)
                carPath.lineTo(carStartX + 52f, carBaseY - 15f)
                carPath.lineTo(carStartX + 70f, carBaseY - 32f)
                carPath.lineTo(carStartX + 115f, carBaseY - 30f)
                carPath.lineTo(carStartX + 130f, carBaseY - 12f)
                carPath.lineTo(carStartX + 155f, carBaseY - 10f)
                carPath.lineTo(carStartX + 152f, carBaseY)
                carPath.close()
                canvas.drawPath(carPath, carPaint)

                val wheelPaint = Paint().apply {
                    color = Color.parseColor("#00FFC2")
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    isAntiAlias = true
                }
                canvas.drawCircle(carStartX + 35f, carBaseY, 11f, carPaint)
                canvas.drawCircle(carStartX + 115f, carBaseY, 11f, carPaint)
                canvas.drawCircle(carStartX + 35f, carBaseY, 11f, wheelPaint)
                canvas.drawCircle(carStartX + 115f, carBaseY, 11f, wheelPaint)
            }
            "Cosmic Space" -> {
                val colorStart = Color.parseColor("#0F031F")
                val colorEnd = Color.parseColor("#030107")
                bgPaint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colorStart, colorEnd, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
                bgPaint.shader = null

                val starPaint = Paint().apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                for (si in 0..30) {
                    val sPhase = frame * 0.08f + si * 1.8f
                    val sx = (si * 43) % width.toFloat()
                    val sy = (si * 29) % height.toFloat()
                    val size = 1.0f + 1.5f * abs(sin(sPhase))
                    starPaint.alpha = (50 + 205 * abs(cos(sPhase))).toInt().coerceIn(0, 255)
                    canvas.drawCircle(sx, sy, size, starPaint)
                }

                val planetX = width / 2f
                val planetY = height * 0.4f
                val planetRadius = 55f

                val ringPaint = Paint().apply {
                    color = Color.parseColor("#A88F2D")
                    style = Paint.Style.STROKE
                    strokeWidth = 10f
                    isAntiAlias = true
                }
                canvas.save()
                canvas.translate(planetX, planetY)
                canvas.rotate(-15f)
                canvas.drawOval(-100f, -12f, 100f, 12f, ringPaint)
                canvas.restore()

                val bodyPaint = Paint().apply {
                    shader = RadialGradient(
                        planetX - 15f, planetY - 15f, planetRadius * 1.5f,
                        Color.parseColor("#D4A373"), Color.parseColor("#261C14"), Shader.TileMode.CLAMP
                    )
                    isAntiAlias = true
                }
                canvas.drawCircle(planetX, planetY, planetRadius, bodyPaint)

                val shipPaint = Paint().apply {
                    color = Color.parseColor("#CCCCCC")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val shipX = (frame * 3f * speed) % (width + 120) - 60
                val shipY = height * 0.72f + sin(frame * 0.1f) * 15f
                val shipPath = android.graphics.Path()
                shipPath.moveTo(shipX, shipY)
                shipPath.lineTo(shipX - 35f, shipY - 12f)
                shipPath.lineTo(shipX - 35f, shipY + 12f)
                shipPath.close()
                canvas.drawPath(shipPath, shipPaint)

                val enginePaint = Paint().apply {
                    color = Color.parseColor("#FF5500")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val tailPath = android.graphics.Path()
                tailPath.moveTo(shipX - 35f, shipY - 6f)
                tailPath.lineTo(shipX - 50f - abs(sin(frame * 0.5f)) * 12f, shipY)
                tailPath.lineTo(shipX - 35f, shipY + 6f)
                tailPath.close()
                canvas.drawPath(tailPath, enginePaint)
            }
            "Warm Retro" -> {
                val colorStart = Color.parseColor("#2D1D13")
                val colorEnd = Color.parseColor("#0F0906")
                bgPaint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colorStart, colorEnd, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
                bgPaint.shader = null

                val scanPaint = Paint().apply {
                    color = Color.parseColor("#12B2620A")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(15f, 15f, width - 15f, height - 15f, scanPaint)

                val linePaint = Paint().apply {
                    color = Color.parseColor("#44000000")
                    strokeWidth = 1.5f
                }
                var ly = 15f
                while (ly < height - 15) {
                    canvas.drawLine(15f, ly, width - 15f, ly, linePaint)
                    ly += 6f
                }

                val beamPaint = Paint().apply {
                    color = Color.parseColor("#15FFFFFF")
                    style = Paint.Style.FILL
                }
                val beamY = (frame * 2.5f * speed) % height
                canvas.drawRect(15f, beamY, width - 15f, beamY + 30f, beamPaint)

                val wavePaint = Paint().apply {
                    color = Color.parseColor("#ECA13C")
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    isAntiAlias = true
                }
                val centerX = width / 2f
                val centerY = height / 2f
                for (r in 1..4) {
                    val radius = r * 30f + (frame * 1.5f * speed) % 30f
                    wavePaint.alpha = (255 * (1f - radius / 180f)).toInt().coerceIn(0, 255)
                    if (radius < 180f) {
                        canvas.drawCircle(centerX, centerY, radius, wavePaint)
                    }
                }
            }
            "Hot Lava" -> {
                val colorStart = Color.parseColor("#210400")
                val colorEnd = Color.parseColor("#070100")
                bgPaint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colorStart, colorEnd, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
                bgPaint.shader = null

                val lavaPaint = Paint().apply {
                    color = Color.parseColor("#FF4500")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawRect(0f, height * 0.76f, width.toFloat(), height.toFloat(), lavaPaint)

                val flamePaint = Paint().apply {
                    color = Color.parseColor("#FF2200")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val wavePath = android.graphics.Path()
                val baseWaveY = height * 0.8f
                wavePath.moveTo(0f, height.toFloat())
                wavePath.lineTo(0f, baseWaveY)
                
                val p1x = width * 0.33f
                val p1y = baseWaveY + sin(frame * 0.1f) * 15f
                val p2x = width * 0.66f
                val p2y = baseWaveY - cos(frame * 0.12f) * 12f
                
                wavePath.quadTo(p1x / 2, p1y - 20f, p1x, p1y)
                wavePath.quadTo((p1x + p2x) / 2, p2y + 15f, p2x, p2y)
                wavePath.quadTo((p2x + width) / 2, baseWaveY - 10f, width.toFloat(), baseWaveY)
                wavePath.lineTo(width.toFloat(), height.toFloat())
                wavePath.close()
                canvas.drawPath(wavePath, flamePaint)

                val sparkPaint = Paint().apply {
                    color = Color.parseColor("#FFAA00")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                for (ei in 0..20) {
                    val phase = frame * 0.12f + ei * 1.5f
                    val ey = height.toFloat() - ((frame * 6.0f * speed + ei * 42f) % 320f)
                    val ex = (ei * 24f + 95f * sin(phase)) % width.toFloat()
                    val esize = 1.5f + 3.5f * abs(sin(phase))
                    canvas.drawCircle(ex, ey, esize, sparkPaint)
                }
            }
            "Emerald Forest" -> {
                val colorStart = Color.parseColor("#012111")
                val colorEnd = Color.parseColor("#000B06")
                bgPaint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colorStart, colorEnd, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
                bgPaint.shader = null

                val orbX = width / 2f
                val orbY = height * 0.42f
                val orbPaint = Paint().apply {
                    isAntiAlias = true
                    shader = RadialGradient(
                        orbX, orbY, 110f,
                        Color.parseColor("#A84BFF94"), Color.TRANSPARENT, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(orbX, orbY, 110f, orbPaint)

                val pinePaint = Paint().apply {
                    color = Color.parseColor("#010905")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }

                val xCoords = listOf(width * 0.15f, width * 0.42f, width * 0.65f, width * 0.85f)
                val heights = listOf(140f, 210f, 160f, 185f)
                for (pi in xCoords.indices) {
                    val px = xCoords[pi]
                    val pBaseY = height * 0.88f
                    val pHeight = heights[pi] + sin(frame * 0.05f + pi) * 6f

                    val pPath = android.graphics.Path()
                    pPath.moveTo(px, pBaseY - pHeight)
                    pPath.lineTo(px - 32f, pBaseY - pHeight + 40f)
                    pPath.lineTo(px - 14f, pBaseY - pHeight + 35f)
                    pPath.lineTo(px - 45f, pBaseY - pHeight + 85f)
                    pPath.lineTo(px - 20f, pBaseY - pHeight + 78f)
                    pPath.lineTo(px - 55f, pBaseY)
                    pPath.lineTo(px + 55f, pBaseY)
                    pPath.lineTo(px + 20f, pBaseY - pHeight + 78f)
                    pPath.lineTo(px + 45f, pBaseY - pHeight + 85f)
                    pPath.lineTo(px + 14f, pBaseY - pHeight + 35f)
                    pPath.lineTo(px + 32f, pBaseY - pHeight + 40f)
                    pPath.close()
                    canvas.drawPath(pPath, pinePaint)
                }

                canvas.drawRect(0f, height * 0.86f, width.toFloat(), height.toFloat(), pinePaint)

                val leafPaint = Paint().apply {
                    color = Color.parseColor("#505EFFB7")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                for (li in 0..12) {
                    val phase = frame * 0.09f + li * 2.3f
                    val ly = ((frame * 3.5f * speed + li * 50f) % 360f) + 50f
                    val lx = (li * 35f + ly * 0.3f + sin(phase) * 15f) % width.toFloat()
                    canvas.save()
                    canvas.translate(lx, ly)
                    canvas.rotate(phase * 40f)
                    canvas.drawOval(-12f, -5f, 12f, 5f, leafPaint)
                    canvas.restore()
                }
            }
            "Golden Sunset" -> {
                val colorStart = Color.parseColor("#3B1800")
                val colorEnd = Color.parseColor("#15031C")
                bgPaint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colorStart, colorEnd, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
                bgPaint.shader = null

                val sunPaint = Paint().apply {
                    isAntiAlias = true
                    shader = RadialGradient(
                        width / 2f, height * 0.44f, 140f,
                        Color.parseColor("#FFEE9300"), Color.TRANSPARENT, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(width / 2f, height * 0.44f, 140f, sunPaint)

                val discPaint = Paint().apply {
                    color = Color.parseColor("#FFF4A3")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(width / 2f, height * 0.44f, 40f, discPaint)

                val mPaint = Paint().apply {
                    color = Color.parseColor("#12051D")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val mPath = android.graphics.Path()
                mPath.moveTo(0f, height.toFloat())
                mPath.lineTo(0f, height * 0.65f)
                mPath.lineTo(width * 0.35f, height * 0.52f)
                mPath.lineTo(width * 0.68f, height * 0.61f)
                mPath.lineTo(width.toFloat(), height * 0.48f)
                mPath.lineTo(width.toFloat(), height.toFloat())
                mPath.close()
                canvas.drawPath(mPath, mPaint)

                val birdPaint = Paint().apply {
                    color = Color.parseColor("#0C0314")
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                    isAntiAlias = true
                }
                for (bi in 0..4) {
                    val bx = ((frame * 2.2f * speed + bi * 80f) % (width + 100)) - 50f
                    val by = height * 0.25f - bi * 18f + sin(frame * 0.08f + bi) * 10f
                    val wingOsc = abs(sin(frame * 0.4f + bi)) * 6f
                    val bPath = android.graphics.Path()
                    bPath.moveTo(bx - 12f, by - wingOsc)
                    bPath.lineTo(bx, by)
                    bPath.lineTo(bx + 12f, by - wingOsc)
                    canvas.drawPath(bPath, birdPaint)
                }
            }
        }

        if (text.isNotBlank()) {
            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 21f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                isFakeBoldText = true
                setShadowLayer(5f, 0f, 2f, Color.BLACK)
            }
            canvas.drawText(text, width / 2f, height * 0.88f, textPaint)
        }
    }

    suspend fun generateTimelineVideo(
        context: Context,
        clipsJson: String,
        audioFile: File,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val cacheDir = context.cacheDir
        val videoFile = File(cacheDir, "spark_timeline_${System.currentTimeMillis()}.mp4")

        val width = 360
        val height = 640
        val frameRate = 15
        val bitRate = 800000 

        val clipsList = mutableListOf<com.example.util.TimelineClipData>()
        val transitionsMap = mutableMapOf<String, com.example.util.TransitionData>()

        try {
            val root = org.json.JSONObject(clipsJson)
            val clipsArr = root.optJSONArray("clips")
            if (clipsArr != null) {
                for (i in 0 until clipsArr.length()) {
                    val obj = clipsArr.getJSONObject(i)
                    clipsList.add(
                        com.example.util.TimelineClipData(
                            id = obj.optString("id", i.toString()),
                            title = obj.optString("title", "Scéna"),
                            mood = obj.optString("mood", "Neon Cyberpunk"),
                            durationSec = obj.optInt("durationSec", 4),
                            text = obj.optString("text", "")
                        )
                    )
                }
            }

            val transArr = root.optJSONArray("transitions")
            if (transArr != null) {
                for (i in 0 until transArr.length()) {
                    val obj = transArr.getJSONObject(i)
                    val fromId = obj.optString("fromClipId")
                    transitionsMap[fromId] = com.example.util.TransitionData(
                        fromClipId = fromId,
                        toClipId = obj.optString("toClipId"),
                        transitionType = obj.optString("transitionType", "None"),
                        durationSec = obj.optDouble("durationSec", 1.0).toFloat()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ExportFileHelper", "Chyba parsování časové osy: ${e.message}")
            clipsList.add(com.example.util.TimelineClipData("1", "Úvod", "Neon Cyberpunk", 4, "INTRO SCENE"))
            clipsList.add(com.example.util.TimelineClipData("2", "Sloka", "Cosmic Space", 4, "COSMIC VERSE"))
        }

        if (clipsList.isEmpty()) {
            clipsList.add(com.example.util.TimelineClipData("1", "Jedna", "Neon Cyberpunk", 4, "PRODUKCE SPUŠTĚNA"))
        }

        var totalFrames = 0
        val clipStartFrames = IntArray(clipsList.size)
        val clipEndFrames = IntArray(clipsList.size)
        for (idx in clipsList.indices) {
            clipStartFrames[idx] = totalFrames
            totalFrames += clipsList[idx].durationSec * frameRate
            clipEndFrames[idx] = totalFrames
        }

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3)
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        val muxer = MediaMuxer(videoFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        val bufferInfo = MediaCodec.BufferInfo()
        var frame = 0

        while (frame < totalFrames) {
            val ptsUs = (frame * 1_000_000L / frameRate)

            val canvas: Canvas? = inputSurface.lockCanvas(null)
            if (canvas != null) {
                try {
                    var clipIdx = 0
                    for (i in clipsList.indices) {
                        if (frame >= clipStartFrames[i] && frame < clipEndFrames[i]) {
                            clipIdx = i
                            break
                        }
                    }

                    val currentClip = clipsList[clipIdx]
                    val currentStart = clipStartFrames[clipIdx]
                    val currentEnd = clipEndFrames[clipIdx]

                    val nextIdx = clipIdx + 1
                    var isTransitionActive = false
                    var transType = "None"
                    var transProgress = 0.0f

                    if (nextIdx < clipsList.size) {
                        val trans = transitionsMap[currentClip.id]
                        if (trans != null && trans.transitionType != "None") {
                            val transFrames = (trans.durationSec * frameRate).toInt().coerceIn(1, 150)
                            val transitionStartFrame = currentEnd - transFrames
                            if (frame >= transitionStartFrame) {
                                isTransitionActive = true
                                transType = trans.transitionType
                                transProgress = (frame - transitionStartFrame).toFloat() / transFrames
                            }
                        }
                    }

                    if (!isTransitionActive) {
                        drawTimelineScene(
                            canvas = canvas,
                            mood = currentClip.mood,
                            frame = frame.toFloat(),
                            width = width,
                            height = height,
                            text = currentClip.text,
                            speed = 1.0f
                        )
                    } else {
                        val nextClip = clipsList[nextIdx]
                        
                        when (transType) {
                            "Fade" -> {
                                val alphaA = 1.0f - transProgress
                                val alphaB = transProgress

                                val layerPaintA = Paint().apply { alpha = (alphaA * 255).toInt() }
                                canvas.saveLayer(null, layerPaintA)
                                drawTimelineScene(canvas, currentClip.mood, frame.toFloat(), width, height, currentClip.text, 1.0f)
                                canvas.restore()

                                val layerPaintB = Paint().apply { alpha = (alphaB * 255).toInt() }
                                canvas.saveLayer(null, layerPaintB)
                                drawTimelineScene(canvas, nextClip.mood, frame.toFloat(), width, height, nextClip.text, 1.0f)
                                canvas.restore()
                            }
                            "Wipe" -> {
                                drawTimelineScene(canvas, currentClip.mood, frame.toFloat(), width, height, currentClip.text, 1.0f)

                                canvas.save()
                                canvas.clipRect(0f, 0f, transProgress * width, height.toFloat())
                                drawTimelineScene(canvas, nextClip.mood, frame.toFloat(), width, height, nextClip.text, 1.0f)
                                canvas.restore()
                            }
                            "Flash" -> {
                                val flashAlpha = 1.0f - abs(transProgress - 0.5f) * 2f
                                if (transProgress < 0.5f) {
                                    drawTimelineScene(canvas, currentClip.mood, frame.toFloat(), width, height, currentClip.text, 1.0f)
                                } else {
                                    drawTimelineScene(canvas, nextClip.mood, frame.toFloat(), width, height, nextClip.text, 1.0f)
                                }

                                val flashPaint = Paint().apply {
                                    color = Color.WHITE
                                    style = Paint.Style.FILL
                                    alpha = (flashAlpha.coerceIn(0f, 1f) * 255).toInt()
                                }
                                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), flashPaint)
                            }
                            "Zoom" -> {
                                val scaleA = 1.0f + 0.5f * transProgress
                                val alphaA = 1.0f - transProgress

                                val scaleB = 0.5f + 0.5f * transProgress
                                val alphaB = transProgress

                                val layerPaintA = Paint().apply { alpha = (alphaA * 255).toInt() }
                                canvas.saveLayer(null, layerPaintA)
                                canvas.save()
                                canvas.translate(width / 2f, height / 2f)
                                canvas.scale(scaleA, scaleA)
                                canvas.translate(-width / 2f, -height / 2f)
                                drawTimelineScene(canvas, currentClip.mood, frame.toFloat(), width, height, currentClip.text, 1.0f)
                                canvas.restore()
                                canvas.restore()

                                val layerPaintB = Paint().apply { alpha = (alphaB * 255).toInt() }
                                canvas.saveLayer(null, layerPaintB)
                                canvas.save()
                                canvas.translate(width / 2f, height / 2f)
                                canvas.scale(scaleB, scaleB)
                                canvas.translate(-width / 2f, -height / 2f)
                                drawTimelineScene(canvas, nextClip.mood, frame.toFloat(), width, height, nextClip.text, 1.0f)
                                canvas.restore()
                                canvas.restore()
                            }
                            else -> {
                                if (transProgress < 0.5f) {
                                    drawTimelineScene(canvas, currentClip.mood, frame.toFloat(), width, height, currentClip.text, 1.0f)
                                } else {
                                    drawTimelineScene(canvas, nextClip.mood, frame.toFloat(), width, height, nextClip.text, 1.0f)
                                }
                            }
                        }
                    }
                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }
            }

            var outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 2500L)
            while (outputBufferIndex >= 0) {
                val encodedData = encoder.getOutputBuffer(outputBufferIndex)
                if (encodedData != null) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size != 0) {
                        if (!muxerStarted) {
                            trackIndex = muxer.addTrack(encoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        bufferInfo.presentationTimeUs = ptsUs
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                }
                encoder.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 0L)
            }

            frame++
            if (frame % 15 == 0) {
                onProgress(frame.toFloat() / totalFrames * 0.95f)
            }
        }

        try {
            encoder.stop()
            encoder.release()
            inputSurface.release()
        } catch(e: Exception) {
            Log.e("ExportFileHelper", "Chyba při zastavování encoderu: ${e.message}")
        }

        try {
            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()
        } catch(e: Exception) {
            Log.e("ExportFileHelper", "Chyba při uvolňování muxeru: ${e.message}")
        }

        Log.d("ExportFileHelper", "Timeline video successfully generated: ${videoFile.absolutePath}")
        onProgress(1.0f)
        videoFile
    }
}

data class TimelineClipData(
    val id: String,
    val title: String,
    val mood: String,
    val durationSec: Int,
    val text: String
)

data class TransitionData(
    val fromClipId: String,
    val toClipId: String,
    val transitionType: String,
    val durationSec: Float
)

