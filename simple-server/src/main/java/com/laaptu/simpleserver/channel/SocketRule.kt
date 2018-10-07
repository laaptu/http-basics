package com.laaptu.simpleserver.channel

interface SocketRule {
    fun start()
    fun stop()
    fun sendMessage(message: String)
    fun getType(): String
    fun subscribeToMessage(messageListener: MessageListener)
}

interface MessageListener {
    fun onMessageReceived(message: String)
    fun onInfoReceived(info: String)
}

object SocketType {
    const val Server = "Server : "
    const val Client = "Client : "
}