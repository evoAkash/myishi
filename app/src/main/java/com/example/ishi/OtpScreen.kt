package com.example.ishi


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import kotlinx.android.synthetic.main.activity_otp_screen.*
import java.util.concurrent.TimeUnit


class OtpScreen : AppCompatActivity() {
    //These are the objects needed
    //It is the verification id that will be sent to the user
    lateinit var mVerificationId: String

    //The edittext to input the code
    lateinit var editTextCode: EditText

    //firebase auth object
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_screen)
        //initializing objects

        //initializing objects
        mAuth = FirebaseAuth.getInstance()
        editTextCode=findViewById(R.id.editTextCode)


        //getting mobile number from the previous activity
        //and sending the verification code to the number


        //getting mobile number from the previous activity
        //and sending the verification code to the number
        val intent = intent
        val mobile = intent.getStringExtra("mobile")
        sendVerificationCode(mobile)
        //if the automatic sms detection did not work, user can also enter the code manually
        //so adding a click listener to the button
       buttonSignIn.setOnClickListener{

       }

    }fun verifyotp(){
        val code = editTextCode.text.toString().trim { it <= ' ' }
        if (code.isEmpty() || code.length < 6) {
            editTextCode.setError("Enter valid code")
            editTextCode.requestFocus()
            return
        }
        //verifying the code entered manually
        verifyVerificationCode(code)



    }

    //the method is sending verification code
    //the country id is concatenated
    //you can take the country id as user input as well
    private fun sendVerificationCode(mobile: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91$mobile",
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mCallbacks)

    }
    private val mCallbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                //Getting the code sent by SMS
                val code = phoneAuthCredential.smsCode

                //sometime the code is not detected automatically
                //in this case the code will be null
                //so user has to manually enter the code
                if (code != null) {
                    editTextCode.setText(code)
                    //verifying the code
                    verifyVerificationCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@OtpScreen, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                s: String,
                forceResendingToken: ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                mVerificationId = s

            }
        }
    private fun verifyVerificationCode(otp: String) {
        //creating the credential
        val credential = PhoneAuthProvider.getCredential(mVerificationId, otp)

        //signing the user
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this@OtpScreen,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        //verification successful we will start the profile activity
                        val intent =
                            Intent(this@OtpScreen, HomeScreen::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {

                        //verification unsuccessful.. display an error message
                        var message =
                            "Somthing is wrong, we will fix it soon..."
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            message = "Invalid code entered..."
                        }
                        val snackbar: Snackbar = Snackbar.make(
                            findViewById(R.id.progressbar),
                            message,
                            Snackbar.LENGTH_LONG
                        )
                        snackbar.setAction("Dismiss",null)
                        snackbar.show()
                    }
                })
    }



}
