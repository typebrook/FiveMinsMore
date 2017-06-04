package io.typebrook.fiveminsmore;

import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.File;

/**
 * Created by pham on 2017/5/3.
 *
 * Constant
 */

public class Constant {
    // Paths of this APP
    public static final File DIR_EXTERNAL = new File(Environment.getExternalStorageDirectory() + "/5MinsMore");
    public static final File DIR_GPX_FILE = new File(DIR_EXTERNAL.getPath() + "/gpx");

    // Boundaries of TAIWAN
    public static final LatLng TAIWAN_CENTER = new LatLng(23.76, 120.96);

    // ZOOM restriction of TAIWAN
    public static final int TAIWAN_ZOOM_MIN = 7;
    public static final int TAIWAN_ZOOM_MAX = 20;
    // ZOOM Level when creating map
    public static final int STARTING_ZOOM = 7;

    // Code for filepicker package
    static final int REQUEST_CODE_PICK_GPX_FILE = 0x500;
    static final int REQUEST_CODE_PICK_MAPSFORGE_FILE = 0x600;
    static final int REQUEST_CODE_PICK_KML_FILE = 0x700;

    // TODO need to delete
    public static final String DEFAULT_THEME_PATH =
            Environment.getExternalStorageDirectory().getPath() + "/GTs/mapthemes/MOI_OSM.xml";

    // Z-Index for different basemap
    public static final int ZINDEX_BASEMAP = -10;

    // Interval for tracking
    static final int TIME_INTERVAL_FOR_TRACKING = 5;
    static int DISTANCE_INTERVAL_FOR_TRKPTS = 10;
}
