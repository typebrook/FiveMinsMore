package io.typebrook.fiveminsmore.utils;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

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

    public static LatLng location2LatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    public static List<LatLng> locations2LatLngs(List<Location> locs) {
        if (locs.isEmpty()) {
            throw new EmptyStackException();
        }

        List<LatLng> latLngs = new ArrayList<>();

        for (Location loc : locs) {
            latLngs.add(location2LatLng(loc));
        }
        return latLngs;
    }
}
