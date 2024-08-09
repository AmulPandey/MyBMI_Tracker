package com.example.bmi

import android.content.Intent
import android.content.IntentSender
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient

import com.google.android.gms.common.api.ApiException

import com.google.android.material.snackbar.Snackbar
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

class LogIn : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest

    private val tokenManager: TokenManager by lazy { TokenManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)



        if (tokenManager.isUserLoggedIn()) {
            proceedToMainActivity()
        } else {
            setupGoogleSignIn()
        }

        backgroundblur()


    }

    private fun backgroundblur() {

        // Initialize the BlurView
        val blurView = findViewById<BlurView>(R.id.blurView)

        // Set the blur radius
        val radius = 5f

        // Get the decor view and root view
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)

        // Optional: Set a drawable to draw before each blur frame
        val windowBackground: Drawable? = decorView.background

        // Setup BlurView
        blurView.setupWith(rootView, RenderScriptBlur(this)) // Or use RenderEffectBlur
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true) // Optional: Enable automatic updates
        //.setHasFixedTransformationMatrix(true) // Optional: Use if the blur is static

    }

    private fun setupGoogleSignIn() {
        oneTapClient = Identity.getSignInClient(this)
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.your_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()

        oneTapClient.beginSignIn(signUpRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    showSnackBar("Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                showSnackBar("Google Sign-In failed: ${e.localizedMessage}")
            }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ONE_TAP) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    tokenManager.saveUserExistence()  // Save login state
                    proceedToMainActivity()
                } else {
                    showSnackBar("No ID token found!")
                }
            } catch (e: ApiException) {
                showSnackBar("Sign-In failed: ${e.localizedMessage}")
            }
        }
    }

    private fun proceedToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Close the login activity
    }

    companion object {
        private const val REQ_ONE_TAP = 2
    }
}
