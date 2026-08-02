#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTLog.h>

#import "UnimpSDKBridge.h"

// Import the Swift generated header.
// The module name is derived from the pod name with hyphens → underscores.
#if __has_include(<react_native_unimp/react_native_unimp-Swift.h>)
#import <react_native_unimp/react_native_unimp-Swift.h>
#elif __has_include("react_native_unimp-Swift.h")
#import "react_native_unimp-Swift.h"
#endif

/**
 * UnimpModule — Objective-C++ TurboModule entry point.
 *
 * This class is the bridge between React Native and the Swift
 * implementation (`UnimpCore`).  It handles:
 *   - Module registration via RCT_EXPORT_MODULE
 *   - Method exposure via RCT_EXPORT_METHOD / RCT_REMAP_METHOD
 *   - Event emission via RCTEventEmitter
 *
 * All business logic is delegated to `UnimpCore` (Swift).
 */
@interface UnimpModule : RCTEventEmitter <RCTBridgeModule>
@property (nonatomic, strong) UnimpCore *core;
@end

@implementation UnimpModule {
    BOOL hasListeners;
}

RCT_EXPORT_MODULE(Unimp)

- (instancetype)init {
    self = [super init];
    if (self) {
        _core = [[UnimpCore alloc] init];

        // Wire up the event forwarding: when the Swift core receives
        // an event from the DCUniMP SDK, forward it to React Native.
        __weak typeof(self) weakSelf = self;
        _core.onEventReceive = ^(NSString *event, id data, NSString *callbackId) {
            __strong typeof(weakSelf) strongSelf = weakSelf;
            if (!strongSelf || !strongSelf->hasListeners) {
                return;
            }
            NSMutableDictionary *params = [NSMutableDictionary dictionary];
            params[@"event"] = event ?: @"";
            params[@"data"] = data ?: [NSNull null];
            if (callbackId) {
                params[@"callbackId"] = callbackId;
            }
            [strongSelf sendEventWithName:@"onEventReceive" body:params];
        };
    }
    return self;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[
        @"onError",
        @"onEventReceive",
        @"onClose",
        @"onMenuButtonClick",
        @"onCapsuleCloseButtonClick"
    ];
}

- (void)startObserving {
    hasListeners = YES;
}

- (void)stopObserving {
    hasListeners = NO;
}

+ (BOOL)requiresMainQueueSetup {
    return NO;
}

#pragma mark - Initialization

RCT_REMAP_METHOD(initialize,
                 initializeWithParams:(NSDictionary *)params
                 capsuleButtonStyle:(NSDictionary *)btnStyle
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core initializeWithParams:params
               capsuleButtonStyle:btnStyle
                            resolve:resolve
                             reject:reject];
}

RCT_REMAP_METHOD(isInitialize,
                 resolveIsInitializeWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core isInitializeWithResolve:resolve reject:reject];
}

#pragma mark - Path & resource management

RCT_REMAP_METHOD(getAppBasePath,
                 getAppBasePathWithAppid:(NSString *)appid
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core getAppBasePathWithAppid:appid resolve:resolve reject:reject];
}

RCT_REMAP_METHOD(getResourceFilePath,
                 getResourceFilePathWithAppid:(NSString *)appid
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core getResourceFilePathWithAppid:appid resolve:resolve reject:reject];
}

RCT_REMAP_METHOD(getWgtPath,
                 getWgtPathWithAppid:(NSString *)appid
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core getWgtPathWithAppid:appid resolve:resolve reject:reject];
}

RCT_REMAP_METHOD(releaseWgtToRunPath,
                 releaseWgtToRunPathWithAppid:(NSString *)appid
                 wgtPath:(NSString *)wgtPath
                 password:(NSString *)password
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core releaseWgtToRunPathWithAppid:appid
                                      wgtPath:wgtPath
                                     password:password
                                       resolve:resolve
                                        reject:reject];
}

RCT_REMAP_METHOD(isExistsApp,
                 isExistsAppWithAppid:(NSString *)appid
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core isExistsAppWithAppid:appid resolve:resolve reject:reject];
}

#pragma mark - Mini-program lifecycle

RCT_REMAP_METHOD(openUniMP,
                 openUniMPWithAppid:(NSString *)appid
                 configuration:(NSDictionary *)configuration
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core openUniMPWithAppid:appid
                     configuration:configuration
                             resolve:resolve
                              reject:reject];
}

RCT_REMAP_METHOD(closeUniMP,
                 closeUniMPWithAppid:(NSString *)appid
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core closeUniMPWithAppid:appid resolve:resolve reject:reject];
}

RCT_REMAP_METHOD(showOrHideUniMP,
                 showOrHideUniMPWithAppid:(NSString *)appid
                 show:(BOOL)show
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core showOrHideUniMPWithAppid:appid show:show resolve:resolve reject:reject];
}

RCT_REMAP_METHOD(sendUniMPEvent,
                 sendUniMPEventWithAppid:(NSString *)appid
                 eventName:(NSString *)eventName
                 data:(NSDictionary *)data
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core sendUniMPEventWithAppid:appid
                              eventName:eventName
                                   data:data
                                resolve:resolve
                                 reject:reject];
}

RCT_REMAP_METHOD(getCurrentPageUrl,
                 getCurrentPageUrlWithAppid:(NSString *)appid
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core getCurrentPageUrlWithAppid:appid resolve:resolve reject:reject];
}

RCT_REMAP_METHOD(getAppVersionInfo,
                 getAppVersionInfoWithAppid:(NSString *)appid
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core getAppVersionInfoWithAppid:appid resolve:resolve reject:reject];
}

#pragma mark - Event callbacks

RCT_EXPORT_METHOD(setOnUniMPEventCallBack) {
    [self.core setOnUniMPEventCallBack];
}

RCT_EXPORT_METHOD(setDefMenuButtonClickCallBack) {
    [self.core setDefMenuButtonClickCallBack];
}

RCT_EXPORT_METHOD(setUniMPOnCloseCallBack) {
    [self.core setUniMPOnCloseCallBack];
}

RCT_EXPORT_METHOD(setCapsuleCloseButtonClickCallBack) {
    [self.core setCapsuleCloseButtonClickCallBack];
}

RCT_REMAP_METHOD(invokeUniMPEventCallback,
                 invokeUniMPEventCallbackWithCallbackId:(NSString *)callbackId
                 responseData:(id)responseData
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self.core invokeUniMPEventCallbackWithCallbackId:callbackId
                                         responseData:responseData
                                                resolve:resolve
                                                 reject:reject];
}

@end
