package com.s.sendlite.Utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.s.sendlite.R
import com.s.sendlite.dataClass.Music
import kotlinx.android.synthetic.main.card.view.*

class MusicAdapter(private val listItems: List<Music>) : RecyclerView.Adapter<MusicAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = listItems.size


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val listItem = listItems[position]
        holder.setData(listItem)
    }

    inner class MyViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
        fun setData(listItem: Music) {
            item.file_name.text = listItem.toString()
        }
    }
}