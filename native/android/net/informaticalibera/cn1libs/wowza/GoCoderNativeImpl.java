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

import android.Manifest;
import android.os.Bundle;

import com.codename1.impl.android.AndroidNativeUtil;
import com.codename1.impl.android.LifecycleListener;
import com.codename1.io.Log;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZBroadcastStatus;
import com.wowza.gocoder.sdk.api.status.WOWZBroadcastStatusCallback;
import com.wowza.gocoder.sdk.api.status.WOWZPlayerStatus;
import com.wowza.gocoder.sdk.api.status.WOWZPlayerStatusCallback;

public class GoCoderNativeImpl implements WOWZBroadcastStatusCallback, WOWZPlayerStatusCallback {

    // The top-level GoCoder API interface
    private WowzaGoCoder goCoder = null;

    // The GoCoder SDK camera view
    private WOWZCameraView goCoderCameraView = null;

    // The GoCoder SDK media config
    private WOWZMediaConfig goCoderMediaConfig;

    // The GoCoder SDK audio device
    private WOWZAudioDevice goCoderAudioDevice;

    // The GoCoder SDK broadcaster
    private WOWZBroadcast goCoderBroadcaster;

    // The broadcast configuration settings
    private WOWZBroadcastConfig goCoderBroadcastConfig;

    // Player configuration settings
    private WOWZPlayerConfig mStreamPlayerConfig;

    // Player video
    private WOWZPlayerView mStreamPlayerView;

    // Status
    private String status;

    private boolean startRequested = false;

    // instance of the current class
    private GoCoderNativeImpl instance;

    public GoCoderNativeImpl() {
        this.instance = this;
    }

    public android.view.View getCameraView(String param) {

        final String key = param;

        if (AndroidNativeUtil.checkForPermission(Manifest.permission.CAMERA, "") && AndroidNativeUtil.checkForPermission(Manifest.permission.RECORD_AUDIO, "")) {

            com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {

                public void run() {

                    // check if goCoder was initialized
                    if (goCoder == null) {
                        goCoder = WowzaGoCoder.init(AndroidNativeUtil.getContext(), key);
                        if (goCoder == null) {
                            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
                            com.codename1.io.Log.p(goCoderInitError.getErrorDescription(), Log.ERROR);
                            net.informaticalibera.cn1libs.wowza.Utilities.sendLog();
                            goCoderCameraView = null; // there was an error
                        }
                    }

                    if (goCoder != null) {
                        goCoderCameraView = new WOWZCameraView(AndroidNativeUtil.getContext());
                        switch (net.informaticalibera.cn1libs.wowza.GoCoder.getQuality()) {
                            case (net.informaticalibera.cn1libs.wowza.GoCoder.LOW_QUALITY_360p):
                                goCoderMediaConfig = WOWZMediaConfig.FRAME_SIZE_352x288;
                                goCoderMediaConfig.setVideoFrameSize(640, 360);
                                break;
                            case (net.informaticalibera.cn1libs.wowza.GoCoder.MEDIUM_QUALITY_720p):
                                goCoderMediaConfig = WOWZMediaConfig.FRAME_SIZE_1280x720;
                                break;
                            case (net.informaticalibera.cn1libs.wowza.GoCoder.HIGH_QUALITY_1080p):
                                goCoderMediaConfig = WOWZMediaConfig.FRAME_SIZE_1920x1080;
                                break;
                            default:
                                goCoderMediaConfig = WOWZMediaConfig.FRAME_SIZE_1280x720;
                                break;
                        }
                        goCoderCameraView.setCameraConfig(goCoderMediaConfig);
                        // Start the camera preview display
                        goCoderCameraView.startPreview();

                        LifecycleListener listener = new LifecycleListener() {
                            public void onCreate(Bundle savedInstanceState) {
                            }

                            public void onPause() {
                                if (!goCoderCameraView.isPreviewPaused()) {
                                    goCoderCameraView.onPause();
                                }
                            }

                            public void onDestroy() {
                            }

                            public void onSaveInstanceState(Bundle b) {
                            }

                            public void onLowMemory() {
                            }

                            public void onResume() {
                                if (goCoderCameraView.isPreviewPaused()) {
                                    goCoderCameraView.onResume();
                                } else {
                                    goCoderCameraView.startPreview();
                                }
                            }
                        };

                        AndroidNativeUtil.addLifecycleListener(listener);
                    }
                }
            });
        };

        return goCoderCameraView;

    }

    public android.view.View getPlayerView(String param, String param1, int param2, String param3, String param4, String param5, String param6) {
        final String key = param;
        final String hostAddress = param1;
        final int portNumber = param2;
        final String applicationName = param3;
        final String streamName = param4;
        final String username = param5;
        final String password = param6;

        // check if goCoder was initialized
        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            @Override
            public void run() {

                if (goCoder == null) {
                    goCoder = WowzaGoCoder.init(AndroidNativeUtil.getContext(), key);
                    if (goCoder == null) {
                        WOWZError goCoderInitError = WowzaGoCoder.getLastError();
                        com.codename1.io.Log.p(goCoderInitError.getErrorDescription(), Log.ERROR);
                        net.informaticalibera.cn1libs.wowza.Utilities.sendLog();
                        mStreamPlayerView = null; // there was an error
                    }
                }

                if (goCoder != null) {

                    mStreamPlayerConfig = new WOWZPlayerConfig();
                    mStreamPlayerConfig.setIsPlayback(true);
                    mStreamPlayerConfig.setHostAddress(hostAddress);
                    mStreamPlayerConfig.setApplicationName(applicationName);
                    mStreamPlayerConfig.setStreamName(streamName);
                    mStreamPlayerConfig.setPortNumber(portNumber);
                    mStreamPlayerConfig.setUsername(username);
                    mStreamPlayerConfig.setPassword(password);

                    mStreamPlayerView = new WOWZPlayerView(AndroidNativeUtil.getContext());
                    mStreamPlayerView.play(mStreamPlayerConfig, instance);
                }
            }
        });

        return mStreamPlayerView;
    }

    public void stopPlayer() {
        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            @Override
            public void run() {

                if (mStreamPlayerView.isPlaying()) {
                    mStreamPlayerView.stop(instance);
                }
            }
        });
    }

    public void startBroadcast(String param, int param1, String param2, String param3, String param4, String param5) {
        startRequested = true;

        final String hostAddress = param;
        final int portNumber = param1;
        final String applicationName = param2;
        final String streamName = param3;
        final String username = param4;
        final String password = param5;

        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            @Override
            public void run() {

                // Create an audio device instance for capturing and broadcasting audio
                goCoderAudioDevice = new WOWZAudioDevice();

                // Create a broadcaster instance
                goCoderBroadcaster = new WOWZBroadcast();

                // Create a configuration instance for the broadcaster
                goCoderBroadcastConfig = new WOWZBroadcastConfig(goCoderMediaConfig);

                // Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream
                goCoderBroadcastConfig.setHostAddress(hostAddress);
                goCoderBroadcastConfig.setPortNumber(portNumber);
                goCoderBroadcastConfig.setApplicationName(applicationName);
                goCoderBroadcastConfig.setStreamName(streamName);
                goCoderBroadcastConfig.setUsername(username);
                goCoderBroadcastConfig.setPassword(password);

                // Designate the camera preview as the video source
                goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView);

                // Designate the audio device as the audio broadcaster
                goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);

                goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, instance);
            }
        });
    }

    public void stopBroadcast() {
        startRequested = false;
        if (goCoderBroadcaster.getStatus().isBroadcasting()) {
            goCoderBroadcaster.endBroadcast();
        }
    }

    public String getBroadcastStatus() {
        return status;
    }

    public boolean isSupported() {
        return true;
    }

    @Override
    public void onWZStatus(final WOWZBroadcastStatus goCoderStatus) {

        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            @Override
            public void run() {

                WOWZBroadcastStatus.BroadcastState myState = goCoderStatus.getState();

                if (myState == WOWZBroadcastStatus.BroadcastState.READY) {
                    status = "starting";
                } else if (myState == WOWZBroadcastStatus.BroadcastState.BROADCASTING) {
                    status = "running";
                } else if (myState == WOWZBroadcastStatus.BroadcastState.IDLE) {
                    status = "stopped";
                } else {
                    status = null;
                }

            }
        });

    }

    @Override
    public void onWZError(final WOWZBroadcastStatus goCoderStatus) {
        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            @Override
            public void run() {
                com.codename1.io.Log.p("Streaming error: " + goCoderStatus.getLastError().getErrorDescription(), Log.ERROR);
                com.codename1.io.Log.p("Streaming error: " + goCoderStatus.getLastError().toString(), Log.ERROR);
                net.informaticalibera.cn1libs.wowza.Utilities.sendLog();
                status = null;
                if (startRequested) {
                    net.informaticalibera.cn1libs.wowza.Utilities.showReconnectingToastBar();
                    final java.util.Timer timer = new java.util.Timer();
                    timer.schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if (startRequested) {
                                com.codename1.io.Log.p("Trying to start broadcast again", Log.DEBUG);
                                goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, instance);
                            }
                            timer.cancel();
                        }
                    }, 10000);
                }
            }
        });
    }

    @Override
    public void onWZStatus(final WOWZPlayerStatus goCoderStatus) {
        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            @Override
            public void run() {
                WOWZPlayerStatus.PlayerState myState = goCoderStatus.getState();

                if (myState == WOWZPlayerStatus.PlayerState.BUFFERING || myState == WOWZPlayerStatus.PlayerState.CONNECTING) {
                    status = "starting";
                } else if (myState == WOWZPlayerStatus.PlayerState.PLAYING) {
                    status = "running";
                } else if (myState == WOWZPlayerStatus.PlayerState.STOPPING || myState == WOWZPlayerStatus.PlayerState.IDLE || myState == WOWZPlayerStatus.PlayerState.PAUSING) {
                    status = "stopped";
                } else {
                    status = null;
                }
            }
        });
    }

    @Override
    public void onWZError(final WOWZPlayerStatus goCoderStatus) {
        com.codename1.impl.android.AndroidImplementation.runOnUiThreadAndBlock(new Runnable() {
            @Override
            public void run() {
                com.codename1.io.Log.p("Playing stream error: " + goCoderStatus.getLastError().getErrorDescription(), Log.ERROR);
                com.codename1.io.Log.p("Playing stream error: " + goCoderStatus.getLastError().toString(), Log.ERROR);
                net.informaticalibera.cn1libs.wowza.Utilities.sendLog();
            }
        });
    }
}
