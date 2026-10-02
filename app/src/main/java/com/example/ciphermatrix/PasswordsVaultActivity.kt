package com.example.ciphermatrix

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// تأكد أنك مستورد الـ SoundManager
import com.example.ciphermatrix.SoundManager

class PasswordsVaultActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var credentialAdapter: CredentialAdapter
    private var fullCredentialsList: List<Credential> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passwords_vault)

        // 🔒 Auto-Lock on Minimize
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                val intent = Intent(this@PasswordsVaultActivity, VaultAuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        })

        credentialAdapter = CredentialAdapter(mutableListOf()) { id ->
            db.collection("users").document(uid).collection("vault").document(id).delete()
        }
        val rv = findViewById<RecyclerView>(R.id.rv_passwords)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = credentialAdapter

        db.collection("users").document(uid).collection("vault").addSnapshotListener { snapshots, _ ->
            fullCredentialsList = snapshots?.map { it.toObject(Credential::class.java).copy(id = it.id) } ?: emptyList()
            filterQuery(findViewById<EditText>(R.id.et_search).text.toString())
        }

        val etSearch = findViewById<EditText>(R.id.et_search)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filterQuery(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<View>(R.id.btn_add_password).setOnClickListener { showDialog() }
    }

    private fun filterQuery(text: String) {
        val query = text.lowercase().trim()
        val filteredList = if (query.isEmpty()) fullCredentialsList else fullCredentialsList.filter { it.acc.lowercase().contains(query) }
        credentialAdapter.updateList(filteredList)
    }

    private fun showDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_credential, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        val etAccountName = view.findViewById<TextInputEditText>(R.id.et_account_name)
        val etSecretPassword = view.findViewById<TextInputEditText>(R.id.et_secret_password)
        val btnGeneratePassword = view.findViewById<Button>(R.id.btn_generate_password)
        val btnEncryptSave = view.findViewById<Button>(R.id.btn_encrypt_save)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        //
        btnGeneratePassword.setOnClickListener {
            val securePass = generateStrongPassword()
            etSecretPassword.setText(securePass)

            // sound manager
            SoundManager.playSound(this, R.raw.matrix_generation)

            Toast.makeText(this, "Secure password generated!", Toast.LENGTH_SHORT).show()
        }

        btnEncryptSave.setOnClickListener {
            val acc = etAccountName.text.toString()
            val pass = etSecretPassword.text.toString()
            if (acc.isNotEmpty() && pass.isNotEmpty()) {
                db.collection("users").document(uid).collection("vault")
                    .add(hashMapOf("acc" to acc, "pass" to CryptoHelper.encrypt(pass)))
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun generateStrongPassword(): String {
        val allChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[{]};:,.<>?"
        return (1..16).map { allChars.random() }.joinToString("")
    }
}