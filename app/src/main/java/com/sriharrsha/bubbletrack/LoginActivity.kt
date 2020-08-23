package com.sriharrsha.bubbletrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class LoginActivity : AppCompatActivity() {
    private val CODE_FIREBASE_SIGN_IN = 2019
    private val auth = FirebaseAuth.getInstance();
    val TAG: String = "Login Screen";


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the status bar.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        actionBar?.hide()
        
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_login)

        if (auth.currentUser != null) {
            // already signed in
            startActivity(
                Intent(this, UsagePermissionsActivity::class.java)
            )
        } else {
            //Allow only U.S And India
            val whitelistedCountries: MutableList<String> = ArrayList()
            whitelistedCountries.add("in")
            whitelistedCountries.add("us")

            // not signed in
            startActivityForResult(
                // Get an instance of AuthUI based on the default app
                AuthUI.getInstance().createSignInIntentBuilder().
                setAvailableProviders(
                    listOf(
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.PhoneBuilder()
                    .setWhitelistedCountries(whitelistedCountries)
                    .setDefaultCountryIso(Locale.getDefault().country).build())
                )   .setTheme(R.style.LoginTheme)
                    .setIsSmartLockEnabled(true)
                    .build(),
                CODE_FIREBASE_SIGN_IN);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == CODE_FIREBASE_SIGN_IN){
            val response  = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(this, UsagePermissionsActivity::class.java)
                        .putExtra("my_token", response?.idpToken)
                )
                finish();
            }else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "You cancelled the login.", Toast.LENGTH_SHORT).show()
                    return;
                }
                if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "Check your Internet Connection.", Toast.LENGTH_SHORT).show()
                    return;
                }
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
