package com.delivero.driver.helpers

import android.content.Context
import com.delivero.driver.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class Preference {

    companion object{
        fun saveUser(context:Context,user: User){
            val preference=context.getSharedPreferences("DeliveroDriver",Context.MODE_PRIVATE)
            val editor=preference.edit();
            editor.putString(Firebase.auth.currentUser!!.uid, Gson().toJson(user))
            editor.apply()
        }

        fun getUser(context: Context): User? {
            val preference=context.getSharedPreferences("DeliveroDriver",Context.MODE_PRIVATE)
            if (preference.contains(Firebase.auth.currentUser!!.uid)){
                val cUser=preference.getString(Firebase.auth.currentUser!!.uid,null)
                return Gson().fromJson(cUser, User::class.java)
            }
            return null;
        }
    }
}