import { type ConfigPlugin, withPlugins } from '@expo/config-plugins';

import { withAndroidUnimp, type AndroidUnimpPluginOptions } from './withAndroidUnimp';
import { withIosUnimp, type IosUnimpPluginOptions } from './withIosUnimp';

/**
 * Options for the react-native-unimp Expo config plugin.
 */
export interface UnimpPluginOptions {
  /**
   * Android-specific options.
   */
  android?: AndroidUnimpPluginOptions;
  /**
   * iOS-specific options.
   */
  ios?: IosUnimpPluginOptions;
}

/**
 * Default plugin options.
 */
const DEFAULT_OPTIONS: UnimpPluginOptions = {
  android: {
    sdkVersion: '5.14',
  },
  ios: {
    debug: true,
  },
};

/**
 * The main Expo config plugin for react-native-unimp.
 *
 * This plugin automatically configures the native projects for the
 * UniMP SDK when running `expo prebuild`.
 *
 * @example
 * ```json
 * // app.json
 * {
 *   "plugins": [
 *     [
 *       "react-native-unimp",
 *       {
 *         "android": {
 *           "mavenRepositoryUrl": "https://your-maven-repo.com"
 *         },
 *         "ios": {
 *           "debug": true
 *         }
 *       }
 *     ]
 *   ]
 * }
 * ```
 */
const withUnimp: ConfigPlugin<UnimpPluginOptions | void> = (config, options) => {
  const mergedOptions: UnimpPluginOptions = {
    ...DEFAULT_OPTIONS,
    ...options,
    android: { ...DEFAULT_OPTIONS.android, ...options?.android },
    ios: { ...DEFAULT_OPTIONS.ios, ...options?.ios },
  };

  return withPlugins(config, [
    [withAndroidUnimp, mergedOptions.android],
    [withIosUnimp, mergedOptions.ios],
  ]);
};

export default withUnimp;
export { withAndroidUnimp, withIosUnimp };
export type { AndroidUnimpPluginOptions, IosUnimpPluginOptions };
