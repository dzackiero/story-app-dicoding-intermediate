package com.pnj.storyapp.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.pnj.storyapp.R
import com.pnj.storyapp.databinding.ActivityRegisterBinding
import com.pnj.storyapp.util.Result
import com.pnj.storyapp.ui.ViewModelFactory
import com.pnj.storyapp.ui.login.LoginActivity
import com.pnj.storyapp.util.ValidateType
import com.pnj.storyapp.util.showLoading
import com.pnj.storyapp.util.showToast
import com.pnj.storyapp.util.validate

class RegisterActivity : AppCompatActivity() {

    private val viewModel: RegisterViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }

    private fun setupAction() {
        binding.apply {
            btnGotoLogin.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            btnRegister.setOnClickListener {
                if (validateForm()) {
                    val name = edRegisterName.text.toString()
                    val email = edRegisterEmail.text.toString()
                    val password = edRegisterPassword.text.toString()

                    viewModel.register(name, email, password)
                        .observe(this@RegisterActivity) { result ->
                            if (result != null) {
                                when (result) {
                                    is Result.Loading -> {
                                        btnRegister.showLoading(true)
                                        btnRegister.isEnabled = false

                                    }

                                    is Result.Error -> {
                                        btnRegister.showLoading(false)
                                        progressbar.isVisible = false
                                        showToast(result.error)
                                    }

                                    is Result.Success -> {
                                        btnRegister.showLoading(false)
                                        progressbar.isVisible = false
                                        MaterialAlertDialogBuilder(this@RegisterActivity)
                                            .setTitle(getString(R.string.your_account_has_been_created))
                                            .setMessage(getString(R.string.please_login_to_your_account))
                                            .setPositiveButton("Login") { dialog, _ ->
                                                dialog.dismiss()
                                                val intent =
                                                    Intent(
                                                        this@RegisterActivity,
                                                        LoginActivity::class.java
                                                    )
                                                intent.flags =
                                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                            }
                                            .create().show()
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
                edRegisterEmail.validate("Email cannot be empty", ValidateType.REQUIRED),
                edRegisterEmail.validate("Must be a valid email", ValidateType.EMAIL),
                edRegisterName.validate("Name cannot be empty", ValidateType.REQUIRED),
                edRegisterPassword.validate("Password cannot be empty", ValidateType.REQUIRED),
                edRegisterPassword.validate(
                    "a password must contain at least 8 characters",
                    ValidateType.MIN_CHAR,
                    8
                ),
            )

            return !validates.contains(false)
        }
    }
}