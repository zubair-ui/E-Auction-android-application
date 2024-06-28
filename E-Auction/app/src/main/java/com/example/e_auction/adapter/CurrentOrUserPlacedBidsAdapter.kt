package com.example.e_auction.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.e_auction.HomeActivity
import com.example.e_auction.PlacedBidsActivity
import com.example.e_auction.contract.Bid
import com.example.e_auction.contract.Request
import com.example.e_auction.contract.Response
import com.example.e_auction.databinding.ItCurrentOrUserPlacedBidsBinding
import com.example.e_auction.databinding.ItUserAddedBidsBinding
import com.example.e_auction.network.IRequestContract
import com.example.e_auction.network.NetworkClient
import com.example.e_auction.util.Constant
import com.example.e_auction.util.DataProvider
import retrofit2.Call
import retrofit2.Callback

class CurrentOrUserPlacedBidsAdapter(var activity: Activity, var context: Context, var dataSource: MutableList<Bid>):
    RecyclerView.Adapter<CurrentOrUserPlacedBidsAdapter.CurrentOrUserPlacedBidsViewHolder>(),
    Callback<Response> {

    private var progressDialog: ProgressDialog = ProgressDialog(context)
    private val retrofitClient = NetworkClient.getNetworkClient()
    private val requestContract = retrofitClient.create(IRequestContract::class.java)
    private lateinit var updatedBid: Bid
    private var updatedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentOrUserPlacedBidsViewHolder {
        val binding = ItCurrentOrUserPlacedBidsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CurrentOrUserPlacedBidsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: CurrentOrUserPlacedBidsViewHolder,@SuppressLint("RecyclerView") position: Int) {
        val bid = dataSource[position]
        holder.bind(bid)
        holder.title.text = bid.title
        holder.image.setImageBitmap(base64ToBitmap(bid.image))
        holder.description.text = bid.description
        holder.startDate.text = bid.startDate
        holder.endDate.text = bid.endDate
        holder.startingBid.text = bid.startingBid.toString()
        holder.highestBid.text = bid.highestBid.toString()
        holder.highestBidder.text = bid.highestBidderEmail

        holder.submitButton.setOnClickListener {
            val userBidInt = holder.userBid.text.toString().toInt()
            val highestBidInt = holder.highestBid.text.toString().toInt()
            if(userBidInt<highestBidInt){
                context.showToast("Please Enter valid value")
            }
            else{
                progressDialog.setMessage("Please wait...")
                progressDialog.setCancelable(false)
                updatedBid = bid
                updatedPosition = position
                val request = Request(
                    action = Constant.UPDATE_HIGHEST_BIDDER,
                    userEmail = DataProvider.userEmail,
                    bidId = bid.bidId,
                    highestBid = userBidInt.toString()
                )
                progressDialog.show()
                val callResponse = requestContract.makeApiCall(request)
                callResponse.enqueue(this)
                holder.bind(bid)
            }
        }


    }
    private fun base64ToBitmap(base64String: String): Bitmap? {
        // Decode Base64 string to byte array
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

        // Create Bitmap from byte array
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    class CurrentOrUserPlacedBidsViewHolder(private var binding: ItCurrentOrUserPlacedBidsBinding) : RecyclerView.ViewHolder(binding.root) {
        var title = binding.tvTitle
        var image = binding.imgItem
        var description = binding.tvDescription
        var startDate = binding.tvStartDate
        var endDate = binding.tvEndDate
        var startingBid = binding.tvStartingBid
        var highestBid = binding.tvHighestBid
        var highestBidder = binding.tvHighestBidder
        var userBid = binding.etUserBid
        var submitButton = binding.btBid
    

    fun bind(bid: Bid) {
            binding.tvTitle.text = bid.title
            binding.imgItem.setImageBitmap(base64ToBitmap(bid.image))
            binding.tvDescription.text = bid.description
            binding.tvStartDate.text = bid.startDate
            binding.tvEndDate.text = bid.endDate
            binding.tvStartingBid.text = bid.startingBid.toString()
            binding.tvHighestBid.text = bid.highestBid.toString()
            binding.tvHighestBidder.text = bid.highestBidderEmail
        }
        private fun base64ToBitmap(base64String: String): Bitmap? {
            // Decode Base64 string to byte array
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

            // Create Bitmap from byte array
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }
    }

    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
        if (progressDialog.isShowing)
            progressDialog.dismiss()
        if (response.body() != null) {
            val serverResponse = response.body()
            if (serverResponse!!.status) {
                context.showToast(serverResponse.message)
                val intent = Intent(context, PlacedBidsActivity::class.java)
                context.startActivity(intent)
            } else {
                context.showToast(serverResponse.message)
            }
        } else {
            context.showToast("Server is not responding. Please contact your system administrator")
        }
    }

    override fun onFailure(call: Call<Response>, t: Throwable) {
        if (progressDialog.isShowing)
            progressDialog.dismiss()
        context.showToast("Server is not responding. Please contact your system administrator")
    }
    private fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
