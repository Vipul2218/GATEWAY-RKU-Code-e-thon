package com.example.gateway.network

import com.example.gateway.utils.AppConstants.API_BASE_URL
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    val getClient: GetData
        get() {
            val gson = GsonBuilder().setLenient().create()

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder().connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES).writeTimeout(3, TimeUnit.MINUTES)
                .addInterceptor(interceptor).build()

            val builder = Retrofit.Builder().baseUrl(API_BASE_URL).client(client)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()

            return builder.create(GetData::class.java)
        }
}