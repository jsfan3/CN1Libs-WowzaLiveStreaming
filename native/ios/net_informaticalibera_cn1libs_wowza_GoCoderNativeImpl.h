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

#import <Foundation/Foundation.h>

@interface net_informaticalibera_cn1libs_wowza_GoCoderNativeImpl : NSObject {
    UIView* container;
}

-(void*)getCameraView:(NSString*)param;
-(void)stopPlayer;
-(void*)getPlayerView:(NSString*)param param1:(NSString*)param1 param2:(int)param2 param3:(NSString*)param3 param4:(NSString*)param4 param5:(NSString*)param5 param6:(NSString*)param6;
-(void)startBroadcast:(NSString*)param param1:(int)param1 param2:(NSString*)param2 param3:(NSString*)param3 param4:(NSString*)param4 param5:(NSString*)param5;
-(NSString*)getBroadcastStatus;
-(void)stopBroadcast;
-(BOOL)isSupported;
@end
