#import "UnimpSDKBridge.h"
#import "DCUniMP.h"

@interface UnimpSDKBridge () <DCUniMPSDKEngineDelegate>
@property (nonatomic, strong) NSMutableDictionary<NSString *, DCUniMPInstance *> *uniMPInstances;
@property (nonatomic, strong) NSMutableDictionary<NSString *, DCUniMPKeepAliveCallback> *eventCallbacks;
@property (nonatomic, copy) void (^eventReceiveBlock)(NSString *event, id data, id callback);
@end

@implementation UnimpSDKBridge

- (instancetype)init {
    self = [super init];
    if (self) {
        _uniMPInstances = [NSMutableDictionary dictionary];
        _eventCallbacks = [NSMutableDictionary dictionary];
    }
    return self;
}

#pragma mark - Initialization

- (BOOL)initializeWithParams:(NSDictionary *)params
          capsuleButtonStyle:(NSDictionary *)btnStyle
                       error:(NSError **)error {
    @try {
        NSArray *items = params[@"items"];
        NSMutableArray *sheetItems = [NSMutableArray array];
        for (NSUInteger i = 0; i < items.count; i++) {
            [sheetItems addObject:[[DCUniMPMenuActionSheetItem alloc]
                initWithTitle:items[i][@"title"]
                    identifier:items[i][@"key"]]];
        }

        [DCUniMPSDKEngine setDefaultMenuItems:sheetItems];

        if (!params[@"btnStyle"]) {
            [DCUniMPSDKEngine configCapsuleButtonStyle:btnStyle];
        }

        [DCUniMPSDKEngine setMenuButtonHidden:![params[@"capsule"] boolValue]];
        [DCUniMPSDKEngine setDelegate:self];
        return YES;
    } @catch (NSException *exception) {
        if (error) {
            *error = [NSError errorWithDomain:@"UnimpErrorDomain"
                                          code:-1
                                      userInfo:@{NSLocalizedDescriptionKey: exception.reason ?: @"Unknown error"}];
        }
        return NO;
    }
}

- (BOOL)isInitialize {
    // iOS SDK does not expose an explicit isInitialize check.
    // Once the engine delegate is set, the SDK is considered initialized.
    return YES;
}

#pragma mark - Path & resource management

- (NSString *)getAppBasePath:(NSString *)appid {
    return [DCUniMPSDKEngine getUniMPRunPathWithAppid:appid];
}

- (NSString *)getResourceFilePath:(NSString *)appid {
    return [[NSBundle mainBundle] pathForResource:appid ofType:@"wgt"];
}

- (BOOL)isExistsApp:(NSString *)appid {
    return [DCUniMPSDKEngine isExistsUniMP:appid];
}

- (NSDictionary *)releaseWgtToRunPath:(NSString *)appid
                              wgtPath:(NSString *)wgtPath
                             password:(NSString *)password
                                error:(NSError **)error {
    if (![DCUniMPSDKEngine isExistsUniMP:appid]) {
        if (!wgtPath) {
            if (error) {
                *error = [NSError errorWithDomain:@"UnimpErrorDomain"
                                              code:-1
                                          userInfo:@{NSLocalizedDescriptionKey: @"资源路径不正确，请检查"}];
            }
            return nil;
        }
        NSError *installError;
        if ([DCUniMPSDKEngine installUniMPResourceWithAppid:appid
                                           resourceFilePath:wgtPath
                                                   password:password
                                                     error:&installError]) {
            return [DCUniMPSDKEngine getUniMPVersionInfoWithAppid:appid];
        } else {
            if (error) {
                *error = installError ?: [NSError errorWithDomain:@"UnimpErrorDomain"
                                                              code:-1
                                                          userInfo:@{NSLocalizedDescriptionKey: @"应用资源部署失败"}];
            }
            return nil;
        }
    } else {
        if (error) {
            *error = [NSError errorWithDomain:@"UnimpErrorDomain"
                                          code:-1
                                      userInfo:@{NSLocalizedDescriptionKey: @"应用资源已存在"}];
        }
        return nil;
    }
}

- (NSDictionary *)getAppVersionInfo:(NSString *)appid {
    return [DCUniMPSDKEngine getUniMPVersionInfoWithAppid:appid];
}

#pragma mark - Mini-program lifecycle

- (BOOL)openUniMP:(NSString *)appid
     configuration:(NSDictionary *)configuration
             error:(NSError **)error {
    @try {
        if (![DCUniMPSDKEngine isExistsUniMP:appid]) {
            if (error) {
                *error = [NSError errorWithDomain:@"UnimpErrorDomain"
                                              code:-1
                                          userInfo:@{NSLocalizedDescriptionKey: @"未找到小程序应用资源"}];
            }
            return NO;
        }

        DCUniMPConfiguration *config = [[DCUniMPConfiguration alloc] init];

        if (configuration[@"extraData"]) {
            config.extraData = configuration[@"extraData"];
        }
        config.enableBackground = configuration[@"enableBackground"] ? [configuration[@"enableBackground"] boolValue] : NO;
        if (configuration[@"openMode"]) {
            config.openMode = [configuration[@"openMode"] integerValue];
        } else {
            config.openMode = DCUniMPOpenModePresent;
        }
        config.enableGestureClose = configuration[@"enableGestureClose"] ? [configuration[@"enableGestureClose"] boolValue] : YES;
        if (configuration[@"redirectPath"]) {
            config.path = configuration[@"redirectPath"];
        }

        dispatch_async(dispatch_get_main_queue(), ^{
            [DCUniMPSDKEngine openUniMP:appid configuration:config completed:^(DCUniMPInstance * _Nullable uniMPInstance, NSError * _Nullable openError) {
                if (uniMPInstance) {
                    @synchronized(self.uniMPInstances) {
                        self.uniMPInstances[appid] = uniMPInstance;
                    }
                } else {
                    NSLog(@"打开小程序出错：%@", openError);
                }
            }];
        });
        return YES;
    } @catch (NSException *exception) {
        if (error) {
            *error = [NSError errorWithDomain:@"UnimpErrorDomain"
                                          code:-1
                                      userInfo:@{NSLocalizedDescriptionKey: exception.reason ?: @"Unknown error"}];
        }
        return NO;
    }
}

- (BOOL)closeUniMP:(NSString *)appid error:(NSError **)error {
    @synchronized(self.uniMPInstances) {
        DCUniMPInstance *instance = self.uniMPInstances[appid];
        if (!instance) {
            if (error) {
                *error = [NSError errorWithDomain:@"UnimpErrorDomain"
                                              code:-1
                                          userInfo:@{NSLocalizedDescriptionKey: @"小程序实例不存在"}];
            }
            return NO;
        }
        [instance closeWithCompletion:^(BOOL success, NSError * _Nullable closeError) {
            if (success) {
                @synchronized(self.uniMPInstances) {
                    [self.uniMPInstances removeObjectForKey:appid];
                }
            }
        }];
        return YES;
    }
}

- (BOOL)showOrHideUniMP:(NSString *)appid show:(BOOL)show error:(NSError **)error {
    @synchronized(self.uniMPInstances) {
        DCUniMPInstance *instance = self.uniMPInstances[appid];
        if (!instance) {
            if (error) {
                *error = [NSError errorWithDomain:@"UnimpErrorDomain"
                                              code:-1
                                          userInfo:@{NSLocalizedDescriptionKey: @"小程序实例不存在"}];
            }
            return NO;
        }
        if (show) {
            [instance showWithCompletion:nil];
        } else {
            [instance hideWithCompletion:nil];
        }
        return YES;
    }
}

- (BOOL)sendUniMPEvent:(NSString *)appid eventName:(NSString *)eventName data:(NSDictionary *)data {
    @synchronized(self.uniMPInstances) {
        DCUniMPInstance *instance = self.uniMPInstances[appid];
        if (instance) {
            [instance sendUniMPEvent:eventName data:data];
            return YES;
        }
        return NO;
    }
}

- (NSString *)getCurrentPageUrl:(NSString *)appid {
    @synchronized(self.uniMPInstances) {
        DCUniMPInstance *instance = self.uniMPInstances[appid];
        if (instance) {
            return [DCUniMPSDKEngine getCurrentPageUrl] ?: @"";
        }
        return @"";
    }
}

#pragma mark - Event callbacks

- (void)setOnUniMPEventCallBackWithReceiveBlock:(void (^)(NSString *, id, id))block {
    self.eventReceiveBlock = block;
    [DCUniMPSDKEngine setDelegate:self];
}

- (BOOL)invokeUniMPEventCallback:(NSString *)callbackId
                    responseData:(id)responseData
                           error:(NSError **)error {
    if (callbackId.length == 0) {
        if (error) {
            *error = [NSError errorWithDomain:@"UnimpErrorDomain"
                                          code:-1
                                      userInfo:@{NSLocalizedDescriptionKey: @"callbackId不能为空"}];
        }
        return NO;
    }

    if (responseData != nil &&
        ![responseData isKindOfClass:[NSString class]] &&
        ![responseData isKindOfClass:[NSDictionary class]]) {
        if (error) {
            *error = [NSError errorWithDomain:@"UnimpErrorDomain"
                                          code:-1
                                      userInfo:@{NSLocalizedDescriptionKey: @"回调参数仅支持 NSString 或 NSDictionary"}];
        }
        return NO;
    }

    @synchronized(self.eventCallbacks) {
        DCUniMPKeepAliveCallback callback = self.eventCallbacks[callbackId];
        [self.eventCallbacks removeObjectForKey:callbackId];
        if (!callback) {
            if (error) {
                *error = [NSError errorWithDomain:@"UnimpErrorDomain"
                                              code:-1
                                          userInfo:@{NSLocalizedDescriptionKey: @"未找到callbackId对应的小程序回调"}];
            }
            return NO;
        }
        callback(responseData, NO);
        return YES;
    }
}

#pragma mark - DCUniMPSDKEngineDelegate

- (void)onUniMPEventReceive:(NSString *)event data:(id)data callback:(DCUniMPKeepAliveCallback)callback {
    if (callback) {
        NSString *callbackId = [NSUUID UUID].UUIDString;
        @synchronized(self.eventCallbacks) {
            self.eventCallbacks[callbackId] = [callback copy];
        }
        if (self.eventReceiveBlock) {
            self.eventReceiveBlock(event, data, callbackId);
        }
    } else {
        if (self.eventReceiveBlock) {
            self.eventReceiveBlock(event, data, nil);
        }
    }
}

@end
