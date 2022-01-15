package com.lr_soft.fidget_ball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.os.SystemClock
import java.util.*
import kotlin.math.ceil

class PhysicsContainer(val width: Int, val height: Int) {
    private val balls = mutableListOf<Ball>()

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
        }
    }

    private fun Ball.physicsStep(timeSinceLastStep: Float) {
        applyGravity(timeSinceLastStep)
        applyVelocity(timeSinceLastStep)
        fixCollision(timeSinceLastStep)
        updatePositionForDraw()
    }

    private fun Ball.applyGravity(timeSinceLastStep: Float) {
        val g = unit * 1.2f
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
            radius = unit * 0.04f,
        ).also { balls.add(it) }
    }

    fun moveCurrentBallToPosition(position: PointF) {
        currentNewBall?.apply {
            this.position.set(position)
            this.position.x = this.position.x.coerceIn(radius..width - radius)
            this.position.y = this.position.y.coerceIn(radius..height - radius)
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
}