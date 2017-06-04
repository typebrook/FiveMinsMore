package io.typebrook.fiveminsmore.res;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.typebrook.fiveminsmore.utils.MediaScanner;

/**
 * Created by pham on 2017/5/22.
 * 紀錄其它地圖軟體的外部資料夾
 * 未來應該用外部檔案取代，讓使用者可以自己編輯
 */

public class OtherAppPaths {
    static final String PATH_DOWNLOAD = Environment.getExternalStorageDirectory() + "/Download";
    static final String PATH_GTS = Environment.getExternalStorageDirectory() + "/GTs/gpx";
    static final String PATH_ORUXMAP = Environment.getExternalStorageDirectory() + "/oruxmaps/tracklogs";
    static final String PATH_LOCUS = Environment.getExternalStorageDirectory() + "/Locus/export";

    public static List<String >getList(){
        List<String> list = new ArrayList<>();
        list.add(PATH_DOWNLOAD);
        list.add(PATH_GTS);
        list.add(PATH_ORUXMAP);
        list.add(PATH_LOCUS);
        return list;
    }

    public static void checkMediaDatabase(Context context){
        String mimeType = "application/gpx+xml";
        for (String path : getList()) {
            new MediaScanner(context).scanFile(new File(path), mimeType);
        }
    }
}