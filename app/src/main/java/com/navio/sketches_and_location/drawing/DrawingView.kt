package com.navio.sketches_and_location.drawing

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.createScaledBitmap
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.navio.sketches_and_location.data.AnnotationCanvas
import com.navio.sketches_and_location.data.OperationCanvas
import com.navio.sketches_and_location.data.CommentCanvas
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate

//attrs is used to pass the attributes defined in the XML to the parent View
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val drawPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private var currentPath: Path? = null
    private var currentComment: String? = null

    var lastOperation: OperationCanvas? = null
    var lastAnnotation: AnnotationCanvas? = null
    var lastComment: CommentCanvas? = null

    //List
    private val paths = mutableListOf<Path>()
    private val comments = mutableListOf<CommentCanvas>()
    private val history = mutableListOf<EditOperation>()
    private val annotations = mutableListOf<AnnotationCanvas>()

    //Flags
    private var shouldDraw = false
    private var shouldWrite = false
    private var shouldAnnotate = false
    private var shouldMove = false

    init {
        isFocusable = true //Focusable view
        isFocusableInTouchMode =
            true //Gain focus and handle touch events without requiring an explicit click or focus change event.
//        setupPaint(getContext().getColor(R.color.ocher))
        setupPaint(Color.RED)
    }

    private fun setupPaint(color: Int) {
        //Stroke parameters
        drawPaint.color = color
        drawPaint.isAntiAlias = true //Softer, less pixelated edges
        drawPaint.strokeWidth = 12f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND //Stroke body shape
        drawPaint.strokeCap = Paint.Cap.ROUND //Stroke end shape
        //Text parameters
        textPaint.color = color
        textPaint.textSize = 80f
        textPaint.textAlign = Paint.Align.LEFT
        //Font and style
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    //Where we call canvas to be painted
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawOnCanvas(canvas)
        writeOnCanvas(canvas)
        annotateOnCanvas(canvas)
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
        } else if (shouldMove && history.isNotEmpty()) {
            when (history.last()) {
                EditOperation.WRITE -> {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            if (comments.isNotEmpty()) {
                                lastComment = comments.last()
                                lastComment?.x = x
                                lastComment?.y = y
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (comments.isNotEmpty()) {
                                lastComment?.x = x
                                lastComment?.y = y
                            }
                        }
                        // Handle other motion events if needed
                        else -> return true
                    }
                }
                EditOperation.ANNOTATE -> {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            if (annotations.isNotEmpty()) {
                                lastAnnotation = annotations.last()
                                lastAnnotation?.x = x
                                lastAnnotation?.y = y
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (annotations.isNotEmpty()) {
                                lastAnnotation?.x = x
                                lastAnnotation?.y = y
                            }
                        }
                        // Handle other motion events if needed
                        else -> return false
                    }
                }
                // Handle other edit operations if needed
                else -> {}
            }
            invalidate()
        }
        return true
    }

    //Draw, move and write flags
    fun startDrawing() {
        selectOperation(EditOperation.DRAW)
    }

    fun startWriting(comment: String) {
        currentComment = comment
        selectOperation(EditOperation.WRITE)
    }

    fun startMoving() {
        selectOperation(EditOperation.MOVE)
    }

    fun startAnnotating(annotation: AnnotationCanvas) {

        selectOperation(EditOperation.ANNOTATE)
        annotations.add(annotation)
        history.add(EditOperation.ANNOTATE)
        invalidate()
    }

    fun startAnnotating(image: ImageView) {

        selectOperation(EditOperation.ANNOTATE)
        val viewWidth = image.width.toFloat()
        val viewHeight = image.height.toFloat()

        val percent = 0.05f

        val lines = arrayOf("Z: 233.66", "Y: 256.76", "X: 123.54", LocalDate.now().toString())
        var count = 0
        val commentList = mutableListOf<CommentCanvas>()

        for (line in lines) {
            count++
            commentList.add(
                CommentCanvas(
                    line,
                    (percent * viewWidth),
                    (percent * viewHeight * count)
                )
            )
        }
        annotations.add(AnnotationCanvas(100f, 100f, commentList))
        history.add(EditOperation.ANNOTATE)
        invalidate()
    }

    //Paint into the canvas each time onDraw is called
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

    private fun annotateOnCanvas(canvas: Canvas) {
        for (annotation in annotations) {
            for (comment in annotation.comments) {
                canvas.drawText(
                    comment.text,
                    annotation.x + comment.x,
                    annotation.y + comment.y,
                    textPaint
                )
            }
        }
        shouldAnnotate = false
    }

    //Delete all drawings and writings
    fun clear() {
        paths.clear()
        comments.clear()
        annotations.clear()
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
                EditOperation.ANNOTATE -> {
                    if (annotations.isNotEmpty()) {
                        annotations.removeLast()
                        invalidate()
                    }
                }
                EditOperation.MOVE -> {
                    if (annotations.isNotEmpty()) {
                        annotations.removeLast()
                        invalidate()
                    }
                }
                else -> {}
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

    //todo Do in coroutine
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

    private fun selectOperation(operation: EditOperation) {
        shouldWrite = false
        shouldDraw = false
        shouldAnnotate = false
        shouldMove = false

        when (operation) {
            EditOperation.DRAW -> shouldDraw = true
            EditOperation.WRITE -> shouldWrite = true
            EditOperation.ANNOTATE -> shouldAnnotate = true
            EditOperation.MOVE -> shouldMove = true
            else -> {}
        }
    }

    companion object {
        private const val PNG_EXTENSION = ".png"
    }
}

//Each type of edit operation
enum class EditOperation {
    DRAW, WRITE, ANNOTATE, MOVE, NONE
}