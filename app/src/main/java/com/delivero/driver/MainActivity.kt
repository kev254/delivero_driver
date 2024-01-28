package com.delivero.driver

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.delivero.driver.databinding.ActivityMainBinding
import com.delivero.driver.interfaces.Collections
import com.delivero.driver.models.Token
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        navHostFragment=supportFragmentManager.findFragmentById(R.id.homeContainerView) as NavHostFragment
        navController=navHostFragment.navController
        appBarConfiguration= AppBarConfiguration.Builder(navController.graph).build()

        setupActionBarWithNavController(navController,appBarConfiguration)
        setUpMessaging()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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