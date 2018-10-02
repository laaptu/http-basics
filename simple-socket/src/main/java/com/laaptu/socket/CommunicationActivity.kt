package com.laaptu.socket

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity

class CommunicationActivity : AppCompatActivity() {

    companion object {
        const val IS_CLIENT = "isClient"
        const val IP_ADDRESS = "ipAddress"
        fun getStartIntent(context: Context, isClient: Boolean, ipAddress: String): Intent {
            val intent = Intent(context, CommunicationActivity::class.java)
            intent.putExtra(IS_CLIENT, isClient)
            intent.putExtra(IP_ADDRESS, ipAddress)
            return intent
        }
    }

}