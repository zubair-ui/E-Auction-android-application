package com.example.e_auction.network

import com.example.e_auction.contract.Request
import com.example.e_auction.contract.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IRequestContract {
    @POST("service.php")
    fun makeApiCall(@Body request: Request): Call<Response>
}