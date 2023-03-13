package com.unimp;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

import io.dcloud.feature.sdk.DCSDKInitConfig;
import io.dcloud.feature.sdk.DCUniMPSDK;
import io.dcloud.feature.sdk.MenuActionSheetItem;

@ReactModule(name = UnimpModule.NAME)
public class UnimpModule extends ReactContextBaseJavaModule {
  public static final String NAME = "Unimp";

  public UnimpModule(ReactApplicationContext reactContext) {
    super(reactContext);
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

  @ReactMethod
  public void initialize(ReadableMap params, final Promise promise) {
    Array items = params.getArray("items");
    List<MenuActionSheetItem> sheetItems = new ArrayList<>();
    for (int i = 0; i < items.size(); i++) {
      Object item = items.getMap(i);
      MenuActionSheetItem sheetItem = new MenuActionSheetItem(item.getString("title"), item.getString("key"));
      sheetItems.add(sheetItem);
    }
    DCSDKInitConfig config = new DCSDKInitConfig.Builder()
    	.setCapsule(true)
    	.setMenuDefFontSize("16px")
    	.setMenuDefFontColor("#ff00ff")
    	.setMenuDefFontWeight("normal")
    	.setMenuActionSheetItems(sheetItems)
    	.build();
    DCUniMPSDK.getInstance().initialize(reactContext, config, new IDCUniMPPreInitCallback() {
        @Override
        public void onInitFinished(boolean isSuccess) {
            Log.e("unimp", "onInitFinished-----------"+isSuccess);
            promise.resolve(isSuccess);
        }
    });
  }
}
