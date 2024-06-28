package com.gymdatapro.gymmanagementsystm.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.gymdatapro.gymmanagementsystm.Mainscreen
import com.gymdatapro.gymmanagementsystm.R
import com.gymdatapro.gymmanagementsystm.UserData
import com.gymdatapro.gymmanagementsystm.databinding.ActivityLoginBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        binding.submitLogin.setOnClickListener {
            val loginUsername = binding.enterMobile.text.toString().trim()
            val loginPassword = binding.enterPassword.text.toString().trim()
            if (loginUsername.isNotEmpty() && loginPassword.isNotEmpty()) {
                loginUser(loginUsername, loginPassword)
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "All fields are mandatory",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.forgetPass.setOnClickListener {
            showForgetPasswordDialog()
        }
    }

    private fun showForgetPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Forget Password")

        // Set up the input fields
        val view = layoutInflater.inflate(R.layout.dialog_forget_password, null)
        val usernameEditText = view.findViewById<EditText>(R.id.editTextUsername)
        val emailEditText = view.findViewById<EditText>(R.id.editTextEmail)
        builder.setView(view)

        // Set up the buttons
        builder.setPositiveButton("Reset") { dialog, _ ->
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            // Retrieve password from database
            databaseReference.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val userData = dataSnapshot.children.first().getValue(UserData::class.java)
                            val password = userData?.password ?: "Password not found"

                            // Send password to email
                            sendPasswordToEmail(email, password)
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Username not found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Database Error: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun sendPasswordToEmail(email: String, password: String) {
        // You can use your preferred method to send an email with the password
        // Here, we are just displaying the password in a Toast message
        Toast.makeText(
            this,
            "Your password is: $password",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun loginUser(username: String, password: String) {
        databaseReference.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userData = userSnapshot.getValue(UserData::class.java)
                            if (userData != null && userData.password == password) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        Mainscreen::class.java
                                    )
                                )
                                finish()
                                return
                            }
                        }
                    }
                    Toast.makeText(
                        this@LoginActivity,
                        "Login Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Database Error:${databaseError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
