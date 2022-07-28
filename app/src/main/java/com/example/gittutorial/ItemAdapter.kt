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
                // LetterViewHolder(ItemDesignersLetterBinding.inflate(parent.inflater, parent, false))
                val layoutInflater = LayoutInflater.from(parent.context)
                //user_list_item.xml is below
                LetterViewHolder(layoutInflater.inflate(R.layout.item_designers_letter, parent, false))

            }

            else ->{
                val layoutInflater = LayoutInflater.from(parent.context)
                //user_list_item.xml is below
              BrandViewHolder(layoutInflater.inflate(R.layout.item_designers_brand, parent, false))
            }

        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LetterViewHolder -> holder.bind(_data[position] as String)
            is BrandViewHolder -> holder.bind(_data[position] as Grocery)
        }
    }

    override fun getItemCount(): Int = _data.size

    inner class BrandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var txtLabel:TextView
        lateinit var txtTitle:TextView
        init {
            txtLabel = itemView.findViewById(R.id.txt_label)
            txtTitle =itemView.findViewById(R.id.txt_title)
        }
        fun bind(grocery: Grocery) {


                txtLabel.text = grocery.name
                txtTitle.text = grocery.ctegory
//                root.setOnClickListener {
//                    onBrandClicked(brand)
//                }

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