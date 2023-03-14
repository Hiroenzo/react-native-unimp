package com.unimp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.module.annotations.ReactModule;

import java.util.ArrayList;
import java.util.List;

import io.dcloud.feature.sdk.DCSDKInitConfig;
import io.dcloud.feature.sdk.DCUniMPSDK;
import io.dcloud.feature.sdk.Interface.IDCUniMPPreInitCallback;
import io.dcloud.feature.sdk.Interface.IUniMP;
import io.dcloud.feature.sdk.MenuActionSheetItem;
import io.dcloud.feature.unimp.config.IUniMPReleaseCallBack;
import io.dcloud.feature.unimp.config.UniMPOpenConfiguration;
import io.dcloud.feature.unimp.config.UniMPReleaseConfiguration;

@ReactModule(name = UnimpModule.NAME)
public class UnimpModule extends ReactContextBaseJavaModule {
  public static final String NAME = "Unimp";

  ReactApplicationContext context;

  public UnimpModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.context = reactContext;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b);
  }

  /**
   * 初始化小程序
   * @param params  小程序菜单参数
   */
  @ReactMethod
  public void initialize(ReadableMap params, final Promise promise) {
    MenuActionSheetItem item = new MenuActionSheetItem("关于", "gy");
    List<MenuActionSheetItem> sheetItems = new ArrayList<>();
    sheetItems.add(item);
    DCSDKInitConfig config = new DCSDKInitConfig.Builder()
    	.setCapsule(false)
    	.setMenuDefFontSize("16px")
    	.setMenuDefFontColor("#ff00ff")
    	.setMenuDefFontWeight("normal")
    	.setMenuActionSheetItems(sheetItems)
    	.build();
    DCUniMPSDK.getInstance().initialize(this.context, config, new IDCUniMPPreInitCallback() {
        @Override
        public void onInitFinished(boolean isSuccess) {
            Log.e("unimp", "onInitFinished-----------"+isSuccess);
            promise.resolve(isSuccess);
        }
    });
  }

  /**
   * 校验SDK是否初始化成功
   */
  @ReactMethod
  public void isInitialize(final Promise promise) {
    try {
      Boolean isInit = DCUniMPSDK.getInstance().isInitialize();
      promise.resolve(isInit);
    } catch (Exception e) {
      promise.reject(e);
    }
  }

  /**
   * 获取小程序运行路径
   * uni小程序运行路径 路径格式： "/xxx/xxx/宿主包名/files/apps/"
   */
  @ReactMethod
  public void getAppBasePath(final Promise promise) {
    try {
      String basePath = DCUniMPSDK.getInstance().getAppBasePath(this.context);
      promise.resolve(basePath);
    } catch (Exception e) {
      promise.reject(e);
    }
  }

  /**
   * 将wgt包中的资源文件释放到uni小程序运行时路径下
   * @param appid    uni小程序的id
   * @param wgtPath  uni小程序应用资源包路径 仅支持SD路径 不支持assets
   * @param password 资源包解压密码（猜的）
   */
  @ReactMethod
  public void releaseWgtToRunPath(String appid, String wgtPath, String password, final Promise promise) {
    UniMPReleaseConfiguration uniMPReleaseConfiguration = new UniMPReleaseConfiguration();
    uniMPReleaseConfiguration.wgtPath = wgtPath;
    uniMPReleaseConfiguration.password = password;

    // ReactApplicationContext context = this.context;

    DCUniMPSDK.getInstance().releaseWgtToRunPath(appid, uniMPReleaseConfiguration, new IUniMPReleaseCallBack() {
      @Override
      public void onCallBack(int code, Object pArgs) {
        Log.e("unimp","code ---  " + code + "  pArgs --" + pArgs);
        try {
          if (code == 1) {
            // DCUniMPSDK.getInstance().openUniMP(context, appid);
            promise.resolve(code);
          } else {
            throw new Exception((String) pArgs);
          }
        } catch (Exception e) {
          promise.reject(e);
        }
      }
    });
  }

  /**
   * 检查当前appid小程序是否已释放wgt资源
   * 可用来检查当前appid资源是否存在
   * @param appid 小程序appid
   */
  @ReactMethod
  public void isExistsApp(String appid, final Promise promise) {
    try {
      Boolean isExists = DCUniMPSDK.getInstance().isExistsApp(appid);
      promise.resolve(isExists);
    } catch (Exception e) {
      promise.reject(e);
    }
  }

  /**
   * 启动小程序
   * @param appid   uni小程序应用id
   */
  @ReactMethod
  public void openUniMP(String appid, final Promise promise) {
    try {
      UniMPOpenConfiguration uniMPOpenConfiguration = new UniMPOpenConfiguration();
      uniMPOpenConfiguration.extraData.put("darkmode", "auto");
      IUniMP unimp = DCUniMPSDK.getInstance().openUniMP(this.context, appid, uniMPOpenConfiguration);
      promise.resolve(unimp);
    } catch (Exception e) {
      e.printStackTrace();
      promise.reject(e);
    }
  }
}
