package com.jhb.audioeffectsclient.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.remember
import androidx.core.app.NotificationCompat
import com.jhb.audioeffectsclient.R
import com.jhb.audioeffectsclient.network.AudioStreamer

class AudioService : Service() {


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
//        audioStreamer.startStream()
        val notification = NotificationCompat.Builder(this, "audioStream")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Streaming audio")
            .setContentText("Enjoy!")
            .build()
        startForeground(1, notification)
    }

    enum class Actions {
        START, STOP
    }

}