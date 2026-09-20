package com.example.shelastudio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.shelastudio.databinding.ActivityMainBinding
import com.example.shelastudio.di.RepositoryProvider

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: MainActivity with bottom nav")

        // Navigation is only wired up once a valid session is confirmed,
        // otherwise fragments would load and immediately fail on Firestore
        // calls that require an authenticated UID.
        if (!ensureSignedIn()) return

        setupBottomNavigation()
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

    /**
     * Guards the main app. If no authenticated session exists the user is
     * returned to the login screen, so every Firestore path below this
     * point is guaranteed a valid UID.
     *
     * @return true if an authenticated session is active, false if the user
     *         was redirected to sign in.
     */
    private fun ensureSignedIn(): Boolean {
        val uid = RepositoryProvider.auth.currentUserId()
        return if (uid == null) {
            Log.w(TAG, "No authenticated session - redirecting to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            false
        } else {
            Log.i(TAG, "Authenticated session active. UID=$uid")
            true
        }
    }

    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        Log.d(TAG, "Bottom nav connected to nav controller")
    }
}