import {
  NativeEventEmitter,
  NativeModules,
  Platform,
  TurboModuleRegistry,
} from 'react-native';

import type { Spec } from '../NativeUnimp';

const LINKING_ERROR =
  `The package 'react-native-unimp' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// On the New Architecture, TurboModuleRegistry returns the generated
// TurboModule.  On the legacy bridge (RN ≥ 0.71) it transparently falls
// back to the NativeModule registered under the same name, so this single
// call covers both architectures.
export const Unimp: Spec =
  (TurboModuleRegistry.get<Spec>('Unimp') as Spec | null) ??
  (NativeModules.Unimp as Spec | undefined) ??
  new Proxy(
    {} as Spec,
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  );

export const UnimpEventEmitter = () => new NativeEventEmitter(Unimp as any);
