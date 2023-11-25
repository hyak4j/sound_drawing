package com.hyak4j.sounddrawing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hyak4j.sounddrawing.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // viewBinding
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpViewComponment()
    }

    private fun setUpViewComponment() {
        // 將 ProgressBar元件交由 PaintView控制
        binding.paintView.setProgressBar(binding.progressBar)

        // 清除按鍵
        binding.btnClear.setOnClickListener {
            binding.paintView.clear()
        }
    }
}