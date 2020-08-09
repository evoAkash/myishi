package com.example.ishi

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
lateinit var editTextMobile: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonContinue.setOnClickListener{conti()}
        editTextMobile=findViewById(R.id.editTextMobile)


    }fun conti(){
        val mobile = editTextMobile.text.toString().trim { it <= ' ' }
        if(mobile.isEmpty()|| mobile.length < 10){
            editTextMobile.setError("Enter a valid mobile");
            editTextMobile.requestFocus();
            return;

        }
        val intent = Intent(this@MainActivity, OtpScreen::class.java)
        intent.putExtra("mobile", mobile)
        startActivity(intent)
    }
}
