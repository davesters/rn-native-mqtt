require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-native-mqtt"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  An MQTT client or React Native that actually works with a simple Javascript interface
                   DESC
  s.homepage     = "https://github.com/davesters/rn-native-mqtt"
  s.license      = "MIT"
  s.license    = { :type => "MIT", :file => "LICENSE.md" }
  s.authors      = { "David Corona" => "yourname@email.com" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/davesters/rn-native-mqtt.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.dependency "React"
end

