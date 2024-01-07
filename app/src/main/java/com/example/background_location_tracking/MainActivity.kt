package com.example.background_location_tracking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.background_location_tracking.fragment.LocationTrackingFragment
import com.example.background_location_tracking.utils.Constant


class MainActivity : AppCompatActivity() {

    private var resultGpsCallBack: ResultGpsCallBack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.window.statusBarColor = Color.TRANSPARENT
        this.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
     // get the gps location result
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.REQUEST_CODE_1) {
            if (resultCode == RESULT_OK) {
                resultGpsCallBack?.gpsCallBack()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.locationTrackingFragment)
        // Check if the current fragment is the home screen
        if (currentFragment is LocationTrackingFragment) {
            // If on the home screen, finish the activity (close the app)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    fun setActivityListener(activityListener: ResultGpsCallBack) {
        this.resultGpsCallBack = activityListener
    }


    interface ResultGpsCallBack {
        fun gpsCallBack()
    }

}