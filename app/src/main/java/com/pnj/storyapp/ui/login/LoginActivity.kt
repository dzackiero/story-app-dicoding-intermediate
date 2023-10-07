package com.pnj.storyapp.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.pnj.storyapp.R
import com.pnj.storyapp.databinding.ActivityLoginBinding
import com.pnj.storyapp.ui.ViewModelFactory
import com.pnj.storyapp.ui.home.HomeActivity
import com.pnj.storyapp.ui.register.RegisterActivity
import com.pnj.storyapp.util.Result
import com.pnj.storyapp.util.ValidateType
import com.pnj.storyapp.util.showLoading
import com.pnj.storyapp.util.showToast
import com.pnj.storyapp.util.validate

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels { ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }

    private fun setupAction() {
        binding.apply {
            btnGotoRegister.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
                finish()
            }

            btnLogin.setOnClickListener {
                if (validateForm()) {
                    val email = edLoginEmail.text.toString()
                    val password = edLoginPassword.text.toString()

                    viewModel.login(email, password)
                        .observe(this@LoginActivity) { result ->
                            if (result != null) {
                                when (result) {
                                    is Result.Loading -> {
                                        btnLogin.showLoading(true)
                                    }

                                    is Result.Error -> {
                                        btnLogin.showLoading(false)
                                        showToast("Error: ${result.error}")
                                    }

                                    is Result.Success -> {
                                        btnLogin.showLoading(false)
                                        showToast(getString(R.string.success, "Login"))
                                        val intent =
                                            Intent(this@LoginActivity, HomeActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                }
                            }
                        }
                }
            }

        }
    }

    private fun validateForm(): Boolean {
        binding.apply {
            val validates = listOf(
                edLoginEmail.validate("Email", ValidateType.REQUIRED),
                edLoginEmail.validate("Email", ValidateType.EMAIL),
                edLoginPassword.validate("Password", ValidateType.REQUIRED),
                edLoginPassword.validate("Password", ValidateType.MIN_CHAR, 8),
            )

            return !validates.contains(false)
        }
    }
}