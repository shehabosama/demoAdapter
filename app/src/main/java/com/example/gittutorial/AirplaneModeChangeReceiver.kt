package com.example.gittutorial

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast

class AirplaneModeChangeReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // intent contains the information about the broadcast
        // in our case broadcast is change of airplane mode

        // if getBooleanExtra contains null value,it will directly return back
        val isAirPlaneModeEnabled = intent?.getBooleanExtra("state" ,false)?:return

        // check whether airplane mode is enabled or not

        if(isAirPlaneModeEnabled){
            Toast.makeText(context, "Airplane mode is enabled", Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(context, "Airplane mode is disabled", Toast.LENGTH_LONG).show()
        }
    }
}