package io.typebrook.fiveminsmore.model;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import io.typebrook.fiveminsmore.R;

/**
 * Created by pham on 2017/5/23.
 */

public class TrackPointsStyle {
    public static final MarkerOptions TRKPTS_STYLE = new MarkerOptions()
            .zIndex(5)
            .anchor(0.5f, 0.5f)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_trkpt));
}
