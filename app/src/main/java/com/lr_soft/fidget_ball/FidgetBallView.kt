package com.lr_soft.fidget_ball

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class FidgetBallView(context: Context): View(context) {
    private val paint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
    }

    private var physicsContainer: PhysicsContainer? = null

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val physicsContainer = physicsContainer
        if (
            physicsContainer == null ||
            physicsContainer.width != width ||
            physicsContainer.height != height
        ) {
            // noinspection DrawAllocation
            this.physicsContainer = PhysicsContainer(width, height)
        }
    }

    override fun onDraw(canvas: Canvas) {
        physicsContainer?.draw(canvas)
    }
}