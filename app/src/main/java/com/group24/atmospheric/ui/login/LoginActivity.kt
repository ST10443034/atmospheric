package com.group24.atmospheric.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.group24.atmospheric.MainActivity
import com.group24.atmospheric.R
import com.group24.atmospheric.databinding.ActivityLoginBinding
import com.group24.atmospheric.ui.ViewModelFactory
import com.group24.atmospheric.ui.register.RegisterActivity
import kotlinx.coroutines.launch

/**
 * Activity for User Login.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        setupCreateAccountLink()
        setupListeners()
        observeState()
    }

    private fun setupCreateAccountLink() {
        val full = binding.tvCreateAccount.text.toString()
        val linkStart = full.indexOf("Create one")
        if (linkStart == -1) return
        val spannable = SpannableString(full)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.link)),
            linkStart,
            full.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvCreateAccount.text = spannable
    }

    /** Receives the Google account picker result and hands the ID token to the ViewModel. */
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                val token = account.idToken
                if (token != null) viewModel.loginWithGoogle(token)
                else viewModel.reportError(getString(R.string.login_google_failed))
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign-in error, status=${e.statusCode}")
                // 10 = DEVELOPER_ERROR: this build's SHA-1 is not registered in Firebase yet.
                val message = if (e.statusCode == 10) R.string.login_google_unavailable
                else R.string.login_google_failed
                if (e.statusCode != 12501) viewModel.reportError(getString(message)) // 12501 = user cancelled
            }
        }

    private fun startGoogleSignIn() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // generated from google-services.json
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(this, options)
        // Sign out of any cached Google session first so the account chooser always appears.
        client.signOut().addOnCompleteListener { googleLauncher.launch(client.signInIntent) }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()
            viewModel.login(email, pass)
        }

        binding.btnGoogleSignIn.setOnClickListener { startGoogleSignIn() }

        binding.tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnLogin.isEnabled = false
                        binding.btnLogin.text = getString(R.string.action_login_busy)
                        binding.tvLoginError.visibility = View.GONE
                    }
                    is LoginUiState.Success -> {
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                    is LoginUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = getString(R.string.action_login)
                        binding.tvLoginError.text = state.message
                        binding.tvLoginError.visibility = View.VISIBLE
                    }
                    LoginUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.text = getString(R.string.action_login)
                    }
                }
            }
        }
    }
}
