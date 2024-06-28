package com.gymdatapro.gymmanagementsystm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class fee_pending_main : Fragment() {
    private lateinit var dbref: DatabaseReference
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<AddMember>
    private lateinit var filteredList: ArrayList<AddMember>
    private lateinit var adapter: MyAdapter
    private lateinit var searchEditText: EditText
    private lateinit var noDataFoundTextView: TextView
    private val SMS_PERMISSION_REQUEST_CODE = 123

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fee_pending_main, container, false)
        userRecyclerView = view.findViewById(R.id.recyclerView)
        searchEditText = view.findViewById(R.id.edtPendingSearch)
        noDataFoundTextView = view.findViewById(R.id.txtPendingNDF)

        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userArrayList = arrayListOf()
        filteredList = arrayListOf()
        adapter = MyAdapter(requireContext(), filteredList) // Initialize the adapter
        userRecyclerView.adapter = adapter // Set the adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }
        })

        getUserData()
        return view
    }

    private fun getUserData() {
        dbref = FirebaseDatabase.getInstance().getReference("Add_Member")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userArrayList.clear() // Clear the existing data
                val currentDate = getCurrentDate()

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(AddMember::class.java)

                    if (user != null && isExpiryDatePassed(user.Expire.toString(), currentDate)) {
                        // Send SMS before adding the user to the list
                        sendSMS(user.Mobile.toString())
                        // Add the user to the list
                        userArrayList.add(user)
                    }
                }

                filter(searchEditText.text.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun filter(text: String) {
        filteredList.clear()
        for (user in userArrayList) {
            if (user.Mobile!!.contains(text)) {
                filteredList.add(user)
            }
        }

        adapter.notifyDataSetChanged()

        if (filteredList.isEmpty()) {
            userRecyclerView.visibility = View.GONE
            noDataFoundTextView.visibility = View.VISIBLE
        } else {
            userRecyclerView.visibility = View.VISIBLE
            noDataFoundTextView.visibility = View.GONE
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        return dateFormat.format(currentDate)
    }

    private fun isExpiryDatePassed(expiryDate: String, currentDate: String): Boolean {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val expiry = dateFormat.parse(expiryDate)
        val current = dateFormat.parse(currentDate)
        return expiry != null && current != null && expiry.before(current)
    }

    private fun sendSMS(phoneNumber: String) {
        if (!isAdded || context == null) return

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_REQUEST_CODE)
        } else {
            val message = "Your gym subscription has expired. Please renew it."
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(requireContext(), "SMS sent!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "SMS permission granted.", Toast.LENGTH_SHORT).show()
        }
    }
}
