package com.s.sendlite.Utils


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.s.sendlite.R
import com.s.sendlite.dataClass.Photo
import kotlinx.android.synthetic.main.card.view.*

class PhotoAdapter(private val listItems: List<Photo>) : RecyclerView.Adapter<PhotoAdapter.MyViewHolder>() {

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
        fun setData(listItem: Photo) {
            item.file_name.text = listItem.toString()
        }
    }
}