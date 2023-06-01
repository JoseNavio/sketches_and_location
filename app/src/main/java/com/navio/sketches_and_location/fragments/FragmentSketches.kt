package com.navio.sketches_and_location.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.navio.sketches_and_location.R
import com.navio.sketches_and_location.data.AnnotationCanvas
import com.navio.sketches_and_location.data.CommentCanvas
import com.navio.sketches_and_location.databinding.DialogConfirmDeleteBinding
import com.navio.sketches_and_location.databinding.DialogEditPaintBinding
import com.navio.sketches_and_location.databinding.DialogFileNameBinding
import com.navio.sketches_and_location.databinding.DialogInsertTextBinding
import com.navio.sketches_and_location.databinding.FragmentSketchesLayoutBinding
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentSketches : Fragment() {

    private lateinit var binding: FragmentSketchesLayoutBinding
    private lateinit var bindingDialogPalette: DialogEditPaintBinding
    private lateinit var selectedColor: ColorDrawable
    private var sliderValue: Float = 100f
    private var sliderStroke: Float = 10f

    //Load colors
    private lateinit var white: ColorDrawable
    private lateinit var red: ColorDrawable
    private lateinit var ocher: ColorDrawable
    private lateinit var blue: ColorDrawable
    private lateinit var green: ColorDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSketchesLayoutBinding.inflate(layoutInflater)
        initViews()
        initColors()
        setButtons()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Modify top bar
        (activity as AppCompatActivity).supportActionBar?.apply {
//            hide()
            setBackgroundDrawable(context?.let {
                ContextCompat.getColor(
                    it,
                    R.color.brown
                )
            }?.let { ColorDrawable(it) })
            title = "Image Editor"
            subtitle = "TcpGPS"
        }
        visualizeImage(R.raw.foto)
        return binding.root
    }

    //Init views related task and configuration
    private fun initViews() {

    }

    //Init the available colors
    private fun initColors() {
        //If context is not null init the colors
        context?.let {
            white = ColorDrawable(it.getColor(R.color.white))
            red = ColorDrawable(it.getColor(R.color.red))
            ocher = ColorDrawable(it.getColor(R.color.ocher))
            blue = ColorDrawable(it.getColor(R.color.blue))
            green = ColorDrawable(it.getColor(R.color.green))
        }
        //Default color
        selectedColor = red
    }

    //Set up layout buttons
    private fun setButtons() {
        //Allow to draw on screen
        binding.buttonDraw.setOnClickListener {// > Draw
            binding.drawingView.startDrawing()
        }
        //Introduces a text on canvas
        binding.buttonText.setOnClickListener {// > Write
            writeComment()
        }
        //Introduces annotation after select checks in dialog
        binding.buttonAnnotations.setOnClickListener {// > Annotate
            showAttributesDialog()
        }
        //Edit paint style
        binding.buttonPalette.setOnClickListener {
            showPaletteDialog()
        }
        //Stores file in app internal storage
        binding.buttonSave.setOnClickListener {// > Save
            val imageBitmap = BitmapFactory.decodeResource(resources, R.raw.foto)
            showNameDialog(object : OnTextPassed {
                override fun onTextPassed(name: String) {
                    saveAfterGettingName(name, imageBitmap)
                }
            })
        }
        //Move last operation
        binding.buttonMove.setOnClickListener {// > Move
            binding.drawingView.startMoving()
        }
        //todo Test copy
        binding.buttonCopy.setOnClickListener {
            binding.drawingView.copyLast()
        }
        //Undo last change
        binding.buttonClear.setOnClickListener {// > Undo
            binding.drawingView.undo()
        }
        //Clear entire canvas
        binding.buttonDelete.setOnClickListener {// > Delete
            showDeleteDialog()
        }
        //Clears window
        binding.buttonClear.setOnLongClickListener {// > Clear
            binding.drawingView.clear()
            true
        }
    }

    private fun writeComment() {

        showTextDialog(object : OnTextPassed {

            override fun onTextPassed(comment: String) {
                if (comment.isNotBlank()) {
                    binding.drawingView.startWriting(comment)
                    val toastTop = Toast.makeText(
                        context,
                        "Select a point on the screen",
                        Toast.LENGTH_SHORT
                    )
                    toastTop.setGravity(Gravity.TOP, 111, 110)
                    toastTop.show()
                }
            }
        })
    }

    private fun showPaletteDialog() {

        bindingDialogPalette = DialogEditPaintBinding.inflate(layoutInflater)

        //Highlight the current selected color
        switchSelectedColor(selectedColor)

        bindingDialogPalette.colorSwatchWhite.setOnClickListener {
            deselectOldColor(selectedColor)
            switchSelectedColor(white)
        }
        bindingDialogPalette.colorSwatchRed.setOnClickListener {
            deselectOldColor(selectedColor)
            switchSelectedColor(red)
        }
        bindingDialogPalette.colorSwatchOcher.setOnClickListener {
            deselectOldColor(selectedColor)
            switchSelectedColor(ocher)
        }
        bindingDialogPalette.colorSwatchBlue.setOnClickListener {
            deselectOldColor(selectedColor)
            switchSelectedColor(blue)
        }
        bindingDialogPalette.colorSwatchGreen.setOnClickListener {
            deselectOldColor(selectedColor)
            switchSelectedColor(green)
        }
        //Slider setup
        bindingDialogPalette.sliderText.value = sliderValue
        bindingDialogPalette.sliderText.valueFrom = 50f
        bindingDialogPalette.sliderText.valueTo = 300f

        bindingDialogPalette.sliderStroke.value = sliderStroke
        bindingDialogPalette.sliderStroke.valueFrom = 5f
        bindingDialogPalette.sliderStroke.valueTo = 30f

        val alertDialogFile = context?.let {

            AlertDialog.Builder(it)
                .setTitle("Style")
                .setView(bindingDialogPalette.root)
                .setPositiveButton("Ok") { _, _ ->
                    //Changes color
                    binding.drawingView.selectColor(selectedColor.color)
                    //Changes text size
                    sliderValue = bindingDialogPalette.sliderText.value
                    binding.drawingView.selectTextSize(sliderValue)
                    //Changes stroke size
                    sliderStroke = bindingDialogPalette.sliderStroke.value
                    binding.drawingView.selectStrokeSize(sliderStroke)
                }
                .setNegativeButton("Cancel", null)
                .create()
        }
        alertDialogFile?.show()
    }

    private fun switchSelectedColor(color: ColorDrawable) {

        Log.d("Navio_Switch", "${color.color}")
        when (color) {
            white -> {
                bindingDialogPalette.colorSwatchWhite.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.color_blob_white
                    )
            }
            red -> {
                bindingDialogPalette.colorSwatchRed.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.color_blob_red
                    )
            }
            ocher -> {
                bindingDialogPalette.colorSwatchOcher.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.color_blob_ocher
                    )
            }
            blue -> {
                bindingDialogPalette.colorSwatchBlue.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.color_blob_blue
                    )
            }
            green -> {
                bindingDialogPalette.colorSwatchGreen.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.color_blob_green
                    )
            }
        }
        //Select the new color
        selectedColor = color
    }

    private fun deselectOldColor(color: ColorDrawable) {
        when (color) {
            white -> {
                bindingDialogPalette.colorSwatchWhite.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.color_blob_white_dark
                    )
            }
            red -> {
                bindingDialogPalette.colorSwatchRed.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.color_blob_red_dark
                    )
            }
            ocher -> {
                bindingDialogPalette.colorSwatchOcher.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.color_blob_ocher_dark
                    )
            }
            blue -> {
                bindingDialogPalette.colorSwatchBlue.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.color_blob_blue_dark
                    )
            }
            green -> {
                bindingDialogPalette.colorSwatchGreen.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.color_blob_green_dark
                    )
            }
        }
    }

    private fun showNameDialog(callback: OnTextPassed) {

        val dialogFileBinding: DialogFileNameBinding = DialogFileNameBinding.inflate(layoutInflater)
        var fileName = "imagen"

        dialogFileBinding.labelComment.text = "Nombre del archivo: "

        val alertDialogFile = context?.let {
            AlertDialog.Builder(it)
                .setTitle("GUARDAR IMAGEN")
                .setView(dialogFileBinding.root)
                .setPositiveButton("Guardar") { _, _ ->
                    fileName = dialogFileBinding.fieldFileName.text.toString()
                    callback.onTextPassed(fileName)
                }
                .setNegativeButton("Cancelar", null)
                .create()
        }
        alertDialogFile?.show()
    }

    private fun showTextDialog(callback: OnTextPassed) {

        val dialogTextBinding: DialogInsertTextBinding =
            DialogInsertTextBinding.inflate(layoutInflater)

        dialogTextBinding.labelCommentInsertText.text = "Add a comment: "

        val alertDialogText = context?.let {
            AlertDialog.Builder(it)
                .setTitle("Comments")
                .setView(dialogTextBinding.root)
                .setPositiveButton("Ok") { _, _ ->
                    //Return written comment
                    callback.onTextPassed(dialogTextBinding.fieldInsertText.text.toString())
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
        }
        alertDialogText?.show()
    }

    private fun showDeleteDialog() {

        val dialogTextBinding: DialogConfirmDeleteBinding =
            DialogConfirmDeleteBinding.inflate(layoutInflater)

        val alertDialogText = context?.let {
            AlertDialog.Builder(it)
                .setTitle("Clear screen")
                .setView(dialogTextBinding.root)
                .setPositiveButton("Accept") { _, _ ->
                    //Delete all changes
                    binding.drawingView.clear()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
        }
        alertDialogText?.show()
    }

    private fun showAttributesDialog() {

        val dialogAttributesView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_specify_annotation, null)

        val x = dialogAttributesView.findViewById<CheckBox>(R.id.checkbox_x)
        val y = dialogAttributesView.findViewById<CheckBox>(R.id.checkbox_y)
        val z = dialogAttributesView.findViewById<CheckBox>(R.id.checkbox_z)
        val date = dialogAttributesView.findViewById<CheckBox>(R.id.checkbox_date)

        val attributes = mutableListOf<CommentCanvas>()

        //Show dialog
        val dialogAttributes = context?.let {
            AlertDialog.Builder(it)
                .setTitle("Attributes")
                .setView(dialogAttributesView)
                .setPositiveButton("OK") { dialog, _ ->
                    //todo Change Y for count * default
                    var checkCount = 0
                    // Retrieve the checked state of the checkboxes and set conditions
                    if (x.isChecked) {
                        attributes.add(CommentCanvas(0f, 100f * checkCount, "x: 150020.123"))
                        checkCount++
                    }
                    if (y.isChecked) {
                        attributes.add(CommentCanvas(0f, 100f * checkCount, "y: 144066.025"))
                        checkCount++
                    }
                    if (z.isChecked) {
                        attributes.add(CommentCanvas(0f, 100f * checkCount, "z: 230001.567"))
                        checkCount++
                    }
                    if (date.isChecked) {
                        val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"))
                        } else {
                            //todo Check if this works
                            val dateFormat =
                                SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault())
                            dateFormat.format(Date())
                        }
                        attributes.add(
                            CommentCanvas(

                                0f,
                                100f * checkCount,
                                currentDate
                            )
                        )
                    }

                    val annotation: AnnotationCanvas = AnnotationCanvas(50f, 100f, attributes)

                    binding.drawingView.startAnnotating(annotation)

                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Handle Cancel button click
                    dialog.dismiss()
                }
                .create()
        }
        dialogAttributes?.show()
    }

    private fun saveAfterGettingName(name: String, bitmap: Bitmap) {

        //If name is blank --> "imagen" else "name"
        val fileName = name.ifBlank { "imagen" }
        var finalBitmap = binding.drawingView.generateBitmap(bitmap)
        val savedFile = binding.drawingView.saveDrawing(fileName, finalBitmap)
        if (savedFile != null) {
            Toast.makeText(context, "Imagen guardada ${savedFile.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "La aplicación fallo al tratar de guardar la imagen",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //Why I cannot set image directly?
    private fun visualizeImage(imageID: Int) {
        //binding.imageContainer.setImageResource(imageID)
        val externalDir = requireContext().getExternalFilesDir(null)
        Glide.with(requireContext())
            .load(File(externalDir, "foto.jpg"))
            .into(binding.imageContainer)
    }

    companion object {
        fun newInstance() = FragmentSketches()
    }
}

//Pass the name from Dialog to button click listener event
interface OnTextPassed {
    fun onTextPassed(text: String)
}

