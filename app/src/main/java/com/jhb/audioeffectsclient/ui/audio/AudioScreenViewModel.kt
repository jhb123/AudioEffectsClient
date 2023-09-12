package com.jhb.audioeffectsclient.ui.audio

import android.media.AudioRecord
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhb.audioeffectsclient.ui.main.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.log
import kotlin.math.max

class AudioScreenViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(AudioScreenUiState())
    val uiState : StateFlow<AudioScreenUiState> = _uiState

    private val deque = ArrayDeque(List(10) { 0.0 })
    private val waveform = ArrayDeque(List(100) { 0f })

    fun scanIpAddresses() {

        viewModelScope.launch(Dispatchers.IO) {
            val addresses = mutableListOf<String>()
            _uiState.update {
                it.copy(
                    addresses = addresses,
                    isScanning = true
                )
            }

            Log.i(TAG, "Starting Scan")
            val subnet = "192.168.0."


            for (i in 0..255){
                val address = subnet+i
                //InetSocketAddress.createUnresolved(address,8000).isUnresolved
                if(InetAddress.getByName(address).isReachable(50)){
                    val socket_address = InetSocketAddress(address, 43442)
                    val socket = Socket()
                    try {
                        socket.connect(socket_address,1000)
                        socket.close()
                        addresses.add(InetAddress.getByName(address).hostName)
                        Log.i(TAG, "Found server at $address")
                        addresses.sort()
                        _uiState.update {
                            it.copy(addresses = addresses)
                        }
                    }catch (e: IOException) {
                        Log.i(TAG, "$address has no valid server")
                    }
                }
                else{
                    //Log.i(TAG,"${address} not found")
                }
            }
            _uiState.update {
                it.copy(isScanning = false)
            }
            Log.i(TAG,"Finished Scanning network")
        }
    }


    fun toggleProcessAudio(audioRecord: AudioRecord): Int{

        if (uiState.value.record){
            Log.i(TAG,"Initiating the Stop Recording sequence")
            audioRecord.stop()
            audioRecord.release()
            _uiState.update {
                it.copy(record = false)
            }
        } else {
            Log.i(TAG,"Initiating the Start Recording sequence")
            _uiState.update {
                it.copy(record = true)
            }
            audioRecord.startRecording()

            viewModelScope.launch(Dispatchers.IO) {
                var buffer = FloatArray(128)
                var i = 0

                var lastMax = 0f
                while (uiState.value.record) {
                    audioRecord.read(buffer,0,128, AudioRecord.READ_BLOCKING)
                    i += 1
                    if (buffer.max() > lastMax) lastMax = buffer.max()

                    if (i%10 == 0){
                        val soundLevel = lastMax //logarithmicSound(buffer.max(),0.5f)
                        lastMax = 0f
                        deque.addFirst(soundLevel.toDouble())
                        deque.removeLast()
                        waveform.addFirst(soundLevel)
                        waveform.removeLast()

                        //Log.i(TAG,"Sound level: $soundLevel")
                        _uiState.update {
                            it.copy(
                                level = deque.average(),
                                maxLevel = max(soundLevel.toDouble(),it.maxLevel),
                                audioWaveForm = waveform.toList()
                            )
                        }
                    }
                }
            }
        }

        Log.i(TAG,"Finished modifying the recording state")
        return 1
    }

    private fun logarithmicSound(p: Float, p0: Float): Float {
        if( p0 <= 0 ) return 0f
        else if( p <= 0 ) return 0f
        else return 20 * log(p/p0,10f)
    }

}