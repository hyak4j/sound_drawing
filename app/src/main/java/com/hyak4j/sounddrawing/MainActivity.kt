package com.hyak4j.sounddrawing

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.hyak4j.sounddrawing.databinding.ActivityMainBinding
import com.hyak4j.sounddrawing.view.PaintView

class MainActivity : AppCompatActivity() {
    // viewBinding
    private lateinit var binding: ActivityMainBinding
    private lateinit var paintView: PaintView
    private lateinit var modeArray: Array<MaterialButton>
    private lateinit var colorArray: Array<MaterialButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpViewComponment()
    }

    private fun setUpViewComponment() {
        paintView = binding.paintView

        // 將 ProgressBar元件交由 PaintView控制
        paintView.setProgressBar(binding.progressBar)

        // === 模式相關 ===
        modeArray = arrayOf(binding.btnClear, binding.btnFill, binding.btnPen, binding.btnEraser)
        // 清除按鍵
        binding.btnClear.setOnClickListener {
            setModeButtonBorder(binding.btnClear)
            paintView.clear()
        }

        // 填滿按鍵
        binding.btnFill.setOnClickListener {
            setModeButtonBorder(binding.btnFill)
            paintView.changeMode(-1)
        }

        // 畫筆按鍵
        binding.btnPen.setOnClickListener {
            setModeButtonBorder(binding.btnPen)
            paintView.changeMode(1)
        }

        // 橡皮擦
        binding.btnEraser.setOnClickListener {
            setModeButtonBorder(binding.btnEraser)
            paintView.changeMode(0)
        }

        // === 畫筆顏色相關 ===
        colorArray = arrayOf(binding.btnBlack, binding.btnWhite, binding.btnRed, binding.btnGreen, binding.btnBlue,
            binding.btnYellow, binding.btnPurple, binding.btnPink)

        // 預設藍色
        setColor(binding.btnBlue, R.color.blue)

        binding.btnBlack.setOnClickListener {
            setColor(binding.btnBlack, R.color.black)
        }

        binding.btnWhite.setOnClickListener {
            setColor(binding.btnWhite, R.color.white)
        }

        binding.btnRed.setOnClickListener {
            setColor(binding.btnRed, R.color.red)
        }

        binding.btnGreen.setOnClickListener {
            setColor(binding.btnGreen, R.color.green)
        }

        binding.btnBlue.setOnClickListener {
            setColor(binding.btnBlue, R.color.blue)
        }

        binding.btnYellow.setOnClickListener {
            setColor(binding.btnYellow, R.color.yellow)
        }

        binding.btnPurple.setOnClickListener {
            setColor(binding.btnPurple, R.color.purple)
        }

        binding.btnPink.setOnClickListener {
            setColor(binding.btnPink, R.color.pink)
        }
    }

    // 選定模式UI顯示
    private fun setModeButtonBorder(modeButton: MaterialButton){
        for (modeBtn in modeArray){
            // 清除
            modeBtn.strokeWidth = 0
        }
        modeButton.strokeWidth = 10
        modeButton.strokeColor = ColorStateList.valueOf(
            resources.getColor(R.color.dark_theme_blue, null)
        )
    }

    // 畫筆顏色選定UI顯示
    private fun setColor(colorButton: MaterialButton, color: Int){
        for (colorBtn in colorArray){
            // 清除
            colorBtn.strokeWidth = 0
        }
        paintView.changeColor(color, applicationContext)
        colorButton.strokeWidth = 10
        colorButton.strokeColor = ColorStateList.valueOf(
            resources.getColor(R.color.theme_blue, null)
        )
    }
}