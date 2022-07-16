package com.example.gittutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println("this is contained in my second commit")
        println("local change")
        println("CHANGE second")
        println("these changes will marge from experimantl to main branch")
    }
}