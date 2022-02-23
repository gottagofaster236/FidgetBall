package com.lr_soft.fidget_ball

import android.graphics.PointF
import android.graphics.RectF

class Box(
    bounds: RectF,
    bounceCoefficient: Float,
) {
    private val halfPlanes = listOf(
        HalfPlane(PointF(bounds.left, bounds.bottom), PointF(bounds.left, bounds.top), bounceCoefficient),
        HalfPlane(PointF(bounds.left, bounds.top), PointF(bounds.right, bounds.top), bounceCoefficient),
        HalfPlane(PointF(bounds.right, bounds.top), PointF(bounds.right, bounds.bottom), bounceCoefficient),
        HalfPlane(PointF(bounds.right, bounds.bottom), PointF(bounds.left, bounds.bottom), bounceCoefficient)
    )

    fun adjustBallPositionAndVelocity(ball: Ball, timeSinceLastStep: Float) {
        halfPlanes.map { segment ->
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
