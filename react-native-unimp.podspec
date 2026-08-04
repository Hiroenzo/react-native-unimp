require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-unimp"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "13.0" }
  s.source       = { :git => "https://github.com/isB1ar/react-native-unimp.git", :tag => "#{s.version}" }

  # Source files – includes Swift, Obj-C++, and headers
  s.source_files = "ios/**/*.{h,m,mm,swift}"

  # Preserve the DCUniMP assets directory
  s.resource_bundles = {
    "react-native-unimp" => ["ios/**/*.xcdatamodeld", "ios/**/*.xib", "ios/**/*.storyboard"]
  }

  # Swift support
  s.pod_target_xcconfig = {
    "DEFINES_MODULE" => "YES",
    "SWIFT_VERSION" => "5.0",
    "SWIFT_INSTALL_OBJC_HEADER" => "YES"
  }

  s.dependency "React"
  s.dependency "React-Core"
  s.dependency 'unimp/Core'
end
