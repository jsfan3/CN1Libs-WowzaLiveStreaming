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

import com.codename1.io.Log;
import com.codename1.io.rest.ErrorCodeHandler;
import com.codename1.io.rest.Response;
import com.codename1.util.OnComplete;
import java.util.Map;
import static net.informaticalibera.cn1libs.wowza.WowzaAccount.isVerboseLog;
import static net.informaticalibera.cn1libs.wowza.WowzaLiveStream.getRequest;

/**
 * This class represents the configuration needed by GoCoder SDK to connect to
 * Wowza Streaming Cloud live stream
 *
 * @author Francesco Galgani 
 */
public class GoCoderBroadcastConfig {

    private final String id;
    private String hostAddress;
    private int portNumber;
    private String applicationName;
    private String streamName;
    private String username;
    private String password;

    private GoCoderBroadcastConfig(String id) {
        this.id = id;
    }

    ;
    
    /**
     * (Async) Get an instance of GoCoderBroadcastConfig after querying the
     * Wowza Cloud server for the stream with the given id.
     * 
     * @param id of the stream
     * @param onComplete callback on success
     * @param onFail callback on failure
     */
    public static void getInstance(String id, OnComplete<GoCoderBroadcastConfig> onComplete, Runnable onFail) {
        GoCoderBroadcastConfig instance = new GoCoderBroadcastConfig(id);

        if (id == null) {
            throw new IllegalStateException("GoCoderBroadcastConfig.getInstance -> id of the stream cannot be null");
        }

        getRequest("transcoders/" + id)
                .onErrorCodeString(new ErrorCodeHandler<String>() {
                    @Override
                    public void onError(Response<String> v) {
                        if (v.getResponseCode() == 401) {
                            if (isVerboseLog()) {
                                Log.p("GoCoderBroadcastConfig.getInstance -> (Code 401) Unauthorized, failed to get the state of stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("GoCoderBroadcastConfig.getInstance -> Unknow response with code " + v.getResponseCode() + ", failed to get the state of the stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        }
                    }
                })
                .fetchAsJsonMap(new OnComplete<Response<Map>>() {
                    @Override
                    public void completed(Response<Map> v) {
                        // success
                        Map responseData = v.getResponseData();
                        Map response = (Map) responseData.get("transcoder");

                        try {
                            instance.hostAddress = (String) response.get("domain_name");
                            instance.portNumber = ((Double) response.get("source_port")).intValue();
                            instance.applicationName = (String) response.get("application_name");
                            instance.streamName = (String) response.get("stream_name");
                            instance.username = (String) response.get("username");
                            instance.password = (String) response.get("password");
                            if (isVerboseLog()) {
                                Log.p("GoCoderBroadcastConfig.getInstance -> (Code 200) Successfully get the transcoder info for the stream with id " + id, Log.DEBUG);
                            }
                            onComplete.completed(instance);
                        } catch (Exception ex) {
                            if (isVerboseLog()) {
                                Log.e(ex);
                                Log.p("GoCoderBroadcastConfig.getInstance -> (Code 200) Error: the transcoder info for the stream with id " + id + " is invalid", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        }
                    }
                });
    }

    public String getId() {
        return id;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getStreamName() {
        return streamName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
