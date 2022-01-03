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
        val position = ball.position.toPointF()
        val velocity = ball.velocity

        val velocityProjection = velocity crossProduct vector
        if (velocityProjection == 0f) {
            return -1f
        }
        val signedDistance = (position - start) crossProduct vector
        val timeAfterContingence = (signedDistance + ball.radius) / velocityProjection
        val timeAfterCenterIsOnLine = signedDistance / velocityProjection

        if (timeAfterContingence < 0) {
            return -1f
        }

        val contigencePoint = position - velocity * timeAfterCenterIsOnLine
        val contigencePointInsideCheck = (start - contigencePoint) dotProduct (contigencePoint - end)
        if (contigencePointInsideCheck >= 0) {
            // The ball collides at the contigence point
            return timeAfterContingence
        }

        val closestSegmentEnd = listOf(start, end).minBy { dist(it, contigencePoint) }!!
        // TODO()
        return -1f
    }

    fun adjustBallPositionAndVelocity(ball: Ball, timeAfterCollision: Float) {
        val position = ball.position.toPointF()
        val velocity = ball.velocity

        val parallelVelocity = velocity projectOnto vector
        val perpendicularVelocity = velocity projectOnto vector.perpendicular()

        val correctedVelocity = (parallelVelocity - perpendicularVelocity) * bounceCoefficient
        val positionAtCollision = position - velocity * timeAfterCollision
        val correctedPosition = positionAtCollision + correctedVelocity * timeAfterCollision

        ball.position.set(correctedPosition)
        ball.velocity.set(correctedVelocity)
    }
}
