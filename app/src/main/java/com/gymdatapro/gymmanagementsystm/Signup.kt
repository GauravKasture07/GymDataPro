package com.gymdatapro.gymmanagementsystm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.gymdatapro.gymmanagementsystm.activity.LoginActivity
import com.gymdatapro.gymmanagementsystm.databinding.ActivitySignupBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Signup : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseDatabase=FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.reference.child("users")
        binding.signupSubmitLogin.setOnClickListener{
            val signupUsername=binding.signEnterMobile.text.toString()
            val signupPassword=binding.singupEnterPassword.text.toString()
            if(signupUsername.isNotEmpty() && signupPassword.isNotEmpty()){
                signupUser(signupUsername,signupPassword)
            }else{
                Toast.makeText(this@Signup,"All field is mandatory",Toast.LENGTH_SHORT).show()
            }
        }
        binding.loginRedirect.setOnClickListener {
            startActivity(Intent(this@Signup,LoginActivity::class.java))
            finish()
        }
    }
    private fun signupUser(username:String,password:String){
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!dataSnapshot.exists()){
                    val id=databaseReference.push().key
                    val userData=UserData(id,username,password)
                    databaseReference.child(id!!).setValue(userData)
                    Toast.makeText(this@Signup,"Signup successful",Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@Signup,LoginActivity::class.java))
                    finish()
                }else{
                    Toast.makeText(this@Signup,"User already exists",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Signup,"Database Error:${databaseError.message}",Toast.LENGTH_SHORT).show()
            }
        })
    }
}