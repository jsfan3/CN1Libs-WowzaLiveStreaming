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

import com.codename1.components.SpanLabel;
import com.codename1.io.Log;
import com.codename1.system.NativeLookup;
import com.codename1.ui.Component;

/**
 * GoCoder main class
 *
 * @author Francesco Galgani
 */
public class GoCoder {

    /**
     * Video resolution of 640x360 pixels, 1-Mbps video bitrate, 30 fps,
     * keyframe interval of 30, 44.1 kHz audio sample rate, 64-kbps audio
     * bitrate.
     */
    public static final int LOW_QUALITY_360p = 0;

    /**
     * Video resolution of 1280x720 pixels, 3.75-Mbps video bitrate, 30 fps,
     * keyframe interval of 30, 44.1 kHz audio sample rate, 64-kbps audio
     * bitrate.
     */
    public static final int MEDIUM_QUALITY_720p = 1;

    /**
     * Video resolution of 1920x1080 pixels, 5-Mbps video bitrate, 30 fps,
     * keyframe interval of 30, 44.1 kHz audio sample rate, 64-kbps audio
     * bitrate.
     */
    public static final int HIGH_QUALITY_1080p = 2;

    private static int quality = MEDIUM_QUALITY_720p;
    private static GoCoderNative goCoderNative = NativeLookup.create(GoCoderNative.class);

    /**
     * Start broadcast.
     */
    public static void startBroadcast(GoCoderBroadcastConfig config) {
        if (goCoderNative != null && goCoderNative.isSupported()) {
            goCoderNative.startBroadcast(config.getHostAddress(), config.getPortNumber(), config.getApplicationName(), config.getStreamName(), config.getUsername(), config.getPassword());
        } else {
            if (WowzaAccount.isVerboseLog()) {
                Log.p("GoCoder.startBroadcast -> GoCoder is not supported in the current platform", Log.DEBUG);
            }
        }
    }

    /**
     * Stop the broadcast that is currently running.
     */
    public static void stopBroadcast() {
        if (goCoderNative != null && goCoderNative.isSupported()) {
            goCoderNative.stopBroadcast();
        } else {
            if (WowzaAccount.isVerboseLog()) {
                Log.p("GoCoder.stopBroadcast -> GoCoder is not supported in the current platform", Log.DEBUG);
            }
        }
    }

    /**
     * Stop the broadcast that is currently running.
     */
    public static void stopPlayer() {
        if (goCoderNative != null && goCoderNative.isSupported()) {
            goCoderNative.stopPlayer();
        } else {
            if (WowzaAccount.isVerboseLog()) {
                Log.p("GoCoder.stopPlayer -> GoCoder is not supported in the current platform", Log.DEBUG);
            }
        }
    }

    /**
     * Get the status of the broadcast that is currently running.
     *
     * @return status, is one of: stopped, starting, running, stopping (or null
     * in the Simulator)
     */
    public static String getBroadcastStatus() {
        if (goCoderNative != null && goCoderNative.isSupported()) {
            String status = goCoderNative.getBroadcastStatus();
            if (WowzaAccount.isVerboseLog()) {
                // Log.p("GoCoder.getBroadcastStatus -> The status of the current broadcast is: " + status, Log.DEBUG);
            }
            return status;
        } else {
            if (WowzaAccount.isVerboseLog()) {
                Log.p("GoCoder.getBroadcastStatus -> GoCoder is not supported in the current platform", Log.DEBUG);
            }
            return null;
        }
    }

    /**
     * Initialize GoCoder SDK (only on the first invocation) and returns the
     * camera view.
     *
     * @return a PeerComponent
     */
    public static Component getCameraView() {
        if (goCoderNative != null && goCoderNative.isSupported()) {
            String key;
            if (Utilities.isAndroidNative()) {
                key = WowzaAccount.getApiKey_Android();
            } else if (Utilities.isiOSNative()) {
                key = WowzaAccount.getApiKey_iOS();
            } else {
                throw new IllegalStateException("GoCoder.startBroadcast -> The current platform is not supported");
            }
            return goCoderNative.getCameraView(key);
        } else {
            return new SpanLabel("GoCoder.getCameraView -> GoCoder is not supported in the current platform");
        }
    }

    /**
     * Initialize GoCoder SDK (only on the first invocation) and returns the
     * player view.
     *
     * @return a PeerComponent
     */
    public static Component getPlayerView(GoCoderBroadcastConfig config) {
        if (goCoderNative != null && goCoderNative.isSupported()) {
            String key;
            if (Utilities.isAndroidNative()) {
                key = WowzaAccount.getApiKey_Android();
            } else if (Utilities.isiOSNative()) {
                key = WowzaAccount.getApiKey_iOS();
            } else {
                throw new IllegalStateException("GoCoder.startBroadcast -> The current platform is not supported");
            }
            return goCoderNative.getPlayerView(key, config.getHostAddress(), config.getPortNumber(), config.getApplicationName(), config.getStreamName(), config.getUsername(), config.getPassword());
        } else {
            return new SpanLabel("GoCoder.getPlayerView -> GoCoder is not supported in the current platform");
        }
    }

    /**
     * Gets the quality
     *
     * @return one of: LOW_QUALITY_360p, MEDIUM_QUALITY_720p, HIGH_QUALITY_1080p
     */
    public static int getQuality() {
        return quality;
    }

    /**
     * Sets the quality, defaults to MEDIUM_QUALITY_720p
     *
     * @param quality one of: LOW_QUALITY_360p, MEDIUM_QUALITY_720p,
     * HIGH_QUALITY_1080p
     */
    public static void setQuality(int quality) {
        GoCoder.quality = quality;
    }

}
