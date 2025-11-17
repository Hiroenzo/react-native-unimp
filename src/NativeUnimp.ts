import type { TurboModule } from 'react-native/Libraries/TurboModule/RCTExport';
import { TurboModuleRegistry } from 'react-native';

/**
 * Unimp TurboModule 接口定义
 * 支持React Native新架构的完整类型定义
 */

// 基础配置类型 - 兼容现有接口
export interface InitializeParams {
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

export interface CapsuleBtnStyle {
  // 背景颜色
  backgroundColor?: string;
  // 字体颜色
  textColor?: string;
  // 高亮颜色
  highlightColor?: string;
  // 边框颜色
  borderColor?: string;
}

export interface UniMPConfiguration {
  // 配置启动小程序时传递的参数
  extraData?: string | object | IExtraDataProps;
  // 小程序启动方式
  openMode?: 'DCUniMPOpenModePresent' | 'DCUniMPOpenModePush';
  // 是否开启侧滑手势关闭小程序
  enableGestureClose?: boolean;
  // 是否开启后台运行
  enableBackground?: boolean;
  // 可用于页面直达等操作的地址
  redirectPath?: string;
}

export interface IExtraDataProps {
  // 是否支持暗色模式
  darkMode?: 'auto' | 'dark' | 'light';
}

export interface AppVersionInfo {
  // versionName
  name: string;
  // versionCode
  code: string;
}

export interface Spec extends TurboModule {
  /**
   * 初始化SDK
   * @param params 初始化参数
   * @param capsuleBtnStyle 胶囊按钮样式（可选）
   * @returns 初始化是否成功
   */
  initialize(
    params: InitializeParams,
    capsuleBtnStyle?: CapsuleBtnStyle
  ): Promise<boolean>;

  /**
   * 检查是否已初始化
   * @returns 是否已初始化
   */
  isInitialize(): Promise<boolean>;

  /**
   * 获取应用基础路径
   * @param appid 应用ID（可选）
   * @returns 基础路径
   */
  getAppBasePath(appid?: string): Promise<string>;

  /**
   * 获取小程序运行路径
   * @param appid 小程序ID
   * @returns 运行路径
   */
  getUniMPRunPathWithAppid(appid: string): Promise<string>;

  /**
   * 释放WGT资源到运行路径
   * @param appid 小程序ID
   * @param wgtPath WGT文件路径（可选）
   * @param password 密码（可选）
   * @returns 释放结果
   */
  releaseWgtToRunPath(
    appid: string,
    wgtPath?: string | null,
    password?: string
  ): Promise<any>;

  /**
   * 检查小程序是否存在
   * @param appid 小程序ID
   * @returns 是否存在
   */
  isExistsApp(appid: string): Promise<boolean>;

  /**
   * 获取WGT路径
   * @param appid 小程序ID
   * @returns WGT路径
   */
  getWgtPath(appid: string): Promise<string>;

  /**
   * 获取资源文件路径
   * @param appid 小程序ID
   * @returns 资源文件路径
   */
  getResourceFilePath(appid: string): Promise<string>;

  /**
   * 打开小程序
   * @param appid 小程序ID
   * @param configuration 配置参数（可选）
   * @returns 打开结果
   */
  openUniMP(appid: string, configuration?: UniMPConfiguration): Promise<any>;

  /**
   * 关闭小程序
   * @param appid 小程序ID
   * @returns 是否成功关闭
   */
  closeUniMP(appid: string): Promise<boolean>;

  /**
   * 显示或隐藏小程序
   * @param appid 小程序ID
   * @param show 是否显示
   * @returns 是否成功
   */
  showOrHideUniMP(appid: string, show: boolean): Promise<boolean>;

  /**
   * 发送事件到小程序
   * @param appid 小程序ID
   * @param eventName 事件名称
   * @param data 事件数据
   * @returns 是否发送成功
   */
  sendUniMPEvent(
    appid: string,
    eventName: string,
    data: Record<string, any>
  ): Promise<boolean>;

  /**
   * 获取应用版本信息
   * @param appid 小程序ID
   * @returns 版本信息
   */
  getAppVersionInfo(appid: string): Promise<any>;

  /**
   * 获取当前页面URL
   * @param appid 小程序ID
   * @returns 当前页面URL
   */
  getCurrentPageUrl(appid: string): Promise<string>;

  /**
   * 添加事件监听器
   * @param eventName 事件名称
   */
  addListener(eventName: string): void;

  /**
   * 移除事件监听器
   * @param count 要移除的监听器数量
   */
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Unimp');
