package com.example.util

import android.graphics.*
import java.io.File
import java.io.FileOutputStream

object CoverArtRenderer {
    // Generate a beautiful Bitmap cover based on user parameters
    fun renderCoverArt(
        title: String,
        subtitle: String,
        artPreset: String,
        prompt: String,
        fontSizeScale: Float, // 18f to 40f
        hueShift: Float, // 0f to 360f
        showGrid: Boolean = true,
        glowIntensity: Float = 20f
    ): Bitmap {
        val width = 512
        val height = 512
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Setup Paints and Gradients based on theme
        val bgPaint = Paint().apply { isAntiAlias = true }
        
        val colors = when (artPreset) {
            "Retro Synthwave" -> intArrayOf(Color.parseColor("#0C021A"), Color.parseColor("#1D043F"), Color.parseColor("#E60067"))
            "Cosmic Odyssey" -> intArrayOf(Color.parseColor("#02010A"), Color.parseColor("#0F0F3D"), Color.parseColor("#124C8F"))
            "Minimalist Vector" -> intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#E1E6E8"), Color.parseColor("#A4C1C4"))
            "Heavy Metal Inferno" -> intArrayOf(Color.parseColor("#000000"), Color.parseColor("#220000"), Color.parseColor("#800505"))
            "Dreamy Ambient" -> intArrayOf(Color.parseColor("#4B79A1"), Color.parseColor("#7EA1A5"), Color.parseColor("#283E51"))
            "Acid Neon" -> intArrayOf(Color.parseColor("#051E0E"), Color.parseColor("#083C15"), Color.parseColor("#00FF87"))
            else -> intArrayOf(Color.parseColor("#0C021A"), Color.parseColor("#1D043F"), Color.parseColor("#2E0854"))
        }

        // Handle hue shift if selected
        if (hueShift > 0 && artPreset != "Minimalist Vector") {
            val hsv = FloatArray(3)
            for (i in colors.indices) {
                Color.colorToHSV(colors[i], hsv)
                hsv[0] = (hsv[0] + hueShift) % 360f
                colors[i] = Color.HSVToColor(hsv)
            }
        }

        val gradient = LinearGradient(0f, 0f, 0f, height.toFloat(), colors, null, Shader.TileMode.CLAMP)
        bgPaint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Draw stylistic elements
        val random = java.util.Random((title.hashCode() + prompt.hashCode()).toLong())
        
        when (artPreset) {
            "Retro Synthwave" -> {
                // Large sun in the background
                val sunPaint = Paint().apply {
                    isAntiAlias = true
                    shader = LinearGradient(
                        256f, 128f, 256f, 320f,
                        intArrayOf(Color.parseColor("#FFE600"), Color.parseColor("#FF0066")),
                        null, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(256f, 240f, 110f, sunPaint)

                // Sun horizontal horizontal cuts (replicates real synthwave aesthetic!)
                val cutPaint = Paint().apply {
                    isAntiAlias = true
                    shader = gradient
                }
                for (y in 230..340 step 14) {
                    val thickness = (y - 230) / 10f + 2f
                    canvas.drawRect(120f, y.toFloat(), 390f, y.toFloat() + thickness, cutPaint)
                }

                if (showGrid) {
                    // Sunset vertical lines merging to center/vanishing point
                    val gridPaint = Paint().apply {
                        isAntiAlias = true
                        color = Color.parseColor("#00FFF0")
                        strokeWidth = 2f
                        alpha = 150
                    }
                    val horizonY = 320f
                    for (x in -200..712 step 40) {
                        canvas.drawLine(x.toFloat(), height.toFloat(), 256f, horizonY, gridPaint)
                    }
                    // Horizontal lines with exponential perspective
                    for (y in 320..512 step 12) {
                        val factor = ((y - 320f) / 192f)
                        val screenY = horizonY + factor * factor * 192f
                        canvas.drawLine(0f, screenY, width.toFloat(), screenY, gridPaint)
                    }
                }
            }
            "Cosmic Odyssey" -> {
                // Nebula clouds / glowing outer space circles
                val cloudPaint = Paint().apply {
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(glowIntensity + 20f, BlurMaskFilter.Blur.NORMAL)
                }
                for (i in 0..4) {
                    cloudPaint.color = if (i % 2 == 0) Color.parseColor("#8E2DE2") else Color.parseColor("#4A00E0")
                    cloudPaint.alpha = 80
                    val cx = random.nextFloat() * width
                    val cy = random.nextFloat() * height
                    val radius = 100f + random.nextFloat() * 120f
                    canvas.drawCircle(cx, cy, radius, cloudPaint)
                }

                // Starry galaxy background dots
                val starPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.WHITE
                }
                for (i in 0..70) {
                    starPaint.alpha = 50 + random.nextInt(205)
                    val starSize = 1f + random.nextFloat() * 2.5f
                    val sx = random.nextFloat() * width
                    val sy = random.nextFloat() * height
                    canvas.drawCircle(sx, sy, starSize, starPaint)
                }
            }
            "Minimalist Vector" -> {
                // Clean geometry circles, squares, line art
                val shapePaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    color = Color.parseColor("#1E113C")
                }
                canvas.drawCircle(256f, 256f, 150f, shapePaint)
                canvas.drawCircle(256f, 256f, 80f, shapePaint)
                
                // Cross axes line
                canvas.drawLine(50f, 256f, 462f, 256f, shapePaint)
                canvas.drawLine(256f, 50f, 256f, 462f, shapePaint)
                
                // Solid highlight square
                val fillPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#E6004C")
                    alpha = 180
                }
                canvas.drawRect(236f, 236f, 276f, 276f, fillPaint)
            }
            "Heavy Metal Inferno" -> {
                // Grunge scratches, brutal geometry, dark lightning arcs
                val metalPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#FF0000")
                    strokeWidth = 3f
                    style = Paint.Style.STROKE
                }
                
                // Central pentagram framework
                val path = Path()
                path.moveTo(256f, 80f)
                path.lineTo(360f, 380f)
                path.lineTo(120f, 190f)
                path.lineTo(392f, 190f)
                path.lineTo(152f, 380f)
                path.close()
                canvas.drawPath(path, metalPaint)

                // Scratch lines
                val scratchPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.WHITE
                    strokeWidth = 1.2f
                    alpha = 80
                }
                for (i in 0..12) {
                    canvas.drawLine(random.nextFloat()*width, 0f, random.nextFloat()*width, height.toFloat(), scratchPaint)
                }
            }
            "Dreamy Ambient" -> {
                // Blur gradient orbs
                val orbPaint = Paint().apply {
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)
                }
                for (i in 0..6) {
                    orbPaint.color = when (i % 3) {
                        0 -> Color.parseColor("#FFDF00")
                        1 -> Color.parseColor("#FF4E50")
                        else -> Color.parseColor("#F9D423")
                    }
                    orbPaint.alpha = 90
                    canvas.drawCircle(
                        100f + random.nextFloat() * 312f,
                        100f + random.nextFloat() * 312f,
                        80f + random.nextFloat() * 90f,
                        orbPaint
                    )
                }
            }
            "Acid Neon" -> {
                // Circular ripples and cross wires
                val neonPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                    color = Color.parseColor("#00FF87")
                    maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
                }
                val rawNeonPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = Color.WHITE
                }
                for (radius in 40..220 step 45) {
                    canvas.drawCircle(256f, 256f, radius.toFloat(), neonPaint)
                    canvas.drawCircle(256f, 256f, radius.toFloat(), rawNeonPaint)
                }
                canvas.drawLine(50f, 50f, 462f, 462f, neonPaint)
                canvas.drawLine(50f, 462f, 462f, 50f, neonPaint)
            }
        }

        // 3. Render Overlays
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = fontSizeScale * 1.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            
            val shadowColor = when (artPreset) {
                "Retro Synthwave" -> Color.parseColor("#FFE600")
                "Cosmic Odyssey" -> Color.parseColor("#4A00E0")
                "Heavy Metal Inferno" -> Color.parseColor("#FF0000")
                "Acid Neon" -> Color.parseColor("#00FF87")
                else -> Color.BLACK
            }
            setShadowLayer(8f, 2f, 2f, shadowColor)
        }

        val subPaint = Paint().apply {
            isAntiAlias = true
            color = if (artPreset == "Minimalist Vector") Color.parseColor("#1E113C") else Color.parseColor("#B0BFCF")
            textAlign = Paint.Align.CENTER
            textSize = 14f * 1.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            letterSpacing = 0.2f
        }

        val cleanTitle = title.take(28).uppercase()
        val cleanSubLabel = subtitle.take(35).uppercase()

        // Classic Frame border
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = if (artPreset == "Minimalist Vector") Color.parseColor("#1E113C") else Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            alpha = if (artPreset == "Minimalist Vector") 150 else 70
        }
        canvas.drawRect(24f, 24f, width - 24f, height - 24f, borderPaint)

        canvas.drawText(cleanTitle, 256f, 410f, textPaint)
        canvas.drawText(cleanSubLabel, 256f, 440f, subPaint)

        return bitmap
    }

    // Save Bitmap helper
    fun saveCoverBitmap(bitmap: Bitmap, targetFile: File): Boolean {
        return try {
            val out = FileOutputStream(targetFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
