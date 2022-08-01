package com.example.gittutorial

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    //generate the notification
    // attach the notification created with custom view
    // show the notification

    fun getRemoteView(title:String , message:String):RemoteViews{
        val remoteView = RemoteViews(BuildConfig.APPLICATION_ID,R.layout.notificatoin)
        remoteView.setTextViewText(R.id.message , message)
        remoteView.setTextViewText(R.id.title ,title)
        remoteView.setImageViewResource(R.id.icon , R.drawable.ic_launcher_background)
        return remoteView
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name:CharSequence = "alarm_channel"
            val description = "Channel for alarm manager"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(resources.getString(R.string.channel_id) , name , importance).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun generateNotification(title:String , message:String){
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(this, (0..2147483647).random(), contentIntent, 0)

        val fullScreenIntent = Intent(this, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(this, (0..2147483647).random(), fullScreenIntent, 0)


        var builder = NotificationCompat.Builder(this!! , "alarm_manager_id")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Alarm Notification manager")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)

            builder = builder.setContent(getRemoteView(title,message))
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify((0..2147483647).random() , builder.build())
        createNotificationChannel()
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.notification !=null){
            generateNotification(remoteMessage.notification!!.title!! , remoteMessage.notification!!.body!!)
        }
    }
    override fun onNewToken(token: String) {



    }


}