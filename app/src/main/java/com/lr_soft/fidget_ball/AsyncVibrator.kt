package com.lr_soft.fidget_ball

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

/**
 * android.os.Vibrator::vibrate can take up to 5 milliseconds.
 * This helper class moves that call onto a background thread.
 */
class AsyncVibrator(context: Context) {
    private val vibrator = getVibrator(context)

    private val vibrationRequestsQueue = ArrayBlockingQueue<Long>(VIBRATOR_QUEUE_CAPACITY)

    private var vibrationThread: Thread? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun vibrate(lengthMs: Long) {
        vibrationRequestsQueue.offer(lengthMs)
    }

    fun start() {
        vibrationThread = thread {
            processVibrationRequests()
        }
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

            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0) {
                // Vibrate if the user hasn't muted their phone.
                vibrateImpl(lengthMs)
            }
        }
    }

    private fun vibrateImpl(lengthMs: Long) {
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

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private companion object {
        const val VIBRATOR_QUEUE_CAPACITY = 10
    }
}