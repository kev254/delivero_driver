package com.delivero.driver.helpers

import android.content.Context
import androidx.core.content.ContextCompat
import com.delivero.driver.R
import com.delivero.driver.interfaces.Collections
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

    @OptIn(DelicateCoroutinesApi::class)
    fun updateUserCoordinates(latitude:Double, longitude:Double, location:String){
        GlobalScope.launch(Dispatchers.IO) {
            val updates= mutableMapOf<String,Any>()
            updates["latitude"] = latitude
            updates["longitude"] = longitude
            updates["location"] = location
            Firebase.firestore.collection(Collections.USERS)
                .document(Firebase.auth.currentUser!!.uid)
                .update(updates)
        }

    }
}