package com.example.e_auction.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.e_auction.contract.Bid
import com.example.e_auction.databinding.ItPreviousOrFutureBidsBinding

class PreviousOrFutureBidsAdapter(
    var context: Context,
    var dataSource: MutableList<Bid>
) : RecyclerView.Adapter<PreviousOrFutureBidsAdapter.PreviousOrFutureBidsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviousOrFutureBidsViewHolder {
        val binding = ItPreviousOrFutureBidsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PreviousOrFutureBidsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: PreviousOrFutureBidsViewHolder, position: Int) {
        val bid = dataSource[position]
        holder.bind(bid)
    }

    class PreviousOrFutureBidsViewHolder(private val binding: ItPreviousOrFutureBidsBinding) : RecyclerView.ViewHolder(binding.root) {
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
}
