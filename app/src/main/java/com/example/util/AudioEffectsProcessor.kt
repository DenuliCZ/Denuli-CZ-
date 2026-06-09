package com.example.util

import com.example.data.database.AudioTrack
import kotlin.math.pow

object AudioEffectsProcessor {

    suspend fun applyStudioVoiceFilter(samples: ShortArray, genre: String): ShortArray {
        // 1. Noise gate / reduction threshold (attenuate clicks/background hum)
        val threshold = 400
        val tempOutput = ShortArray(samples.size)
        
        val windowSize = 512
        val step = 256
        val sampleRate = 22050f
        
        // Frequencies corresponding to genre chords
        val targetFrequencies = when (genre.lowercase()) {
            "hiphop", "hip-hop", "rap" -> doubleArrayOf(110.0, 130.81, 146.83, 164.81, 196.0, 220.0, 261.63, 293.66, 329.63, 392.0, 440.0) // A Minor
            "rock", "metal" -> doubleArrayOf(110.0, 123.47, 146.83, 164.81, 196.0, 220.0, 246.94, 293.66, 329.63, 392.0, 440.0) // E Minor/A Minor
            else -> doubleArrayOf(130.81, 146.83, 164.81, 174.61, 196.0, 220.0, 246.94, 261.63, 293.66, 329.63, 349.23, 392.0, 440.0) // C Major for Pop/others
        }

        System.arraycopy(samples, 0, tempOutput, 0, samples.size)

        var i = 0
        while (i + windowSize <= samples.size) {
            var maxVal = 0
            for (j in 0 until windowSize) {
                val absV = Math.abs(samples[i + j].toInt())
                if (absV > maxVal) maxVal = absV
            }
            
            val gateRatio = if (maxVal < threshold) 0.08f else 1.0f
            
            var bestLag = -1
            var bestR = -1.0
            val minLag = (sampleRate / 450f).toInt()
            val maxLag = (sampleRate / 85f).toInt()
            
            for (lag in minLag..maxLag) {
                var r = 0.0
                for (j in 0 until windowSize - lag) {
                    r += samples[i + j].toDouble() * samples[i + j + lag].toDouble()
                }
                if (bestLag == -1 || r > bestR) {
                    bestR = r
                    bestLag = lag
                }
            }
            
            val detectedFreq = if (bestLag > 0) sampleRate / bestLag else 0f
            
            if (detectedFreq in 85f..450f && maxVal > threshold) {
                var nearestFreq = targetFrequencies[0]
                var minDiff = Math.abs(detectedFreq - nearestFreq)
                for (tf in targetFrequencies) {
                    val diff = Math.abs(detectedFreq - tf)
                    if (diff < minDiff) {
                        minDiff = diff
                        nearestFreq = tf
                    }
                }
                
                val ratio = (nearestFreq / detectedFreq.toDouble()).coerceIn(0.88, 1.12)
                for (j in 0 until windowSize) {
                    val srcIndex = i + (j * ratio).toInt()
                    if (srcIndex in samples.indices) {
                        val valDouble = samples[srcIndex].toDouble()
                        tempOutput[i + j] = (valDouble * gateRatio).coerceIn(-32768.0, 32767.0).toInt().toShort()
                    }
                }
            } else {
                for (j in 0 until windowSize) {
                    val valDouble = samples[i + j].toDouble()
                    tempOutput[i + j] = (valDouble * gateRatio).coerceIn(-32768.0, 32767.0).toInt().toShort()
                }
            }
            
            i += step
        }
        return tempOutput
    }

    suspend fun generateHarmonicCopy(originalFile: java.io.File, outputFile: java.io.File, pitchRatio: Double) {
        val sampleRate = 22050
        val pcmBytes = ExportFileHelper.decodeToPcm(originalFile, sampleRate)
        if (pcmBytes.isEmpty()) return
        
        val shortCount = pcmBytes.size / 2
        val shortArr = ShortArray(shortCount)
        java.nio.ByteBuffer.wrap(pcmBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArr)
        
        val targetSize = (shortArr.size / pitchRatio).toInt()
        val processed = ShortArray(targetSize)
        for (i in 0 until targetSize) {
            val srcIndex = (i * pitchRatio).toInt()
            if (srcIndex in shortArr.indices) {
                processed[i] = shortArr[srcIndex]
            }
        }
        writeSamplesToWavFile(processed, sampleRate, outputFile)
    }

    suspend fun processTrackFileWithEffects(track: AudioTrack, originalFile: java.io.File, outputFile: java.io.File) {
        val sampleRate = 22050
        val pcmBytes = ExportFileHelper.decodeToPcm(originalFile, sampleRate)
        if (pcmBytes.isEmpty()) return
        
        val shortCount = pcmBytes.size / 2
        val shortArr = ShortArray(shortCount)
        java.nio.ByteBuffer.wrap(pcmBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArr)
        
        val processed = applyEffects(track, shortArr)
        writeSamplesToWavFile(processed, sampleRate, outputFile)
    }

    fun writeSamplesToWavFile(samples: ShortArray, sampleRate: Int, outputFile: java.io.File) {
        val numChannels = 1
        val bitsPerSample = 16
        val dataSize = samples.size * 2
        val totalFileSize = 36 + dataSize
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)

        val intToByteArray = { value: Int ->
            val b = ByteArray(4)
            b[0] = (value and 0xff).toByte()
            b[1] = (value shr 8 and 0xff).toByte()
            b[2] = (value shr 16 and 0xff).toByte()
            b[3] = (value shr 24 and 0xff).toByte()
            b
        }
        val shortToByteArray = { value: Short ->
            val b = ByteArray(2)
            b[0] = (value.toInt() and 0xff).toByte()
            b[1] = (value.toInt() shr 8 and 0xff).toByte()
            b
        }

        java.io.BufferedOutputStream(java.io.FileOutputStream(outputFile)).use { out ->
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

            val outBytes = ByteArray(samples.size * 2)
            java.nio.ByteBuffer.wrap(outBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(samples)
            out.write(outBytes)
        }
    }

    fun applyEffects(track: AudioTrack, samples: ShortArray): ShortArray {
        // If no effects are enabled, and EQ is flat, return original
        val hasEq = track.eqLow != 0.0f || track.eqMid != 0.0f || track.eqHigh != 0.0f
        if (!hasEq && !track.compEnabled && !track.reverbEnabled) {
            return samples
        }

        // Initialize processors
        val eq = ThreeBandEQ()
        val compressor = DynamicCompressor()
        val reverb = AudioReverb()

        // Calculate linear gains for EQ (-12 to +12 dB)
        val lowGain = 10.0.pow(track.eqLow.toDouble() / 20.0)
        val midGain = 10.0.pow(track.eqMid.toDouble() / 20.0)
        val highGain = 10.0.pow(track.eqHigh.toDouble() / 20.0)

        val output = ShortArray(samples.size)
        for (i in samples.indices) {
            var s = samples[i].toDouble()

            // 1. EQ
            if (hasEq) {
                s = eq.processSample(s, lowGain, midGain, highGain)
            }

            // 2. Compressor
            if (track.compEnabled) {
                s = compressor.processSample(s, track.compThreshold.toDouble(), track.compRatio.toDouble())
            }

            // 3. Reverb
            if (track.reverbEnabled) {
                s = reverb.processSample(s, track.reverbWet.toDouble(), track.reverbFeedback.toDouble())
            }

            // Clamp limit to prevent clipping
            output[i] = s.coerceIn(-32768.0, 32767.0).toInt().toShort()
        }
        return output
    }

    // Neil C 3-band EQ algorithm
    private class ThreeBandEQ {
        var lf: Double = 0.0
        var f1p0: Double = 0.0
        var f1p1: Double = 0.0
        var f1p2: Double = 0.0
        var f1p3: Double = 0.0

        var hf: Double = 0.0
        var f2p0: Double = 0.0
        var f2p1: Double = 0.0
        var f2p2: Double = 0.0
        var f2p3: Double = 0.0

        init {
            // Cutoff frequencies: ~250Hz low, ~4000Hz high (tuned for 22050Hz sample rate)
            lf = 2.0 * Math.sin(Math.PI * 250.0 / 22050.0)
            hf = 2.0 * Math.sin(Math.PI * 4000.0 / 22050.0)
        }

        fun processSample(sample: Double, lowGain: Double, midGain: Double, highGain: Double): Double {
            // Low band split
            f1p0 += lf * (sample - f1p0)
            f1p1 += lf * (f1p0 - f1p1)
            f1p2 += lf * (f1p1 - f1p2)
            f1p3 += lf * (f1p2 - f1p3)
            val l = f1p3

            // High band split
            f2p0 += hf * (sample - f2p0)
            f2p1 += hf * (f2p0 - f2p1)
            f2p2 += hf * (f2p1 - f2p2)
            f2p3 += hf * (f2p2 - f2p3)
            val h = sample - f2p3

            // Mid band
            val m = sample - (h + l)

            return (l * lowGain) + (m * midGain) + (h * highGain)
        }
    }

    // Feed-forward Peak Envelope Compressor
    private class DynamicCompressor {
        private var envelope: Double = 0.0
        private val attackCoef = Math.exp(-1.0 / (22050.0 * 0.010)) // 10ms attack
        private val releaseCoef = Math.exp(-1.0 / (22050.0 * 0.150)) // 150ms release

        fun processSample(sample: Double, thresholdDb: Double, ratio: Double): Double {
            val absSample = Math.abs(sample)
            if (absSample > envelope) {
                envelope = attackCoef * envelope + (1.0 - attackCoef) * absSample
            } else {
                envelope = releaseCoef * envelope + (1.0 - releaseCoef) * absSample
            }

            val envDb = if (envelope < 1.0) -100.0 else 20.0 * Math.log10(envelope / 32768.0)

            var gainReductionDb = 0.0
            if (envDb > thresholdDb) {
                val excessDb = envDb - thresholdDb
                val targetDb = thresholdDb + (excessDb / ratio)
                gainReductionDb = envDb - targetDb
            }

            val reductionFactor = 10.0.pow(-gainReductionDb / 20.0)
            return sample * reductionFactor
        }
    }

    // Schroeder all-pass comb reverb diffuser
    private class AudioReverb {
        private val delayLength1 = 1103 // 50ms
        private val delayLength2 = 1373 // 62ms
        private val delayLength3 = 1697 // 77ms

        private val delayLine1 = DoubleArray(delayLength1)
        private val delayLine2 = DoubleArray(delayLength2)
        private val delayLine3 = DoubleArray(delayLength3)

        private var ptr1 = 0
        private var ptr2 = 0
        private var ptr3 = 0

        private val allpassLength = 487 // 22ms
        private val allpassLine = DoubleArray(allpassLength)
        private var allpassPtr = 0

        fun processSample(sample: Double, wet: Double, feedback: Double): Double {
            val outComb1 = delayLine1[ptr1]
            delayLine1[ptr1] = sample + outComb1 * feedback
            ptr1 = (ptr1 + 1) % delayLength1

            val outComb2 = delayLine2[ptr2]
            delayLine2[ptr2] = sample + outComb2 * (feedback * 0.95)
            ptr2 = (ptr2 + 1) % delayLength2

            val outComb3 = delayLine3[ptr3]
            delayLine3[ptr3] = sample + outComb3 * (feedback * 0.9)
            ptr3 = (ptr3 + 1) % delayLength3

            val combSum = (outComb1 + outComb2 + outComb3) / 3.0

            val g = 0.5
            val apDelay = allpassLine[allpassPtr]
            val apInput = combSum + apDelay * g
            val apOut = -g * apInput + apDelay
            allpassLine[allpassPtr] = apInput
            allpassPtr = (allpassPtr + 1) % allpassLength

            return sample * (1.0 - wet) + apOut * wet
        }
    }
}
