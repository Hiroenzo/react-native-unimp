package com.unimp

import com.facebook.react.bridge.*
import com.facebook.react.turbomodule.core.interfaces.TurboModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableArray

interface UnimpModuleSpec : TurboModule {
    
    // 初始化相关
    fun initialize(params: ReadableMap, capsuleBtnStyle: ReadableMap?, promise: Promise)
    fun isInitialize(promise: Promise)
    
    // 路径相关
    fun getAppBasePath(promise: Promise)
    fun getWgtPath(appid: String, promise: Promise)
    
    // WGT资源相关
    fun releaseWgtToRunPath(appid: String, wgtPath: String?, password: String, promise: Promise)
    fun isExistsApp(appid: String, promise: Promise)
    
    // 小程序生命周期
    fun openUniMP(appid: String, configuration: ReadableMap?, promise: Promise)
    fun closeUniMP(appid: String, promise: Promise)
    fun showOrHideUniMP(appid: String, show: Boolean, promise: Promise)
    
    // 事件通信
    fun sendUniMPEvent(appid: String, eventName: String, data: ReadableMap, promise: Promise)
    
    // 信息获取
    fun getAppVersionInfo(appid: String, promise: Promise)
    fun getCurrentPageUrl(appid: String, promise: Promise)
    
    // 事件监听设置
    fun setDefMenuButtonClickCallBack()
    fun setUniMPOnCloseCallBack()
    fun setCapsuleCloseButtonClickCallBack()
    fun setOnUniMPEventCallBack()
}