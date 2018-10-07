package com.laaptu.socket.channel

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.io.*
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket

class Server(var portNumber: Int) : SocketRule {
    init {
        //https://www.webopedia.com/quick_ref/portnumbers.asp
        if (portNumber < 1025)
            portNumber = 8080
    }

    private val serverSocket = ServerSocket(portNumber)
    private var clientSocket: Socket? = null
    private var outputWriter: PrintWriter? = null
    private var inputReader: InputStream? = null
    private val readBuffer: ByteArray = ByteArray(1024)
    private var messageListener: MessageListener? = null

    private val runningJob = Job()
    private var isRunning = true
    private var clientIPAddress = ""

    override fun start() {
        launch(CommonPool, parent = runningJob) {
            startServer()
        }
    }

    private fun startServer() {
        while (isRunning) {
            try {
                var connectedSocket = serverSocket.accept()
                readFromSocket(connectedSocket)
                Timber.d("Connected to the client at %s", connectedSocket.inetAddress.hostAddress)
                sendInfo("Connected to the client at ${connectedSocket.inetAddress.hostAddress}")
            } catch (exception: Exception) {
                Timber.e("Error accepting to the socket due to : %s", exception.message)
            }
        }
    }


    private fun readFromSocket(connectedSocket: Socket) {
        clientSocket = connectedSocket
        clientIPAddress = connectedSocket.inetAddress.hostAddress
        launch(CommonPool, parent = runningJob) {
            try {
                outputWriter = PrintWriter(BufferedWriter(OutputStreamWriter(connectedSocket.getOutputStream())), true)
                inputReader = connectedSocket.getInputStream()

                while (isRunning) {
                    Timber.d("Server running at %s", System.currentTimeMillis().toString())
                    inputReader?.let {
                        var bytes = it.read(readBuffer)
                        val inputMessage = String(readBuffer, 0, bytes)
                        if (!inputMessage.isEmpty())
                            broadCastIncomingMessage("( @ $clientIPAddress )  $inputMessage")
                    }
                }
            } catch (exception: Exception) {
                Timber.e("Error creating the input and output stream due to: %s", exception.message)
            }
        }
    }


    private fun broadCastIncomingMessage(message: String) {
        messageListener?.let { msgListener ->
            launch(UI) {
                msgListener.onMessageReceived(message)
            }
        }
    }

    override fun stop() {
        isRunning = false
        messageListener = null
        runningJob.cancel()
        try {
            serverSocket.close()
            outputWriter?.close()
            inputReader?.close()
            clientSocket?.close()
        } catch (exception: Exception) {

        }
    }

    override fun sendMessage(message: String) {
        launch(CommonPool, parent = runningJob) {
            outputWriter?.apply {
                println(message)
            }
        }
    }

    private fun sendInfo(info: String) {
        messageListener?.let { msgListener ->
            launch(UI) {
                msgListener.onInfoReceived(info)
            }
        }
    }

    override fun subscribeToMessage(messageListener: MessageListener) {
        this.messageListener = messageListener
    }

    override fun getType(): String {
        return SocketType.Server
    }

}