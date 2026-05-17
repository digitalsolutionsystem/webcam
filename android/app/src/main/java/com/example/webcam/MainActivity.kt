package com.example.webcam

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.view.SurfaceHolder
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtsp.utils.ConstructorParameters
import com.pedro.rtsp.rtsp.RtspClient
import com.pedro.rtspserver.RtspServerCamera2
import com.pedro.encoder.input.video.CameraHelper
import java.util.*

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, ConnectCheckerRtsp {

    private lateinit var rtspServerCamera2: RtspServerCamera2
    private lateinit var surfaceView: SurfaceView
    private lateinit var bStartStop: Button
    private lateinit var bSwitchCamera: Button
    private lateinit var tvUrl: TextView
    private lateinit var tvStatus: TextView
    private lateinit var spResolution: Spinner
    
    private val port = 8554
    private val endpoint = "live"
    
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        surfaceView = findViewById(R.id.surfaceView)
        bStartStop = findViewById(R.id.b_start_stop)
        bSwitchCamera = findViewById(R.id.b_switch_camera)
        tvUrl = findViewById(R.id.tv_url)
        tvStatus = findViewById(R.id.tv_status)
        spResolution = findViewById(R.id.sp_resolution)

        rtspServerCamera2 = RtspServerCamera2(surfaceView, this, port)
        surfaceView.holder.addCallback(this)

        bStartStop.setOnClickListener {
            if (!rtspServerCamera2.isStreaming) {
                if (checkPermissions()) {
                    startStream()
                } else {
                    requestPermissions()
                }
            } else {
                stopStream()
            }
        }

        bSwitchCamera.setOnClickListener {
            try {
                rtspServerCamera2.switchCamera()
            } catch (e: CameraOpenException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        setupResolutionSpinner()
        updateUrlDisplay()
    }

    private fun setupResolutionSpinner() {
        val resolutions = arrayOf("720p (1280x720)", "1080p (1920x1080)", "480p (640x480)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, resolutions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spResolution.adapter = adapter
    }

    private fun startStream() {
        val resolution = spResolution.selectedItem.toString()
        val width = if (resolution.contains("1080p")) 1920 else if (resolution.contains("720p")) 1280 else 640
        val height = if (resolution.contains("1080p")) 1080 else if (resolution.contains("720p")) 720 else 480

        tvStatus.text = "Status: Connecting..."
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_light))

        if (rtspServerCamera2.prepareVideo(width, height, 30, 4000 * 1024, CameraHelper.getCameraOrientation(this))) {
            rtspServerCamera2.prepareAudio()
            rtspServerCamera2.startStream(endpoint)
            bStartStop.text = "STOP STREAM"
        } else {
            Toast.makeText(this, "Error preparing stream", Toast.LENGTH_SHORT).show()
            tvStatus.text = "Status: Error"
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }
    }

    private fun stopStream() {
        rtspServerCamera2.stopStream()
        bStartStop.text = "START STREAM"
        tvStatus.text = "Status: Idle"
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
    }

    private fun updateUrlDisplay() {
        val ip = getIpAddress()
        tvUrl.text = "rtsp://$ip:$port/$endpoint"
    }

    private fun getIpAddress(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                // Prioritize rndis0 (USB Tethering) and eth0
                if (iface.name.contains("rndis") || iface.name.contains("eth") || iface.name.contains("wlan")) {
                    val addresses = iface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val addr = addresses.nextElement()
                        if (!addr.isLoopbackAddress && addr is java.net.InetAddress && addr.hostAddress.contains(".")) {
                            return addr.hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "0.0.0.0"
    }

    private fun checkPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, 1)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Nothing to do here
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        rtspServerCamera2.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (rtspServerCamera2.isStreaming) {
            rtspServerCamera2.stopStream()
        }
        rtspServerCamera2.stopPreview()
    }

    // ConnectCheckerRtsp implementation
    override fun onConnectionSuccessRtsp() {
        runOnUiThread { 
            Toast.makeText(this, "Connection Success", Toast.LENGTH_SHORT).show() 
            tvStatus.text = "Status: Streaming"
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        }
    }

    override fun onConnectionFailedRtsp(reason: String) {
        runOnUiThread {
            Toast.makeText(this, "Connection Failed: $reason", Toast.LENGTH_SHORT).show()
            tvStatus.text = "Status: Connection Error"
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            stopStream()
        }
    }

    override fun onNewBitrateRtsp(bitrate: Long) {}
    override fun onDisconnectRtsp() {
        runOnUiThread { 
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show() 
            tvStatus.text = "Status: Disconnected"
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
        }
    }
    override fun onAuthErrorRtsp() {}
    override fun onAuthSuccessRtsp() {}
}
