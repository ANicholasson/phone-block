package com.mtnance.phoneblock.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.mtnance.phoneblock.R
import kotlinx.android.synthetic.main.fragment_home.view.*

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(activity, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(activity, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(activity, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(activity, R.anim.to_bottom_anim) }

    private var clicked = false
    private lateinit var fragmentView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_home, container, false)

        fragmentView.new_item_menu_btn.setOnClickListener {
            onAddButtonClicked()
        }
        fragmentView.new_number_btn.setOnClickListener {
            Toast.makeText(activity, "NOT YET IMPLEMENTED", Toast.LENGTH_SHORT).show()
        }
        fragmentView.new_land_code_btn.setOnClickListener {
            Toast.makeText(activity, "NOT YET IMPLEMENTED", Toast.LENGTH_SHORT).show()
        }

        // Return the fragment view/layout
        return fragmentView;
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            fragmentView.new_number_btn.visibility = View.VISIBLE
            fragmentView.new_land_code_btn.visibility = View.VISIBLE
        } else {
            fragmentView.new_number_btn.visibility = View.INVISIBLE
            fragmentView.new_land_code_btn.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            fragmentView.new_number_btn.startAnimation(fromBottom)
            fragmentView.new_land_code_btn.startAnimation(fromBottom)
            fragmentView.new_item_menu_btn.startAnimation(rotateOpen)
        } else {
            fragmentView.new_number_btn.startAnimation(toBottom)
            fragmentView.new_land_code_btn.startAnimation(toBottom)
            fragmentView.new_item_menu_btn.startAnimation(rotateClose)
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            fragmentView.new_number_btn.isClickable = true
            fragmentView.new_land_code_btn.isClickable = true
        } else {
            fragmentView.new_number_btn.isClickable = false
            fragmentView.new_land_code_btn.isClickable = false
        }
    }
}