package com.example.tracker2.ui.main

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.tracker2.databinding.FragmentItemBinding
import com.example.tracker2.ui.main.placeholder.PlaceholderContent
import kotlin.random.Random

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(
    private val values: List<Pair<Int, String>>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    private var phList = ArrayList<PlaceholderContent.PlaceholderItem>()

    init {
        phList = values as ArrayList<PlaceholderContent.PlaceholderItem>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.first.toString()
        holder.contentView.text = item.second
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }


}