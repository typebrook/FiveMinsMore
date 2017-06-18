package io.typebrook.fiveminsmore;

import android.graphics.Color;
import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.File;

/**
 * Created by pham on 2017/5/3.
 * <p>
 * Constant
 */

public class Constant {
    // Paths of this APP
    public static final File DIR_EXTERNAL = new File(Environment.getExternalStorageDirectory() + "/5MinsMore");
    public static final File DIR_GPX_FILE = new File(DIR_EXTERNAL.getPath() + "/gpx");


    // mapsforge suffix
    public static final String SUFFIX_MAPSFORGE = "map";
    public static final String SUFFIX_THEME = "xml";
    public static final String SUFFIX_GPX = "gpx";
    public static final String SUFFIX_KML = "kml";

    // Center and Boundaries of TAIWAN
    public static final LatLng TAIWAN_CENTER = new LatLng(23.76, 120.96);
    public static final LatLngBounds TAIWAN_BOUNDARY = new LatLngBounds(
            new LatLng(20, 119), new LatLng(26.5, 123));

    // ZOOM restriction of TAIWAN
    public static final int TAIWAN_ZOOM_MIN = 7;
    public static final int TAIWAN_ZOOM_MAX = 20;
    // ZOOM Level when creating map
    public static final int STARTING_ZOOM = 7;

    // 座標系表示方式
    public static final int COOR_WGS84_D = 0;
    public static final int COOR_WGS84_DMS = 1;
    public static final int COOR_TWD97 = 2;
    public static final int COOR_TWD67 = 3;
    // Coordinate Presentation
    public static final CharSequence[] COOR_METHODS = {
            "經緯度(度)",
            "經緯度(度分秒)",
            "TWD97(二度分帶)",
            "TWD67(二度分帶)"
    };

    // Code for filepicker package
    public static final int REQUEST_CODE_PICK_GPX_FILE = 0x500;
    public static final int REQUEST_CODE_PICK_MAPSFORGE_FILE = 0x600;
    public static final int REQUEST_CODE_PICK_MAPSFORGE_THEME_FILE = 0x700;
    //    public static final int REQUEST_CODE_PICK_KML_FILE = 0x800;
    public static final int REQUEST_CODE_PICK_POI_FILE = 0x900;

    // TODO need to delete
    public static final String DEFAULT_THEME_PATH =
            Environment.getExternalStorageDirectory().getPath() + "/GTs/mapthemes/MOI_OSM.xml";

    // Z-Index for different overlay or tiles
    public static final int ZINDEX_BASEMAP = -10;
    public static final int ZINDEX_ADDTILE = 1;
    public static final int ZINDEX_POLYLINE = 5;
    public static final int ZINDEX_POLYLINE_CHOSEN = 6;

    // Interval for tracking
    static final int TIME_INTERVAL_FOR_TRACKING = 5;
    static int DISTANCE_INTERVAL_FOR_TRKPTS = 10;

    // Default Categories for POI
    public static final String[] POI_SEARCH_LIST = {
            "Natural",
            "Places",
            "Tourism"
    };

    // Color of Track
    public static final int DEFAULT_TRACK_COLOR = Color.RED;
    public static final int CHOSEN_TRACK_COLOR = Color.YELLOW;

}
