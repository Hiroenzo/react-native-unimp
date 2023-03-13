import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-unimp' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Unimp = NativeModules.Unimp
  ? NativeModules.Unimp
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return Unimp.multiply(a, b);
}

export declare interface Config {
  // 胶囊按钮的标题和标识
  items?: { title: string; key: string }[];
  //是否显示胶囊按钮
  capsule?: boolean;
  //安卓独有，胶囊按钮字体大小
  fontSize?: string;
  //安卓独有，胶囊按钮字体颜色
  fontColor?: string;
  //安卓独有，胶囊按钮字体宽度
  fontWeight?: string;
}

export function initialize(params: Config = {}): Promise<boolean> {
  return Unimp.initialize({
    items: [],
    capsule: true,
    fontSize: '16px',
    fontColor: '#000',
    fontWeight: 'normal',
    ...params,
  });
}
