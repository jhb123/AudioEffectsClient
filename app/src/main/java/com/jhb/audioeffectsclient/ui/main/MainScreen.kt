package com.jhb.audioeffectsclient.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.jhb.audioeffectsclient.ui.permission.PermissionScreen
import com.jhb.audioeffectsclient.ui.theme.AudioEffectsClientTheme
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min


@Composable
fun Main(){

    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val uiState by mainScreenViewModel.uiState.collectAsState()

    MainScreen(
        uiState= uiState,
        toggleRecord = { },
        toggleProcessAudio = {mainScreenViewModel.toggleProcessAudio(it)},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainScreenUiState,
    toggleRecord: ()->Unit,
    toggleProcessAudio: (AudioRecord)->Int,
){
    AudioEffectsClientTheme {
        Scaffold(topBar = { TopBar() }) { paddingValues->
            ScreenContent(
                uiState = uiState,
                toggleRecord = toggleRecord,
                toggleProcessAudio = toggleProcessAudio,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScreenContent(
    uiState:MainScreenUiState,
    toggleRecord: ()->Unit,
    toggleProcessAudio: (AudioRecord)->Int,
    modifier: Modifier = Modifier){

    val recordAudioPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )

    if (recordAudioPermissionState.status.isGranted) {
        AudioMonitor(
            uiState= uiState,
            toggleRecord = toggleRecord,
            toggleProcessAudio = toggleProcessAudio
        )
    } else {
        PermissionScreen(
            showRational = recordAudioPermissionState.status.shouldShowRationale,
            requestPermission = { recordAudioPermissionState.launchPermissionRequest() }
        )
    }


}

@Composable
fun AudioMonitor(
    uiState: MainScreenUiState,
    toggleRecord : ()->Unit,
    toggleProcessAudio: (AudioRecord)->Int,
    modifier: Modifier = Modifier){
    val context = LocalContext.current

    val recorder = if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    else {
        //val mainScreenViewModel: MainScreenViewModel = viewModel()

        val audioRecord = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build()
            )
            .setBufferSizeInBytes(128)
            .build()

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            Button(onClick = {
                if (uiState.record){
                    //toggleRecord(audioRecord)
                    //audioRecord.stop()
                    toggleProcessAudio(audioRecord)
                } else {
                    //toggleRecord(audioRecord)
                    //audioRecord.startRecording()
                    toggleProcessAudio(audioRecord)
                }
                 }) {
                Text(text = if (uiState.record) "Stop" else "Start")
            }

            Text(text = "Level: ${uiState.level}")


            Text(text = "Max level: ${uiState.maxLevel}")
            Box(modifier = Modifier.height(400.dp)) {
                audioMeter(uiState.level,uiState.maxLevel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(){
    TopAppBar(title = { Text(text = "Audio Effects Client") })
}

@Composable
fun audioMeter(level:Double, maxlevel:Double){
    Log.i(TAG,"level $level")
    val db = if (level > 0.0) 20 * log( 1/level,10.0) else 0.001
    Log.i(TAG,"decibel $db")
    Column(verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
        .fillMaxSize()) {
        Box(modifier = Modifier
            .fillMaxHeight(min(max(0.001, (1-db/100)),1.0).toFloat()).fillMaxWidth(1f)
            .background(Color.Red)
        )
    }
//    Box(
//
//    ) {
//    }
}