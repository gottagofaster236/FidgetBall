package com.lr_soft.fidget_ball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.os.SystemClock
import android.util.Log
import java.util.*

class PhysicsContainer(val width: Int, val height: Int) {
    private val ball = Ball(
        position = ConcurrentPointF(width / 2f, height / 2f),
        velocity = PointF(width * 0.1f, width * 0.1f),
        radius = width * 0.05f
    )

    private val obstacles: MutableList<Obstacle> = mutableListOf()

    init {
        obstacles.add(
            Box(
                bounds = RectF(0f, 0f, width.toFloat(), height.toFloat()),
                facingOutwards = false,
                bounceCoefficient = 1f,
                color = Color.BLACK
            )
        )
    }

    fun draw(canvas: Canvas) {
        ball.draw(canvas)
        for (obstacle in obstacles) {
            obstacle.draw(canvas)
        }
    }

    private var physicsTaskTimer: Timer? = null

    fun startPhysics() {
        stopPhysics()
        lastPhysicsStepTime = SystemClock.uptimeMillis()

        physicsTaskTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    physicsStep()
                }
            }, 0L, 16L)
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

        applyGravity(timeSinceLastStep)
        applyVelocity(timeSinceLastStep)
        fixCollision(timeSinceLastStep)
    }

    private fun applyGravity(timeSinceLastStep: Float) {
        val g = width * 0.2f
        ball.velocity.y += timeSinceLastStep * g
    }

    private fun applyVelocity(timeSinceLastStep: Float) {
        ball.position.set(
            ball.position.toPointF() + ball.velocity * timeSinceLastStep
        )
    }

    private fun fixCollision(timeSinceLastStep: Float) {
        for (obstacle in obstacles) {
            obstacle.adjustBallPositionAndVelocity(ball, timeSinceLastStep)
        }
    }
}