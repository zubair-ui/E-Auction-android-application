package com.example.e_auction

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.e_auction.contract.Request
import com.example.e_auction.contract.Response
import com.example.e_auction.databinding.ActivitySignUpBinding
import com.example.e_auction.network.IRequestContract
import com.example.e_auction.network.NetworkClient
import com.example.e_auction.util.Constant
import retrofit2.Call
import retrofit2.Callback
import java.security.MessageDigest

class SignUpActivity : AppCompatActivity(), Callback<Response> {

    private lateinit var progressDialog: ProgressDialog
    private val retrofitClient = NetworkClient.getNetworkClient()
    private val requestContract = retrofitClient.create(IRequestContract::class.java)
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(true)

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val userName = binding.etName.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showToast("Please fill in all fields")
            } else {
                progressDialog.show()
                val request = Request(
                    action = Constant.SIGN_UP,
                    username = userName,
                    userEmail = email,
                    password = password
                )
                val callResponse = requestContract.makeApiCall(request)
                callResponse.enqueue(this)
            }
        }

        binding.cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                // Hide password
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }

    override fun onFailure(call: Call<Response>, t: Throwable) {
        if (progressDialog.isShowing)
            progressDialog.dismiss()
        showToast("Server is not responding. Please contact your system administrator")
    }

    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
        if (progressDialog.isShowing)
            progressDialog.dismiss()
        if (response.body() != null) {
            val serverResponse = response.body()
            if (serverResponse!!.status) {
                showToast(serverResponse.message)
                Intent(this, LogInActivity::class.java).apply {
                    startActivity(this)
                    finish()
                }
            } else {
                showToast(serverResponse.message)
                binding.etName.setText(serverResponse.message)
            }
        } else {
            showToast("Server is not responding. Please contact your system administrator")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
