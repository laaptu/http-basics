package com.laaptu.simpleserver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.laaptu.simpleserver.channel.*
import com.laaptu.socket.R
import kotlinx.android.synthetic.main.activity_communicate.*
import kotlinx.android.synthetic.main.content_communicate.*

class CommunicationActivity : AppCompatActivity(), MessageListener {


    enum class CommunicationType {
        Client, Server
    }

    lateinit var communicationType: CommunicationType
    lateinit var socket: SocketRule
    var serverIpAddress: String = ""
    var serverPortNumber: Int = 0

    lateinit var chatAdapter: ChatAdapter

    companion object {
        const val IS_CLIENT = "isClient"
        const val IP_ADDRESS = "ipAddress"
        const val SERVER_PORT = "serverPort"
        fun getStartIntent(context: Context, isClient: Boolean, ipAddress: String, serverPort: Int): Intent {
            val intent = Intent(context, CommunicationActivity::class.java)
            intent.putExtras(getBundle(isClient, ipAddress, serverPort))
            return intent
        }

        fun getBundle(isClient: Boolean, ipAddress: String, serverPort: Int): Bundle {
            val bundle = Bundle()
            bundle.putString(IP_ADDRESS, ipAddress)
            bundle.putInt(SERVER_PORT, serverPort)
            bundle.putBoolean(IS_CLIENT, isClient)
            return bundle
        }

        fun isClient(bundle: Bundle): Boolean {
            return bundle.getBoolean(IS_CLIENT, false)
        }

        fun getIpAddress(bundle: Bundle): String {
            return bundle.getString(IP_ADDRESS, "")
        }

        fun getServerPort(bundle: Bundle): Int {
            return bundle.getInt(SERVER_PORT, 8088)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communicate)
        val bundle: Bundle = if (intent.extras == null) getBundle(true, "192.168.10.10", 8088) else intent.extras
        getValuesFromBundle(bundle)
        startSocketCommunication()
        updateToolbar(communicationType)
        initViews()

    }

    private fun updateToolbar(communicationType: CommunicationType) {
        var toolbarTitle = ""
        var drawableId = 0
        when (communicationType) {
            CommunicationType.Server -> {
                toolbarTitle = SocketType.Server
                drawableId = R.drawable.ic_server
            }
            CommunicationType.Client -> {
                toolbarTitle = SocketType.Client
                drawableId = R.drawable.ic_client
            }
        }
        toolbar.title = "$toolbarTitle : @ ${getDeviceIPAddress()}"
        toolbar.logo = ContextCompat.getDrawable(this, drawableId)
    }

    private fun initViews() {
        chatAdapter = ChatAdapter(ArrayList())
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvChatList.layoutManager = linearLayoutManager
        rvChatList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvChatList.adapter = chatAdapter

        btnSend.setOnClickListener {
            val message = txtInput.text.toString()
            if (!message.isEmpty()) {
                txtInput.setText("")
                hideKeyboard(txtInput)
                val chatMessage: ChatMessage =
                        constructMessage(message, communicationType != CommunicationType.Server)
                updateUIWithNewMessage(chatMessage)
                socket.sendMessage(message)
            }
        }
    }

    private fun getValuesFromBundle(bundle: Bundle) {
        communicationType = if (isClient(bundle)) CommunicationType.Client else CommunicationType.Server
        serverIpAddress = getIpAddress(bundle)
        serverPortNumber = getServerPort(bundle)
    }


    private fun startSocketCommunication() {
        socket = when (communicationType) {
            CommunicationType.Server -> {
                Server(serverPortNumber)
            }
            CommunicationType.Client -> {
                Client(serverIpAddress, serverPortNumber)
            }
        }
        socket.start()
        socket.subscribeToMessage(this)
    }

    override fun onPause() {
        super.onPause()
        socket.stop()
    }

    override fun onMessageReceived(message: String) {
        val chatMessage: ChatMessage = when (communicationType) {
            CommunicationType.Client -> constructMessage(message, false)
            CommunicationType.Server -> constructMessage(message, true)
        }
        updateUIWithNewMessage(chatMessage)

    }

    override fun onInfoReceived(info: String) {
        Snackbar.make(rvChatList, info, Snackbar.LENGTH_SHORT).show()
    }

    private fun constructMessage(message: String, isClient: Boolean): ChatMessage {
        return when (isClient) {
            true -> ChatMessage(message, R.drawable.chat_bubble_client,
                    R.drawable.ic_client, true)
            else -> ChatMessage(message, R.drawable.chat_bubble_server,
                    R.drawable.ic_server, false)
        }
    }

    private fun updateUIWithNewMessage(chatMessage: ChatMessage) {
        chatAdapter.addItem(chatMessage)
        rvChatList.scrollToPosition(chatAdapter.itemCount - 1)
    }


}