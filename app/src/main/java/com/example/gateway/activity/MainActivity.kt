package com.example.gateway.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gateway.R
import com.example.gateway.adapters.QuotesListAdapter
import com.example.gateway.models.QuoteData
import com.example.gateway.models.QuoteListResponse
import com.example.gateway.network.RetrofitInstance
import com.example.gateway.utils.NetworkUtils
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val quotesList = ArrayList<QuoteData>()
    lateinit var quotesAdapter: QuotesListAdapter
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        initAdapter()
        if (NetworkUtils.isNetworkConnected(applicationContext)) {
            getPhotosFromServer()
        } else {
            Toast.makeText(
                this@MainActivity,
                getString(R.string.str_network_error),
                Toast.LENGTH_SHORT
            )
                .show()
        }

    }

    private fun initAdapter() {
        quotesAdapter = QuotesListAdapter(quotesList)
        recyclerQuotes.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerQuotes.adapter = quotesAdapter
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog.show(
            this,
            getString(R.string.str_loading),
            getString(R.string.str_please_wait),
            false,false
        )
    }

    private fun getPhotosFromServer() {
        showLoadingDialog()
        val call: Call<QuoteListResponse> = RetrofitInstance.getClient.getQuotesList()
        call.enqueue(object : Callback<QuoteListResponse?> {

            override fun onResponse(
                call: Call<QuoteListResponse?>,
                response: Response<QuoteListResponse?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val quoteListResponse = response.body()!!
                    quotesList.addAll(quoteListResponse.results)
                    quotesAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.str_something_went_wrong),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                progressDialog.dismiss()
            }

            override fun onFailure(call: Call<QuoteListResponse?>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.str_something_went_wrong),
                    Toast.LENGTH_SHORT
                )
                    .show()
                progressDialog.dismiss()
            }
        })
    }


}
