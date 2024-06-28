package com.example.e_auction.contract

import com.google.gson.annotations.SerializedName

data class Bid (
    @SerializedName("bidId") var bidId:String="",
    @SerializedName("userEmail") var userEmail:String="",
    @SerializedName("title") var title:String="",
    @SerializedName("description") var description:String="",
    @SerializedName("image") var image:String="",
    @SerializedName("startingBid") var startingBid:String="",
    @SerializedName("highestBid") var highestBid:String="",
    @SerializedName("highestBidderEmail") var highestBidderEmail:String="",
    @SerializedName("startDate") var startDate:String="",
    @SerializedName("endDate") var endDate:String=""
)