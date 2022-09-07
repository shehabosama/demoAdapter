package com.example.gittutorial

import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.gittutorial.databinding.ActivityAlarmManagerSampleBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*

class AlarmManagerSample : AppCompatActivity() {
    private lateinit var binding:ActivityAlarmManagerSampleBinding
    private lateinit var picker:MaterialTimePicker
    private lateinit var calendar: Calendar
    private lateinit var alarmManager:AlarmManager
    private lateinit var pendingIntent:PendingIntent
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmManagerSampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!Settings.canDrawOverlays(this)) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 0)
        }
        createNotificationChannel()
        binding.btnSelectTime.setOnClickListener {
            showTimePicker()
        }
        binding.btnSetAlarm.setOnClickListener {
            setAlarm()
        }
        binding.btnCancelAlarm.setOnClickListener {
            cancelAlarm()
        }
    }

    private fun cancelAlarm() {
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        pendingIntent = PendingIntent.getBroadcast(this,0, intent ,0 )

        alarmManager.cancel(pendingIntent)
        Toast.makeText(this, "Alarm canceled", Toast.LENGTH_SHORT).show()
    }

    private fun setAlarm() {
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        pendingIntent = PendingIntent.getBroadcast(this,(0..2147483647).random(), intent ,0 )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,

            pendingIntent
        )
        Toast.makeText(this, "The alarm puttied successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showTimePicker() {
        picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(12).setMinute(0).setTitleText("Select Alarm Time").build()
        picker.show(supportFragmentManager , "alarm_manager_id")
        picker.addOnPositiveButtonClickListener {
            if(picker.hour>12){
                binding.timeText.text =String.format("%02d",picker.hour-12)+" : "+ String.format("%02d" , picker.minute)+" PM"
            }else{
                binding.timeText.text =  String.format("%02d" , picker.hour)+" : "+String.format("%02d" , picker.minute)+" AM"
            }
            calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = picker.hour
            calendar[Calendar.MINUTE] = picker.minute
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] =0



        }
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
}