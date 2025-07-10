package com.example.whatsapp.ui

import android.content.SharedPreferences
import android.os.Bundle
import com.example.whatsapp.R
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.whatsapp.databinding.FragmentAuthorizationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AuthorizationFragment : BaseFragment<FragmentAuthorizationBinding>() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        binding.ccp.registerCarrierNumberEditText(binding.etNumber)
        binding.ccp.setBackgroundColor(android.graphics.Color.WHITE)

        binding.ivNext.setOnClickListener {
            binding.tvCountryCode.text = ""
            binding.ccp.launchCountrySelectionDialog()
        }

        binding.ccp.setOnCountryChangeListener {
            val countryName = binding.ccp.selectedCountryName
            val countryCode = binding.ccp.selectedCountryCode
            binding.tvCountryName.text = countryName
            binding.tvCountryCode.text = "+$countryCode"
            binding.tvCountryName.isVisible = true
            binding.ivNext.isVisible = true
        }

        binding.btnRegister.setOnClickListener {
            val fullPhoneNumber = binding.ccp.fullNumberWithPlus

            if (binding.ccp.isValidFullNumber) {
                findNavController().navigate(AuthorizationFragmentDirections.actionAuthorizationFragmentToVerifyCodeFragment(fullPhoneNumber))
            } else {
                Toast.makeText(requireContext(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getViewBinding(): FragmentAuthorizationBinding {
        return FragmentAuthorizationBinding.inflate(layoutInflater)
    }
}
