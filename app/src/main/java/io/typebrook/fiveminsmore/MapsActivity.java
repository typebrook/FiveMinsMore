package io.typebrook.fiveminsmore;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.StateListDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.data.kml.KmlLayer;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.filter.entity.NormalFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;
import io.typebrook.fiveminsmore.Poi.PoiSearchTask;
import io.typebrook.fiveminsmore.filepicker.CustomFilePickActivity;
import io.typebrook.fiveminsmore.gpx.GpxHolder;
import io.typebrook.fiveminsmore.gpx.GpxUtils;
import io.typebrook.fiveminsmore.offlinetile.MapsForgeTilesProvider;
import io.typebrook.fiveminsmore.res.OtherAppPaths;
import io.typebrook.fiveminsmore.utils.MapUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static io.typebrook.fiveminsmore.Constant.REQUEST_CODE_PICK_GPX_FILE;
import static io.typebrook.fiveminsmore.Constant.REQUEST_CODE_PICK_KML_FILE;
import static io.typebrook.fiveminsmore.Constant.REQUEST_CODE_PICK_MAPSFORGE_FILE;
import static io.typebrook.fiveminsmore.Constant.REQUEST_CODE_PICK_POI_FILE;
import static io.typebrook.fiveminsmore.Constant.STARTING_ZOOM;
import static io.typebrook.fiveminsmore.Constant.TAIWAN_CENTER;
import static io.typebrook.fiveminsmore.Constant.TIME_INTERVAL_FOR_TRACKING;
import static io.typebrook.fiveminsmore.Constant.ZINDEX_BASEMAP;
import static io.typebrook.fiveminsmore.MapsManager.MAP_CODE_MAIN;
import static io.typebrook.fiveminsmore.MapsManager.MAP_CODE_SUB;
import static io.typebrook.fiveminsmore.model.PolylilneStyle.STYLE_WHILE_TRACKING;
import static io.typebrook.fiveminsmore.model.TrackPointsStyle.TRKPTS_STYLE;

/*
* The Main Activity contains Google Map Fragment
* Most of works on map are done here.
*/
public class MapsActivity extends AppCompatActivity implements
        TrackingService.CallBack,
        ServiceConnection,
        OnMapReadyCallback, Button.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {

    private final String TAG = "MapsActivity";

    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3857;
    private final String PREFS_NAME = "PREFS_5MinsMore";

    // 地圖元件
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private MapsManager mMapsManager;
    private GpxManager mGpxManager;

    // Activity Layout
    private ViewGroup mLayoutContainer;
    private ActionBar mActionBar;

    // Google API用戶端物件
    protected GoogleApiClient mGoogleApiClient;

    // Location請求物件
    private LocationRequest mLocationRequest;
    // 記錄目前最新的位置
    private Location mCurrentLocation;
    // 紀錄現在航跡
    private List<Location> mMyTrkpts = new ArrayList<>();
    private Polyline mMyTrackOnMap;
    private boolean isTracking = false;

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
    private MenuItem CheckedMenuItem;

    // TODO need to add corresponding functions
    KmlLayer kmlLayer;
    CameraPosition lastCameraPosition;

    // File stores POIs
    private String mPoiFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 取得地圖物件
        setContentView(R.layout.activity_maps);
        mLayoutContainer = (ViewGroup) findViewById(R.id.layout_container);
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        // For tracking, Create an instance of GoogleAPIClient and LocationRequest.
        configGoogleApiClient();
        configLocationRequest();

        // Buttons on map
        mSwitchBtn = (Button) findViewById(R.id.btn_switch);
        mTrackingBtn = (Button) findViewById(R.id.btn_tracking);
        mPickTilesBtn = (Button) findViewById(R.id.btn_pick_tiles);
        mGpxManagerBtn = (Button) findViewById(R.id.btn_gpx_files_list);
        mHelpBtn = (Button) findViewById(R.id.btn_search);

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

        // 取得ActionBar
        mActionBar = getSupportActionBar();

        // 還原各項設定
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        // 檢查是否紀錄航跡中
        isTracking = prefs.getBoolean("isTracking", false);
        if (isTracking) {
            // bind TrackingService
            Intent bindIntent = new Intent(this, TrackingService.class);
            bindService(bindIntent, this, BIND_AUTO_CREATE);
        }

        // 取得上次相機位置
        LatLng lastTarget = new LatLng(prefs.getFloat(
                "cameraLat", (float) TAIWAN_CENTER.latitude),
                prefs.getFloat("cameraLon", (float) TAIWAN_CENTER.longitude));
        Float lastZoom = prefs.getFloat("cameraZoom", STARTING_ZOOM);
        lastCameraPosition = new CameraPosition(lastTarget, lastZoom, 0, 0);

        // 取得POI檔案
        mPoiFile = prefs.getString("poiFIle", null);
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
        MapUtils.setTaiwanBoundaries(map);

        // 設定相機位置
        if (lastCameraPosition == null)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(TAIWAN_CENTER, STARTING_ZOOM));
        else
            map.moveCamera(CameraUpdateFactory.newCameraPosition(lastCameraPosition));
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
                    mActionBar.hide();
                else
                    mActionBar.show();

                break;

            case R.id.btn_pick_tiles:
                mMapsManager.setTileOverlay();
                break;

            case R.id.btn_tracking:
                isTracking = !isTracking;

                if (isTracking) {
                    // 檢查GPS設定
                    checkBeforeStartTracking();
                } else {
                    // 移除FusedLocationApi
                    removeLocationUpdates();
                    // Save to GPX file
                    saveToGpxFile();

                    mTrackingBtn.setSelected(false);
                }
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
                OtherAppPaths.checkMediaDatabase(this);
                Intent pickGpxIntent = new Intent(this, CustomFilePickActivity.class);
                pickGpxIntent.putExtra(CustomFilePickActivity.SUFFIX, new String[]{"gpx", "kml"});
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

            case R.id.btn_sync:
                mMapsManager.changeSyncMaps();
                v.setSelected(!v.isSelected());
                break;

            case R.id.btn_search:
                if (mPoiFile == null)
                    PoiSearchTask.choosePoiFile(this);
                else
                    PoiSearchTask.searchInterface(this, mMapsManager, mPoiFile);
        }
    }

    // get the data from filepicker.CustomFilePickActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || resultCode != RESULT_OK)
            return;

        ArrayList<NormalFile> fileList = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
        if (fileList.isEmpty())
            return;

        switch (requestCode) {
            case REQUEST_CODE_PICK_GPX_FILE:
                for (NormalFile fileData : fileList) {
                    File file = new File(fileData.getPath());
                    mGpxManager.add(file, mMapsManager);
                }
                mGpxManager.renewDialog();
                break;

            case REQUEST_CODE_PICK_MAPSFORGE_FILE:
                MapsForgeTilesProvider p = new MapsForgeTilesProvider(getApplication(),
                        new File(fileList.get(0).getPath()));

                mMapsManager.getMapTiles().set(mMapsManager.getCurrentMapCode(),
                        mMapsManager.getCurrentMap().addTileOverlay(new TileOverlayOptions().tileProvider(p)));
                mMapsManager.getMapTiles().get(mMapsManager.getCurrentMapCode()).setZIndex(ZINDEX_BASEMAP);
                mMapsManager.getCurrentMap().setMapType(GoogleMap.MAP_TYPE_NONE);
                break;

            case REQUEST_CODE_PICK_KML_FILE:
                try {
                    InputStream kmlStream = new FileInputStream(new File(fileList.get(0).getPath()));
                    kmlLayer = new KmlLayer(mMap, kmlStream, this);
                    kmlLayer.addLayerToMap();
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
                break;

            case REQUEST_CODE_PICK_POI_FILE:
                mPoiFile = fileList.get(0).getPath();
                if (mPoiFile != null)
                    onClick(findViewById(R.id.btn_search));
                else
                    Toast.makeText(this, "無法開啟檔案", Toast.LENGTH_SHORT).show();
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
            // 雙地圖模式
            case R.id.action_sub_map:
                if (CheckedMenuItem != null && item.getItemId() != CheckedMenuItem.getItemId())
                    onOptionsItemSelected(CheckedMenuItem);

                if (item.isChecked()) {
                    importSubMap();
                    CheckedMenuItem = item;
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

            // 閱讀模式
            case R.id.action_read:
                if (CheckedMenuItem != null && item.getItemId() != CheckedMenuItem.getItemId())
                    onOptionsItemSelected(CheckedMenuItem);

                if (item.isChecked()) {
                    if (mGpxManager.getGpxTree().getChildren().isEmpty()) {
                        item.setChecked(false);
                        break;
                    }
                    importReadFragment();
                    CheckedMenuItem = item;
                } else {
                    removeReadFragment();
                }
                break;

            case R.id.gps:
                Intent GpsConfigIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(GpsConfigIntent);
                break;

            case R.id.cross:
                showCross = !item.isChecked();
                for (ImageView cross : mCrossSet)
                    cross.setVisibility(showCross ? View.VISIBLE : View.INVISIBLE);

                View tvLatLon = findViewById(R.id.tvCoord);
                tvLatLon.setVisibility(showCross ? View.VISIBLE : View.INVISIBLE);
                break;

            case R.id.about:
                Intent GitHubIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/typebrook/FiveMinsMore/wiki"));
                startActivity(GitHubIntent);
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
        editor.putFloat("cameraLat", (float) mMap.getCameraPosition().target.latitude);
        editor.putFloat("cameraLon", (float) mMap.getCameraPosition().target.longitude);
        editor.putFloat("cameraZoom", mMap.getCameraPosition().zoom);
        editor.putString("poiFIle", mPoiFile);
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
    // 表示已經連接到Google Services
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
            Toast.makeText(this, R.string.google_play_service_missing, Toast.LENGTH_LONG).show();
        }
    }

    public MapsManager getMapsManager() {
        return mMapsManager;
    }

    // Check for permission to access Location
    private boolean checkLocationPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permissions
    private void askPermission() {
        ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        );
    }

    // 設定mLocationRequest
    protected void configLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(TIME_INTERVAL_FOR_TRACKING * 1000); //milliseconds
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
        Log.d(TAG, "Location services suspended. Please reconnect.");
        mGoogleApiClient.connect();
    }

    // 與TrackingService繫結時，更新紀錄中的航跡
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        TrackingService.TrackingBinder trackingBinder = (TrackingService.TrackingBinder) service;

        // Set the CallBack
        trackingBinder.getService().setCallBack(this);

        // 將按鈕顏色變紅
        mTrackingBtn.setSelected(true);

        // 將航跡點與Service內同步
        mMyTrkpts = trackingBinder.getTrkpts();

        // Tracking where you passed
        mMyTrackOnMap = mMap.addPolyline(STYLE_WHILE_TRACKING);

        // 更新地圖上的航跡
        updateTrackPts(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    /*
    * FusedLocationApi to get location update
    * see https://developer.android.com/training/location/receive-location-updates.html
    */
    public void requestLocationUpdates() {
        if (checkLocationPermission()) {
            Log.d(TAG, "In FusedLocation");

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
    }

    private void saveToGpxFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("請輸入航跡名稱");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        java.util.Calendar mCal = java.util.Calendar.getInstance();
        CharSequence timeString = DateFormat.format("yyyy-MM-dd_kk-mm-ss", mCal.getTime());
        input.setText(timeString);
        final String trkName = input.getText().toString();
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
                GpxUtils.polyline2Xml(getBaseContext(), trkName, mMyTrkpts);
            }
        });
        // 將航跡加入GpxManager
        mGpxManager.add(GpxUtils.locs2TreeNode(trkName, mMyTrkpts), mMapsManager);

        builder.setCancelable(false);
        builder.show();
    }

    // 更新螢幕上的航跡
    private void updateTrackPts(boolean updateAllPts) {
        if (updateAllPts) {
            // 將所有航跡點加入地圖
            for (Location location : mMyTrkpts) {
                mMap.addMarker(TRKPTS_STYLE.position(MapUtils.loc2LatLng(location)));
            }
        } else {
            // 將最新航跡點加入地圖
            mMap.addMarker(TRKPTS_STYLE.position(MapUtils.loc2LatLng(mCurrentLocation)));
        }

        // 將航跡的Polyline更新
        if (!mMyTrkpts.isEmpty())
            mMyTrackOnMap.setPoints(MapUtils.locs2LatLngs(mMyTrkpts));

        // 將攝影機對準最初的航跡點
        if (mMyTrkpts.size() == 1) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    MapUtils.loc2LatLng(mMyTrkpts.get(0)), 15));
        }
    }

    // 開始紀錄航跡
    private void startTracking() {
        // 檢查飛航模式
        askAirPlaneMode();

        // 清空目前航跡點
        mMyTrkpts = new ArrayList<>();

        // 使用FusedLocationApi持續取得位置
        requestLocationUpdates();
    }

    // 收到目前的定位設定
    @Override
    public void onResult(@NonNull LocationSettingsResult result) {
        final LocationSettingsStates states = result.getLocationSettingsStates();
        if (!states.isGpsUsable() || states.isNetworkLocationUsable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("提醒一下");

            String warning = "要開始紀錄航跡了!" + "\n\n若接下來的路徑上沒有基地台，建議定位模式使用「僅用GPS」，可以更節約電量。";
            if (!states.isLocationUsable()) {
                builder.setTitle("哎呀糟糕了");
                warning = "紀錄航跡需要開啟定位功能，您目前還沒有把該功能啟用。" + "\n\nPS: " + warning;

                builder.setCancelable(false);
            } else {
                builder.setNeutralButton("這樣就好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        startTracking();
                    }
                });
            }
            builder.setMessage(warning);

            builder.setPositiveButton("馬上設定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent GpsConfigIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(GpsConfigIntent);
                    startTracking();
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isTracking = false;
                }
            });

            builder.show();
        }
    }

    // Ask whether set Airplane Mode
    private void askAirPlaneMode() {
        // 若minsdk >= 17，可改為使用Settings.Global.AIRPLANE_MODE_ON
        Boolean isEnabled = Settings.System.getInt(this.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;

        if (!isEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("提醒一下");
            String warning = "目前尚未開啟飛航模式。\n\n" +
                    "若您在紀錄航跡時不會用到網路，建議開啟飛航模式，讓手機電力更持久";
            builder.setMessage(warning);

            // Set up the buttons
            builder.setNegativeButton("這樣就好", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton("馬上設定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent AirModeConfigIntent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                    startActivity(AirModeConfigIntent);
                }
            });

            builder.setCancelable(false);
            builder.show();
        }
    }

    // 開啟雙地圖模式
    private void importSubMap() {
        getLayoutInflater().inflate(R.layout.fragment_sub_map, (ViewGroup) findViewById(R.id.sub_content), true);
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
        mLayoutContainer.addView(btnSet);
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

    // 關閉雙地圖模式
    private void removeSubMap() {
        mMapsManager.disableSubMap();
        mMapsManager.setCurrentMap(MAP_CODE_MAIN);

        mBottomMapBtn.setVisibility(View.INVISIBLE);
        mTopMapBtn.setVisibility(View.INVISIBLE);

        FragmentTransaction mTransaction = getFragmentManager().beginTransaction();
        mTransaction.remove(mSubMapFragment).commit();
        mSubMapFragment = null;
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

    // 開啟閱讀模式
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

    // 關閉閱讀模式
    private void removeReadFragment() {
        FragmentTransaction mTransaction = getFragmentManager().beginTransaction();
        mTransaction.remove(readFragment).commit();

        mFragmentsNumber--;

        if (mFragmentsNumber == 0)
            setSubContentLayout(0.0f);
    }

    // Adjust layout of sub content, f to determine layout weight
    private void setSubContentLayout(float f) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, 0);
        params.weight = f;
        findViewById(R.id.sub_content).setLayoutParams(params);
    }

    @Override
    public void getServiceData(Location location) {
        mCurrentLocation = location;
        updateTrackPts(false);
    }

    private void checkBeforeStartTracking() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(this);
    }
}