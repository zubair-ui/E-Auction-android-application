package com.example.e_auction

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.e_auction.databinding.ActivityLogInBinding
import com.example.e_auction.HomeActivity
import com.example.e_auction.contract.Request
import com.example.e_auction.contract.Response
import com.example.e_auction.network.IRequestContract
import com.example.e_auction.network.NetworkClient
import com.example.e_auction.util.Constant
import retrofit2.Call
import retrofit2.Callback
import java.security.MessageDigest

class LogInActivity : AppCompatActivity(), Callback<Response> {
    private lateinit var progressDialog: ProgressDialog
    private val retrofitClient = NetworkClient.getNetworkClient()
    private val requestContract = retrofitClient.create(IRequestContract::class.java)
    private lateinit var sharedPreferences: SharedPreferences
    private var userEmail:String=""
    private var userPassword:String=""
    private lateinit var binding: ActivityLogInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_log_in)

        sharedPreferences = getSharedPreferences(Constant.PREF_NAME, Context.MODE_PRIVATE)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("please wait...")
        progressDialog.setCancelable(true)

        binding.cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                binding.etPassword.transformationMethod= PasswordTransformationMethod.getInstance()
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        checkIfUserAlreadyRegistered()

        binding.tvCreateAccount.setOnClickListener {
            Intent(this,SignUpActivity::class.java).apply{
                startActivity(this)
            }

        }

        binding.btnSubmit.setOnClickListener {
            userEmail=binding.etEmail.text.toString().trim()
            userPassword=binding.etPassword.text.toString().trim()

            if(userPassword.isEmpty()||userEmail.isEmpty()){
                showToast("Please enter complete information")
            }
            else{
                progressDialog.show()
                val request = Request(
                    action = Constant.LOG_IN,
                    userEmail = userEmail,
                    password = userPassword
                )
                val callResponse = requestContract.makeApiCall(request)
                callResponse.enqueue(this)
            }
        }
    }


    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
        if (progressDialog.isShowing)
            progressDialog.dismiss()
        if (response.body() != null) {
            val serverResponse = response.body()
            if (serverResponse!!.status) {
                showToast(serverResponse.message)
                if(userEmail.isNotEmpty()||userPassword.isNotEmpty()){
                    saveUserToPref(userEmail,userPassword)
                }
                Intent(this, HomeActivity::class.java).apply {
                    startActivity(this)
                    finish()
                }
            } else {
                showToast(serverResponse.message)
            }
        } else {
            showToast("Server is not responding. Please contact your system administrator")
        }
    }

    override fun onFailure(call: Call<Response>, t: Throwable) {
        if (progressDialog.isShowing)
            progressDialog.dismiss()
        showToast("Server is not responding. Please contact your system administrator")
    }
    private fun saveUserToPref(userEmail:String,userPassword:String){
        val editor = sharedPreferences.edit()
        editor.putString(Constant.KEY_USER_EMAIL,userEmail)
        editor.putString(Constant.KEY_USER_PASSWORD,userPassword)
        editor.commit()
    }

    private fun checkIfUserAlreadyRegistered(){
        val email:String =
            sharedPreferences.getString(Constant.KEY_USER_EMAIL,"invalid user email").toString()
        val password:String =
            sharedPreferences.getString(Constant.KEY_USER_PASSWORD,"invalid user password").toString()

        if(!email.contentEquals("invalid user email")
            && !password.contentEquals("invalid user password")){

            progressDialog.show()
            val request = Request(
                action = Constant.LOG_IN,
                userEmail = email,
                password = password
            )
            val callResponse = requestContract.makeApiCall(request)
            callResponse.enqueue(this)

        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}