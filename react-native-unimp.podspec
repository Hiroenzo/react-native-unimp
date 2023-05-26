require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-unimp"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "11.0" }
  s.source       = { :git => "https://github.com/Hiroenzo/react-native-unimp.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,a}"

  s.dependency "React"
  s.vendored_frameworks = 'ios/libs/**/*.framework'
  s.resources = 'ios/Resources/**/*.{js,ttf,bundle}'
  s.vendored_libraries = "ios/libs/**/*.a"
  s.frameworks = "JavaScriptCore","CoreMedia","MediaPlayer","AVFoundation","AVKit","GLKit","OpenGLES","CoreText","QuartzCore","CoreGraphics","QuickLook","CoreTelephony","AssetsLibrary","CoreLocation","AddressBook"
  s.library = "c++"
end
