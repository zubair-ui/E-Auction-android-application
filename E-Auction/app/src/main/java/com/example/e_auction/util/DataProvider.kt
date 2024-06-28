package com.example.e_auction.util

import com.example.e_auction.contract.Bid
import com.example.e_auction.contract.Response

object DataProvider {
    var response: Response = Response()
    var bid: Bid = Bid()
    lateinit var userEmail:String
}