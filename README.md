# react-native-unimp

集成uni小程序SDK，支持**Android**和**iOS**

## 安装

```sh
npm install react-native-unimp
```

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

## 配置
### Android配置

1、添加资源文件

uni小程序SDK包文件夹目录结构说明：

```
|-- uniMPSDK/SDK	//uni小程序SDK
	|-- assets		// assets资源文件
	|-- Libs		//依赖库
	|-- res			// 资源文件
	|-- src			//微信分享支付需要的activity
	|-- AndroidManifest.xml //模块配置信息
	|-- proguard.cfg  //混淆配置
/-- uniMPSDK/DEMO	//uni小程序SDK示例DEMO
/-- uniMPSDK\Features // 框架已有的原生功能依赖库
	|-- libs //原生功能依赖库

```

SDK包中的**assets**需要拷贝到项目中，目录一般在**app/src/main/assets**下.


2、导入小程序应用资源

打开android原生项目。在主Module模块的assets路径下创建apps/(内置uni小程序的appid)/www 路径，例如**apps/__UNI__04E3A11/www**，将之前导出的应用资源包解压释放到apps/__UNI__04E3A11/www路径下(解压方法将资源包的扩展 .wgt 重命名为 .zip 然后使用解压软件打开)

3、修改gradle配置

在**android/app/build.gradle**中，添加以下配置，否则可能会出现无法开启小程序，并提示 **运行路径中无 uni 小程序(\_\_UNI\_\_XXXXXXXX)应用资源，请检查应用资源是否正常部署**的问题：

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
### iOS配置

1、初始化 sdk engine

在**AppDelegate.m**中：

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

2、导入小程序应用资源

打开原生工程目录，在iOS项目路径中创建名称为Apps的文件夹，将之前导出的wgt包拷贝到Apps文件夹中，再右键菜单中选择Add Files to “工程名...”，然后打开工程目录，选择 Apps 文件夹，然后点击“Add”，将应用资源包添加到工程中。

3、配置CocoaPods

打开**podfile**文件，添加以下内容：

```
target 'XXX' do
  pod 'SSZipArchive'

  ...
end
```

在工程的**Build Settings -> Preprocessor Macros -> Debug**和**Release**中分别添加**HAVE_INTTYPES_H**，**HAVE_PKCRYPT**，**HAVE_STDINT_H**，**HAVE_WZAES**，**HAVE_ZLIB** 这 5 个宏定义（注意：原有的配置项下不要删除，点击+号添加））


## 支持的接口

| 序号 | 接口名称            | 参数                                                         | 支持平台      | 描述                                                         |
| ---- | ------------------- | ------------------------------------------------------------ | ------------- | ------------------------------------------------------------ |
| 1    | initialize          | params: InitializeProps, capsuleBtnStyle?: ICapsuleBtnStyleProps | Android / iOS | 小程序初始化方法，支持初始化胶囊按钮样式和菜单               |
| 2    | isInitialize        | /                                                            | Android       | 检测小程序引擎是否已经初始化                                 |
| 3    | getAppBasePath      | appid?: string                                               | Android / iOS | 获取小程序运行路径                                           |
| 4    | releaseWgtToRunPath | appid: string, wgtPath?: string, password?: string           | Android / iOS | 将wgt包中的资源文件释放到uni小程序运行时路径下               |
| 5    | getWgtPath          | appid: string                                                | Android / iOS | 读取导入到工程中的wgt应用资源                                |
| 6    | isExistsApp         | appid: string                                                | Android / iOS | 检查当前appid小程序是否已释放wgt资源，可用来检查当前appid资源是否存在 |
| 7    | openUniMP           | appid: string, configuration?: IConfigurationProps           | Android / iOS | 启动小程序                                                   |
| 8    | getAppVersionInfo   | appid: string                                                | Android / iOS | 获取uni小程序版本信息                                        |

