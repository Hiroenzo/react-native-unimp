#import <React/RCTBridgeModule.h>
#import "RCUniMP.h"
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeDelegate.h>

@interface Unimp : RCTEventEmitter <RCTBridgeModule,DCUniMPSDKEngineDelegate,RCTBridgeDelegate>

@end
