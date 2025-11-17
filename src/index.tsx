// React Native 新架构支持
export * from './utils/EventEmitter';
export * from './utils/Methods';
export * from './utils/Types';

// 导出TurboModule规范（用于新架构）
export { default as NativeUnimp } from './NativeUnimp';
export type { Spec as NativeUnimpSpec } from './NativeUnimp';

// 向后兼容性导出
export { Unimp, UnimpEventEmitter } from './utils/NativeUnimp';
