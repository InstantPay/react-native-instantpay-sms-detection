package com.instantpaysmsdetection

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener


class SmsHelper {

    private val SUCCESS: String = "SUCCESS"
    private val FAILED: String = "FAILED"
    private lateinit var DATA: String
    private var responsePromise: Promise? = null
    private var mContext: ReactApplicationContext? = null
    private var mReceiver: BroadcastReceiver? = null
    private var cActivity: Activity? = null

    private val onSuccessListener = OnSuccessListener<Void> {
        val isRegister: Boolean = tryToRegisterReceiver()

        var isSuccess = "Unknow"
        if(isRegister){
            isSuccess = "Success"
        }

        resolve("${isSuccess}, Please check on listener", SUCCESS, "", "ListenForSmsDetect")
    }

    private val onFailureListener = OnFailureListener {
        unregisterReceiverIfNeeded()
        resolve("Failed to register the SMS listener", FAILED)
    }

    /**
     * Start Method to register service
     */
    fun startRetriever(context : ReactApplicationContext, activity : Activity, promise: Promise){

        responsePromise = promise

        mContext = context

        cActivity = activity

        val smsRetrieverClient = SmsRetriever.getClient(context)

        val tasks = smsRetrieverClient.startSmsRetriever()

        tasks.addOnSuccessListener(onSuccessListener)

        tasks.addOnFailureListener(onFailureListener)
    }

    /**
     * Request one-time consent to read an SMS verification code
     */
    fun requestSmsConsent(context : ReactApplicationContext, activity : Activity, promise: Promise){

        responsePromise = promise

        mContext = context

        cActivity = activity

        val smsRetrieverClient = SmsRetriever.getClient(context)

        val tasks = smsRetrieverClient.startSmsUserConsent(null)

        tasks.addOnSuccessListener(onSuccessListener)

        tasks.addOnFailureListener(onFailureListener)

    }

    /**
     * Register Broadcast to read sms
     */
    private fun tryToRegisterReceiver() : Boolean{

        mReceiver = SmsBroadcastReceiver(mContext!!, cActivity!!)

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)

        try {

            if(Build.VERSION.SDK_INT >= 34 && mContext!!.applicationInfo.targetSdkVersion >= 34){
                mContext!!.registerReceiver(mReceiver, intentFilter, Context.RECEIVER_EXPORTED)
            }
            else{
                mContext!!.registerReceiver(mReceiver, intentFilter)
            }

            return true
        }
        catch (e: Exception){
            resolve("Something went wrong #TRRSDP1", FAILED, "", "", e.message.toString())
            return false
        }
    }

    /**
     * Un-Register the Broadcast
     */
    private fun unregisterReceiverIfNeeded(){

        if (mReceiver == null) {
            return;
        }

        try {
            mContext!!.unregisterReceiver(mReceiver);
        } catch (e: Exception) {
            resolve("Something went wrong #URRSDP1", FAILED, "", "", e.message.toString())
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
