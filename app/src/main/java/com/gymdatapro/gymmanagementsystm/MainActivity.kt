package com.gymdatapro.gymmanagementsystm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.gymdatapro.gymmanagementsystm.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private var mDelayHandler: Handler? = null
    private val splash_delay: Long = 3000
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mDelayHandler = Handler()
        mDelayHandler?.postDelayed(mRunnable, splash_delay)
    }

    private val mRunnable: Runnable = Runnable {
        val intent = Intent(this, Signup::class.java)
        startActivity(intent);
        finish()
    }
}