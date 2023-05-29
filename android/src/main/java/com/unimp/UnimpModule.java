package com.unimp;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.dcloud.feature.sdk.DCSDKInitConfig;
import io.dcloud.feature.sdk.DCUniMPCapsuleButtonStyle;
import io.dcloud.feature.sdk.DCUniMPSDK;
import io.dcloud.feature.sdk.Interface.IDCUniMPOnCapsuleCloseButtontCallBack;
import io.dcloud.feature.sdk.Interface.IDCUniMPPreInitCallback;
import io.dcloud.feature.sdk.Interface.IMenuButtonClickCallBack;
import io.dcloud.feature.sdk.Interface.IUniMP;
import io.dcloud.feature.sdk.Interface.IUniMPOnCloseCallBack;
import io.dcloud.feature.sdk.MenuActionSheetItem;
import io.dcloud.feature.unimp.config.IUniMPReleaseCallBack;
import io.dcloud.feature.unimp.config.UniMPOpenConfiguration;
import io.dcloud.feature.unimp.config.UniMPReleaseConfiguration;

@ReactModule(name = UniMPModule.NAME)
public class UniMPModule extends ReactContextBaseJavaModule {
  public static final String NAME = "UniMP";

  ReactApplicationContext context;

  public UniMPModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.context = reactContext;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  /**
   * 传递监听事件
   *
   * @param eventName 事件
   * @param params    参数
   */
  private void sendEvent(String eventName, @Nullable WritableMap params) {
    this.context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }

  /**
   * 初始化小程序
   *
   * @param params   小程序胶囊按钮参数
   * @param btnStyle 胶囊样式
   */
  @ReactMethod
  public void initialize(ReadableMap params, ReadableMap btnStyle, final Promise promise) {
    MenuActionSheetItem item = new MenuActionSheetItem("关于", "gy");
    List<MenuActionSheetItem> sheetItems = new ArrayList<>();
    sheetItems.add(item);

    DCUniMPCapsuleButtonStyle style = new DCUniMPCapsuleButtonStyle();
    style.setBackgroundColor(btnStyle.getString("backgroundColor"));
    style.setTextColor(btnStyle.getString("textColor"));
    style.setBorderColor(btnStyle.getString("borderColor"));
    style.setHighlightColor(btnStyle.getString("highlightColor"));

    DCSDKInitConfig config = new DCSDKInitConfig.Builder()
        .setCapsule(params.getBoolean("capsule"))
        .setCapsuleButtonStyle(style)
        .setMenuDefFontSize(params.getString("fontSize"))
        .setMenuDefFontColor(params.getString("fontColor"))
        .setMenuDefFontWeight(params.getString("fontWeight"))
        .setEnableBackground(params.getBoolean("isEnableBackground"))
        .setMenuActionSheetItems(sheetItems)
        .build();
    DCUniMPSDK.getInstance().initialize(this.context, config, new IDCUniMPPreInitCallback() {
      @Override
      public void onInitFinished(boolean isSuccess) {
        Log.e("unimp", "onInitFinished-----------" + isSuccess);
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
   *
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
        Log.e("unimp", "code ---  " + code + "  pArgs --" + pArgs);
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
   *
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
   *
   * @param appid         uni小程序应用id
   * @param configuration uni小程序应用配置
   */
  @ReactMethod
  public void openUniMP(String appid, ReadableMap configuration, final Promise promise) {
    try {
      UniMPOpenConfiguration config = new UniMPOpenConfiguration();
      IUniMP unimp = DCUniMPSDK.getInstance().openUniMP(this.context, appid, config);
      promise.resolve(true);
    } catch (Exception e) {
      e.printStackTrace();
      promise.reject(e);
    }
  }

  /**
   * 获取uni小程序版本信息
   *
   * @param appid 小程序appid
   */
  @ReactMethod
  public void getAppVersionInfo(String appid, final Promise promise) {
    try {
      JSONObject jsonObject = DCUniMPSDK.getInstance().getAppVersionInfo(appid);
      if (jsonObject != null) {
        promise.resolve(jsonObject.toString());
      } else {
        promise.resolve(null);
      }
    } catch (Exception e) {
      promise.reject(e);
    }
  }

  /**
   * 小程序菜单点击事件回调
   *
   * @param callback 回调事件
   */
  @ReactMethod
  public void setDefMenuButtonClickCallBack(final Callback callback) {
    DCUniMPSDK.getInstance().setDefMenuButtonClickCallBack(new IMenuButtonClickCallBack() {
      @Override
      public void onClick(String appid, String buttonid) {
        Log.e("unimp", "点击了" + appid + "的" + buttonid);
        WritableMap params = new WritableNativeMap();
        params.putString("appid", appid);
        params.putString("buttonid", buttonid);
        callback.invoke(params);
      }
    });
  }

  /**
   * 监听小程序被关闭事件
   *
   * @param callback 回调事件
   */
  @ReactMethod
  public void setUniMPOnCloseCallBack(final Callback callback) {
    DCUniMPSDK.getInstance().setUniMPOnCloseCallBack(new IUniMPOnCloseCallBack() {
      @Override
      public void onClose(String appid) {
        Log.e("unimp", appid + "被关闭了");
        callback.invoke(appid);
      }
    });
  }

  /**
   * 小程序胶囊按钮点击关闭事件
   *
   * @param callback 回调事件
   */
  @ReactMethod
  public void setCapsuleCloseButtonClickCallBack(final Callback callback) {
    DCUniMPSDK.getInstance().setCapsuleCloseButtonClickCallBack(new IDCUniMPOnCapsuleCloseButtontCallBack() {
      @Override
      public void closeButtonClicked(String appid) {
        Log.e("unimp", appid + "胶囊点击了关闭按钮");
        callback.invoke(appid);
      }
    });
  }
}
