package com.delivero.driver.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.delivero.driver.helpers.Preference
import com.delivero.driver.helpers.Utils
import com.delivero.driver.MainActivity
import com.delivero.driver.R
import com.delivero.driver.databinding.FragmentLoginBinding
import com.delivero.driver.helpers.isValidEmail
import com.delivero.driver.interfaces.Collections
import com.delivero.driver.models.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FragmentLogin:Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private var utils= Utils()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
       binding=FragmentLoginBinding.inflate(inflater, container, false)

        binding.logIn.setOnClickListener {
            getInput()
        }
        binding.signUp.setOnClickListener {
            findNavController().navigate(FragmentLoginDirections.actionFragmentLoginToFragmentRegister())
        }
        return binding.root
    }

    private fun getInput() {
        val email=binding.emailInput.text.toString()
        val password=binding.passwordInput.text.toString()
        if (email.isEmpty() || !email.isValidEmail()){
            utils.showMessageDialog(requireContext(),"You missed something","Please provide the email address")
            return
        }
        if (password.isEmpty() || password.length<6){
            utils.showMessageDialog(requireContext(),"You missed something","The password must be at least 6 characters")
            return

        }

        binding.progress.show()
        Firebase.auth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                getUser(it.user!!.uid)
            }.addOnFailureListener {
                binding.progress.hide()
                if (it.message?.contains("no user record") == true){
                    MaterialAlertDialogBuilder(requireContext())
                        .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_white_rounded_10))
                        .setTitle("User Record Not Found")
                        .setMessage("Would you like to create an account?")
                        .setPositiveButton("Create Account"){
                                dialog,_->
                            dialog?.dismiss()
                            findNavController().navigate(FragmentLoginDirections.actionFragmentLoginToFragmentRegister())
                        }.setNegativeButton("Cancel"){
                                dialog,_->
                            dialog?.dismiss()
                        }.create()
                        .show()
                }else{
                    utils.showMessageDialog(requireContext(),"Could not log in","${it.message}")
                }
                Log.e("Failed to log in:"," ${it.message}")
            }
    }


    private fun getUser(uid:String){
        Firebase.firestore.collection(Collections.USERS)
            .document(uid)
            .get()
            .addOnSuccessListener {
                binding.progress.hide()
                if (it.exists()){
                    val user=it.toObject(User::class.java)
                    if (user!!.roles.contains("Driver")) {
                        Preference.saveUser(requireContext(), user!!)
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    }else{
                        findNavController().navigate(FragmentLoginDirections.actionFragmentLoginToRiderDetailsFragment(user))
                    }
                }else{
                    MaterialAlertDialogBuilder(requireContext())
                        .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_white_rounded_10))
                        .setTitle("User Record Not Found")
                        .setMessage("Would you like to create an account?")
                        .setPositiveButton("Create Account"){
                            dialog,_->
                            dialog?.dismiss()
                            findNavController().navigate(FragmentLoginDirections.actionFragmentLoginToFragmentRegister())
                        }.setNegativeButton("Cancel"){
                            dialog,_->
                            dialog?.dismiss()
                        }.create()
                        .show()
                }
            }.addOnFailureListener {
                binding.progress.hide()
                Log.e("Failed to get User","${it.message}")
                utils.showMessageDialog(requireContext(),"Failed to Log In","Please check your connection and try again")
            }
    }
}