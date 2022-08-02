package com.example.gittutorial

import android.os.Build

inline fun <T> sdk29AndUp(onSdk29:()->T):T?{
    return if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.Q){
        onSdk29()
    }else null
}

fun isSDK30AndUp() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

fun isSDK29() = Build.VERSION.SDK_INT == Build.VERSION_CODES.Q