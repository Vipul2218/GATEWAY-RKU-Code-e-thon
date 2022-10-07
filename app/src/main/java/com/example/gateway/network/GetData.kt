package com.example.gateway.network

import com.example.gateway.models.QuoteListResponse
import retrofit2.Call
import retrofit2.http.GET

interface GetData {

    @GET("quotes?page=1")
    fun getQuotesList() : Call<QuoteListResponse>

}