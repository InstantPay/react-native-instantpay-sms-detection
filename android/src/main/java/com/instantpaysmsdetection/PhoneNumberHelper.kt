package com.instantpaysmsdetection


import com.facebook.react.bridge.ReactApplicationContext
import android.content.Context
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.autofill.HintConstants
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Api

//import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest

class PhoneNumberHelper {

    private val SUCCESS: String = "SUCCESS"
    private val FAILED: String = "FAILED"
    private lateinit var DATA: String
    private var responsePromise: Promise? = null

    fun requestPhoneNumber(context : ReactApplicationContext, activity : Activity, promise: Promise){

        responsePromise = promise

        try {

            val hintRequest = GetPhoneNumberHintIntentRequest.builder().build()

            val signInClient = Identity.getSignInClient(activity)

            signInClient.getPhoneNumberHintIntent(hintRequest)
                .addOnSuccessListener { result: PendingIntent ->
                    try {

                        activity.startIntentSenderForResult(
                            result.intentSender,
                            InstantpaySmsDetectionModule.REQUEST_PHONE_NUMBER_CODE,
                            null,
                            0 ,
                            0,
                            0
                        )

                    } catch (e: Exception) {
                        resolve("Launching the Intent failed", FAILED, "", "", e.message.toString())
                    }
                }
                .addOnFailureListener {
                    resolve("Phone Number Hint failed", FAILED, "", "PhoneNumberSuggestion", "Please allow phone number sharing from setting -> google -> autofill -> phone number sharing")
                }

        }
        catch (e : Exception){
            resolve("Something went wrong #PNHSD1", FAILED, "", "", e.message.toString())
        }
    }

    /**
     * Help to return data to React native
     */
    private fun resolve(message: String,status: String = FAILED,data: String = "",actCode: String = "", exceptionMessage: String = "") {

        if (responsePromise == null) {
            return;
        }

        val map: WritableMap = Arguments.createMap()
        map.putString("status", status)
        map.putString("message", message)

        if(data.isNotEmpty()){
            map.putString("data", data)
        }

        if(actCode.isNotEmpty()){
            map.putString("actCode", actCode)
        }

        if(exceptionMessage.isNotEmpty()){
            map.putString("exceptionMessage", exceptionMessage)
        }

        responsePromise!!.resolve(map)
        responsePromise = null
    }

}
