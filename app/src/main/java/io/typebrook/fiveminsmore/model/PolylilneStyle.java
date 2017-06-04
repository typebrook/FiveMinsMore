package io.typebrook.fiveminsmore.model;

import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.PolylineOptions;

import io.typebrook.fiveminsmore.R;
import io.typebrook.fiveminsmore.utils.MapUtils;

/**
 * Created by pham on 2017/5/23.
 */

public class PolylilneStyle {
    public static final PolylineOptions STYLE_IN_MANAGER = new PolylineOptions()
            .color(Color.RED)
            .startCap(new CustomCap(BitmapDescriptorFactory.fromResource(
                    R.drawable.ic_start_point_24dp), 10))
            .endCap(new CustomCap(BitmapDescriptorFactory.fromResource(
                    R.drawable.ic_arrowhead_white), 5))
            .jointType(JointType.ROUND)
            .zIndex(5);

    // 紀錄時的航跡樣式
    public static final PolylineOptions STYLE_WHILE_TRACKING = new PolylineOptions()
            .color(Color.BLUE)
            .zIndex(5);
}
