package io.typebrook.fiveminsmore.draw;

import android.content.Context;
import android.graphics.Color;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import io.typebrook.fiveminsmore.model.CustomMarker;

/**
 * Created by pham on 2017/5/8.
 */

public class DrawUtils implements
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener {
    private GoogleMap map;

    private static PolylineOptions opts = new PolylineOptions()
            .width(5)
            .color(Color.YELLOW)
            .zIndex(200);
    private static Polyline polyline;

    public DrawUtils(GoogleMap map) {
        this.map = map;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (polyline != null)
            polyline.remove();
        polyline = map.addPolyline(opts.add(latLng));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    public static List<LatLng> endDraw() {
        if (polyline == null)
            return null;

        List<LatLng> pts = polyline.getPoints();
        polyline.remove();
        opts = new PolylineOptions()
                .width(5)
                .color(Color.YELLOW)
                .zIndex(200);

        return pts;
    }
}
