package com.lr_soft.fidget_ball

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class FidgetBallView(context: Context): View(context) {
    private var physicsContainer: PhysicsContainer? = null

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val physicsContainer = physicsContainer
        if (
            physicsContainer == null ||
            physicsContainer.width != width ||
            physicsContainer.height != height
        ) {
            this.physicsContainer?.stopPhysics()
            // noinspection DrawAllocation
            this.physicsContainer = PhysicsContainer(width, height).apply {
                if (hasWindowFocus()) {
                    startPhysics()
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        postDelayed(::invalidate, 16)
        physicsContainer?.draw(canvas)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) {
            physicsContainer?.startPhysics()
        } else {
            physicsContainer?.stopPhysics()
        }
    }
}