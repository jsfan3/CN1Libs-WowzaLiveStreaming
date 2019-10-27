package net.informaticalibera.cn1libs.wowza;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class HMAC_SHA256Impl implements net.informaticalibera.cn1libs.wowza.HMAC_SHA256 {

    public String HMAC_sha256(String param, String param1) {
        String secret = param;
        String message = param1;

        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return Hex.encodeHexString(sha256_HMAC.doFinal(message.getBytes()));
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
    }

    public boolean isSupported() {
        return true;
    }

}
