package com.example.background_location_tracking

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.background_location_tracking.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.statusBarColor = Color.TRANSPARENT
        this.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Handler().postDelayed({
            // Start the main activity
            val mainIntent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, 500)

    }

}