package com.navio.sketches_and_location.drawing

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.createScaledBitmap
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.navio.sketches_and_location.data.CommentCanvas
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

//attrs is used to pass the attributes defined in the XML to the parent View
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val drawPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private var currentPath: Path? = null
    private var currentComment: String? = null

    //List
    private val paths = mutableListOf<Path>()
    private val comments = mutableListOf<CommentCanvas>()
    private val history = mutableListOf<EditOperation>()

    //Flags
    private var shouldDraw = false
    private var shouldWrite = false

    init {
        isFocusable = true //Focusable view
        isFocusableInTouchMode =
            true //Gain focus and handle touch events without requiring an explicit click or focus change event.
        setupPaint()
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
        textPaint.color = Color.RED
        textPaint.textSize = 96f
        textPaint.textAlign = Paint.Align.LEFT
        //Font and style
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawOnCanvas(canvas)
        writeOnCanvas(canvas)

    }

    //Watches where screen has been touched to draw on it
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        if (shouldDraw) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Sets the initial point of the path
                    currentPath = Path()
                    currentPath?.moveTo(x, y)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Adds a segment to the path
                    currentPath?.lineTo(x, y)
                }
                MotionEvent.ACTION_UP -> {
                    // Add path to list and start a new path
                    currentPath?.let { path ->
                        paths.add(path)
                        history.add(EditOperation.DRAW)
                        currentPath = null
                    }
                }
                else -> return false
            }
            // Force the view to redraw
            invalidate()
        } else if (shouldWrite) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //Returning true notifies that event already handled
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    // Do any required actions when the finger is lifted
                    currentComment?.let { comment ->
                        comments.add(CommentCanvas(comment, x, y))
                        currentComment = null
                        history.add(EditOperation.WRITE)
                    }
                }
                else -> return false //Avoid to cancelling ACTION_UP if finger moves before release
            }
            // Force the view to redraw
            invalidate()
        }
        return true
    }

    //Draw and Write
    fun startDrawing() {
        this.shouldDraw = true
        this.shouldWrite = false
    }

    fun startWriting(comment: String) {
        this.currentComment = comment
        shouldDraw = false
        shouldWrite = true
    }

    private fun drawOnCanvas(canvas: Canvas) {
        //Draws the temp path before releasing screen
        currentPath?.let {
            canvas.drawPath(it, drawPaint)
        }
        //Draws all paths stored
        for (path in paths) {
            canvas.drawPath(path, drawPaint)
        }
    }

    private fun writeOnCanvas(canvas: Canvas) {
        for (comment in comments) {
            canvas.drawText(comment.text, comment.x, comment.y, textPaint)
        }
        shouldWrite = false
    }

    //Delete all drawings and writings
    fun clear() {
        paths.clear()
        comments.clear()
        invalidate()
    }
    //Undo last operation
    fun undo() {
        if (history.isNotEmpty()) {
            when (history.removeLast()) {
                EditOperation.DRAW -> {
                    if (paths.isNotEmpty()) {
                        paths.removeLast()
                        invalidate()
                    }
                }
                EditOperation.WRITE -> {
                    if (comments.isNotEmpty()) {
                        comments.removeLast()
                        invalidate()
                    }
                }
            }
        }
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
    //todo Provisional
    fun drawCoordinates(image: ImageView) {
        shouldDraw = false
        shouldWrite = true
        currentComment = "X = 123, Y = 122"

        val viewWidth = image.width.toFloat()
        val viewHeight = image.height.toFloat()

        val twentyFivePercent = 0.15f
        val size = min(viewWidth, viewHeight) * twentyFivePercent

        comments.add(CommentCanvas(currentComment!!, size, size))
        history.add(EditOperation.WRITE)

        invalidate()
        shouldWrite = false
    }

    companion object {
        private const val PNG_EXTENSION = ".png"
    }
}

enum class EditOperation {
    DRAW, WRITE
}