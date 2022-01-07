package com.lr_soft.fidget_ball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.os.SystemClock
import android.util.Log
import java.util.*

class PhysicsContainer(val width: Int, val height: Int) {
    val balls = mutableListOf<Ball>()

    private val obstacles: MutableList<Obstacle> = mutableListOf()

    init {
        balls.addAll(listOf(
            Ball(
                position = PointF(width / 2f, height / 2f),
                velocity = PointF(width * 1f, -width * 3f),
                radius = width * 0.05f,
                applyPhysics = true
            ),
            Ball(
                position = PointF(width / 2f, height / 2f),
                velocity = PointF(-width * 2f, width * 1.5f),
                radius = width * 0.05f
            )
        ))

        obstacles.add(
            Box(
                bounds = RectF(0f, 0f, width.toFloat(), height.toFloat()),
                facingOutwards = false,
                bounceCoefficient = 0.7f,
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
        val g = width * 0.8f
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
}