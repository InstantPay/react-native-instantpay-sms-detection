package com.instantpaysmsdetection

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.phone.SmsRetriever
import org.json.JSONObject

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

    @ReactMethod
    fun simCardsInfo(options:String? = null, promise: Promise){

        responsePromise = promise

        val subscriptionManager = reactContexts.getSystemService(SubscriptionManager::class.java)

        val hasPermission = reactContexts.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)

        if(PackageManager.PERMISSION_GRANTED == hasPermission){

            val infoList = subscriptionManager?.activeSubscriptionInfoList

            if(infoList!=null && infoList.size > 0){

                val simInfoList: WritableArray = Arguments.createArray()

                for (info in infoList){

                    val itemList: WritableMap = Arguments.createMap()

                    itemList.putString("displayName", info.displayName.toString())

                    itemList.putString("simSlotIndex", info.simSlotIndex.toString())

                    itemList.putString("iccId", info.iccId)

                    itemList.putString("carrierName", info.carrierName.toString())

                    itemList.putString("countryIso", info.countryIso)

                    if(SubscriptionManager.DATA_ROAMING_ENABLE == info.dataRoaming){
                        itemList.putString("dataRoaming", "ENABLE")
                    }
                    else{
                        itemList.putString("dataRoaming", "DISABLE")
                    }

                    itemList.putString("iconTint", info.iconTint.toString())

                    itemList.putString("subscriptionId", info.subscriptionId.toString())

                    if(Build.VERSION.SDK_INT >= 29){

                        itemList.putString("cardId", info.cardId.toString())

                        itemList.putString("carrierId", info.carrierId.toString())

                        itemList.putString("groupUuid", info.groupUuid.toString())

                        itemList.putString("eSim", info.isEmbedded.toString())

                        itemList.putString("isOpportunistic", info.isOpportunistic.toString())

                        itemList.putString("mobileCountryCode", info.mccString.toString())

                        itemList.putString("mobileNetworkCode", info.mncString.toString())

                        if(SubscriptionManager.SUBSCRIPTION_TYPE_LOCAL_SIM == info.subscriptionType){
                            itemList.putString("subscriptionType", "LOCAL_SIM")
                        }
                        else if(SubscriptionManager.SUBSCRIPTION_TYPE_REMOTE_SIM == info.subscriptionType){
                            itemList.putString("subscriptionType", "REMOTE_SIM")
                        }
                        else{
                            itemList.putString("subscriptionType", "")
                        }
                    }
                    else{

                        itemList.putString("cardId", "")

                        itemList.putString("carrierId", "")

                        itemList.putString("groupUuid", "")

                        itemList.putString("eSim", "")

                        itemList.putString("isOpportunistic", "")

                        itemList.putString("mobileCountryCode", "")

                        itemList.putString("mobileNetworkCode", "")

                        itemList.putString("subscriptionType", "")
                    }

                    if(Build.VERSION.SDK_INT >=33){

                        itemList.putString("portIndex", info.portIndex.toString())

                        itemList.putString("usageSetting", info.usageSetting.toString())
                    }
                    else{
                        itemList.putString("portIndex", "")

                        itemList.putString("usageSetting", "")
                    }

                    simInfoList.pushMap(itemList)
                }

                return resolve("Success", SUCCESS, simInfoList)
            }
            else{
                return resolve("No SIM Found on the device", FAILED, null, "NoSim")
            }
        }
        else{
            return resolve("Permission not found [READ_PHONE_STATE]", FAILED, null, "PermissionNotFound")
        }
    }

    @ReactMethod
    fun simCardPhoneNumber(subscriptionId:Int, promise: Promise){

        responsePromise = promise

        val subscriptionManager = reactContexts.getSystemService(SubscriptionManager::class.java)

        if(Build.VERSION.SDK_INT >=33){

            val hasPhoneNumberPermission = reactContexts.checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS)

            if(hasPhoneNumberPermission == PackageManager.PERMISSION_GRANTED){
                val phoneNumber = subscriptionManager.getPhoneNumber(subscriptionId);

                val outputData: WritableMap = Arguments.createMap()

                outputData.putString("phoneNumber", phoneNumber)

                return resolve("Success",SUCCESS, outputData)
            }
            else{
                return resolve("Permission not found [READ_PHONE_NUMBERS]", FAILED, null, "PermissionNotFound")
            }
        }
        else{
            return resolve("Not Supported in current android version [Available from Android 13]", FAILED, null, "NotSupported")
        }
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

    private fun resolve(message: String,status: String = FAILED,data: WritableArray ,actCode: String = ""){

        if (responsePromise == null) {
            return;
        }

        val map: WritableMap = Arguments.createMap()
        map.putString("status", status)
        map.putString("message", message)

        if (data != null) {
            if(data.size() > 0){
                map.putArray("data",data)
            }
        }
        else{
            map.putString("data","")
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

    fun convertArrayListToWritableMap(arrayList: ArrayList<MutableMap<String, String>>): WritableArray {
        val writableArray: WritableArray = Arguments.createArray()

        for (map in arrayList) {
            val writableMap: WritableMap = Arguments.createMap()
            for ((key, value) in map) {
                writableMap.putString(key, value)
            }
            writableArray.pushMap(writableMap)
        }

        return writableArray
    }
}
