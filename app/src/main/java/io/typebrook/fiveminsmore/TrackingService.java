package io.typebrook.fiveminsmore;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.typebrook.fiveminsmore.Constant.DISTANCE_INTERVAL_FOR_TRKPTS;

/**
 * Created by pham on 2017/4/15.
 * Service for Tracking where you go, even when you wipe app out.
 */

public class TrackingService extends Service {
    private static final String TAG = "TrackingService";
    private static final int ID_NOTIFICATION = 3857;

    private TrackingBinder mBinder = new TrackingBinder();

    private Location mLastPosition;
    List<Location> mTrkpts = new ArrayList<>();

    // 紀錄api呼叫Service的次數和已收到的位置資料數目
    private int count = 0;
    private int succeed = 0;
    private long startTime = new Date().getTime();

    @Override
    public void onCreate() {
        // 開始紀錄航跡時，顯示notification，點擊即可開啟MapsActivity
        Intent openIntent = new Intent(this, MapsActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openIntent, 0);

        // notification內容為現在時間
        Date date = new Date();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("紀錄航跡中")
                .setContentText("" + date)
                .setSmallIcon(R.drawable.ic_five_mins_more)
                .setOngoing(true)
                .setContentIntent(contentIntent);
        startForeground(ID_NOTIFICATION , notificationBuilder.build());
    }

    @WorkerThread
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        count++;
        if (LocationResult.hasResult(intent)) {
            // 從fusedLocationApi取得位置資料
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                succeed++;

                // 若小於最小間距，則不紀錄該航跡
                if (mTrkpts.size() > 1) {
                    double interval = SphericalUtil.computeDistanceBetween(
                            new LatLng(location.getLatitude(), location.getLongitude()),
                            new LatLng(mLastPosition.getLatitude(), mLastPosition.getLongitude()));
                    if (interval < DISTANCE_INTERVAL_FOR_TRKPTS)
                        return START_STICKY;
                }

                mTrkpts.add(location);
                mLastPosition = location;

                // Send Location Update to Activity
                if (callBack != null)
                    callBack.getServiceData(location);
            }
        }

        // Message for testing
        Log.d(TAG, "Record Times: " + count + ", Succeed times: " + succeed +
                ",  Points number: " + mTrkpts.size()
                + ", Time from start: " + (new Date().getTime() - startTime) / 1000);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getBaseContext(), "結束紀錄航跡", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class TrackingBinder extends Binder {
        TrackingService getService() {
            return TrackingService.this;
        }

        List<Location> getTrkpts() {
            Log.d("TAG", "getTrkpts() executed");
            return mTrkpts;
        }
    }

    // Callback implemented by MapsActivity
    private CallBack callBack = null;
    interface CallBack {
        void getServiceData(Location location);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }
}
