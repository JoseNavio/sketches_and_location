package com.navio.sketches_and_location.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.navio.sketches_and_location.databinding.DialogFileNameBinding
import com.navio.sketches_and_location.databinding.FragmentSketchesLayoutBinding

class FragmentSketches : Fragment() {

    lateinit var binding: FragmentSketchesLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSketchesLayoutBinding.inflate(layoutInflater)
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
    //Set up layout buttons
    private fun setButtons() {

        //Clears window
        binding.buttonClear.setOnClickListener {
            binding.drawingView.clear()
        }
        //Stores file in app internal storage
        //Use a callback to get the names instead of setting both Dialogs
        binding.buttonSave.setOnClickListener {

            showNameDialog(object : OnNamePassed {

                override fun onNamePassed(name: String) {
                    saveAfterGettingName(name)
                }
            })
        }
        //Stores file in gallery
        binding.buttonAddToGallery.setOnClickListener {

            showNameDialog(object : OnNamePassed {

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

        val alertDialog = context?.let {
            AlertDialog.Builder(it)
                .setTitle("GUARDAR IMAGEN")
                .setView(dialogBinding.root)
                .setPositiveButton("Guardar") { _, _ ->
                    fileName = dialogBinding.fieldFileName.text.toString()
                    callback.onNamePassed(fileName)
                }
                .setNegativeButton("Cancelar", null)
                .create()
        }
        alertDialog?.show()
    }
    private fun saveAfterGettingName(name: String) {

        //If name is blank --> "imagen" else "name"
        val fileName = name.ifBlank { "imagen" }

        val savedFile = binding.drawingView.saveDrawing(fileName)
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
    companion object {
        fun newInstance() = FragmentSketches()
    }
}
//Pass the name from Dialog to button click listener event
interface OnNamePassed{
    fun onNamePassed(name : String)
}