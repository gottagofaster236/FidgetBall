package com.lr_soft.fidget_ball

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF

class Box(
    private val bounds: RectF,
    facingOutwards: Boolean = true,
    color: Int
) : Obstacle {
    private val paint = Paint().apply {
        this.color = color
    }

    private val segments: List<Segment>

    init {
        val segments = listOf(
            Segment(PointF(bounds.left, bounds.bottom), PointF(bounds.right, bounds.bottom)),
            Segment(PointF(bounds.right, bounds.bottom), PointF(bounds.right, bounds.top)),
            Segment(PointF(bounds.right, bounds.top), PointF(bounds.left, bounds.top)),
            Segment(PointF(bounds.left, bounds.top), PointF(bounds.left, bounds.bottom))
        )

        if (facingOutwards) {
            this.segments = segments
        } else {
            this.segments = segments.map { segment ->
                Segment(segment.end, segment.start)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(bounds, paint)
    }

    override fun adjustBallPositionAndVelocity(ball: Ball, timeSinceLastStep: Float) {
        // TODO there may be two of those
        val (segment, timeAfterCollision) = segments.map { segment ->
            segment to segment.getTimeAfterCollision(ball)
        }.filter { (_, timeAfterIntersection) ->
            // NaN will return false for each of the comparisons
            timeSinceLastStep > timeAfterIntersection && timeAfterIntersection > 0
        }.minBy(Pair<Segment, Float>::second) ?: return

        segment.adjustBallPositionAndVelocity(ball, timeAfterCollision)
    }
}