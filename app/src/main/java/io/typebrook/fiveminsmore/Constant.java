package io.typebrook.fiveminsmore;

import android.os.Environment;

/**
 * Created by pham on 2017/5/3.
 */

public class Constant {

    public static final int REQUEST_CODE_PICK_GPX_FILE = 0x500;
    public static final int REQUEST_CODE_PICK_MAPSFORGE_FILE = 0x600;
    public static final int REQUEST_CODE_PICK_KML_FILE = 0x700;

    public static final String TEST_THEME_PATH =
            Environment.getExternalStorageDirectory().getPath() + "/GTs/mapthemes/MOI_OSM.xml";


    // Test
    public static final String TEST_IMAGE_PATH =
            Environment.getExternalStorageDirectory().getPath()
                    + "/GTs/mapthemes/moiosm_res/";

    public static final String SRC_SVG = "wm_frg_yellow_diamond.svg";
    public static final String SRC_PNG = "ic_folder_black_24dp.png";


}
