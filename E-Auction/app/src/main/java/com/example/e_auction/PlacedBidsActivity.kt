package com.example.e_auction

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_auction.databinding.ActivityPlacedBidsBinding
import com.example.e_auction.HomeActivity
import com.example.e_auction.adapter.CurrentOrUserPlacedBidsAdapter
import com.example.e_auction.adapter.PreviousOrFutureBidsAdapter
import com.example.e_auction.contract.Bid
import com.example.e_auction.contract.Request
import com.example.e_auction.contract.Response
import com.example.e_auction.network.IRequestContract
import com.example.e_auction.network.NetworkClient
import com.example.e_auction.util.Constant
import com.example.e_auction.util.DataProvider
import retrofit2.Call
import retrofit2.Callback

class PlacedBidsActivity : AppCompatActivity(), Callback<Response> {
    private lateinit var email:String
    private lateinit var binding:ActivityPlacedBidsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressDialog: ProgressDialog
    private val retrofitClient = NetworkClient.getNetworkClient()
    private val requestContract = retrofitClient.create(IRequestContract::class.java)
    lateinit var userPlacedBidsAdapter: CurrentOrUserPlacedBidsAdapter
    lateinit var dataSource:MutableList<Bid>
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_placed_bids)


        sharedPreferences = getSharedPreferences(Constant.PREF_NAME, Context.MODE_PRIVATE)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("please wait...")
        progressDialog.setCancelable(true)

        email = sharedPreferences.getString(Constant.KEY_USER_EMAIL,"").toString()

        binding.imgHome.setOnClickListener {
            Intent(this, HomeActivity::class.java).apply{
                startActivity(this)
            }
        }
        binding.imgUserBids.setOnClickListener{
            Intent(this,UserBidsActivity::class.java).apply{
                startActivity(this)
            }
        }
        binding.imgSignOut.setOnClickListener {
            signOut()
        }
        binding.rvUserPlacedBids.layoutManager = LinearLayoutManager(this)
    }

    private fun signOut(){
        val editor = sharedPreferences.edit()
        editor.clear().commit()

        Intent(this,LogInActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    private fun showUserPlacedBids(){
        context = this
        dataSource = DataProvider.response.usersHighestBids
        if(dataSource.size>0){
            userPlacedBidsAdapter = CurrentOrUserPlacedBidsAdapter(this,context,dataSource)
            binding.rvUserPlacedBids.adapter = userPlacedBidsAdapter

            binding.tvNoBids.visibility = View.INVISIBLE
        }else{
            binding.tvNoBids.visibility = View.VISIBLE
        }

    }


    override fun onStart() {
        super.onStart()
        progressDialog.show()
        val request = Request(
            action = Constant.GET_BIDS,
            userEmail = email
        )
        val callResponse = requestContract.makeApiCall(request)
        callResponse.enqueue(this)
    }


    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
        if(progressDialog.isShowing)
            progressDialog.dismiss()
        if(response.body()!=null){
            val serverResponse = response.body()
            if(serverResponse!!.status){
                DataProvider.response = serverResponse
                showUserPlacedBids()
            }
            else{
                showToast(serverResponse.message)
            }
        }
        else{
            showToast("Server is not responding. Please contact your system administrator")

        }
    }

    override fun onFailure(call: Call<Response>, t: Throwable) {
        if(progressDialog.isShowing)
            progressDialog.dismiss()
        showToast("Server is not responding. Please contact your system administrator")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}