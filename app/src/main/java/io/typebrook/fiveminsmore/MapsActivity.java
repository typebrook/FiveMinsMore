package io.typebrook.fiveminsmore;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.data.kml.KmlLayer;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.filter.entity.NormalFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;
import io.typebrook.fiveminsmore.draw.DrawUtils;
import io.typebrook.fiveminsmore.draw.DrawingView;
import io.typebrook.fiveminsmore.filepicker.CustomFilePickActivity;
import io.typebrook.fiveminsmore.gpx.GpxHolder;
import io.typebrook.fiveminsmore.gpx.GpxUtils;
import io.typebrook.fiveminsmore.offlinetile.MapsForgeTilesProvider;
import io.typebrook.fiveminsmore.utils.MapUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static io.typebrook.fiveminsmore.Constant.REQUEST_CODE_PICK_GPX_FILE;
import static io.typebrook.fiveminsmore.Constant.REQUEST_CODE_PICK_KML_FILE;
import static io.typebrook.fiveminsmore.Constant.REQUEST_CODE_PICK_MAPSFORGE_FILE;
import static io.typebrook.fiveminsmore.MapsManager.MAP_CODE_MAIN;
import static io.typebrook.fiveminsmore.MapsManager.MAP_CODE_SUB;
import static io.typebrook.fiveminsmore.MapsManager.TRKPTS_STYLE;
import static io.typebrook.fiveminsmore.MapsManager.TRK_STYLE;

/*
* The Main Activity contains Google Map Fragment
* Most of works on map are done here.
*/
public class MapsActivity extends AppCompatActivity implements
        ServiceConnection,
        OnMapReadyCallback, Button.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = "MapsActivity";

    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3857;
    private final String PREFS_NAME = "PREFS_5MinsMore";

    // 地圖元件
    private GoogleMap mMap;
    private MapsManager mMapsManager;
    GpxManager mGpxManager;

    // Activity Layout
    ViewGroup layoutContainer;
    ActionBar actionBar;

    // Google API用戶端物件
    protected GoogleApiClient mGoogleApiClient;

    // Location請求物件
    private LocationRequest mLocationRequest;
    // 記錄目前最新的位置
    LatLng mCurrentLatLng;
    // 紀錄現在航跡
    private List<Location> mCurrentTrkpts = new ArrayList<>();
    private Polyline mCurrentTrack;
    private List<Polyline> mMyTracks = new ArrayList<>();
    private boolean isTracking = false;

    public static final String LOCATION_UPDATE = "io.typebrook.fiveminsmore.LOCATION_UPDATE";

    private BroadcastReceiver mBroadcast = new BroadcastReceiver() {

        @Override
        public void onReceive(Context mContext, Intent mIntent) {
            updateTracking(false);
        }
    };
    // 按鈕群組
    private List<View> mBtnsSet = new ArrayList<>();
    // 主畫面按鈕
    private Button mSwitchBtn;
    private Button mTrackingBtn;
    private Button mPickTilesBtn;
    private Button mGpxManagerBtn;
    private Button mHelpBtn;
    private ImageButton mTopMapBtn;
    private ImageButton mBottomMapBtn;
    // 準心十字
    private List<ImageView> mCrossSet = new ArrayList<>();

    private boolean showCross = true;
    // 其它Fragment
    MapFragment mSubMapFragment;
    ReadFragment readFragment;
    private int mFragmentsNumber = 0;
    private static final String SUBMAP_FRAGMENT_TAG = "map";
    private static final String READ_FRAGMENT_TAG = "read";

    // TODO need to implement corresponding functions
    KmlLayer kmlLayer;
    int indexOfPad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 取得地圖物件
        setContentView(R.layout.activity_maps);
        layoutContainer = (ViewGroup) findViewById(R.id.layout_container);
        // Obtain the MapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // For tracking, Create an instance of GoogleAPIClient and LocationRequest.
        configGoogleApiClient();
        configLocationRequest();

        // Buttons on map
        mSwitchBtn = (Button) findViewById(R.id.btn_switch);
        mTrackingBtn = (Button) findViewById(R.id.btn_tracking);
        mPickTilesBtn = (Button) findViewById(R.id.btn_pick_tiles);
        mGpxManagerBtn = (Button) findViewById(R.id.btn_gpx_files_list);
        mHelpBtn = (Button) findViewById(R.id.btn_help);

        mBtnsSet.add(mPickTilesBtn);
        mBtnsSet.add(mTrackingBtn);
        mBtnsSet.add(mGpxManagerBtn);
        mBtnsSet.add(mHelpBtn);

        mSwitchBtn.setOnClickListener(this);
        for (View btn : mBtnsSet) {
            btn.setOnClickListener(this);
        }

        // Add center cross into main map
        mCrossSet.add(MAP_CODE_MAIN, (ImageView) findViewById(R.id.cross));

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        // 檢查是否紀錄航跡中
        isTracking = prefs.getBoolean("isTracking", false);

        if (isTracking) {
            // bind TrackingService
            Intent bindIntent = new Intent(this, TrackingService.class);
            bindService(bindIntent, this, BIND_AUTO_CREATE);
        }

        // Test
        actionBar = getSupportActionBar();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;
        mMapsManager = new MapsManager(this, map);
        mGpxManager = new GpxManager(this);

        // Check whether this app could get location
        if (checkLocationPermission())
            map.setMyLocationEnabled(true);
        else
            askPermission();

        // Set the boundaries of Taiwan, and set other view by using onCameraMove().
        mMapsManager.setTaiwanBoundaries();
        mMapsManager.onCameraMove();

        // TODO add blue dot beam to indicate user direction
    }

    // Button functions on map
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // Hide all things except map
            case R.id.btn_switch:
                v.setSelected(!v.isSelected());
                ((Button) v).setText(v.isSelected() ? "顯示" : "隱藏");
                int visibility = v.isSelected() ? View.INVISIBLE : View.VISIBLE;

                for (View btn : mBtnsSet) {
                    btn.setVisibility(visibility);
                }

                mMap.getUiSettings().setZoomControlsEnabled(visibility == 0);
                mMap.getUiSettings().setMyLocationButtonEnabled(visibility == 0);
                if (mMapsManager.getMapsNum() > 1)
                    mMapsManager.getMap(MAP_CODE_SUB).getUiSettings().setZoomControlsEnabled(visibility == 0);

                findViewById(R.id.zoom_number).setVisibility(visibility);

                if (v.isSelected())
                    actionBar.hide();
                else
                    actionBar.show();

                break;

            case R.id.btn_pick_tiles:
                mMapsManager.setTileOverlay();
                break;

            case R.id.btn_tracking:
                isTracking = !isTracking;

                if (isTracking) {
                    // 清空目前航跡點
                    mCurrentTrkpts = new ArrayList<>();
                    if (!mMyTracks.isEmpty())
                        mCurrentTrack.remove();

                    // 將按鈕顏色變紅
                    mTrackingBtn.setTextColor(Color.RED);

                    // 使用FusedLocationApi持續取得位置
                    requestLocationUpdates();

                    // 更新紀錄中的航跡
                    updateTracking(true);
                } else {
                    // 將按鈕顏色變黑
                    mTrackingBtn.setTextColor(Color.BLACK);

                    // 移除FusedLocationApi
                    removeLocationUpdates();
                }
                mTrackingBtn.setSelected(isTracking);
                break;

            case R.id.btn_gpx_files_list:
                mGpxManager.showDialog();
                this.onClick(mSwitchBtn);
                mSwitchBtn.setVisibility(View.INVISIBLE);
                break;

            case R.id.leave_gpx_manager:
                mGpxManager.removeDialog();
                this.onClick(mSwitchBtn);
                mSwitchBtn.setVisibility(View.VISIBLE);
                break;

            // See the usage: https://github.com/fishwjy/MultiType-FilePicker
            case R.id.btn_pick_gpx_files:
                Intent pickGpxIntent = new Intent(this, CustomFilePickActivity.class);
                pickGpxIntent.putExtra(NormalFilePickActivity.SUFFIX, new String[]{"gpx"});
                startActivityForResult(pickGpxIntent, REQUEST_CODE_PICK_GPX_FILE);
                break;

            case R.id.btn_map_top:
            case R.id.btn_map_bottom:
                mBottomMapBtn.setSelected(!mBottomMapBtn.isSelected());
                mTopMapBtn.setSelected(!mTopMapBtn.isSelected());

                if (mBottomMapBtn.isSelected())
                    mMapsManager.setCurrentMap(MAP_CODE_MAIN);
                else
                    mMapsManager.setCurrentMap(MAP_CODE_SUB);

                break;

            case R.id.btn_help:
                onClick(mSwitchBtn);
                RelativeLayout pad = (RelativeLayout) this.getLayoutInflater().inflate(R.layout.layout_draw, null);

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                pad.setLayoutParams(layoutParams);

                layoutContainer.addView(pad);
                indexOfPad = ((ViewGroup) pad.getParent()).indexOfChild(pad);

                DrawingView drawingView = (DrawingView) pad.findViewById(R.id.DrawingView);
                drawingView.initializePen();

                Button exit = (Button) pad.findViewById(R.id.exit_drawing);
                exit.setOnClickListener(this);
                break;


            case R.id.exit_drawing:
                layoutContainer.removeViewAt(indexOfPad);
                onClick(mSwitchBtn);
                break;

            case R.id.btn_sync:
                mMapsManager.changeSyncMaps();
                v.setSelected(!v.isSelected());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        ArrayList<NormalFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
        if (list.isEmpty())
            return;

        switch (requestCode) {
            case REQUEST_CODE_PICK_GPX_FILE:
                if (resultCode == RESULT_OK) {
                    for (NormalFile fileData : list) {
                        File file = new File(fileData.getPath());
                        mGpxManager.add(file, mMapsManager);
                    }
                    mGpxManager.renewDialog();
                }
                break;

            case REQUEST_CODE_PICK_MAPSFORGE_FILE:
                if (resultCode == RESULT_OK) {
                    MapsForgeTilesProvider p = new MapsForgeTilesProvider(getApplication(),
                            new File(list.get(0).getPath()));

                    mMapsManager.getMapTiles().set(mMapsManager.getCurrentMapCode(),
                            mMapsManager.getCurrentMap().addTileOverlay(new TileOverlayOptions().tileProvider(p)));
                    mMapsManager.getMapTiles().get(mMapsManager.getCurrentMapCode()).setZIndex(-10);
                    mMapsManager.getCurrentMap().setMapType(GoogleMap.MAP_TYPE_NONE);
                }
                break;

            case REQUEST_CODE_PICK_KML_FILE:
                if (resultCode == RESULT_OK) {
                    try {
                        InputStream kmlStream = new FileInputStream(new File(list.get(0).getPath()));
                        kmlLayer = new KmlLayer(mMap, kmlStream, this);
                        kmlLayer.addLayerToMap();
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                }
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        item.setChecked(!item.isChecked());

        switch (item.getItemId()) {
            // Set sub map for contrast
            case R.id.action_sub_map:

                if (item.isChecked()) {
                    importSubMap();
                } else {
                    removeSubMap();
                }

                // Change the icon of menu item
                StateListDrawable stateListDrawable = (StateListDrawable)
                        ContextCompat.getDrawable(this, R.drawable.item_sub_map);
                int[] state = {item.isChecked() ? android.R.attr.state_checked : android.R.attr.state_empty};
                stateListDrawable.setState(state);
                item.setIcon(stateListDrawable.getCurrent());

                break;

            case R.id.action_draw:
                if (item.isChecked()) {
                    DrawUtils drawUtils = new DrawUtils(mMapsManager.getCurrentMap());
                    mMapsManager.getCurrentMap().setOnMapClickListener(drawUtils);
                    mMapsManager.getCurrentMap().setOnMapLongClickListener(drawUtils);
                    mMapsManager.getCurrentMap().setOnInfoWindowClickListener(drawUtils);
                    mMapsManager.getCurrentMap().setOnMarkerClickListener(drawUtils);

                } else {
                    DrawUtils.endDraw();
                    mMapsManager.getCurrentMap().setOnMapClickListener(mMapsManager);
                    mMapsManager.getCurrentMap().setOnMapLongClickListener(mMapsManager);
                    mMapsManager.getCurrentMap().setOnInfoWindowClickListener(mMapsManager);
                    mMapsManager.getCurrentMap().setOnMarkerClickListener(mMapsManager.getCurrentClusterManager());
                }
                break;

            case R.id.action_read:
                if (item.isChecked()) {
                    if (mGpxManager.getGpxTree().getChildren().isEmpty()) {
                        item.setChecked(false);
                        break;
                    }
                    if (mSubMapFragment != null)
                        removeSubMap();
                    importReadFragment();
                } else
                    removeReadFragment();
                break;

            case R.id.gps:
                Intent GpsConfigIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(GpsConfigIntent);
                break;

            case R.id.cross:
                showCross = !item.isChecked();
                for (ImageView cross : mCrossSet)
                    cross.setVisibility(showCross ? View.VISIBLE : View.INVISIBLE);
                View tvLatLon = findViewById(R.id.indicator);
                tvLatLon.setVisibility(showCross ? View.VISIBLE : View.INVISIBLE);
                break;

            case R.id.about:
                try {
                    Intent FacebookIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/100000205564454"));
                    startActivity(FacebookIntent);
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/appetizerandroid")));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isTracking", isTracking);
        editor.apply(); //important, otherwise it wouldn't save.
    }

    @Override
    public void onBackPressed() {
        if (mGpxManager.isShowingDialog())
            this.onClick(findViewById(R.id.leave_gpx_manager));
        else
            moveTaskToBack(true);
    }

    // Reaction to whether user granted or denied permissions
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (checkLocationPermission())
                        mMap.setMyLocationEnabled(true);
                } else {
                    // Permission denied
                    Log.d(TAG, "Fail to get permission PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION");
                }
                break;
            }
        }
    }

    // ConnectionCallbacks
    // 已經連線到Google Services
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Location services connected.");
    }

    // Google Services連線失敗
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // ConnectionResult參數是連線失敗的資訊
        int errorCode = connectionResult.getErrorCode();

        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, R.string.google_play_service_missing,
                    Toast.LENGTH_LONG).show();
        }
    }

    // Check for permission to access Location
    private boolean checkLocationPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permissions
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        );
    }

    // 設定mLocationRequest
    protected void configLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3 * 1000); //milliseconds
        mLocationRequest.setFastestInterval(3 * 1000); //milliseconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // 設定mGoogleApiClient
    private synchronized void configGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    // Google Services連線中斷
    @Override
    public void onConnectionSuspended(int i) {
        // int參數是連線中斷的代號
        Log.i(TAG, "Location services suspended. Please reconnect.");
        mGoogleApiClient.connect();
    }

    // 與TrackingService繫結時，更新紀錄中的航跡
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        TrackingService.MyBinder myBinder = (TrackingService.MyBinder) service;

        // 將按鈕顏色變紅
        mTrackingBtn.setSelected(true);

        // 監聽回傳的位置訊息
        registerReceiver(mBroadcast, new IntentFilter(LOCATION_UPDATE));

        // 將航跡點與Service內同步
        mCurrentTrkpts = myBinder.getTrkpts();

        // Tracking where you passed
        mMyTracks.add(mCurrentTrack);
        mCurrentTrack = mMap.addPolyline(TRK_STYLE);
        // 更新地圖上的航跡
        updateTracking(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isTracking = false;
    }

    // PendingIntent
    public void requestLocationUpdates() {
        if (checkLocationPermission()) {
            Log.i(TAG, "In FusedLocation");

            // Set intent for Tracking
            Intent mServiceIntent = new Intent(this, TrackingService.class);
            PendingIntent mPendingIntent = PendingIntent.getService(
                    this, 0, mServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, mPendingIntent);
        }

        // bind TrackingService
        Intent bindIntent = new Intent(this, TrackingService.class);
        bindService(bindIntent, this, BIND_AUTO_CREATE);
    }

    // 結束TrackingService
    public void removeLocationUpdates() {
        // Set intent for Tracking
        Intent mServiceIntent = new Intent(this, TrackingService.class);
        PendingIntent mPendingIntent = PendingIntent.getService(
                this, 0, mServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // End tracking
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mPendingIntent);
        unbindService(this);
        stopService(mServiceIntent);

        // Save to GPX file
        saveToGpxFile();
    }

    private void saveToGpxFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("請輸入航跡名稱");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        java.util.Calendar mCal = java.util.Calendar.getInstance();
        CharSequence timeString = DateFormat.format("yyyy-MM-dd kk:mm:ss", mCal.getTime());
        input.setText(timeString);
        builder.setView(input);

        // Set up the buttons
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("儲存為GPX檔", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GpxUtils.polyline2Xml(input.getText().toString(), mCurrentTrkpts);
            }
        });

        builder.show();

    }

    // 更新螢幕上的航跡
    private void updateTracking(boolean updateAllPts) {
        if (updateAllPts) {
            // 將所有航跡點加入地圖
            for (Location location : mCurrentTrkpts) {
                mMap.addMarker(TRKPTS_STYLE.position(MapUtils.location2LatLng(location)));
            }
        } else {
            // 將最新航跡點加入地圖
            Location location = mCurrentTrkpts.get(mCurrentTrkpts.size() - 1);
            mMap.addMarker(TRKPTS_STYLE.position(MapUtils.location2LatLng(location)));
        }

        // 更新Cluster
        mMapsManager.getCurrentClusterManager().cluster();

        // 將航跡的Polyline更新
        if (!mCurrentTrkpts.isEmpty())
            mCurrentTrack.setPoints(MapUtils.locations2LatLngs(mCurrentTrkpts));

        // 將攝影機對準最新航跡點
        if (mCurrentTrkpts.size() > 0 && updateAllPts) {
            mCurrentLatLng = MapUtils.location2LatLng(mCurrentTrkpts.get(mCurrentTrkpts.size() - 1));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 15));
        }
    }

    // 在上方載入第二張地圖供對照
    private void importSubMap() {
        getLayoutInflater().inflate(R.layout.sub_map, (ViewGroup) findViewById(R.id.sub_content), true);
        mSubMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.sub_map);
        mFragmentsNumber++;

        Button syncBtn = (Button) findViewById(R.id.btn_sync);
        syncBtn.setOnClickListener(this);
        mBtnsSet.add(syncBtn);

        mSubMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                mMapsManager.enableSubMap(map);
            }
        });

        // get the Cross ImageView
        ImageView cross = new ImageView(getBaseContext());
        RelativeLayout sub_content = (RelativeLayout) findViewById(R.id.sub_content);
        sub_content.addView(cross);
        cross.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_cross_24dp));
        RelativeLayout.LayoutParams params_cross =
                (RelativeLayout.LayoutParams) cross.getLayoutParams();
        params_cross.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        cross.setLayoutParams(params_cross);
        cross.setVisibility(showCross ? View.VISIBLE : View.INVISIBLE);
        mCrossSet.add(MAP_CODE_SUB, cross);

        // Put map into choosing button set
        LinearLayout btnSet = (LinearLayout)
                getLayoutInflater().inflate(R.layout.btn_set_map_choosing, null);
        layoutContainer.addView(btnSet);
        RelativeLayout.LayoutParams params_btn_set =
                (RelativeLayout.LayoutParams) btnSet.getLayoutParams();
        params_btn_set.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        btnSet.setLayoutParams(params_btn_set);

        mTopMapBtn = (ImageButton) btnSet.findViewById(R.id.btn_map_top);
        mBottomMapBtn = (ImageButton) btnSet.findViewById(R.id.btn_map_bottom);
        mTopMapBtn.setOnClickListener(this);
        mBottomMapBtn.setOnClickListener(this);
        mBtnsSet.add(mTopMapBtn);
        mBtnsSet.add(mBottomMapBtn);

        mBottomMapBtn.setSelected(true);
        this.onClick(mTopMapBtn);

        // Set the sub content layout
        setSubContentLayout(1.0f);
    }

    private void removeSubMap() {
        mMapsManager.disableSubMap();
        mMapsManager.setCurrentMap(MAP_CODE_MAIN);

        mBottomMapBtn.setVisibility(View.INVISIBLE);
        mTopMapBtn.setVisibility(View.INVISIBLE);

        FragmentTransaction mTransaction = getFragmentManager().beginTransaction();
        mTransaction.remove(mSubMapFragment).commit();
        mFragmentsNumber--;

        ((ViewGroup) findViewById(R.id.sub_content)).removeAllViews();

        mCrossSet.remove(MAP_CODE_SUB);

        mBtnsSet.remove(mTopMapBtn);
        mBtnsSet.remove(mBottomMapBtn);

        Button syncBtn = (Button) findViewById(R.id.btn_sync);
        mBtnsSet.remove(syncBtn);

        if (mFragmentsNumber == 0)
            setSubContentLayout(0.0f);
    }

    private void importReadFragment() {
        List<WayPoint> wpts = new ArrayList<>();
        Gpx Gpx = ((GpxHolder.GpxTreeItem) mGpxManager.getGpxTree().getChildren().get(0).getValue()).gpx;
        for (WayPoint wpt : Gpx.getWayPoints()) {
            wpts.add(wpt);
        }

        readFragment = ReadFragment.newInstance(wpts);
        mFragmentsNumber++;
        FragmentTransaction mTransaction = getFragmentManager().beginTransaction();
        mTransaction.replace(R.id.sub_content, readFragment).commit();

        setSubContentLayout(0.4f);
    }

    private void removeReadFragment() {
        FragmentTransaction mTransaction = getFragmentManager().beginTransaction();
        mTransaction.remove(readFragment).commit();

        mFragmentsNumber--;

        if (mFragmentsNumber == 0)
            setSubContentLayout(0.0f);
    }


    private void setSubContentLayout(float f) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, 0);
        params.weight = f;
        findViewById(R.id.sub_content).setLayoutParams(params);
    }
}