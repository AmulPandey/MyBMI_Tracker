package com.example.bmi

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.cncoderx.wheelview.OnWheelChangedListener
import com.example.bmi.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.snackbar.Snackbar
import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager

    private val _binding get() = binding

    private lateinit var weightAdapter: WeightPickerAdapter
    private var gender = 'M'
    var height = 1
    private var weight = 50
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        tokenManager = TokenManager(this)  // Initialize TokenManager

        animationView()

        // Gender
        val titlesOfGender: List<String> = listOf("F", "O", "M")
        _binding.genderWheelView.apply {
            titles = titlesOfGender
            elevation = 0f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                isFocusedByDefault = true
            }
            isSelected = true
            focusedIndex = 2
        }
        _binding.genderWheelView.selectListener = {
            gender = titlesOfGender[it][0]
        }

        // Weight
        val pickerLayoutManager = PickerLayoutManager(this, PickerLayoutManager.HORIZONTAL, false)
        pickerLayoutManager.apply {
            isChangeAlpha = true
            scaleDownBy = 0.99f
            scaleDownDistance = 0.8f
            initialPrefetchItemCount = 3
            isSmoothScrollbarEnabled = true
            scrollToPosition(49)
        }

        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(_binding.weightRecyclerBtn)

        weightAdapter = WeightPickerAdapter(this, getData(151), _binding.weightRecyclerBtn)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _binding.weightRecyclerBtn.defaultFocusHighlightEnabled = true
        }
        _binding.weightRecyclerBtn.apply {
            layoutManager = pickerLayoutManager as RecyclerView.LayoutManager
            adapter = weightAdapter
            isSelected = true
            requestFocus()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                isFocusedByDefault = true
            }
        }

        pickerLayoutManager.setOnScrollStopListener { view ->
            weight = Integer.parseInt((view as TextView).text.toString())
        }

        // Height
        _binding.heightWheel.onWheelChangedListener =
            OnWheelChangedListener { view, _, newIndex ->
                val text = view.getItem(newIndex)
                height = Integer.parseInt(text.toString())
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _binding.heightWheel.apply {
                defaultFocusHighlightEnabled = true
            }
        }

        _binding.startButton.setOnActiveListener {
            animationViewUp()
            _binding.startButton.alpha = 0f
            Handler(Looper.getMainLooper()).postDelayed({

                val intent = Intent(this, Result::class.java)
                intent.putExtra("Height", height.toDouble())
                intent.putExtra("Weight", weight.toDouble())
                if (gender == 'M') {
                    intent.putExtra("Gender", 0)
                } else {
                    intent.putExtra("Gender", 1)
                }
                startActivity(intent)

            }, 500)
        }

        // View Past BMI Values button
        val viewPastBmiButton = findViewById<Button>(R.id.view_past_bmi_button)
        viewPastBmiButton.setOnClickListener {
            val intent = Intent(this, BMIHistoryActivity::class.java)
            startActivity(intent)
        }

        oneTapClient = Identity.getSignInClient(this)

        val logoutButton: Button = findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Sign out the user from Google
        oneTapClient.signOut()
            .addOnCompleteListener(this) {
                // Clear user existence
                tokenManager.clearUserExistence()

                // Redirect to login activity
                val intent = Intent(this, LogIn::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish() // Close MainActivity
            }
            .addOnFailureListener {
                Log.e(TAG, "Sign out failed: ${it.localizedMessage}")
                showSnackBar("Sign out failed: ${it.localizedMessage}")
            }
    }

    private fun getData(count: Int): List<String> {
        val data: MutableList<String> = ArrayList()
        for (i in 0 until count) {
            data.add(i.toString())
        }
        return data
    }

    private fun animationView() {
        _binding.apply {
            bodyContainer.translationY = 150f
            footerContainer.translationY = 150f
            heightWheel.translationY = 150f
            weightRecyclerBtn.translationX = 150f

            bodyContainer.alpha = 0f
            footerContainer.alpha = 0f
            heightWheel.alpha = 0f
            weightRecyclerBtn.alpha = 0f

            bodyContainer.animate().translationY(0f).alpha(1f).setDuration(500).setStartDelay(300).start()
            footerContainer.animate().translationY(0f).alpha(1f).setDuration(500).setStartDelay(400).start()
            heightWheel.animate().translationY(0f).alpha(1f).setDuration(500).setStartDelay(450).start()
            weightRecyclerBtn.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(500).start()
        }
    }

    private fun animationViewUp() {
        _binding.apply {
            textView.animate().translationY(0f).alpha(0f).setDuration(50).setStartDelay(0).start()
            bodyContainer.animate().translationY(-250f).alpha(0f).setDuration(500).setStartDelay(0).start()
            footerContainer.animate().translationY(-250f).alpha(0f).setDuration(500).setStartDelay(50).start()
            heightWheel.animate().translationY(-250f).alpha(0f).setDuration(500).setStartDelay(100).start()
            weightRecyclerBtn.animate().translationX(-250f).alpha(0f).setDuration(500).setStartDelay(150).start()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (doubleBackToExitPressedOnce) {
            onBackPressedDispatcher.onBackPressed()
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}
