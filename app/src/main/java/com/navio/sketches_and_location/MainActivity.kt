package com.navio.sketches_and_location

import android.os.Bundle
import android.util.Log
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

    private fun setButtons() {
        //Clears window
        binding.buttonClear.setOnClickListener {
            binding.drawingView.clear()
        }
        //Stores file in app internal storage
        //Use a callback to get the names instead of setting both Dialogs
        binding.buttonSave.setOnClickListener {

            showNameDialog(object : OnNamePassed{

                override fun onNamePassed(name: String) {
                    saveAfterGettingName(name)
                }
            })
        }
        //Stores file in gallery
        binding.buttonAddToGallery.setOnClickListener {

            showNameDialog(object : OnNamePassed{

                override fun onNamePassed(name: String) {
                    binding.drawingView.addImageToGallery(name)
                }
            })
        }
    }
    private fun showNameDialog(callback: OnNamePassed) {

        val dialogBinding: DialogFileNameBinding = DialogFileNameBinding.inflate(layoutInflater)
        var fileName = "imagen"

        dialogBinding.labelComment.text = "Nombre del archivo: "

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("GUARDAR IMAGEN")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                fileName = dialogBinding.fieldFileName.text.toString()
                callback.onNamePassed(fileName)
            }
            .setNegativeButton("Cancelar", null)
            .create()
        alertDialog.show()
    }
    private fun saveAfterGettingName(name: String) {

        //If name is blank --> "imagen" else "name"
        val fileName = name.ifBlank { "imagen" }

        val savedFile = binding.drawingView.saveDrawing(fileName)
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
//Pass the name from Dialog to button click listener event
interface OnNamePassed{
    fun onNamePassed(name : String)
}