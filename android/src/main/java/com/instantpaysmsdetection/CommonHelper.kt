package com.instantpaysmsdetection

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

object CommonHelper {

    const val LOG_TAG = "IpaySmsDetectLog*"

    fun logPrint(value: String?) {
        if (value == null) {
            return
        }
        Log.i(LOG_TAG, value)
    }

    /**
     * Make Output before send to React Native
     */
    fun response(params : MutableMap<String, String>) : WritableMap {

        val map: WritableMap = Arguments.createMap()
        //map.putString("status", status)

        var statusType = ""

        if(params.containsKey("status") && params["status"]!!.isNotEmpty()){
            statusType = ""
            map.putString("status", params["status"])
        }
        else{
            statusType = "FAILED"
            map.putString("status", "FAILED")
        }

        if(params.containsKey("message") && params["message"]!!.isNotEmpty()){
            map.putString("message", params["message"])
        }
        else{

            if(statusType == "FAILED"){
                map.putString("message", "Something went wrong, #ISD")
            }
            else{
                map.putString("message", "")
            }
        }

        if(params.containsKey("actCode") && params["actCode"]!!.isNotEmpty()){
            map.putString("actCode", params["actCode"])
        }
        else{
            map.putString("actCode", "")
        }

        if(params.containsKey("data")){
            map.putString("data", params["data"])
        }
        else{
            map.putString("data", "")
        }

        if(params.containsKey("exceptionMessage") && params["exceptionMessage"]!!.isNotEmpty()){
            map.putString("exceptionMessage", params["exceptionMessage"])
        }

        return map;
    }
}
