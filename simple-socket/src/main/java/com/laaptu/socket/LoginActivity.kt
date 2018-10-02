package com.laaptu.socket

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            validateAndStartSocketCommunication()
        }
        cbClient.setOnCheckedChangeListener { buttonView, isChecked ->
            cbServer.isChecked = !isChecked
            if (isChecked)
                txtLayoutIp.hint = "Enter valid client ip address"
        }
        cbServer.setOnCheckedChangeListener { buttonView, isChecked ->
            cbClient.isChecked = !isChecked
            if (isChecked)
                txtLayoutIp.hint = "Enter valid server ip address"
        }
        txtIpAddress.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                validateAndStartSocketCommunication()
            false
        }
        cbServer.isChecked = true
        txtDeviceIp.text = "DEVICE IP = " + getDeviceIPAddress()
    }

    private fun validateAndStartSocketCommunication(): Boolean {
        val ipAddress = txtIpAddress.text.toString()
        if (!isValidIpAddress(ipAddress)) {
            txtLayoutIp.error = "Please enter a valid IP address"
            return false
        }
        txtLayoutIp.isErrorEnabled = false
        hideKeyboard(txtIpAddress)
        startActivity(CommunicationActivity.getStartIntent(this, cbClient.isChecked, ipAddress))
        finish()
        return true

    }

    private fun isValidIpAddress(ipAddress: String): Boolean {
        val matcher = Patterns.IP_ADDRESS.matcher(ipAddress)
        return matcher.matches()
    }
}
