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

import com.codename1.io.JSONParser;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.io.rest.ErrorCodeHandler;
import com.codename1.io.rest.RequestBuilder;
import com.codename1.io.rest.Response;
import com.codename1.io.rest.Rest;
import com.codename1.util.OnComplete;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import static net.informaticalibera.cn1libs.wowza.WowzaAccount.isVerboseLog;

/**
 * <p>
 * Every instance of this class represents a Wowza Live Stream, that is
 * indentified by its "name", "id" and "connection_code".
 * </p>
 * <p>
 * The "name" is a descriptive name for the live stream.<br>
 * The "id" is the unique alphanumeric string that identifies the live
 * stream.<br>
 * The "connection_code" is a six-character, alphanumeric string that allows
 * certain encoders, including Wowza Streaming Engine and the Wowza GoCoder app,
 * to connect with Wowza Streaming Cloud. The code can be used once and expires
 * 24 hours after it's created.
 * </p>
 * <p>
 * More info about the REST API used by this class:<br>
 * <a href="https://www.wowza.com/docs/how-to-use-the-wowza-streaming-cloud-rest-api" target="noframe">About
 * the Wowza Streaming Cloud REST API</a><br>
 * <a href="https://sandbox.cloud.wowza.com/api/current/docs" target="noframe">Wowza
 * Streaming Cloud REST API Reference Documentation (v1.3)</a>
 * </p>
 *
 * @author Francesco Galgani 
 */
public class WowzaLiveStream {

    private static final String url = "https://api.cloud.wowza.com";
    private static final String apiVersion = "/api/v1.3/";

    private static final List<String> broadcastLocations = Utilities.createList("asia_pacific_australia", "asia_pacific_india", "asia_pacific_japan", "asia_pacific_singapore", "asia_pacific_s_korea", "asia_pacific_taiwan", "eu_belgium", "eu_germany", "eu_ireland", "south_america_brazil", "us_central_iowa", "us_east_s_carolina", "us_east_virginia", "us_west_california", "us_west_oregon");
    private static final List<String> broadcastLocationsOnlyPassthrough = Utilities.createList("asia_pacific_taiwan", "eu_belgium", "us_central_iowa", "us_east_s_carolina");
    private static final List<String> transcoderTypes = Utilities.createList("transcoded", "passthrough");

    private String name = null;
    private String id = null;
    private String connection_code = null;

    private final WowzaLiveStream instance;
    private WowzaLiveStreamParams creationParams;

    private static boolean hmacAuthentication = false;

    public WowzaLiveStream() {
        this.instance = this;
    }

    /**
     * <p>
     * (Async) Create a new live stream with the given name and using the
     * default params; note that you can create up to 10 within 3 hours, see
     * <a href="https://www.wowza.com/docs/Wowza-Streaming-Cloud-REST-API-limits" target="noframe">usage
     * limits</a>.
     * </p>
     * <p>
     * You can use the same name for different streams, although this is not
     * recommended.
     * </p>
     * <p>
     * It throws an IllegalArgumentException if the params are not valid,
     * according to
     * <a href="https://cloud.wowza.com/api/current/docs#operation/createLiveStream" target="noframe">Wowza
     * documentation requirements</a> about creating a new live streaming.
     * Further documentation about params requirements is in the section
     * <a href="https://www.wowza.com/docs/how-to-broadcast-a-live-stream-by-using-the-wowza-streaming-cloud-rest-api#paramIngest" target="noframe">Ingest
     * parameters</a>.
     * </p>
     *
     * @param name is a descriptive name of the stream
     * @param onComplete is a callback for success
     * @param onFail is a callback for failure
     */
    public void create(String name, OnComplete<WowzaLiveStream> onComplete, Runnable onFail) {
        WowzaLiveStreamParams params = new WowzaLiveStreamParams();
        params.name.set(name); // the name is the only mandatory property to be set
        this.create(params, onComplete, onFail);
    }

    /**
     * <p>
     * (Async) Create a new live stream with the given params; note that you can
     * create up to 10 within 3 hours, see
     * <a href="https://www.wowza.com/docs/Wowza-Streaming-Cloud-REST-API-limits" target="noframe">usage
     * limits</a>.
     * </p>
     * <p>
     * It throws an IllegalArgumentException if the params are not valid,
     * according to
     * <a href="https://cloud.wowza.com/api/current/docs#operation/createLiveStream" target="noframe">Wowza
     * documentation requirements</a> about creating a new live streaming.
     * Further documentation about params requirements is in the section
     * <a href="https://www.wowza.com/docs/how-to-broadcast-a-live-stream-by-using-the-wowza-streaming-cloud-rest-api#paramIngest" target="noframe">Ingest
     * parameters</a>.
     * </p>
     *
     * @param params is a PBO with all relevant stream options
     * @param onComplete is a callback for success
     * @param onFail is a callback for failure
     */
    public void create(WowzaLiveStreamParams params, OnComplete<WowzaLiveStream> onComplete, Runnable onFail) {
        throwExceptionIfIllegalParams(params);
        this.creationParams = params;

        // created the json with the given params
        Map<String, Object> paramsMap = params.getPropertyIndex().toMapRepresentation();
        Map<String, Object> body = new HashMap<>();
        body.put("live_stream", paramsMap);
        String json = JSONParser.mapToJson(body);

        postRequest("live_streams")
                .body(json)
                .onErrorCodeString(new ErrorCodeHandler<String>() {
                    @Override
                    public void onError(Response<String> v) {
                        if (v.getResponseCode() == 401) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.create -> (Code 401) Unauthorized, failed to create live stream with name " + params.name.get() + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else if (v.getResponseCode() == 422) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.create -> (Code 422) Unprocessable Entity, failed to create live stream with name " + params.name.get() + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.create -> Unknow response with code " + v.getResponseCode() + ", failed to create live stream with name " + params.name.get() + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
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
                        Map<String, Object> responseData = v.getResponseData();
                        Map<String, Object> response = (Map<String, Object>) responseData.get("live_stream");
                        name = (String) response.get("name");
                        id = (String) response.get("id");
                        connection_code = (String) response.get("connection_code");
                        if (isVerboseLog()) {
                            Log.p("WowzaLiveStream.create -> (Code 201) Successfully created live stream with name " + name, Log.DEBUG);
                        }
                        onComplete.completed(instance);
                    }
                });

    }

    static RequestBuilder postRequest(String api) {
        if (hmacAuthentication) {
            return Rest.post(url + apiVersion + api)
                    .jsonContent()
                    .header("wsc-access-key", WowzaAccount.getAccessKey())
                    .header("wsc-timestamp", System.currentTimeMillis() + "")
                    .header("wsc-signature", Utilities.generate_request_signature(apiVersion + api, WowzaAccount.getRestKey(), System.currentTimeMillis()));
        } else {
            return Rest.post(url + apiVersion + api)
                    .jsonContent()
                    .header("wsc-access-key", WowzaAccount.getAccessKey())
                    .header("wsc-api-key", Util.xorDecode(WowzaAccount.getRestKey()));
        }
    }

    static RequestBuilder getRequest(String api) {
        if (hmacAuthentication) {
            return Rest.get(url + apiVersion + api)
                    .jsonContent()
                    .header("wsc-access-key", WowzaAccount.getAccessKey())
                    .header("wsc-timestamp", System.currentTimeMillis() + "")
                    .header("wsc-signature", Utilities.generate_request_signature(apiVersion + api, WowzaAccount.getRestKey(), System.currentTimeMillis()));
        } else {
            return Rest.get(url + apiVersion + api)
                    .jsonContent()
                    .header("wsc-access-key", WowzaAccount.getAccessKey())
                    .header("wsc-api-key", Util.xorDecode(WowzaAccount.getRestKey()));
        }
    }

    static RequestBuilder putRequest(String api) {
        if (hmacAuthentication) {
            return Rest.put(url + apiVersion + api)
                    .jsonContent()
                    .header("wsc-access-key", WowzaAccount.getAccessKey())
                    .header("wsc-timestamp", System.currentTimeMillis() + "")
                    .header("wsc-signature", Utilities.generate_request_signature(apiVersion + api, WowzaAccount.getRestKey(), System.currentTimeMillis()));
        } else {
            return Rest.put(url + apiVersion + api)
                    .jsonContent()
                    .header("wsc-access-key", WowzaAccount.getAccessKey())
                    .header("wsc-api-key", Util.xorDecode(WowzaAccount.getRestKey()));
        }
    }

    private void throwExceptionIfIllegalParams(WowzaLiveStreamParams params) {
        if (params.name.get() == null || params.name.get().isEmpty()) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> Name cannot be null or empty");
        }
        if (params.name.get().length() > 200) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> Name cannot be longer than 200 chars");
        }
        if (!validNameChars(params.name.get())) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> Name can contain only uppercase and lowercase letters; numbers; and the period (.), underscore (_), and hyphen (-) characters. No other special characters can be used.");
        }
        if (params.aspect_ratio_width.getInt() % 8 != 0) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> aspect_ratio_width must be divisible by 8");
        }
        if (params.aspect_ratio_height.getInt() % 8 != 0) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams ->  aspect_ratio_height must be divisible by 8");
        }
        if (params.aspect_ratio_width.getInt() > 3840 || params.aspect_ratio_width.getInt() < 10) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> aspect_ratio_width must be between 10 and 3840");
        }
        if (params.aspect_ratio_height.getInt() > 2160 || params.aspect_ratio_height.getInt() < 10) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> aspect_ratio_height must be between 10 and 2160");
        }
        if (!is16_9(params.aspect_ratio_width.getInt(), params.aspect_ratio_height.getInt()) && !is4_3(params.aspect_ratio_width.getInt(), params.aspect_ratio_height.getInt())) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> aspect_ratio_width & aspect_ratio_height must be in proportion 16:9 or 4:3");
        }
        if (!broadcastLocations.contains(params.broadcast_location.get())) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> invalid broadcast_location " + params.broadcast_location.get());
        }
        if (!"wowza_gocoder".equals(params.encoder.get())) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> invalid encoder, the only supported encoder by this cn1lib is \"wowza_gocoder\"");
        }
        if (!transcoderTypes.contains(params.transcoder_type.get())) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> invalid transcoder_type " + params.transcoder_type.get());
        }
        if ("passthrough".equals(params.transcoder_type.get()) && !broadcastLocationsOnlyPassthrough.contains(params.broadcast_location.get())) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> invalid transcoder_type, passthrough is not available in the selected broadcast location " + params.broadcast_location.get());
        }
        if (params.recording.get() == null) {
            throw new IllegalArgumentException("Invalid WowzaLiveStreamParams -> invalid recording value, it must be true or false, it cannot be null");
        }
    }

    private boolean is16_9(int width, int height) {
        return (height * 16 / 9 == width);
    }

    private boolean is4_3(int width, int height) {
        return (height * 4 / 3 == width);
    }

    /**
     * Name can contain only uppercase and lowercase letters; numbers; and the
     * period (.), underscore (_), and hyphen (-) characters. No other special
     * characters can be used.
     *
     * @param name
     * @return true is the name contains only valid chars
     */
    private boolean validNameChars(String input) {
        String specialChars = "._-";
        boolean result = true;

        for (int i = 0; i < input.length(); i++) {
            char currentCharacter = input.charAt(i);
            if (!Character.isDigit(currentCharacter)
                    && !Character.isUpperCase(currentCharacter)
                    && !Character.isLowerCase(currentCharacter)
                    && !specialChars.contains(String.valueOf(currentCharacter))) {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * Descriptive name for the live stream
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Unique alphanumeric string that identifies the live stream
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * The "connection_code" is a six-character, alphanumeric string that allows
     * certain encoders, including Wowza Streaming Engine and the Wowza GoCoder
     * app, to connect with Wowza Streaming Cloud. The code can be used once and
     * expires 24 hours after it's created.
     *
     * @return connection_code
     */
    public String getConnection_code() {
        return connection_code;
    }

    /**
     * (Async) Fetches the list of all stream ids for the current account
     *
     * @param onComplete is a callback for success
     * @param onFail is a callback for failure
     */
    public static void fetchAllLiveStreams(OnComplete<List<String>> onComplete, Runnable onFail) {
        getRequest("live_streams")
                .onErrorCodeString(new ErrorCodeHandler<String>() {
                    @Override
                    public void onError(Response<String> v) {
                        if (v.getResponseCode() == 401) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchAllLiveStreams -> (Code 401) Unauthorized, failed to get all streams for the current account, string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchAllLiveStreams -> Unknow response with code " + v.getResponseCode() + ", failed to get all streams for the current account, string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
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
                        List<Map> response = (List<Map>) responseData.get("live_streams");
                        ArrayList<String> idList = new ArrayList<>();
                        for (Map stream : response) {
                            idList.add((String) stream.get("id"));
                        }
                        if (isVerboseLog()) {
                            Log.p("WowzaLiveStream.fetchAllLiveStreams -> (Code 200) Successfully get all streams for the current account", Log.DEBUG);
                        }
                        onComplete.completed(idList);
                    }
                });

    }

    /**
     * Fetches a thumbnail url for the given stream; the thumbnail url can be
     * null if there is no stream data.
     *
     * @param id of the given stream
     * @param onComplete is a callback invoked if a thumbnail url was returned
     * @param onNull is a callback invoked if the server returned "null" as
     * thumbnail url
     * @param onFail is a callback for failure
     */
    public static void fetchThumbnail(String id, OnComplete<String> onComplete, Runnable onNull, Runnable onFail) {
        getRequest("live_streams/" + id + "/thumbnail_url")
                .onErrorCodeString(new ErrorCodeHandler<String>() {
                    @Override
                    public void onError(Response<String> v) {
                        if (v.getResponseCode() == 401) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchThumbnail -> (Code 401) Unauthorized, failed to get thumbnail for the stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchThumbnail -> Unknow response with code " + v.getResponseCode() + ", failed to get thumbnail for the stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
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
                        Map response = (Map) responseData.get("live_stream");
                        String url = (String) response.get("thumbnail_url");
                        if ("null".equals(url) || url == null) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchThumbnail -> (Code 200) Warning: thumbnail url for the given stream id " + id + " is null", Log.WARNING);
                            }
                            onNull.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchThumbnail -> (Code 200) Successfully get thumbnail url for the given stream id " + id, Log.DEBUG);
                            }
                            onComplete.completed(url);
                        }
                    }
                });
    }

    /**
     * <p>
     * (Async) Fetches the state of the given stream, providing all the possible
     * callbacks.
     * </p>
     * <p>
     * Note: Transcoding charges accrue for started live streams even if they
     * aren't actively streaming content. Wowza Streaming Cloud automatically
     * stops idle live streams after 20 minutes, but you should stop your stream
     * as soon as your event ends to avoid accruing unnecessary charges.</p>
     *
     * @param id of the given stream, it cannot be null
     * @param isStarted is a callback invoked if the state is "started", it can
     * be null if you don't want to provide a callback for this state
     * @param isStopped is a callback invoked if the state is "stopped", it can
     * be null if you don't want to provide a callback for this state
     * @param isStarting is a callback invoked if the state is "starting", it
     * can be null if you don't want to provide a callback for this state
     * @param isStopping is a callback invoked if the state is "stopping", it
     * can be null if you don't want to provide a callback for this state
     * @param isResetting is a callback invoked if the state is "resetting", it
     * can be null if you don't want to provide a callback for this state
     * @param onFail is a callback for failure
     */
    public static void fetchState(String id, OnComplete<String> isStarted, OnComplete<String> isStopped, OnComplete<String> isStarting, OnComplete<String> isStopping, OnComplete<String> isResetting, Runnable onFail) {
        if (id == null) {
            throw new IllegalStateException("WowzaLiveStreaming.fetchState -> id of the stream cannot be null");
        }

        getRequest("live_streams/" + id + "/state")
                .onErrorCodeString(new ErrorCodeHandler<String>() {
                    @Override
                    public void onError(Response<String> v) {
                        if (v.getResponseCode() == 401) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchState -> (Code 401) Unauthorized, failed to get the state of stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchState -> Unknow response with code " + v.getResponseCode() + ", failed to get the state of the stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
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
                        Map response = (Map) responseData.get("live_stream");
                        String state = (String) response.get("state");
                        if (isVerboseLog()) {
                            Log.p("WowzaLiveStream.fetchState -> (Code 200) Successfully get the state " + state + " for the stream with id " + id, Log.DEBUG);
                        }
                        if ("started".equals(state)) {
                            if (isStarted != null) {
                                isStarted.completed(id);
                            }
                        } else if ("stopped".equals(state)) {
                            if (isStopped != null) {
                                isStopped.completed(id);
                            }
                        } else if ("starting".equals(state)) {
                            if (isStarting != null) {
                                isStarting.completed(id);
                            }
                        } else if ("stopping".equals(state)) {
                            if (isStopping != null) {
                                isStopping.completed(id);
                            }
                        } else if ("resetting".equals(state)) {
                            if (isResetting != null) {
                                isResetting.completed(id);
                            }
                        } else {
                            throw new IllegalStateException("WowzaLiveStream.fetchState -> The server returned the invalid state \"" + state + "\" for the stream id " + id);
                        }
                    }
                });
    }

    /**
     * <p>
     * (Async) Fetches the state of the given stream, providing a minimal set of
     * callbacks
     * </p>
     * <p>
     * Note: Transcoding charges accrue for started live streams even if they
     * aren't actively streaming content. Wowza Streaming Cloud automatically
     * stops idle live streams after 20 minutes, but you should stop your stream
     * as soon as your event ends to avoid accruing unnecessary charges.</p>
     *
     * @param id of the given stream; if "id" is null, it is treated as a
     * special case that will invoke the isOtherState callback.
     * @param isStarted is a callback invoked if the state is "started", it can
     * be null if you don't want to provide a callback for this state
     * @param isStarting is a callback invoked if the state is "starting", it
     * can be null if you don't want to provide a callback for this state
     * @param isOtherState is a callback invoked if the state is neither
     * "started" nor "stopped", it can be null if you don't want to provide a
     * callback for this state
     * @param onFail is a callback for failure
     */
    public static void fetchState(String id, OnComplete<String> isStarted, OnComplete<String> isStarting, OnComplete<String> isOtherState, Runnable onFail) {
        if (id == null) {
            isOtherState.completed(id);
            return;
        }

        getRequest("live_streams/" + id + "/state")
                .onErrorCodeString(new ErrorCodeHandler<String>() {
                    @Override
                    public void onError(Response<String> v) {
                        if (v.getResponseCode() == 401) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchState -> (Code 401) Unauthorized, failed to get the state of stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchState -> Unknow response with code " + v.getResponseCode() + ", failed to get the state of the stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
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
                        Map response = (Map) responseData.get("live_stream");
                        String state = (String) response.get("state");
                        if (isVerboseLog()) {
                            Log.p("WowzaLiveStream.fetchState -> (Code 200) Successfully get the state " + state + " for the stream with id " + id, Log.DEBUG);
                        }
                        if ("started".equals(state)) {
                            if (isStarted != null) {
                                isStarted.completed(id);
                            }
                        } else if ("starting".equals(state)) {
                            if (isStarting != null) {
                                isStarting.completed(id);
                            }
                        } else {
                            if (isOtherState != null) {
                                isOtherState.completed(id);
                            }
                        }
                    }
                });
    }

    /**
     * <p>
     * (Async) Fetches the state of the given stream, providing a minimal set of
     * callbacks
     * </p>
     * <p>
     * Note: Transcoding charges accrue for started live streams even if they
     * aren't actively streaming content. Wowza Streaming Cloud automatically
     * stops idle live streams after 20 minutes, but you should stop your stream
     * as soon as your event ends to avoid accruing unnecessary charges.</p>
     *
     * @param id of the given stream; if "id" is null, it is treated as a
     * special case that will invoke the isOtherState callback.
     * @param isStopped is a callback invoked if the state is "stopped", it can
     * be null if you don't want to provide a callback for this state
     * @param isOtherState is a callback invoked if the state is not "stopped",
     * it can be null if you don't want to provide a callback for this state
     * @param onFail is a callback for failure
     */
    public static void fetchState(String id, OnComplete<String> isStopped, OnComplete<String> isOtherState, Runnable onFail) {
        if (id == null) {
            isOtherState.completed(id);
            return;
        }

        getRequest("live_streams/" + id + "/state")
                .onErrorCodeString(new ErrorCodeHandler<String>() {
                    @Override
                    public void onError(Response<String> v) {
                        if (v.getResponseCode() == 401) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchState -> (Code 401) Unauthorized, failed to get the state of stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchState -> Unknow response with code " + v.getResponseCode() + ", failed to get the state of the stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
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
                        Map response = (Map) responseData.get("live_stream");
                        String state = (String) response.get("state");
                        if (isVerboseLog()) {
                            Log.p("WowzaLiveStream.fetchState -> (Code 200) Successfully get the state " + state + " for the stream with id " + id, Log.DEBUG);
                        }
                        if ("stopped".equals(state)) {
                            if (isStopped != null) {
                                isStopped.completed(id);
                            }
                        } else {
                            if (isOtherState != null) {
                                isOtherState.completed(id);
                            }
                        }
                    }
                });
    }
    
    /**
     * <p>
     * (Async) Fetches the connection state of the given stream, providing a minimal set of
     * callbacks
     * </p>
     * <p>
     * Note: Transcoding charges accrue for started live streams even if they
     * aren't actively streaming content. Wowza Streaming Cloud automatically
     * stops idle live streams after 20 minutes, but you should stop your stream
     * as soon as your event ends to avoid accruing unnecessary charges.</p>
     *
     * @param id of the given stream
     * @param isFine is a callback invoked when the connection state is normal
     * @param isNotFine is a callback invoked when the connection state is warning or no_data
     * @param onFail is a callback for failure
     */
    public static void fetchConnectionState(String id, OnComplete<String> isFine, OnComplete<String> isNotFine, Runnable onFail) {

        getRequest("live_streams/" + id + "/stats")
                .onErrorCodeString(new ErrorCodeHandler<String>() {
                    @Override
                    public void onError(Response<String> v) {
                        if (v.getResponseCode() == 401) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchConnectionState -> (Code 401) Unauthorized, failed to get the connection state of stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.fetchConnectionState -> Unknow response with code " + v.getResponseCode() + ", failed to get the connection state of the stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
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
                        Map response = (Map) responseData.get("live_stream");
                        Map connected = (Map) response.get("connected");
                        
                        String status = (String) connected.get("value");
                        if (isVerboseLog()) {
                            Log.p("WowzaLiveStream.fetchConnectionState -> (Code 200) Successfully get the status " + status + " for the stream with id " + id, Log.DEBUG);
                        }
                        if ("Yes".equals(status)) {
                            if (isFine != null) {
                                isFine.completed(id);
                            }
                        } else {
                            if (isNotFine != null) {
                                isNotFine.completed(id);
                            }
                        }
                    }
                });
    }

    /**
     * (Async) Fetches the download url of a recorded event
     *
     * @param id of the given recorded stream
     * @param onComplete is a callback for success
     * @param onFail is a callback for failure
     */
    public static void fetchRecording(String id, OnComplete<String> onComplete, Runnable onFail) {
        throw new IllegalStateException("WowzaLiveStream.fetchRecording not yet implemented");
    }

    /**
     * <p>
     * (Async) Get a stopped stream from your pool and manage your pool accoring
     * to the provided startingSize and threshold.</p>
     * <p>
     * About the performance, this methods performs sequential REST requests
     * until it finds a stopped stream, so it can require some seconds before
     * launching the onComplete callback.</p>
     *
     * @param startingSize of your pool: if your pool size is less than your
     * startingSize, then this method adds a new stream to the pool
     * @param threshold is a percentage of active streams: when the threshold is
     * exceeded, a new stream is always added, regardless of the startingSize
     * value
     * @param defaultParams can be used to customize the parameters (except the
     * name) of the new stream that could be created, it can be null to use the
     * default parameters
     * @param onComplete callback invoked on success, the passed value is the id
     * of a stopped stream of the pool
     * @param onFail callback invoked on failure
     */
    public static void getStreamFromPool(int startingSize, int threshold, WowzaLiveStreamParams defaultParams, OnComplete<String> onComplete, Runnable onFail) {
        fetchAllLiveStreams((List<String> ids) -> {
            // success
            int poolSize = ids.size();
            if (isVerboseLog()) {
                Log.p("WowzaLiveStream.getStreamFromPool -> Pool size: " + poolSize, Log.DEBUG);
            }
            int[] usedStreams = {0};
            int[] indexId = {0};
            Runnable[] stateOfStream = {null};
            stateOfStream[0] = () -> {
                String newStreamName = "Stream-" + (poolSize + 1);
                WowzaLiveStreamParams params;
                if (defaultParams != null) {
                    params = defaultParams;
                } else {
                    params = new WowzaLiveStreamParams();
                }
                params.name.set(newStreamName);

                String idToBeChecked = null;
                if (poolSize > 0) {
                    idToBeChecked = ids.get(indexId[0]);
                }

                WowzaLiveStream.fetchState(idToBeChecked, streamId -> {
                    // we got a stopped stream
                    if (isVerboseLog()) {
                        Log.p("WowzaLiveStream.getStreamFromPool -> Found the stopped stream: " + streamId + ", which index is " + indexId[0] + " (starting from 0) of a pool of " + poolSize + " streams", Log.DEBUG);
                    }
                    onComplete.completed(streamId);
                    // does we need to create a new stream?
                    WowzaLiveStream stream = new WowzaLiveStream();
                    if (poolSize < startingSize) {
                        stream.create(params, newStream -> {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.getStreamFromPool -> Created new stream with name " + newStream.getName() + " and id " + newStream.getId() + ", because poolSize < startingSize (" + poolSize + " < " + startingSize + ")", Log.DEBUG);
                            }
                        }, () -> {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.getStreamFromPool -> Failed to create new stream", Log.ERROR);
                                Log.sendLogAsync();
                            }
                        });
                    } else if (usedStreams[0] * 100 / poolSize > threshold) {
                        stream.create(params, newStream -> {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.getStreamFromPool -> Created new stream with name " + newStream.getName() + " and id " + newStream.getId() + ", because " + (usedStreams[0] * 100 / poolSize) + "% of usedStreams > threshold (" + threshold + "%)", Log.DEBUG);
                            }
                        }, () -> {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.getStreamFromPool -> Failed to create new stream", Log.ERROR);
                                Log.sendLogAsync();
                            }
                        });
                    }
                }, streamId -> {
                    // the stream is not stopped
                    if (isVerboseLog()) {
                        Log.p("WowzaLiveStream.getStreamFromPool -> The stream with id " + streamId + " is not stopped.", Log.DEBUG);
                    }
                    usedStreams[0] = usedStreams[0] + 1; // we count the used streams
                    indexId[0] = indexId[0] + 1; // we increase the index of the pool
                    if (indexId[0] >= poolSize) {
                        // there are no stopped stream, so we try to create a new one
                        WowzaLiveStream stream = new WowzaLiveStream();
                        OnComplete<WowzaLiveStream> newStreamCreated = (WowzaLiveStream v) -> {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.getStreamFromPool -> Created new stream with name " + v.getName() + " and id " + v.getId() + ", because there are no stopped streams", Log.DEBUG);
                            }
                            onComplete.completed(v.getId());
                        };
                        Runnable newStreamCreationFailed = () -> {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.getStreamFromPool -> Failed to create a new live stream", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        };
                        stream.create(params, newStreamCreated, newStreamCreationFailed);
                    } else {
                        // we recursively run the WowzaLiveStream.fetchState for the next stream
                        if (stateOfStream[0] != null) {
                            stateOfStream[0].run();
                        }
                    }
                }, () -> {
                    // server error
                    if (isVerboseLog()) {
                        Log.p("WowzaLiveStream.getStreamFromPool -> Server error trying to get the state of stream with id " + ids.get(indexId[0]), Log.ERROR);
                        Log.sendLogAsync();
                    }
                    onFail.run();
                });
            };
            if (stateOfStream[0] != null) {
                stateOfStream[0].run();
            }
        }, () -> {
            // failure
            if (isVerboseLog()) {
                Log.p("WowzaLiveStream.getStreamFromPool -> Failed to get all stream ids", Log.ERROR);
                Log.sendLogAsync();
            }
            onFail.run();
        });
    }

    /**
     * <p>
     * (Async) Starts a stream and wait that the starting is completed before
     * calling the callback "isStarted".</p>
     *
     * <p>
     * After a default timeout of 120 seconds, if the starting is not completed
     * then the onFail callback is called. You can customize the timeout.</p>
     *
     * @param id of the stream
     * @param isStarted is a callback called when the starting is completed
     * @param onFail is a callback for failure
     * @param timeout (seconds) null to use the default timeout, or any other
     * value &gt;= 30 to set a custom timeout.
     */
    public static void start(String id, OnComplete<String> isStarted, Runnable onFail, Integer timeout) {
        if (id == null) {
            throw new IllegalStateException("WowzaLiveStreaming.start -> id of the stream cannot be null");
        }
        if (timeout != null && timeout < 30) {
            throw new IllegalStateException("WowzaLiveStreaming.start -> timeout must be >= 30 or null to use the default timeout");
        }

        putRequest("live_streams/" + id + "/start")
                .onErrorCodeString(new ErrorCodeHandler<String>() {
                    @Override
                    public void onError(Response<String> v) {
                        if (v.getResponseCode() == 401) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.start -> (Code 401) Unauthorized, failed to get the state of stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.start -> Unknow response with code " + v.getResponseCode() + ", failed to get the state of the stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
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
                        Map response = (Map) responseData.get("live_stream");
                        String state = (String) response.get("state");
                        if ("starting".equals(state)) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.start -> (Code 200) Successfully get the state " + state + " for the stream with id " + id, Log.DEBUG);
                            }
                            int maxTime;
                            if (timeout != null) {
                                maxTime = timeout * 1000;
                            } else {
                                maxTime = 120 * 1000;
                            }
                            long startingTime = System.currentTimeMillis();
                            Runnable[] checking = {null};
                            checking[0] = () -> {
                                // check the timeout
                                if (System.currentTimeMillis() - startingTime > maxTime) {
                                    if (isVerboseLog()) {
                                        Log.p("WowzaLiveStream.start -> Timeout reached while starting the stream with id " + id, Log.ERROR);
                                        Log.sendLogAsync();
                                    }
                                    onFail.run();
                                } else {
                                    fetchState(id, myId -> {
                                        // the stream is started
                                        if (isVerboseLog()) {
                                            Log.p("WowzaLiveStream.start -> Successfully started the stream with id " + id + " after " + ((System.currentTimeMillis() - startingTime) / 1000) + " seconds", Log.DEBUG);
                                        }
                                        isStarted.completed(id);
                                    }, myId -> {
                                        // the stream is starting
                                        TimerTask task = new TimerTask() {
                                            @Override
                                            public void run() {
                                                if (checking[0] != null) {
                                                    checking[0].run();
                                                }
                                            }
                                        };
                                        Timer timer = new Timer();
                                        timer.schedule(task, 2000);
                                    }, myId -> {
                                        // other state
                                        if (isVerboseLog()) {
                                            Log.p("WowzaLiveStream.start -> Invalid state after starting the stream with id " + id, Log.ERROR);
                                            Log.sendLogAsync();
                                        }
                                        onFail.run();
                                    }, onFail);
                                }
                            };
                            if (checking[0] != null) {
                                checking[0].run();
                            }
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.start -> The state " + state + " for the stream with id " + id + " is not \"starting\", so the starting request is failed", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        }

                    }
                });
    }

    /**
     * <p>
     * (Async) Stops a stream.</p>
     *
     * @param id of the stream
     * @param isStopped is a callback for successfull stopping
     * @param onFail is a callback for failure
     */
    public static void stop(String id, OnComplete<String> isStopped, Runnable onFail) {
        if (id == null) {
            throw new IllegalStateException("WowzaLiveStreaming.stop -> id of the stream cannot be null");
        }

        putRequest("live_streams/" + id + "/stop")
                .onErrorCodeString(new ErrorCodeHandler<String>() {
                    @Override
                    public void onError(Response<String> v) {
                        if (v.getResponseCode() == 401) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.stop -> (Code 401) Unauthorized, failed to get the state of stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.stop -> Unknow response with code " + v.getResponseCode() + ", failed to get the state of the stream " + id + ", string returned by the server: \"" + v.getResponseData() + "\"", Log.ERROR);
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
                        Map response = (Map) responseData.get("live_stream");
                        String state = (String) response.get("state");
                        if ("stopped".equals(state)) {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.stop -> (Code 200) Successfully get the state " + state + " for the stream with id " + id, Log.DEBUG);
                            }
                            isStopped.completed(id);
                        } else {
                            if (isVerboseLog()) {
                                Log.p("WowzaLiveStream.start -> The state " + state + " for the stream with id " + id + " is not \"stopped\", so the stopping request is failed", Log.ERROR);
                                Log.sendLogAsync();
                            }
                            onFail.run();
                        }
                    }
                });
    }

    /**
     * Enables or disables the hmac authentication; by default it's disabled.
     *
     * @param hmacAuthentication true to enable or false to disable
     */
    public static void setHmacAuthentication(boolean hmacAuthentication) {
        WowzaLiveStream.hmacAuthentication = hmacAuthentication;
    }

}
