import Foundation

/**
 * UnimpCore — Swift implementation of the react-native-unimp module.
 *
 * This class wraps `UnimpSDKBridge` (which in turn wraps the Objective-C
 * DCUniMP SDK) and exposes `@objc` methods that the Objective-C++
 * TurboModule entry point (`UnimpModule.mm`) calls into.
 *
 * All business logic lives here in Swift; the Obj-C++ layer is only a
 * thin registration / event-emission bridge.
 */
@objc(UnimpCore)
public class UnimpCore: NSObject {

    // MARK: - Properties

    private let bridge = UnimpSDKBridge()

    /// Closure called when a mini-program sends an event to the host.
    /// Set by the Obj-C++ TurboModule wrapper so it can forward the
    /// event to React Native via `RCTEventEmitter`.
    @objc public var onEventReceive: ((String, Any?, String?) -> Void)?

    // MARK: - Initialization

    @objc public func initialize(
        params: [String: Any],
        capsuleButtonStyle: [String: Any]?,
        resolve: @escaping (Bool) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        var error: NSError?
        let success = bridge.initialize(
            withParams: params,
            capsuleButtonStyle: capsuleButtonStyle,
            error: &error
        )
        if success {
            resolve(true)
        } else {
            reject("-1", error?.localizedDescription ?? "初始化失败", error)
        }
    }

    @objc public func isInitialize(
        resolve: @escaping (Bool) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        resolve(bridge.isInitialize())
    }

    // MARK: - Path & resource management

    @objc public func getAppBasePath(
        appid: String,
        resolve: @escaping (String) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        let path = bridge.getAppBasePath(appid)
        resolve(path)
    }

    @objc public func getResourceFilePath(
        appid: String,
        resolve: @escaping (String?) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        let path = bridge.getResourceFilePath(appid)
        resolve(path)
    }

    @objc public func getWgtPath(
        appid: String,
        resolve: @escaping (String) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        // On iOS, wgt resources are bundled in the main bundle.
        let path = bridge.getResourceFilePath(appid) ?? ""
        resolve(path)
    }

    @objc public func releaseWgtToRunPath(
        appid: String,
        wgtPath: String?,
        password: String?,
        resolve: @escaping (Any) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        var error: NSError?
        let result = bridge.releaseWgt(
            toRunPath: appid,
            wgtPath: wgtPath,
            password: password,
            error: &error
        )
        if let result = result {
            resolve(result)
        } else {
            reject("-1", error?.localizedDescription ?? "部署失败", error)
        }
    }

    @objc public func isExistsApp(
        appid: String,
        resolve: @escaping (Bool) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        resolve(bridge.isExistsApp(appid))
    }

    // MARK: - Mini-program lifecycle

    @objc public func openUniMP(
        appid: String,
        configuration: [String: Any]?,
        resolve: @escaping (String) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        var error: NSError?
        let success = bridge.openUniMP(
            appid,
            configuration: configuration,
            error: &error
        )
        if success {
            resolve(appid)
        } else {
            reject("-1", error?.localizedDescription ?? "打开小程序失败", error)
        }
    }

    @objc public func closeUniMP(
        appid: String,
        resolve: @escaping (Bool) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        var error: NSError?
        let success = bridge.closeUniMP(appid, error: &error)
        if success {
            resolve(true)
        } else {
            reject("-1", error?.localizedDescription ?? "关闭小程序失败", error)
        }
    }

    @objc public func showOrHideUniMP(
        appid: String,
        show: Bool,
        resolve: @escaping (Bool) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        var error: NSError?
        let success = bridge.showOrHideUniMP(appid, show: show, error: &error)
        if success {
            resolve(true)
        } else {
            reject("-1", error?.localizedDescription ?? "操作失败", error)
        }
    }

    @objc public func sendUniMPEvent(
        appid: String,
        eventName: String,
        data: [String: Any],
        resolve: @escaping (Bool) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        let success = bridge.sendUniMPEvent(appid, eventName: eventName, data: data)
        resolve(success)
    }

    @objc public func getCurrentPageUrl(
        appid: String,
        resolve: @escaping (String) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        let url = bridge.getCurrentPageUrl(appid)
        resolve(url)
    }

    @objc public func getAppVersionInfo(
        appid: String,
        resolve: @escaping (String) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        let info = bridge.getAppVersionInfo(appid)
        if let info = info,
           let data = try? JSONSerialization.data(withJSONObject: info),
           let jsonString = String(data: data, encoding: .utf8) {
            resolve(jsonString)
        } else {
            resolve("")
        }
    }

    // MARK: - Event callbacks

    @objc public func setOnUniMPEventCallBack() {
        bridge.setOnUniMPEventCallBack { [weak self] event, data, callbackId in
            self?.onEventReceive?(event, data, callbackId)
        }
    }

    @objc public func setDefMenuButtonClickCallBack() {
        // Menu button click is handled via DCUniMPSDKEngineDelegate
        // On iOS, this is not a separate registration like Android.
    }

    @objc public func setUniMPOnCloseCallBack() {
        // Close callback is handled via DCUniMPSDKEngineDelegate
        // On iOS, this is not a separate registration like Android.
    }

    @objc public func setCapsuleCloseButtonClickCallBack() {
        // Capsule close button is handled via DCUniMPSDKEngineDelegate
        // On iOS, this is not a separate registration like Android.
    }

    @objc public func invokeUniMPEventCallback(
        callbackId: String,
        responseData: Any?,
        resolve: @escaping (Bool) -> Void,
        reject: @escaping (String, String, Error?) -> Void
    ) {
        var error: NSError?
        let success = bridge.invokeUniMPEventCallback(
            callbackId,
            responseData: responseData,
            error: &error
        )
        if success {
            resolve(true)
        } else {
            reject("-1", error?.localizedDescription ?? "回调失败", error)
        }
    }
}
