package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class FollowersAdapter(private val followers: List<String>) :
    RecyclerView.Adapter<FollowersAdapter.FollowerViewHolder>() {

    class FollowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.follower_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_follower, parent, false)
        return FollowerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        holder.nameTextView.text = followers[position]
    }

    override fun getItemCount(): Int {
        return followers.size
    }
}
