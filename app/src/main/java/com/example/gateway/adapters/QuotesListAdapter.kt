package com.example.gateway.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gateway.R
import kotlinx.android.synthetic.main.list_item.view.*
import com.example.gateway.models.QuoteData

class QuotesListAdapter(private val quotesList: List<QuoteData>) :
    RecyclerView.Adapter<QuotesListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return quotesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quoteData = quotesList[position]
        holder.tvContent.text = quoteData.content
        holder.tvAuthor.text = "--"+quoteData.author
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvContent = itemView.itemTvContent!!
        val tvAuthor = itemView.itemTvAuthor!!

    }
}
