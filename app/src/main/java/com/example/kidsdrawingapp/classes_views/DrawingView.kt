package com.example.kidsdrawingapp.classes_views

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Toast
import com.example.kidsdrawingapp.R
import com.example.kidsdrawingapp.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.pow

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mDrawPath: CustomPath? = null
    private var mDrawPath2: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrashSize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()
    private val distances = ArrayList<Double>()
    private val mseList = ArrayList<Double>()
    private var circleCenterX = 0f
    private var circleCenterY = 0f
    private var circleRadius = 0f


    init {
        setUpDrawing()
    }

    /**
     * This method initializes the attributes of the
     * ViewForDrawing class.
     */
    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrashSize)
        mDrawPath2 = CustomPath(color, mBrashSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrashSize = 20.toFloat()
        circleCenterX = 550.3f
        circleCenterY = 900.9f
        circleRadius = 400f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    fun saveCircle() {
        mDrawPath2!!.addCircle(circleCenterX, circleCenterY, circleRadius, Path.Direction.CW)
        mPaths.add(mDrawPath2!!)

    }

    //change color
    fun setColor(newColor: Int) {
        color = newColor
        mDrawPaint!!.color = color
    }

    fun getCurrentColor(): Int {
        return color
    }

    /**
     * This method is called when a stroke is drawn on the canvas
     * as a part of the painting.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        //the draw stays on screen(draw the path that saved in ArrayList)
        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        if (!mDrawPath!!.isEmpty) {

            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    /**
     * This method acts as an event listener when a touch
     * event is detected on the device.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrashSize
                mDrawPath!!.reset()

                if (touchX != null && touchY != null) {
                    mDrawPath!!.moveTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null && touchY != null) {
                    calcDistance(touchX, touchY)
                    mDrawPath!!.lineTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_UP -> {
                //save current path
                //calcResult()
                mPaths.add(mDrawPath!!)
                calcMSE()
                mDrawPath = CustomPath(color, mBrashSize)
            }
            else -> {
                return false
            }
        }
        invalidate()
        return true
    }

    fun calcDistance(touchX: Float, touchY: Float) {
        var calcDist = Math.hypot(
            (touchX - circleCenterX).toDouble(),
            (touchY - circleCenterY).toDouble()
        )
        distances.add(calcDist)
    }


    fun calcMSE(){
        var sum = 0.0
        for (distance in distances) {
            sum += (400 - distance).pow(2)
        }
        val MSE = sum/ distances.size
            Log.d("circlerrors","MSE: $MSE")
        mseList.add(MSE)
        distances.clear()
        mPaths.removeAt(1)
        Toast.makeText(context,"${mseList.size}/3",Toast.LENGTH_LONG).show()
        if(mseList.size == 3){
            showDialog()
        }
    }

    fun calcResult():String{
        var res = 0.0
        for (mse in mseList) {
            res += mse
        }
        res /= mseList.size

        if(res > 350){
           return  "There is a high probability for parkinson disease"
        }else{
            return  "There is a low probability for parkinson disease"
        }
    }

    private fun showDialog() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setTitle("Results")
        alertDialog.setMessage(calcResult())
        alertDialog.setPositiveButton(
            "Start Over"
        ) { _, _ ->
            mseList.clear()
        }

        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()

    }

    fun setSizeForBrush(newSize: Float) {
        mBrashSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
    }

    fun onClickUndo() {
        if (mPaths.isNotEmpty()) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))

            invalidate() //call onDraw again
        }
    }

    fun onClickRedo() {
        if (mUndoPaths.isNotEmpty()) {
            mPaths.add(mUndoPaths.removeAt(mUndoPaths.size - 1))

            invalidate() //call onDraw again
        }


    }

    // An inner class for custom path with two params as color and stroke size.
    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }


}