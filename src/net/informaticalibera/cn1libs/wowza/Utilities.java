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

import com.codename1.components.ToastBar;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.system.NativeLookup;
import com.codename1.ui.CN;
import com.codename1.ui.Display;
import com.codename1.util.StringUtil;
import com.sun.javafx.util.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities
 *
 * @author Francesco Galgani
 */
public class Utilities {

    private static HMAC_SHA256 hmac = NativeLookup.create(HMAC_SHA256.class);

    public static List<String> createList(String... items) {
        List<String> list = new ArrayList<>(items.length);
        for (String item : items) {
            list.add(item);
        }
        return list;
    }

    /**
     * <p>
     * Generates a signature string for a Wowza Stream Cloud API request.</p>
     * <p>
     * This code is adapted from
     * <a href="https://www.wowza.com/docs/how-to-use-the-wowza-streaming-cloud-rest-api#signature" target="noframe">Generate
     * and encrypt a signature for the request</a></p>
     *
     * @param requestPath value
     * @param apiKey value (xor encoded)
     * @param timeStamp value
     * @return the request signature
     */
    public static String generate_request_signature(String requestPath, String apiKey, long timeStamp) {
        // Make sure we only have the path.  No query parameters
        requestPath = StringUtil.tokenize(requestPath, '?').get(0);

        // Make sure there is a leading slash
        if (!requestPath.startsWith("/")) {
            requestPath = "/" + requestPath;
        }

        // Make sure there is not a trailing slash
        if (requestPath.endsWith("/")) {
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        }

        // Make the complete request string
        String data = timeStamp + ":" + requestPath + ":" + Util.xorDecode(apiKey);

        return hmac_sha256(Util.xorDecode(apiKey), data);
    }

    /**
     * HMAC-SHA256 generator. It gives the same output of
     * https://www.freeformatter.com/hmac-generator.html
     *
     * @param key is an unique piece of information that is used to compute the
     * HMAC and is known both by the sender and the receiver of the message.
     * @param message to be transmitted
     * @return a Hash-based message authentication code (HMAC)
     */
    public static String hmac_sha256(String key, String message) {
        if (hmac != null && hmac.isSupported()) {
            String code = hmac.HMAC_sha256(key, message);
            return code;
        } else {
            throw new IllegalStateException("HMAC is not implemented for the current platform");
        }
    }

    /**
     * Returns true if the app is running as native Android app
     *
     * @return
     */
    public static boolean isAndroidNative() {
        return !isSimulator() && "and".equals(CN.getPlatformName());
    }

    /**
     * Returns true if the app is running as native iOS app
     *
     * @return
     */
    public static boolean isiOSNative() {
        return !isSimulator() && "ios".equals(CN.getPlatformName());
    }

    /**
     * Returns true if the app is running in the CN1 Simulator
     *
     * @return
     */
    public static boolean isSimulator() {
        return Display.getInstance().isSimulator();
    }

    /**
     * Called by native code, it sends by email the current log.
     */
    public static void sendLog() {
        CN.callSerially(() -> {
            Log.sendLogAsync();
        });
    }

    /**
     * Called by native code, it shows a ToastBar with the text "Trying to
     * reconnect within 10 seconds...".
     */
    public static void showReconnectingToastBar() {
        CN.callSerially(() -> {
            ToastBar.showMessage("Network issue, trying to reconnect within 10 seconds, please wait...", com.codename1.ui.FontImage.MATERIAL_WARNING, 10000);
        });
    }

}
