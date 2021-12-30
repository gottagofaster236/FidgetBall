package com.lr_soft.fidget_ball

import android.graphics.PointF

class Segment(val start: PointF, val end: PointF) {
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

        val velocityProjection = velocity dotProduct vector
        if (velocityProjection == 0f) {
            return -1f
        }
        val signedDistance = (velocity - start) crossProduct vector
        val timeAfterContingence = (signedDistance + ball.radius) / velocityProjection
        val timeAfterCenterIsOnLine = signedDistance / velocityProjection

        if (timeAfterContingence < 0) {
            return -1f
        }

        val contigencePoint = position - velocity * timeAfterCenterIsOnLine
        val insideCheck = (start - contigencePoint) crossProduct (contigencePoint - end)
        if (insideCheck >= 0) {
            // The ball collides at the contigence point
            return timeAfterContingence
        }

        val closestSegmentEnd = listOf(start, end).minBy { dist(it, contigencePoint) }!!
        TODO()
    }

    fun adjustBallPositionAndVelocity(ball: Ball, timeAfterCollision: Float) {
        val position = ball.position.toPointF()
        val velocity = ball.velocity

        val parallelVelocity = velocity projectOnto vector
        val perpendicularVector = PointF(-vector.y, vector.x)
        val perpendicularVelocity = velocity projectOnto perpendicularVector

        val correctedVelocity = parallelVelocity - perpendicularVelocity
        val positionAtCollision = position - velocity * timeAfterCollision
        val correctedPosition = positionAtCollision + correctedVelocity * timeAfterCollision

        ball.position.set(correctedPosition)
        ball.velocity.set(correctedVelocity)
    }
}
