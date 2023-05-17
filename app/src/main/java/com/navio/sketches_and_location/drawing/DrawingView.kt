package com.navio.sketches_and_location.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

//attrs is used to pass the attributes defined in the XML to the parent View
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val drawPaint: Paint = Paint()
    private val path: Path = Path()

    init {
        isFocusable = true //Focusable view
        isFocusableInTouchMode =
            true //Gain focus and handle touch events without requiring an explicit click or focus change event.
        setupPaint()
    }

    private fun setupPaint() {
        drawPaint.color = Color.BLACK
        drawPaint.isAntiAlias = true //Softer, less pixelated edges
        drawPaint.strokeWidth = 8f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND //Stroke body shape
        drawPaint.strokeCap = Paint.Cap.ROUND //Stroke end shape
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //Sets the initial point of the path
                path.moveTo(touchX, touchY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                //Adds a segment to the path
                path.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                // Do any required actions when finger is lifted
                //todo We can send a listener to disable button when screen is released
            }
            else -> return false
        }

        // Force the view to redraw
        invalidate()
        return true
    }

    fun clear() {
        path.reset()
        invalidate()
    }

    fun saveDrawing(name: String): File? {
        //Width and Height take View's dimensions
        //Bitmap.Config.ARGB_8888 --> 32 bits, full color and transparency on background
        //val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        //White background
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.eraseColor(Color.WHITE)

        val canvas = Canvas(bitmap)
        draw(canvas)

        val fileName = "$name.png"
        val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(fileDir, fileName)

        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }
}