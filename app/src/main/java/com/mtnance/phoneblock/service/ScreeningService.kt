package com.mtnance.phoneblock.service

import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.PhoneNumberUtils
import com.mtnance.phoneblock.helpers.Constants
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
            val countryCode = Locale.getDefault().country
            val formattedPhoneNumber = PhoneNumberUtils.formatNumber(
                phoneNumber,
                countryCode
            )

            // Check if the phone number is stored in contacts
            val caller = getContactName(phoneNumber)
            if (caller != null)
                return;

            // TODO If caller is not in contact list, check against whitelist/blacklist

            var validLandCode = ""
            for (landCode in Constants.WHITELISTED_COUNTRY_CODES) {
                if (phoneNumber.startsWith(landCode)) {
                    validLandCode = landCode
                }
            }

            if (validLandCode.isEmpty()) {
                respondToCall(details, buildResponse())
                Timber.tag("📞🔫🤖")
                    .w("Automatically declined unknown caller: $formattedPhoneNumber")
            } else {
                Timber.tag("📞🔫🤖").i("Did not screen known caller: ($formattedPhoneNumber)")
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

    /**
     * Method for getting the name of an existing contact matching given phone number
     */
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