package com.delivero.driver.helpers

import android.content.Context
import androidx.core.content.ContextCompat
import com.delivero.driver.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Utils {
    fun showMessageDialog(context: Context,title:String,message:String){
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_white_rounded_10))
            .setPositiveButton("OK"){
                dialog,_->
                dialog?.dismiss()
            }
            .create()
            .show()
    }
}