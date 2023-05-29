#import <Foundation/Foundation.h>
#import "UniMP.h"

#define UNIErrorDomain @" An Error Has Occurred"

@implementation UniMP {
    bool hasListeners;
}

RCT_EXPORT_MODULE(UniMP);

- (NSArray<NSString *> *)supportedEvents {
  return @[@"onError"];
}

/**
 * 初始化小程序
 * @param params  小程序胶囊按钮参数
 * @param btnStyle 胶囊按钮样式
 */
RCT_EXPORT_METHOD(initialize:(NSDictionary *)params CapsuleButtonStyle:(NSDictionary *)btnStyle resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        NSArray *items = params[@"items"];
        NSMutableArray *sheetItems = [NSMutableArray array];
        
        for (int i=0; i<items.count; i++) {
            NSLog(@"-> %@",items[i]);
            [sheetItems addObject:[[DCUniMPMenuActionSheetItem alloc] initWithTitle:items[i][@"title"] identifier:items[i][@"key"]]];
        }
        
        [DCUniMPSDKEngine setDefaultMenuItems:sheetItems];
        
        //if (!params[@"btnStyle"]) {
        //    [DCUniMPSDKEngine configCapsuleButtonStyle:btnStyle];
        //}
        
        //[DCUniMPSDKEngine setMenuButtonHidden:!params[@"capsule"]];
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
        resolve([NSNumber numberWithBool:YES]);
    } else {
        resolve([NSNumber numberWithBool:NO]);
    }
}

/**
 * 获取APP运行路径（应用资源目录）
 * @param appid 小程序appid
 */
RCT_EXPORT_METHOD(getAppBasePath:(NSString *)appid resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        NSString *basePath = [DCUniMPSDKEngine getUniMPRunPathWithAppid:appid];
        resolve(basePath);
    } @catch (NSException *exception) {
        reject(@"-1", exception.reason, nil);
    }
}

/**
 * 获取已经部署的小程序应用资源版本信息
 * @param appid appid
 */
RCT_EXPORT_METHOD(getAppVersionInfo:(NSString *)appid resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        NSDictionary *info = [DCUniMPSDKEngine getUniMPVersionInfoWithAppid:appid];
        resolve(info);
    } @catch (NSException *exception) {
        reject(@"-1", exception.reason, nil);
    }
    
}

/**
 * 读取导入到工程中的wgt应用资源
 * @param appid appid
 */
RCT_EXPORT_METHOD(getResourceFilePath:(NSString *)appid resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        NSString *resourcePath = [[NSBundle mainBundle] pathForResource:appid ofType:@"wgt"];
        resolve(resourcePath);
    } @catch (NSException *exception) {
        reject(@"-1", exception.reason, nil);
    }
}

/**
 * 将wgt资源部署到运行路径中
 * @param appid  appid
 */
RCT_EXPORT_METHOD(releaseWgtToRunPath:(NSString *)appid resourceFilePath:(NSString *)wgtPath password:(NSString *)password resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    #warning 注意：isExistsUniMP: 方法判断的仅是运行路径中是否有对应的应用资源，宿主还需要做好内置wgt版本的管理，如果更新了内置的wgt也应该执行 installUniMPResourceWithAppid 方法应用最新的资源
    if (![DCUniMPSDKEngine isExistsUniMP:appid]) {
        if (!wgtPath) {
            reject(@"-1", @"资源路径不正确，请检查", nil);
            return;
        }
        // 将应用资源部署到运行路径中
        NSError *error;
        if ([DCUniMPSDKEngine installUniMPResourceWithAppid:appid resourceFilePath:wgtPath password:password error:&error]) {
            NSLog(@"小程序 %@ 应用资源文件部署成功，版本信息：%@",appid,[DCUniMPSDKEngine getUniMPVersionInfoWithAppid:appid]);
            resolve([DCUniMPSDKEngine getUniMPVersionInfoWithAppid:appid]);
        } else {
            NSLog(@"小程序 %@ 应用资源部署失败： %@",appid,error);
            reject(@"-1", @"应用资源部署失败", error);
        }
    } else {
        NSLog(@"已存在小程序 %@ 应用资源，版本信息：%@",appid,[DCUniMPSDKEngine getUniMPVersionInfoWithAppid:appid]);
        reject(@"-1", @"应用资源已存在", nil);
    }
}

/**
 * 启动小程序
 * @param appid                   appid
 * @param configuration 小程序配置信息
 */
RCT_EXPORT_METHOD(openUniMP:(NSString *)appid configuration:(NSDictionary *)configuration resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([DCUniMPSDKEngine isExistsUniMP:appid]) {
        // 初始化小程序的配置信息对象
        DCUniMPConfiguration *config = [[DCUniMPConfiguration alloc] init];
        // 配置启动小程序时传递的数据（目标小程序可在 App.onLaunch，App.onShow 中获取到启动时传递的数据）
        config.extraData = @{ @"launchInfo":@"Hello UniMP" };
        // 开启后台运行
        config.enableBackground = NO;
        // 设置打开方式
        config.openMode = DCUniMPOpenModePush;
        // 启用侧滑手势关闭小程序
        config.enableGestureClose = YES;
        
        [DCUniMPSDKEngine openUniMP:appid configuration:config completed:^(DCUniMPInstance * _Nullable uniMPInstance, NSError * _Nullable error) {
            if (uniMPInstance) {
                resolve([NSNumber numberWithBool:YES]);
            } else {
                NSLog(@"打开小程序出错：%@", error);
                reject(@"-1", @"小程序开启失败", nil);
            }
        }];
    } else {
        reject(@"-1", @"未找到小程序应用资源", nil);
    }
}

@end