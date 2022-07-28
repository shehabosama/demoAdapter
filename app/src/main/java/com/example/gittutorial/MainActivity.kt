package com.example.gittutorial

import Grocery
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private val adapter by lazy {
        ItemAdapter(
            onGroceryClicked = ::makeToast
        )
    }
    private val list:MutableList<Any> = mutableListOf()
    lateinit var recyclerView: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recycler_view)
        linearLayoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        recyclerView.layoutManager = linearLayoutManager

        val groceries = listOf(
            Grocery("A Tomato" ,"Vegetables" , "1b" , 3.0,3),
            Grocery("A Union" ,"Vegetables" , "1b" , 3.0,3),
            Grocery("Mushrooms" ,"Vegetables" , "1b" , 4.0,1),
            Grocery("Bagels" ,"Bakery" , "Pack" , 1.5,2),
            Grocery("Olive" ,"Pantry" , "Bottle" , 6.0,1),
            Grocery("Ice cream" ,"Frozen" , "Pack" , 3.0,2),)

        groceries.sortedBy { it.name }.groupBy { it.name.substring(0..0) }.forEach {it1->
            list.add(it1.key)
            it1.value.forEach {it2-> list.add(it2) }
        }.also {
            adapter.updateData(list)
            recyclerView.adapter = adapter
        }

    }
    private fun makeToast(grocery: Grocery) {
        Toast.makeText(this, grocery.name, Toast.LENGTH_SHORT).show()
    }
}