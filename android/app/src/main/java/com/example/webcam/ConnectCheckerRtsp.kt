package com.example.webcam

import com.pedro.rtsp.utils.ConnectCheckerRtsp

/**
 * Common interface for RTSP connection status.
 */
interface ConnectCheckerRtsp : ConnectCheckerRtsp {
    override fun onConnectionSuccessRtsp()
    override fun onConnectionFailedRtsp(reason: String)
    override fun onNewBitrateRtsp(bitrate: Long)
    override fun onDisconnectRtsp()
    override fun onAuthErrorRtsp()
    override fun onAuthSuccessRtsp()
}
