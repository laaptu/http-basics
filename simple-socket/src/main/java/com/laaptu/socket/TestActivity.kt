package com.laaptu.socket

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class TestActivity : AppCompatActivity() {
    var isRunning = true
    val runningJobs = Job()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_login)
        startServerListen()
    }

    private fun startServerListen() {
        launch(CommonPool, parent = runningJobs) {
            startInputListen()
            while (isRunning) {
                Timber.d("StartServerListen ####: %s", Thread.currentThread().name)
            }
        }

    }

    private fun startInputListen() {
        launch(CommonPool, parent = runningJobs) {
            startOutputListen()
            while (isRunning) {
                Timber.d("StartInputListen ####: %s", Thread.currentThread().name)
            }
        }

    }

    private fun startOutputListen() {
        launch(CommonPool, parent = runningJobs) {
            while (isRunning) {
                Timber.d("StartOutputListen ####: %s", Thread.currentThread().name)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isRunning = false
        runningJobs.cancel()
    }


}