package com.lr_soft.fidget_ball

import android.graphics.Canvas

interface Obstacle {
    fun draw(canvas: Canvas)

    fun adjustBallPositionAndVelocity(ball: Ball, timeSinceLastStep: Float)
}
