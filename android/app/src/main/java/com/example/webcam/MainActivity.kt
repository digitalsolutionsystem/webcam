package com.example.webcam

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.net.NetworkInterface

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var bStartStop: Button
    private lateinit var bSwitchCamera: Button
    private lateinit var tvUrl: TextView
    private lateinit var tvStatus: TextView
    private lateinit var spResolution: Spinner

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var mjpegServer: MjpegServer? = null
    private var isStreaming = false
    private var currentCameraIndex = 0

    private val port = 8080

    private val permissions = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView  = findViewById(R.id.surfaceView)
        bStartStop   = findViewById(R.id.b_start_stop)
        bSwitchCamera= findViewById(R.id.b_switch_camera)
        tvUrl        = findViewById(R.id.tv_url)
        tvStatus     = findViewById(R.id.tv_status)
        spResolution = findViewById(R.id.sp_resolution)

        surfaceView.holder.addCallback(this)
        setupResolutionSpinner()
        updateUrlDisplay()

        bStartStop.setOnClickListener {
            if (!isStreaming) {
                if (checkPermissions()) startStream() else requestPerms()
            } else {
                stopStream()
            }
        }

        bSwitchCamera.setOnClickListener {
            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val count = manager.cameraIdList.size
            if (count > 1) {
                currentCameraIndex = (currentCameraIndex + 1) % count
                if (isStreaming) { stopStream(); startStream() }
            } else {
                Toast.makeText(this, "Hanya ada 1 kamera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupResolutionSpinner() {
        val resolutions = arrayOf("720p (1280x720)", "480p (640x480)", "1080p (1920x1080)")
        spResolution.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, resolutions).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try { backgroundThread?.join() } catch (e: InterruptedException) { e.printStackTrace() }
        backgroundThread = null
        backgroundHandler = null
    }

    private fun startStream() {
        startBackgroundThread()

        val res = spResolution.selectedItem.toString()
        val width  = when { res.contains("1080p") -> 1920; res.contains("720p") -> 1280; else -> 640 }
        val height = when { res.contains("1080p") -> 1080; res.contains("720p") -> 720;  else -> 480 }

        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = manager.cameraIdList
        if (cameraIds.isEmpty()) {
            showStatus("Tidak ada kamera", android.R.color.holo_red_dark)
            return
        }
        val cameraId = cameraIds[minOf(currentCameraIndex, cameraIds.size - 1)]

        // Setup ImageReader untuk ambil frame JPEG
        imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2)
        imageReader!!.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            try {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                mjpegServer?.pushFrame(bytes)
            } finally {
                image.close()
            }
        }, backgroundHandler)

        // Start MJPEG server
        mjpegServer = MjpegServer(port)
        mjpegServer!!.start()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) return

        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera

                val surfaces = mutableListOf<Surface>(imageReader!!.surface)
                val previewSurface = surfaceView.holder.surface
                if (previewSurface != null) surfaces.add(previewSurface)

                camera.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                            surfaces.forEach { addTarget(it) }
                        }.build()
                        session.setRepeatingRequest(request, null, backgroundHandler)
                        isStreaming = true
                        runOnUiThread {
                            bStartStop.text = "STOP STREAM"
                            showStatus("Streaming aktif ✓", android.R.color.holo_green_dark)
                            updateUrlDisplay()
                        }
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        runOnUiThread { showStatus("Gagal konfigurasi kamera", android.R.color.holo_red_dark) }
                    }
                }, backgroundHandler)
            }
            override fun onDisconnected(camera: CameraDevice) { camera.close() }
            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                runOnUiThread { showStatus("Error kamera: $error", android.R.color.holo_red_dark) }
            }
        }, backgroundHandler)
    }

    private fun stopStream() {
        captureSession?.close(); captureSession = null
        cameraDevice?.close();   cameraDevice = null
        imageReader?.close();    imageReader = null
        mjpegServer?.stop();     mjpegServer = null
        stopBackgroundThread()
        isStreaming = false
        runOnUiThread {
            bStartStop.text = "START STREAM"
            showStatus("Idle", android.R.color.darker_gray)
        }
    }

    private fun showStatus(msg: String, colorRes: Int) {
        tvStatus.text = "Status: $msg"
        tvStatus.setTextColor(ContextCompat.getColor(this, colorRes))
    }

    private fun updateUrlDisplay() {
        tvUrl.text = "http://${getIpAddress()}:$port/"
    }

    private fun getIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.name.startsWith("rndis") || iface.name.startsWith("eth") || iface.name.startsWith("wlan")) {
                    val addrs = iface.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        if (!addr.isLoopbackAddress && addr.hostAddress.contains('.'))
                            return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return "0.0.0.0"
    }

    private fun checkPermissions() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPerms() = ActivityCompat.requestPermissions(this, permissions, 1)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            startStream()
        else
            Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) { if (isStreaming) stopStream() }

    override fun onDestroy() {
        super.onDestroy()
        if (isStreaming) stopStream()
    }
}
