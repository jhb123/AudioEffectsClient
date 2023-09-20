package com.jhb.audioeffectsclient.network

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import audio.items.Audio
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder


const val TAG = "AudioStreamer"


object AudioStreamer  {

    // This object is for starting, configuring, sending, and stopping the stream of
    // audio data to the server.
    // Having this be an object might not be the nicest way to implement this feature.
    // Since its a 1 screen app and there's only one audio streamer, it is convenient.

    private val socket = DatagramSocket()
    private val isStreaming = MutableStateFlow(false)

    fun endStream(){
        isStreaming.update { false }
    }

    suspend fun scanIpAddresses() : Flow<String> = flow {

        Log.i(TAG, "Starting Scan")
        val subnet = "192.168.0."

        for (i in 0..255) {
            val address = subnet + i
            if (InetAddress.getByName(address).isReachable(50)) {
                val socketAddress = InetSocketAddress(address, 43442)
                try {
                    Log.i(TAG, "Found server at $address")
                    emit(socketAddress.hostName)
                } catch (e: IOException) {
                    Log.i(TAG, "$address has no valid server")
                }
            }
        }
        Log.i(TAG, "Finished Scanning network")

    }.flowOn(Dispatchers.IO)

    suspend fun connectToServer(address: String){
        val socketAddress = InetSocketAddress(address, 43442)
        try {

            withContext(Dispatchers.IO) {
                socket.connect(socketAddress)
            }
        }
        catch(e: SocketException) {
            Log.w(TAG,"Failed to connect to socket: $e")
        }
    }

    suspend fun sendConfig() {
        val cfgMsg = makeConfigMessage().toByteArray()
        try {
            withContext(Dispatchers.IO) {
                socket.send(DatagramPacket(cfgMsg, cfgMsg.size))
            }
        }
        catch(e: IOException) {
            Log.w(TAG,"Failed to send configuration: $e")
        }
    }

    suspend fun startStream(audioRecord: AudioRecord) : Flow<FloatArray> = flow {
        isStreaming.update { true }

        audioRecord.startRecording()

        val buffer = FloatArray(128)

        while (isStreaming.value) {
            audioRecord.read(buffer, 0, 128, AudioRecord.READ_BLOCKING)
            val msg = makeDataMessage(buffer)//.writeTo(outputStream)
            val bytesMsg = msg.toByteArray()
            socket.send(DatagramPacket(bytesMsg, 0, bytesMsg.size))
            delay(1) //Is the server too slow?
            emit(buffer)
        }
        Log.i(TAG,"Sending termination message")
        val termMsg = makeTerminationMessage().toByteArray()
        audioRecord.stop()
        socket.send(DatagramPacket(termMsg, 0, termMsg.size))
    }.flowOn(Dispatchers.IO)


    private fun makeConfigMessage() : Audio.config {

        val cfg = Audio.config.newBuilder()
            .setChannels(1)
            .setEncoding(Audio.Encoding.F32)
            .setSampleRate(44100)
            .setEndian(Audio.Endian.Little) // Android is always little endian
            .build()
        Log.i(com.jhb.audioeffectsclient.ui.audio.TAG,"Channels: ${cfg.channels}, encoding: ${cfg.encoding}, sample rate: ${cfg.sampleRate}")
        Log.i(com.jhb.audioeffectsclient.ui.audio.TAG, "${cfg.toByteArray().toList()}")
        return cfg
    }

    private fun makeDataMessage(data: FloatArray): Audio.data  {
        // this assumes that the data will always be float. This would be nice to
        // make into a library function probably, and make it work with all data!
        val byteArray = ByteArray(4*data.size)
        ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(data)

        val msgData = ByteString.copyFrom(byteArray)
        val msg = Audio.data.newBuilder().apply {
            messageData = msgData
            terminateConnection = false
        }.build()
        return msg
    }

    private fun makeTerminationMessage(): Audio.data {
        val msg = Audio.data.newBuilder().apply {
            terminateConnection = true
        }.build()
        return msg
    }

}