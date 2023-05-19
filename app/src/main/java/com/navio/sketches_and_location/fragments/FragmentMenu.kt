package com.navio.sketches_and_location.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.navio.sketches_and_location.OnFragmentChosen
import com.navio.sketches_and_location.R
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
        //Animations
        fadeIn(binding.buttonImageSketches)
        fadeIn(binding.buttonImageLocation)
        //Progress bar
        fadeOut(binding.loadingAnimation)
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

    //Animations
    private fun fadeIn(view: View) {
        view.animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        view.animate()
    }
    private fun fadeOut(view: View) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Animation start
                view.visibility = VISIBLE
            }
            override fun onAnimationEnd(animation: Animation?) {
                // Animation end
                view.visibility = INVISIBLE // or View.GONE to make it completely hidden
            }
            override fun onAnimationRepeat(animation: Animation?) {
                // Animation repeat
            }
        })
        view.startAnimation(animation)
    }

    companion object {
        fun newInstance(listener: OnFragmentChosen) = FragmentMenu(listener)
    }
}
