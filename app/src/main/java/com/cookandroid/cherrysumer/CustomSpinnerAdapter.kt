package com.cookandroid.cherrysumer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class CustomSpinnerAdapter(
    private val context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, R.layout.spinner_selected_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_selected_item, parent, false)

        val textView = view.findViewById<TextView>(R.id.selected_text)
        textView.text = items[position]

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_dropdown_item, parent, false)

        val textView = view.findViewById<TextView>(R.id.dropdown_text)
        textView.text = items[position]

        return view
    }
}