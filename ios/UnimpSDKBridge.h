#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * UnimpSDKBridge is a thin Objective-C wrapper around the DCUniMP SDK.
 *
 * It exists so that the Swift implementation (Unimp.swift) can interact
 * with the Objective-C-only DCUniMP framework without requiring a
 * bridging header or custom module map — the bridge class is compiled
 * in the same pod target and is therefore directly visible to Swift.
 */
@interface UnimpSDKBridge : NSObject

#pragma mark - Initialization

- (BOOL)initializeWithParams:(NSDictionary *)params
          capsuleButtonStyle:(NSDictionary *)btnStyle
                       error:(NSError **)error;
- (BOOL)isInitialize;

#pragma mark - Path & resource management

- (NSString *)getAppBasePath:(NSString *)appid;
- (NSString *)getResourceFilePath:(NSString *)appid;
- (BOOL)isExistsApp:(NSString *)appid;
- (NSDictionary *)releaseWgtToRunPath:(NSString *)appid
                              wgtPath:(NSString *)wgtPath
                             password:(NSString *)password
                                error:(NSError **)error;
- (NSDictionary *)getAppVersionInfo:(NSString *)appid;

#pragma mark - Mini-program lifecycle

- (BOOL)openUniMP:(NSString *)appid
     configuration:(NSDictionary *)configuration
             error:(NSError **)error;
- (BOOL)closeUniMP:(NSString *)appid error:(NSError **)error;
- (BOOL)showOrHideUniMP:(NSString *)appid show:(BOOL)show error:(NSError **)error;
- (BOOL)sendUniMPEvent:(NSString *)appid eventName:(NSString *)eventName data:(NSDictionary *)data;
- (NSString *)getCurrentPageUrl:(NSString *)appid;

#pragma mark - Event callbacks

- (void)setOnUniMPEventCallBackWithReceiveBlock:(void (^)(NSString *event, id data, id callback))block;
- (BOOL)invokeUniMPEventCallback:(NSString *)callbackId
                    responseData:(id)responseData
                           error:(NSError **)error;

@end

NS_ASSUME_NONNULL_END
