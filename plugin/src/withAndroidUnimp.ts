import {
  type ConfigPlugin,
  withProjectBuildGradle,
  withAppBuildGradle,
  withAndroidManifest,
} from '@expo/config-plugins';

/**
 * Options for the Android side of the plugin.
 */
export interface AndroidUnimpPluginOptions {
  /**
   * Maven repository URL where the UniMP SDK aar files are hosted.
   * If not provided, no additional repository is added.
   */
  mavenRepositoryUrl?: string;
  /**
   * UniMP SDK version (default: 5.14).
   */
  sdkVersion?: string;
}

/**
 * Adds the UniMP Maven repository to the project-level build.gradle.
 */
const withUnimpMavenRepo: ConfigPlugin<AndroidUnimpPluginOptions> = (
  config,
  options
) => {
  return withProjectBuildGradle(config, (modConfig) => {
    if (!options.mavenRepositoryUrl) {
      return modConfig;
    }

    const buildGradle = modConfig.modResults.contents;
    const repoLine = `        maven { url '${options.mavenRepositoryUrl}' }`;

    // Add to buildscript.repositories
    if (
      buildGradle.includes('buildscript') &&
      !buildGradle.includes(options.mavenRepositoryUrl)
    ) {
      modConfig.modResults.contents = buildGradle.replace(
        /(buildscript\s*\{[\s\S]*?repositories\s*\{)/,
        `$1\n${repoLine}`
      );
    }

    // Add to allprojects.repositories
    const allReposMatch = modConfig.modResults.contents.match(
      /(allprojects\s*\{[\s\S]*?repositories\s*\{)/
    );
    if (
      allReposMatch &&
      !modConfig.modResults.contents.includes(options.mavenRepositoryUrl)
    ) {
      modConfig.modResults.contents = modConfig.modResults.contents.replace(
        allReposMatch[0],
        `${allReposMatch[0]}\n${repoLine}`
      );
    }

    return modConfig;
  });
};

/**
 * Adds aaptOptions and other gradle configurations to app/build.gradle.
 */
const withUnimpAppGradle: ConfigPlugin<void> = (config) => {
  return withAppBuildGradle(config, (modConfig) => {
    let appGradle = modConfig.modResults.contents;

    // Add aaptOptions if not already present
    if (!appGradle.includes('aaptOptions')) {
      const aaptConfig = `    aaptOptions {
        additionalParameters '--auto-add-overlay'
        ignoreAssetsPattern "!.svn:!.git:.*:!CVS:!thumbs.db:!picasa.ini:!*.scc:*~"
    }`;

      // Insert inside the android { } block
      appGradle = appGradle.replace(
        /(android\s*\{)/,
        `$1\n${aaptConfig}`
      );
    }

    // Ensure extractNativeLibs is set for compatibility
    if (!appGradle.includes('extractNativeLibs')) {
      const extractNativeConfig = `    packagingOptions {
        jniLibs {
            useLegacyPackaging true
        }
    }`;

      appGradle = appGradle.replace(
        /(android\s*\{)/,
        `$1\n${extractNativeConfig}`
      );
    }

    modConfig.modResults.contents = appGradle;
    return modConfig;
  });
};

/**
 * Adds necessary permissions and configuration to AndroidManifest.xml.
 */
const withUnimpManifest: ConfigPlugin<void> = (config) => {
  return withAndroidManifest(config, (modConfig) => {
    const manifest = modConfig.modResults;

    // Add WRITE_EXTERNAL_STORAGE permission
    const permissions = manifest.manifest['uses-permission'] || [];
    const hasWritePermission = permissions.some(
      (p: any) =>
        p.$?.['android:name'] === 'android.permission.WRITE_EXTERNAL_STORAGE'
    );
    if (!hasWritePermission) {
      permissions.push({
        $: { 'android:name': 'android.permission.WRITE_EXTERNAL_STORAGE' },
      });
      manifest.manifest['uses-permission'] = permissions;
    }

    // Set extractNativeLibs on the application node
    const application = manifest.manifest.application?.[0];
    if (application?.$ && !application.$['android:extractNativeLibs']) {
      application.$['android:extractNativeLibs'] = 'true';
    }

    return modConfig;
  });
};

/**
 * Main Android config plugin for react-native-unimp.
 */
export const withAndroidUnimp: ConfigPlugin<AndroidUnimpPluginOptions> = (
  config,
  options = {}
) => {
  config = withUnimpMavenRepo(config, options);
  config = withUnimpAppGradle(config);
  config = withUnimpManifest(config);
  return config;
};
