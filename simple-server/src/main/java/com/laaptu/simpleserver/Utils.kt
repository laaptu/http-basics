package com.laaptu.simpleserver

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

fun Activity.hideKeyboard(view: View) {
    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun getDeviceIPAddress(): String {
    NetworkInterface.getNetworkInterfaces()?.iterator()?.forEach { networkInterface: NetworkInterface ->
        networkInterface.inetAddresses?.iterator()?.forEach { inetAddress: InetAddress ->
            if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address)
                return inetAddress.hostAddress
        }
    }
    return "IP Can't Be Found"
}