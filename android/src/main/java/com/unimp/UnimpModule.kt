package com.unimp

import android.util.Log

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Dynamic
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableMapKeySetIterator
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import io.dcloud.feature.sdk.DCSDKInitConfig
import io.dcloud.feature.sdk.DCUniMPCapsuleButtonStyle
import io.dcloud.feature.sdk.DCUniMPSDK
import io.dcloud.feature.sdk.Interface.IDCUniMPOnCapsuleCloseButtontCallBack
import io.dcloud.feature.sdk.Interface.IDCUniMPPreInitCallback
import io.dcloud.feature.sdk.Interface.IMenuButtonClickCallBack
import io.dcloud.feature.sdk.Interface.IOnUniMPEventCallBack
import io.dcloud.feature.sdk.Interface.IUniMP
import io.dcloud.feature.sdk.Interface.IUniMPOnCloseCallBack
import io.dcloud.feature.sdk.MenuActionSheetItem
import io.dcloud.feature.unimp.DCUniMPJSCallback
import io.dcloud.feature.unimp.config.IUniMPReleaseCallBack
import io.dcloud.feature.unimp.config.UniMPOpenConfiguration
import io.dcloud.feature.unimp.config.UniMPReleaseConfiguration

@ReactModule(name = UnimpModule.NAME)
class UnimpModule(private val context: ReactApplicationContext) :
    ReactContextBaseJavaModule(context) {

    companion object {
        const val NAME = "Unimp"
        private val iUniMPMap = mutableMapOf<String, IUniMP>()
        private var isBackgroundMode = false
        private val callbackMap = ConcurrentHashMap<String, DCUniMPJSCallback>()
    }

    override fun getName(): String = NAME

    // ── Event helper ───────────────────────────────────────────────

    private fun sendEvent(eventName: String, params: WritableMap?) {
        context
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    // ── Initialization ─────────────────────────────────────────────

    @ReactMethod
    fun initialize(params: ReadableMap, btnStyle: ReadableMap?, promise: Promise) {
        val sheetItems = mutableListOf<MenuActionSheetItem>()
        val items = params.getArray("items")
        if (items != null && items.size() != 0) {
            for (i in 1 until items.size()) {
                val item = items.getMap(i)
                sheetItems.add(MenuActionSheetItem(item.getString("title"), item.getString("key")))
            }
        }

        val style = DCUniMPCapsuleButtonStyle()
        if (btnStyle != null) {
            if (btnStyle.hasKey("backgroundColor") && !btnStyle.isNull("backgroundColor")) {
                style.backgroundColor = btnStyle.getString("backgroundColor")
            }
            if (btnStyle.hasKey("textColor") && !btnStyle.isNull("textColor")) {
                style.textColor = btnStyle.getString("textColor")
            }
            if (btnStyle.hasKey("borderColor") && !btnStyle.isNull("borderColor")) {
                style.borderColor = btnStyle.getString("borderColor")
            }
            if (btnStyle.hasKey("highlightColor") && !btnStyle.isNull("highlightColor")) {
                style.highlightColor = btnStyle.getString("highlightColor")
            }
        }

        if (params.hasKey("isEnableBackground")) {
            isBackgroundMode = params.getBoolean("isEnableBackground")
        }

        val config = DCSDKInitConfig.Builder()
            .setCapsule(params.getBoolean("capsule"))
            .setCapsuleButtonStyle(style)
            .setMenuDefFontSize(params.getString("fontSize"))
            .setMenuDefFontColor(params.getString("fontColor"))
            .setMenuDefFontWeight(params.getString("fontWeight"))
            .setEnableBackground(params.getBoolean("isEnableBackground"))
            .setMenuActionSheetItems(sheetItems)
            .build()

        DCUniMPSDK.getInstance().initialize(context, config, object : IDCUniMPPreInitCallback {
            override fun onInitFinished(isSuccess: Boolean) {
                Log.e(NAME, "onInitFinished-----------$isSuccess")
                promise.resolve(isSuccess)
            }
        })
    }

    @ReactMethod
    fun isInitialize(promise: Promise) {
        try {
            promise.resolve(DCUniMPSDK.getInstance().isInitialize)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    // ── Path & resource management ─────────────────────────────────

    @ReactMethod
    fun getAppBasePath(appid: String?, promise: Promise) {
        try {
            val basePath = DCUniMPSDK.getInstance().getAppBasePath(context)
            promise.resolve(basePath)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun getResourceFilePath(appid: String, promise: Promise) {
        // Android does not bundle wgt in assets the same way iOS does.
        // Return the external cache path for consistency.
        try {
            val wgtPath = context.externalCacheDir?.path + "/$appid.wgt"
            promise.resolve(wgtPath)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun getWgtPath(appid: String, promise: Promise) {
        try {
            val wgtPath = context.externalCacheDir?.path + "/$appid.wgt"
            promise.resolve(wgtPath)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun releaseWgtToRunPath(appid: String, wgtPath: String?, password: String?, promise: Promise) {
        val config = UniMPReleaseConfiguration()
        config.wgtPath = wgtPath
        config.password = password

        DCUniMPSDK.getInstance().releaseWgtToRunPath(appid, config, object : IUniMPReleaseCallBack {
            override fun onCallBack(code: Int, pArgs: Any?) {
                Log.e(NAME, "code ---  $code  pArgs --$pArgs")
                try {
                    if (code == 1) {
                        promise.resolve(code)
                    } else {
                        throw Exception(pArgs as String?)
                    }
                } catch (e: Exception) {
                    promise.reject(e)
                }
            }
        })
    }

    @ReactMethod
    fun isExistsApp(appid: String, promise: Promise) {
        try {
            promise.resolve(DCUniMPSDK.getInstance().isExistsApp(appid))
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    // ── Mini-program lifecycle ─────────────────────────────────────

    @ReactMethod
    fun openUniMP(appid: String, configuration: ReadableMap?, promise: Promise) {
        try {
            if (!DCUniMPSDK.getInstance().isInitialize) {
                promise.reject(Exception("SDK未初始化，请先调用initialize方法"))
                return
            }

            val config = UniMPOpenConfiguration()

            // extraData
            if (configuration != null && configuration.hasKey("extraData")) {
                val extraData = configuration.getMap("extraData")
                val iterator = extraData!!.keySetIterator()
                while (iterator.hasNextKey()) {
                    val key = iterator.nextKey()
                    when (extraData.getType(key)) {
                        ReadableType.Boolean -> config.extraData[key] = extraData.getBoolean(key)
                        ReadableType.String -> config.extraData[key] = extraData.getString(key)
                        ReadableType.Map -> config.extraData[key] = extraData.getMap(key)
                        ReadableType.Array -> config.extraData[key] = extraData.getArray(key)
                        else -> {}
                    }
                }
            }

            // redirectPath
            if (configuration != null && configuration.hasKey("redirectPath") && !configuration.isNull("redirectPath")) {
                config.redirectPath = configuration.getString("redirectPath")
            }

            // arguments
            if (configuration != null && configuration.hasKey("arguments") && !configuration.isNull("arguments")) {
                val arguments = configuration.getMap("arguments")
                if (arguments != null) {
                    val argsJson = JSONObject()
                    val iterator = arguments.keySetIterator()
                    while (iterator.hasNextKey()) {
                        val key = iterator.nextKey()
                        when (arguments.getType(key)) {
                            ReadableType.Boolean -> argsJson.put(key, arguments.getBoolean(key))
                            ReadableType.String -> argsJson.put(key, arguments.getString(key))
                            ReadableType.Number -> argsJson.put(key, arguments.getDouble(key))
                            ReadableType.Map -> argsJson.put(key, arguments.getMap(key))
                            ReadableType.Array -> argsJson.put(key, arguments.getArray(key))
                            else -> {}
                        }
                    }
                    config.arguments = argsJson
                }
            }

            // splashClass (not yet implemented)
            if (configuration != null && configuration.hasKey("splashClass") && !configuration.isNull("splashClass")) {
                Log.w(NAME, "splashClass参数暂未实现，需要自定义IDCUniMPAppSplashView实现类")
            }

            val unimp = DCUniMPSDK.getInstance().openUniMP(context, appid, config)

            if (unimp != null) {
                iUniMPMap[appid] = unimp
                promise.resolve(appid)
            } else {
                promise.reject(Exception("启动小程序失败，返回的IUniMP对象为null"))
            }
        } catch (e: Exception) {
            Log.e(NAME, "启动小程序异常", e)
            promise.reject(e)
        }
    }

    @ReactMethod
    fun closeUniMP(appid: String, promise: Promise) {
        val uniMP = iUniMPMap[appid]
        if (uniMP != null && uniMP.isRuning) {
            val result = uniMP.closeUniMP()
            if (result) {
                iUniMPMap.remove(appid)
            }
            promise.resolve(result)
        } else {
            promise.reject(Exception("$appid 小程序未开启"))
        }
    }

    @ReactMethod
    fun showOrHideUniMP(appid: String, show: Boolean, promise: Promise) {
        val uniMP = iUniMPMap[appid]
        if (uniMP != null && uniMP.isRuning) {
            if (isBackgroundMode) {
                val result = if (show) uniMP.showUniMP() else uniMP.hideUniMP()
                promise.resolve(result)
            } else {
                promise.reject(Exception("仅开启后台模式生效"))
            }
        } else {
            promise.reject(Exception("$appid 小程序未开启"))
        }
    }

    @ReactMethod
    fun sendUniMPEvent(appid: String, eventName: String, data: ReadableMap, promise: Promise) {
        val uniMP = iUniMPMap[appid]
        if (uniMP != null && uniMP.isRuning) {
            uniMP.sendUniMPEvent(eventName, data)
        } else {
            promise.reject(Exception("$appid 小程序未开启"))
        }
    }

    @ReactMethod
    fun getCurrentPageUrl(appid: String, promise: Promise) {
        val uniMP = iUniMPMap[appid]
        if (uniMP != null && uniMP.isRuning) {
            promise.resolve(uniMP.currentPageUrl)
        } else {
            promise.reject(Exception("$appid 小程序未开启"))
        }
    }

    @ReactMethod
    fun getAppVersionInfo(appid: String, promise: Promise) {
        try {
            val jsonObject = DCUniMPSDK.getInstance().getAppVersionInfo(appid)
            if (jsonObject != null) {
                promise.resolve(jsonObject.toString())
            } else {
                promise.resolve(null)
            }
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    // ── Event callback registration ────────────────────────────────

    @ReactMethod
    fun setDefMenuButtonClickCallBack() {
        DCUniMPSDK.getInstance().setDefMenuButtonClickCallBack(object : IMenuButtonClickCallBack {
            override fun onClick(appid: String, buttonId: String) {
                Log.e(NAME, "点击了$appid 的$buttonId")
                val params = WritableNativeMap()
                params.putString("appid", appid)
                params.putString("buttonId", buttonId)
                sendEvent("onMenuButtonClick", params)
            }
        })
    }

    @ReactMethod
    fun setUniMPOnCloseCallBack() {
        DCUniMPSDK.getInstance().setUniMPOnCloseCallBack(object : IUniMPOnCloseCallBack {
            override fun onClose(appid: String) {
                Log.e(NAME, "$appid 被关闭了")
                val params = WritableNativeMap()
                params.putString("appid", appid)
                sendEvent("onClose", params)
            }
        })
    }

    @ReactMethod
    fun setCapsuleCloseButtonClickCallBack() {
        DCUniMPSDK.getInstance().setCapsuleCloseButtonClickCallBack(object : IDCUniMPOnCapsuleCloseButtontCallBack {
            override fun closeButtonClicked(appid: String) {
                Log.i(NAME, "$appid 胶囊点击了关闭按钮")
                val params = WritableNativeMap()
                params.putString("appid", appid)
                sendEvent("onCapsuleCloseButtonClick", params)
            }
        })
    }

    @ReactMethod
    fun setOnUniMPEventCallBack() {
        DCUniMPSDK.getInstance().setOnUniMPEventCallBack(object : IOnUniMPEventCallBack {
            override fun onUniMPEventReceive(
                appid: String,
                event: String,
                data: Any?,
                dcUniMPJSCallback: DCUniMPJSCallback?
            ) {
                val params = WritableNativeMap()
                params.putString("appid", appid)
                params.putString("event", event)
                when {
                    data == null -> params.putNull("data")
                    data is String -> params.putString("data", data)
                    else -> params.putMap("data", objectToReadableMap(data))
                }

                if (dcUniMPJSCallback != null) {
                    val callbackId = UUID.randomUUID().toString()
                    callbackMap[callbackId] = dcUniMPJSCallback
                    params.putString("callbackId", callbackId)
                }
                sendEvent("onEventReceive", params)
            }
        })
    }

    @ReactMethod
    fun invokeUniMPEventCallback(callbackId: String?, responseData: Dynamic, promise: Promise) {
        if (callbackId.isNullOrEmpty()) {
            promise.reject(Exception("callbackId不能为空"))
            return
        }

        try {
            val callbackResult: Any? = when {
                responseData.isNull -> null
                responseData.type == ReadableType.String -> responseData.asString()
                responseData.type == ReadableType.Map -> readableMapToJSONObject(responseData.asMap())
                else -> {
                    promise.reject(Exception("回调参数仅支持 String 或 JSON Object"))
                    return
                }
            }

            val callback = callbackMap.remove(callbackId)
            if (callback == null) {
                promise.reject(Exception("未找到callbackId对应的小程序回调"))
                return
            }

            callback.invoke(callbackResult)
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    // ── JSON conversion helpers ────────────────────────────────────

    private fun readableMapToJSONObject(readableMap: ReadableMap?): JSONObject? {
        if (readableMap == null) return null

        val jsonObject = JSONObject()
        val iterator = readableMap.keySetIterator()

        try {
            while (iterator.hasNextKey()) {
                val key = iterator.nextKey()
                when (readableMap.getType(key)) {
                    ReadableType.Null -> jsonObject.put(key, JSONObject.NULL)
                    ReadableType.Boolean -> jsonObject.put(key, readableMap.getBoolean(key))
                    ReadableType.Number -> {
                        try {
                            jsonObject.put(key, readableMap.getInt(key))
                        } catch (e: Exception) {
                            jsonObject.put(key, readableMap.getDouble(key))
                        }
                    }
                    ReadableType.String -> jsonObject.put(key, readableMap.getString(key))
                    ReadableType.Map -> jsonObject.put(key, readableMapToJSONObject(readableMap.getMap(key)))
                    ReadableType.Array -> jsonObject.put(key, readableArrayToJSONArray(readableMap.getArray(key)))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return jsonObject
    }

    private fun readableArrayToJSONArray(readableArray: ReadableArray?): JSONArray? {
        if (readableArray == null) return null

        val jsonArray = JSONArray()

        try {
            for (i in 0 until readableArray.size()) {
                when (readableArray.getType(i)) {
                    ReadableType.Null -> jsonArray.put(JSONObject.NULL)
                    ReadableType.Boolean -> jsonArray.put(readableArray.getBoolean(i))
                    ReadableType.Number -> {
                        try {
                            jsonArray.put(readableArray.getInt(i))
                        } catch (e: Exception) {
                            jsonArray.put(readableArray.getDouble(i))
                        }
                    }
                    ReadableType.String -> jsonArray.put(readableArray.getString(i))
                    ReadableType.Map -> jsonArray.put(readableMapToJSONObject(readableArray.getMap(i)))
                    ReadableType.Array -> jsonArray.put(readableArrayToJSONArray(readableArray.getArray(i)))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return jsonArray
    }

    private fun objectToReadableMap(obj: Any): ReadableMap {
        val map = Arguments.createMap()

        when (obj) {
            is JSONObject -> {
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    putObjectToMap(map, key, obj.opt(key))
                }
            }
            is Map<*, *> -> {
                for ((key, value) in obj) {
                    putObjectToMap(map, key.toString(), value)
                }
            }
            else -> throw IllegalArgumentException("Input object is not a JSON Object.")
        }

        return map
    }

    private fun putObjectToMap(map: WritableMap, key: String, value: Any?) {
        when {
            value == null || value === JSONObject.NULL -> map.putNull(key)
            value is String -> map.putString(key, value)
            value is Int -> map.putInt(key, value)
            value is Number -> map.putDouble(key, value.toDouble())
            value is Boolean -> map.putBoolean(key, value)
            value is JSONObject || value is Map<*, *> -> map.putMap(key, objectToReadableMap(value))
            value is JSONArray -> map.putArray(key, objectToReadableArray(value))
            value is List<*> -> map.putArray(key, objectToReadableArray(value))
            else -> throw IllegalArgumentException("Unsupported value type: ${value::class.java.name}")
        }
    }

    private fun objectToReadableArray(jsonArray: JSONArray): ReadableArray {
        val array = Arguments.createArray()
        for (i in 0 until jsonArray.length()) {
            putObjectToArray(array, jsonArray.opt(i))
        }
        return array
    }

    private fun objectToReadableArray(list: List<*>): ReadableArray {
        val array = Arguments.createArray()
        for (item in list) {
            putObjectToArray(array, item)
        }
        return array
    }

    private fun putObjectToArray(array: WritableArray, item: Any?) {
        when {
            item == null || item === JSONObject.NULL -> array.pushNull()
            item is String -> array.pushString(item)
            item is Int -> array.pushInt(item)
            item is Number -> array.pushDouble(item.toDouble())
            item is Boolean -> array.pushBoolean(item)
            item is JSONObject || item is Map<*, *> -> array.pushMap(objectToReadableMap(item))
            item is JSONArray -> array.pushArray(objectToReadableArray(item))
            item is List<*> -> array.pushArray(objectToReadableArray(item))
            else -> throw IllegalArgumentException("Unsupported array element type: ${item!!::class.java.name}")
        }
    }
}
