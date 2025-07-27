package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.CategoryAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.CategoryModel
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {

        binding= ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getCategories()

    }
    fun getCategories(){
        FirebaseFirestore.getInstance().collection("categories")
            .get().addOnSuccessListener {
                val categoryList=it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }

    }
    fun setupCategoryRecyclerView(categoryList: List<CategoryModel>){
        categoryAdapter= CategoryAdapter(categoryList)
        binding.categoriesRecylerView.layoutManager= LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        binding.categoriesRecylerView.adapter= categoryAdapter
    }
}