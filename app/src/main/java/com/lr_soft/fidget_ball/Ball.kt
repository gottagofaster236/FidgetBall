package com.lr_soft.fidget_ball

import android.graphics.*
import android.os.SystemClock
import kotlin.math.roundToInt

class Ball(
    val position: PointF,
    val radius: Float,
    private val color: Int = Color.BLUE,
    private val trailColor: Int = Color.rgb(0, 200, 255),
) {
    val velocity = PointF()

    /**
     * [position] can contain intermediary results as the physics calculations are running.
     *
     * On the contrary, [positionOnScreen] has the position at the last finished physics step,
     * that is safe to draw on screen,
     */
    private val positionOnScreen = ConcurrentPointF(position)

    private val paint = Paint().apply {
        color = this@Ball.color
        isAntiAlias = true
    }

    /**
     * If `false`, the ball's position and velocity aren't subject to the physics calculations.
     */
    @Volatile
    var applyPhysics = false
        private set

    /**
     * The time at which the user released the ball into the playing field.
     */
    @Volatile
    var releaseTime = Long.MAX_VALUE

    /**
     * The number of times that the ball has collided with a wall.
     */
    var collisionCount = 0

    private val trail = Path().apply {
        moveTo(position.x, position.y)
    }

    private val lastTrailPoint = PointF(position.x, position.y)

    private val trailPaint = Paint().apply {
        color = Color.TRANSPARENT
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = radius * PATH_WIDTH_RELATIVE_TO_RADIUS
    }

    fun drawBackground(canvas: Canvas) {
        if (!applyPhysics || getTimeSinceRelease() <= PATH_FADEOUT_LENGTH) {
            updatePathColor()
            canvas.drawPath(trail, trailPaint)
        }
    }

    fun drawForeground(canvas: Canvas) {
        val position = positionOnScreen.get()
        updateDrawColor()
        canvas.drawCircle(position.x, position.y, radius, paint)
    }

    fun startApplyingPhysics() {
        applyPhysics = true
        releaseTime = SystemClock.uptimeMillis()
    }

    fun shouldBeDeleted(): Boolean {
        return getTimeSinceRelease() > TIME_BEFORE_DELETION
    }

    /**
     * Updates the current position of the ball on screen with the value in [position].
     */
    fun updatePositionOnScreen() {
        positionOnScreen.set(position)
        if (!applyPhysics && position != lastTrailPoint) {
            /**
             * If [applyPhysics] if `false`, that means that this function was called from the UI thread
             * (check the usages yourself). Thus it's safe to use make operations on the [trail].
             * ([Path] can only be used from the UI thread).
             */
            val midpoint = (position + lastTrailPoint) * 0.5f
            trail.quadTo(lastTrailPoint.x, lastTrailPoint.y, midpoint.x, midpoint.y)
            lastTrailPoint.set(position)
        }
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
            (getTimeSinceRelease() / PATH_FADEOUT_LENGTH * PATH_INITIAL_ALPHA)
                .coerceAtLeast(0f) + (1 - PATH_INITIAL_ALPHA)
        trailPaint.color = applyFadeout(trailColor, fadeoutPercentage)
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
        const val PATH_FADEOUT_LENGTH = 1f
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