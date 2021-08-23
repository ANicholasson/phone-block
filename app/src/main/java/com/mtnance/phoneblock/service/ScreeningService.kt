package com.mtnance.phoneblock.service

import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.PhoneNumberUtils
import com.mtnance.phoneblock.helpers.ScreeningPreferences
import timber.log.Timber
import java.util.*

class ScreeningService : CallScreeningService() {
    private val prefs by lazy { ScreeningPreferences(this) }

    /**
     * Called when a new incoming or outgoing call is added which is not in the user's contact list.
     */
    override fun onScreenCall(details: Call.Details) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }

        if (details.callDirection == Call.Details.DIRECTION_INCOMING) {
            val phoneNumber = details.handle.schemeSpecificPart
            val formattedPhoneNumber = PhoneNumberUtils.formatNumber(
                phoneNumber,
                Locale.getDefault().country
            )
            val caller = getContactName(phoneNumber)
            if (caller == null) {
                respondToCall(details, buildResponse())
                Timber.tag("📞🔫🤖").w("Automatically declined unknown caller: $formattedPhoneNumber")
            } else {
                Timber.tag("📞🔫🤖").i("Did not screen known caller: $caller ($formattedPhoneNumber)")
            }
        }
    }

    private fun buildResponse(): CallResponse {
        return CallResponse.Builder()
            .setDisallowCall(prefs.isServiceEnabled)
            .setRejectCall(prefs.isServiceEnabled)
            .setSkipNotification(prefs.skipCallNotification)
            .setSkipCallLog(prefs.skipCallLog)
            .build()
    }

    private fun getContactName(phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return cursor?.use {
            when (cursor.moveToFirst()) {
                true -> cursor.getString(0)
                else -> null
            }
        }
    }
}