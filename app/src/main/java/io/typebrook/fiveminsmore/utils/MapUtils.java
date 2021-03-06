package io.typebrook.fiveminsmore.utils;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

import io.typebrook.fiveminsmore.Cluster.CustomMarker;

import static io.typebrook.fiveminsmore.Constant.STARTING_ZOOM;
import static io.typebrook.fiveminsmore.Constant.TAIWAN_BOUNDARY;
import static io.typebrook.fiveminsmore.Constant.TAIWAN_CENTER;
import static io.typebrook.fiveminsmore.Constant.TAIWAN_ZOOM_MAX;
import static io.typebrook.fiveminsmore.Constant.TAIWAN_ZOOM_MIN;

/**
 * Created by pham on 2017/5/9.
 * Some convenient ways to manipulate map
 */

public class MapUtils {
    // 設定鏡頭可動範圍
    public static void setTaiwanBoundaries(GoogleMap map) {
        map.setMinZoomPreference(TAIWAN_ZOOM_MIN);
        map.setMaxZoomPreference(TAIWAN_ZOOM_MAX);
        map.setLatLngBoundsForCameraTarget(TAIWAN_BOUNDARY);
    }

    public static void zoomToPolyline(GoogleMap map, Polyline p) {
        if (p == null || p.getPoints().isEmpty())
            return;

        LatLngBounds.Builder builder = LatLngBounds.builder();

        for (LatLng latLng : p.getPoints()) {
            builder.include(latLng);
        }
        final LatLngBounds bounds = builder.build();

        try{
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void zoomToMarker(GoogleMap map, CustomMarker m) {
        final LatLngBounds bounds = new LatLngBounds(m.getPosition(), m.getPosition());

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 400));
    }

    public static LatLng loc2LatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    public static List<LatLng> locs2LatLngs(List<Location> locs) {
        List<LatLng> latLngs = new ArrayList<>();

        for (Location loc : locs) {
            latLngs.add(loc2LatLng(loc));
        }
        return latLngs;
    }

    public static LatLngBounds getMapBounds(GoogleMap map){
        return map.getProjection().getVisibleRegion().latLngBounds;
    }
}
