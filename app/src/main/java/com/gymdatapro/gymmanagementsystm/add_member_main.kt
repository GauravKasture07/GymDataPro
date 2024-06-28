package com.gymdatapro.gymmanagementsystm

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gymdatapro.gymmanagementsystm.databinding.FragmentAddMemberMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class add_member_main : Fragment() {

    private lateinit var binding: FragmentAddMemberMainBinding
    private var selectedImageUri: Uri? = null
    private var feeData: FeeData? = null
    private var gender = "Male"

    companion object {
        private const val IMAGE_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
        private const val GALLERY_REQUEST_CODE = 102
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddMemberMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imgPicDate.setOnClickListener {
            activity?.let {
                val cal = Calendar.getInstance()
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH)
                val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    it,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        cal.set(Calendar.YEAR, year)
                        cal.set(Calendar.MONTH, monthOfYear)
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        val myFormat = "dd/MM/yyyy"
                        val sdf = SimpleDateFormat(myFormat, Locale.US)
                        binding.edtJoining.setText(sdf.format(cal.time))
                    },
                    year,
                    month,
                    dayOfMonth
                )
                datePickerDialog.show()
            }
        }

        binding.spMemberShip.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val value = binding.spMemberShip.selectedItem.toString().trim()

                if (value == "Select") {
                    binding.edtExpire.setText("")
                    calculateTotal(binding.spMemberShip, binding.edDiscount, binding.edAmount)
                } else {
                    if (binding.edtJoining.text.toString().trim().isNotEmpty()) {
                        when (value) {
                            "1 Month" -> calculateExpireDate(1, binding.edtExpire)
                            "3 Month" -> calculateExpireDate(3, binding.edtExpire)
                            "6 Month" -> calculateExpireDate(6, binding.edtExpire)
                            "1 Year" -> calculateExpireDate(12, binding.edtExpire)
                            "3 Year" -> calculateExpireDate(36, binding.edtExpire)
                        }
                        calculateTotal(binding.spMemberShip, binding.edDiscount, binding.edAmount)
                    } else {
                        showToast("Select Joining date first")
                        binding.spMemberShip.setSelection(0)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.edDiscount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    calculateTotal(binding.spMemberShip, binding.edDiscount, binding.edAmount)
                }
            }
        })

        binding.imgTakeImage.setOnClickListener {
            pickImage()
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rdMale -> {
                    gender = "Male"
                    Log.d("Gender", "Male selected")
                }
                R.id.rdFemale -> {
                    gender = "Female"
                    Log.d("Gender", "Female selected")
                }
            }
        }

        binding.btnAddMember.setOnClickListener {
            if (validate()) {
                val FName = binding.edFirstName.text.toString()
                val LName = binding.edLastName.text.toString()
                val Age = binding.edAge.text.toString()
                val Gender = gender
                val Weight = binding.edWeight.text.toString()
                val DateOfJoining = binding.edtJoining.text.toString()
                val Mobile = binding.edMobile.text.toString()
                val Discount = binding.edDiscount.text.toString()
                val Total = binding.edAmount.text.toString()
                val Image = selectedImageUri.toString()
                insertUserDataToFirebase(
                    FName,
                    LName,
                    Age,
                    Gender,
                    Weight,
                    DateOfJoining,
                    binding.spMemberShip.selectedItem.toString(),
                    Mobile,
                    binding.edtExpire.text.toString(),
                    Discount,
                    Total,
                    Image
                )
            }
        }

        retrieveData(userid = "1")
    }

    private fun insertUserDataToFirebase(
        FName: String,
        LName: String,
        Age: String,
        Gender: String,
        Weight: String,
        DateOfJoining: String,
        Membership: String,
        Mobile: String,
        Expire: String,
        Discount: String,
        Total: String,
        Image: String
    ) {
        Log.d("InsertUserData", "Gender before insertion: $Gender")
        val userId = FirebaseDatabase.getInstance().getReference("Add_Member").push().key
        if (userId != null) {
            val addMember = AddMember(
                id = userId,
                FName = FName,
                LName = LName,
                Age = Age,
                Gender = Gender,
                Weight = Weight,
                DateOfJoining = DateOfJoining,
                Membership = Membership,
                Mobile = Mobile,
                Expire = Expire,
                Discount = Discount,
                Total = Total,
                Image = Image
            )
            FirebaseDatabase.getInstance().getReference("Add_Member").child(userId).setValue(addMember)
                .addOnSuccessListener {
                    binding.edFirstName.text.clear()
                    binding.edLastName.text.clear()
                    binding.edAge.text.clear()
                    binding.edWeight.text.clear()
                    binding.edMobile.text.clear()
                    binding.edtJoining.text.clear()
                    binding.edAmount.text.clear()
                    binding.edtJoining.text.clear()
                    binding.imgPicDate.setImageURI(null)
                    binding.spMemberShip.setSelection(0)
                    showToast("Data sent successfully")
                }
                .addOnFailureListener {
                    showToast("Failed to send data")
                }
        } else {
            showToast("Failed to generate user ID")
        }
    }

    private fun pickImage() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose your profile picture")

        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(takePicture, CAMERA_REQUEST_CODE)
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.CAMERA),
                            CAMERA_PERMISSION_CODE
                        )
                    }
                }
                options[item] == "Choose from Gallery" -> {
                    val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE)
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        })
        builder.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    selectedImageUri = saveImageToInternalStorage(bitmap)
                    if (selectedImageUri != null) {
                        Log.d("ImageURI", "Image URI: $selectedImageUri")
                        binding.imgpic.setImageURI(selectedImageUri)
                    } else {
                        showToast("Failed to save image")
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        Log.d("ImageURI", "Image URI: $selectedImageUri")
                        binding.imgpic.setImageURI(selectedImageUri)
                    } else {
                        showToast("Failed to get image from gallery")
                    }
                }
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri? {
        val filename = "profile_image_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, filename)

        return try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePicture, CAMERA_REQUEST_CODE)
            } else {
                showToast("Camera permission denied")
            }
        }
    }

    private fun retrieveData(userid:String) {
        if (isAdded && context != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("fee_update")

            databaseReference.child(userid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val feeDataMap = dataSnapshot.value as? Map<String, Any>

                        if (feeDataMap != null) {
                            feeData = FeeData(
                                id = feeDataMap["id"] as? String ?: "",
                                onemonth = feeDataMap["onemonth"] as? String ?: "",
                                threemonth = feeDataMap["threemonth"] as? String ?: "",
                                sixmonth = feeDataMap["sixmonth"] as? String ?: "",
                                oneyear = feeDataMap["oneyear"] as? String ?: "",
                                threeyear = feeDataMap["threeyear"] as? String ?: ""
                            )

                            Log.d("retrieveData", "Fee data retrieved successfully: $feeData")

                            // Now you can use the feeData object as needed
                            // For example, update UI elements with the fee data
                        } else {
                            Log.e("retrieveData", "Failed to retrieve fee data: Data is null")
                            showToast("Failed to retrieve fee data")
                        }
                    } else {
                        Log.e("retrieveData", "No fee data found")
                        showToast("No fee data found")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                    Log.e("retrieveData", "Failed to retrieve fee data: ${databaseError.message}")
                    showToast("Failed to retrieve fee data")
                }
            })
        }
    }

    private fun calculateExpireDate(monthsToAdd: Int, dateField: EditText) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        try {
            cal.time = sdf.parse(binding.edtJoining.text.toString().trim())
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        cal.add(Calendar.MONTH, monthsToAdd)
        dateField.setText(sdf.format(cal.time))
    }

    private fun calculateTotal(spMember: Spinner, edtDis: EditText, edtAmt: EditText) {
        val month = spMember.selectedItem.toString().trim()
        var discount = edtDis.text.toString().trim()

        if (edtDis.text.toString().isEmpty()) {
            discount = "0"
        }
        if (feeData != null) {
            when (month) {
                "Select" -> edtAmt.setText("")
                "1 Month" -> {
                    if (feeData?.onemonth!!.trim().isNotEmpty()) {
                        val discountAmount = (feeData?.onemonth!!.toDouble() * discount.toDouble()) / 100
                        val total = feeData?.onemonth!!.toDouble() - discountAmount
                        edtAmt.setText(total.toString())
                    }
                }
                "3 Month" -> {
                    if (feeData?.threemonth!!.trim().isNotEmpty()) {
                        val discountAmount = (feeData?.threemonth!!.toDouble() * discount.toDouble()) / 100
                        val total = feeData?.threemonth!!.toDouble() - discountAmount
                        edtAmt.setText(total.toString())
                    }
                }
                "6 Month" -> {
                    if (feeData?.sixmonth!!.trim().isNotEmpty()) {
                        val discountAmount = (feeData?.sixmonth!!.toDouble() * discount.toDouble()) / 100
                        val total = feeData?.sixmonth!!.toDouble() - discountAmount
                        edtAmt.setText(total.toString())
                    }
                }
                "1 Year" -> {
                    if (feeData?.oneyear!!.trim().isNotEmpty()) {
                        val discountAmount = (feeData?.oneyear!!.toDouble() * discount.toDouble()) / 100
                        val total = feeData?.oneyear!!.toDouble() - discountAmount
                        edtAmt.setText(total.toString())
                    }
                }
                "3 Year" -> {
                    if (feeData?.threeyear!!.trim().isNotEmpty()) {
                        val discountAmount = (feeData?.threeyear!!.toDouble() * discount.toDouble()) / 100
                        val total = feeData?.threeyear!!.toDouble() - discountAmount
                        edtAmt.setText(total.toString())
                    }
                }
            }
        }
    }
    private fun validate(): Boolean {
        var valid = true

        if (binding.edFirstName.text.toString().trim().isEmpty()) {
            binding.edFirstName.error = "Required"
            valid = false
        }

        if (binding.edLastName.text.toString().trim().isEmpty()) {
            binding.edLastName.error = "Required"
            valid = false
        }

        if (binding.edAge.text.toString().trim().isEmpty()) {
            binding.edAge.error = "Required"
            valid = false
        }

        if (binding.edWeight.text.toString().trim().isEmpty()) {
            binding.edWeight.error = "Required"
            valid = false
        }

        if (binding.edMobile.text.toString().trim().isEmpty()) {
            binding.edMobile.error = "Required"
            valid = false
        }

        if (binding.edtJoining.text.toString().trim().isEmpty()) {
            binding.edtJoining.error = "Required"
            valid = false
        }

        if (binding.spMemberShip.selectedItem.toString() == "Select") {
            showToast("Please select a membership")
            valid = false
        }

        if (binding.edDiscount.text.toString().trim().isEmpty()) {
            binding.edDiscount.setText("0")
        }

        return valid
    }
}
