#import <Foundation/Foundation.h>
#import "Unimp.h"

#define UNIErrorDomain @" An Error Has Occurred"

@implementation Unimp {
    bool hasListeners;
}

RCT_EXPORT_MODULE()

/**
 * 初始化小程序
 * @param params  小程序胶囊按钮参数
 * @param btnStyle 胶囊按钮样式
 */
RCT_EXPORT_METHOD(initialize:(NSDictionary *)params CapsuleButtonStyle:(DCUniMPCapsuleButtonStyle *)btnStyle resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        NSArray *items = params[@"items"];
        NSMutableArray *sheetItems = [NSMutableArray array];
        
        for (int i=0; i<items.count; i++) {
            NSLog(@"-> %@",items[i]);
            [sheetItems addObject:[[DCUniMPMenuActionSheetItem alloc] initWithTitle:items[i][@"title"] identifier:items[i][@"key"]]];
        }
        
        [DCUniMPSDKEngine setDefaultMenuItems:sheetItems];
        
        if (!params[@"btnStyle"]) {
            [DCUniMPSDKEngine configCapsuleButtonStyle:btnStyle];
        }
        
        [DCUniMPSDKEngine setMenuButtonHidden:!params[@"capsule"]];
        [DCUniMPSDKEngine setDelegate:self];
        resolve([NSNumber numberWithBool:YES]);
    } @catch (NSException *exception) {
        reject(@"-1", exception.reason, nil);
    }
}

/**
 * 检查当前appid资源是否存在
 * @param appid 小程序appid
 */
RCT_EXPORT_METHOD(isExistsApp:(NSString *)appid resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([DCUniMPSDKEngine isExistsUniMP:appid]) {
        resolve(@YES);
    } else {
        reject(@"-1", @"小程序资源不存在", nil);
    }
}

/**
 * 获取APP运行路径（应用资源目录）
 * @param appid 小程序appid
 */
RCT_EXPORT_METHOD(getUniMPRunPathWithAppid:(NSString *)appid resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *id = [DCUniMPSDKEngine getUniMPRunPathWithAppid:appid];
    resolve(id);
}

/**
 * 获取已经部署的小程序应用资源版本信息
 * @param appid appid
 */
RCT_EXPORT_METHOD(getAppVersionInfo:(NSString *)appid resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSDictionary *info = [DCUniMPSDKEngine getUniMPVersionInfoWithAppid:appid];
    resolve(info);
}

/**
 * 将wgt资源部署到运行路径中
 * @param appid        appid
 * @param wgtPath   wgt资源路径
 * @param password wgt资源解压密码，无则传nil
 */
RCT_EXPORT_METHOD(releaseWgtToRunPath:(NSString *)appid resourceFilePath:(NSString *)wgtPath password:(NSString *)password resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if (![DCUniMPSDKEngine isExistsUniMP:appid]) {
        NSError *error;
        NSString *appResourcePath = [[NSBundle mainBundle] pathForResource:appid ofType:@"wgt"];
        if (!appResourcePath) {
            reject(@"-1", @"资源路径不正确，请检查", nil);
            return;
        }
        BOOL success = [DCUniMPSDKEngine installUniMPResourceWithAppid:appid resourceFilePath:wgtPath password:password error:&error];
        if (success) {
            // 应用资源文件部署成功
            resolve([DCUniMPSDKEngine getUniMPVersionInfoWithAppid:appid]);
        } else {
            // 应用资源部署失败
            reject(@"-1", @"应用资源部署失败", error);
        }
    } else {
        reject(@"-1", @"应用资源已存在", nil);
    }
}

/**
 * 启动小程序
 * @param appid                   appid
 * @param configuration 小程序配置信息
 */
RCT_EXPORT_METHOD(openUniMP:(NSString *)appid configuration:(DCUniMPConfiguration * __nullable)configuration resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([DCUniMPSDKEngine isExistsUniMP:appid]) {
        [DCUniMPSDKEngine openUniMP:appid configuration:configuration completed:^(DCUniMPInstance * _Nullable uniMPInstance, NSError * _Nullable error) {
            if (uniMPInstance) {
                resolve([NSNumber numberWithBool:YES]);
            } else {
                reject(@"-1", @"小程序开启失败", nil);
            }
        }];
    } else {
        reject(@"-1", @"未找到小程序应用资源", nil);
    }
}

@end
