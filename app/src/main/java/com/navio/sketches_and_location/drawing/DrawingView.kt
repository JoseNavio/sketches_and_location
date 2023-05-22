package com.navio.sketches_and_location.drawing

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.createScaledBitmap
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.navio.sketches_and_location.fragments.OnScreenTouched
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

//attrs is used to pass the attributes defined in the XML to the parent View
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val drawPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val path: Path = Path()

    //Flags
    private var shouldDraw = false
    private var shouldWrite = false

    //Text
    private var comment: String = "No"
    private var xPosition: Float = 0f
    private var yPosition: Float = 0f

    //Listener
    private lateinit var screenListener: OnScreenTouched

    init {
        isFocusable = true //Focusable view
        isFocusableInTouchMode =
            true //Gain focus and handle touch events without requiring an explicit click or focus change event.
        setupPaint()
    }

    fun setupScreenListener(listener: OnScreenTouched) {
        screenListener = listener
    }

    private fun setupPaint() {
        //Stroke parameters
        drawPaint.color = Color.RED
        drawPaint.isAntiAlias = true //Softer, less pixelated edges
        drawPaint.strokeWidth = 12f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND //Stroke body shape
        drawPaint.strokeCap = Paint.Cap.ROUND //Stroke end shape
        //Text parameters
        textPaint.color = Color.BLACK
        textPaint.textSize = 64f
        textPaint.textAlign = Paint.Align.LEFT
        //Font and style
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when {
            shouldDraw -> drawOnCanvas(canvas)
            shouldWrite -> writeOnCanvas(canvas)
        }
    }

    //todo Can use onTouch instead of onTouchEvent to handle writing?
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
                Log.d("Navio_OnDraw", "Levantado")
                screenListener.onPositionXY(event.x, event.y)
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

    fun startDrawing() {
        shouldDraw = true
        //Reset the path in case the touch event registered something before
        path.reset()
    }

    fun startWriting(text: String, x: Float, y: Float) {
        comment = text
        xPosition = x
        yPosition = y
        shouldDraw = false
        shouldWrite = true
    }

    private fun drawOnCanvas(canvas: Canvas) {
        canvas.drawPath(path, drawPaint)
    }

    private fun writeOnCanvas(canvas: Canvas) {
        canvas.drawText(comment, xPosition, yPosition, textPaint)
        shouldWrite = false
    }

    //Generates a bitmap with the current sketch
    fun generateBitmap(bitmap: Bitmap): Bitmap {
        //Scales the real image to be the size it was when showed on this view
        val adaptedHeight = (bitmap.height * width) / bitmap.width
        val scaledBitmap = createScaledBitmap(bitmap, width, adaptedHeight, true)
        //Creates an editable bitmap
        var mutableBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)
        //Draw canvas contents into bitmap
        draw(Canvas(mutableBitmap))
        return mutableBitmap
    }

    fun saveDrawing(name: String, bitmap: Bitmap): File? {

        val fileName = name.let { givenName ->
            if (givenName.contains(PNG_EXTENSION)) givenName else givenName + PNG_EXTENSION
        }
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

    companion object {
        private const val PNG_EXTENSION = ".png"
    }
}