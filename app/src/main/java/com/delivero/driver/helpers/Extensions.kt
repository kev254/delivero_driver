package com.delivero.driver.helpers

import android.content.Context
import android.util.Patterns
import android.view.View
import android.widget.Toast

fun View.show(){
    visibility=View.VISIBLE
}

fun View.hide(){
    visibility=View.GONE
}

fun View.remove(){
    visibility=View.INVISIBLE
}

fun Context.toast(message:String){
    Toast.makeText(this,message,Toast.LENGTH_LONG).show()
}

fun Context.shortToast(message:String){
    Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
}

fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()