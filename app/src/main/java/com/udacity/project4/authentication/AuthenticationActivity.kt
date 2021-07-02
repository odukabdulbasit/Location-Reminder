package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    companion object {
        val TAG: String = AuthenticationActivity::class.java.simpleName
        const val SIGN_IN_CODE_REQUEST = 100
    }

    private lateinit var binding: ActivityAuthenticationBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        binding.login.setOnClickListener {
            performLogin()
        }

        viewModel.authenticationState.observe(this) { authenticationState ->
            if (authenticationState == LoginViewModel.AuthenticationState.AUTHENTICATED) {
                startRemindersActivity()
            }
        }
    }

    private fun performLogin() {
        val providers =
            arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_CODE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_CODE_REQUEST) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK && response != null) startRemindersActivity()
        }
    }

    private fun startRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }
}
