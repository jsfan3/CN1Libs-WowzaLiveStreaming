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
package net.informaticalibera.cn1libs.wowza;

/**
 * Register the Wowza GoCoder SDK API keys
 *
 * @author Francesco Galgani 
 */
public class WowzaAccount {
    private static String apiKey_iOS = null;
    private static String apiKey_Android = null;
    private static String restKey = null;
    private static String accessKey = null;
    private static boolean verboseLog = false;

    static String getApiKey_iOS() {
        return apiKey_iOS;
    }

    public static void setApiKey_iOS(String apiKey_iOS) {
        WowzaAccount.apiKey_iOS = apiKey_iOS;
    }

    static String getApiKey_Android() {
        return apiKey_Android;
    }

    public static void setApiKey_Android(String apiKey_Android) {
        WowzaAccount.apiKey_Android = apiKey_Android;
    }

    static String getRestKey() {
        return restKey;
    }

    public static void setRestKey(String restKey) {
        WowzaAccount.restKey = restKey;
    }

    static String getAccessKey() {
        return accessKey;
    }

    public static void setAccessKey(String accessKey) {
        WowzaAccount.accessKey = accessKey;
    }

    public static void setVerboseLog(boolean verboseLog) {
        WowzaAccount.verboseLog = verboseLog;
    }

    static boolean isVerboseLog() {
        return verboseLog;
    }
    
    

    
    
}
