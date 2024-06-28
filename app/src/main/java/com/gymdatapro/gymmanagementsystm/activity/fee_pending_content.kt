package com.gymdatapro.gymmanagementsystm.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.gymdatapro.gymmanagementsystm.FeeData
import com.gymdatapro.gymmanagementsystm.databinding.FragmentFeePendingContentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class fee_pending_content : DialogFragment() {
    private lateinit var binding: FragmentFeePendingContentBinding
    private var feeData: FeeData? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = LayoutInflater.from(requireActivity())
        binding = FragmentFeePendingContentBinding.inflate(inflater)
        builder.setView(binding.root)
        setupUI()
        setupListeners()
        retrieveData()
        return builder.create()
    }


    private fun setupUI() {

    }

    private fun setupListeners() {
        val cal = Calendar.getInstance()
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd/MM/yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                binding.edtJoining1.setText(sdf.format(cal.time))
            }

        binding.imgPicDate1.setOnClickListener {
            activity?.let {
                DatePickerDialog(
                    it,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        binding.spMemberShip1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = binding.spMemberShip1.selectedItem.toString().trim()
                if (value == "Select") {
                    binding.edtExpire1.setText("")
                } else {
                    if (binding.edtJoining1.text.toString().trim().isNotEmpty()) {
                        when (value) {
                            "1 Month" -> calculateExpireDate(1)
                            "3 Month" -> calculateExpireDate(3)
                            "6 Month" -> calculateExpireDate(6)
                            "1 Year" -> calculateExpireDate(12)
                            "3 Year" -> calculateExpireDate(36)
                        }
                    } else {
                        showToast("Select Joining date first")
                        binding.spMemberShip1.setSelection(0)
                    }
                }
                calculateTotal()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.edDiscount1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    try {
                        val discount = s.toString().toDouble()
                        if (discount < 0 || discount > 100) {
                            binding.edDiscount1.error = "Discount must be between 0 and 100"
                        } else {
                            calculateTotal()
                        }
                    } catch (e: NumberFormatException) {
                        binding.edDiscount1.error = "Invalid discount value"
                    }
                }
            }
        })
        binding.btnUpdatepop.setOnClickListener {
            if (validateInputs()) {
                updateDataInDatabase()
                dismiss()
            }
        }

        binding.btnCanclepop.setOnClickListener {

            dismiss()
        }
    }
    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.edtMobile.text.toString().trim().isEmpty()) {
            binding.edtMobile.error = "Mobile number is required"
            isValid = false
        }

        if (binding.edtJoining1.text.toString().trim().isEmpty()) {
            binding.edtJoining1.error = "Joining date is required"
            isValid = false
        }

        if (binding.spMemberShip1.selectedItem.toString().trim() == "Select") {
            showToast("Please select a membership type")
            isValid = false
        }

        if (binding.edtExpire1.text.toString().trim().isEmpty()) {
            binding.edtExpire1.error = "Expire date is required"
            isValid = false
        }

        if (binding.edDiscount1.text.toString().trim().isEmpty()) {
            binding.edDiscount1.error = "Discount is required"
            isValid = false
        }

        return isValid
    }

    private fun calculateTotal() {
        val month = binding.spMemberShip1.selectedItem.toString().trim()
        val discount = binding.edDiscount1.text.toString().toDoubleOrNull() ?: 0.0

        feeData?.let {
            val total = when (month) {
                "1 Month" -> it.onemonth?.toDouble() ?: 0.0
                "3 Month" -> it.threemonth?.toDouble() ?: 0.0
                "6 Month" -> it.sixmonth?.toDouble() ?: 0.0
                "1 Year" -> it.oneyear?.toDouble() ?: 0.0
                "3 Year" -> it.threeyear?.toDouble() ?: 0.0
                else -> 0.0
            }
            val discountAmount = (total * discount) / 100
            binding.edAmount1.setText((total - discountAmount).toString())
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun calculateExpireDate(month: Int) {
        val dtStart = binding.edtJoining1.text.toString().trim()
        if (dtStart.isNotEmpty()) {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            try {
                val date1 = format.parse(dtStart)
                val cal = Calendar.getInstance()
                date1?.let { cal.time = it }
                cal.add(Calendar.MONTH, month)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                binding.edtExpire1.setText(sdf.format(cal.time))
            } catch (e: ParseException) {
                e.printStackTrace()
                showToast("Please select the Date")
            }
        } else {
            showToast("Joining date is empty")
        }
    }

    private fun retrieveData() {
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("fee_update").child("1")
        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                feeData = snapshot.getValue(FeeData::class.java)
            } else {
                showToast("User not exist")
            }
        }.addOnFailureListener { exception ->
            Log.e("RetrieveData", "Failed to retrieve data", exception)
        }
    }

    private fun updateDataInDatabase() {
        // Fetch the data from the dialog's EditText and Spinner fields
        val mobileNumber = binding.edtMobile.text.toString()
        val DateOfJoining = binding.edtJoining1.text.toString()
        val membership = binding.spMemberShip1.selectedItem.toString()
        val expire = binding.edtExpire1.text.toString()
        val discount = binding.edDiscount1.text.toString().toDoubleOrNull() ?: 0.0
        val total = binding.edAmount1.text.toString()

        // Query the database to find the user with the provided mobile number
        val databaseReference = FirebaseDatabase.getInstance().getReference("Add_Member")
        val query = databaseReference.orderByChild("mobile").equalTo(mobileNumber)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        // Get the user ID of the user to update
                        val userId = userSnapshot.key

                        // Create a map containing the updated data
                        val updatedData = mapOf(
                            "mobileno" to mobileNumber,
                            "DateOfJoining" to DateOfJoining,
                            "membership" to membership,
                            "expire" to expire,
                            "discount" to discount.toString(), // Convert Double to String
                            "total" to total // Assuming totalAmount is already a String
                        )

                        // Update the user's data in the database
                        userId?.let {
                            databaseReference.child(it).updateChildren(updatedData)
                                .addOnSuccessListener {
                                    showToast("Data updated successfully")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("UpdateData", "Failed to update data", exception)
                                    showToast("Failed to update data")
                                }
                        }
                    }
                } else {
                    showToast("User not found. Please enter a valid mobile number or previous mobile number that you register.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to retrieve user data")
                Log.e("UpdateData", "Failed to retrieve user data", error.toException())
            }
        })
    }




    private fun showToast(value: String) {
        activity?.let {
            Toast.makeText(it, value, Toast.LENGTH_LONG).show()
        }
    }
}
