package com.group24.atmospheric.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()
            viewModel.login(email, pass)
        }

        binding.btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "Google Sign-In Defer: See PoE spec", Toast.LENGTH_SHORT).show()
        }

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
