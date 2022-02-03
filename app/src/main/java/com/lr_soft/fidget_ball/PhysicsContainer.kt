package com.lr_soft.fidget_ball

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.os.Build
import android.os.SystemClock
import android.view.Display
import android.view.WindowManager
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class PhysicsContainer(context: Context, val width: Int, val height: Int) {
    private val balls = CopyOnWriteArrayList<Ball>()

    private var currentBalls = mutableMapOf<Int, Ball>()

    private val obstacles: MutableList<Obstacle> = mutableListOf()

    private val unit = (width + height) / 2

    private val refreshRate = context.displayRefreshRate

    private val asyncVibrator = AsyncVibrator(context)

    init {
        obstacles.add(
            Box(
                bounds = RectF(0f, 0f, width.toFloat(), height.toFloat()),
                facingOutwards = false,
                bounceCoefficient = 0.6f,
                color = Color.BLACK
            )
        )
    }

    fun draw(canvas: Canvas) {
        // Make a copy of the list to avoid locking twice.
        val balls = balls.toList()
        for (ball in balls) {
            ball.drawBackground(canvas)
        }
        for (ball in balls) {
            ball.drawForeground(canvas)
        }

        for (obstacle in obstacles) {
            obstacle.draw(canvas)
        }
    }

    private var physicsTaskTimer: Timer? = null

    fun startPhysics() {
        stopPhysics()
        lastPhysicsStepTime = SystemClock.uptimeMillis()

        val period = (1000 / refreshRate).toLong().coerceAtLeast(1)

        physicsTaskTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    physicsStep()
                }
            }, 0L, period)
        }

        asyncVibrator.start()
    }

    fun stopPhysics() {
        physicsTaskTimer?.cancel()
        physicsTaskTimer = null
        asyncVibrator.stop()
    }

    private var lastPhysicsStepTime = 0L

    private fun physicsStep() {
        val currentTime = SystemClock.uptimeMillis()
        val timeSinceLastStep = (currentTime - lastPhysicsStepTime) / 1000f
        lastPhysicsStepTime = currentTime

        balls.filter(Ball::applyPhysics).forEachIndexed { index, ball ->
            val vibrateOnCollision =
                (index < MAX_BALLS_VIBRATING / 2 || balls.lastIndex - index < MAX_BALLS_VIBRATING / 2)
                    && ball.collisionCount < MAX_VIBRATIONS_PER_BALL
            ball.physicsStep(timeSinceLastStep, vibrateOnCollision)
            if (ball.shouldBeDeleted()) {
                balls.remove(ball)
            }
        }
    }

    private fun Ball.physicsStep(timeSinceLastStep: Float, vibrateOnCollision: Boolean = false) {
        applyGravity(timeSinceLastStep)
        applyVelocity(timeSinceLastStep)
        fixCollision(timeSinceLastStep, vibrateOnCollision)
        updatePositionOnScreen()
    }

    private fun Ball.applyGravity(timeSinceLastStep: Float) {
        val g = unit * GRAVITY_ACCELERATION
        velocity.y += timeSinceLastStep * g
    }

    private fun Ball.applyVelocity(timeSinceLastStep: Float) {
        position.set(
            position + velocity * timeSinceLastStep
        )
    }

    private fun Ball.fixCollision(timeSinceLastStep: Float, vibrateOnCollision: Boolean) {
        val oldVelocity = PointF(velocity.x, velocity.y)
        for (obstacle in obstacles) {
            obstacle.adjustBallPositionAndVelocity(this, timeSinceLastStep)
        }
        if (vibrateOnCollision) {
            val velocityDifference = (velocity - oldVelocity).length() / unit
            if (velocityDifference >= VELOCITY_DIFFERENCE_VIBRATION_THRESHOLD) {
                asyncVibrator.vibrate(VIBRATION_LENGTH)
                collisionCount++
            }
        }
    }

    fun createCurrentBall(id: Int, position: PointF) {
        currentBalls[id]?.let {
            balls.remove(it)
        }

        currentBalls[id] = Ball(
            position = position,
            radius = unit * BALL_RADIUS,
        ).apply {
            makeSureIsInBounds()
            updatePositionOnScreen()
            balls.add(this)
        }
    }

    fun moveCurrentBallToPosition(id: Int, position: PointF) {
        currentBalls[id]?.apply {
            this.position.set(position)
            makeSureIsInBounds()
            updatePositionOnScreen()
        }
    }

    fun addCurrentBallToField(id: Int, velocity: PointF) {
        currentBalls[id]?.apply {
            this.velocity.set(velocity)
            startApplyingPhysics()
        }
        currentBalls.remove(id)
    }

    private fun Ball.makeSureIsInBounds() {
        position.x = position.x.coerceIn(radius..width - radius)
        position.y = position.y.coerceIn(radius..height - radius)
    }

    private val Context.displayRefreshRate: Float
        get() {
            val display: Display? = if (Build.VERSION.SDK_INT >= 30) {
                display
            } else {
                @Suppress("DEPRECATION")
                (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            }
            return display?.refreshRate ?: 60f
        }

    private companion object Constants {
        const val GRAVITY_ACCELERATION = 3f
        const val BALL_RADIUS = 0.045f
        const val VELOCITY_DIFFERENCE_VIBRATION_THRESHOLD = 1f
        const val VIBRATION_LENGTH = 30L
        const val MAX_BALLS_VIBRATING = 10
        const val MAX_VIBRATIONS_PER_BALL = 10
    }
}