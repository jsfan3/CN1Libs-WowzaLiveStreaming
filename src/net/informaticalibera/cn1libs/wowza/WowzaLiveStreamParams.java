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

import com.codename1.properties.BooleanProperty;
import com.codename1.properties.IntProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

/**
 * Parameters used to create a WowzaLiveStream object, all properties (except
 * "name") have a default value; see
 * <a href="https://www.wowza.com/docs/how-to-broadcast-a-live-stream-by-using-the-wowza-streaming-cloud-rest-api#paramIngest" target="noframe">Ingest
 * parameters</a> for more info.
 *
 * @author Francesco Galgani 
 */
public class WowzaLiveStreamParams implements PropertyBusinessObject {

    public final IntProperty<WowzaLiveStreamParams> aspect_ratio_height = new IntProperty<>("aspect_ratio_height", 1080);
    public final IntProperty<WowzaLiveStreamParams> aspect_ratio_width = new IntProperty<>("aspect_ratio_width", 1920);
    public final Property<String, WowzaLiveStreamParams> broadcast_location = new Property<>("broadcast_location", "eu_germany");
    public final Property<String, WowzaLiveStreamParams> encoder = new Property<>("encoder", "wowza_gocoder");
    public final Property<String, WowzaLiveStreamParams> name = new Property<>("name", null);
    public final Property<String, WowzaLiveStreamParams> transcoder_type = new Property<>("transcoder_type", "transcoded");
    public final BooleanProperty<WowzaLiveStreamParams> disable_authentication = new BooleanProperty<>("disable_authentication", false);
    public final BooleanProperty<WowzaLiveStreamParams> recording = new BooleanProperty<>("recording", true);

    public final PropertyIndex idx = new PropertyIndex(this, "WowzaLiveStreamParams",
            aspect_ratio_height, aspect_ratio_width, broadcast_location,
            encoder, name, transcoder_type, disable_authentication, recording
    );

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }

    public WowzaLiveStreamParams() {
        int quality = GoCoder.getQuality();
        switch (quality) {
            case (GoCoder.LOW_QUALITY_360p):
                aspect_ratio_width.set(640);
                aspect_ratio_height.set(360);
                break;
            case (GoCoder.MEDIUM_QUALITY_720p):
                aspect_ratio_width.set(1280);
                aspect_ratio_height.set(720);
                break;
            case (GoCoder.HIGH_QUALITY_1080p):
                aspect_ratio_width.set(1920);
                aspect_ratio_height.set(1080);
                break;
        }
    }

}
