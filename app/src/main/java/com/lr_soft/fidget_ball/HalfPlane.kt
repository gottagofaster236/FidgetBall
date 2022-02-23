package com.lr_soft.fidget_ball

import android.graphics.PointF

/**
 * Represents a half-plane with collision.
 * The half-plane is placed to the left of the vector
 * formed by [startPoint] and [endPoint].
 */
class HalfPlane(
    private val startPoint: PointF,
    private val endPoint: PointF,
    private val bounceCoefficient: Float
) {
    private val vector: PointF

    init {
        val denormalizedVector = endPoint - startPoint
        vector = denormalizedVector / denormalizedVector.length()
    }

    /**
     * Returns the time that has passed since the ball has collided
     * with this half-plane, or a negative value if it hasn't collided yet.
     */
    fun getTimeAfterCollision(ball: Ball): Float {
        val position = ball.position
        val velocity = ball.velocity

        val velocityProjection = velocity crossProduct vector
        if (velocityProjection <= 0f) {
            return -Float.MAX_VALUE
        }
        val signedDistance = (position - startPoint) crossProduct vector
        return (signedDistance + ball.radius) / velocityProjection
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
