package com.mtnance.phoneblock

import android.Manifest
import android.app.role.RoleManager
import android.app.role.RoleManager.ROLE_CALL_SCREENING
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mtnance.phoneblock.helpers.ScreeningPreferences
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    companion object {
        private const val REQUEST_ID_BECOME_CALL_SCREENER = 1
        private const val REQUEST_ID_REQUEST_READ_CONTACTS_PERMISSION = 1

        private const val EXTRA_CONTACT_READ_PERMISSION_DENIED = "contact_permission_denied_forever"
    }

    private val roleManager by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getSystemService(RoleManager::class.java)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
    }

    private val prefs by lazy { ScreeningPreferences(this) }

    private val hasCallScreeningRole: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            roleManager.isRoleHeld(ROLE_CALL_SCREENING)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }

    private val readContactsPermissionGranted: Boolean
        get() = EasyPermissions.hasPermissions(this, Manifest.permission.READ_CONTACTS)

    private var contactsAccessDeniedForever = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contactsAccessDeniedForever = savedInstanceState
            ?.getBoolean(EXTRA_CONTACT_READ_PERMISSION_DENIED, false) ?: false

        addUiListeners()
        updateUi()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ID_BECOME_CALL_SCREENER) {
            updateUi()

            when (resultCode) {
                RESULT_OK -> Timber.i("Role was granted")
                else -> Timber.e("Role was not granted")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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
                intent.data = Uri.fromParts("package", this.packageName, null)
                startActivity(intent)
                Toast.makeText(this, "", Toast.LENGTH_LONG).show()
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "", Toast.LENGTH_LONG).show()
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
            roleManager.createRequestRoleIntent(ROLE_CALL_SCREENING)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        startActivityForResult(intent,
            REQUEST_ID_BECOME_CALL_SCREENER
        )
    }

    private fun addUiListeners() {

    }

    private fun updateUi() {

    }
}