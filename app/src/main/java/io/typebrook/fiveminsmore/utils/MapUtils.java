package io.typebrook.fiveminsmore.utils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import io.typebrook.fiveminsmore.model.CustomMarker;

/**
 * Created by pham on 2017/5/9.
 */

public class MapUtils {
    public static void zoomToPolyline(GoogleMap map, Polyline p) {
        LatLngBounds.Builder builder = LatLngBounds.builder();

        for (LatLng latLng : p.getPoints()) {
            builder.include(latLng);
        }
        final LatLngBounds bounds = builder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    public static void zoomToMarker(GoogleMap map, CustomMarker m) {
        final LatLngBounds bounds = new LatLngBounds(m.getPosition(), m.getPosition());

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 400));
    }
}
