package com.lr_soft.fidget_ball

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import java.util.concurrent.ArrayBlockingQueue

/**
 * Vibrator::vibrate can take up to 5 milliseconds.
 * This helper class moves that call onto a background thread.
 */
class AsyncVibrator(context: Context) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val vibrationRequestsQueue = ArrayBlockingQueue<Long>(VIBRATOR_QUEUE_CAPACITY)

    private var vibrationThread: Thread? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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

            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                // Don't vibrate if the user has muted their phone.
                continue
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