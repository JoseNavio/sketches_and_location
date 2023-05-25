package com.navio.sketches_and_location.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import com.navio.sketches_and_location.R
import com.navio.sketches_and_location.data.AnnotationCanvas
import com.navio.sketches_and_location.data.CommentCanvas
import com.navio.sketches_and_location.databinding.DialogConfirmDeleteBinding
import com.navio.sketches_and_location.databinding.DialogFileNameBinding
import com.navio.sketches_and_location.databinding.DialogInsertTextBinding
import com.navio.sketches_and_location.databinding.FragmentSketchesLayoutBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FragmentSketches : Fragment() {

    lateinit var binding: FragmentSketchesLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSketchesLayoutBinding.inflate(layoutInflater)
        initViews()
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
        //Stores file in app internal storage
        binding.buttonSave.setOnClickListener {// > Save
            val imageBitmap = BitmapFactory.decodeResource(resources, R.raw.tiger)
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
                        attributes.add(CommentCanvas("x: 150020.123", 0f, 100f * checkCount))
                        checkCount++
                    }
                    if (y.isChecked) {
                        attributes.add(CommentCanvas("y: 144066.025", 0f, 100f * checkCount))
                        checkCount++
                    }
                    if (z.isChecked) {
                        attributes.add(CommentCanvas("z: 230001.567", 0f, 100f * checkCount))
                        checkCount++
                    }
                    if (date.isChecked) {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")
                        attributes.add(
                            CommentCanvas(
                                LocalDateTime.now().format(formatter),
                                0f,
                                100f * checkCount
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
        binding.imageContainer.setImageResource(imageID)
    }

    companion object {
        fun newInstance() = FragmentSketches()
    }
}

//Pass the name from Dialog to button click listener event
interface OnTextPassed {
    fun onTextPassed(text: String)
}

