package com.example.e_auction.contract

import com.google.gson.annotations.SerializedName

data class Request (
    @SerializedName("action") var action: String = "",
    @SerializedName("userEmail") var userEmail: String = "",
    @SerializedName("username") var username: String = "",
    @SerializedName("password") var password: String = "",
    @SerializedName("bidId") var bidId: String = "",
    @SerializedName("title") var title: String = "",
    @SerializedName("description") var description: String = "",
    @SerializedName("image") var image: String = "",
    @SerializedName("startingBid") var startingBid: String = "",
    @SerializedName("startDate") var startDate: String = "",
    @SerializedName("endDate") var endDate: String = "",
    @SerializedName("highestBid") var highestBid: String = "",
    @SerializedName("highestBidderEmail") var highestBidderEmail: String = ""
)
