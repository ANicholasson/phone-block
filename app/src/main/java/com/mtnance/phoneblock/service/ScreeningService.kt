package com.mtnance.phoneblock.service

import android.os.Build
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
            var validLandCode = ""
            for (landCode in Constants.WHITELISTED_COUNTRY_CODES) {
                if (phoneNumber.startsWith(landCode)) {
                    validLandCode = landCode
                }
            }

            if (validLandCode.isNullOrEmpty()) {
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
}