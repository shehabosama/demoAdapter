package com.example.gittutorial

import android.content.Context
import com.squareup.picasso.Cache
import com.squareup.picasso.Picasso
import java.util.concurrent.Executors


class ImageHandler {

    companion object{
        private var instance: Picasso? = null
        fun getSharedInstance(context: Context?): Picasso? {
            return if (instance == null) {
                instance = Picasso.Builder(context!!).executor(Executors.newSingleThreadExecutor())
                    .memoryCache(Cache.NONE).indicatorsEnabled(true).build()
                instance
            } else {
                instance
            }
        }
    }

}