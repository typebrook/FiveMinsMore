package io.typebrook.fiveminsmore.utils;

import java.io.File;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

/*
* Recursively scan files in directory into media database
*/

public class MediaScanner {

    private static final String TAG = MediaScanner.class.getSimpleName();

    private MediaScannerConnection mConn = null;
    private ScannerClient mClient = null;
    private File mFile = null;
    private String mMimeType = null;

    public MediaScanner(Context context) {
        if (mClient == null) {
            mClient = new ScannerClient();
        }
        if (mConn == null) {
            mConn = new MediaScannerConnection(context, mClient);
        }
    }

    class ScannerClient implements
            MediaScannerConnection.MediaScannerConnectionClient {

        public void onMediaScannerConnected() {

            if (mFile == null) {
                return;
            }
            scan(mFile, mMimeType);
        }

        public void onScanCompleted(String path, Uri uri) {
            mConn.disconnect();
        }

        private void scan(File file, String type) {
            Log.i(TAG, "scan " + file.getAbsolutePath());
            if (file.isFile()) {
                mConn.scanFile(file.getAbsolutePath(), null);
                return;
            }
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : file.listFiles()) {
                scan(f, type);
            }
        }
    }

    public void scanFile(File file, String mimeType) {
        mFile = file;
        mMimeType = mimeType;
        mConn.connect();
    }

}
