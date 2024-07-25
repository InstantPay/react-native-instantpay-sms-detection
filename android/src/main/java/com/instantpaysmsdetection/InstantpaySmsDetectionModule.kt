package com.instantpaysmsdetection

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.android.gms.auth.api.identity.Identity

class InstantpaySmsDetectionModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    val SUCCESS: String = "SUCCESS"
    val FAILED: String = "FAILED"
    lateinit var DATA: String
    private var responsePromise: Promise? = null
    private  var phoneNumberHelper: PhoneNumberHelper
    private var smsHelper: SmsHelper

    override fun getName(): String {
        return NAME
    }

    companion object {
        const val NAME = "InstantpaySmsDetection"

        const val LOG_TAG = "IpaySmsDetectLog*"

        private lateinit var reactContexts: ReactApplicationContext

        const val REQUEST_PHONE_NUMBER_CODE = 101

        const val SMS_CONSENT_REQUEST_CODE = 102

        var isRequestForConsentSms = false
    }

    private val registerActivityResult = object : BaseActivityEventListener() {
        override fun onActivityResult(
            activity: Activity?,
            requestCode: Int,
            resultCode: Int,
            data: Intent?
        ) {
            super.onActivityResult(activity, requestCode, resultCode, data)

            //logPrint("Reached on onActivityResultw ${requestCode}")

            if(requestCode == REQUEST_PHONE_NUMBER_CODE){

                try {

                    val phoneNumber = Identity.getSignInClient(currentActivity!!).getPhoneNumberFromIntent(data)

                    val output: WritableMap = Arguments.createMap()
                    output.putString("phoneNumber", phoneNumber)

                    resolve("Successful", SUCCESS, output)
                }
                catch (e: Exception){
                    resolve(e.message!!.stringOnly(), FAILED)
                }
            }

            if(requestCode == SMS_CONSENT_REQUEST_CODE){

                try {

                    val params = mutableMapOf<String, String>()

                    params["status"] = "FAILED"
                    params["message"] = "Something went wrong #ISDAR1"

                    if(resultCode == Activity.RESULT_OK && data !=null){
                        // Get SMS message content

                        val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)

                        //val simSubscriptionId = data.getStringExtra(SmsRetriever.EXTRA_SIM_SUBSCRIPTION_ID)

                        //val isPermission = data.getStringExtra(SmsRetriever.SEND_PERMISSION)

                        params["status"] = "SUCCESS"

                        params["message"] = "Got the user consent successfully"

                        val dataObj = mutableMapOf<String, String>()
                        dataObj["sms"] = message.toString()
                        dataObj["simSubscriptionId"] = ""
                        dataObj["sendPermission"] =  ""

                        params["data"] = JSONObject(dataObj as Map<String, String>?).toString()

                        params["actCode"] = "ACCEPTED"

                        val outPer = CommonHelper.response(params)

                        sendEvent(reactContexts, "StartSmsListener", outPer)

                    }
                    else{ // Consent denied. User can type OTC manually.

                        params["status"] = "FAILED"

                        params["message"] = "user has denied the consent"

                        params["actCode"] = "REJECTED"

                        val outPer = CommonHelper.response(params)

                        sendEvent(reactContexts, "StartSmsListener", outPer)
                    }
                }
                catch (e: Exception){
                    resolve(e.message!!.stringOnly(), FAILED)
                }
            }

        }
    }

    init {
        reactContexts = reactContext

        phoneNumberHelper = PhoneNumberHelper()

        smsHelper = SmsHelper()

        reactContexts.addActivityEventListener(registerActivityResult);
    }

    @ReactMethod
    fun requestPhoneNumber(promise: Promise) {

        responsePromise = promise

        val activity = currentActivity

        phoneNumberHelper.requestPhoneNumber(reactContexts, activity!!, promise)
    }

    @ReactMethod
    fun startSmsRetriever(promise: Promise) {

        responsePromise = promise

        val activity = currentActivity

        smsHelper.startRetriever(reactContexts, activity!!, promise)
    }

    @ReactMethod
    fun requestSmsConsent(senderPhoneNumber:String? = null, promise: Promise){

        responsePromise = promise

        val activity = currentActivity

        isRequestForConsentSms = true

        smsHelper.requestSmsConsent(senderPhoneNumber, reactContexts, activity!!, promise)
    }

    // Required for rn built in EventEmitter Calls.
    @ReactMethod
    fun addListener(eventName: String?) {
    }

    @ReactMethod
    fun removeListeners(count: Int?) {

    }

    /**
     * Help to return data to React native
     */
    private fun resolve(message: String,status: String = FAILED,data: WritableMap? = null ,actCode: String = "") {

        if (responsePromise == null) {
            return;
        }

        val map: WritableMap = Arguments.createMap()
        map.putString("status", status)
        map.putString("message", message)

        if(data != null){
            map.putMap("data", data)
        }
        else{
            map.putString("data", "")
        }

        responsePromise!!.resolve(map)
        responsePromise = null
    }

    /**
     * Send Event to React Native
     */
    private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
        if (reactContext.hasCatalystInstance()) {

            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        }
    }

    /**
     * extract digit from string and return
     */
    private fun String.digitsOnly(): String{
        val regex = Regex("[^0-9]")
        return regex.replace(this, "")
    }

    /**
     * extract alpha-numeric from string and return
     */
    private fun String.alphaNumericOnly(): String{
        val regex = Regex("[^A-Za-z0-9 ]")
        return regex.replace(this, "")
    }

    /**
     * extract string from string and return
     */
    private fun String.stringOnly(): String{
        val regex = Regex("[^A-Za-z ]")
        return regex.replace(this, "").trim()
    }

    /**
     * For Show Log
     */
    private fun logPrint(value: String?) {
        if (value == null) {
            return
        }
        Log.i(LOG_TAG, value)
    }
}
