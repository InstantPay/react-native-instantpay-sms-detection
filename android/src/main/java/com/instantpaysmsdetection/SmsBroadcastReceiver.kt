package com.instantpaysmsdetection

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import org.json.JSONObject

class SmsBroadcastReceiver(var mContext: ReactApplicationContext, var cActivity: Activity) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if(SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action){

            val params = mutableMapOf<String, String>()
            params["status"] = "FAILED"
            params["message"] = "Something went wrong #SBROR1"


            val extras = intent.extras

            if(extras == null){

                params["exceptionMessage"] = "Sms Broadcast intent value of extras is null"

                val outPer = CommonHelper.response(params)

                sendEvent(mContext, "StartSmsListener", outPer)

                return
            }

            val status = extras.get(SmsRetriever.EXTRA_STATUS) as Status

            when(status.statusCode){

                CommonStatusCodes.SUCCESS -> {

                    if(InstantpaySmsDetectionModule.isRequestForConsentSms){

                        val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)

                        try {

                            val expectedAction = "com.google.android.gms.auth.api.phone.ACTION_USER_CONSENT"
                            val expectedPackage = "com.google.android.gms"

                            if (consentIntent?.`package` == expectedPackage &&  consentIntent.action == expectedAction){
                                cActivity.startActivityForResult(consentIntent, InstantpaySmsDetectionModule.SMS_CONSENT_REQUEST_CODE)
                            }
                            else{

                                params["exceptionMessage"] = "Invalid Action Found"

                                val outPer = CommonHelper.response(params)

                                sendEvent(mContext, "StartSmsListener", outPer)
                            }
                            
                        }
                        catch (e : Exception){

                            params["exceptionMessage"] = e.message.toString()

                            val outPer = CommonHelper.response(params)

                            sendEvent(mContext, "StartSmsListener", outPer)
                        }
                    }
                    else{

                        val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE)

                        var simSubscriptionId = extras.get(SmsRetriever.EXTRA_SIM_SUBSCRIPTION_ID)

                        var isPermission = extras.get(SmsRetriever.SEND_PERMISSION)

                        params["status"] = "SUCCESS"

                        params["message"] = "Receiver SMS Successfully"

                        val dataObj = mutableMapOf<String, String>()
                        dataObj["sms"] = message.toString()
                        dataObj["simSubscriptionId"] = simSubscriptionId.toString()
                        dataObj["sendPermission"] = isPermission.toString()

                        params["data"] = JSONObject(dataObj as Map<*, *>).toString()

                        params["actCode"] = "SUCCESS"

                        val outPer = CommonHelper.response(params)

                        sendEvent(mContext, "StartSmsListener", outPer)
                    }

                    return
                }

                CommonStatusCodes.TIMEOUT -> {
                    params["status"] = "FAILED"

                    params["message"] = "Failed to receive SMS"

                    params["actCode"] = "TIMEOUT"

                    val outPer = CommonHelper.response(params)

                    sendEvent(mContext, "StartSmsListener", outPer)
                    return
                }

                CommonStatusCodes.CANCELED -> {
                    params["status"] = "FAILED"

                    params["message"] = "Canceled to process"

                    params["actCode"] = "CANCELED"

                    val outPer = CommonHelper.response(params)

                    sendEvent(mContext, "StartSmsListener", outPer)
                    return
                }

                CommonStatusCodes.ERROR -> {
                    params["status"] = "FAILED"

                    params["actCode"] = "ERROR"

                    val outPer = CommonHelper.response(params)

                    sendEvent(mContext, "StartSmsListener", outPer)
                    return
                }

                else -> {
                    params["status"] = "FAILED"

                    params["message"] = "Failed to receive SMS"

                    params["exceptionMessage"] = "get the status as ${status.statusMessage}"

                    val outPer = CommonHelper.response(params)

                    sendEvent(mContext, "StartSmsListener", outPer)
                }
            }
        }

    }

    /**
     * Send Event to React Native
     */
    private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
        if (reactContext.hasReactInstance()) {

            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        }
    }
}
