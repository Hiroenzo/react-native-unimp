import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

/**
 * Codegen TurboModule Spec for react-native-unimp
 *
 * This spec is consumed by React Native's Codegen to generate native
 * abstract classes (Android) / protocols (iOS) that the platform
 * implementations must conform to.
 *
 * NOTE: All platform-specific behaviour is handled inside the native
 * implementations.  The JS layer in `utils/Methods.ts` still performs
 * Platform.select() where needed, so the spec intentionally exposes a
 * superset of methods that covers both Android and iOS.
 */
export interface Spec extends TurboModule {
  // ── Initialization ──────────────────────────────────────────────
  initialize(params: Object, capsuleBtnStyle: Object): Promise<boolean>;
  isInitialize(): Promise<boolean>;

  // ── Path & resource management ──────────────────────────────────
  getAppBasePath(appid: string): Promise<string>;
  getResourceFilePath(appid: string): Promise<string>;
  getWgtPath(appid: string): Promise<string>;
  releaseWgtToRunPath(
    appid: string,
    wgtPath: string,
    password: string
  ): Promise<Object>;
  isExistsApp(appid: string): Promise<boolean>;

  // ── Mini-program lifecycle ──────────────────────────────────────
  openUniMP(appid: string, configuration: Object): Promise<string>;
  closeUniMP(appid: string): Promise<boolean>;
  showOrHideUniMP(appid: string, show: boolean): Promise<boolean>;
  sendUniMPEvent(
    appid: string,
    eventName: string,
    data: Object
  ): Promise<boolean>;
  getCurrentPageUrl(appid: string): Promise<string>;
  getAppVersionInfo(appid: string): Promise<string>;

  // ── Event callbacks (void – fire-and-forget from JS) ────────────
  setOnUniMPEventCallBack(): void;
  setDefMenuButtonClickCallBack(): void;
  setUniMPOnCloseCallBack(): void;
  setCapsuleCloseButtonClickCallBack(): void;

  // ── UniMP event callback invocation ─────────────────────────────
  invokeUniMPEventCallback(
    callbackId: string,
    responseData: Object
  ): Promise<boolean>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Unimp');
