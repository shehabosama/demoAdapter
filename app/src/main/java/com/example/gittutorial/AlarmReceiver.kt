package com.example.gittutorial

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.lang.Package.getPackage

class AlarmReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
//        val i = Intent(context , MainActivity::class.java)
//        i?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val pendingIntent = PendingIntent.getActivity(context , 0 , i , 0)


        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(context, (0..2147483647).random(), contentIntent, 0)

        val fullScreenIntent = Intent(context, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(context, (0..2147483647).random(), fullScreenIntent, 0)

        val remoteView = RemoteViews(BuildConfig.APPLICATION_ID,R.layout.dialog_success)
        val builder = NotificationCompat.Builder(context!! , "alarm_manager_id")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Alarm Notification manager")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setCustomContentView(remoteView)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify((0..2147483647).random() , builder.build())


    }
}