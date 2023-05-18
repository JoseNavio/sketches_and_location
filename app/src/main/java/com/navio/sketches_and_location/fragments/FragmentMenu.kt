package com.navio.sketches_and_location.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.navio.sketches_and_location.OnFragmentChosen
import com.navio.sketches_and_location.databinding.FragmentMenuLayoutBinding

class FragmentMenu(private val listener: OnFragmentChosen) : Fragment() {

    private lateinit var binding: FragmentMenuLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentMenuLayoutBinding.inflate(layoutInflater)
        setButtons()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.apply {
//          hide()
            title = "Sketches and Location"
            subtitle = "Menú"
        }
        return binding.root
    }

    private fun setButtons() {
        binding.buttonImageSketches.setOnClickListener {
            listener.onSketchesChosen()
        }
        binding.buttonImageLocation.setOnClickListener {
            listener.onLocationChosen()
        }
    }

    companion object {
        fun newInstance(listener: OnFragmentChosen) = FragmentMenu(listener)
    }
}