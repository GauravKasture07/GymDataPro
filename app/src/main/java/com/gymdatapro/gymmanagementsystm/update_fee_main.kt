package com.gymdatapro.gymmanagementsystm

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gymdatapro.gymmanagementsystm.databinding.FragmentUpdateFeeMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class update_fee_main : Fragment() {
    private lateinit var binding: FragmentUpdateFeeMainBinding
    private val databaseReference = FirebaseDatabase.getInstance().getReference("fee_update")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpdateFeeMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.updateFeeButton.setOnClickListener {

            if (validate()) {
                val oneMonth = binding.oneMonthFee.text.toString()
                val threeMonth = binding.ThreeMonthFee.text.toString()
                val sixMonth = binding.SixMonthFee.text.toString()
                val oneYear = binding.oneYearFee.text.toString()
                val threeYear = binding.ThreeYearFee.text.toString()

                insertUserDataToFirebase(oneMonth, threeMonth, sixMonth, oneYear, threeYear)
            }
        }
        retrieveFeeData(userId = "1")
    }
    private fun validate(): Boolean {
        if (binding.oneMonthFee.text.toString().trim().isEmpty()) {
            showToast("Enter one month fee")
            return false
        } else if (binding.ThreeMonthFee.text.toString().trim().isEmpty()) {
            showToast("Enter three month fee")
            return false
        } else if (binding.SixMonthFee.text.toString().trim().isEmpty()) {
            showToast("Enter six month fee")
            return false
        } else if (binding.oneYearFee.text.toString().trim().isEmpty()) {
            showToast("Enter one Year fee")
            return false
        } else if (binding.ThreeYearFee.text.toString().trim().isEmpty()) {
            showToast("Enter Three Year fee")
            return false
        }
        return true
    }

    private fun showToast(value: String) {
        Toast.makeText(requireActivity(), value, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun insertUserDataToFirebase(
        oneMonth: String,
        threeMonth: String,
        sixMonth: String,
        oneYear: String,
        threeYear: String
    ) {

        val userId = "1"

        val feeData = FeeData(
            id = userId,
            onemonth = oneMonth,
            threemonth = threeMonth,
            sixmonth = sixMonth,
            oneyear = oneYear,
            threeyear = threeYear
        )
            databaseReference.child(userId).setValue(feeData)
                .addOnSuccessListener {
                    binding.oneMonthFee.text.clear()
                    binding.ThreeMonthFee.text.clear()
                    binding.SixMonthFee.text.clear()
                    binding.oneYearFee.text.clear()
                    binding.ThreeYearFee.text.clear()
                    showToast("Data sent successfully")
                }
                .addOnFailureListener {

                    showToast("Failed to send data")
                }
    }
    private fun retrieveFeeData(userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("fee_update")

        databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val feeData = dataSnapshot.getValue(FeeData::class.java)

                if (feeData != null) {
                    binding.updateoneMonthFee.text = feeData.onemonth
                    binding.updateThreeMonthFee.text = feeData.threemonth
                    binding.updateSixMonthFee.text = feeData.sixmonth
                    binding.updateoneYearFee.text = feeData.oneyear
                    binding.updateThreeYearFee.text = feeData.threeyear
                } else {
                    Log.e(TAG, "No data found for userId: $userId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Log.e(TAG, "Failed to retrieve fee data: ${databaseError.message}")
            }
        })
    }
}