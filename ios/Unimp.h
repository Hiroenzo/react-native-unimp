#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeDelegate.h>

#import "DCUniMP.h"

@interface Unimp : RCTEventEmitter <RCTBridgeModule,DCUniMPSDKEngineDelegate,RCTBridgeDelegate>

@property (nonatomic, weak) NSMutableDictionary *uniMPInstance; /**< 保存当前打开的小程序应用的引用 注意：请使用 weak 修辞，否则应在关闭小程序时置为 nil */

// 关闭小程序的回调方法
// @param appid appid
- (void)uniMPOnClose:(NSString *)appid;

@end
