package com.example.shelastudio

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class LaunchActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LaunchActivity"
        private const val LAUNCH_DELAY_MS = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Launch screen starting")
        setContentView(R.layout.activity_launch)

        // After 2 seconds, move to Login screen
        Handler(Looper.getMainLooper()).postDelayed({
            Log.i(TAG, "Launch delay finished, navigating to LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Remove from back stack so back button exits app
        }, LAUNCH_DELAY_MS)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}