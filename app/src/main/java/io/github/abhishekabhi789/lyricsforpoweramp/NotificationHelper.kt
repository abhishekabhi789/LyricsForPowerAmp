package io.github.abhishekabhi789.lyricsforpoweramp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.maxmpz.poweramp.player.PowerampAPI
import io.github.abhishekabhi789.lyricsforpoweramp.model.Track
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference
import java.util.UUID

class NotificationHelper(private val context: Context) {
    private val isNotificationEnabled = AppPreference.getShowNotification(context)
    private val TAG = javaClass.simpleName
    private var notificationId: Int = generateNotificationId(context)
    private var channelName: String =
        context.getString(R.string.lyrics_request_handling_notifications)

    companion object {
        private const val CHANNEL_ID = "request_handling_notification"
        private const val DEFAULT_NOTIFICATION_ID = 789
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (!isNotificationEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun makeNotification(
        title: String,
        content: String,
        subText: String? = null,
        track: Track? = null
    ) {
        if (!isNotificationEnabled) return
        Log.d(TAG, "makeNotification: $content")
        val pendingIntent = if (track != null) {
            val intent = Intent(context, MainActivity::class.java).apply {
                action = LyricsRequestReceiver.MANUAL_SEARCH_ACTION
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(PowerampAPI.Track.REAL_ID, track.realId!!)
                putExtra(PowerampAPI.Track.TITLE, track.trackName!!)
                putExtra(PowerampAPI.Track.ARTIST, track.artistName!!)
                putExtra(PowerampAPI.Track.ALBUM, track.albumName!!)
                putExtra(PowerampAPI.Track.DURATION_MS, track.duration!! * 1000)
            }

            PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .apply {
                if (track != null) setAutoCancel(true)
                if (pendingIntent != null) setContentIntent(pendingIntent)
                if (subText != null) setSubText(subText)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) setPriority(Notification.PRIORITY_LOW)
            }
            .build()
        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification() {
        if (!isNotificationEnabled) return
        Log.d(TAG, "cancelNotification:")
        notificationManager.cancel(notificationId)
    }

    private fun generateNotificationId(context: Context): Int {
        val overwriteNotification = AppPreference.getOverwriteNotification(context)
        return if (overwriteNotification) DEFAULT_NOTIFICATION_ID else UUID.randomUUID().hashCode()
    }
}
