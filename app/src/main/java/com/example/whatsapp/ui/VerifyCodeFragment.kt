package com.example.whatsapp.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.whatsapp.R
import com.example.whatsapp.databinding.FragmentVerifyCodeBinding
import com.example.whatsapp.ui.AuthorizationFragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class VerifyCodeFragment : BaseFragment<FragmentVerifyCodeBinding>() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore
    private lateinit var fullPhoneNumber: String
    private var verificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    override fun getViewBinding(): FragmentVerifyCodeBinding {
        return FragmentVerifyCodeBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: VerifyCodeFragmentArgs by navArgs()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        fullPhoneNumber = args.phoneNumber
        if (fullPhoneNumber.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Phone number missing, please try again.",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
            return
        }

        sendVerificationCode(fullPhoneNumber)

        binding.btnNext.setOnClickListener {
            val otpCode = binding.otpView.otp
            if (otpCode.length == 6 && !verificationId.isNullOrEmpty()) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(requireContext(), "Enter a valid 6-digit OTP", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(
                        requireContext(),
                        "Verification failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@VerifyCodeFragment.verificationId = verificationId
                    this@VerifyCodeFragment.resendToken = token
                    Toast.makeText(
                        requireContext(),
                        "Verification code sent to $phoneNumber",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    val uid = currentUser?.uid
                    val phoneNumber = currentUser?.phoneNumber

                    val map = hashMapOf("uid" to uid, "number" to phoneNumber)

                    firestore.collection("users").document(uid.toString()).set(map)
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "User added successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Failed to save user",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    Toast.makeText(requireContext(), "Signed in successfully", Toast.LENGTH_LONG)
                        .show()
                    findNavController().navigate(R.id.action_verifyCodeFragment_to_mainFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Invalid OTP. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}