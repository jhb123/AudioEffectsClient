package com.jhb.audioeffectsclient.ui.audio

import android.media.AudioRecord
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.items.Audio
import com.google.protobuf.ByteString
import com.jhb.audioeffectsclient.network.AudioStreamer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.log
import kotlin.math.max


class AudioScreenViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(AudioScreenUiState())
    val uiState : StateFlow<AudioScreenUiState> = _uiState

    private val deque = ArrayDeque(List(10) { 0.0 })
    private val waveform = ArrayDeque(List(100) { 0f })

    private val audioStreamer = AudioStreamer()

    var outputFromFlow = 0


    fun handleConnection(audioRecord: AudioRecord, address: String){
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
            throwable.printStackTrace()
        }


        _uiState.update {
            it.copy(record = true)
        }
        viewModelScope.launch(Dispatchers.IO+ coroutineExceptionHandler) {
            audioStreamer.connectToServer(address)
            audioStreamer.sendConfig()
            var lastMax = 0f
            var i = 0
            audioStreamer.startStream(audioRecord).collect { buffer->
                i += 1
                if (buffer.max() > lastMax) lastMax = buffer.max()

                if (i%10 == 0){
                    val soundLevel = lastMax //logarithmicSound(buffer.max(),0.5f)
                    lastMax = 0f
                    waveform.addFirst(soundLevel)
                    waveform.removeLast()

                    //Log.i(TAG,"Sound level: $soundLevel")
                    _uiState.update {
                        it.copy(
                            maxLevel = max(soundLevel.toDouble(),it.maxLevel),
                            audioWaveForm = waveform.toList()
                        )
                    }
                }
            }
            Log.i(TAG,"finished streaming")
        }

    }


    fun scanIpAddresses() {

        viewModelScope.launch(Dispatchers.IO) {
            val addresses = mutableStateListOf<String>()
            _uiState.update {
                it.copy(
                    addresses = addresses,
                    isScanning = true
                )
            }
            audioStreamer.scanIpAddresses().collect {newAddress ->
                addresses.add(newAddress)
            }
            _uiState.update {
                it.copy(isScanning = false)
            }
        }
    }


    private fun logarithmicSound(p: Float, p0: Float): Float {
        if( p0 <= 0 ) return 0f
        else if( p <= 0 ) return 0f
        else return 20 * log(p/p0,10f)
    }

    fun stopStream() {
        audioStreamer.endStream()
        _uiState.update {
            it.copy(record = false)
        }
    }

}