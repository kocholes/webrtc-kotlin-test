package com.ggarro.kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.ggarro.kotlin.orientation.BaseOrientationManager
import com.ggarro.kotlin.orientation.OrientationManagerFactory
import org.webrtc.*
import org.webrtc.PeerConnection.*
import org.webrtc.PeerConnection.Observer
import org.webrtc.PeerConnectionFactory.InitializationOptions
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity() {
    private var orientationManager: BaseOrientationManager? = null;

    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null
    private var signalingClient: SignalingClient? = null
    private var sdpObserver: SdpObserver? = null

    private var pingStartTimestamp: Long = 0L

    // UI Elements
    private var createOfferBtn: Button? = null
    private var connectionIdInput: EditText? = null
    private var messageHistoryLbl: TextView? = null
    private var messageInput: EditText? = null
    private var sendBtn: Button? = null
    private var sendPingBtn: Button? = null
    private var sendRollBtn: Button? = null
    private var connectBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sensor initialization
        orientationManager = OrientationManagerFactory.getManager(OrientationManagerFactory.ROTATION_VECTOR, applicationContext)

        // Create the peer connection
        peerConnection = createPeerConnection()

        // Session description observer
        sdpObserver = createSessionDescriptionObserver()

        // UI Elements
        connectionIdInput = findViewById(R.id.connectionIdInput)
        messageHistoryLbl = findViewById(R.id.messageHistoryLbl)
        messageInput = findViewById(R.id.messageInput)

        // Handle create offer click
        createOfferBtn = findViewById(R.id.createOfferBtn)
        createOfferBtn?.setOnClickListener {
            dataChannel = createDataChannel()
            createOffer()
        }

        connectBtn = findViewById(R.id.connectBtn)
        connectBtn?.setOnClickListener {
            createAnswer(connectionIdInput?.text.toString())
        }

        // Handle send message click
        sendBtn = findViewById((R.id.sendBtn))
        sendBtn?.setOnClickListener {
            val message = messageInput?.text.toString()
            if (message != "") {
                sendMessage("msg|${message}")
                messageHistoryLbl?.setText(messageHistoryLbl?.text.toString() + "Local: $message \n")
                messageInput?.setText("")
            }
        }

        // Handle send ping click
        sendPingBtn = findViewById((R.id.sendPingBtn))
        sendPingBtn?.setOnClickListener {
            pingStartTimestamp = Date().time
            sendMessage("ping|request")
        }

        // Handle send roll click
        sendRollBtn = findViewById((R.id.sendRollBtn))
        sendRollBtn?.setOnClickListener {
            orientationManager?.initListener {
                sendMessage("rot|${it}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        orientationManager?.stopListener()
    }

    private fun createPeerConnection(): PeerConnection? {
        val factory = createPeerConnectionFactory()
        val config = getPeerConnectionConfig()

        return factory
            .createPeerConnection(config, object : Observer {
                override fun onSignalingChange(signalingState: SignalingState) {}
                override fun onIceConnectionChange(iceConnectionState: IceConnectionState) {}
                override fun onIceConnectionReceivingChange(b: Boolean) {}
                override fun onIceGatheringChange(iceGatheringState: IceGatheringState) {}
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    signalingClient?.addLocalIceCandidate(iceCandidate)
                }

                override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {}
                override fun onAddStream(mediaStream: MediaStream) {}
                override fun onRemoveStream(mediaStream: MediaStream) {}
                override fun onDataChannel(dataChannel: DataChannel) {
                    this@MainActivity.dataChannel = dataChannel
                }

                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(
                    rtpReceiver: RtpReceiver,
                    mediaStreams: Array<MediaStream>
                ) {
                }
            })
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        val options = InitializationOptions
            .builder(applicationContext)
            // add option here
            //.setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        return PeerConnectionFactory.builder()
            // add options here
            //.setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            //.setOptions(PeerConnectionFactory.Options().apply {})
            .createPeerConnectionFactory()
    }

    private fun getPeerConnectionConfig(): RTCConfiguration {
        val iceServers = listOf(
            IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
        )
        val config = RTCConfiguration(iceServers)
        config.iceCandidatePoolSize = 10

        return config
    }

    private fun createSessionDescriptionObserver(): SdpObserver {
        val sdpObserver = object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(sdpObserver, sessionDescription)

                signalingClient?.setLocalDescription(sessionDescription)
                signalingClient?.onRemoteDescription { remoteDescription ->
                    peerConnection?.setRemoteDescription(sdpObserver, remoteDescription)
                }
                signalingClient?.onRemoteIceCandidate { remoteCandidate ->
                    peerConnection?.addIceCandidate(remoteCandidate)
                }

                Log.i("SdpObserver", "Connection ID: ${signalingClient?.getConnectionId()}")
                this@MainActivity.runOnUiThread {
                    connectionIdInput?.setText(signalingClient?.getConnectionId())
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(s: String?) {}
            override fun onSetFailure(s: String?) {}
        }

        return sdpObserver
    }

    private fun createOffer() {
        // Instance the signaling client
        signalingClient = SignalingClient()

        val mediaConstraints = MediaConstraints()
        peerConnection?.createOffer(sdpObserver, mediaConstraints)
    }

    private fun createAnswer(connectionId: String) {
        // Instance the signaling client
        signalingClient = SignalingClient(connectionId)

        val mediaConstraints = MediaConstraints()
        peerConnection?.createAnswer(sdpObserver, mediaConstraints)
    }

    private fun createDataChannel(): DataChannel? {
        val dataChannelInit = DataChannel.Init()
        // set options here
//        dataChannelInit.maxRetransmitTimeMs
        dataChannelInit.ordered = true
//        dataChannelInit.maxRetransmits = 0

        val dataChannel = peerConnection?.createDataChannel("sendData", dataChannelInit)
        dataChannel?.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(l: Long) {}
            override fun onStateChange() {
                Log.i("DCObserver", "DataChannel status: ${dataChannel.state()}")
            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                val data = buffer.data
                val bytes = ByteArray(data.remaining())
                data[bytes]
                val message = String(bytes)
                Log.i("DcObserver", "New message: $message")
                val msgParts = message.split("|")
                val msgType = msgParts[0]
                val msgData = msgParts[1]

                when (msgType) {
                    "msg" -> {
                        this@MainActivity.runOnUiThread {
                            messageHistoryLbl?.setText(messageHistoryLbl?.text.toString() + "Remote: $msgData \n")
                        }
                    }
                    "ping" -> {
                        when (msgData) {
                            "request" -> sendMessage("ping|response")
                            "response" -> {
                                this@MainActivity.runOnUiThread {
                                    messageHistoryLbl?.setText("Propagation relay: ${(Date().time - pingStartTimestamp) / 2}")
                                }
                            }
                        }
                    }
                }
            }
        })

        return dataChannel
    }

    private fun sendMessage(message: String) {
        val byteBuffer = ByteBuffer.wrap(message.toByteArray(Charset.defaultCharset()))
        val buffer = DataChannel.Buffer(byteBuffer, false)
        dataChannel?.send(buffer)
    }

}