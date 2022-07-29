package com.example.gittutorial

import Grocery
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(
    private val onGroceryClicked: (Grocery) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val LETTER_TYPE = 0
        private const val Grocery_TYPE = 1
    }

    private val _data = mutableListOf<Any>()
    val data: List<Any> get() = _data

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Any>) {
        _data.clear()
        _data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (_data[position]) {
            is String -> LETTER_TYPE
            else -> Grocery_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            LETTER_TYPE ->{
                val layoutInflater = LayoutInflater.from(parent.context)
                LetterViewHolder(layoutInflater.inflate(R.layout.item_designers_letter, parent, false))

            }

            else ->{
                val layoutInflater = LayoutInflater.from(parent.context)
              GroceryViewHolder(layoutInflater.inflate(R.layout.item_grocery, parent, false))
            }

        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LetterViewHolder -> holder.bind(_data[position] as String)
            is GroceryViewHolder -> holder.bind(_data[position] as Grocery)
        }
    }

    override fun getItemCount(): Int = _data.size

    inner class GroceryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var txtLabel:TextView
        lateinit var txtTitle:TextView
        init {
            txtLabel = itemView.findViewById(R.id.txt_label)
            txtTitle =itemView.findViewById(R.id.txt_title)
        }
        fun bind(grocery: Grocery) {


                txtLabel.text = grocery.name
                txtTitle.text = grocery.ctegory
            txtLabel.setOnClickListener {
                    onGroceryClicked(grocery)
                }

        }
    }

    inner class LetterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var textView: TextView
        fun bind(brandLetter: String) {
           textView =  itemView.findViewById(R.id.txt_title)
            textView.text = brandLetter
        }
    }
}