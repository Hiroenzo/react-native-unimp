import { NativeEventEmitter, Platform } from 'react-native';
import NativeUnimp, { type Spec } from '../NativeUnimp';

// 检查原生模块是否可用
function isTurboModuleAvailable(): boolean {
  try {
    return NativeUnimp != null;
  } catch (error) {
    return false;
  }
}

const LINKING_ERROR =
  `The package 'react-native-unimp' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n' +
  '- New Architecture is properly configured\n';

// 创建符合 Spec 接口的代理对象
function createProxySpec(): Spec {
  return new Proxy({} as Spec, {
    get(_target, _prop) {
      return () => {
        throw new Error(LINKING_ERROR);
      };
    },
  });
}

// 使用TurboModule
export const Unimp: Spec = isTurboModuleAvailable()
  ? NativeUnimp
  : createProxySpec();

// 创建事件发射器
export const UnimpEventEmitter = () => {
  if (isTurboModuleAvailable()) {
    return new NativeEventEmitter(NativeUnimp);
  }
  throw new Error(LINKING_ERROR);
};
