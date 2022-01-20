package com.lr_soft.fidget_ball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.SystemClock
import kotlin.math.roundToInt

class Ball(
    val position: PointF,
    val radius: Float,
    private val color: Int = Color.BLUE
) {
    val velocity = PointF()

    /**
     * If `false`, the ball's position and velocity aren't subject to the physics calculations.
     */
    var applyPhysics = false

    /**
     * The time at which the user released the ball.
     */
    @Volatile
    var creationTime: Long = 0

    /**
     * [position] can contain intermediary results as the physics calculations are running.
     *
     * On the contrary, [positionForDraw] has the position at the last finished physics step,
     * that is safe to draw on screen,
     */
    private val positionForDraw = ConcurrentPointF(position)

    private val paint = Paint().apply {
        color = this@Ball.color
        isAntiAlias = true
    }

    /**
     * Updates the current position of the ball on screen with the value in [position].
     */
    fun updatePositionForDraw() {
        positionForDraw.set(position)
    }

    fun draw(canvas: Canvas) {
        val position = positionForDraw.get()
        updateDrawColor()
        canvas.drawCircle(position.x, position.y, radius, paint)
    }

    fun shouldBeDeleted(): Boolean {
        return getTimeSinceCreation() > TIME_BEFORE_DELETION
    }

    private fun updateDrawColor() {
        val timeSinceCreation = getTimeSinceCreation()
        val fadeoutStartTime = TIME_BEFORE_DELETION - FADEOUT_LENGTH

        val timeSinceFadeoutStart = timeSinceCreation - fadeoutStartTime
        if (timeSinceFadeoutStart <= 0) {
            return
        }
        val fadeoutPercentage = 1 - timeSinceFadeoutStart / FADEOUT_LENGTH
        val alpha = (fadeoutPercentage * 255f).roundToInt().coerceIn(0..255)
        val colorWithoutAlpha = color and ((1 shl 3 * 8) - 1)
        val drawColor = (alpha shl 3 * 8) or colorWithoutAlpha
        paint.color = drawColor
    }

    private fun getTimeSinceCreation(): Float {
        return (SystemClock.uptimeMillis() - creationTime) / 1000f
    }

    private companion object Constants {
        const val TIME_BEFORE_DELETION = 5f
        const val FADEOUT_LENGTH = 2f
    }
}

class ConcurrentPointF(pointF: PointF) {
    private var x: Float = pointF.x
    private var y: Float = pointF.y

    @Synchronized
    fun set(pointF: PointF) {
        x = pointF.x
        y = pointF.y
    }

    @Synchronized
    fun get(): PointF {
        return PointF(x, y)
    }
}