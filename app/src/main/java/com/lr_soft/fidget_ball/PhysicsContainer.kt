package com.lr_soft.fidget_ball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.os.SystemClock
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class PhysicsContainer(val width: Int, val height: Int) {
    private val balls = CopyOnWriteArrayList<Ball>()

    private var currentBalls = mutableMapOf<Int, Ball>()

    private val obstacles: MutableList<Obstacle> = mutableListOf()

    private val unit = (width + height) / 2

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

    fun startPhysics(displayRefreshRate: Float) {
        stopPhysics()
        lastPhysicsStepTime = SystemClock.uptimeMillis()

        val period = (1000 / displayRefreshRate).toLong().coerceAtLeast(1)

        physicsTaskTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    physicsStep()
                }
            }, 0L, period)
        }
    }

    fun stopPhysics() {
        physicsTaskTimer?.cancel()
        physicsTaskTimer = null
    }

    private var lastPhysicsStepTime = 0L

    private fun physicsStep() {
        val currentTime = SystemClock.uptimeMillis()
        val timeSinceLastStep = (currentTime - lastPhysicsStepTime) / 1000f
        lastPhysicsStepTime = currentTime

        balls.filter { it.applyPhysics }.forEach {
            it.physicsStep(timeSinceLastStep)
            if (it.shouldBeDeleted()) {
                balls.remove(it)
            }
        }
    }

    private fun Ball.physicsStep(timeSinceLastStep: Float) {
        applyGravity(timeSinceLastStep)
        applyVelocity(timeSinceLastStep)
        fixCollision(timeSinceLastStep)
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

    private fun Ball.fixCollision(timeSinceLastStep: Float) {
        for (obstacle in obstacles) {
            obstacle.adjustBallPositionAndVelocity(this, timeSinceLastStep)
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
        if (!currentBalls.containsKey(id)) {
            createCurrentBall(id, position)
        }
        currentBalls.getValue(id).apply {
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

    private companion object Constants {
        const val GRAVITY_ACCELERATION = 3f
        const val BALL_RADIUS = 0.045f
    }
}