
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNUnimpSpec.h"

@interface Unimp : NSObject <NativeUnimpSpec>
#else
#import <React/RCTBridgeModule.h>

@interface Unimp : NSObject <RCTBridgeModule>
#endif

@end
