package com.mtnance.phoneblock.fragments

import android.Manifest
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.mtnance.phoneblock.R
import com.mtnance.phoneblock.helpers.ScreeningPreferences
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    companion object {
        private const val REQUEST_ID_BECOME_CALL_SCREENER = 1
        private const val REQUEST_ID_REQUEST_READ_CONTACTS_PERMISSION = 1

        private const val EXTRA_CONTACT_READ_PERMISSION_DENIED = "contact_permission_denied_forever"
    }

    private lateinit var fragmentView: View

    private val roleManager by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.getSystemService(RoleManager::class.java)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
    }

    private val prefs by lazy { ScreeningPreferences(requireActivity()) }

    private val hasCallScreeningRole: Boolean?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }

    private val readContactsPermissionGranted: Boolean
        get() = EasyPermissions.hasPermissions(requireActivity(), Manifest.permission.READ_CONTACTS)

    private var contactsAccessDeniedForever = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        contactsAccessDeniedForever = savedInstanceState
            ?.getBoolean(EXTRA_CONTACT_READ_PERMISSION_DENIED, false) ?: false

        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_settings, container, false)

        addUiListeners()
        updateUi()

        // Return the fragment view/layout
        return fragmentView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ID_BECOME_CALL_SCREENER) {
            updateUi()

            when (resultCode) {
                AppCompatActivity.RESULT_OK -> Timber.i("Role was granted")
                else -> Timber.e("Role was not granted")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_CONTACT_READ_PERMISSION_DENIED, contactsAccessDeniedForever)
    }

    //<editor-fold desc="EasyPermissions.PermissionCallbacks">
    override fun onPermissionsGranted(requestCode: Int, grantedPermissions: List<String>) {
        if (Manifest.permission.READ_CONTACTS in grantedPermissions) {
            requestRole()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, deniedPermissions: List<String>) {
        // only in onPermissionsDenied() is it certain if a permission has been permanently denied
        if (Manifest.permission.READ_CONTACTS in deniedPermissions) {
            contactsAccessDeniedForever = EasyPermissions.permissionPermanentlyDenied(
                this,
                Manifest.permission.READ_CONTACTS
            )
        }
    }
    //</editor-fold>

    private fun requestContactsPermission() {
        if (contactsAccessDeniedForever) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", activity?.packageName, null)
                startActivity(intent)
                Toast.makeText(activity, "", Toast.LENGTH_LONG).show()
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, "", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_ID_REQUEST_READ_CONTACTS_PERMISSION
            )
        }
    }

    private fun requestRole() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        startActivityForResult(
            intent,
            REQUEST_ID_BECOME_CALL_SCREENER
        )
    }

    private fun addUiListeners() {
        fragmentView.activateButton.setOnClickListener {
            requestContactsPermission()
        }

        fragmentView.enableSwitch.setOnCheckedChangeListener { _, enabled ->
            prefs.isServiceEnabled = enabled
            updateUi()
        }
        fragmentView.skipNotificationSwitch.setOnCheckedChangeListener { _, skip ->
            prefs.skipCallNotification = skip
            updateUi()
        }
        fragmentView.skipCallLogSwitch.setOnCheckedChangeListener { _, skip ->
            prefs.skipCallLog = skip
            updateUi()
        }
    }

    private fun updateUi() {
        val isInstalled = readContactsPermissionGranted && hasCallScreeningRole == true
        fragmentView.statusLabel.text = when (isInstalled) {
            true -> getString(R.string.status_activated)
            else -> getString(R.string.status_inactive)
        }
        fragmentView.activateButton.isVisible = isInstalled.not()
        fragmentView.enableSwitch.isVisible = isInstalled
        fragmentView.skipNotificationSwitch.isVisible = isInstalled
        fragmentView.skipCallLogSwitch.isVisible = isInstalled

        fragmentView.enableSwitch.isChecked = prefs.isServiceEnabled

        fragmentView.skipNotificationSwitch.isEnabled = prefs.isServiceEnabled
        fragmentView.skipNotificationSwitch.isChecked = prefs.skipCallNotification

        fragmentView.skipCallLogSwitch.isEnabled = prefs.isServiceEnabled
        fragmentView.skipCallLogSwitch.isChecked = prefs.skipCallLog
    }
}