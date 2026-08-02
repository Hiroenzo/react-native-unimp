# react-native-unimp

![NPM Downloads](https://img.shields.io/npm/d18m/react-native-unimp?style=for-the-badge&logo=npm&link=https%3A%2F%2Fwww.npmjs.com%2Fpackage%2Freact-native-unimp)
![Node Current](https://img.shields.io/node/v/react-native-unimp?style=for-the-badge&logo=nodedotjs)
![React Native](https://img.shields.io/badge/React_Native-0.71%2B-61DAFB?style=for-the-badge&logo=react)
![New Architecture](https://img.shields.io/badge/New_Architecture-TurboModules-61DAFB?style=for-the-badge&logo=react)
![Expo](https://img.shields.io/badge/Expo-Config_Plugin-000020?style=for-the-badge&logo=expo)
![Android](https://img.shields.io/badge/Android-Kotlin-7F52FF?style=for-the-badge&logo=kotlin)
![iOS](https://img.shields.io/badge/iOS-Swift-F05138?style=for-the-badge&logo=swift)

> 集成uni小程序SDK，支持**Android**和**iOS**，目前只集成了基础模块，其他原生功能依赖库需要自行集成。
>
> v2.0.0 全面支持 React Native **新架构（TurboModules）** 与 **Expo Config Plugin**，Android 端已使用 **Kotlin**、iOS 端已使用 **Swift** 重写，同时通过 interop 层保持对旧架构的向后兼容。

## SDK 版本
- `Android`已更新至2025年11月11日发布的 **SDK 5.14 版本**
- `ios`版本为 **SDK 5.15 版本**

## v2.0.0 破坏性变更

v2.0.0 是一次重大重构版本，请注意以下破坏性变更：

- **最低 React Native 版本提升至 0.71+**：新架构基于 Codegen 与 TurboModules，要求 RN 0.71 及以上版本。
- **Android 原生代码由 Java 重写为 Kotlin**：旧版本基于 Java 的自定义原生模块修改方式不再适用，请参考新的 Kotlin 实现。
- **iOS 原生代码由 Objective-C 重写为 Swift**：核心逻辑迁移至 Swift，并通过 Obj-C++ 桥接层对接 TurboModule 与 UniMP SDK。
- **新增 Codegen 规范文件**：`Codegen` 已纳入构建流程，TurboModule 接口由 TypeScript spec 自动生成原生代码。
- **Expo 支持**：通过 `app.plugin.js` Config Plugin 接入，Expo 项目无需手动修改原生代码。

> 如果你的项目仍使用旧架构（Paper），本库会自动通过 interop 层降级运行，无需额外配置。

## 新架构支持 (New Architecture)

v2.0.0 已完整适配 React Native 新架构（New Architecture），主要特性如下：

### TurboModules

本库通过 [Codegen](https://reactnative.dev/docs/the-new-architecture/pillars-codegen) 规范文件定义原生模块接口，在构建时自动生成 C++ / Java(Kotlin) / Objective-C++ 胶水代码，实现 TurboModule 通信：

- **接口定义**：使用 TypeScript 编写 spec 文件，由 Codegen 生成各平台的原生类型绑定。
- **Android（Kotlin）**：`UnimpModule.kt` 实现 TurboModule 接口，直接对接 UniMP Android SDK。
- **iOS（Swift + Obj-C++）**：`Unimp.swift` 为 Swift 核心，`UnimpModule.mm` 为 Obj-C++ TurboModule 桥接层，负责把 JSI/TurboModule 调用转发到 Swift 实现。

### 向后兼容（Interop Layer）

对于尚未启用新架构的项目，本库通过 React Native 提供的 interop layer 自动降级为传统 Bridge 模式运行，无需修改任何业务代码：

- 启用新架构时走 TurboModule（JSI 同步调用，性能更优）。
- 未启用新架构时自动回退到 Legacy Bridge 异步通信。
- 业务侧 `import * as Unimp from 'react-native-unimp'` 的调用方式完全一致，无需针对架构差异做适配。

### 原生实现结构

```
android/src/main/java/com/unimp/
  ├── UnimpModule.kt      # Kotlin TurboModule 实现
  └── UnimpPackage.kt     # Android 包注册

ios/
  ├── Unimp.swift          # Swift 核心实现
  ├── UnimpModule.mm       # Obj-C++ TurboModule 桥接层
  ├── UnimpSDKBridge.h     # Obj-C++ SDK 桥接头文件
  └── UnimpSDKBridge.mm    # Obj-C++ SDK 桥接实现（对接 DCUniMP SDK）
```

## 示例

完整示例可以查看 [react-native-unimp-example](https://github.com/Hiroenzo/react-native-unimp-example)

## 安装

### Bare React Native

```sh
npm install react-native-unimp
```

iOS 安装依赖：

```sh
cd ios && pod install
```

Android 需要为 UniMP SDK 添加 maven 仓库地址，详见下方 [Android配置](#android配置)。

### Expo

Expo 项目同样通过 npm 安装，随后在 `app.json` / `app.config.js` 中注册 Config Plugin，无需手动执行 `pod install` 或修改原生文件：

```sh
npm install react-native-unimp
```

详细的 Expo 配置方式见 [Expo 集成](#expo-集成)。

## Expo 集成

v2.0.0 起提供 `app.plugin.js` Config Plugin，Expo 项目（含 Expo managed workflow 与裸 RN + Expo 工作流）可通过插件自动完成绝大部分原生配置。

### 1. 注册 Config Plugin

在 `app.json` 中将 `react-native-unimp` 添加到 `plugins` 数组，并按需传入配置参数：

```json
// app.json
{
  "plugins": [
    [
      "react-native-unimp",
      {
        "android": {
          "mavenRepositoryUrl": "https://your-maven-repo.com"
        },
        "ios": {
          "debug": true
        }
      }
    ]
  ]
}
```

配置参数说明：

| 平台 | 参数 | 类型 | 说明 |
| --- | --- | --- | --- |
| `android` | `mavenRepositoryUrl` | `string` | UniMP Android SDK 所在的 maven 仓库地址，插件会自动写入到 `build.gradle` |
| `ios` | `debug` | `boolean` | 是否在控制台输出 JS log（对应 SDK 的 `debug` 参数），默认 `false` |

### 2. 生成原生项目并构建

```sh
# 预构建（生成 ios / android 原生工程）
npx expo prebuild

# 运行
npx expo run:ios
npx expo run:android
```

### 3. 导入小程序应用资源

Config Plugin 会自动处理 Gradle / Pod 等构建配置，但小程序的 wgt 资源仍需按平台手动导入：

- **Android**：将 wgt 资源解压到 `android/app/src/main/assets/apps/__UNI__XXXX/www` 目录。
- **iOS**：将 wgt 包放入 `apps` 文件夹并添加到 Xcode 工程中。

具体导入方式与 Bare RN 一致，详见下方 [Android配置](#android配置) 与 [iOS配置](#ios配置) 中的「导入小程序应用资源」小节。

> Config Plugin 已自动完成以下配置，Expo 项目**无需手动重复操作**：Android 的 maven 仓库、`aaptOptions`、Pod 依赖；iOS 的 SDK 引擎初始化、生命周期方法注入。

## 使用示例

```js
import * as Unimp from 'react-native-unimp';

// 初始化小程序
Unimp.initialize(
  { isEnableBackground: false, capsule: true },
  { backgroundColor: '#1991FB' }
)
  .then(async () => {
    const isInitialize = await Unimp.isInitialize();
    if (isInitialize) {
      console.log(`[小程序初始化]: 成功`);
    }
  })
  .catch((e) => console.log(`[小程序初始化]: 失败：${e.message}`));
```

### 接收小程序事件并回调

`setOnUniMPEventCallBack()` 依赖已初始化的 UniMP SDK，必须等待 `initialize()`
成功后调用，否则原生事件监听不会生效。`UnimpEventEmitter().addListener()` 是 RN
事件订阅，可以提前创建。

```ts
import {
  IUniMPEventReceiveProps,
  initialize,
  invokeUniMPEventCallback,
  setOnUniMPEventCallBack,
  UniMPEvent,
  UnimpEventEmitter,
} from 'react-native-unimp';

async function subscribeUniMPEvents() {
  // 1. RN 事件订阅可以在 SDK 初始化前创建
  const subscription = UnimpEventEmitter().addListener(
    UniMPEvent.onEventReceive,
    async ({ appid, event, data, callbackId }: IUniMPEventReceiveProps) => {
      console.log('收到小程序事件：', { appid, event, data });

      // 小程序传入 callback 时才会存在 callbackId
      if (callbackId) {
        await invokeUniMPEventCallback(callbackId, {
          message: '宿主已收到事件',
        });
      }
    }
  );

  // 2. 初始化 UniMP SDK
  await initialize(
    { isEnableBackground: false, capsule: true },
    { backgroundColor: '#1991FB' }
  );

  // 3. 必须在初始化成功后注册原生事件监听
  setOnUniMPEventCallBack();

  // 调用方应在页面或应用作用域销毁时执行 subscription.remove()
  return subscription;
}
```

事件参数说明：

| 属性 | 类型 | 说明 |
| --- | --- | --- |
| `event` | `string` | 小程序发送的事件名称 |
| `data` | `string \| Record<string, unknown>` | 小程序携带的数据 |
| `callbackId` | `string \| undefined` | 小程序传入 callback 时生成，用于向对应 callback 回传结果 |
| `appid` | `string \| undefined` | Android 提供；iOS SDK 不提供 |

当前 `UniMPEvent` 支持的事件：

| 枚举 | 事件名称 | 支持平台 | 触发时机 |
| --- | --- | --- | --- |
| `UniMPEvent.onEventReceive` | `onEventReceive` | Android / iOS | 小程序调用 `uni.sendNativeEvent()` 向宿主发送事件 |
| `UniMPEvent.onClose` | `onClose` | Android | 小程序关闭 |
| `UniMPEvent.onMenuButtonClick` | `onMenuButtonClick` | Android | 用户点击小程序自定义菜单项 |
| `UniMPEvent.onCapsuleCloseButtonClick` | `onCapsuleCloseButtonClick` | Android | 用户点击胶囊关闭按钮 |

`onEventReceive` 需要在初始化成功后调用 `setOnUniMPEventCallBack()`。其他 Android
事件同样需要在初始化成功后注册对应的原生 callback：

| 事件 | Android 原生注册方法 |
| --- | --- |
| `onClose` | `setUniMPOnCloseCallBack()` |
| `onMenuButtonClick` | `setDefMenuButtonClickCallBack()` |
| `onCapsuleCloseButtonClick` | `setCapsuleCloseButtonClickCallBack()` |

注意事项：

- `invokeUniMPEventCallback()` 返回 `Promise<boolean>`。当前是单次回调，调用成功后对应的 `callbackId` 会失效；参数错误或找不到回调时 Promise 会 reject。
- 如果小程序没有传入 callback，事件参数中不会包含 `callbackId`，宿主无需回调。
- iOS 如需识别事件所属的 `appid`，可由 uni-app 调用方将其放入 `data`。
- `setOnUniMPEventCallBack()` 必须在 `initialize()` 成功后调用；每次 SDK 初始化流程只需调用一次。
- `UnimpEventEmitter().addListener()` 不受初始化顺序限制，但重复订阅会导致业务回调被重复执行。
- 页面或应用作用域销毁时调用 `subscription.remove()`，避免遗留 JS 监听。

## 配置

> **Expo 项目提示**：如果你使用 Expo Config Plugin 接入，下面 Android / iOS 的构建级配置（maven 仓库、Gradle `aaptOptions`、Pod 依赖、iOS SDK 引擎初始化及生命周期方法等）已由插件自动完成，通常**无需手动配置**。以下内容主要面向 Bare React Native 项目，或需要自定义原生配置的场景。

### Android配置

> v2.0.0 起 Android 原生模块已由 Java 重写为 **Kotlin**（`UnimpModule.kt` / `UnimpPackage.kt`）。自定义原生扩展请基于 Kotlin 代码修改，旧的 Java 实现不再维护。

#### 1. 集成SDK文件

修改原生集成方式，已移除 `android/libs` 下的 `aar` 文件，按照 `build.gradle` 的引入格式，导入到 `JFrog` 或其他的 `maven` 仓库，并添加 `maven` 仓库地址到你项目的 `android/build.gradle`，示例如下：
```gradle
buildscript {
    ext {
      ...
    }
    repositories {
        google()
        mavenCentral()
        maven { url '这里添加本地的maven仓库地址' }
    }
    dependencies {
      ...
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url '这里添加本地的maven仓库地址' }
    }
}
```

#### 2. 导入小程序应用资源

打开android原生项目。在主Module模块的```assets```路径下创建```apps/(内置uni小程序的appid)/www```路径，例如```apps/__UNI__04E3A11/www```，将之前导出的应用资源包解压释放到```apps/__UNI__04E3A11/www```路径下(解压方法将资源包的扩展```.wgt```重命名为```.zip```然后使用解压软件打开)

#### 3. 修改gradle配置

在```android/app/build.gradle```中，添加以下配置，否则可能会出现无法开启小程序，并提示 **运行路径中无 uni 小程序(\_\_UNI\_\_XXXXXXXX)应用资源，请检查应用资源是否正常部署**的问题：

```
android {
  //此处配置必须添加 否则无法正确运行
  aaptOptions {
    additionalParameters '--auto-add-overlay'
    //noCompress 'foo', 'bar'
    ignoreAssetsPattern "!.svn:!.git:.*:!CVS:!thumbs.db:!picasa.ini:!*.scc:*~"
  }
}
```

#### 4. 异常情况

如果出现打开小程序后界面空白，`Logcat` 显示以下日志，需要将 `android/build.gradle` 的`targetSdkVersion` 改为 `28` 即可。

```azure
2024-11-28 16:10:12.573  6748-7055  WXParams       com.unimpexample         E  setCrashFilePath: /data/user/0/com.unimpexample/app_crash
2024-11-28 16:10:12.573  6748-7055  weex           com.unimpexample         E  getUseSingleProcess is running false
2024-11-28 16:10:12.573  6748-7055  weex           com.unimpexample         E  getReleaseMap:true
2024-11-28 16:10:12.573  6748-7055  weex           com.unimpexample         E  getLibJsbPath is running /data/user/0/com.unimpexample/cache/cache/weex/libs/weexjsb/arm64-v8a/libweexjsb.so
2024-11-28 16:10:12.573  6748-7055  weex           com.unimpexample         E  getLibLdPath is running /data/app/com.unimpexample-_xag59cq6fFJxVJuCI463A==/lib/arm64:/data/app/com.unimpexample-_xag59cq6fFJxVJuCI463A==/base.apk!/lib/arm64-v8a
```

检查 `minSdkVersion` 取值范围 `19~22` 注意 `>=23` 一定要在原生项目主app的`AndroidManifest.xml` 中的 `application` 节点配置 `android:extractNativeLibs="true"`

### iOS配置

> v2.0.0 起 iOS 核心逻辑已由 Objective-C 重写为 **Swift**（`Unimp.swift`），并通过 Obj-C++ 桥接层（`UnimpModule.mm` / `UnimpSDKBridge.h/.mm`）对接 TurboModule 与 DCUniMP SDK。Swift 与 Obj-C++ 之间的互操作由桥接层处理，业务侧调用方式保持不变。

#### 1. 安装Git LFS

部分依赖库文件较大，需要安装```git-lfs```插件下载大文件，终端执行git lfs version查看本地是否已安装该插件，如未安装参考安装[Git LFS](https://docs.github.com/zh/enterprise-server@3.8/repositories/working-with-files/managing-large-files/installing-git-large-file-storage)

#### 2. 初始化 sdk engine

> 低于 `React Native 0.77.0` 版本

在 `AppDelegate.m` 中：

```c++
#import "DCUniMP.h"

...

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  // Override point for customization after application launch.

  // 配置参数
  NSMutableDictionary *options = [NSMutableDictionary dictionaryWithDictionary:launchOptions];
  // 设置 debug YES 会在控制台输出 js log，默认不输出 log，注：需要引入 liblibLog.a 库
  [options setObject:[NSNumber numberWithBool:YES] forKey:@"debug"];
  // 初始化引擎
  [DCUniMPSDKEngine initSDKEnvironmentWithLaunchOptions:options];

  return YES;
}

...

#pragma mark - App 生命周期方法
- (void)applicationDidBecomeActive:(UIApplication *)application {
  [DCUniMPSDKEngine applicationDidBecomeActive:application];
}

- (void)applicationWillResignActive:(UIApplication *)application {
  [DCUniMPSDKEngine applicationWillResignActive:application];
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
  [DCUniMPSDKEngine applicationDidEnterBackground:application];
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
  [DCUniMPSDKEngine applicationWillEnterForeground:application];
}

- (void)applicationWillTerminate:(UIApplication *)application {
  [DCUniMPSDKEngine destory];
}

...

#pragma mark - 如果需要使用 URL Scheme 或 通用链接相关功能，请实现以下方法
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {
  // 通过 url scheme 唤起 App
  [DCUniMPSDKEngine application:app openURL:url options:options];
  return YES;
}

- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray<id<UIUserActivityRestoring>> * _Nullable))restorationHandler {
  // 通过通用链接唤起 App
  [DCUniMPSDKEngine application:application continueUserActivity:userActivity];
  return YES;
}

```

> 高于 `React Native 0.77.0` 版本

首先在 `*-Brdging-Header.h` 文件中导入 `DCUniMP` 模块

```c++
//
//  Use this file to import your target's public headers that you would like to expose to Swift.
//
#define uabp_Bridging_Header_h

#import "DCUniMP.h"
```

在 `AppDelegate.swift` 中

```swift
import UIKit
import React
import React_RCTAppDelegate
import ReactAppDependencyProvider

@main
// 2. 阿里云推送 AppDelegate 遵循 UNUserNotificationCenterDelegate 协议
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate  {
  var window: UIWindow?

  var reactNativeDelegate: ReactNativeDelegate?
  var reactNativeFactory: RCTReactNativeFactory?

  func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
  ) -> Bool {
    let delegate = ReactNativeDelegate()
    let factory = RCTReactNativeFactory(delegate: delegate)
    delegate.dependencyProvider = RCTAppDependencyProvider()

    reactNativeDelegate = delegate
    reactNativeFactory = factory

    // 3. 设置通知代理 (必须在应用启动时设置)
    UNUserNotificationCenter.current().delegate = self

    window = UIWindow(frame: UIScreen.main.bounds)

    factory.startReactNative(
      withModuleName: "uabp",
      in: window,
      launchOptions: launchOptions
    )

    // 1. 创建一个可变字典，用于配置 SDK 启动参数
    // 这里的 options 类型需要是 [AnyHashable: Any] 来兼容 Objective-C 的 SDK
    var options: [AnyHashable: Any] = launchOptions ?? [:]

    // 2. 设置 debug 参数为 YES，会在控制台输出 JS log
    // 注意：需要引入 liblibLog.a 库才能看到 JS log
    // 我们使用 NSNumber(value: true) 来确保正确桥接到 Objective-C 的 BOOL/NSNumber 类型
    options["debug"] = NSNumber(value: true)

    // 3. 初始化引擎
    DCUniMPSDKEngine.initSDKEnvironment(launchOptions: options)

    return true
  }

  // MARK: - App Lifecycle Methods (DCUniMPSDK)

  /// Called when the application is about to become the active state.
  func applicationDidBecomeActive(_ application: UIApplication) {
      // Call the DCUniMPSDK method corresponding to app activation
      DCUniMPSDKEngine.applicationDidBecomeActive(application)
  }

  /// Called when the application is about to move from active to inactive state.
  func applicationWillResignActive(_ application: UIApplication) {
      // Call the DCUniMPSDK method corresponding to app resigning active state
      DCUniMPSDKEngine.applicationWillResignActive(application)
  }

  /// Called when the application is entering the background state.
  func applicationDidEnterBackground(_ application: UIApplication) {
      // Call the DCUniMPSDK method corresponding to entering background
      DCUniMPSDKEngine.applicationDidEnterBackground(application)
  }

  /// Called when the application is about to enter the foreground from the background state.
  func applicationWillEnterForeground(_ application: UIApplication) {
      // Call the DCUniMPSDKEngine method corresponding to entering foreground
      DCUniMPSDKEngine.applicationWillEnterForeground(application)
  }

  /// Called when the application is about to terminate.
  func applicationWillTerminate(_ application: UIApplication) {
      // Call the SDK's destory method to clean up resources before termination.
      DCUniMPSDKEngine.destory()
  }
}

class ReactNativeDelegate: RCTDefaultReactNativeFactoryDelegate {
  override func sourceURL(for bridge: RCTBridge) -> URL? {
    self.bundleURL()
  }

  override func bundleURL() -> URL? {
#if DEBUG
    RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index")
#else
    Bundle.main.url(forResource: "main", withExtension: "jsbundle")
#endif
  }
}

```

#### 3. 导入小程序应用资源

打开原生工程目录，在iOS项目路径中创建名称为Apps的文件夹，将之前导出的wgt包拷贝到Apps文件夹中，再右键菜单中选择Add Files to “工程名...”，然后打开工程目录，选择 Apps 文件夹，然后点击“Add”，将应用资源包添加到工程中。

> iOS已改用Pod方式集成，以下配置可以跳过

#### ~~4. 配置CocoaPods~~

~~打开**podfile**文件，添加以下内容：~~

```
# target 'XXX' do
#   pod 'SSZipArchive'
#
#   ...
# end
```

~~在工程的**Build Settings -> Preprocessor Macros -> Debug**和**Release**中分别添加**HAVE_INTTYPES_H**，**HAVE_PKCRYPT**，**HAVE_STDINT_H**，**HAVE_WZAES**，**HAVE_ZLIB** 这 5 个宏定义（注意：原有的配置项下不要删除，点击+号添加））~~


## 支持的接口

| 序号 | 接口名称            | 参数                                                               | 支持平台      | 描述                                        |
|----| ------------------- |------------------------------------------------------------------| ------------- |-------------------------------------------|
| 1  | initialize          | params: InitializeProps, capsuleBtnStyle?: ICapsuleBtnStyleProps | Android / iOS | 小程序初始化方法，支持初始化胶囊按钮样式和菜单                   |
| 2  | isInitialize        | /                                                                | Android       | 检测小程序引擎是否已经初始化                            |
| 3  | getAppBasePath      | appid?: string                                                   | Android / iOS | 获取小程序运行路径                                 |
| 4  | releaseWgtToRunPath | appid: string, wgtPath?: string, password?: string               | Android / iOS | 将wgt包中的资源文件释放到uni小程序运行时路径下                |
| 5  | getWgtPath          | appid: string                                                    | Android / iOS | 读取导入到工程中的wgt应用资源                          |
| 6  | isExistsApp         | appid: string                                                    | Android / iOS | 检查当前appid小程序是否已释放wgt资源，可用来检查当前appid资源是否存在 |
| 7  | openUniMP           | appid: string, configuration?: IConfigurationProps               | Android / iOS | 启动小程序                                     |
| 8  | getAppVersionInfo   | appid: string                                                    | Android / iOS | 获取uni小程序版本信息                              |
| 9  | closeUniMP   | appid: string                                                    | Android / iOS | 宿主关闭小程序                                   |
| 10 | showOrHideUniMP   | appid: string, show: boolean                                     | Android / iOS | 当前小程序显示到前台/退到后台                                   |
| 11 | sendUniMPEvent   | appid: string, eventName: string, data: Record<string, any>                         | Android / iOS | 宿主主动触发事件到正在运行的小程序                                   |
| 12 | getCurrentPageUrl   | appid: string | Android / iOS | 获取运行时uni小程序的当前页面url 可用于页面直达等操作的地址 |
| 13 | setOnUniMPEventCallBack | / | Android / iOS | SDK 初始化完成后，设置监听小程序向宿主发送的事件 |
| 14 | invokeUniMPEventCallback | callbackId: string, responseData: UniMPEventData | Android / iOS | 将宿主处理结果单次回调给小程序，返回 `Promise<boolean>` |
