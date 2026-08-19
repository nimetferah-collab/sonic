package com.nimet.sonicwallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

private const val FRAME_DELAY_MS = 33L // ~30fps

/**
 * Orijinal, Sega'nın Sonic görsellerinden bağımsız olarak Canvas üzerinde
 * baştan çizilen "hızlı mavi kirpi" temalı canlı duvar kağıdı.
 */
class SonicWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = SonicEngine()

    private inner class SonicEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private val drawRunner = Runnable { drawFrame() }

        private var visible = false
        private var width = 0
        private var height = 0
        private var frame = 0L
        private var homeOffsetX = 0f

        // ---- jump physics (tap to jump) ----
        private var jumpY = 0f
        private var jumpVelocity = 0f
        private var isJumping = false

        // ---- entities ----
        private data class Cloud(var x: Float, var y: Float, val scale: Float, val speed: Float)
        private data class Ring(var x: Float, val y: Float, val phase: Float)

        private val clouds = mutableListOf<Cloud>()
        private val rings = mutableListOf<Ring>()

        private val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        private val hillFarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#66BB6A") }
        private val hillNearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#43A047") }
        private val groundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2E7D32") }
        private val groundStripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1B5E20") }
        private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFC107")
            style = Paint.Style.STROKE
        }
        private val heroBluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E6FD9") }
        private val heroBlueDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#134FA0") }
        private val heroSkinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFC98A") }
        private val heroShoePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E53935") }
        private val heroShoeBucklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFD54F") }
        private val eyeWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        private val eyePupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0D1B2A") }
        private val dustPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
            alpha = 150
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onOffsetsChanged(
            xOffset: Float, yOffset: Float,
            xOffsetStep: Float, yOffsetStep: Float,
            xPixelOffset: Int, yPixelOffset: Int
        ) {
            homeOffsetX = xOffset
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            width = w
            height = h
            setupEntities()
            skyPaint.shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.parseColor("#4FC3F7"), Color.parseColor("#B3E5FC"),
                Shader.TileMode.CLAMP
            )
            sunPaint.shader = RadialGradient(
                width * 0.82f, height * 0.16f, width * 0.16f,
                Color.parseColor("#FFF59D"), Color.parseColor("#00FFF59D"),
                Shader.TileMode.CLAMP
            )
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunner)
        }

        override fun onTouchEvent(event: MotionEvent) {
            if (event.action == MotionEvent.ACTION_DOWN && !isJumping) {
                isJumping = true
                jumpVelocity = -22f
            }
        }

        private fun setupEntities() {
            clouds.clear()
            repeat(4) { i ->
                clouds += Cloud(
                    x = Random.nextInt(0, max(width, 1)).toFloat(),
                    y = height * (0.08f + 0.06f * i),
                    scale = 0.7f + Random.nextFloat() * 0.8f,
                    speed = 0.25f + Random.nextFloat() * 0.35f
                )
            }
            rings.clear()
            repeat(5) { i ->
                rings += Ring(
                    x = width * 0.6f + i * width * 0.35f,
                    y = height * 0.68f,
                    phase = i * 0.9f
                )
            }
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    render(canvas)
                }
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: IllegalArgumentException) {
                        // surface was destroyed mid-frame; ignore, next visibility change stops the loop
                    }
                }
            }
            handler.removeCallbacks(drawRunner)
            if (visible) {
                handler.postDelayed(drawRunner, FRAME_DELAY_MS)
            }
        }

        // ---------------------------------------------------------------
        // Rendering
        // ---------------------------------------------------------------

        private fun render(canvas: Canvas) {
            if (width == 0 || height == 0) return
            frame++
            updateJump()

            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)
            canvas.drawCircle(width * 0.82f, height * 0.16f, width * 0.16f, sunPaint)

            drawClouds(canvas)
            drawHillLayer(canvas, hillFarPaint, height * 0.62f, height * 0.09f, speed = 0.4f, waveLen = width * 0.5f)
            drawHillLayer(canvas, hillNearPaint, height * 0.70f, height * 0.12f, speed = 0.9f, waveLen = width * 0.65f)

            val groundTop = height * 0.78f
            canvas.drawRect(0f, groundTop, width.toFloat(), height.toFloat(), groundPaint)
            drawGroundStripes(canvas, groundTop)

            drawRings(canvas)
            drawHero(canvas, groundTop)
        }

        private fun updateJump() {
            if (isJumping) {
                jumpY += jumpVelocity
                jumpVelocity += 1.6f // gravity
                if (jumpY >= 0f) {
                    jumpY = 0f
                    jumpVelocity = 0f
                    isJumping = false
                }
            }
        }

        private fun drawClouds(canvas: Canvas) {
            for (cloud in clouds) {
                cloud.x -= cloud.speed
                if (cloud.x < -width * 0.25f) cloud.x = width + width * 0.1f
                val cx = cloud.x
                val cy = cloud.y
                val s = cloud.scale
                canvas.drawCircle(cx, cy, 26f * s, cloudPaint)
                canvas.drawCircle(cx + 30f * s, cy - 14f * s, 22f * s, cloudPaint)
                canvas.drawCircle(cx + 58f * s, cy, 26f * s, cloudPaint)
                canvas.drawRect(cx - 10f * s, cy, cx + 68f * s, cy + 20f * s, cloudPaint)
            }
        }

        private fun drawHillLayer(
            canvas: Canvas, paint: Paint, baseY: Float, amplitude: Float,
            speed: Float, waveLen: Float
        ) {
            val offset = (frame * speed) % waveLen
            val path = Path()
            path.moveTo(-offset, height.toFloat())
            var x = -offset
            var toggle = false
            path.lineTo(x, baseY)
            while (x < width + waveLen) {
                val nextX = x + waveLen / 2f
                val peakY = if (toggle) baseY - amplitude else baseY + amplitude * 0.3f
                path.quadTo(x + waveLen / 4f, peakY, nextX, baseY)
                x = nextX
                toggle = !toggle
            }
            path.lineTo(x, height.toFloat())
            path.close()
            canvas.drawPath(path, paint)
        }

        private fun drawGroundStripes(canvas: Canvas, groundTop: Float) {
            val stripeW = 46f
            val gap = 70f
            val speed = 6f
            val offset = (frame * speed) % gap
            var x = -offset
            val y1 = groundTop + (height - groundTop) * 0.25f
            val y2 = y1 + 14f
            while (x < width) {
                canvas.drawRect(x, y1, x + stripeW, y2, groundStripePaint)
                x += gap
            }
        }

        private fun drawRings(canvas: Canvas) {
            val speed = 6.5f
            for (ring in rings) {
                ring.x -= speed
                if (ring.x < -60f) ring.x = width + Random.nextInt(80, 260)

                val spin = sin(frame * 0.08f + ring.phase)
                val ringW = 34f * (0.35f + 0.65f * kotlin.math.abs(spin))
                val ringH = 34f
                ringPaint.strokeWidth = 7f
                canvas.drawOval(
                    ring.x - ringW / 2f, ring.y - ringH / 2f,
                    ring.x + ringW / 2f, ring.y + ringH / 2f,
                    ringPaint
                )
            }
        }

        private fun drawHero(canvas: Canvas, groundTop: Float) {
            val cx = width * 0.28f + homeOffsetX * width * 0.08f
            val bob = if (!isJumping) sin(frame * 0.35f) * 6f else 0f
            val cy = groundTop - 46f + jumpY + bob

            val runCycle = frame * 0.45f
            val legSwing = sin(runCycle) * 34f
            val armSwing = sin(runCycle + Math.PI.toFloat()) * 26f

            canvas.save()
            canvas.translate(cx, cy)

            // motion dust behind feet
            if (!isJumping) {
                val dustAlpha = ((sin(runCycle) + 1f) * 60f).toInt()
                dustPaint.alpha = dustAlpha
                canvas.drawLine(-46f, 40f, -76f, 36f, dustPaint)
                canvas.drawLine(-42f, 48f, -68f, 46f, dustPaint)
            }

            // back leg
            drawLeg(canvas, -legSwing)
            // back arm
            drawArm(canvas, -armSwing)

            // tail spikes (back of body)
            val spikes = Path().apply {
                moveTo(-8f, -18f); lineTo(-40f, -30f); lineTo(-10f, -6f); close()
                moveTo(-4f, -30f); lineTo(-30f, -52f); lineTo(2f, -34f); close()
                moveTo(6f, -38f); lineTo(-10f, -66f); lineTo(16f, -46f); close()
            }
            canvas.drawPath(spikes, heroBluePaint)

            // torso
            canvas.drawCircle(0f, -6f, 26f, heroBluePaint)

            // head
            canvas.drawCircle(14f, -38f, 22f, heroBluePaint)
            // muzzle
            canvas.drawOval(10f, -34f, 40f, -16f, heroSkinPaint)
            // eye
            canvas.drawCircle(26f, -44f, 7f, eyeWhitePaint)
            canvas.drawCircle(28f, -43f, 3.4f, eyePupilPaint)

            // front arm
            drawArm(canvas, armSwing)
            // front leg
            drawLeg(canvas, legSwing)

            canvas.restore()
        }

        private fun drawLeg(canvas: Canvas, swingDeg: Float) {
            canvas.save()
            canvas.translate(0f, 16f)
            canvas.rotate(swingDeg)
            canvas.drawRoundRect(-6f, 0f, 6f, 34f, 6f, 6f, heroBlueDarkPaint)
            canvas.drawOval(-11f, 30f, 13f, 46f, heroShoePaint)
            canvas.drawCircle(0f, 38f, 3.5f, heroShoeBucklePaint)
            canvas.restore()
        }

        private fun drawArm(canvas: Canvas, swingDeg: Float) {
            canvas.save()
            canvas.translate(0f, -14f)
            canvas.rotate(swingDeg)
            canvas.drawRoundRect(-5f, 0f, 5f, 26f, 5f, 5f, heroBluePaint)
            canvas.drawCircle(0f, 28f, 6f, heroSkinPaint)
            canvas.restore()
        }
    }
}
