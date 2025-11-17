import { Platform } from 'react-native';

import { Unimp } from './NativeUnimp';

import type {
  IAppVersionInfoProps,
  ICapsuleBtnStyleProps,
  IConfigurationProps,
  InitializeProps,
} from './Types';

// 错误处理辅助函数
async function handleNativeCall<T>(
  method: () => Promise<T>,
  fallback?: T
): Promise<T> {
  try {
    return await method();
  } catch (error) {
    if (fallback !== undefined) {
      return fallback;
    }
    throw error;
  }
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
  return handleNativeCall(
    () => Unimp.initialize(params, capsuleBtnStyle),
    false
  );
}

/**
 * 校验小程序SDK是否已初始化（Android）
 */
export function isInitialize(): Promise<boolean> {
  // 只支持Android
  if (Platform.OS === 'android') {
    return handleNativeCall(() => Unimp.isInitialize(), false);
  }
  return Promise.resolve(true);
}

/**
 * 获取小程序运行路径
 * @param appid 小程序appid
 */
export function getAppBasePath(appid?: string): Promise<string> {
  if (Platform.OS === 'android') {
    return handleNativeCall(() => Unimp.getAppBasePath(), '');
  } else {
    if (!appid) {
      return Promise.reject({ message: 'appid不能为空' });
    }
    return handleNativeCall(() => Unimp.getUniMPRunPathWithAppid(appid), '');
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
  wgtPath?: string | null,
  password?: string
): Promise<any> {
  return handleNativeCall(
    () => Unimp.releaseWgtToRunPath(appid, wgtPath, password),
    null
  );
}

/**
 * 检查当前appid小程序是否已释放wgt资源
 * 可用来检查当前appid资源是否存在
 * @param appid 小程序appid
 */
export function isExistsApp(appid: string): Promise<boolean> {
  return handleNativeCall(() => Unimp.isExistsApp(appid), false);
}

/**
 * 读取导入到工程中的wgt应用资源
 * @param appid 小程序appid
 */
export function getWgtPath(appid: string): Promise<string> {
  return handleNativeCall(() => Unimp.getWgtPath(appid), '');
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
      return handleNativeCall(
        () => Unimp.openUniMP(appid, configuration),
        null
      );
    } else {
      const isExists = await isExistsApp(appid);
      if (!isExists) {
        const wgtPath = await handleNativeCall(
          () => Unimp.getResourceFilePath(appid),
          ''
        );
        if (!wgtPath) {
          return Promise.reject({ message: `未找到 ${appid} 的资源路径` });
        }
        await releaseWgtToRunPath(appid, wgtPath);
      }
      return handleNativeCall(
        () => Unimp.openUniMP(appid, configuration),
        null
      );
    }
  } catch (error) {
    return Promise.reject(error);
  }
}

/**
 * 手动关闭小程序
 * @param appid 小程序ID
 */
export async function closeUniMP(appid: string): Promise<boolean> {
  return handleNativeCall(() => Unimp.closeUniMP(appid), false);
}

/**
 * 当前小程序显示到前台/退到后台
 * @param appid 小程序ID
 * @param show  显示或隐藏
 */
export async function showOrHideUniMP(
  appid: string,
  show: boolean
): Promise<boolean> {
  return handleNativeCall(() => Unimp.showOrHideUniMP(appid, show), false);
}

/**
 * 宿主主动触发事件到正在运行的小程序
 * @param appid     小程序ID
 * @param eventName 显示或隐藏
 * @param data      传参
 */
export async function sendUniMPEvent(
  appid: string,
  eventName: string,
  data: Record<string, any>
): Promise<boolean> {
  return handleNativeCall(
    () => Unimp.sendUniMPEvent(appid, eventName, data),
    false
  );
}

/**
 * 获取uni小程序版本信息
 * @param appid uni小程序应用id
 */
export function getAppVersionInfo(
  appid: string
): Promise<IAppVersionInfoProps> {
  return handleNativeCall(
    async () => {
      const result = await Unimp.getAppVersionInfo(appid);
      // 确保返回的对象符合 IAppVersionInfoProps 接口
      if (
        result &&
        typeof result === 'object' &&
        'name' in result &&
        'code' in result
      ) {
        return result as IAppVersionInfoProps;
      }
      return { name: '', code: '' };
    },
    { name: '', code: '' }
  );
}

/**
 * 获取运行时uni小程序的当前页面url 可用于页面直达等操作的地址
 * @param appid 当前运行的小程序应用id
 */
export function getCurrentPageUrl(appid: string): Promise<string> {
  return handleNativeCall(() => Unimp.getCurrentPageUrl(appid), '');
}
