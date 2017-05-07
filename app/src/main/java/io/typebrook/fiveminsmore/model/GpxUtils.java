package io.typebrook.fiveminsmore.model;

import android.graphics.Color;
import android.util.Log;

import io.typebrook.fiveminsmore.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.unnamed.b.atv.model.TreeNode;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;

import static io.typebrook.fiveminsmore.model.GpxHolder.ITEM_ICON_GPX;
import static io.typebrook.fiveminsmore.model.GpxHolder.ITEM_ICON_TRACK;
import static io.typebrook.fiveminsmore.model.GpxHolder.ITEM_ICON_WAYPOINT;
import static io.typebrook.fiveminsmore.model.GpxHolder.ITEM_TYPE_GPX;
import static io.typebrook.fiveminsmore.model.GpxHolder.ITEM_TYPE_TRACK;
import static io.typebrook.fiveminsmore.model.GpxHolder.ITEM_TYPE_WAYPOINT;

/**
 * Created by pham on 2017/4/9.
 */

public class GpxUtils {
    private final static String TAG = "GpxUtils";

    // 讀入GPX檔
    public static Gpx parseGpx(InputStream in) {
        Log.i("Inputstream", "parseGpx: parsing");
        GPXParser mParser = new GPXParser(); // consider injection
        try {
            Gpx gpx = mParser.parse(in);
            Log.d(TAG, "parseGpx: parsed succeed");
            return gpx;
        } catch (IOException | XmlPullParserException e) {
            // do something with this exception
            e.printStackTrace();
            Log.d(TAG, "parseGpx: parsed failed");
            return null;
        }
    }

    // 將航跡畫在地圖上(只取trk0, seg0)
    public static Polyline drawTrack(Track trk, GoogleMap map) {
        List<TrackPoint> trkPts = new ArrayList<>();

        for (TrackSegment seg : trk.getTrackSegments()) {
            trkPts.addAll(seg.getTrackPoints());
        }

        PolylineOptions pos = new PolylineOptions();

        LatLngBounds.Builder builder = LatLngBounds.builder();

        for (TrackPoint trkPt : trkPts) {
            LatLng latLng = new LatLng(trkPt.getLatitude(), trkPt.getLongitude());
            pos.add(latLng);
            builder.include(latLng);
        }

        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150), 1500, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });

        return map.addPolyline(pos
                .color(Color.RED)
                .startCap(new CustomCap(BitmapDescriptorFactory.fromResource(
                        R.drawable.ic_start_point_24dp), 10))
                .endCap(new CustomCap(BitmapDescriptorFactory.fromResource(
                        R.drawable.ic_arrowhead_white), 5))
                .jointType(JointType.ROUND)
                .zIndex(200));
    }

    // 將航點畫在地圖上
    public static CustomMarker drawWaypt(WayPoint wpt, ClusterManager<CustomMarker> manager) {
        String name = wpt.getName();
        LatLng latLng = new LatLng(wpt.getLatitude(), wpt.getLongitude());
        String lat = String.format(Locale.getDefault(), "%.6f", latLng.latitude);
        String lon = String.format(Locale.getDefault(), "%.6f", latLng.longitude);
        String snippet = "北緯" + lat + "度，東經" + lon + "度";

        CustomMarker newCustomMarker = new CustomMarker(latLng, name, snippet);
        manager.addItem(newCustomMarker);

        // Let the marker show on map instantly.
        manager.cluster();

        return newCustomMarker;
    }

    public static TreeNode getTreeNode(String filename, Gpx gpx) {
        GpxHolder.IconTreeItem.Builder gpx_builder = new GpxHolder.IconTreeItem.Builder();
        TreeNode gpxRoot = new TreeNode(gpx_builder
                .setType(ITEM_TYPE_GPX)
                .setIcon(ITEM_ICON_GPX)
                .setText(filename)
                .build());

        for (Track trk : gpx.getTracks()) {
            GpxHolder.IconTreeItem.Builder trk_builder = new GpxHolder.IconTreeItem.Builder();
            TreeNode trkNode = new TreeNode(trk_builder
                    .setType(ITEM_TYPE_TRACK)
                    .setIcon(ITEM_ICON_TRACK)
                    .setText(trk.getTrackName())
                    .setTrack(trk)
                    .build());
            gpxRoot.addChildren(trkNode);
        }

        for (WayPoint wpt : gpx.getWayPoints()) {
            GpxHolder.IconTreeItem.Builder wpt_builder = new GpxHolder.IconTreeItem.Builder();
            TreeNode wptNode = new TreeNode(wpt_builder
                    .setType(ITEM_TYPE_WAYPOINT)
                    .setIcon(ITEM_ICON_WAYPOINT)
                    .setText(wpt.getName())
                    .setWayPoint(wpt)
                    .build());
            gpxRoot.addChildren(wptNode);
        }

        return gpxRoot;
    }

}
