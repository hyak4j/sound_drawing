package com.hyak4j.sounddrawing

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.hyak4j.sounddrawing.databinding.ActivityMainBinding
import com.hyak4j.sounddrawing.view.PaintView

class MainActivity : AppCompatActivity() {
    companion object{
        private val RECORD_AUDIO_REQUEST = 10
    }
    // viewBinding
    private lateinit var binding: ActivityMainBinding
    private lateinit var paintView: PaintView
    private lateinit var modeArray: Array<MaterialButton>
    private lateinit var colorBtnArray: Array<MaterialButton>
    private lateinit var colorArray: Array<Int>

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

        if (isRecordAudioGranted()){
            // 錄音權限已允許
            Toast.makeText(this, "${resources.getString(R.string.txt_record_audio)}${resources.getString(R.string.txt_permission)}${resources.getString(R.string.txt_granted)}", Toast.LENGTH_LONG).show()
            // TODO:進行錄音相關工作
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
            paintView.changeColor(R.color.white, applicationContext)
            setColor(binding.btnWhite, R.color.white)
        }

        // === 畫筆顏色相關 ===
        colorBtnArray = arrayOf(binding.btnBlack, binding.btnWhite, binding.btnRed, binding.btnGreen, binding.btnBlue,
            binding.btnYellow, binding.btnPurple, binding.btnPink)

        colorArray = arrayOf(R.color.black, R.color.white, R.color.red, R.color.green, R.color.blue,
            R.color.yellow, R.color.purple, R.color.pink)

        // 預設值: 畫筆模式、藍色
        setModeButtonBorder(binding.btnPen)
        paintView.changeMode(1)
        setColor(binding.btnBlue, R.color.blue)

        // 各顏色按鈕處理 (將各顏色重複程式碼簡化)
        for (index in colorArray.indices){
            colorBtnArray[index].setOnClickListener {
                setColor(it as MaterialButton, colorArray[index])

                if (paintView.getMode() == 0){
                    setModeButtonBorder(binding.btnPen)
                    paintView.changeMode(1)
                }
            }
        }
    }

    private fun isRecordAudioGranted(): Boolean {
        return if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            true
        }else{
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST)
            false
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
        for (colorBtn in colorBtnArray){
            // 清除
            colorBtn.strokeWidth = 0
        }
        paintView.changeColor(color, applicationContext)
        colorButton.strokeWidth = 10
        colorButton.strokeColor = ColorStateList.valueOf(
            resources.getColor(R.color.theme_blue, null)
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            RECORD_AUDIO_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // TODO:進行錄音相關工作
                }else{
                    binding.txtCommand.text = resources.getString(R.string.txt_record_failed)
                    binding.btnCommand.setOnClickListener {
                        Toast.makeText(this, "${resources.getString(R.string.txt_record_failed)},${resources.getString(R.string.txt_please_granted)}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}