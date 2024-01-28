package com.delivero.driver.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.delivero.driver.models.ServiceType


class IconListAdapter(mContext: Context, var services:List<ServiceType>): ArrayAdapter<String>(mContext,android.R.layout.select_dialog_item,android.R.id.text1) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = super.getView(position, convertView, parent)
        val tv = v.findViewById<View>(android.R.id.text1) as TextView

        tv.text=services[position].typeName
        //Put the image on the TextView
     /*   Glide.with(mContext).load(services[position].icon).apply(RequestOptions().override(24,24).fitCenter()).into(
            object : CustomTarget<Drawable>(50,50){
                override fun onLoadCleared(placeholder: Drawable?) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(placeholder, null, null, null)
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                ) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(resource, null, null, null)
                }
            }
        )*/
        //Put the image on the TextView

        //Add margin between image and text (support various screen densities)
        tv.setCompoundDrawablesWithIntrinsicBounds(services[position].icon, 0, 0, 0)

        //Add margin between image and text (support various screen densities)
        tv.setCompoundDrawablePadding(((5 * context.resources.displayMetrics.density + 0.5f).toInt()))

        return v
    }
}