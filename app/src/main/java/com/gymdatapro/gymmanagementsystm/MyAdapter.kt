package com.gymdatapro.gymmanagementsystm

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gymdatapro.gymmanagementsystm.activity.fee_pending_content
import com.google.firebase.database.FirebaseDatabase

class MyAdapter(
    val c: Context,
    private val userList: ArrayList<AddMember>,
) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = userList[position]
        Glide.with(holder.itemView.context)
            .load(currentitem.Image)
            .error(R.drawable.form_image)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Image loaded successfully
                    return false
                }
            })
            .into(holder.cardImage)

        // Set text views
        holder.cardFirst.text = currentitem.FName
        holder.cardLast.text = currentitem.LName
        holder.cardGender.text = currentitem.Gender
        holder.cardAge.text = currentitem.Age
        holder.cardWeight.text = currentitem.Weight
        holder.cardMobile.text = currentitem.Mobile
        holder.cardJoining.text = currentitem.DateOfJoining
        holder.cardMember.text = currentitem.Membership
        holder.cardExpire.text = currentitem.Expire
        holder.cardDiscount.text = currentitem.Discount
        holder.cardTotal.text = currentitem.Total

        // Set onClickListeners for buttons
        holder.btnDelete.setOnClickListener {
            popupMenus(position)
        }
        holder.btnCall.setOnClickListener {
            currentitem.Mobile?.let { it1 -> makeCall(it1) }
        }
        holder.btnUpdate.setOnClickListener {
            // Show the update dialog box
            showUpdateDialog()
        }

        // Set tag for position
        holder.itemView.tag = position
    }

    @SuppressLint("ResourceType", "NotifyDataSetChanged")
    private fun popupMenus(position: Int) {
        val alertDialogBuilder = AlertDialog.Builder(c)
        alertDialogBuilder.setTitle("Delete User")
        alertDialogBuilder.setMessage("Are you sure you want to delete this user?")
        alertDialogBuilder.setPositiveButton("Delete") { dialog, _ ->
            val deletedUser = userList[position]
            userList.removeAt(position)
            notifyDataSetChanged() // Notify adapter that data set has changed

            val dbref = FirebaseDatabase.getInstance().getReference("Add_Member")
            dbref.child(deletedUser.id.toString()).removeValue()

            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        c.startActivity(intent)
    }
    private fun showUpdateDialog() {
        val fragmentManager = (c as AppCompatActivity).supportFragmentManager
        val fragment = fee_pending_content()
        fragment.show(fragmentManager, "fee_pending_content")
    }
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnCall: Button = itemView.findViewById(R.id.btnCall)
        val btnUpdate:Button=itemView.findViewById(R.id.btnUpdate)
        val cardImage: ImageView = itemView.findViewById(R.id.cardImage)
        val cardFirst: TextView = itemView.findViewById(R.id.cardFirstName)
        val cardLast: TextView = itemView.findViewById(R.id.cardLastName)
        val cardGender: TextView = itemView.findViewById(R.id.cardGender)
        val cardAge: TextView = itemView.findViewById(R.id.cardAge)
        val cardWeight: TextView = itemView.findViewById(R.id.cardWeight)
        val cardMobile: TextView = itemView.findViewById(R.id.cardMobile)
        val cardJoining: TextView = itemView.findViewById(R.id.cardJoining)
        val cardMember: TextView = itemView.findViewById(R.id.cardMember)
        val cardExpire: TextView = itemView.findViewById(R.id.cardExpire)
        val cardDiscount: TextView = itemView.findViewById(R.id.cardDiscount)
        val cardTotal: TextView = itemView.findViewById(R.id.cardTotal)
    }
    }
