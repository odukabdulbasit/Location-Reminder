package com.udacity.project4.locationreminders

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.map
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    //private val viewModel: LoginViewModel by viewModel()

    //private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
     //       android.os.Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)



       /* viewModel.authenticationState.observe(this, { authState ->
            val authenticationActivityIntent = Intent(this, AuthenticationActivity::class.java)
            when (authState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.i(TAG,"Authenticated")
                }
                else -> startActivity(authenticationActivityIntent)
            }
        })*/

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}

const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
const val TAG = "RemindersMainActivity"
const val LOCATION_PERMISSION_INDEX = 0
const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1