package com.lr_soft.fidget_ball

import android.graphics.PointF

class Segment(val start: PointF, val end: PointF, val bounceCoefficient: Float) {
    private val vector: PointF

    init {
        val denormalizedVector = end - start
        vector = denormalizedVector / denormalizedVector.length()
    }

    /**
     * Returns a negative value or NaN if the ball has not yet collided with the segment.
     */
    fun getTimeAfterCollision(ball: Ball): Float {
        val position = ball.position
        val velocity = ball.velocity

        val velocityProjection = velocity crossProduct vector
        if (velocityProjection <= 0f) {
            return -Float.MAX_VALUE
        }
        val signedDistance = (position - start) crossProduct vector
        val timeAfterContingence = (signedDistance + ball.radius) / velocityProjection
        val timeAfterCenterIsOnLine = signedDistance / velocityProjection

        if (timeAfterContingence < 0) {
            return -Float.MAX_VALUE
        }

        val contigencePoint = position - velocity * timeAfterCenterIsOnLine
        val contigencePointInsideCheck = (start - contigencePoint) dotProduct (contigencePoint - end)
        if (contigencePointInsideCheck >= 0 || true) {
            // The ball collides at the contigence point
            return timeAfterContingence
        }

        val closestSegmentEnd = listOf(start, end).minByOrNull { dist(it, contigencePoint) }!!
        // TODO()
        return -Float.MAX_VALUE
    }

    fun adjustBallPositionAndVelocity(ball: Ball, timeAfterCollision: Float) {
        val position = ball.position
        val velocity = ball.velocity

        val parallelVelocity = velocity projectOnto vector
        val perpendicularVelocity = velocity projectOnto vector.perpendicular()

        val correctedVelocity = parallelVelocity - perpendicularVelocity * bounceCoefficient
        val positionAtCollision = position - velocity * timeAfterCollision
        val correctedPosition = positionAtCollision + correctedVelocity * timeAfterCollision

        ball.position.set(correctedPosition)
        ball.velocity.set(correctedVelocity)
    }
}
