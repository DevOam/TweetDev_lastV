package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class FollowingAdapter(private val following: List<String>) :
    RecyclerView.Adapter<FollowingAdapter.FollowingViewHolder>() {

    class FollowingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.follower_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_follower, parent, false)
        return FollowingViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        holder.nameTextView.text = following[position]
    }

    override fun getItemCount(): Int {
        return following.size
    }
}
