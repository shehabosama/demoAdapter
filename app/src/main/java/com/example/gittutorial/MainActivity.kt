package com.example.gittutorial

import Grocery
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class MainActivity : AppCompatActivity() {
    //lateinit var receiver: AirplaneModeChangeReceiver
    private val adapter by lazy {
        ItemAdapter(
            onGroceryClicked = ::makeToast
        )
    }
    private val list:MutableList<Any> = mutableListOf()
    lateinit var recyclerView: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        showWhenLockedAndTurnScreenOn()


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        receiver = AirplaneModeChangeReceiver()
//        IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED).also{
//            // receiver is the broadcast receiver that we have registered
//            // and it is the intent filter that we have created
//            registerReceiver(receiver,it)
//
//        }
        Firebase.messaging.isAutoInitEnabled = true

        recyclerView = findViewById(R.id.recycler_view)
        linearLayoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        recyclerView.layoutManager = linearLayoutManager

        val groceries = listOf(
            Grocery("A Tomato" ,"Vegetables" , "1b" , 3.0,3),
            Grocery("A Union" ,"Vegetables" , "1b" , 3.0,3),
            Grocery("Mushrooms" ,"Vegetables" , "1b" , 4.0,1),
            Grocery("Bagels" ,"Bakery" , "Pack" , 1.5,2),
            Grocery("Olive" ,"Pantry" , "Bottle" , 6.0,1),
            Grocery("Ice cream" ,"Frozen" , "Pack" , 3.0,2),)

        groceries.sortedBy { it.name }.groupBy { it.name.substring(0..0) }.forEach {it1->
            list.add(it1.key)
            it1.value.forEach {it2-> list.add(it2) }
        }.also {
            adapter.updateData(list)
            recyclerView.adapter = adapter
        }

    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    // ...
//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun askNotificationPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
//            PackageManager.PERMISSION_GRANTED
//        ) {
//            // FCM SDK (and your app) can post notifications.
//        } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
//            // TODO: display an educational UI explaining to the user the features that will be enabled
//            //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
//            //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
//            //       If the user selects "No thanks," allow the user to continue without notifications.
//        } else {
//            // Directly ask for the permission
//            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//        }
//    }

    private fun makeToast(grocery: Grocery) {
        Toast.makeText(this, grocery.name, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
       // unregisterReceiver(receiver)
    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        NotificationManagerCompat.from(this).cancelAll();
    }
}