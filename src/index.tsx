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

export declare interface InitializeProps {
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

/**
 * 初始化小程序SDK
 * @param params 初始化参数
 */
export function initialize(params: InitializeProps = {}): Promise<boolean> {
  return Unimp.initialize(params);
}

/**
 * 校验小程序SDK是否已初始化
 */
export function isInitialize(): Promise<boolean> {
  return Unimp.isInitialize();
}

/**
 * 获取小程序运行路径
 */
export function getAppBasePath(): Promise<string> {
  return Unimp.getAppBasePath();
}

/**
 * 将wgt包中的资源文件释放到uni小程序运行时路径下
 * @param appid    uni小程序的id
 * @param wgtPath  uni小程序应用资源包路径 仅支持SD路径 不支持assets
 * @param password 资源包解压密码（猜的）
 */
export function releaseWgtToRunPath(
  appid: string,
  wgtPath: string,
  password?: string
): Promise<any> {
  return Unimp.releaseWgtToRunPath(appid, wgtPath, password);
}

/**
 * 检查当前appid小程序是否已释放wgt资源
 * 可用来检查当前appid资源是否存在
 * @param appid 小程序appid
 */
export function isExistsApp(appid: string): Promise<boolean> {
  return Unimp.isExistsApp(appid);
}

/**
 * 启动小程序
 * @param appid uni小程序应用id
 */
export function openUniMP(appid: string): Promise<any> {
  return Unimp.openUniMP(appid);
}
