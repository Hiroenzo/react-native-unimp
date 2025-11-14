package com.unimp;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dcloud.feature.sdk.DCSDKInitConfig;
import io.dcloud.feature.sdk.DCUniMPCapsuleButtonStyle;
import io.dcloud.feature.sdk.DCUniMPSDK;
import io.dcloud.feature.sdk.Interface.IDCUniMPOnCapsuleCloseButtontCallBack;
import io.dcloud.feature.sdk.Interface.IDCUniMPPreInitCallback;
import io.dcloud.feature.sdk.Interface.IMenuButtonClickCallBack;
import io.dcloud.feature.sdk.Interface.IOnUniMPEventCallBack;
import io.dcloud.feature.sdk.Interface.IUniMP;
import io.dcloud.feature.sdk.Interface.IUniMPOnCloseCallBack;
import io.dcloud.feature.sdk.MenuActionSheetItem;
import io.dcloud.feature.unimp.DCUniMPJSCallback;
import io.dcloud.feature.unimp.config.IUniMPReleaseCallBack;
import io.dcloud.feature.unimp.config.UniMPOpenConfiguration;
import io.dcloud.feature.unimp.config.UniMPReleaseConfiguration;

@ReactModule(name = UnimpModule.NAME)
public class UnimpModule extends ReactContextBaseJavaModule {
  public static final String NAME = "Unimp";

  private final ReactApplicationContext context;

  private static final Map<String, IUniMP> iUniMPMap = new HashMap<>();
  private static boolean isBackgroundMode = false;

  public UnimpModule(ReactApplicationContext reactContext) {
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
    // 更新小程序菜单
    List<MenuActionSheetItem> sheetItems = new ArrayList<>();
    ReadableArray items = params.getArray("items");
    if (items != null && items.size() != 0) {
      for (int i = 1; i < items.size(); i++) {
        ReadableMap item = items.getMap(i);
        sheetItems.add(new MenuActionSheetItem(item.getString("title"), item.getString("key")));
      }
    }

    // 更新小程序胶囊按钮样式
    DCUniMPCapsuleButtonStyle style = new DCUniMPCapsuleButtonStyle();
    if (btnStyle != null) {
      if (btnStyle.hasKey("backgroundColor") && !btnStyle.isNull("backgroundColor")) {
        style.setBackgroundColor(btnStyle.getString("backgroundColor"));
      }
      if (btnStyle.hasKey("textColor") && !btnStyle.isNull("textColor")) {
        style.setTextColor(btnStyle.getString("textColor"));
      }
      if (btnStyle.hasKey("borderColor") && !btnStyle.isNull("borderColor")) {
        style.setBorderColor(btnStyle.getString("borderColor"));
      }
      if (btnStyle.hasKey("highlightColor") && !btnStyle.isNull("highlightColor")) {
        style.setHighlightColor(btnStyle.getString("highlightColor"));
      }
    }

    if (params.hasKey("isEnableBackground")) {
      isBackgroundMode = params.getBoolean("isEnableBackground");
    }

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
        Log.e(NAME, "onInitFinished-----------" + isSuccess);
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
   * 获取小程序wgt文件路径
   *
   * @param appid uni小程序的id
   */
  @ReactMethod
  public void getWgtPath(String appid, final Promise promise) {
    try {
      String wgtPath = this.context.getExternalCacheDir().getPath() + "/" + appid + ".wgt";
      promise.resolve(wgtPath);
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

    DCUniMPSDK.getInstance().releaseWgtToRunPath(appid, uniMPReleaseConfiguration, new IUniMPReleaseCallBack() {
      @Override
      public void onCallBack(int code, Object pArgs) {
        Log.e(NAME, "code ---  " + code + "  pArgs --" + pArgs);
        try {
          if (code == 1) {
            // DCUniMPSDK.getInstance().openUniMP(this.context, appid);
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
   * @param appid         小程序appid
   * @param configuration 小程序应用配置
   */
  @ReactMethod
  public void openUniMP(String appid, ReadableMap configuration, final Promise promise) {
    try {
      // 检查SDK是否已初始化
      if (!DCUniMPSDK.getInstance().isInitialize()) {
        promise.reject(new Exception("SDK未初始化，请先调用initialize方法"));
        return;
      }

      UniMPOpenConfiguration config = new UniMPOpenConfiguration();
      
      // 处理extraData参数
      if (configuration != null && configuration.hasKey("extraData")) {
        ReadableMap extraData = configuration.getMap("extraData");
        ReadableMapKeySetIterator iterator = extraData.keySetIterator();
        while (iterator.hasNextKey()) {
          String key = iterator.nextKey();
          switch (extraData.getType(key)) {
            case Boolean:
              config.extraData.put(key, extraData.getBoolean(key));
              break;
            case String:
              config.extraData.put(key, extraData.getString(key));
              break;
            case Map:
              config.extraData.put(key, extraData.getMap(key));
              break;
            case Array:
              config.extraData.put(key, extraData.getArray(key));
              break;
          }
        }
      }
      
      // 处理redirectPath参数 - 小程序页面直达地址
      if (configuration != null && configuration.hasKey("redirectPath") && !configuration.isNull("redirectPath")) {
        config.redirectPath = configuration.getString("redirectPath");
      }
      
      // 处理arguments参数 - 小程序启动参数
      if (configuration != null && configuration.hasKey("arguments") && !configuration.isNull("arguments")) {
        ReadableMap arguments = configuration.getMap("arguments");
        if (arguments != null) {
          JSONObject argsJson = new JSONObject();
          ReadableMapKeySetIterator iterator = arguments.keySetIterator();
          while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (arguments.getType(key)) {
              case Boolean:
                argsJson.put(key, arguments.getBoolean(key));
                break;
              case String:
                argsJson.put(key, arguments.getString(key));
                break;
              case Number:
                argsJson.put(key, arguments.getDouble(key));
                break;
              case Map:
                argsJson.put(key, arguments.getMap(key));
                break;
              case Array:
                argsJson.put(key, arguments.getArray(key));
                break;
            }
          }
          config.arguments = argsJson;
        }
      }
      
      // 处理splashView参数 - 自定义启动页
      if (configuration != null && configuration.hasKey("splashClass") && !configuration.isNull("splashClass")) {
        // 注意：splashClass需要是IDCUniMPAppSplashView接口的实现类的完整类名
        // 这里暂时不实现，因为需要具体的类实现
        Log.w(NAME, "splashClass参数暂未实现，需要自定义IDCUniMPAppSplashView实现类");
      }
      
      // 启动小程序
      IUniMP unimp = DCUniMPSDK.getInstance().openUniMP(this.context, appid, config);
      
      if (unimp != null) {
        iUniMPMap.put(appid, unimp);
        promise.resolve(appid);
      } else {
        promise.reject(new Exception("启动小程序失败，返回的IUniMP对象为null"));
      }
    } catch (Exception e) {
      Log.e(NAME, "启动小程序异常", e);
      promise.reject(e);
    }
  }

  /**
   * 手动调用关闭小程序
   *
   * @param appid 小程序ID
   */
  @ReactMethod
  public void closeUniMP(String appid, final Promise promise) {
    IUniMP uniMP = iUniMPMap.get(appid);
    if (uniMP != null && uniMP.isRuning()) {
      boolean result = uniMP.closeUniMP();
      if (result) {
        iUniMPMap.remove(appid);
      }
      promise.resolve(result);
    } else {
      promise.reject(new Exception(appid + "小程序未开启"));
    }
  }

  /**
   * 当前小程序显示到前台/退到后台。仅开启后台模式生效！
   *
   * @param appid 小程序ID
   * @param show  显示或隐藏
   */
  @ReactMethod
  public void showOrHideUniMP(String appid, boolean show, final Promise promise) {
    IUniMP uniMP = iUniMPMap.get(appid);
    if (uniMP != null && uniMP.isRuning()) {
      if (isBackgroundMode) {
        boolean result;
        if (show) {
          result = uniMP.showUniMP();
        } else {
          result = uniMP.hideUniMP();
        }
        promise.resolve(result);
      } else {
        promise.reject(new Exception("仅开启后台模式生效"));
      }
    } else {
      promise.reject(new Exception(appid + "小程序未开启"));
    }
  }

  /**
   * 宿主主动触发事件到正在运行的小程序
   *
   * @param appid     小程序ID
   * @param eventName 事件名
   * @param data      传参
   */
  @ReactMethod
  public void sendUniMPEvent(String appid, String eventName, ReadableMap data, final Promise promise) {
    IUniMP uniMP = iUniMPMap.get(appid);
    if (uniMP != null && uniMP.isRuning()) {
      uniMP.sendUniMPEvent(eventName, data);
    } else {
      promise.reject(new Exception(appid + "小程序未开启"));
    }
  }

  /**
   * 获取运行时uni小程序的当前页面url 可用于页面直达等操作的地址
   *
   * @param appid 小程序ID
   */
  @ReactMethod
  public void getCurrentPageUrl(String appid, final Promise promise) {
    IUniMP uniMP = iUniMPMap.get(appid);
    if (uniMP != null && uniMP.isRuning()) {
      String url = uniMP.getCurrentPageUrl();
      promise.resolve(url);
    } else {
      promise.reject(new Exception(appid + "小程序未开启"));
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
   */
  @ReactMethod
  public void setDefMenuButtonClickCallBack() {
    DCUniMPSDK.getInstance().setDefMenuButtonClickCallBack(new IMenuButtonClickCallBack() {
      @Override
      public void onClick(String appid, String buttonId) {
        Log.e(NAME, "点击了" + appid + "的" + buttonId);
        WritableMap params = new WritableNativeMap();
        params.putString("appid", appid);
        params.putString("buttonId", buttonId);
        sendEvent("onMenuButtonClick", params);
      }
    });
  }

  /**
   * 监听小程序被关闭事件
   */
  @ReactMethod
  public void setUniMPOnCloseCallBack() {
    DCUniMPSDK.getInstance().setUniMPOnCloseCallBack(new IUniMPOnCloseCallBack() {
      @Override
      public void onClose(String appid) {
        Log.e(NAME, appid + "被关闭了");
        WritableMap params = new WritableNativeMap();
        params.putString("appid", appid);
        sendEvent("onClose", params);
      }
    });
  }

  /**
   * 小程序胶囊按钮点击关闭事件
   */
  @ReactMethod
  public void setCapsuleCloseButtonClickCallBack() {
    DCUniMPSDK.getInstance().setCapsuleCloseButtonClickCallBack(new IDCUniMPOnCapsuleCloseButtontCallBack() {
      @Override
      public void closeButtonClicked(String appid) {
        Log.i(NAME, appid + "胶囊点击了关闭按钮");
        WritableMap params = new WritableNativeMap();
        params.putString("appid", appid);
        sendEvent("onCapsuleCloseButtonClick", params);
      }
    });
  }

  /**
   * 设置监听小程序发送给宿主的事件
   */
  @ReactMethod
  public void setOnUniMPEventCallBack() {
    DCUniMPSDK.getInstance().setOnUniMPEventCallBack(new IOnUniMPEventCallBack() {
      @Override
      public void onUniMPEventReceive(String appid, String event, Object data, DCUniMPJSCallback dcUniMPJSCallback) {
        WritableMap params = new WritableNativeMap();
        params.putString("appid", appid);
        params.putString("event", event);
        ReadableMap receiveData = objectToReadableMap(data);
        params.putMap("data", receiveData);
        sendEvent("onEventReceive", params);
      }
    });
  }

  private static ReadableMap objectToReadableMap(Object object) {
    WritableMap map = Arguments.createMap();

    if (object instanceof Map<?, ?> objMap) {
      for (Map.Entry<?, ?> entry : objMap.entrySet()) {
        String key = entry.getKey().toString();
        Object value = entry.getValue();

        if (value instanceof String) {
          map.putString(key, (String) value);
        } else if (value instanceof Integer) {
          map.putInt(key, (Integer) value);
        } else if (value instanceof Double) {
          map.putDouble(key, (Double) value);
        } else if (value instanceof Boolean) {
          map.putBoolean(key, (Boolean) value);
        } else if (value instanceof Map) {
          map.putMap(key, objectToReadableMap(value));
        } else if (value instanceof List) {
          map.putArray(key, objectToReadableArray((List<?>) value));
        } else if (value == null) {
          map.putNull(key);
        } else {
          throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
        }
      }
    } else {
      throw new IllegalArgumentException("Input object is not a Map.");
    }

    return map;
  }

  public static ReadableArray objectToReadableArray(List<?> list) {
    WritableArray array = Arguments.createArray();

    for (Object item : list) {
      if (item instanceof String) {
        array.pushString((String) item);
      } else if (item instanceof Integer) {
        array.pushInt((Integer) item);
      } else if (item instanceof Double) {
        array.pushDouble((Double) item);
      } else if (item instanceof Boolean) {
        array.pushBoolean((Boolean) item);
      } else if (item instanceof Map) {
        array.pushMap(objectToReadableMap(item));
      } else if (item instanceof List) {
        array.pushArray(objectToReadableArray((List<?>) item));
      } else if (item == null) {
        array.pushNull();
      } else {
        throw new IllegalArgumentException("Unsupported array element type: " + item.getClass().getName());
      }
    }

    return array;
  }
}
