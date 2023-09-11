package com.jhb.audioeffectsclient.ui.main

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.ArrayDeque
import kotlin.math.max

const val TAG = "MainScreenViewModel"

class MainScreenViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState : StateFlow<MainScreenUiState> = _uiState

    private val deque = ArrayDeque(List(10) { 0.0 })


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

                while (uiState.value.record) {
                    audioRecord.read(buffer,0,128,AudioRecord.READ_BLOCKING)
                    i += 1
                    if (i%10 == 0){
                        val soundLevel = buffer.max()
                        deque.addFirst(soundLevel.toDouble())
                        deque.removeLast()

                        //Log.i(TAG,"Sound level: $soundLevel")
                        _uiState.update {
                            it.copy(
                                level = deque.average(),
                                maxLevel = max(soundLevel.toDouble(),it.maxLevel)
                            )
                        }
                    }
                }
            }
        }

        Log.i(TAG,"Finished modifying the recording state")
        return 1
    }




}