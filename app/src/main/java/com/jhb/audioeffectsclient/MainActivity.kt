package com.jhb.audioeffectsclient

import android.os.Bundle
import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.jhb.audioeffectsclient.ui.main.Main

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //SIMPLE, improve permission later
        val permissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.RECORD_AUDIO
            )
        }
        else {
            arrayOf(
                Manifest.permission.RECORD_AUDIO
            )
        }
        ActivityCompat.requestPermissions(
            this,
            permissions,
            0
        )
        setContent {
            Main()
        }
    }
}