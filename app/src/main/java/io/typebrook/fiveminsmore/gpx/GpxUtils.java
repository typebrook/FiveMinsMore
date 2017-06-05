package io.typebrook.fiveminsmore.gpx;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jamesmurty.utils.XMLBuilder2;
import com.unnamed.b.atv.model.TreeNode;

import org.joda.time.DateTime;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;
import io.typebrook.fiveminsmore.R;
import io.typebrook.fiveminsmore.model.CustomMarker;
import io.typebrook.fiveminsmore.utils.MapUtils;

import static io.typebrook.fiveminsmore.Constant.DIR_GPX_FILE;
import static io.typebrook.fiveminsmore.model.PolylilneStyle.STYLE_IN_MANAGER;

/**
 * Created by pham on 2017/4/9.
 * 處理有關GPX檔的功能
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

    public static TreeNode gpxFile2TreeNode(String filename, Gpx gpx) {
        GpxHolder.GpxTreeItem.Builder gpx_builder = new GpxHolder.GpxTreeItem.Builder();
        TreeNode gpxRoot = new TreeNode(gpx_builder
                .setType(GpxHolder.ITEM_TYPE_GPX)
                .setIcon(GpxHolder.ITEM_ICON_GPX)
                .setText(filename)
                .setGpx(gpx)
                .build());

        List<Track> tracks = gpx.getTracks();
        if (tracks.size() > 1) {
            TreeNode node_trks = new TreeNode(gpx_builder
                    .setType(GpxHolder.ITEM_TYPE_GPX)
                    .setIcon(GpxHolder.ITEM_ICON_TRACK)
                    .setText("航跡")
                    .build());
            gpxRoot.addChild(node_trks);
            addTrks2Node(node_trks, tracks);
        } else {
            addTrks2Node(gpxRoot, tracks);
        }

        List<WayPoint> wpts = gpx.getWayPoints();
        if (wpts.size() > 1) {
            TreeNode node_wpts = new TreeNode(gpx_builder
                    .setType(GpxHolder.ITEM_TYPE_GPX)
                    .setIcon(GpxHolder.ITEM_ICON_WAYPOINT)
                    .setText("航點")
                    .build());
            gpxRoot.addChild(node_wpts);
            addWpts2Node(node_wpts, wpts);
        } else {
            addWpts2Node(gpxRoot, wpts);
        }

        return gpxRoot;
    }

    private static void addTrks2Node(TreeNode node, List<Track> trks) {
        for (Track trk : trks) {
            GpxHolder.GpxTreeItem.Builder trk_builder = new GpxHolder.GpxTreeItem.Builder();
            TreeNode trkNode = new TreeNode(trk_builder
                    .setType(GpxHolder.ITEM_TYPE_TRACK)
                    .setIcon(GpxHolder.ITEM_ICON_TRACK)
                    .setText(trk.getTrackName())
                    .setTrkOpts(GpxUtils.trk2TrkOpts(trk))
                    .build());
            node.addChildren(trkNode);
        }
    }

    private static void addWpts2Node(TreeNode node, List<WayPoint> wpts) {
        for (WayPoint wpt : wpts) {
            GpxHolder.GpxTreeItem.Builder wpt_builder = new GpxHolder.GpxTreeItem.Builder();
            TreeNode wptNode = new TreeNode(wpt_builder
                    .setType(GpxHolder.ITEM_TYPE_WAYPOINT)
                    .setIcon(GpxHolder.ITEM_ICON_WAYPOINT)
                    .setText(wpt.getName())
                    .setMarker(wpt)
                    .build());
            node.addChildren(wptNode);
        }
    }

    public static TreeNode locs2TreeNode(String trkName, List<Location> locs) {
        GpxHolder.GpxTreeItem.Builder trk_builder = new GpxHolder.GpxTreeItem.Builder();

        PolylineOptions opts = STYLE_IN_MANAGER.addAll(MapUtils.locs2LatLngs(locs));

        return new TreeNode(trk_builder
                .setType(GpxHolder.ITEM_TYPE_TRACK)
                .setIcon(GpxHolder.ITEM_ICON_TRACK)
                .setText(trkName)
                .setTrkOpts(opts)
                .build());
    }

    // TODO A temporary method to generate gpx file, need to improve
    public static void polyline2Xml(Context context, String trkName, List<Location> pts) {
        if (pts.size() == 0 || !isExternalStorageWritable())
            return;
        XMLBuilder2 builder = XMLBuilder2.create("gpx");
        builder.a("version", "1.1")
                .a("creator", "https://github.com/typebrook/FiveMinsMore")
                .a("xmlns", "http://www.topografix.com/GPX/1/1")
                .a("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                .a("xsi:schemaLocation", "http://www.topografix.com/GPX/1/0 " +
                        "http://www.topografix.com/GPX/1/0/gpx.xsd");

        XMLBuilder2 trk = builder.e("trk");
        trk.e("name").t(trkName);

        XMLBuilder2 trkseg = trk.e("trkseg");

        for (Location pt : pts) {
            String lat = String.format(Locale.getDefault(), "%.6f", pt.getLatitude());
            String lon = String.format(Locale.getDefault(), "%.6f", pt.getLongitude());

            trkseg.e("trkpt").a("lat", lat).a("lon", lon)
                    .e("ele").t(String.format(Locale.getDefault(), "%.0f", pt.getAltitude())).up()
                    .e("time").t(new DateTime(pt.getTime()).toString());
            Log.d(TAG, new DateTime(pt.getTime()).toString());
        }

        // 若外部資料夾不存在，就建立新的
        if (!DIR_GPX_FILE.exists()) {
            boolean result = DIR_GPX_FILE.mkdirs();
            Log.d(TAG, "result of making dir " + DIR_GPX_FILE.getPath() + ": " + result);
        }

        try {
            String path = DIR_GPX_FILE.getPath() + "/" + trkName + ".gpx";
            PrintWriter writer = new PrintWriter(new FileOutputStream(path, true));

            Properties outputProperties = new Properties();
            // Explicitly identify the output as an XML document
            outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");
            // Pretty-print the XML output (doesn't work in all cases)
            outputProperties.put(javax.xml.transform.OutputKeys.INDENT, "yes");
            // Get 2-space indenting when using the Apache transformer
            outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");

            builder.toWriter(true, writer, outputProperties);

            // 將File加入android.providers.media 資料庫
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(new File(path));
            intent.setData(uri);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            Log.d(TAG, "Failed to save GPX file");
        }
    }

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
