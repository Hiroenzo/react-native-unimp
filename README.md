# react-native-unimp

mini program for uni-app

## 安装

```sh
npm install react-native-unimp
```

## 使用

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
