package com.hyak4j.sounddrawing.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import com.hyak4j.sounddrawing.model.DrewPath
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PaintView(contex: Context, attrs: AttributeSet) : View(contex, attrs) {
    private var brushSize: Int = 0   // 筆刷尺寸
    private var currentColor = Color.BLUE  // 畫筆顏色
    private val mPaint = Paint() // 畫筆設定
    private lateinit var mCanvas: Canvas // 畫筆
    private lateinit var mBitmap: Bitmap // 畫紙
    private val mBitmapPaint: Paint = Paint()
    private lateinit var mPath: Path  // 儲存軌跡
    private val paths: ArrayList<DrewPath> = ArrayList()

    private var drawPrepare = false // 記錄畫布初始化
    private var newPath = false    // 記錄是否為同一筆劃

    // 線條曲線化 (貝茲曲線相關變數)
    private var mbX = 0f
    private var mbY = 0f

    private val  executor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var progressBar: ProgressBar
    init {
        mPaint.isDither = true // 防抖動
        mPaint.style = Paint.Style.STROKE // 線條
        // 線條圓滑化
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeJoin = Paint.Join.ROUND
        mBitmapPaint.isDither = true
    }

    override fun onDraw(canvas: Canvas) {
        if (!drawPrepare){
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mBitmap.eraseColor(Color.WHITE) // 填充初始顏色
            mCanvas = Canvas(mBitmap)
            drawPrepare = true
        }

        if (newPath){
            // 畫上畫布
            mPaint.color = paths[paths.size - 1].color
            mPaint.strokeWidth = paths[paths.size - 1].brushSize.toFloat()
            mCanvas.drawPath(paths[paths.size - 1].path, mPaint)
        }

        canvas.drawBitmap(mBitmap, 0f, 0f, mBitmapPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                newPath = true
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
                post {
                    // 避免末端來不及畫線，只有點的狀況
                    newPath = false
                }
            }
        }

        return true
    }

    private fun touchStart(x: Float, y: Float){
        brushSize = 10
        mPath = Path()
        paths.add(DrewPath(currentColor, brushSize, mPath))
        mPath.moveTo(x, y)  // 移至軌跡起點
        mbX = x
        mbY = y
    }

    private fun touchMove(x: Float, y: Float){
        mPath.quadTo(mbX, mbY, (mbX + x) / 2, (mbY + y) / 2)
        mbX = x
        mbY = y
    }

    private fun touchUp(){
        mPath.lineTo(mbX, mbY)
    }

    // 清除畫布
    fun clear(){
        executor.execute{
            post {
                // => main thread 顯示 ProgressBar
                progressBar.visibility = VISIBLE
            }
            // 清除軌跡記錄
            paths.clear()

            // 將每個pixel改為白色 => 耗時
            for (i in 0 until mBitmap.width){
                for (j in 0 until mBitmap.height){
                    mBitmap.setPixel(i, j, Color.WHITE)
                }
                post {
                    // 在for loop分批清空每行，UI較流暢
                    invalidate()
                }
            }

            post {
                progressBar.visibility = INVISIBLE
            }
//            postInvalidate()
        }
//        invalidate() => This must be called from a UI thread. To call from a non-UI thread, call postInvalidate().
    }

    // 傳入MainActivity ProgressBar
    fun setProgressBar(mainBar: ProgressBar){
        progressBar = mainBar
        progressBar.visibility = INVISIBLE
    }
}