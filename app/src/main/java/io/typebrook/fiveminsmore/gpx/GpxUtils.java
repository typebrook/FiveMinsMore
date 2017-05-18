package io.typebrook.fiveminsmore.gpx;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jamesmurty.utils.XMLBuilder2;
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
import io.typebrook.fiveminsmore.R;
import io.typebrook.fiveminsmore.model.CustomMarker;

/**
 * Created by pham on 2017/4/9.
 */

public class GpxUtils {
    private final static String TAG = "GpxUtils";

    // 讀入GPX檔
    public static Gpx parseGpx(InputStream in) {
        GPXParser mParser = new GPXParser();

        try {
            Gpx gpx = mParser.parse(in);
            Log.d(TAG, "parseGpx: parsed succeed");
            return gpx;
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            Log.d(TAG, "parseGpx: parsed failed");
            return null;
        }
    }

    // 取得PolyLine設定值，以便將航跡畫在地圖上
    static PolylineOptions trk2TrkOpts(Track trk) {
        List<TrackPoint> trkPts = new ArrayList<>();

        for (TrackSegment seg : trk.getTrackSegments()) {
            trkPts.addAll(seg.getTrackPoints());
        }

        PolylineOptions pos = new PolylineOptions();

        for (TrackPoint trkPt : trkPts) {
            LatLng latLng = new LatLng(trkPt.getLatitude(), trkPt.getLongitude());
            pos.add(latLng);
        }

        return pos
                .color(Color.RED)
                .startCap(new CustomCap(BitmapDescriptorFactory.fromResource(
                        R.drawable.ic_start_point_24dp), 10))
                .endCap(new CustomCap(BitmapDescriptorFactory.fromResource(
                        R.drawable.ic_arrowhead_white), 5))
                .jointType(JointType.ROUND)
                .zIndex(5);
    }

    // 將航點畫在地圖上
    static CustomMarker waypt2Marker(WayPoint wpt) {
        String name = wpt.getName();
        LatLng latLng = new LatLng(wpt.getLatitude(), wpt.getLongitude());
        String lat = String.format(Locale.getDefault(), "%.6f", latLng.latitude);
        String lon = String.format(Locale.getDefault(), "%.6f", latLng.longitude);
        String snippet = "北緯" + lat + "度，東經" + lon + "度";

        return new CustomMarker(latLng, name, snippet);
    }

    public static TreeNode getTreeNode(String filename, Gpx gpx) {
        GpxHolder.GpxTreeItem.Builder gpx_builder = new GpxHolder.GpxTreeItem.Builder();
        TreeNode gpxRoot = new TreeNode(gpx_builder
                .setType(GpxHolder.ITEM_TYPE_GPX)
                .setIcon(GpxHolder.ITEM_ICON_GPX)
                .setText(filename)
                .setGpx(gpx)
                .build());

        for (Track trk : gpx.getTracks()) {
            GpxHolder.GpxTreeItem.Builder trk_builder = new GpxHolder.GpxTreeItem.Builder();
            TreeNode trkNode = new TreeNode(trk_builder
                    .setType(GpxHolder.ITEM_TYPE_TRACK)
                    .setIcon(GpxHolder.ITEM_ICON_TRACK)
                    .setText(trk.getTrackName())
                    .setTrkOpts(trk)
                    .build());
            gpxRoot.addChildren(trkNode);
        }

        for (WayPoint wpt : gpx.getWayPoints()) {
            GpxHolder.GpxTreeItem.Builder wpt_builder = new GpxHolder.GpxTreeItem.Builder();
            TreeNode wptNode = new TreeNode(wpt_builder
                    .setType(GpxHolder.ITEM_TYPE_WAYPOINT)
                    .setIcon(GpxHolder.ITEM_ICON_WAYPOINT)
                    .setText(wpt.getName())
                    .setMarker(wpt)
                    .build());
            gpxRoot.addChildren(wptNode);
        }

        return gpxRoot;
    }

    public static String polyline2Xml(List<CustomMarker> pts) {
        XMLBuilder2 builder = XMLBuilder2.create("gpx");
        builder.a("version", "1.1")
                .a("creator", "https://github.com/typebrook/FiveMinsMore")
                .a("xmlns", "http://www.topografix.com/GPX/1/1")
                .a("xsi:schemaLocation", "http://www.topografix.com/GPX/1/0 " +
                        "http://www.topografix.com/GPX/1/0/gpx.xsd");

        for (CustomMarker pt : pts) {

        }

        return builder.asString();
    }

}
