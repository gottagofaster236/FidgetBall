package com.lr_soft.fidget_ball

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF

class Box(
    private val bounds: RectF,
    private val facingOutwards: Boolean = true,
    bounceCoefficient: Float,
    color: Int
) : Obstacle {
    private val paint = Paint().apply {
        this.color = color
    }

    private val segments: List<Segment>

    init {
        val segments = listOf(
            Segment(PointF(bounds.left, bounds.bottom), PointF(bounds.right, bounds.bottom), bounceCoefficient),
            Segment(PointF(bounds.right, bounds.bottom), PointF(bounds.right, bounds.top), bounceCoefficient),
            Segment(PointF(bounds.right, bounds.top), PointF(bounds.left, bounds.top), bounceCoefficient),
            Segment(PointF(bounds.left, bounds.top), PointF(bounds.left, bounds.bottom), bounceCoefficient)
        )

        if (facingOutwards) {
            this.segments = segments
        } else {
            this.segments = segments.map { segment ->
                Segment(segment.end, segment.start, segment.bounceCoefficient)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        if (facingOutwards) {
            canvas.drawRect(bounds, paint)
        }
    }

    override fun adjustBallPositionAndVelocity(ball: Ball, timeSinceLastStep: Float) {
        segments.map { segment ->
            segment to segment.getTimeAfterCollision(ball)
        }.filter { (_, timeAfterIntersection) ->
            // NaN will return false for each of the comparisons
            timeSinceLastStep > timeAfterIntersection / (1 + FLOAT_ACCEPTABLE_RELATIVE_ERROR)
                && timeAfterIntersection >= 0
        }.forEach { (segment, timeAfterCollision) ->
            segment.adjustBallPositionAndVelocity(ball, timeAfterCollision)
        }
    }

    private companion object Constants {
        const val FLOAT_ACCEPTABLE_RELATIVE_ERROR = 0.1f
    }
}
