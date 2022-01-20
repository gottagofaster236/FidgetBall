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

    private var currentNewBall: Ball? = null

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
        for (ball in balls) {
            ball.draw(canvas)
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
        updatePositionForDraw()
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

    fun createNewCurrentBall(position: PointF) {
        balls.remove(currentNewBall)
        currentNewBall = Ball(
            position = position,
            radius = unit * BALL_RADIUS,
        ).apply {
            makeSureIsInBounds()
            updatePositionForDraw()
            creationTime = SystemClock.uptimeMillis()
            balls.add(this)
        }
    }

    fun moveCurrentBallToPosition(position: PointF) {
        currentNewBall?.apply {
            this.position.set(position)
            makeSureIsInBounds()
            updatePositionForDraw()
        }
    }

    fun addCurrentBall(velocity: PointF) {
        currentNewBall?.apply {
            this.velocity.set(velocity)
            applyPhysics = true
        }
        currentNewBall = null
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