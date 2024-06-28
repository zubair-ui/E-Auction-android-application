package com.example.e_auction.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
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
import com.example.e_auction.AddOrUpdateBidActivity
import com.example.e_auction.contract.Bid
import com.example.e_auction.contract.Request
import com.example.e_auction.contract.Response
import com.example.e_auction.databinding.ItUserAddedBidsBinding
import com.example.e_auction.network.IRequestContract
import com.example.e_auction.network.NetworkClient
import com.example.e_auction.util.Constant
import com.example.e_auction.util.DataProvider
import retrofit2.Call
import retrofit2.Callback

class UserAddedBidsAdapter(var activity: Activity, var context: Context, var dataSource: MutableList<Bid>) :
    RecyclerView.Adapter<UserAddedBidsAdapter.UserAddedBidsViewHolder>(), Callback<Response> {

    private var progressDialog: ProgressDialog = ProgressDialog(context)
    private val retrofitClient = NetworkClient.getNetworkClient()
    private val requestContract = retrofitClient.create(IRequestContract::class.java)
    private lateinit var deletedBid: Bid
    private var deletedPosition: Int = -1

    private fun base64ToBitmap(base64String: String): Bitmap? {
        // Decode Base64 string to byte array
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

        // Create Bitmap from byte array
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAddedBidsViewHolder {
        val view = ItUserAddedBidsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserAddedBidsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: UserAddedBidsViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val bid = dataSource[position]
        holder.title.text = bid.title
        holder.image.setImageBitmap(base64ToBitmap(bid.image))
        holder.description.text = bid.description
        holder.startDate.text = bid.startDate
        holder.endDate.text = bid.endDate
        holder.startingBid.text = bid.startingBid.toString()
        holder.highestBid.text = bid.highestBid.toString()
        holder.highestBidder.text = bid.highestBidderEmail

        holder.btnEdit.setOnClickListener {
            Intent(context, AddOrUpdateBidActivity::class.java).apply {
                DataProvider.bid = bid
                putExtra(Constant.KEY_REASON, 2)  //2 means Edit
                activity.startActivity(this)
            }
        }

        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Bid App Alert")
                .setMessage("Are you sure you want to delete this bid?")
                .setPositiveButton("Yes") { dialog, which ->
                    progressDialog.setMessage("Please wait...")
                    progressDialog.setCancelable(false)
                    deletedBid = bid
                    deletedPosition = position
                    val request = Request(
                        action = Constant.DELETE_BID_ITEM,
                        userEmail = DataProvider.userEmail,
                        bidId = bid.bidId
                    )
                    progressDialog.show()
                    val callResponse = requestContract.makeApiCall(request)
                    callResponse.enqueue(this)
                }
                .setNegativeButton("No") { dialog, which ->
                    dialog?.dismiss()
                }
                .create()
                .show()
        }
    }

    class UserAddedBidsViewHolder(view: ItUserAddedBidsBinding) : RecyclerView.ViewHolder(view.root) {
        var title = view.tvTitle
        var image = view.imgItem
        var description = view.tvDescription
        var startDate = view.tvStartDate
        var endDate = view.tvEndDate
        var startingBid = view.tvStartingBid
        var highestBid = view.tvHighestBid
        var highestBidder = view.tvHighestBidder
        var btnEdit = view.imgEdit
        var btnDelete = view.imgDelete
    }

    override fun onFailure(call: Call<Response>, t: Throwable) {
        if (progressDialog.isShowing)
            progressDialog.dismiss()
        context.showToast("Server is not responding. Please contact your system administrator")
    }

    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
        if (progressDialog.isShowing)
            progressDialog.dismiss()
        if (response.body() != null) {
            val serverResponse = response.body()
            if (serverResponse!!.status) {
                dataSource.remove(deletedBid)
                notifyItemRemoved(deletedPosition)
                notifyItemRangeChanged(deletedPosition, dataSource.size)
                context.showToast(serverResponse.message)
            } else {
                context.showToast(serverResponse.message)
            }
        } else {
            context.showToast("Server is not responding. Please contact your system administrator")
        }
    }

    private fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
