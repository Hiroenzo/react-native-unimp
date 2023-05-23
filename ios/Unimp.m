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
        NSArray *item = params[@"items"];
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
        reject([NSNumber numberWithBool:NO]);
    }
}

/**
 * 检查当前appid资源是否存在
 * @param appid 小程序appid
 */
RCT_EXPORT_METHOD(isExistsApp:(NSString *)appid resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve([DCUniMPSDKEngine isExistsUniMP:appid]);
}

/**
 * 获取APP运行路径（应用资源目录）
 * @param appid 小程序appid
 */
RCT_EXPORT_METHOD(getUniMPRunPathWithAppid:(NSString *)appid resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve([DCUniMPSDKEngine getUniMPRunPathWithAppid:appid]);
}

/**
 * 获取已经部署的小程序应用资源版本信息
 * @param appid appid
 */
RCT_EXPORT_METHOD(getUniMPVersionInfoWithAppid:(NSString *)appid resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve([DCUniMPSDKEngine getUniMPVersionInfoWithAppid:appid]);
}

/**
 * 将wgt资源部署到运行路径中
 * @param appid        appid
 * @param wgtPath   wgt资源路径
 * @param password wgt资源解压密码，无则传nil
 */
RCT_EXPORT_METHOD(releaseWgtToRunPath:(NSString *)appid resourceFilePath:(NSString *)wgtPath password:(NSString *)password resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    NSError *error;
    BOOL success = [DCUniMPSDKEngine installUniMPResourceWithAppid:appid resourceFilePath:wgtPath password:password error:&error];
    if (success) {
        resolve(success);
    } else {
        reject([NSNumber numberWithBool:NO], error);
    }
}

/**
 * 启动小程序
 * @param appid                   appid
 * @param configuration 小程序配置信息
 */
RCT_EXPORT_METHOD(openUniMP:(NSString *)appid configuration:(DCUniMPConfiguration * __nullable)configuration resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseResolveBlock)reject)
{
    [DCUniMPSDKEngine openUniMP:appid configuration:configuration completed:^(DCUniMPInstance * _Nullable uniMPInstance, NSError * _Nullable error) {
        if (uniMPInstance) {
            resolve([NSNumber numberWithBool:YES]);
        } else {
            reject([NSNumber numberWithBool:NO], error);
        }
    }];
}

@end
