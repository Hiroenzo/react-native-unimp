#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeDelegate.h>

#import "DCUniMP.h"

@interface Unimp : RCTEventEmitter <RCTBridgeModule,DCUniMPSDKEngineDelegate,RCTBridgeDelegate>

@end
