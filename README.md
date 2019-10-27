
Wowza Live Streaming Events for Codename One<br>Table of Contents
=================

   * [Introduction](#cn1libs---wowza-live-events-streaming)
   * [General info](#general-info)
      * [Test only on real devices](#test-only-on-real-devices)
         * [How to debug on Android Studio and on XCode](#how-to-debug-on-android-studio-and-on-xcode)
      * [Secure authentication HMAC](#secure-authentication-hmac)
         * [Secure authentication and timestamp in virtual machines](#secure-authentication-and-timestamp-in-virtual-machines)
         * [Secure authentication and export laws](#secure-authentication-and-export-laws)
      * [Installation](#installation)
      * [Build hint added automatically and compatibility with other CN1Libs](#build-hint-added-automatically-and-compatibility-with-other-cn1libs)
      * [Build hint to be customized and added manually](#build-hint-to-be-customized-and-added-manually)
      * [Device compatibility](#device-compatibility)
      * [Requirements](#requirements)
      * [How to get the four required API keys](#how-to-get-the-four-required-api-keys)
      * [Javadocs of this CN1Lib](#javadocs-of-this-cn1lib)
   * [Example of usage, step by step](#example-of-usage-step-by-step)
      * [Enable the verbose log](#enable-the-verbose-log)
      * [Choose the streaming quality](#choose-the-streaming-quality)
      * [How to insert the API keys in the CN1Lib](#how-to-insert-the-api-keys-in-the-cn1lib)
      * [Create a live stream](#create-a-live-stream)
         * [Create a live stream (the easiest way, default parameters)](#create-a-live-stream-the-easiest-way-default-parameters)
         * [Create a live stream (with custom parameters)](#create-a-live-stream-with-custom-parameters)
      * [Live stream creation errors and API limits](#live-stream-creation-errors-and-api-limits)
      * [Start broadcasting / recording a live stream event](#start-broadcasting--recording-a-live-stream-event)
         * [Start a stream before broadcasting a live event and stop it when the event is finished](#start-a-stream-before-broadcasting-a-live-event-and-stop-it-when-the-event-is-finished)
         * [Check the state of a stream](#check-the-state-of-a-stream)
         * [Get the first stopped stream in a pool of streams](#get-the-first-stopped-stream-in-a-pool-of-streams)
            * [Example 1: empty pool (no stream)](#example-1-empty-pool-no-stream)
            * [Example 2: poolSize &lt; startingSize](#example-2-poolsize--startingsize)
            * [Example 3: poolSize &gt;= startingSize &amp;&amp; usedStream \x &gt; threshold \x](#example-3-poolsize--startingsize--usedstream---threshold-)
            * [Example 4: all streams of the pool are used](#example-4-all-streams-of-the-pool-are-used)
            * [Example 5: poolSize &gt;= startingSize &amp;&amp; usedStream \x &lt; threshold \x](#example-5-poolsize--startingsize--usedstream---threshold-)
            * [Additional info about the use of pool](#additional-info-about-the-use-of-pool)
         * [Start the stream](#start-the-stream)
         * [Start, stop, play the broadcasting](#start-stop-play-the-broadcasting)
   * [Full working example (iOS or Android app)](#full-working-example-ios-or-android-app)
   * [License](#license)

# CN1Libs - Wowza Live Events Streaming

**_Disclaimer: consider this CN1Lib in alpha stage, not ready yet for production environments. If you want to contribute to the code to make this CN1Lib working better, you are welcome._**

The purpose of this CN1Lib is to add live streaming capabilities to iOS and Android Codename One apps, hiding all the complexities and reducing the effort.

However live events are not trivial, that's why you should read this README carefully. After the necessary explanations, there is a full working example to stream a live event from a smartphone to other smartphones.

In the following photo, you can see a streaming from my iPhone to my Android, *using the full working example code that I reported at the bottom of this Readme*: in the photo, I'm looking at the computer clock to see the streaming delay.

![Wowza Streaming with Codename One](https://user-images.githubusercontent.com/1997316/67627234-92c10300-f847-11e9-94b5-1c5ce1643587.jpg)

# General info

This CN1Lib allows to:
- broadcast a live video streaming event from a mobile app: the streaming is identified by an unique `id` (automatically assigned);
- play the live video streaming with a given `id`, with an adaptive bitrate;
- record live streamings to be able to watch them later;
- all the streaming operations (storage, processing, adapting to multi-bitrate, broadcasting to multiple devices, etc.) are automatically backended by [Wowza Cloud Service](https://www.wowza.com/products/streaming-cloud);
- have an accurate logging.

Limitations:
- **you have to lock the screen orientation**, otherwise the app can crash or do bad things on rotate (see: https://stackoverflow.com/a/48717607).

More specifically, this CN1Lib integrates and makes use of [GoCoder SDK for iOS](https://www.wowza.com/docs/how-to-install-gocoder-sdk-for-ios), [GoCoder SDK for Android](https://www.wowza.com/docs/how-to-install-gocoder-sdk-for-android), and it performs RESTful requests to the [Wowza Streaming Cloud service](https://www.wowza.com/products/streaming-cloud) (live event plan).

## Test only on real devices

Running and debugging apps that use the GoCoder SDK (like this CN1Lib) using a device emulator isn't recommended due to the wide variance in functionality between the software-based audio and video codecs used by the emulator and the hardware-based codecs installed on most devices.

### How to debug on Android Studio and on XCode

You need to use the "include source" feature (https://www.codenameone.com/how-do-i---use-the-include-sources-feature-to-debug-the-native-code-on-iosandroid-etc.html).

* After opening the downloaded sources on Android Studio, you have to use a Gradle 4.6 local distribution ([download](https://services.gradle.org/distributions/gradle-4.6-bin.zip)), otherwise the project sync will fail: you can set it in File, Settings, "Build, Execution, Deployment", Gradle, "Use local gradle distribution". To compile, I use the Android SDK Tools 25.2.5 ([download](https://androidsdkoffline.blogspot.com/p/android-sdk-tools.html)), as explained in: https://github.com/codenameone/CodenameOne/issues/2816#issuecomment-496186168

* Before opening the workspace file in Xcode, you have to extract [this file](https://github.com/jsfan3/CN1Libs-WowzaLiveStreaming/files/3775410/Archivio.zip) and copy its content in the `dist` folder of the sources: after that, open the terminal, `cd` to the `dist` folder and run `pod install`. After that, you can use the sources in Xcode. This procedure is necessary because the build server currently doesn't include the podspecs and frameworks in the sources \*tar.gz ([more info](https://github.com/jsfan3/CN1Libs-WowzaLiveStreaming/issues/1)).

## Secure authentication HMAC

The Wowza Streaming Cloud REST API uses hash-based message authentication code (HMAC) for secure authentication in production environments. In this form of authentication, your secret key is never sent in the http request headers, however _the app have to know your secret key to generate a "wsc-signature" for every request_: this is not the maximum security (because your secret key is obfuscated in the app code), but it is still a good level of security and the best security we can get without an intermediary server that stores your keys in a safe place.

Technically, **this CN1Lib implements an HMAC generator algorithm (for iOS, Android and Codename One Simulator)** as requested in the Wowza document "[Generate and encrypt a signature for the request](https://www.wowza.com/docs/how-to-use-the-wowza-streaming-cloud-rest-api#signature)".

However, HMAC authentication is disabled by default because it can give issues. To enable that, use `WowzaLiveStream.setHmacAuthentication(true)`.

### Secure authentication and timestamp in virtual machines

Note that the HMAC used by Wowza relies on the timestamp: its value must be **within 15 seconds** of the Wowza server clock, or the signature is considered invalid.

This assumes that the clock of the mobile devices and of your developing computer are correctly synchronized. Nowadays all devices are automatically synchronized with atomic clocks and normally you should not worry about that. To test that, if you have two or more mobile phones you can open the clock inside the [Kitchen Sink demo](https://www.codenameone.com/demos-KitchenSink.html): the clocks will indicate the same time.

However, I noticed that the clock of the Virtualbox guest machine which I use to develop is not synchronized correctly with the clock of the host machine ***after taking a shapshot***: this is an old age bug of VirtualBox that can cause authentication issues in the Codename One Simulator. A simple workaround is to run the following Bash script after taking a snapshot (assuming that you are using Ubuntu or a derived distro):
```
#!/bin/bash
sudo service ntp stop
sudo ntpdate it.pool.ntp.org
sudo service ntp start
```
You can replace `it.pool.ntp.org` with one of the NTP servers listed in https://www.ntppool.org/

### Secure authentication and export laws

I don't think that there are issues with U.S. export laws _(secure "authentication" is one of the exemptions provided under category 5 part 2, of the BIS Export Administration Regulation)_, however you can find all the necessary info at the pages:

* [Complying with Encryption Export Regulations](https://help.apple.com/app-store-connect/#/devc3f64248f)
* [Export compliance documentation for encryption](https://developer.apple.com/documentation/security/complying_with_encryption_export_regulations)


## Installation
Follow the standard way to install a CN1Lib from the Extension Manager: https://www.codenameone.com/blog/automatically-install-update-distribute-cn1libs-extensions.html

## Build hint added automatically and compatibility with other CN1Libs

Take note that this CN1Lib appends automatically the build hints:

```
codename1.arg.android.xpermissions=<uses-permission android\:name\="android.permission.CAMERA" android\:required\="false" /><uses-permission android\:name\="android.permission.RECORD_AUDIO" android\:required\="false" /><uses-permission android\:name\="android.permission.INTERNET" android\:required\="false" /><uses-permission android\:name\="android.permission.FLASHLIGHT" android\:required\="false" /><uses-permission android\:name\="android.permission.MODIFY_AUDIO_SETTINGS" android\:required\="false" />
codename1.arg.android.gradleDep=compile group\: 'commons-codec', name\: 'commons-codec', version\: '1.13';
codename1.arg.android.proguardKeep=-keep class com.wowza.** { *; }
codename1.arg.ios.pods=WowzaGoCoderSDK
```

It also sets as required:
```
codename1.arg.android.min_sdk_version=23
codename1.arg.ios.deployment_target=11.0
```

This can affect the compatibility with your project or with other CN1Libs.

## Build hint to be customized and added manually

[Cocoa keys](https://developer.apple.com/library/archive/documentation/General/Reference/InfoPlistKeyReference/Articles/CocoaKeys.html) required to access camera and microphone, you can customize them with proper description:

```
ios.NSCameraUsageDescription=Some functionality of the application requires access to your camera
ios.NSMicrophoneUsageDescription=Some functionality of the application requires access to your microphone
```

## Device compatibility

- iOS 11 or later.

- Android 6.0 or later.

## Requirements

[Wowza Streaming Cloud](https://www.wowza.com/docs/wowza-streaming-cloud) offers serveral services. This CN1Lib is intended to be used with a Wowza [Streaming Cloud Live Event-Based](https://www.wowza.com/pricing/streaming-cloud-plans/live-event) service, that allows: live stream recording, multi-bitrate streaming, unlimited simultaneous input streams, etc..

## How to get the four required API keys
To use with CN1Lib you need a subscription to the page [Wowza Streaming Cloud Live Event-Based Pricing](https://www.wowza.com/pricing/streaming-cloud-plans/live-event). At the time of writing, the basic plan costs $49, however there is also a 30-days [free trial](https://www.wowza.com/pricing/cloud-developer-free-trial).

After the registration, you need your API keys, entering your "iOS Bundle Identifier" and your "Android Package Name" at the page [Wowza GoCoder SDK license](https://www.wowza.com/products/gocoder/sdk/license). Note that you can register as many iOS Bundle Identifiers and Android Package Names as you need, because the use of GoCoder SDK is free. You will receive an e-mail with subject `CONFIDENTIAL: Your Wowza GoCoder SDK License`, containing an API key for Android and an API key for iOS.

After that, open https://cloud.wowza.com/ with your credentials (for more info, see: [Prepare authentication for your request](https://www.wowza.com/docs/how-to-broadcast-a-live-stream-by-using-the-wowza-streaming-cloud-rest-api#prepare-authentication-for-your-request)). In the menu bar, click your user name and choose API Access. In the middle of the page there is your Wowza Streaming Cloud REST Api key. **Note that this is your private key: don't copy it directly in the app, but firstly obfuscate it using this [String Encoder](https://www.codenameone.com/demos/StringEncoder/index.html)** _(it's a simply Xor encoding, for more info see "[Secure Codename One apps](https://www.codenameone.com/manual/security.html)")_. You can put in the app code a comment with your private key near to the obfuscated version of the key, it's safe because the comments are removed from the compiled code.

Finally, you need an access key, that is is a unique, alphanumeric string that you can use to authenticate RESTful HTTP requests. To generate a new access key, click "Add Access Key" on the left menu. Leave "Enabled" selected so that the key is immediately available, and provide an optional "Description". Then, click "Add".

At this point, you should have the required four keys to use this CN1Lib.

## Javadocs of this CN1Lib

See the Javadocs: 

The main class to be used is: 

# Example of usage, step by step

## Enable the verbose log
This CN1Lib can act silently or with a verbose logs. In both cases, you can write your logs in the callbacks. To enable the verbose logging, that uses the `Log.debug` level, you can use:
```
// Enable verbose logging (which log level is "debug")
WowzaAccount.setVerboseLog(true);
```

## Choose the streaming quality
To simplify the life, this CN1Lib has only three presets that are fine on mobile devices, you can choose one of them so:

* `GoCoder.setQuality(GoCoder.LOW_QUALITY_360p);` -> Video resolution of 640x360 pixels, 1-Mbps video bitrate, 30 fps, keyframe interval of 30, 44.1 kHz audio sample rate, 64-kbps audio bitrate.

* `GoCoder.setQuality(GoCoder.MEDIUM_QUALITY_720p);` -> **default quality**, Video resolution of 1280x720 pixels, 3.75-Mbps video bitrate, 30 fps, keyframe interval of 30, 44.1 kHz audio sample rate, 64-kbps audio bitrate.

* `GoCoder.setQuality(GoCoder.HIGH_QUALITY_1080p);` -> Video resolution of 1920x1080 pixels, 5-Mbps video bitrate, 30 fps, keyframe interval of 30, 44.1 kHz audio sample rate, 64-kbps audio bitrate.

## How to insert the API keys in the CN1Lib
```
// Register the Wowza keys
WowzaAccount.setApiKey_iOS("GOSK-ABCD-ABCD-ABCD-ABCD-ABCD");
WowzaAccount.setApiKey_Android("GOSK-ABCD-ABCD-ABCD-ABCD-ABCD");
// to obfuscate: https://www.codenameone.com/demos/StringEncoder/index.html
WowzaAccount.setRestKey("put-here-your-OBFUSCATED-private-key");
WowzaAccount.setAccessKey("abcdefghijklmnopqrstuvewxyzabcdefghijklmnopqrstuvewxyzabcdefghi");
```

## Create a live stream
You can create a live stream with default parameters or custom parameters. In both cases, you can see the live streams you create at https://cloud.wowza.com/

### Create a live stream (the easiest way, default parameters)
You can create a Wowza Streaming Cloud live stream that will receive a stream from you app so:
```
// Create a new streaming with default parameters
WowzaLiveStream stream = new WowzaLiveStream();
OnComplete<WowzaLiveStream> onComplete = (WowzaLiveStream v) -> {
    Log.p("New live stream created, the id is: " + v.getId(), Log.INFO);
};
Runnable onFail = () -> {
    Log.p("Failed to create a new live stream", Log.WARNING);
};
stream.create("MyLiveStream_1", onComplete, onFail);
```
This code hides all the default values that I set and that I suppose fine for starting to test the streaming _(adaptive bitrate, 1920x1080, server placed in Germany, streams recorded on the server)_. Note that you can create up to 10 streams within 3 hours, as explained [here](https://www.wowza.com/docs/Wowza-Streaming-Cloud-REST-API-limits). You can use the same name for different streams, although in my opinion it's not a good way to do (because this does not help to distinguish them).

After executing this code, you'll have a log like the following:
```
[EDT] 0:0:2,186 - WowzaLiveStream -> (Code 201) Successfully created live stream with name MyLiveStream_1
[EDT] 0:0:2,186 - New live stream created, the id is: abc123fg
```

You can see the stream you just created in the [Wowza Streaming Cloud web site](https://cloud.wowza.com/).

### Create a live stream (with custom parameters)
Keep in mind that the customizations allowed by this CN1Lib are a bit restrictive: I implemented a deep check of all the custom values to be sure that they are valid according to the Wowza documentation and according to the purposes of this CN1Lib.

More specifically, the parameters that you can customize are:

- `name` -> required, from 1 to 200 chars, no spaces, can contain only uppercase and lowercase letters; numbers; and the period (`.`), underscore (`_`), and hyphen (`-`) characters. No other special characters can be used.

- `aspect_ratio_width` -> defaults to 1920, allowed values from 768 to 3840, it must be divisible by 8, keep attention that aspect_ratio_width & aspect_ratio_height must be in proportion 16:9 or 4:3.

- `aspect_ratio_height` -> defaults to 1080, allowed values from 576 to 2160, it must be divisible by 8, keep attention that aspect_ratio_width & aspect_ratio_height must be in proportion 16:9 or 4:3.

- `broadcast_location` -> defaults to `eu_germany`, valid values are: `asia_pacific_australia`, `asia_pacific_india`, `asia_pacific_japan`, `asia_pacific_singapore`, `asia_pacific_s_korea`, `asia_pacific_taiwan`, `eu_belgium`, `eu_germany`, `eu_ireland`, `south_america_brazil`, `us_central_iowa`, `us_east_s_carolina`, `us_east_virginia`, `us_west_california`, `us_west_oregon`.

- `transcoder_type` -> defaults to `transcoded` (that means multi-bitrate), the only other available option is `passthrough` (that means single bitrate); note that `passthrough` is supported only if `broadcast_location` is one of: `asia_pacific_taiwan`, `eu_belgium`, `us_central_iowa`, `us_east_s_carolina`.

- `recording` -> default to `true` (it records the live event allowing it to be played later), the only other available option is `false`.

Additional note about stream size: _you can encode and broadcast up to 3840x2160, or 8.3 megapixels (4K), but playback is only supported up to 1920x1080_.

Example:
```
// Create a new streaming with custom parameters
WowzaLiveStream stream = new WowzaLiveStream();
OnComplete<WowzaLiveStream> onComplete = (WowzaLiveStream v) -> {
    Log.p("New live stream created, the id is: " + v.getId(), Log.INFO);
};
Runnable onFail = () -> {
    Log.p("Failed to create a new live stream", Log.WARNING);
};
WowzaLiveStreamParams params = new WowzaLiveStreamParams();
params.name.set("MyLiveEventWithCustomParameters"); // the name is the only mandatory property to be set
params.aspect_ratio_width.set(768); // note that 768x576 is a 4:3 streaming, normally you should use 16:9 widescreen streams
params.aspect_ratio_height.set(576);
params.broadcast_location.set("eu_belgium"); // Belgium is one of the few locations that supports "passthrough"
// params.transcoder_type.set("passthrough"); // means single bitrate (but it doesn't seem available for trial accounts)
params.recording.set(Boolean.FALSE); // the video will not be recorder to be seen later
stream.create(params, onComplete, onFail);
```

Note that `WowzaLiveStreamParams` is a [PropertyBusinessObject](https://www.codenameone.com/blog/properties-are-amazing.html) that contains more parameters than the ones mentioned, but you cannot change them, otherwise you will get an `IllegalArgumentException` by `stream.create(params, onComplete, onFail)`.

## Live stream creation errors and API limits
If in the previous steps of stream creation occurs a server error, it's accurately logged. For example:
```
[EDT] 0:0:1,554 - WowzaLiveStream -> (Code 422) Unprocessable Entity, failed to create live stream with name MyLiveEventWithCustomParameters, string returned by the server: "{"meta":{"status":422,"code":"ERR-422-InvalidInteraction","title":"Invalid Interaction Error","message":"This profile can't create passthrough transcoders.","description":""}}"
[EDT] 0:0:1,554 - Failed to create a new live stream
```
I guess that the cause of this specific issue is that I used a trial account. Remember that there are [limits for paid users](https://www.wowza.com/docs/Wowza-Streaming-Cloud-REST-API-limits) and [limits for trial accounts](https://www.wowza.com/pricing/cloud-developer-free-trial).

In general, when you exceed an [API limit](https://www.wowza.com/docs/Wowza-Streaming-Cloud-REST-API-limits), you get a 429 "too many requests" error. 

## Start broadcasting / recording a live stream event

To start a stream, it must be previously created and its current state must be "stopped".

### Start a stream before broadcasting a live event and stop it when the event is finished
Transcoding charges accrue for started live streams even if they aren't actively streaming content. Wowza Streaming Cloud automatically stops idle live streams after 20 minutes, but you should stop your stream as soon as your event ends to avoid accruing unnecessary charges _(as documented [here](https://www.wowza.com/docs/how-to-broadcast-a-live-stream-by-using-the-wowza-streaming-cloud-rest-api#stop-the-live-stream))_.

### Check the state of a stream
Remember that every stream is identified by an unique `id`. To check the state of a given stream, you can use one of three versions of the method `fetchState` of the class `WowzaLiveStream`, for example:
```
WowzaLiveStream.fetchState(id, streamId -> {
    // the stream is stopped
    Log.p("The stream with id " + streamId + " is stopped.", Log.INFO);
}, streamId -> {
    // the stream is not stopped
    Log.p("The stream with id " + streamId + " is not stopped.", Log.INFO);
}, () -> {
    // server error
    Log.p("Server error trying to get the state of stream with id " + id, Log.ERROR);
});
```
The previous method signature has a minimal set of callbacks.
You can have a callback for each of the possible states using this other method:
```
public static void fetchState(String id, OnComplete<String> isStarted, OnComplete<String> isStopped, OnComplete<String> isStarting, OnComplete<String> isStopping, OnComplete<String> isResetting, Runnable onFail)
```

### Get the first stopped stream in a pool of streams
Normally you can create up to 10 streams within 3 hours, but this limit can be worked around in a legal way permitted (and encouraged) by Wowza. In fact in the Wowza documentation, section [Create a pool of resources](https://www.wowza.com/docs/Wowza-Streaming-Cloud-REST-API-limits#create-a-pool-of-resources), it's written:

_«[...] These limits apply only to the number of new resources you can create within a three-hour period. You can create additional resources in the next three-hour window, and there's no limit to the total number of Wowza Streaming Cloud resources (live streams, transcoders, stream sources, and stream targets) that you can have provisioned and ready to use in your account._

_Particularly in high-volume production environments, we recommend that you manage API limits by creating a pool of resources, configured for your specific streaming needs, in advance. This way, you always have resources available when you need them._

_For example, create 50 stream targets over the course of 15 hours during a period of downtime. Then, when you activate one, request another from the API at the same time. This allows you to tap a pool of ready-and-waiting resources on the fly while simultaneously replenishing the pool with a new resource, all without being restrained by the API's limits._

_You can scale the size of your pool up or down as needed, depending on your usage. For example, instead of adding a new resource when you start using one, you might want to set minimum and maximum thresholds — when 80 percent of your pool is active, for example, start requesting that many more from the API. If you're only using 30 percent on a regular basis, delete some resources (but remember to allow time to re-create them if you need to do so later). This helps you avoid bumping up against limits when you're in a production crunch.»_

The method `getStreamFromPool` of the class `WowzaLiveStream` implements what suggested in the quoted documentation, its Javadoc is:
```
public static void getStreamFromPool(int startingSize,
                                     int threshold,
                                     WowzaLiveStreamParams defaultParams,
                                     com.codename1.util.OnComplete<java.lang.String> onComplete,
                                     java.lang.Runnable onFail)
(Async) Get a stopped stream from your pool and manage your pool accoring to the provided startingSize and threshold.

About the performance, this methods performs sequential REST requests until it finds a stopped stream, so it can require some seconds before launching the onComplete callback.

Parameters:
startingSize - of your pool: if your pool size is less than your startingSize, then this method adds a new stream to the pool
threshold - is a percentage of active streams: when the threshold is exceeded, a new stream is always added, regardless of the startingSize value
defaultParams - can be used to customize the parameters (except the name) of the new stream that could be created, it can be null to use the default parameters
onComplete - callback invoked on success, the passed value is the id of a stopped stream of the pool
onFail - callback invoked on failure
```

Remember that in trial mode you cannot have more than three active streams.

#### Example 1: empty pool (no stream)

```
WowzaLiveStream.getStreamFromPool(3, 60, null, streamId -> {
    Log.p("Stopped stream id provided by getStreamFromPool: " + streamId);
}, () -> Log.p("Failed to get a stopped streaming", Log.WARNING));
```

logs:
```
[EDT] 0:0:1,546 - WowzaLiveStream.fetchAllLiveStreams -> (Code 200) Successfully get all streams for the current account
[EDT] 0:0:1,546 - WowzaLiveStream.getStreamFromPool -> Pool size: 0
[EDT] 0:0:1,612 - WowzaLiveStream.getStreamFromPool -> The stream with id null is not stopped.
[EDT] 0:0:2,641 - WowzaLiveStream.create -> (Code 201) Successfully created live stream with name Stream-1
[EDT] 0:0:2,641 - WowzaLiveStream.getStreamFromPool -> Created new stream with name Stream-1 and id qf0qzpds, because there are no stopped streams
[EDT] 0:0:2,641 - Stopped stream id provided by getStreamFromPool: qf0qzpds
```

#### Example 2: poolSize < startingSize

```
WowzaLiveStream.getStreamFromPool(3, 60, null, streamId -> {
    Log.p("Stopped stream id provided by getStreamFromPool: " + streamId);
}, () -> Log.p("Failed to get a stopped streaming", Log.WARNING));
```

logs:
```
[EDT] 0:0:1,563 - WowzaLiveStream.fetchAllLiveStreams -> (Code 200) Successfully get all streams for the current account
[EDT] 0:0:1,563 - WowzaLiveStream.getStreamFromPool -> Pool size: 1
[EDT] 0:0:1,910 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state stopped for the stream with id qf0qzpds
[EDT] 0:0:1,911 - WowzaLiveStream.getStreamFromPool -> Found the stopped stream: qf0qzpds, which index is 0 (starting from 0) of a pool of 1 streams
[EDT] 0:0:1,911 - Stopped stream id provided by getStreamFromPool: qf0qzpds
[EDT] 0:0:2,834 - WowzaLiveStream.create -> (Code 201) Successfully created live stream with name Stream-2
[EDT] 0:0:2,834 - WowzaLiveStream.getStreamFromPool -> Created new stream with name Stream-2 and id hxnyskfm, because poolSize < startingSize (1 < 3)
```

#### Example 3: poolSize >= startingSize && usedStream % > threshold %

```
WowzaLiveStream.getStreamFromPool(2, 40, null, streamId -> {
    Log.p("Stopped stream id provided by getStreamFromPool: " + streamId);
}, () -> Log.p("Failed to get a stopped streaming", Log.WARNING));
```

logs:
```
[EDT] 0:0:1,547 - WowzaLiveStream.fetchAllLiveStreams -> (Code 200) Successfully get all streams for the current account
[EDT] 0:0:1,547 - WowzaLiveStream.getStreamFromPool -> Pool size: 2
[EDT] 0:0:1,903 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state started for the stream with id qf0qzpds
[EDT] 0:0:1,903 - WowzaLiveStream.getStreamFromPool -> The stream with id qf0qzpds is not stopped.
[EDT] 0:0:2,195 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state stopped for the stream with id hxnyskfm
[EDT] 0:0:2,195 - WowzaLiveStream.getStreamFromPool -> Found the stopped stream: hxnyskfm, which index is 1 (starting from 0) of a pool of 2 streams
[EDT] 0:0:2,197 - Stopped stream id provided by getStreamFromPool: hxnyskfm
[EDT] 0:0:3,332 - WowzaLiveStream.create -> (Code 201) Successfully created live stream with name Stream-3
[EDT] 0:0:3,332 - WowzaLiveStream.getStreamFromPool -> Created new stream with name Stream-3 and id 2m1ky74p, because 50% of usedStreams > threshold (40%)
```

#### Example 4: all streams of the pool are used

```
WowzaLiveStream.getStreamFromPool(2, 40, null, streamId -> {
    Log.p("Stopped stream id provided by getStreamFromPool: " + streamId);
}, () -> Log.p("Failed to get a stopped streaming", Log.WARNING));
```

logs:
```
[EDT] 0:0:1,470 - WowzaLiveStream.fetchAllLiveStreams -> (Code 200) Successfully get all streams for the current account
[EDT] 0:0:1,470 - WowzaLiveStream.getStreamFromPool -> Pool size: 3
[EDT] 0:0:1,752 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state started for the stream with id qf0qzpds
[EDT] 0:0:1,755 - WowzaLiveStream.getStreamFromPool -> The stream with id qf0qzpds is not stopped.
[EDT] 0:0:1,965 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state started for the stream with id hxnyskfm
[EDT] 0:0:1,966 - WowzaLiveStream.getStreamFromPool -> The stream with id hxnyskfm is not stopped.
[EDT] 0:0:2,266 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state starting for the stream with id 2m1ky74p
[EDT] 0:0:2,266 - WowzaLiveStream.getStreamFromPool -> The stream with id 2m1ky74p is not stopped.
[EDT] 0:0:3,861 - WowzaLiveStream.create -> (Code 201) Successfully created live stream with name Stream-4
[EDT] 0:0:3,861 - WowzaLiveStream.getStreamFromPool -> Created new stream with name Stream-4 and id dvv0vykn, because there are no stopped streams
[EDT] 0:0:3,862 - Stopped stream id provided by getStreamFromPool: dvv0vykn
```

#### Example 5: poolSize >= startingSize && usedStream % < threshold %
```
WowzaLiveStream.getStreamFromPool(2, 40, null, streamId -> {
    Log.p("Stopped stream id provided by getStreamFromPool: " + streamId);
}, () -> Log.p("Failed to get a stopped streaming", Log.WARNING));
```

logs:
```
[EDT] 0:0:1,635 - WowzaLiveStream.fetchAllLiveStreams -> (Code 200) Successfully get all streams for the current account
[EDT] 0:0:1,635 - WowzaLiveStream.getStreamFromPool -> Pool size: 4
[EDT] 0:0:2,5 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state started for the stream with id qf0qzpds
[EDT] 0:0:2,5 - WowzaLiveStream.getStreamFromPool -> The stream with id qf0qzpds is not stopped.
[EDT] 0:0:2,311 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state stopped for the stream with id hxnyskfm
[EDT] 0:0:2,311 - WowzaLiveStream.getStreamFromPool -> Found the stopped stream: hxnyskfm, which index is 1 (starting from 0) of a pool of 4 streams
[EDT] 0:0:2,311 - Stopped stream id provided by getStreamFromPool: hxnyskfm
```

#### Additional info about the use of pool
The method `getStreamFromPool` of the class `WowzaLiveStream`, used in the previous examples, relies on the fact that you use only that method to get the first not used stream in the pool. Its logic is based on some assumptions and its purpose is to provide a ready-to-use method that implements what suggested in the Wowza documentation. Of course you can implement a different logic on client side or server side, if you wish it.

### Start the stream
After getting the first unused stream of the pool, you must start it and wait that the starting process completes (it can take minutes). The method `start` of the class `WowzaLiveStream` simplify all the process. Its javadoc is:
```
public static void start(java.lang.String id,
                         com.codename1.util.OnComplete<java.lang.String> isStarted,
                         java.lang.Runnable onFail,
                         java.lang.Integer timeout)
(Async) Starts a stream and wait that the starting is completed before calling the callback "isStarted".

After a default timeout of 120 seconds, if the starting is not completed then the onFail callback is called. You can customize the timeout.

Parameters:
id - of the stream
isStarted - is a callback called when the starting is completed
onFail - is a callback for failure
timeout - (seconds) null to use the default timeout, or any other value >= 30 to set a custom timeout.
```

Example of usage:
```
WowzaLiveStream.getStreamFromPool(3, 80, null, streamId -> {
    Log.p("Stopped stream id provided by getStreamFromPool: " + streamId);
    WowzaLiveStream.start(streamId, id -> {
Log.p("WowzaTest -> Successfully started the stream with id " + id, Log.INFO);
    }, () -> {
Log.p("WowzaTest -> Failed to start the stream with id " + streamId, Log.WARNING);
    }, null);
}, () -> Log.p("WowzaTest -> Failed to get a stopped streaming", Log.WARNING));
```

Normal log (successfull starting):
```
[EDT] 0:0:1,935 - Stopped stream id provided by getStreamFromPool: qf0qzpds
[EDT] 0:0:20,556 - WowzaTest -> Successfully started the stream with id qf0qzpds
```

Verbose log (successfull starting):
```
[EDT] 0:0:1,411 - WowzaLiveStream.fetchAllLiveStreams -> (Code 200) Successfully get all streams for the current account
[EDT] 0:0:1,411 - WowzaLiveStream.getStreamFromPool -> Pool size: 4
[EDT] 0:0:1,761 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state started for the stream with id qf0qzpds
[EDT] 0:0:1,761 - WowzaLiveStream.getStreamFromPool -> The stream with id qf0qzpds is not stopped.
[EDT] 0:0:2,89 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state stopped for the stream with id hxnyskfm
[EDT] 0:0:2,90 - WowzaLiveStream.getStreamFromPool -> Found the stopped stream: hxnyskfm, which index is 1 (starting from 0) of a pool of 4 streams
[EDT] 0:0:2,90 - Stopped stream id provided by getStreamFromPool: hxnyskfm
[EDT] 0:0:2,441 - WowzaLiveStream.start -> (Code 200) Successfully get the state starting for the stream with id hxnyskfm
[EDT] 0:0:2,682 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state starting for the stream with id hxnyskfm
[EDT] 0:0:4,943 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state starting for the stream with id hxnyskfm
[EDT] 0:0:7,181 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state starting for the stream with id hxnyskfm
[EDT] 0:0:9,459 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state starting for the stream with id hxnyskfm
[EDT] 0:0:11,688 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state starting for the stream with id hxnyskfm
[EDT] 0:0:15,301 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state starting for the stream with id hxnyskfm
[EDT] 0:0:17,540 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state starting for the stream with id hxnyskfm
[EDT] 0:0:19,805 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state starting for the stream with id hxnyskfm
[EDT] 0:0:22,45 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state started for the stream with id hxnyskfm
[EDT] 0:0:22,45 - WowzaLiveStream.start -> Successfully started the stream with id hxnyskfm after 19 seconds
[EDT] 0:0:22,45 - WowzaTest -> Successfully started the stream with id hxnyskfm
```

Verbose log (failure because we try to exceed the trial mode limit of maximum three started streams)
```
[EDT] 0:0:1,416 - WowzaLiveStream.fetchAllLiveStreams -> (Code 200) Successfully get all streams for the current account
[EDT] 0:0:1,416 - WowzaLiveStream.getStreamFromPool -> Pool size: 4
[EDT] 0:0:1,775 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state started for the stream with id qf0qzpds
[EDT] 0:0:1,775 - WowzaLiveStream.getStreamFromPool -> The stream with id qf0qzpds is not stopped.
[EDT] 0:0:2,78 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state started for the stream with id hxnyskfm
[EDT] 0:0:2,78 - WowzaLiveStream.getStreamFromPool -> The stream with id hxnyskfm is not stopped.
[EDT] 0:0:2,389 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state started for the stream with id 2m1ky74p
[EDT] 0:0:2,390 - WowzaLiveStream.getStreamFromPool -> The stream with id 2m1ky74p is not stopped.
[EDT] 0:0:2,772 - WowzaLiveStream.fetchState -> (Code 200) Successfully get the state stopped for the stream with id dvv0vykn
[EDT] 0:0:2,773 - WowzaLiveStream.getStreamFromPool -> Found the stopped stream: dvv0vykn, which index is 3 (starting from 0) of a pool of 4 streams
[EDT] 0:0:2,773 - Stopped stream id provided by getStreamFromPool: dvv0vykn
[EDT] 0:0:2,999 - WowzaLiveStream.start -> Unknow response with code 422, failed to get the state of the stream dvv0vykn, string returned by the server: "{"meta":{"status":422,"code":"ERR-422-NoPaygTranscodedTranscodersRemaining","title":"No Payg Transcoded Transcoders Remaining Error","message":"You've reached the maximum number of transcoded (ABR) transcoders that may be started at one time during a trial.","description":""}}"
[EDT] 0:0:3,0 - WowzaTest -> Failed to start the stream with id dvv0vykn
```

Verbose log (*fake* example of how a timeout error can appear)
```
[Timer-5] 0:0:53,443 - WowzaLiveStream.start -> Timeout reached while starting the stream with id qf0qzpds
[Timer-5] 0:0:53,444 - WowzaTest -> Failed to start the stream with id qf0qzpds
```

### Start, stop, play the broadcasting

Finally, we are ready to start the broadcasting (and recording) of an event. The `GoCoder` class contains the method to start, stop and play the broadcasting. See the full example below. Currently the method to retreive the recorded events is not yet implemented.


# Full working example (iOS or Android app)

To run this example, import this CN1Lib and enable the Codename One CSS support. Remember to lock the screen orientation (https://stackoverflow.com/a/48717607).

CSS code:
```
#Constants {
    includeNativeBool: true; 
    landscapeTitleUiidBool: true;
}

Default {
    font-family: "native:MainRegular";
    font-size: 3mm;
    color: black;
    background-color: rgba(255,255,255,0.5);
}

Button, Label {
    margin: 1mm;
    padding: 1mm;
}

Label {
    color: black;
}

Button {
    color: black;
}

Button.pressed, Button.selected {
    color: red;
}

Button.disabled {
    color: gray;
}

BottomButtons {
    margin-bottom: 5mm;
}

StatusBarLandscape, ToolbarLandscape, TitleCommandLandscape, BackCommandLandscape, TitleLandscape  {
    margin: 0px;
    padding: 0px;
}

PeerComponent-Cnt {
    background-color: black;
}
```

Java code:
```
/**
 * Example of use of the "Wowza live events streaming CN1Lib"
 */
package net.informaticalibera.test.wowzatrial;

import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import net.informaticalibera.cn1libs.wowza.WowzaLiveStream;
import net.informaticalibera.cn1libs.wowza.WowzaAccount;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Form;
import com.codename1.ui.Dialog;
import com.codename1.ui.Label;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import com.codename1.io.Log;
import com.codename1.ui.Toolbar;
import com.codename1.ui.Button;
import com.codename1.ui.CN;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.util.UITimer;
import net.informaticalibera.cn1libs.wowza.GoCoder;
import net.informaticalibera.cn1libs.wowza.GoCoderBroadcastConfig;
import net.informaticalibera.cn1libs.wowza.Utilities;
import net.informaticalibera.cn1libs.wowza.WowzaLiveStreamParams;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename
 * One</a> for the purpose of building native mobile applications using Java.
 */
public class MyApplication {

    private Form current;
    private Resources theme;
    private String currentStreamId = null;

    public void init(Object context) {
        // use two network threads instead of one
        updateNetworkThreadCount(2);

        theme = UIManager.initFirstTheme("/theme");

        // Enable Toolbar on all Forms by default
        Toolbar.setGlobalToolbar(true);

        // Pro only feature
        Log.bindCrashProtection(true);

        addNetworkErrorListener(err -> {
            // prevent the event from propagating
            err.consume();
            if (err.getError() != null) {
                Log.e(err.getError());
            }
            Log.sendLogAsync();
            Dialog.show("Connection Error", "There was a networking error in the connection to " + err.getConnectionRequest().getUrl(), "OK", null);
        });

        // Enable verbose logging (which log level is "debug")
        WowzaAccount.setVerboseLog(true);

        // Register the Wowza keys
        WowzaAccount.setApiKey_iOS("GOSK-0F47-010C-9585-C2FC-7E20");
        WowzaAccount.setApiKey_Android("GOSK-FE46-010C-E1D2-F6DA-1DCB");
        // xor encoded: 1udfMaUUqioU6XwECtsy26k3PUXgCkX4X8LS1QA4UCTtekLwDNPC4gfP060z320e
        // https://www.codenameone.com/demos/StringEncoder/index.html
        WowzaAccount.setRestKey("MHdnYkhnUl14Y2RZO1Z4VVJmYG0nIHwrSU9De151RxR5Gm93FHdmHHxpf1hIRWNHdXxjdwFRUWgJDAtGDgwPJQ==");
        WowzaAccount.setAccessKey("6Z1wCRpb2gpgdSQgn3pKILcPiGRgywj6Pq4MKjGzy3ajzrQFyKrwVQQeajwa3662");
    }

    public void start() {
        if (current != null) {
            current.show();
            return;
        }

        Display.getInstance().lockOrientation(false);
        int[] time = {0};
        GoCoderBroadcastConfig[] lastConfig = {null};

        GoCoder.setQuality(GoCoder.MEDIUM_QUALITY_720p); // not necessary, it defaults to medium quality
        WowzaLiveStream.setHmacAuthentication(false); // not necessary, it defaults to false

        Form startForm = new Form("Wowza Test", BoxLayout.yCenter());
        Button broadcastStreaming = new Button("Broadcast a stream");
        Button playStreaming = new Button("Play a stream");
        startForm.add(FlowLayout.encloseCenterMiddle(broadcastStreaming, playStreaming));
        startForm.show();

        broadcastStreaming.addActionListener(l -> {

            Form wowzaBroadcast = new Form("Broadcast", new BorderLayout(BorderLayout.CENTER_BEHAVIOR_TOTAL_BELOW));
            hideToolbar(wowzaBroadcast);
            Button startStreaming = new Button("Start");
            Button stopStreaming = new Button("Stop");
            stopStreaming.setEnabled(false);
            wowzaBroadcast.add(BorderLayout.CENTER, checkNotNull(GoCoder.getCameraView()));
            Label streamCode = new Label(" ");
            Container buttons = GridLayout.encloseIn(3, startStreaming, stopStreaming, streamCode);
            buttons.setUIID("BottomButtons");
            wowzaBroadcast.add(BorderLayout.SOUTH, buttons);
            Label chrono = new Label(" ");
            wowzaBroadcast.add(BorderLayout.NORTH, chrono);

            wowzaBroadcast.show();

            UITimer.timer(1000, true, wowzaBroadcast, () -> {
                if (time[0] == 0) {
                    chrono.setText("STOPPED - NO BROADCASTING");
                } else {
                    chrono.setText("BROADCASTING IS RUNNING -> " + time[0] + " seconds");
                }
            });

            startStreaming.addActionListener(ll -> {
                startStreaming.setEnabled(false);
                ToastBar.Status[] status = {ToastBar.showMessage("Searching for an available stream, please wait...", FontImage.MATERIAL_INFO, 120000)};
                WowzaLiveStreamParams params = new WowzaLiveStreamParams();
                WowzaLiveStream.getStreamFromPool(3, 80, null, streamId -> {
                    status[0].clear();
                    Log.p("Stopped stream id provided by getStreamFromPool: " + streamId, Log.INFO);
                    status[0] = ToastBar.showMessage("Starting the new stream, please wait...", FontImage.MATERIAL_INFO, 120000);
                    WowzaLiveStream.start(streamId, id -> {
                        status[0].clear();
                        Log.p("WowzaTest -> Successfully started the stream with id " + id, Log.INFO);
                        ToastBar.showMessage("Successfully started the stream", FontImage.MATERIAL_INFO, 5000);
                        streamCode.setText(streamId);
                        streamCode.getParent().revalidate();
                        setCurrentStreamId(id);
                        // start the streaming sending it with GoCoder SDK
                        Log.p("WowzaTest -> Starting GoCoder SDK", Log.INFO);
                        GoCoderBroadcastConfig.getInstance(id, config -> {
                            Log.p("WowzaTest -> Successfully got the configuration info for the stream with id " + id, Log.INFO);
                            ToastBar.showMessage("Successfully got the configuration info for the stream", FontImage.MATERIAL_INFO, 5000);
                            lastConfig[0] = config;
                            GoCoder.startBroadcast(config);
                            UITimer.timer(10000, false, wowzaBroadcast, () -> {
                                stopStreaming.setEnabled(true);
                            });
                        }, () -> {
                            Log.p("WowzaTest -> Failed to get the configuration info for the stream with id " + id, Log.WARNING);
                            Log.sendLogAsync();
                            ToastBar.showMessage("Failed to get the configuration info for the stream", FontImage.MATERIAL_WARNING, 5000);
                            startStreaming.setEnabled(true);
                        });
                    }, () -> {
                        status[0].clear();
                        Log.p("WowzaTest -> Failed to start the stream with id " + streamId, Log.WARNING);
                        Log.sendLogAsync();
                        ToastBar.showMessage("Failed to start the stream", FontImage.MATERIAL_WARNING, 5000);
                        startStreaming.setEnabled(true);
                    }, null);
                }, () -> {
                    Log.p("WowzaTest -> Failed to get a stopped streaming", Log.WARNING);
                    Log.sendLogAsync();
                    ToastBar.showMessage("Failed to get an available stream", FontImage.MATERIAL_WARNING, 5000);
                    startStreaming.setEnabled(true);
                });
            });

            stopStreaming.addActionListener(ll -> {
                stopStreaming.setEnabled(false);
                UITimer.timer(2000, false, wowzaBroadcast, () -> {
                    startStreaming.setEnabled(true);
                });
                String id = getCurrentStreamId();
                if (id != null) {
                    // we stop GoCoder SDK
                    Log.p("WowzaTest -> Stopping GoCoder SDK", Log.INFO);
                    GoCoder.stopBroadcast();
                    // now we can stop the stream to avoid extra billing
                    WowzaLiveStream.stop(id, myId -> {
                        Log.p("WowzaTest -> Successfully stopped the stream with id " + myId, Log.INFO);
                        startForm.showBack();
                        ToastBar.showMessage("Successfully stopped the stream", FontImage.MATERIAL_INFO, 5000);
                        setCurrentStreamId(null);
                    }, () -> {
                        Log.p("WowzaTest -> Failed to stop the stream with id " + id, Log.WARNING);
                        Log.sendLogAsync();
                        startForm.showBack();
                        ToastBar.showMessage("Failed to stop the stream", FontImage.MATERIAL_WARNING, 5000);
                    });
                } else {
                    startForm.showBack();
                }
            });

            String[] status = {""};
            if (Utilities.isAndroidNative() || Utilities.isiOSNative()) {
                // we check every second if there is a change of state, we log only new states
                UITimer.timer(1000, true, wowzaBroadcast, () -> {

                    String newStatus = GoCoder.getBroadcastStatus();
                    if ("running".equals(newStatus)) {
                        time[0] = time[0] + 1;

                        // if GoCoder broadcast status is "running", then the WowzaCloud stream connection should be "fine", otherwise we can try to reconnect
                        String id = getCurrentStreamId();
                        if (time[0] > 1 && time[0] % 10 == 0) {
                            // we do this check every ten seconds
                            WowzaLiveStream.fetchConnectionState(id, streamId -> {
                                // is fine, nothing to do
                            }, streamId -> {
                                // is not fine
                                time[0] = 0;
                                GoCoder.stopBroadcast();
                                Utilities.showReconnectingToastBar();
                                UITimer.timer(10000, false, wowzaBroadcast, () -> {
                                    if (lastConfig[0] != null) {
                                        GoCoder.startBroadcast(lastConfig[0]);
                                    }
                                });
                            }, () -> {
                                // failure
                                time[0] = 0;
                                GoCoder.stopBroadcast();
                                WowzaLiveStream.stop(id, myId -> {
                                }, () -> {
                                });
                                ToastBar.showMessage("The streaming is stopped because a network issue.", FontImage.MATERIAL_ERROR, 5000);
                            });
                        }
                    } else {
                        time[0] = 0;
                    }
                    if (newStatus != null && !newStatus.equals("null") && !status[0].equals(newStatus)) {
                        status[0] = newStatus;
                        Log.p("WowzaTest -> GoCoder status is: " + status[0], Log.INFO);
                        if ("running".equals(status[0])) {
                            ToastBar.showMessage("Broadcasting is " + status[0], FontImage.MATERIAL_INFO, 5000);
                        }
                    }
                });
            }
        });

        playStreaming.addActionListener(l -> {
            Form wowzaPlayer = new Form("Broadcast", new BorderLayout(BorderLayout.CENTER_BEHAVIOR_TOTAL_BELOW));
            wowzaPlayer.getToolbar().hideToolbar();
            TextField idTextField = new TextField(20);
            idTextField.setEditable(true);
            idTextField.setConstraint(TextArea.SENSITIVE);
            idTextField.setHint("Enter the stream id to play");
            Button playButton = new Button("Play");
            Button backButton = new Button("Back");
            wowzaPlayer.add(BorderLayout.SOUTH, BoxLayout.encloseY(FlowLayout.encloseCenter(idTextField), GridLayout.encloseIn(2, playButton, backButton)));
            wowzaPlayer.show();

            backButton.addActionListener(ll -> {
                GoCoder.stopPlayer();
                startForm.showBack();
            });
            
            idTextField.addCloseListener(ll -> {
                playButton.pressed();
                playButton.released();
            });            

            playButton.addActionListener(ll -> {
                idTextField.stopEditing();
                String id = idTextField.getText().toLowerCase();
                if (id.length() != 8) {
                    ToastBar.showMessage("The id your entered is not valid", FontImage.MATERIAL_WARNING, 5000);
                    return;
                }
                GoCoderBroadcastConfig.getInstance(id, config -> {
                    Log.p("WowzaTest -> Successfully got the configuration info for the stream with id " + id, Log.INFO);
                    ToastBar.showMessage("Successfully got the configuration info for the stream", FontImage.MATERIAL_INFO, 5000);
                    wowzaPlayer.addComponent(0, BorderLayout.CENTER, checkNotNull(GoCoder.getPlayerView(config)));
                    wowzaPlayer.revalidate();
                }, () -> {
                    Log.p("WowzaTest -> Failed to get the configuration info for the stream with id " + id, Log.WARNING);
                    Log.sendLogAsync();
                    ToastBar.showMessage("Failed to get the configuration info for the stream", FontImage.MATERIAL_WARNING, 5000);
                });
            });
        });

    }

    public String getCurrentStreamId() {
        return currentStreamId;
    }

    public void setCurrentStreamId(String currentStreamId) {
        this.currentStreamId = currentStreamId;
    }

    public void stop() {
        current = getCurrentForm();
        if (current instanceof Dialog) {
            ((Dialog) current).dispose();
            current = getCurrentForm();
        }
    }

    public void destroy() {
    }

    /**
     * Checks that the given PeerComponent is not null; it assumes that the
     * PeerComponent ratio is 16:9 and that it takes all available screen space,
     * so it encloses the PeerComponent in a Container that makes it centered.
     *
     * @param cameraView
     * @return
     */
    private Component checkNotNull(Component cameraView) {
        if (cameraView != null) {
            Dimension preferredSize;
            if (CN.isPortrait()) {
                int width = CN.getDisplayWidth();
                int height = width * 16 / 9;
                preferredSize = new Dimension(width, height);
            } else {
                int height = CN.getDisplayHeight();
                int width = height * 16 / 9;
                preferredSize = new Dimension(width, height);
            }
            cameraView.setPreferredSize(preferredSize);
            Container cnt = FlowLayout.encloseCenterMiddle(cameraView);
            cnt.setUIID("PeerComponent-Cnt");
            return cnt;
        } else {
            return FlowLayout.encloseCenterMiddle(new SpanLabel("Error in getting the camera view"));
        }
    }

    /**
     * Hides the Toolbar, preserving the status bar space on non-Android themes;
     * if you want to remove the status bar space on landscape only, you need a
     * theme constant plus the override of one or more UIIDs, for an example
     * see: https://gist.github.com/jsfan3/c5b885a9e1e51cf5d8d8b5c368334242
     *
     * @param form
     */
    public static void hideToolbar(Form form) {
        boolean isAndroidTheme = UIManager.getInstance().isThemeConstant("textComponentOnTopBool", false);
        if (isAndroidTheme) {
            form.getToolbar().setHidden(true, true);
        } else {
            // this preserves the status bar space only
            form.getToolbar().setVisible(false);
            form.getToolbar().getTitleComponent().setHidden(true, true);
        }
    }

}
```

# License
The software that I personally wrote to create this CN1Lib is public-domain software, but the included Wowza GoCoder SDKs have proprietary licenses (EULAs) [reported here](https://www.wowza.com/legal/gocoder-sdk) (note that if you are using a trial account, there is a [separate license](https://www.wowza.com/legal/gocoder-sdk-trial) that prevents the use of Wowza services in a production app).

In [Wowza FAQ](https://www.wowza.com/docs/wowza-gocoder-sdk-faq) it's written: _«[...] other mobile frameworks can be used with GoCoder SDK to develop live streaming apps. Currently, Wowza doesn't offer plug-ins for these frameworks or platforms, but we encourage developers to create plug-ins for their needs. [...]»._ So this CN1Lib can be considered a [fair use](https://en.wikipedia.org/wiki/Fair_use) of the Wowza SDKs.

Moreover, you are forced to accept the Wowza legal contracts and Wowza fees to get the API Keys required to use this CN1Lib.

This CN1Lib also includes the [Apache Commons Codec 1.13](https://commons.apache.org/proper/commons-codec/download_codec.cgi), which license is Apache 2.0.
