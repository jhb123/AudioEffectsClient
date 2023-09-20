package com.jhb.audioeffectsclient

import android.os.Bundle
import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.jhb.audioeffectsclient.network.AudioStreamer
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
        //val audioStreamer = com.jhb.audioeffectsclient.network.AudioStreamer()
        setContent {
            Main()
        }
        //Log.i("MainActivity","ending composition")
        //AudioStreamer.endStream()
    }

//    override fun onDestroy() {
//        Log.i("MainActivity","destroying activity")
//        AudioStreamer.endStream()
//        super.onDestroy()
//    }
}