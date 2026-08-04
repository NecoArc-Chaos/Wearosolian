package dev.solsynth.solian.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Captures uncaught exceptions and persists the stack trace to a file so that
 * crash details can be inspected on-device without a USB/ADB connection.
 *
 * On the next app launch [readLatest] exposes the stored stack trace and the
 * UI can render it for the user to report.
 */
object CrashReport {
    private const val TAG = "CrashReport"
    private const val FILE_NAME = "crash.log"

    private var appContext: Context? = null

    fun install(context: Context) {
        appContext = context.applicationContext
        if (Thread.getDefaultUncaughtExceptionHandler() === handler) return
        Thread.setDefaultUncaughtExceptionHandler(handler)
    }

    private val handler = Thread.UncaughtExceptionHandler { thread, throwable ->
        Log.e(TAG, "Uncaught exception on ${thread.name}", throwable)
        try {
            val writer = StringWriter()
            PrintWriter(writer).use { throwable.printStackTrace(it) }
            val content = buildString {
                append("Thread: ").append(thread.name).append('\n')
                append("Time: ").append(System.currentTimeMillis()).append('\n')
                append("Class: ").append(throwable.javaClass.name).append('\n')
                append("Message: ").append(throwable.message ?: "").append('\n')
                append(writer.toString())
            }
            appContext?.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)?.use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
            }
        } catch (_: Exception) {
            // Never block crash reporting
        }
    }

    fun readLatest(): String? {
        val context = appContext ?: return null
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) file.readText() else null
        } catch (_: Exception) {
            null
        }
    }

    fun clear() {
        val context = appContext ?: return
        try {
            File(context.filesDir, FILE_NAME).delete()
        } catch (_: Exception) {
        }
    }
}
