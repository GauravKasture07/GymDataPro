package com.gymdatapro.gymmanagementsystm

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class home_main : Fragment() {
    private lateinit var dbref: DatabaseReference
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<AddMember>
    private lateinit var filteredList: ArrayList<AddMember>
    private lateinit var searchEditText: EditText
    private lateinit var noDataFoundTextView: TextView
    private lateinit var adapter: MyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_main, container, false)
        userRecyclerView = view.findViewById(R.id.userList)
        searchEditText = view.findViewById(R.id.edtPendingSearch)
        noDataFoundTextView = view.findViewById(R.id.txtPendingNDF)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userArrayList = arrayListOf()
        filteredList = arrayListOf()
        adapter = MyAdapter(requireContext(), userArrayList) // Initialize the adapter
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

                    if (user != null && !isExpiryDatePassed(user.Expire.toString(), currentDate)) {
                        // Only add members whose expiration date has not passed
                        userArrayList.add(user)
                    }
                }

                adapter.notifyDataSetChanged() // Notify the adapter that data set has changed
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
    private fun filter(text: String) {
        filteredList.clear()

        if (text.isEmpty()) {
            filteredList.addAll(userArrayList) // Show all items if search text is empty
        } else {
            for (user in userArrayList) {
                if (user.Mobile!!.contains(text)) {
                    filteredList.add(user)
                    break // Stop after finding the first matching mobile number
                }
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
}
