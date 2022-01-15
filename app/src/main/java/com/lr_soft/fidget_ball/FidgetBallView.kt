package com.lr_soft.fidget_ball

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.os.Build
import android.view.*

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
            @Suppress("DrawAllocation")
            this.physicsContainer = PhysicsContainer(width, height)
            if (hasWindowFocus()) {
                startPhysics()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        invalidate()
        physicsContainer?.draw(canvas)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) {
            startPhysics()
        } else {
            physicsContainer?.stopPhysics()
        }
    }

    private fun startPhysics() {
        val display: Display? = if (Build.VERSION.SDK_INT >= 30) {
            context.display
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        }
        val refreshRate = display?.refreshRate ?: 60f
        physicsContainer?.startPhysics(refreshRate)
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