Pod::Spec.new do |spec|
  spec.name                 = "WowzaGoCoderSDK"
  spec.version              = "1.0.0"
  spec.summary              = "WowzaGoCoderSDK framework."
  spec.description          = "This spec specifies a vendored framework."
  spec.platform             = :ios
  spec.homepage             = "https://github.com/jsfan3/CN1Libs-WowzaLiveStreaming"
  spec.source               = {:path => "."}
  spec.author               = "Francesco Galgani"
  spec.vendored_frameworks  = "WowzaGoCoderSDK.framework"
end


