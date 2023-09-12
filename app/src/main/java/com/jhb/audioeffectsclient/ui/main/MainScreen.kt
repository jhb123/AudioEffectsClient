package com.jhb.audioeffectsclient.ui.main

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.jhb.audioeffectsclient.ui.audio.AudioScreen
import com.jhb.audioeffectsclient.ui.permission.PermissionScreen
import com.jhb.audioeffectsclient.ui.theme.AudioEffectsClientTheme



@Composable
fun Main(){

    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val uiState by mainScreenViewModel.uiState.collectAsState()

    MainScreen(uiState= uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainScreenUiState,
){
    AudioEffectsClientTheme {
        Scaffold(topBar = { TopBar() }) { paddingValues->
            ScreenContent(modifier = Modifier.padding(paddingValues))
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScreenContent(
    modifier: Modifier = Modifier){

    val recordAudioPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )

    val internetPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )
    Box(modifier=modifier) {
        if (recordAudioPermissionState.status.isGranted and internetPermissionState.status.isGranted) {
            AudioScreen()
        } else {
            PermissionScreen(
                audioShowRational = recordAudioPermissionState.status.shouldShowRationale,
                internetShowRational = recordAudioPermissionState.status.shouldShowRationale,
                requestPermission = {
                    recordAudioPermissionState.launchPermissionRequest()
                    internetPermissionState.launchPermissionRequest()
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(){
    TopAppBar(title = { Text(text = "Audio Effects Client") })
}

