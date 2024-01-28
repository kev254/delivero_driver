package com.delivero.driver.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.delivero.driver.MainActivity
import com.delivero.driver.databinding.FragmentRegisterBinding
import com.delivero.driver.helpers.Preference
import com.delivero.driver.helpers.Utils
import com.delivero.driver.helpers.isValidEmail
import com.delivero.driver.interfaces.Collections
import com.delivero.driver.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FragmentRegister:Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private var utils= Utils()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
       binding=FragmentRegisterBinding.inflate(inflater, container, false)

        binding.logIn.setOnClickListener {
            getInput()
        }
        binding.signUp.setOnClickListener {
            findNavController().navigate(FragmentRegisterDirections.actionFragmentRegisterToFragmentLogin())

        }
        return binding.root
    }
    private fun getInput(){
        val email=binding.emailInput.text.toString()
        val password=binding.passwordInput.text.toString()
        val name=binding.nameInput.text.toString()
        val phoneNumber=binding.phoneInput.text.toString()

        if (email.isEmpty() || !email.isValidEmail()){
            utils.showMessageDialog(requireContext(),"You missed something","Please provide the email address")
            return
        }
        if (password.isEmpty() || password.length<6){
            utils.showMessageDialog(requireContext(),"You missed something","The password must be at least 6 characters")
            return
        }

        if (name.isEmpty()){
            utils.showMessageDialog(requireContext(),"You missed something","Please provide your name")
            return
        }
        if (phoneNumber.isEmpty() || phoneNumber.length!=10 || !phoneNumber.isDigitsOnly()){
            utils.showMessageDialog(requireContext(),"You missed something","Please provide a valid phone number")
            return
        }

        val user= User()
        user.email=email
        user.phoneNumber=phoneNumber
        user.userName=name
        user.status="Active"
        user.roles= listOf("Customer")

        binding.progress.show()
        Firebase.auth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                user.userId=it.user!!.uid
               Firebase.firestore.collection(Collections.USERS)
                   .document(it.user!!.uid)
                   .set(user)
                   .addOnSuccessListener {
                      findNavController().navigate(FragmentRegisterDirections.actionFragmentRegisterToRiderDetailsFragment(user))
                   }.addOnFailureListener {
                       binding.progress.hide()
                       utils.showMessageDialog(requireContext(),"Could not create account","${it.message}")
                   }
            }.addOnFailureListener {
                binding.progress.hide()
                utils.showMessageDialog(requireContext(),"Could not create account","${it.message}")

            }

    }
}