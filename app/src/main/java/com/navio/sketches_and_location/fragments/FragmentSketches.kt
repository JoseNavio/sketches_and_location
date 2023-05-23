package com.navio.sketches_and_location.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.navio.sketches_and_location.R
import com.navio.sketches_and_location.databinding.DialogFileNameBinding
import com.navio.sketches_and_location.databinding.DialogInsertTextBinding
import com.navio.sketches_and_location.databinding.FragmentSketchesLayoutBinding

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
//          hide()
            title = "Sketches and Location"
            subtitle = "Sketches"
        }
        return binding.root
    }

    //Init views related task and configuration
    private fun initViews() {

    }

    //Set up layout buttons
    private fun setButtons() {
        //Allow to draw on screen
        binding.buttonDraw.setOnClickListener {
            binding.drawingView.startDrawing()
        }
        //Clears window
        binding.buttonClear.setOnClickListener {
            binding.drawingView.undo()
        }
        binding.buttonClear.setOnLongClickListener{
            binding.drawingView.clear()
            true
        }
        //Stores file in app internal storage
        //Use a callback to get the names instead of setting both Dialogs
        binding.buttonSave.setOnClickListener {

            //todo Try to save changes into image...
            val imageBitmap = BitmapFactory.decodeResource(resources, R.raw.tiger)

            showNameDialog(object : OnTextPassed {

                override fun onTextPassed(name: String) {
                    saveAfterGettingName(name, imageBitmap)
                }
            })
        }
        //Stores file in gallery
        binding.buttonAddImage.setOnClickListener {
            //Set image
            visualizeImage(R.raw.tiger)
        }
        //Introduces a text on canvas
        binding.buttonText.setOnClickListener {
            //todo Need to call the startWriting
            binding.drawingView.startWriting()
        }
    }

//    private fun writeComment(callback: OnParametersChosen) {
//
//        showTextDialog { comment ->
//            callback.onCommentChosen(comment)
//            Toast.makeText(context, "Selecciona un punto en la pantalla", Toast.LENGTH_SHORT).show()
//        }
//
////        binding.drawingView.startWriting()
//    }

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

    private fun showTextDialog(callback: (String) -> Unit) {

        val dialogTextBinding: DialogInsertTextBinding =
            DialogInsertTextBinding.inflate(layoutInflater)

        dialogTextBinding.labelCommentInsertText.text = "Introduce un comentario: "

        val alertDialogText = context?.let {
            AlertDialog.Builder(it)
                .setTitle("INSERTAR TEXTO")
                .setView(dialogTextBinding.root)
                .setPositiveButton("Aceptar") { _, _ ->
                    //Return written comment
                    callback(dialogTextBinding.fieldInsertText.text.toString())
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
        }
        alertDialogText?.show()
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
    fun onTextPassed(name: String)
}

interface OnScreenTouched {
    fun onScreenClicked(x: Float, y: Float, comment: OnTextPassed)
}
