package com.example.e_auction.contract

import com.example.e_auction.contract.Bid

data class Response (
    var status: Boolean = false,
    var responseCode: Int = -1,
    var message: String = "",
    var bidId: String = "",
    var userBids: MutableList<Bid> = mutableListOf(),
    var usersHighestBids: MutableList<Bid> = mutableListOf(),
    var previousBids: MutableList<Bid> = mutableListOf(),
    var currentBids: MutableList<Bid> = mutableListOf(),
    var futureBids: MutableList<Bid> = mutableListOf()
)
