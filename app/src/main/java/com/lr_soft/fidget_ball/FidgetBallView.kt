package com.lr_soft.fidget_ball

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View

class FidgetBallView(context: Context): View(context) {
    private var physicsContainer: PhysicsContainer? = null
    private var velocityTracker: VelocityTracker = VelocityTracker.obtain()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val physicsContainer = physicsContainer
        if (
            physicsContainer == null ||
            physicsContainer.width != width ||
            physicsContainer.height != height
        ) {
            physicsContainer?.stopPhysics()
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

    @SuppressLint("ClickableViewAccessibility", "Recycle")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                velocityTracker.clear()
                velocityTracker.addMovement(event)
            }

            MotionEvent.ACTION_MOVE -> velocityTracker.addMovement(event)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {}

            else -> return false
        }

        val physicsContainer = physicsContainer ?: return true
        val position = PointF(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                physicsContainer.createNewCurrentBall(position)
            }

            MotionEvent.ACTION_MOVE -> {
                physicsContainer.moveCurrentBallToPosition(position)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                physicsContainer.moveCurrentBallToPosition(position)
                val velocity = with(velocityTracker) {
                    // TODO use the two-argument version of the function to limit the max speed.
                    computeCurrentVelocity(1000)  // Compute the speed in pixels per second.
                    PointF(xVelocity, yVelocity)
                }
                physicsContainer.addCurrentBall(velocity)
            }

            else -> return false
        }
        return true
    }
}