package com.hyak4j.sounddrawing.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import com.hyak4j.sounddrawing.model.DrewPath
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.locks.ReentrantLock

class PaintView(contex: Context, attrs: AttributeSet) : View(contex, attrs) {
    /*
        Mode
        1: Pen Mode
       -1: Fill Mode
        0: Eraser Mode
     */
    private var mode = 1

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

    // 單一線程 executor
//    private val  executor: ExecutorService = Executors.newSingleThreadExecutor()

    private val  lock: ReentrantLock = ReentrantLock()

    private lateinit var progressBar: ProgressBar
    init {
        mPaint.isAntiAlias = false // 預防填充線條，因抗鋸齒效果造成無填滿之狀況
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
            MotionEvent.ACTION_DOWN -> if (mode >= 0){
                newPath = true
                touchStart(x, y)
                invalidate()
            }else{
                // -1: Fill Mode
                val fillPoint = Point(x.toInt(), y.toInt())
                // 目前點下pixel的顏色
                val sourceColor = mBitmap.getPixel(x.toInt(), y.toInt())
                val targetColor = currentColor
                fillDrawing(mBitmap, fillPoint, sourceColor, targetColor)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode >= 0){
                    touchMove(x, y)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mode >= 0){
                    touchUp()
                    invalidate()
                    post {
                        // 避免末端來不及畫線，只有點的狀況
                        newPath = false
                    }
                }
            }
        }

        return true
    }

    // 進行顏色填充
    private fun fillDrawing(bmp: Bitmap, fillPoint: Point, sourceColor: Int, targetColor: Int) {
        floodFillLock(bmp, fillPoint, sourceColor, targetColor)
//        floodFillExecutor(bmp, fillPoint, sourceColor, targetColor)
    }

    private fun floodFillExecutor(
        bmp: Bitmap,
        fillPoint: Point,
        sourceColor: Int,
        targetColor: Int
    ) {
//        executor.execute {
//            post {
//                progressBar.visibility = VISIBLE
//            }
//
//            floodFill(bmp, fillPoint, sourceColor, targetColor)
//
//            post {
//                progressBar.visibility = INVISIBLE
//            }
//        }
    }

    private fun floodFillLock(
        bmp: Bitmap,
        fillPoint: Point,
        sourceColor: Int,
        targetColor: Int
    ) {
        Thread {
            post {
                progressBar.visibility = VISIBLE
            }
            lock.lock()
            floodFill(bmp, fillPoint, sourceColor, targetColor)
            lock.unlock()
            post {
                progressBar.visibility = INVISIBLE
            }
        }.start()
    }


    private fun floodFill(bmp: Bitmap, fillPoint: Point, sourceColor: Int, targetColor: Int) {
        /*
            起始節點: fillPoint
            目標顏色: targetColor
            將被替換顏色: sourceColor
         */
        var mNode: Point? = fillPoint
        val width = bmp.width
        val height = bmp.height

        if (sourceColor != targetColor){
            val queue: Queue<Point> = LinkedList() // 儲存目標填充區域
            do {
                var x = mNode!!.x
                var y = mNode!!.y
                while (x > 0 && bmp.getPixel(x - 1 , y) == sourceColor){
                    // 左邊是原來顏色 => 點位左移 (直到為非本來顏色)
                    x --
                }
                var spanUp = false
                var spanDown = false
                while (x < width && bmp.getPixel(x, y) == sourceColor){
                    // 更新為Target顏色
                    bmp.setPixel(x, y, targetColor)
                    if (!spanUp && y > 0 && bmp.getPixel(x, y - 1) == sourceColor){
                        // 往上
                        queue.add(Point(x, y - 1))
                        spanUp = true
                    } else if (spanUp && y > 0 && bmp.getPixel(x, y - 1) != sourceColor){
                        spanUp = false
                    }

                    if (!spanDown && y < height - 1 && bmp.getPixel(x, y + 1) == sourceColor){
                        // 往下
                        queue.add(Point(x, y + 1))
                        spanDown = true
                    } else if (spanDown && y < height - 1 && bmp.getPixel(x, y + 1)!= sourceColor){
                        spanDown = false
                    }
                    x ++
                }
                postInvalidate()
                mNode = queue.poll() // 取出第一個queue或null
            }while (mNode != null)
        }
    }

    private fun touchStart(x: Float, y: Float){
        brushSize = if (mode == 0) 40 else 10
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
        clearLock()
//        clearExecutor()
    }

    private fun clearExecutor() {
//        executor.execute {
//            post {
//                // => main thread 顯示 ProgressBar
//                progressBar.visibility = VISIBLE
//            }
//            // 清除軌跡記錄
//            paths.clear()
//
//            // 將每個pixel改為白色 => 耗時
//            for (i in 0 until mBitmap.width) {
//                for (j in 0 until mBitmap.height) {
//                    mBitmap.setPixel(i, j, Color.WHITE)
//                }
//                post {
//                    // 在for loop分批清空每行，UI較流暢
//                    invalidate()
//                }
//            }
//
//            post {
//                progressBar.visibility = INVISIBLE
//            }
//    //            postInvalidate()
//        }
        //        invalidate() => This must be called from a UI thread. To call from a non-UI thread, call postInvalidate().
    }

    private fun clearLock() {
        Thread {
            post {
                progressBar.visibility = VISIBLE
            }
            // 清除軌跡記錄
            paths.clear()
            lock.lock()
            for (i in 0 until mBitmap.width) {
                for (j in 0 until mBitmap.height) {
                    mBitmap.setPixel(i, j, Color.WHITE)
                }
                post {
                    // 在for loop分批清空每行，UI較流暢
                    invalidate()
                }
            }
            lock.unlock()
            post {
                progressBar.visibility = INVISIBLE
            }
        }.start()
    }

    // 傳入MainActivity ProgressBar
    fun setProgressBar(mainBar: ProgressBar){
        progressBar = mainBar
        progressBar.visibility = INVISIBLE
    }

    // 轉換使用模式
    fun changeMode(wantedMode: Int){
        mode = wantedMode
    }

    // 切換顏色
    fun changeColor(color: Int, context: Context){
        currentColor = context.getColor(color)
    }

    // 取得目前模式
    fun getMode(): Int{
        return mode
    }
}