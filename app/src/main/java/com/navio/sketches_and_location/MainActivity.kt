package com.navio.sketches_and_location

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.navio.sketches_and_location.databinding.ActivityMainBinding
import com.navio.sketches_and_location.fragments.FragmentLocation
import com.navio.sketches_and_location.fragments.FragmentMenu
import com.navio.sketches_and_location.fragments.FragmentSketches

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Hide top bar or modify it --> Not visible
        supportActionBar?.let { actionBar ->
//          actionBar.hide()
            actionBar.title = "Sketches and Location"
            actionBar.subtitle = "Jose Luis Navío Mendoza"
        }
        //Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        attachMenuFragment()
    }
    //Replace fragments
    //Launches menu fragment with a callback to listen which button has been click on it
    private fun attachMenuFragment() {

        supportFragmentManager.commit {
            setReorderingAllowed(true)//Let commit operations decide better operation's order
            replace(
                binding.fragmentContainerActivity.id,
                FragmentMenu.newInstance(object : OnFragmentChosen {
                    override fun onSketchesChosen() {
                        attachSketchesFragment()
                    }

                    override fun onLocationChosen() {
                        attachLocationFragment()
                    }
                })
            )
        }
    }
    //Launches sketches fragment
    private fun attachSketchesFragment() {

        supportFragmentManager.commit {
            setReorderingAllowed(true)//Let commit operations decide better operation's order
            replace(binding.fragmentContainerActivity.id, FragmentSketches.newInstance())
            addToBackStack(null)
        }
    }
    //Launches location fragment
    private fun attachLocationFragment() {

        supportFragmentManager.commit {
            setReorderingAllowed(true)//Let commit operations decide better operation's order
            replace(binding.fragmentContainerActivity.id, FragmentLocation.newInstance())
            addToBackStack(null)
        }
    }
}

interface OnFragmentChosen {
    fun onLocationChosen()
    fun onSketchesChosen()
}
