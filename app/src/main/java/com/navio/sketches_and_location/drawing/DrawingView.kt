package com.navio.sketches_and_location.drawing

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

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
    //Generates a bitmap with the current sketch
    private fun generateBitmap(): Bitmap{

        //Width and Height take View's dimensions
        //Bitmap.Config.ARGB_8888 --> 32 bits, full color and transparency on background
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        //White background
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.eraseColor(Color.WHITE)
        val canvas = Canvas(bitmap)
        this.draw(canvas) // Draw the content of DrawingView onto the canvas
        return bitmap
    }
    fun addImageToGallery(name: String) {
        val saveDialog = AlertDialog.Builder(context)
        saveDialog.setTitle("AÑADIR A GALERÍA")
        saveDialog.setMessage("¿Guardar la imagen en la galería?")
        saveDialog.setPositiveButton("Guardar") { _: DialogInterface?, _: Int ->
            val bmp: Bitmap = generateBitmap()

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
            }

            val resolver = context.contentResolver
            var imageUri: Uri? = null
            var outputStream: OutputStream? = null

            try {
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                imageUri = resolver.insert(contentUri, contentValues)
                imageUri?.let {
                    outputStream = resolver.openOutputStream(it)
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Toast.makeText(context, "La imagen se guardó en la galería", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "Fallo al guardar imagen en la galería",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                outputStream?.close()
                imageUri?.let {
                    resolver.notifyChange(it, null)
                }
            }
        }
        saveDialog.setNegativeButton("Cancelar") { dialog: DialogInterface, _: Int -> dialog.cancel() }
        saveDialog.show()
    }
    fun saveDrawing(name: String): File? {

        val bitmap = generateBitmap()
        val canvas = Canvas(bitmap)
        draw(canvas)

        val fileName = name.let { givenName -> if (givenName.contains(PNG_EXTENSION)) givenName else givenName + PNG_EXTENSION }
//      val fileName = "$name$PNG_EXTENSION"
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

    companion object{

        private const val PNG_EXTENSION = ".png"
    }
}