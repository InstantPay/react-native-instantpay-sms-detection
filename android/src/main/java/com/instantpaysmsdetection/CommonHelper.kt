package com.instantpaysmsdetection

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import org.json.JSONArray
import org.json.JSONObject

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

            val mpData = jsonObjectToWritableMap(JSONObject(params["data"]!!))

            map.putMap("data" , mpData)
        }
        else{
            map.putString("data", "")
        }

        if(params.containsKey("exceptionMessage") && params["exceptionMessage"]!!.isNotEmpty()){
            map.putString("exceptionMessage", params["exceptionMessage"])
        }

        return map;
    }

    /**
    * Convert jsonobject to writable map
     */
    fun jsonObjectToWritableMap(jsonObject: JSONObject): WritableMap {
        val map = Arguments.createMap()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            when (value) {
                is JSONObject -> map.putMap(key, jsonObjectToWritableMap(value))
                is JSONArray -> map.putArray(key, jsonArrayToWritableArray(value))
                is Boolean -> map.putBoolean(key, value)
                is Int -> map.putInt(key, value)
                is Double -> map.putDouble(key, value)
                is String -> map.putString(key, value)
                else -> map.putString(key, value.toString())
            }
        }
        return map
    }

    fun jsonArrayToWritableArray(jsonArray: JSONArray): WritableArray {
        val array = Arguments.createArray()
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.get(i)
            when (value) {
                is JSONObject -> array.pushMap(jsonObjectToWritableMap(value))
                is JSONArray -> array.pushArray(jsonArrayToWritableArray(value))
                is Boolean -> array.pushBoolean(value)
                is Int -> array.pushInt(value)
                is Double -> array.pushDouble(value)
                is String -> array.pushString(value)
                else -> array.pushString(value.toString())
            }
        }
        return array
    }
}
