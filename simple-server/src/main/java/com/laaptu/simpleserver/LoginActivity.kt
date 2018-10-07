package com.laaptu.simpleserver

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import com.laaptu.socket.R
import com.laaptu.socket.R.id.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            validateAndStartSocketCommunication()
        }
        cbClient.setOnCheckedChangeListener { _, isChecked ->
            cbServer.isChecked = !isChecked
            if (isChecked)
                txtLayoutIp.visibility = View.VISIBLE
        }
        cbServer.setOnCheckedChangeListener { _, isChecked ->
            cbClient.isChecked = !isChecked
            if (isChecked)
                txtLayoutIp.visibility = View.GONE

        }
        txtPort.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                validateAndStartSocketCommunication()
            false
        }
        cbServer.isChecked = true
        txtDeviceIp.text = "DEVICE IP = " + getDeviceIPAddress()

        //startActivity(Intent(this, TestActivity::class.java))
    }

    private fun validateAndStartSocketCommunication(): Boolean {
        val ipAddress = txtIpAddress.text.toString()
        if (cbClient.isChecked && !isValidIpAddress(ipAddress)) {
            txtLayoutIp.error = "Please enter a valid IP address"
            return false
        }
        txtLayoutIp.isErrorEnabled = false
        val portNumberValue = txtPort.text.toString()
        if (portNumberValue.isEmpty()) {
            txtLayoutPort.error = "Please enter a valid server port"
            return false
        }
        txtLayoutPort.isErrorEnabled = false
        hideKeyboard(txtIpAddress)
        startActivity(CommunicationActivity.getStartIntent(this, cbClient.isChecked, ipAddress, portNumberValue.toInt()))
        finish()
        return true

    }

    private fun isValidIpAddress(ipAddress: String): Boolean {
        val matcher = Patterns.IP_ADDRESS.matcher(ipAddress)
        return matcher.matches()
    }
}
