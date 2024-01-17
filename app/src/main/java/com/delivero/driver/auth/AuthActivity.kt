package com.delivero.driver.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.delivero.driver.MainActivity
import com.delivero.driver.R
import com.delivero.driver.databinding.ActivityAuthBinding
import com.delivero.driver.helpers.Preference
import com.delivero.driver.helpers.toast
import com.delivero.driver.interfaces.Collections
import com.delivero.driver.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {
    private var showSplash=true
    private lateinit var binding: ActivityAuthBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash=installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition{
            showSplash
        }
        binding= ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        val navHostFragment=supportFragmentManager.findFragmentById(R.id.authContainerView) as NavHostFragment
        navController=navHostFragment.navController
        appBarConfiguration= AppBarConfiguration.Builder(navController.graph).build()
        setupActionBarWithNavController(navController,appBarConfiguration)
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        if (Firebase.auth.currentUser==null){
            showSplash=false
            return
        }
        getCurrentUser()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)||super.onSupportNavigateUp()
    }

    private fun getCurrentUser() {
        Firebase.firestore.collection(Collections.USERS)
            .document(Firebase.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val user=it.toObject(User::class.java)
                    if (user!!.roles.contains("Driver")){
                        Preference.saveUser(this,user)
                        showSplash=false
                        startActivity(Intent(this, MainActivity::class.java))
                    }else{
                        showSplash=false
                        val bundle = Bundle()
                        bundle.putSerializable("user", user)
                        navController.navigate(R.id.riderDetailsFragment,bundle)
                    }

                }else{
                    showSplash=false
                }
            }.addOnFailureListener {
                showSplash=false
toast("Failed to get user. Please Log in again")
            }
    }
}