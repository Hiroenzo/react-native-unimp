import type { TurboModule } from 'react-native/Libraries/TurboModule/RCTExport';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  // 初始化相关
  initialize(params: Object, capsuleBtnStyle?: Object): Promise<boolean>;
  isInitialize(): Promise<boolean>;

  // 路径相关
  getAppBasePath(appid?: string): Promise<string>;
  getUniMPRunPathWithAppid(appid: string): Promise<string>;

  // WGT资源相关
  releaseWgtToRunPath(
    appid: string,
    wgtPath?: string | null,
    password?: string
  ): Promise<any>;
  isExistsApp(appid: string): Promise<boolean>;
  getWgtPath(appid: string): Promise<string>;
  getResourceFilePath(appid: string): Promise<string>;

  // 小程序生命周期
  openUniMP(appid: string, configuration?: Object): Promise<any>;
  closeUniMP(appid: string): Promise<boolean>;
  showOrHideUniMP(appid: string, show: boolean): Promise<boolean>;

  // 事件通信
  sendUniMPEvent(
    appid: string,
    eventName: string,
    data: Object
  ): Promise<boolean>;

  // 信息获取
  getAppVersionInfo(appid: string): Promise<Object>;
  getCurrentPageUrl(appid: string): Promise<string>;

  // 事件监听
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Unimp');
