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
import com.codename1.ui.PeerComponent;

/**
 * @deprecated GoCoder Native Interface, deprecated because you shouldn't use
 * this class directly. Use the "GoCoder" class instead.
 * 
 * @author Francesco Galgani 
 */
public interface GoCoderNative extends NativeInterface {
    
    public void startBroadcast(String hostAddress, int portNumber, String applicationName, String streamName, String username, String password);
    
    public void stopBroadcast();
    
    public String getBroadcastStatus();
    
    public PeerComponent getCameraView(String goCoderApiKey);
    
    public PeerComponent getPlayerView(String goCoderApiKey, String hostAddress, int portNumber, String applicationName, String streamName, String username, String password);
    
    public void stopPlayer();
    
}
