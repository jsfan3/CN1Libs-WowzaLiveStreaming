/**
 * Wowza live events streaming CN1Lib
 * Written in 2019 by Francesco Galgani, https://www.informatica-libera.net/
 *
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * You should have received a copy of the CC0 Public Domain Dedication along
 * with this software. If not, see
 * <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

#import "net_informaticalibera_cn1libs_wowza_GoCoderNativeImpl.h"
#import <WowzaGoCoderSDK/WowzaGoCoderSDK.h>
#import "CodenameOne_GLViewController.h"
#import "cn1_globals.h"
#import "com_codename1_io_Log.h"
#import "com_codename1_ui_CN.h"
#import "net_informaticalibera_cn1libs_wowza_GoCoder.h"

@interface net_informaticalibera_cn1libs_wowza_GoCoderNativeImpl () <WOWZBroadcastStatusCallback, WOWZPlayerStatusCallback>

// The top-level GoCoder API interface
@property (nonatomic, strong) WowzaGoCoder *goCoder;

// Applications use the top-level WowzaConfig class to configure various GoCoder SDK components
@property (nonatomic, strong) WowzaConfig *goCoderMediaConfig;

// Wowza GoCoder Player
@property (nonatomic, strong) WOWZPlayer *player;

// Wowza GoCoder Broadcaster status
@property (nonatomic, strong) NSString *status;

@end

@implementation net_informaticalibera_cn1libs_wowza_GoCoderNativeImpl

BOOL startRequested = NO;

-(void*)getCameraView:(NSString*)param{
    
    __block UIView* result = nil;
    NSString* key = param;
    
    dispatch_sync(dispatch_get_main_queue(), ^{
        
        UIView *topView = [[[[UIApplication sharedApplication] keyWindow] subviews] lastObject];
        int maxWidth = topView.bounds.size.width;
        int maxHeight = topView.bounds.size.height;
        int width = maxWidth;
        int height = maxHeight;
        if (com_codename1_ui_CN_isPortrait___R_boolean(CN1_THREAD_GET_STATE_PASS_SINGLE_ARG)) {
            height = width * 16 / 9;
        } else {
            width = height * 16 / 9;
        }
        
        if (container == nil) {
            //container = [[UIView alloc] initWithFrame:topView.bounds];
            container = [[UIView alloc] initWithFrame:CGRectMake(0, 0, width, height)];
            [container setBackgroundColor:(UIColor.blackColor)];
        }
        
        if (self.goCoder == nil) {
            // Register the GoCoder SDK license key
            NSError *goCoderLicensingError = [WowzaGoCoder registerLicenseKey:key];
            if (goCoderLicensingError != nil) {
                // Log license key registration failure
                com_codename1_io_Log_p___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG [goCoderLicensingError localizedDescription]));
                com_codename1_io_Log_sendLogAsync__(CN1_THREAD_GET_STATE_PASS_SINGLE_ARG);
            } else {
                // Initialize the GoCoder SDK
                self.goCoder = [WowzaGoCoder sharedInstance];
            }
        }
        
        
        if (self.goCoder != nil) {
            // Check the requested quality
            int quality = net_informaticalibera_cn1libs_wowza_GoCoder_getQuality___R_int(CN1_THREAD_GET_STATE_PASS_SINGLE_ARG);
            
            int lowQuality = get_static_net_informaticalibera_cn1libs_wowza_GoCoder_LOW_QUALITY_360p;
            int mediumQuality = get_static_net_informaticalibera_cn1libs_wowza_GoCoder_MEDIUM_QUALITY_720p;
            int highQuality = get_static_net_informaticalibera_cn1libs_wowza_GoCoder_HIGH_QUALITY_1080p;
            
            if (lowQuality == quality) {
                self.goCoderMediaConfig = [[WowzaConfig alloc] initWithPreset:WOWZFrameSizePreset352x288];
                self.goCoderMediaConfig.videoWidth = 640;
                self.goCoderMediaConfig.videoHeight = 360;
            } else if (mediumQuality == quality) {
                self.goCoderMediaConfig = [[WowzaConfig alloc] initWithPreset:WOWZFrameSizePreset1280x720];
            } else if (highQuality == quality) {
                self.goCoderMediaConfig = [[WowzaConfig alloc] initWithPreset:WOWZFrameSizePreset1920x1080];
            } else {
                self.goCoderMediaConfig = [[WowzaConfig alloc] initWithPreset:WOWZFrameSizePreset1280x720];
            }
            
            // Associate the U/I view with the SDK camera preview
            self.goCoder.cameraView = container;
            [self.goCoder.cameraPreview initWithViewAndConfig:container config:self.goCoderMediaConfig];
            
            // Start the camera preview
            [self.goCoder.cameraPreview startPreview];
            
            result = container;
        }
        
    });
    return result;
}

-(void*)getPlayerView:(NSString*)param param1:(NSString*)param1 param2:(int)param2 param3:(NSString*)param3 param4:(NSString*)param4 param5:(NSString*)param5 param6:(NSString*)param6{
    
    NSString* key = param;
    NSString* hostAddress = param1;
    int portNumber = param2;
    NSString* applicationName = param3;
    NSString* streamName = param4;
    NSString* username = param5;
    NSString* password = param6;
    
    __block UIView* result = nil;
    
    dispatch_sync(dispatch_get_main_queue(), ^{
        
        WowzaConfig *config = [WowzaConfig new];
        config.hostAddress = hostAddress;
        config.portNumber = portNumber;
        config.streamName = streamName;
        config.applicationName = applicationName;
        config.audioEnabled = YES;
        config.videoEnabled = YES;
        self.goCoderMediaConfig = config;
        
        UIView *topView = [[[[UIApplication sharedApplication] keyWindow] subviews] lastObject];
        int maxWidth = topView.bounds.size.width;
        int maxHeight = topView.bounds.size.height;
        int width = maxWidth;
        int height = maxHeight;
        if (com_codename1_ui_CN_isPortrait___R_boolean(CN1_THREAD_GET_STATE_PASS_SINGLE_ARG)) {
            height = width * 16 / 9;
        } else {
            width = height * 16 / 9;
        }
        
        if (container == nil) {
            //container = [[UIView alloc] initWithFrame:topView.bounds];
            container = [[UIView alloc] initWithFrame:CGRectMake(0, 0, width, height)];
            [container setBackgroundColor:(UIColor.blackColor)];
        }
        
        if (self.goCoder == nil) {
            // Register the GoCoder SDK license key
            NSError *goCoderLicensingError = [WowzaGoCoder registerLicenseKey:key];
            if (goCoderLicensingError != nil) {
                // Log license key registration failure
                com_codename1_io_Log_p___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG [goCoderLicensingError localizedDescription]));
                com_codename1_io_Log_sendLogAsync__(CN1_THREAD_GET_STATE_PASS_SINGLE_ARG);
            } else {
                // Initialize the GoCoder SDK
                self.goCoder = [WowzaGoCoder sharedInstance];
            }
        }
        
        
        if (self.goCoder != nil) {
            
            self.player = [WOWZPlayer new];
            
            //Set default preroll buffer duration before the stream starts
            self.player.prerollDuration = 3; // 1-3 second buffer
            
            if ([self.player currentPlayState] == WOWZPlayerStateIdle) {
                [self.player play:self.goCoderMediaConfig callback:self];
            }

            result = container;
        }
        
    });
    
    return result;
}

-(void)startBroadcast:(NSString*)param param1:(int)param1 param2:(NSString*)param2 param3:(NSString*)param3 param4:(NSString*)param4 param5:(NSString*)param5{
    
    startRequested = YES;
    
    NSString* hostAddress = param;
    int portNumber = param1;
    NSString* applicationName = param2;
    NSString* streamName = param3;
    NSString* username = param4;
    NSString* password = param5;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        self.goCoderMediaConfig.hostAddress = hostAddress;
        self.goCoderMediaConfig.portNumber = portNumber;
        self.goCoderMediaConfig.applicationName = applicationName;
        self.goCoderMediaConfig.streamName = streamName;
        self.goCoderMediaConfig.username = username;
        self.goCoderMediaConfig.password = password;
        
        if (self.goCoder.status.state != WOWZBroadcastStateBroadcasting) {
            // Start streaming
            self.goCoder.config = self.goCoderMediaConfig;
            [self.goCoder startStreaming:self];
        }
    });
}

-(void)stopBroadcast{
    startRequested = NO;
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.goCoder.status.state != WOWZBroadcastStateIdle) {
            [self.goCoder endStreaming:self];
        }
    });
}

-(void)stopPlayer{
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.player currentPlayState] != WOWZPlayerStateIdle) {
            [self.player stop];
        }
    });
}

-(NSString*)getBroadcastStatus{
    return self.status;
}

-(BOOL)isSupported{
    return YES;
}

-(void)onWOWZStatus:(NSObject *) goCoderStatus {
    
    dispatch_async(dispatch_get_main_queue(), ^{
        
        if ([goCoderStatus isKindOfClass:[WOWZBroadcastStatus class]]) {
            switch (((WOWZBroadcastStatus*)goCoderStatus).state) {
                case WOWZBroadcastStateIdle:
                    self.status = @"stopped";
                    break;
                    
                case WOWZBroadcastStateReady:
                    self.status = @"starting";
                    break;
                    
                case WOWZBroadcastStateBroadcasting:
                    self.status = @"running";
                    break;
                    
                default:
                    self.status = nil;
            }
        } if ([goCoderStatus isKindOfClass:[WOWZPlayerStatus class]]) {
            switch (((WOWZPlayerStatus*)goCoderStatus).state) {
                case WOWZPlayerStateIdle:
                    self.status = @"stopped";
                    break;
                    
                case WOWZPlayerStateConnecting:
                    self.player.playerView = container;
                    self.status = @"starting";
                    break;
                    
                case WOWZPlayerStateBuffering:
                    self.status = @"starting";
                    break;
                    
                case WOWZPlayerStatePlaying:
                    self.status = @"running";
                    break;
                    
                case WOWZPlayerStateStopping:
                    self.status = @"stopped";
                    break;
                default:
                    self.status = nil;
                    break;
            }
        }
        
    });
    
}

-(void)onWOWZError:(NSObject *) goCoderStatus {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([goCoderStatus isKindOfClass:[WOWZBroadcastStatus class]]) {
            com_codename1_io_Log_p___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Broadcasting stream error:"));
            com_codename1_io_Log_p___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG self.goCoder.status.description));
            if (self.goCoder.status.state != WOWZBroadcastStateIdle) {
                [self.goCoder endStreaming:self];
            }
            
            self.status = nil;
            if (startRequested) {
                com_codename1_io_Log_p___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Trying to reconnect within 10 seconds..."));
                double delayInSeconds = 10.0;
                dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
                dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                    [self.goCoder startStreaming:self];
                });
            }
            
        } else if ([goCoderStatus isKindOfClass:[WOWZPlayerStatus class]]) {
            com_codename1_io_Log_p___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG @"Playing stream error:"));
            com_codename1_io_Log_p___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG self.goCoder.status.description));
            if ([self.player currentPlayState] != WOWZPlayerStateIdle) {
                [self.player stop];
            }
        }
    });
}


@end
