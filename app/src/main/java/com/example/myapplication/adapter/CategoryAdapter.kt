package com.example.myapplication.adapter

import android.R.attr.tag
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.SongsListActivity
import com.example.myapplication.databinding.CategoryItemRecylerRowBinding
import com.example.myapplication.model.CategoryModel

class CategoryAdapter(private val categoryList: List<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {
    class MyViewHolder(private val binding :CategoryItemRecylerRowBinding):RecyclerView.ViewHolder(binding.root){

        fun bindData(category: CategoryModel){
            binding .nameTextView.text =category.name.uppercase()
            Glide.with(binding.coverImageView).load(category.coverUrl).apply(
                RequestOptions().transform(RoundedCorners(32))).into(binding.coverImageView)
            val context =binding.root.context
            binding.root.setOnClickListener{
                SongsListActivity.category= category
                context.startActivity(Intent(context, SongsListActivity::class.java))
            }

        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
    val binding= CategoryItemRecylerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
       holder.bindData(categoryList[position])
    }

    override fun getItemCount(): Int {
     return categoryList.size
    }


}