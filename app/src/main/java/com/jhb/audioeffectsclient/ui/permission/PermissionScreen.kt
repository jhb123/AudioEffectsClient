package com.jhb.audioeffectsclient.ui.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PermissionScreen(
    audioShowRational: Boolean,
    internetShowRational: Boolean,
    requestPermission: ()->Unit,
    modifier: Modifier = Modifier

){

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {

        if (internetShowRational and audioShowRational) {
            Text(text = "To use this app, Grant it permission to record audio and access the internet in your phone's settings")
        } else if (audioShowRational) {
            Text(text = "To use this app, Grant it permission to record audio in your phone's settings")
        } else if (internetShowRational) {
            Text(text = "To use this app, Grant it permission to access the internet your phone's settings")
        } else {
            Text(text = "This app needs to record audio and access the internet to function.")
            Button(onClick = requestPermission) {
                Text(text = "Request Permissions")
            }
        }
    }
}