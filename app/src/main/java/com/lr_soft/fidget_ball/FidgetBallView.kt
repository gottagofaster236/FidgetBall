package com.lr_soft.fidget_ball

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.os.Build
import android.view.*

class FidgetBallView(context: Context): View(context) {
    private var physicsContainer: PhysicsContainer? = null
    private var velocityTracker: VelocityTracker = VelocityTracker.obtain()

    init {
        setBackgroundColor(Color.WHITE)
    }

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
        val actionPointerId = event.getPointerId(event.actionIndex)
        val actionPosition = PointF(event.getX(event.actionIndex), event.getY(event.actionIndex))

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                physicsContainer.createCurrentBall(actionPointerId, actionPosition)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                physicsContainer.moveCurrentBallToPosition(actionPointerId, actionPosition)
                val velocity = with(velocityTracker) {
                    // Compute the speed in pixels per second.
                    computeCurrentVelocity(1000)
                    PointF(xVelocity, yVelocity) * VELOCITY_COEFFICIENT
                }
                physicsContainer.addCurrentBallToField(actionPointerId, velocity)
            }

            MotionEvent.ACTION_MOVE -> {
                for (pointerIndex in 0 until event.pointerCount) {
                    val pointerPosition = PointF(event.getX(pointerIndex), event.getY(pointerIndex))
                    val pointerId = event.getPointerId(pointerIndex)
                    physicsContainer.moveCurrentBallToPosition(pointerId, pointerPosition)
                }
            }

            else -> return false
        }
        return true
    }

    private companion object Constants {
        /**
         * The actual finger movement is usually too fast,
         * so a decreasing coefficient is used.
         */
        const val VELOCITY_COEFFICIENT = 0.65f
        const val TAG = "FidgetBallView"
    }
}