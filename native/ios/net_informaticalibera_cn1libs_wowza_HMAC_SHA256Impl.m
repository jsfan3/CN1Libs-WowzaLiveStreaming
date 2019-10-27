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

#import "net_informaticalibera_cn1libs_wowza_HMAC_SHA256Impl.h"
#import <CommonCrypto/CommonHMAC.h>

@implementation net_informaticalibera_cn1libs_wowza_HMAC_SHA256Impl

-(NSString*)HMAC_sha256:(NSString*)param param1:(NSString*)param1{

    NSString* key = param;
    NSString* data = param1;

    const char *cKey = [key cStringUsingEncoding:NSASCIIStringEncoding];
    const char *cData = [data cStringUsingEncoding:NSASCIIStringEncoding];
    unsigned char cHMAC[CC_SHA256_DIGEST_LENGTH];
    CCHmac(kCCHmacAlgSHA256, cKey, strlen(cKey), cData, strlen(cData), cHMAC);
    NSData *hash = [[NSData alloc] initWithBytes:cHMAC length:sizeof(cHMAC)];
    NSString* s = [net_informaticalibera_cn1libs_wowza_HMAC_SHA256Impl dataToHexString:hash];
    return s;
}

+ (NSString*)dataToHexString:(NSData*)theData {
    // thanks to: https://stackoverflow.com/a/14248343

    NSUInteger          len = [theData length];
    char *              chars = (char *)[theData bytes];
    NSMutableString *   hexString = [[NSMutableString alloc] init];

    for(NSUInteger i = 0; i < len; i++ )
        [hexString appendString:[NSString stringWithFormat:@"%0.2hhx", chars[i]]];

    return hexString;
}

-(BOOL)isSupported{
    return YES;
}

@end
