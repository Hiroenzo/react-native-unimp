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

/**
 * 小程序初始化参数
 */
export declare interface InitializeProps {
  // 胶囊按钮的标题和标识
  items?: { title: string; key: string }[];
  // 是否显示胶囊按钮
  capsule: boolean;
  // 胶囊按钮字体大小
  fontSize?: string;
  // 胶囊按钮字体颜色
  fontColor?: string;
  // 胶囊按钮字体宽度
  fontWeight?: string;
  // 设置小程序退出时是否进入后台
  isEnableBackground?: boolean;
}

/**
 * 胶囊按钮样式
 */
export declare interface ICapsuleBtnStyleProps {
  // 背景颜色
  backgroundColor?: string;
  // 字体颜色
  textColor?: string;
  // 高亮颜色
  highlightColor?: string;
  // 边框颜色
  borderColor?: string;
}

/**
 * 初始化小程序SDK
 * @param params          初始化参数
 * @param capsuleBtnStyle 胶囊按钮样式
 */
export function initialize(
  params: InitializeProps,
  capsuleBtnStyle?: ICapsuleBtnStyleProps
): Promise<boolean> {
  return Unimp.initialize(params, capsuleBtnStyle);
}

/**
 * 校验小程序SDK是否已初始化（Android）
 */
export function isInitialize(): Promise<boolean> {
  // 只支持Android
  if (Platform.OS === 'android') {
    return Unimp.isInitialize();
  }
  return Promise.resolve(true);
}

/**
 * 获取小程序运行路径
 * @param appid 小程序appid
 */
export function getAppBasePath(appid?: string): Promise<string> {
  if (Platform.OS === 'android') {
    return Unimp.getAppBasePath();
  } else {
    if (!appid) {
      return Promise.reject({ message: 'appid不能为空' });
    }
    return Unimp.getUniMPRunPathWithAppid(appid);
  }
}

/**
 * 将wgt包中的资源文件释放到uni小程序运行时路径下
 * @param appid    小程序appid
 * @param wgtPath  小程序应用资源包路径 仅支持SD路径 不支持assets
 * @param password 资源包解压密码（猜的）
 */
export function releaseWgtToRunPath(
  appid: string,
  wgtPath?: string | undefined | null,
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
 * 读取导入到工程中的wgt应用资源
 * @param appid 小程序appid
 */
export function getWgtPath(appid: string): Promise<string> {
  return Unimp.getWgtPath(appid);
}

export declare interface IExtraDataProps {
  // 是否支持暗色模式
  darkMode?: 'auto' | 'dark' | 'light';
}

/**
 * 开启小程序时传入的配置参数
 */
export declare interface IConfigurationProps {
  // 配置启动小程序时传递的参数
  extraData?: string | object | IExtraDataProps;
  // 小程序启动方式
  openMode?: 'DCUniMPOpenModePresent' | 'DCUniMPOpenModePush';
  // 是否开启侧滑手势关闭小程序
  enableGestureClose?: boolean;
  // 是否开启后台运行
  enableBackground?: boolean;
}

/**
 * 启动小程序
 * @param appid         uni小程序应用id
 * @param configuration uni小程序应用配置
 */
export async function openUniMP(
  appid: string,
  configuration?: IConfigurationProps
): Promise<any> {
  try {
    if (Platform.OS === 'android') {
      return Unimp.openUniMP(appid, configuration);
    } else {
      const isExists = await isExistsApp(appid);
      if (!isExists) {
        const wgtPath = await Unimp.getResourceFilePath(appid);
        if (!wgtPath) {
          return Promise.reject({ message: `未找到 ${appid} 的资源路径` });
        }
        await releaseWgtToRunPath(appid, wgtPath);
      }
      return Unimp.openUniMP(appid, configuration);
    }
  } catch (error) {
    return Promise.reject(error);
  }
}

/**
 * uni小程序版本信息
 */
export declare interface IAppVersionInfoProps {
  // versionName
  name: string;
  // versionCode
  code: string;
}

/**
 * 获取uni小程序版本信息
 * @param appid uni小程序应用id
 */
export function getAppVersionInfo(
  appid: string
): Promise<IAppVersionInfoProps> {
  return Unimp.getAppVersionInfo(appid);
}

/**
 * 小程序胶囊按钮点击回调参数
 */
export declare interface IMenuClickCallBackProps {
  // uni小程序应用id
  appid: string;
  // 小程序菜单按钮id
  buttonid: string;
}

/**
 * 小程序菜单点击事件回调
 * @param callback 回调方法
 */
export function setDefMenuButtonClickCallBack(
  callback: (arg: IMenuClickCallBackProps) => any
) {
  Unimp.setDefMenuButtonClickCallBack((params: IMenuClickCallBackProps) =>
    callback?.(params)
  );
}

/**
 * 监听小程序被关闭事件
 * @param callback 回调方法
 */
export function setUniMPOnCloseCallBack(callback: (appid: string) => any) {
  Unimp.setUniMPOnCloseCallBack((appid: string) => callback?.(appid));
}

/**
 * 小程序胶囊按钮点击关闭事件
 * @param callback 回调方法
 */
export function setCapsuleCloseButtonClickCallBack(
  callback: (appid: string) => any
) {
  Unimp.setCapsuleCloseButtonClickCallBack((appid: string) =>
    callback?.(appid)
  );
}
