import {
  type ConfigPlugin,
  withAppDelegate,
} from '@expo/config-plugins';

/**
 * Options for the iOS side of the plugin.
 */
export interface IosUnimpPluginOptions {
  /**
   * Whether to enable debug logging in the UniMP SDK.
   * @default true
   */
  debug?: boolean;
}

/**
 * Adds the DCUniMP SDK initialization and lifecycle methods to the
 * Swift AppDelegate (RN >= 0.77).
 */
function modifySwiftAppDelegate(
  contents: string,
  debug: boolean
): string {
  // Add import if not present
  if (!contents.includes('DCUniMP')) {
    contents = contents.replace(
      /(import UIKit\n)/,
      `$1import DCUniMP\n`
    );
  }

  // Add SDK initialization inside didFinishLaunchingWithOptions
  if (!contents.includes('DCUniMPSDKEngine.initSDKEnvironment')) {
    const initCode = `    // UniMP SDK initialization
    var unimpOptions: [AnyHashable: Any] = launchOptions ?? [:]
    unimpOptions["debug"] = NSNumber(value: ${debug})
    DCUniMPSDKEngine.initSDKEnvironment(launchOptions: unimpOptions)
`;

    contents = contents.replace(
      /(func application\([\s\S]*?didFinishLaunchingWithOptions[\s\S]*?\{[\s\S]*?)(return true)/,
      `$1${initCode}\n    $2`
    );
  }

  // Add lifecycle methods before the closing brace of the class
  const lifecycleMethods = `
  // MARK: - UniMP SDK Lifecycle

  func applicationDidBecomeActive(_ application: UIApplication) {
    DCUniMPSDKEngine.applicationDidBecomeActive(application)
  }

  func applicationWillResignActive(_ application: UIApplication) {
    DCUniMPSDKEngine.applicationWillResignActive(application)
  }

  func applicationDidEnterBackground(_ application: UIApplication) {
    DCUniMPSDKEngine.applicationDidEnterBackground(application)
  }

  func applicationWillEnterForeground(_ application: UIApplication) {
    DCUniMPSDKEngine.applicationWillEnterForeground(application)
  }

  func applicationWillTerminate(_ application: UIApplication) {
    DCUniMPSDKEngine.destory()
  }
`;

  if (!contents.includes('DCUniMPSDKEngine.applicationDidBecomeActive')) {
    // Find the last closing brace of the AppDelegate class
    const classMatch = contents.match(
      /(class AppDelegate[\s\S]*?\n\})/
    );
    if (classMatch) {
      contents = contents.replace(
        classMatch[0],
        classMatch[0].replace(/\n\}/, `${lifecycleMethods}\n}`)
      );
    }
  }

  return contents;
}

/**
 * Adds the DCUniMP SDK initialization to the Objective-C AppDelegate
 * (RN < 0.77).
 */
function modifyObjcAppDelegate(
  contents: string,
  debug: boolean
): string {
  // Add import
  if (!contents.includes('#import "DCUniMP.h"')) {
    contents = contents.replace(
      /(#import "AppDelegate\.h")/,
      `$1\n#import "DCUniMP.h"`
    );
  }

  // Add initialization
  if (!contents.includes('DCUniMPSDKEngine')) {
    const initCode = `  // UniMP SDK initialization
  NSMutableDictionary *unimpOptions = [NSMutableDictionary dictionaryWithDictionary:launchOptions];
  [unimpOptions setObject:[NSNumber numberWithBool:${debug ? 'YES' : 'NO'}] forKey:@"debug"];
  [DCUniMPSDKEngine initSDKEnvironmentWithLaunchOptions:unimpOptions];
`;

    contents = contents.replace(
      /(- \(BOOL\)application:[\s\S]*?didFinishLaunchingWithOptions:[\s\S]*?\{[\s\S]*?)(return YES)/,
      `$1${initCode}\n  $2`
    );
  }

  // Add lifecycle methods
  const lifecycleMethods = `
#pragma mark - UniMP SDK Lifecycle

- (void)applicationDidBecomeActive:(UIApplication *)application {
  [DCUniMPSDKEngine applicationDidBecomeActive:application];
}

- (void)applicationWillResignActive:(UIApplication *)application {
  [DCUniMPSDKEngine applicationWillResignActive:application];
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
  [DCUniMPSDKEngine applicationDidEnterBackground:application];
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
  [DCUniMPSDKEngine applicationWillEnterForeground:application];
}

- (void)applicationWillTerminate:(UIApplication *)application {
  [DCUniMPSDKEngine destory];
}
`;

  if (!contents.includes('DCUniMPSDKEngine applicationDidBecomeActive')) {
    contents = contents.replace(
      /(@end)/,
      `${lifecycleMethods}\n$1`
    );
  }

  return contents;
}

/**
 * Main iOS config plugin for react-native-unimp.
 */
export const withIosUnimp: ConfigPlugin<IosUnimpPluginOptions> = (
  config,
  options = {}
) => {
  const debug = options.debug ?? true;

  return withAppDelegate(config, (modConfig) => {
    const isSwift = modConfig.modResults.language === 'swift';

    if (isSwift) {
      modConfig.modResults.contents = modifySwiftAppDelegate(
        modConfig.modResults.contents,
        debug
      );
    } else {
      modConfig.modResults.contents = modifyObjcAppDelegate(
        modConfig.modResults.contents,
        debug
      );
    }

    return modConfig;
  });
};
