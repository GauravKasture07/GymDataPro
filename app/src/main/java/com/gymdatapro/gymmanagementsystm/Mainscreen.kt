package com.gymdatapro.gymmanagementsystm


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.gymdatapro.gymmanagementsystm.activity.LoginActivity
import com.gymdatapro.gymmanagementsystm.databinding.ActivityMainscreenBinding


class Mainscreen : AppCompatActivity() {
    private lateinit var binding:ActivityMainscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragement(home_main())

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.nav_logout->{
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                R.id.nav_home->replaceFragement(home_main())
                R.id.nav_fee_pending->replaceFragement(fee_pending_main())
                R.id.nav_update_fee->replaceFragement(update_fee_main())
                R.id.nav_add->replaceFragement(add_member_main())
                else->{

                }
            }
            true
        }
    }

    private fun replaceFragement(fragement:Fragment){
        val fragmentManager=supportFragmentManager
        val fragmentTransaction=fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragement)
        fragmentTransaction.commit()

    }
}
