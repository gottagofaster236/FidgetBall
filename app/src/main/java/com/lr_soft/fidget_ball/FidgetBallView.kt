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
    private var velocityTrackerForPointerId = mutableMapOf<Int, VelocityTracker>()

    init {
        setBackgroundColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        /**
         * Make sure we're being drawn at the maximum possible FPS
         * by calling [invalidate] immediately.
         */
        invalidate()
        physicsContainer?.draw(canvas)
    }

    /**
     * Handle the View size changes.
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val physicsContainer = physicsContainer
        if (
            physicsContainer == null ||
            physicsContainer.width != width ||
            physicsContainer.height != height
        ) {
            physicsContainer?.stopPhysics()
            @Suppress("DrawAllocation")
            this.physicsContainer = PhysicsContainer(context, width, height)
        }
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
        val physicsContainer = physicsContainer ?: return true
        val actionIndex = event.actionIndex
        val actionPointerId = event.getPointerId(actionIndex)
        val actionPosition = PointF(event.getX(actionIndex), event.getY(actionIndex))

        fun onActionDown() {
            /**
             * Add a new ball when the user touches the screen.
             */
            velocityTrackerForPointerId[actionPointerId] = VelocityTracker.obtain().apply {
                addMovement(event, actionIndex)
            }
            physicsContainer.createCurrentBall(actionPointerId, actionPosition)
        }

        fun onActionUp() {
            /**
             * Calculate the speed and release the ball at that speed
             * when the user lifts their finger.
             */
            physicsContainer.moveCurrentBallToPosition(actionPointerId, actionPosition)
            val velocityTracker = velocityTrackerForPointerId[actionPointerId] ?: return
            val velocity = with(velocityTracker) {
                // Compute the speed in pixels per second.
                computeCurrentVelocity(1000)
                PointF(xVelocity, yVelocity) * VELOCITY_COEFFICIENT
            }
            physicsContainer.addCurrentBallToField(actionPointerId, velocity)
            velocityTrackerForPointerId.remove(actionPointerId)?.recycle()
        }

        fun onActionMove() {
            /**
             * Move the corresponding ball if the user moves their finger.
             */
            for (pointerIndex in 0 until event.pointerCount) {
                val pointerPosition = PointF(event.getX(pointerIndex), event.getY(pointerIndex))
                val pointerId = event.getPointerId(pointerIndex)
                physicsContainer.moveCurrentBallToPosition(pointerId, pointerPosition)
                velocityTrackerForPointerId[pointerId]?.addMovement(event, pointerIndex)
            }
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN ->
                onActionDown()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP ->
                onActionUp()
            MotionEvent.ACTION_MOVE -> onActionMove()
            else -> return false
        }
        return true
    }

    private fun VelocityTracker.addMovement(event: MotionEvent, pointerIndex: Int) {
        val pointerIndexEvent = event.getEventForPointerIndex(pointerIndex)
        addMovement(pointerIndexEvent)
        pointerIndexEvent.recycle()
    }

    /**
     * Returns an event containing the position of the specified [pointerIndex].
     * Call `recycle()` on the event after using it.
     *
     * Based on [this answer from Stackoverflow](https://stackoverflow.com/a/49722416/6120487).
     */
    private fun MotionEvent.getEventForPointerIndex(pointerIndex: Int): MotionEvent {
        return MotionEvent.obtain(
            downTime, eventTime, actionMasked, getX(pointerIndex), getY(pointerIndex), metaState
        )
    }

    companion object Constants {
        /**
         * The actual finger movement is usually too fast,
         * so a decreasing coefficient is used.
         */
        private const val VELOCITY_COEFFICIENT = 0.65f
        const val TAG = "FidgetBallView"
    }
}