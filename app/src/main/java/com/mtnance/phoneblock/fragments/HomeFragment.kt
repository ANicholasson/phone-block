package com.mtnance.phoneblock.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.mtnance.phoneblock.R
import com.mtnance.phoneblock.helpers.ScreeningPreferences
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private val prefs by lazy { ScreeningPreferences(requireActivity()) }

    private lateinit var fragmentView: View
    private lateinit var toggleListType: MaterialButtonToggleGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_home, container, false)
        toggleListType = fragmentView.findViewById(R.id.toggleListType);

        // Select the already selected (stored) list mode
        when (prefs.listMode) {
            "whitelist" -> toggleListType.check(R.id.whitelist_btn)
            "blacklist" -> toggleListType.check(R.id.blacklist_btn)
        }

        // Configure button group for toggling list mode
        toggleListType.addOnButtonCheckedListener { listTypeBtnGroup, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.whitelist_btn -> showConfirmation("whitelist")
                    R.id.blacklist_btn -> showConfirmation("blacklist")
                }
            } else {
                if (listTypeBtnGroup.checkedButtonId == View.NO_ID) {
                    Timber.tag("List Mode").i("Already selected mode got selected.")
                }
            }
        }

        // Return the fragment view/layout
        return fragmentView;
    }

    private fun showConfirmation(mode: String) {
        // TODO Change following to use strings.xml instead of "hardcoded" values
        val dialogBuilder = AlertDialog.Builder(this.context)
        dialogBuilder.setMessage("Are you sure you want to change active list to type $mode")
            .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                when (mode) {
                    "whitelist" -> prefs.listMode = "whitelist"
                    "blacklist" -> prefs.listMode = "blacklist"
                }
            }
            .setNegativeButton("No") { dialogInterface: DialogInterface?, _: Int ->
                // TODO Choose previously selected button / cancel event
                dialogInterface?.cancel()
            }
        // Create dialog
        dialogBuilder.create()
        dialogBuilder.show()
    }
}