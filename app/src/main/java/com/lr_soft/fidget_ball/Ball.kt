package com.lr_soft.fidget_ball

import android.graphics.*
import android.os.SystemClock
import kotlin.math.roundToInt

class Ball(
    val position: PointF,
    val radius: Float,
    private val color: Int = Color.BLUE,
    private val pathColor: Int = Color.rgb(0, 200, 255)
) {
    val velocity = PointF()

    /**
     * If `false`, the ball's position and velocity aren't subject to the physics calculations.
     */
    var applyPhysics = false
        private set

    /**
     * The time at which the user released the ball.
     */
    @Volatile
    var releaseTime = Long.MAX_VALUE

    /**
     * [position] can contain intermediary results as the physics calculations are running.
     *
     * On the contrary, [positionOnScreen] has the position at the last finished physics step,
     * that is safe to draw on screen,
     */
    private val positionOnScreen = ConcurrentPointF(position)

    private val path = Path().apply {
        moveTo(position.x, position.y)
    }

    private val lastPathPoint = PointF(position.x, position.y)

    private val paint = Paint().apply {
        color = this@Ball.color
        isAntiAlias = true
    }

    private val pathPaint = Paint().apply {
        color = Color.TRANSPARENT
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = radius * PATH_WIDTH_RELATIVE_TO_RADIUS
    }

    /**
     * Updates the current position of the ball on screen with the value in [position].
     */
    fun updatePositionOnScreen() {
        positionOnScreen.set(position)
        if (!applyPhysics && position != lastPathPoint) {
            val midpoint = (position + lastPathPoint) * 0.5f
            path.quadTo(lastPathPoint.x, lastPathPoint.y, midpoint.x, midpoint.y)
            lastPathPoint.set(position)
        }
    }

    fun drawBackground(canvas: Canvas) {
        if (!applyPhysics || getTimeSinceRelease() <= PATH_FADEOUT_LENGTH) {
            updatePathColor()
            canvas.drawPath(path, pathPaint)
        }
    }

    fun drawForeground(canvas: Canvas) {
        val position = positionOnScreen.get()
        updateDrawColor()
        canvas.drawCircle(position.x, position.y, radius, paint)
    }

    fun shouldBeDeleted(): Boolean {
        return getTimeSinceRelease() > TIME_BEFORE_DELETION
    }

    fun startApplyingPhysics() {
        applyPhysics = true
        releaseTime = SystemClock.uptimeMillis()
    }

    private fun updateDrawColor() {
        if (!applyPhysics) {
            return
        }
        val fadeoutStartTime = TIME_BEFORE_DELETION - BALL_FADEOUT_LENGTH
        val timeSinceFadeoutStart = getTimeSinceRelease() - fadeoutStartTime
        if (timeSinceFadeoutStart <= 0) {
            return
        }
        val fadeoutPercentage = timeSinceFadeoutStart / BALL_FADEOUT_LENGTH
        paint.color = applyFadeout(color, fadeoutPercentage)
    }

    private fun updatePathColor() {
        val fadeoutPercentage =
            (getTimeSinceRelease() / PATH_FADEOUT_LENGTH * PATH_INITIAL_ALPHA).coerceAtLeast(0f) +
                    (1 - PATH_INITIAL_ALPHA)
        pathPaint.color = applyFadeout(pathColor, fadeoutPercentage)
    }

    private fun applyFadeout(color: Int, fadeoutPercentage: Float): Int {
        val alpha = ((1 - fadeoutPercentage) * 255f).roundToInt().coerceIn(0..255)
        val colorWithoutAlpha = color and ((1 shl 3 * 8) - 1)
        return (alpha shl 3 * 8) or colorWithoutAlpha
    }

    private fun getTimeSinceRelease(): Float {
        return (SystemClock.uptimeMillis() - releaseTime) / 1000f
    }

    private companion object Constants {
        const val TIME_BEFORE_DELETION = 5f
        const val BALL_FADEOUT_LENGTH = 2f
        const val PATH_FADEOUT_LENGTH = 0.75f
        const val PATH_WIDTH_RELATIVE_TO_RADIUS = 0.2f
        const val PATH_INITIAL_ALPHA = 0.3f
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