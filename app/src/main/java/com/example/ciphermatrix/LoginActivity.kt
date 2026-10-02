package com.example.ciphermatrix

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etName = findViewById<EditText>(R.id.et_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val tvError = findViewById<TextView>(R.id.tv_error)

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { goToWelcome() }
                .addOnFailureListener { e -> tvError.text = e.message }
        }

        findViewById<Button>(R.id.btn_register).setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    auth.currentUser?.updateProfile(profileUpdate)
                        ?.addOnCompleteListener { goToWelcome() }
                        ?: goToWelcome()
                }
                .addOnFailureListener { e -> tvError.text = e.message }
        }
    }

    private fun goToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}