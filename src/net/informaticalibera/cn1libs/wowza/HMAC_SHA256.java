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

import com.codename1.system.NativeInterface;

/**
 * <p>
 * @deprecated HMAC-SHA256 implementation for Codename One, it's deprecated
 * because you should not use this interface directly. Use the "Utilities" class
 * instead.</p>
 * <p>
 * In cryptography, an HMAC (sometimes expanded as either keyed-hash message
 * authentication code or hash-based message authentication code) is a specific
 * type of message authentication code (MAC) involving a cryptographic hash
 * function and a secret cryptographic key.</p>
 *
 * @author Francesco Galgani 
 */
public interface HMAC_SHA256 extends NativeInterface {

    public String HMAC_sha256(String key, String data);

}
