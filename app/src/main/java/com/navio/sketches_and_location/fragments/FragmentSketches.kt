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
        binding.buttonDraw.setOnClickListener {// > Draw
            binding.drawingView.startDrawing()
        }
        //Introduces a text on canvas
        binding.buttonText.setOnClickListener {// > Write
            writeComment()
        }
        binding.buttonText.setOnLongClickListener{
            //todo Provisional
            binding.drawingView.drawCoordinates(binding.imageContainer);
            true
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
        //Shows image
        binding.buttonAddImage.setOnClickListener {// > Image
            //Set image
            visualizeImage(R.raw.tiger)
        }
        //Undo last change
        binding.buttonClear.setOnClickListener {// > Undo
            binding.drawingView.undo()
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
                binding.drawingView.startWriting(comment)
                Toast.makeText(context, "Selecciona un punto en la pantalla", Toast.LENGTH_SHORT).show()
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

        dialogTextBinding.labelCommentInsertText.text = "Introduce un comentario: "

        val alertDialogText = context?.let {
            AlertDialog.Builder(it)
                .setTitle("INSERTAR TEXTO")
                .setView(dialogTextBinding.root)
                .setPositiveButton("Aceptar") { _, _ ->
                    //Return written comment
                    callback.onTextPassed(dialogTextBinding.fieldInsertText.text.toString())
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
    fun onTextPassed(text: String)
}

