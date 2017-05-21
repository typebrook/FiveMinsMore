package io.typebrook.fiveminsmore;

import android.os.Environment;

/**
 * Created by pham on 2017/5/3.
 *
 * Constant
 */

public class Constant {
    // Code for filepicker package
    static final int REQUEST_CODE_PICK_GPX_FILE = 0x500;
    static final int REQUEST_CODE_PICK_MAPSFORGE_FILE = 0x600;
    static final int REQUEST_CODE_PICK_KML_FILE = 0x700;

    // TODO need to delete
    public static final String DEFAULT_THEME_PATH =
            Environment.getExternalStorageDirectory().getPath() + "/GTs/mapthemes/MOI_OSM.xml";

    // Z-Index for different layer
    static final int ZINDEX_BASPMAP = -10;

    // Interval for tracking
    static final int INTERVAL_TRACKING = 5;
    static int INTERVAL_BETWEEN_TRKPTS = -1;
}
