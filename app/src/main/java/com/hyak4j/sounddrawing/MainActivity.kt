package com.hyak4j.sounddrawing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.TypedValue
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
    // 聲音辨識
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizeListener: Listener
    private var isListening = false

    inner class Listener(private val context: Context) : RecognitionListener{
        override fun onReadyForSpeech(params: Bundle?) {
        }

        override fun onBeginningOfSpeech() {
        }

        override fun onRmsChanged(rmsdB: Float) {
        }

        override fun onBufferReceived(buffer: ByteArray?) {
        }

        override fun onEndOfSpeech() {
            // 聲音辨識結束處理
            restoreCommandButtonStyle()
        }

        override fun onError(error: Int) {
            // 使用者未說話 ..等錯誤
            restoreCommandButtonStyle()
            binding.txtCommand.text = resources.getString(R.string.txt_error_recognize)
        }

        override fun onResults(results: Bundle) {
            /*
            / TODO: 優化中文完整語句指令
                com.huaban.analysis:hanlp-porter => JiebaSegmenter ?
                先預設en-US
             */
            // 聲音辨識結果返回EXTRA_MAX_RESULTS 結果數
            val soundData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            binding.txtCommand.text = "${resources.getString(R.string.txt_result_sound)} ${soundData!![0]}"

            val commandString = soundData[0].split(" ")
            val colors = ArrayList<String>()
            var penCommand = false
            var fillCommand = false
            for (i in commandString.indices){
                when(commandString[i].lowercase()){
                    showString(R.string.btn_clear), showString(R.string.txt_clear) , "clear" -> {
                        // 包含清除指令時
                        setModeButtonBorder(binding.btnClear)
                        paintView.clear()
                        return
                    }
                    showString(R.string.btn_eraser), "eraser", "erase", "chaser" -> {
                        // 包含橡皮擦指令時
                        setEraserMode()
                        return
                    }
                    showString(R.string.btn_fill), showString(R.string.txt_fill), "fill", "fell", "feel", "fail", "phil" -> {
                        // 包含填充指令時
                        fillCommand = true
                    }
                    showString(R.string.btn_pen), showString(R.string.txt_pen), "pen", "pain", "pane", "pan" -> {
                        penCommand = true
                    }
                    showString(R.string.txt_black), showString(R.string.txt_white), showString(R.string.txt_green),
                    showString(R.string.txt_red), showString(R.string.txt_blue), "blu", showString(R.string.txt_yellow),
                    showString(R.string.txt_purple), showString(R.string.txt_pink),
                    "black", "white", "red", "green", "blue", "yellow", "purple", "pink"-> {
                        // 處理顏色關鍵字
                        colors.add(commandString[i].lowercase())
                    }
                }
            }
            if (penCommand && fillCommand){
                Toast.makeText(context, resources.getString(R.string.txt_error_pen_fill_together), Toast.LENGTH_LONG).show()
                return
            }else if (penCommand){
                setPenMode()
            }else if (fillCommand){
                setFillMode()
            }
            if (colors.size > 1){
                Toast.makeText(context, resources.getString(R.string.txt_error_multicolors), Toast.LENGTH_LONG).show()
            }else if (colors.size == 1){
                // 確認顏色
                when(colors[0]){
                    showString(R.string.txt_black), "black" -> {
                        setColor(binding.btnBlack, R.color.black)
                        paintView.changeColor(R.color.black, context)
                    }
                    showString(R.string.txt_white), "white" -> {
                        setColor(binding.btnWhite, R.color.white)
                        paintView.changeColor(R.color.white, context)
                    }
                    showString(R.string.txt_red), "red" -> {
                        setColor(binding.btnRed, R.color.red)
                        paintView.changeColor(R.color.red, context)
                    }
                    showString(R.string.txt_green), "green", "ring" -> {
                        setColor(binding.btnGreen, R.color.green)
                        paintView.changeColor(R.color.green, context)
                    }
                    showString(R.string.txt_blue), "blue", "blu" -> {
                        setColor(binding.btnBlue, R.color.blue)
                        paintView.changeColor(R.color.blue, context)
                    }
                    showString(R.string.txt_yellow), "yellow" -> {
                        setColor(binding.btnYellow, R.color.yellow)
                        paintView.changeColor(R.color.yellow, context)
                    }
                    showString(R.string.txt_purple), "purple" -> {
                        setColor(binding.btnPurple, R.color.purple)
                        paintView.changeColor(R.color.purple, context)
                    }
                    showString(R.string.txt_pink), "pink" -> {
                        setColor(binding.btnPink, R.color.pink)
                        paintView.changeColor(R.color.pink, context)
                    }
                }
                // 確認現在模式
                if (paintView.getMode() == 0){
                    setModeButtonBorder(binding.btnPen)
                    paintView.changeMode(1)
                }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
        }

        private fun restoreCommandButtonStyle() {
            // 改回聲音指令按鈕樣式
            binding.btnCommand.text = resources.getString(R.string.btn_sound)
            // 從theme.xml取得
            val typeValue = TypedValue()
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typeValue, true)
            val textColor = ContextCompat.getColor(context, typeValue.resourceId)
            binding.btnCommand.setTextColor(textColor)

            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typeValue, true)
            val backgroundColor = ContextCompat.getColor(context, typeValue.resourceId)
            binding.btnCommand.setBackgroundColor(backgroundColor)

            isListening = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpViewComponent()
    }

    private fun setUpViewComponent() {
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
            setUpRecordAudio()
        }

        // 填滿按鍵
        binding.btnFill.setOnClickListener {
            setFillMode()
        }

        // 畫筆按鍵
        binding.btnPen.setOnClickListener {
            setPenMode()
        }

        // 橡皮擦
        binding.btnEraser.setOnClickListener {
            setEraserMode()
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

    private fun setFillMode() {
        // 設為填充模式
        setModeButtonBorder(binding.btnFill)
        paintView.changeMode(-1)
    }

    private fun setPenMode() {
        // 設為畫筆模式
        setModeButtonBorder(binding.btnPen)
        paintView.changeMode(1)
    }

    private fun setEraserMode() {
        // 設為橡皮擦模式
        setModeButtonBorder(binding.btnEraser)
        paintView.changeMode(0)
        paintView.changeColor(R.color.white, applicationContext)
        setColor(binding.btnWhite, R.color.white)
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

    private fun setUpRecordAudio(){
        // 聲音辨識 : 聲音 => 文字
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // 辨識 listener
        recognizeListener = Listener(this)
        speechRecognizer.setRecognitionListener(recognizeListener)
        binding.txtCommand.text = resources.getString(R.string.txt_result_sound)
        binding.btnCommand.setOnClickListener {
            // 開始聲音辨識
            if (!isListening){
                isListening = true
                binding.txtCommand.text = resources.getString(R.string.txt_receive_sound)
                binding.btnCommand.setTextColor(Color.WHITE)
                binding.btnCommand.setBackgroundColor(Color.BLACK)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                /*
                   語音辨識設定
                   語系 en-US, cmn-Hant-TW, cmn-Hant-CN
                   聲音辨識不用遵循特定結構  LANGUAGE_MODEL_FREE_FORM
                   返回結果數
                 */
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                speechRecognizer.startListening(intent)
            }
            Toast.makeText(this, "${resources.getString(R.string.txt_record_audio)}${resources.getString(R.string.txt_permission)}${resources.getString(R.string.txt_granted)}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showString(stringValue: Int): String{
        return resources.getString(stringValue)
    }
}