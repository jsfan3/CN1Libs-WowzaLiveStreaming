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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class HMAC_SHA256Impl {

    public String HMAC_sha256(String param, String param1) {
        String secret = param;
        String message = param1;

        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            // return Hex.encodeHexString(sha256_HMAC.doFinal(message.getBytes()));
            return new String(Hex.encodeHex(sha256_HMAC.doFinal(message.getBytes()))); // https://stackoverflow.com/a/9284092
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
    }

    public boolean isSupported() {
        return true;
    }

}
