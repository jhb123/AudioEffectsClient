package com.jhb.audioeffectsclient.ui.audio

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview

const val TAG = "AudioScreen"
@Composable
fun AudioScreen(){

    val audioScreenViewModel: AudioScreenViewModel = viewModel()
    val uiState by audioScreenViewModel.uiState.collectAsState()

    AudioScreenComposable(
        uiState = uiState,
        scanIps = {audioScreenViewModel.scanIpAddresses()},
        toggleProcessAudio = {audioScreenViewModel.toggleProcessAudio(it)}
    )

}

@Composable
fun AudioScreenComposable(
    uiState: AudioScreenUiState,
    scanIps: ()->Unit,
    toggleProcessAudio: (AudioRecord)->Int){
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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            IpScanner(
                ableToScan = !uiState.isScanning,
                devicesFound = uiState.addresses,
                scan = scanIps,
                connect = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )

            WaveForm(
                modifier = Modifier.height(400.dp),
                yPoints = uiState.audioWaveForm,
                maxY = 0.5f
            )
            Button(
                onClick = {toggleProcessAudio(audioRecord)},
                modifier = Modifier.padding(10.dp)
            ) {
                Text(text = if (uiState.record) "Stop" else "Start")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IpScanner(
    ableToScan: Boolean,
    devicesFound: List<String>?,
    scan: ()->Unit,
    connect: ()->Unit,
    modifier: Modifier = Modifier
){

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.heightIn(min= 50.dp)
    ) {
        if (devicesFound.isNullOrEmpty() and ableToScan) {
            OutlinedButton(
                onClick = scan,
                enabled = ableToScan,
            ) {
                Text(text = "Scan")
                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
            }
        }
        else {
            IconButton(
                onClick = scan,
                enabled = ableToScan,
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
            }
        }
        FlowRow() {
            devicesFound?.let{ list->
                list.forEach {
                    AssistChip(
                        onClick = connect,
                        label = {Text(text = it)},
                        modifier = Modifier.padding(4.dp,0.dp),
                        //enabled = ableToScan
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewOfIpScanner(){
    IpScanner(
        ableToScan = false,
        devicesFound = null,//listOf("example 1", "dev 2", "dasdasdsadasd 3","a"),
        scan = {},
        connect = {},
        modifier = Modifier.width(480.dp)
    )
}

@Composable
fun WaveForm(
    modifier: Modifier = Modifier,
    yPoints: List<Float>,
    maxY: Float = 1f,
    graphColor: Color = MaterialTheme.colorScheme.primary,
) {

    Box(modifier = modifier.clipToBounds()) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
        ) {

            val xSpace = (size.width) / (yPoints.size - 1)
            // flip so high value is up. Its between +/- maxY, so divide by 2 and maxY
            val yNorm = -1 * size.height / maxY / 2
            // shift down the screen
            val yOffset = size.height / 2


            val positiveStrokePath = Path().apply {

                for (i in yPoints.indices) {

                    val currentX = i * xSpace

                    if (i == 0) {

                        moveTo(currentX, yPoints[i] * yNorm + yOffset)
                    } else {

                        val previousX = (i - 1) * xSpace

                        val conX1 = (previousX + currentX) / 2f
                        val conX2 = (previousX + currentX) / 2f

                        val conY1 = yPoints[i - 1] * yNorm + yOffset
                        val conY2 = yPoints[i] * yNorm + yOffset


                        cubicTo(
                            x1 = conX1,
                            y1 = conY1,
                            x2 = conX2,
                            y2 = conY2,
                            x3 = currentX,
                            y3 = yPoints[i] * yNorm + yOffset
                        )
                    }
                }
            }


            val negativeStrokePath = Path().apply {

                for (i in yPoints.indices) {

                    val currentX = i * xSpace

                    if (i == 0) {

                        moveTo(currentX, -1 * yPoints[i] * yNorm + yOffset)
                    } else {

                        val previousX = (i - 1) * xSpace

                        val conX1 = (previousX + currentX) / 2f
                        val conX2 = (previousX + currentX) / 2f

                        val conY1 = -1 * yPoints[i - 1] * yNorm + yOffset
                        val conY2 = -1 * yPoints[i] * yNorm + yOffset


                        cubicTo(
                            x1 = conX1,
                            y1 = conY1,
                            x2 = conX2,
                            y2 = conY2,
                            x3 = currentX,
                            y3 = -1 * yPoints[i] * yNorm + yOffset
                        )
                    }
                }
            }


            positiveStrokePath.apply {
                addPath(negativeStrokePath)
            }


            drawPath(
                path = positiveStrokePath,
                color = graphColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

//            drawPath(
//                path = negativeStrokePath,
//                color = graphColor,
//                style = Stroke(
//                    width = 3.dp.toPx(),
//                    cap = StrokeCap.Round
//                )
//            )

        }
    }
}


@Preview
@Composable
fun WaveFormPreview(){
    WaveForm(modifier = Modifier.height(100.dp), yPoints = listOf(0.0f,0.2f,0.5f,0.4f,0.9f,0.0f), maxY = 1f)
}