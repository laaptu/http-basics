package com.laaptu.socket.channel

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.io.*
import java.lang.Exception
import java.net.InetAddress
import java.net.Socket

class Client(private val ipAddress: String, private val serverPortNumber: Int) : SocketRule {

    private var clientSocket: Socket? = null
    private var outputWriter: PrintWriter? = null
    private var inputReader: BufferedReader? = null
    private var messageListener: MessageListener? = null
    private val runningJob = Job()
    private var isRunning = true

    override fun start() {
        launch(CommonPool, parent = runningJob) {
            val serverAddress = InetAddress.getByName(ipAddress)
            clientSocket = Socket(serverAddress, serverPortNumber)
            try {
                outputWriter = PrintWriter(BufferedWriter(OutputStreamWriter(clientSocket?.getOutputStream())), true)
                inputReader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))
                Timber.d("Connected to the server at %s", ipAddress)
                sendInfo("Connected to the server at $ipAddress")
                while (isRunning) {
                    inputReader?.let {
                        val inputMessage = it.readLine()
                        if (!inputMessage.isNullOrEmpty())
                            broadCastIncomingMessage("( @ $ipAddress ) $inputMessage")
                    }
                }

            } catch (exception: Exception) {
                Timber.e("Error connecting to the server at %s due to : %s",
                        ipAddress, exception.message)
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
            clientSocket?.close()
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
        return SocketType.Client
    }

}