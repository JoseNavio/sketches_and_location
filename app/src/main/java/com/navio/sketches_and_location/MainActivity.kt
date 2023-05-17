package com.navio.sketches_and_location

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.navio.sketches_and_location.databinding.ActivityMainBinding
import com.navio.sketches_and_location.databinding.DialogFileNameBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Hide top bar or modify it
        supportActionBar?.let { actionBar ->
//            actionBar.hide()
            actionBar.title = "Sketches"
            actionBar.subtitle= "Jose Luis Navío Mendoza"
        }
        //Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setButtons()
    }

    private fun launchNameDialog(): String {

        val dialogBinding: DialogFileNameBinding = DialogFileNameBinding.inflate(layoutInflater)
        var fileName = "imagen"

        dialogBinding.labelComment.text = "Nombre del archivo: "

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("GUARDAR IMAGEN")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                fileName = dialogBinding.fieldFileName.text.toString()
                saveAfterGettingName(fileName)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        alertDialog.show()
        return fileName
    }

    private fun setButtons() {
        //Clears window
        binding.buttonClear.setOnClickListener {
            binding.drawingCanvas.clear()
        }
        //Stores file in internal storage
        binding.buttonSave.setOnClickListener {
            launchNameDialog()
        }
        //Stores file in gallery
        binding.buttonAddToGallery.setOnClickListener {
            binding.drawingCanvas.addImageToGallery("provisional")
        }
    }

    private fun saveAfterGettingName(name: String) {

        //If name is blank --> "imagen" else "name"
        val fileName = name.ifBlank { "imagen" }

        val savedFile = binding.drawingCanvas.saveDrawing(fileName)
        if (savedFile != null) {
            Toast.makeText(this, "Imagen guardada ${savedFile.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "La aplicación fallo al tratar de guardar la imagen",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}