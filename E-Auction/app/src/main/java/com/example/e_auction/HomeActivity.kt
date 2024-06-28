package com.example.e_auction

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_auction.LogInActivity
import com.example.e_auction.PlacedBidsActivity
import com.example.e_auction.R
import com.example.e_auction.UserBidsActivity
import com.example.e_auction.adapter.CurrentOrUserPlacedBidsAdapter
import com.example.e_auction.adapter.PreviousOrFutureBidsAdapter
import com.example.e_auction.contract.Bid
import com.example.e_auction.contract.Request
import com.example.e_auction.contract.Response
import com.example.e_auction.databinding.ActivityHomeBinding
import com.example.e_auction.network.IRequestContract
import com.example.e_auction.network.NetworkClient
import com.example.e_auction.util.Constant
import com.example.e_auction.util.DataProvider
import retrofit2.Call
import retrofit2.Callback

class HomeActivity : AppCompatActivity(), Callback<Response> {
    private lateinit var email:String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressDialog: ProgressDialog
    private val retrofitClient = NetworkClient.getNetworkClient()
    private val requestContract = retrofitClient.create(IRequestContract::class.java)
    private lateinit var binding: ActivityHomeBinding
    private lateinit var spinnerValue:String
    lateinit var currentBidsAdapter: CurrentOrUserPlacedBidsAdapter
    lateinit var previousOrFutureBidsAdapter: PreviousOrFutureBidsAdapter
    lateinit var dataSource:MutableList<Bid>
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_home)

        binding.spinner.setSelection(0)
        spinnerValue = binding.spinner.selectedItem.toString()



        sharedPreferences = getSharedPreferences(Constant.PREF_NAME, Context.MODE_PRIVATE)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("please wait...")
        progressDialog.setCancelable(true)

        email = sharedPreferences.getString(Constant.KEY_USER_EMAIL,"").toString()
        DataProvider.userEmail=email



        binding.imgPlacedBids.setOnClickListener {
            Intent(this, PlacedBidsActivity::class.java).apply{
                startActivity(this)
            }
        }
        binding.imgUserBids.setOnClickListener{
            Intent(this, UserBidsActivity::class.java).apply{
                startActivity(this)
            }
        }
        binding.imgSignOut.setOnClickListener {
            signOut()
        }


        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                spinnerValue = binding.spinner.selectedItem.toString()

                if(spinnerValue=="Current Bids"){
                    showCurrentBids()
                }
                else if (spinnerValue=="Previous Bids" ||spinnerValue=="Future Bids"){
                    showPreviousOrFutureBids(spinnerValue)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
        binding.rvBids.layoutManager = LinearLayoutManager(this)
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

    private fun showCurrentBids(){
        context = this
        dataSource = DataProvider.response.currentBids
        if(dataSource.size>0){
            currentBidsAdapter = CurrentOrUserPlacedBidsAdapter(this,context,dataSource)
            binding.rvBids.adapter = currentBidsAdapter

            binding.tvNoBids.visibility = View.INVISIBLE
        }else{
            binding.tvNoBids.visibility = View.VISIBLE
        }

    }
    private fun showPreviousOrFutureBids(type:String){
        context = this
        if(type=="Previous Bids"){
            dataSource = DataProvider.response.previousBids
            if(dataSource.size>0){
                previousOrFutureBidsAdapter = PreviousOrFutureBidsAdapter(context,dataSource)
                binding.rvBids.adapter = previousOrFutureBidsAdapter

                binding.tvNoBids.visibility = View.INVISIBLE
            }else{
                binding.tvNoBids.visibility = View.VISIBLE
            }
        }else{
            dataSource = DataProvider.response.futureBids
            if(dataSource.size>0){
                previousOrFutureBidsAdapter = PreviousOrFutureBidsAdapter(context,dataSource)
                binding.rvBids.adapter = previousOrFutureBidsAdapter

                binding.tvNoBids.visibility = View.INVISIBLE
            }else{
                binding.tvNoBids.visibility = View.VISIBLE
            }
        }


    }

    private fun signOut(){
        val editor = sharedPreferences.edit()
        editor.clear().commit()

        Intent(this,LogInActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
        if(progressDialog.isShowing)
            progressDialog.dismiss()
        if(response.body()!=null){
            val serverResponse = response.body()
            if(serverResponse!!.status){
                DataProvider.response = serverResponse
                showCurrentBids()
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