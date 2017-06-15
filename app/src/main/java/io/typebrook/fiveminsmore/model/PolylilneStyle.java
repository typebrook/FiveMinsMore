package io.typebrook.fiveminsmore.model;

import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.PolylineOptions;

import io.typebrook.fiveminsmore.Constant;
import io.typebrook.fiveminsmore.R;
import io.typebrook.fiveminsmore.utils.MapUtils;

import static io.typebrook.fiveminsmore.Constant.ZINDEX_POLYLINE;

/**
 * Created by pham on 2017/5/23.
 */

public class PolylilneStyle {
    // 在GpxManager中的航跡樣式
    public static final PolylineOptions getDefaultStyle() {
        return new PolylineOptions()
                .color(Constant.DEFAULT_TRACK_COLOR)
                .startCap(new CustomCap(BitmapDescriptorFactory.fromResource(
                        R.drawable.ic_arrowhead_white), 10))
                .endCap(new CustomCap(BitmapDescriptorFactory.fromResource(
                        R.drawable.ic_publish_white_24dp), 5))
                .jointType(JointType.ROUND)
                .zIndex(ZINDEX_POLYLINE);
    }

    // 紀錄時的航跡樣式
    public static final PolylineOptions getTrackingStyle() {
        return new PolylineOptions()
                .color(Color.BLUE)
                .zIndex(ZINDEX_POLYLINE);
    }
}
