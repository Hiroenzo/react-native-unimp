#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeDelegate.h>

#import "DCUniMP.h"

@interface UniMP : RCTEventEmitter <RCTBridgeModule,DCUniMPSDKEngineDelegate,RCTBridgeDelegate>

@end
