package com.lr_soft.fidget_ball

import android.graphics.PointF
import kotlin.math.hypot

operator fun PointF.plus(other: PointF): PointF {
    return PointF(x + other.x, y + other.y)
}

operator fun PointF.minus(other: PointF): PointF {
    return PointF(x - other.x, y - other.y)
}

operator fun PointF.times(scalar: Float): PointF {
    return PointF(x * scalar, y * scalar)
}

operator fun PointF.div(scalar: Float): PointF {
    return PointF(x / scalar, y / scalar)
}

infix fun PointF.dotProduct(other: PointF): Float {
    return x * other.x + y * other.y
}

infix fun PointF.crossProduct(other: PointF): Float {
    return x * other.y - y * other.x
}

infix fun PointF.projectOnto(other: PointF): PointF {
    return other * (this dotProduct other)
}

fun dist(a: PointF, b: PointF): Float {
    return hypot(a.x - b.x, a.y - b.y)
}