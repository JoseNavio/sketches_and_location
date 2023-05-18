package com.navio.sketches_and_location.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.navio.sketches_and_location.databinding.FragmentLocationLayoutBinding
import com.navio.sketches_and_location.databinding.FragmentSketchesLayoutBinding

class FragmentLocation : Fragment() {

    lateinit var binding: FragmentLocationLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentLocationLayoutBinding.inflate(layoutInflater)
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
            subtitle = "Location"
        }
        return binding.root
    }
    //Set up layout buttons
    private fun setButtons() {

    }

    companion object {
        fun newInstance() = FragmentLocation()
    }
}