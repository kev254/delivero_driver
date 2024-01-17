package com.delivero.driver

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.delivero.driver.interfaces.Collections
import com.delivero.driver.models.Token
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setUpMessaging()
    }

    private fun setUpMessaging() {
        Firebase.messaging.subscribeToTopic("Driver")
        Firebase.messaging.subscribeToTopic("Delivero")

        Firebase.messaging.token.addOnSuccessListener {
            val token= Token(Firebase.auth.currentUser!!.uid,it)
            Firebase.firestore.collection(Collections.TOKENS)
                .document(Firebase.auth.currentUser!!.uid)
                .set(token)
                .addOnSuccessListener {

                }
        }
    }
}