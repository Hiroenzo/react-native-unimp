package com.unimp

import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.turbomodule.core.interfaces.TurboModule
import io.dcloud.feature.sdk.*
import io.dcloud.feature.sdk.Interface.*
import io.dcloud.feature.unimp.DCUniMPJSCallback
import io.dcloud.feature.unimp.config.IUniMPReleaseCallBack
import io.dcloud.feature.unimp.config.UniMPOpenConfiguration
import io.dcloud.feature.unimp.config.UniMPReleaseConfiguration
import org.json.JSONObject

@ReactModule(name = UnimpModule.NAME)
class UnimpModule(private val reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext), TurboModule, UnimpModuleSpec {
    
    companion object {
        const val NAME = "Unimp"
        private val iUniMPMap = mutableMapOf<String, IUniMP>()
        private var isBackgroundMode = false
    }

    override fun getName(): String = NAME

    /**
     * 传递监听事件
     */
    private fun sendEvent(eventName: String, @Nullable params: WritableMap?) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    override fun initialize(params: ReadableMap, capsuleBtnStyle: ReadableMap?, promise: Promise) {
        try {
            // 更新小程序菜单
            val sheetItems = mutableListOf<MenuActionSheetItem>()
            val items = params.getArray("items")
            items?.let {
                for (i in 1 until it.size()) {
                    val item = it.getMap(i)
                    sheetItems.add(MenuActionSheetItem(item.getString("title"), item.getString("key")))
                }
            }

            // 更新小程序胶囊按钮样式
            val style = DCUniMPCapsuleButtonStyle().apply {
                capsuleBtnStyle?.let {
                    if (it.hasKey("backgroundColor") && !it.isNull("backgroundColor")) {
                        setBackgroundColor(it.getString("backgroundColor"))
                    }
                    if (it.hasKey("textColor") && !it.isNull("textColor")) {
                        setTextColor(it.getString("textColor"))
                    }
                    if (it.hasKey("borderColor") && !it.isNull("borderColor")) {
                        setBorderColor(it.getString("borderColor"))
                    }
                    if (it.hasKey("highlightColor") && !it.isNull("highlightColor")) {
                        setHighlightColor(it.getString("highlightColor"))
                    }
                }
            }

            isBackgroundMode = if (params.hasKey("isEnableBackground")) {
                params.getBoolean("isEnableBackground")
            } else false

            val config = DCSDKInitConfig.Builder()
                .setCapsule(params.getBoolean("capsule"))
                .setCapsuleButtonStyle(style)
                .setMenuDefFontSize(params.getString("fontSize"))
                .setMenuDefFontColor(params.getString("fontColor"))
                .setMenuDefFontWeight(params.getString("fontWeight"))
                .setEnableBackground(params.getBoolean("isEnableBackground"))
                .setMenuActionSheetItems(sheetItems)
                .build()

            DCUniMPSDK.getInstance().initialize(reactContext, config, object : IDCUniMPPreInitCallback {
                override fun onInitFinished(isSuccess: Boolean) {
                    Log.e(NAME, "onInitFinished-----------$isSuccess")
                    promise.resolve(isSuccess)
                }
            })
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    override fun isInitialize(promise: Promise) {
        try {
            val isInit = DCUniMPSDK.getInstance().isInitialize
            promise.resolve(isInit)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    override fun getAppBasePath(promise: Promise) {
        try {
            val basePath = DCUniMPSDK.getInstance().getAppBasePath(reactContext)
            promise.resolve(basePath)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    override fun getWgtPath(appid: String, promise: Promise) {
        try {
            val wgtPath = reactContext.externalCacheDir?.path + "/$appid.wgt"
            promise.resolve(wgtPath)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    override fun releaseWgtToRunPath(appid: String, wgtPath: String?, password: String, promise: Promise) {
        val uniMPReleaseConfiguration = UniMPReleaseConfiguration().apply {
            this.wgtPath = wgtPath
            this.password = password
        }

        DCUniMPSDK.getInstance().releaseWgtToRunPath(appid, uniMPReleaseConfiguration, 
            object : IUniMPReleaseCallBack {
                override fun onCallBack(code: Int, pArgs: Any?) {
                    Log.e(NAME, "code ---  $code  pArgs --$pArgs")
                    try {
                        if (code == 1) {
                            promise.resolve(code)
                        } else {
                            throw Exception(pArgs as? String ?: "Unknown error")
                        }
                    } catch (e: Exception) {
                        promise.reject(e)
                    }
                }
            })
    }

    override fun isExistsApp(appid: String, promise: Promise) {
        try {
            val isExists = DCUniMPSDK.getInstance().isExistsApp(appid)
            promise.resolve(isExists)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    override fun openUniMP(appid: String, configuration: ReadableMap?, promise: Promise) {
        try {
            // 检查SDK是否已初始化
            if (!DCUniMPSDK.getInstance().isInitialize) {
                promise.reject(Exception("SDK未初始化，请先调用initialize方法"))
                return
            }

            val config = UniMPOpenConfiguration()
            
            // 处理extraData参数
            configuration?.getMap("extraData")?.let { extraData ->
                val iterator = extraData.keySetIterator()
                while (iterator.hasNextKey()) {
                    val key = iterator.nextKey()
                    when (extraData.getType(key)) {
                        ReadableType.Boolean -> config.extraData.put(key, extraData.getBoolean(key))
                        ReadableType.String -> config.extraData.put(key, extraData.getString(key))
                        ReadableType.Map -> config.extraData.put(key, extraData.getMap(key))
                        ReadableType.Array -> config.extraData.put(key, extraData.getArray(key))
                        else -> {}
                    }
                }
            }
            
            // 处理redirectPath参数 - 小程序页面直达地址
            configuration?.getString("redirectPath")?.let {
                config.redirectPath = it
            }
            
            // 处理arguments参数 - 小程序启动参数
            configuration?.getMap("arguments")?.let { arguments ->
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
            
            // 处理splashView参数 - 自定义启动页
            configuration?.getString("splashClass")?.let {
                Log.w(NAME, "splashClass参数暂未实现，需要自定义IDCUniMPAppSplashView实现类")
            }
            
            // 启动小程序
            val unimp = DCUniMPSDK.getInstance().openUniMP(reactContext, appid, config)
            
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

    override fun closeUniMP(appid: String, promise: Promise) {
        val uniMP = iUniMPMap[appid]
        if (uniMP?.isRuning == true) {
            val result = uniMP.closeUniMP()
            if (result) {
                iUniMPMap.remove(appid)
            }
            promise.resolve(result)
        } else {
            promise.reject(Exception("$appid 小程序未开启"))
        }
    }

    override fun showOrHideUniMP(appid: String, show: Boolean, promise: Promise) {
        val uniMP = iUniMPMap[appid]
        if (uniMP?.isRuning == true) {
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

    override fun sendUniMPEvent(appid: String, eventName: String, data: ReadableMap, promise: Promise) {
        val uniMP = iUniMPMap[appid]
        if (uniMP?.isRuning == true) {
            uniMP.sendUniMPEvent(eventName, data)
            promise.resolve(true)
        } else {
            promise.reject(Exception("$appid 小程序未开启"))
        }
    }

    override fun getCurrentPageUrl(appid: String, promise: Promise) {
        val uniMP = iUniMPMap[appid]
        if (uniMP?.isRuning == true) {
            val url = uniMP.currentPageUrl
            promise.resolve(url)
        } else {
            promise.reject(Exception("$appid 小程序未开启"))
        }
    }

    override fun getAppVersionInfo(appid: String, promise: Promise) {
        try {
            val jsonObject = DCUniMPSDK.getInstance().getAppVersionInfo(appid)
            promise.resolve(jsonObject?.toString())
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    override fun setDefMenuButtonClickCallBack() {
        DCUniMPSDK.getInstance().setDefMenuButtonClickCallBack { appid, buttonId ->
            Log.e(NAME, "点击了$appid的$buttonId")
            val params = Arguments.createMap().apply {
                putString("appid", appid)
                putString("buttonId", buttonId)
            }
            sendEvent("onMenuButtonClick", params)
        }
    }

    override fun setUniMPOnCloseCallBack() {
        DCUniMPSDK.getInstance().setUniMPOnCloseCallBack { appid ->
            Log.e(NAME, "$appid 被关闭了")
            val params = Arguments.createMap().apply {
                putString("appid", appid)
            }
            sendEvent("onClose", params)
        }
    }

    override fun setCapsuleCloseButtonClickCallBack() {
        DCUniMPSDK.getInstance().setCapsuleCloseButtonClickCallBack { appid ->
            Log.i(NAME, "$appid 胶囊点击了关闭按钮")
            val params = Arguments.createMap().apply {
                putString("appid", appid)
            }
            sendEvent("onCapsuleCloseButtonClick", params)
        }
    }

    override fun setOnUniMPEventCallBack() {
        DCUniMPSDK.getInstance().setOnUniMPEventCallBack { appid, event, data, dcUniMPJSCallback ->
            val params = Arguments.createMap().apply {
                putString("appid", appid)
                putString("event", event)
                putMap("data", objectToReadableMap(data))
            }
            sendEvent("onEventReceive", params)
        }
    }

    private fun objectToReadableMap(obj: Any?): ReadableMap {
        val map = Arguments.createMap()
        
        when (obj) {
            is Map<*, *> -> {
                obj.forEach { (key, value) ->
                    when (value) {
                        is String -> map.putString(key.toString(), value)
                        is Int -> map.putInt(key.toString(), value)
                        is Double -> map.putDouble(key.toString(), value)
                        is Boolean -> map.putBoolean(key.toString(), value)
                        is Map<*, *> -> map.putMap(key.toString(), objectToReadableMap(value))
                        is List<*> -> map.putArray(key.toString(), objectToReadableArray(value))
                        null -> map.putNull(key.toString())
                        else -> throw IllegalArgumentException("Unsupported value type: ${value?.javaClass?.name}")
                    }
                }
            }
            null -> {}
            else -> throw IllegalArgumentException("Input object is not a Map.")
        }
        
        return map
    }

    private fun objectToReadableArray(list: List<*>?): ReadableArray {
        val array = Arguments.createArray()
        
        list?.forEach { item ->
            when (item) {
                is String -> array.pushString(item)
                is Int -> array.pushInt(item)
                is Double -> array.pushDouble(item)
                is Boolean -> array.pushBoolean(item)
                is Map<*, *> -> array.pushMap(objectToReadableMap(item))
                is List<*> -> array.pushArray(objectToReadableArray(item))
                null -> array.pushNull()
                else -> throw IllegalArgumentException("Unsupported array element type: ${item?.javaClass?.name}")
            }
        }
        
        return array
    }
}