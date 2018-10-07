package com.laaptu.simpleserver.channel

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.io.*
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.nio.Buffer
import java.text.SimpleDateFormat
import java.util.*

class Server(var portNumber: Int) : SocketRule {
    init {
        //https://www.webopedia.com/quick_ref/portnumbers.asp
        if (portNumber < 1025)
            portNumber = 8080
    }

    private val serverSocket = ServerSocket(portNumber)
    private val simpleDateFormat = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss ")
    private var messageListener: MessageListener? = null

    private val runningJob = Job()
    private var isRunning = true

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
            } catch (exception: Exception) {
                Timber.e("Error accepting to the socket due to : %s", exception.message)
            }
        }
    }

    private fun readFromSocket(clientSocket: Socket) {
        val clientIPAddress = clientSocket.inetAddress.hostAddress
        Timber.d("Connected to the client at $clientIPAddress")
        sendInfo("Connected to the client at $clientIPAddress")
        launch(CommonPool, parent = runningJob) {
            var runForAWhile = true
            var outputWriter: PrintWriter? = null
            var inputReader: InputStream? = null
            val readBuffer = ByteArray(1024)
            try {
                outputWriter = PrintWriter(BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream())), true)
                inputReader = clientSocket.getInputStream()
                while (runForAWhile) {
                    Timber.d("Server running at %s", System.currentTimeMillis().toString())
                    inputReader?.let {
                        val bytes = it.read(readBuffer)
                        val inputMessage = String(readBuffer, 0, bytes)
                        if (!inputMessage.isEmpty()) {
                            broadCastIncomingMessage("( @ $clientIPAddress )  $inputMessage")
                            outputWriter.println(getResponseMessage(inputMessage))
                            runForAWhile = false
                        }
                    }
                }
            } catch (exception: Exception) {
                Timber.e("Error creating the input and output stream due to: %s", exception.message)
            } finally {
                outputWriter?.close()
                inputReader?.close()
                clientSocket.close()
            }
        }
    }

    override fun sendMessage(message: String) {
    }

    private fun getResponseMessage(incomingMessage: String): String =
            "You sent:\n $incomingMessage \nBUT Me Server can send you the current date only i.e.\n ${getCurrentDateTime()}"


    private fun getCurrentDateTime(): String = simpleDateFormat.format(Calendar.getInstance().time)


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
        } catch (exception: Exception) {

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