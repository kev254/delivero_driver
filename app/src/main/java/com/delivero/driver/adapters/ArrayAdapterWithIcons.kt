package com.delivero.driver.adapters

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.util.Arrays

class ArrayAdapterWithIcons : ArrayAdapter<String?> {
    private var images: List<Int>

    constructor(context: Context?, items: List<String?>?, images: List<Int>) : super(
        context!!, android.R.layout.select_dialog_item, items!!
    ) {
        this.images = images
    }

    constructor(context: Context?, items: Array<String?>?, images: Array<Int?>) : super(
        context!!, android.R.layout.select_dialog_item, items!!
    ) {
        this.images = Arrays.asList(*images) as List<Int>
    }

    constructor(context: Context, items: Int, images: Int) : super(
        context,
        android.R.layout.select_dialog_item,
        context.resources.getStringArray(items)
    ) {
        val imgs = context.resources.obtainTypedArray(images)
        this.images = object : ArrayList<Int>() {
            init {
                for (i in 0 until imgs.length()) {
                    add(imgs.getResourceId(i, -1))
                }
            }
        }.toList()

        // recycle the array
        imgs.recycle()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<View>(android.R.id.text1) as TextView
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(images[position], 0, 0, 0)
        textView.setCompoundDrawablePadding(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                12f,
                context.resources.displayMetrics
            ).toInt()
        )
        return view
    }
}
