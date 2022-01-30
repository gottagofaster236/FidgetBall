package com.lr_soft.fidget_ball

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import java.util.concurrent.ArrayBlockingQueue

/**
 * @author https://github.com/gottagofaster236
 */
class AsyncVibrator(context: Context) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val vibrationRequestsQueue = ArrayBlockingQueue<Long>(VIBRATOR_QUEUE_CAPACITY)

    private var vibrationThread: Thread? = null

    fun vibrate(lengthMs: Long) {
        vibrationRequestsQueue.offer(lengthMs)
    }

    fun start() {
        vibrationThread = Thread(::processVibrationRequests).apply(Thread::start)
    }

    fun stop() {
        vibrationThread?.interrupt()
        vibrationThread = null
    }

    private fun processVibrationRequests() {
        while (!Thread.currentThread().isInterrupted) {
            val lengthMs = try {
                vibrationRequestsQueue.take()
            } catch (e: InterruptedException) {
                break
            }

            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        lengthMs,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(lengthMs)
            }
        }
    }

    private companion object {
        const val VIBRATOR_QUEUE_CAPACITY = 10
    }
}