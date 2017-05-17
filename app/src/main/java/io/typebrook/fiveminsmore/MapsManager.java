package io.typebrook.fiveminsmore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.maps.android.clustering.ClusterManager;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.typebrook.fiveminsmore.filepicker.CustomFilePickActivity;
import io.typebrook.fiveminsmore.model.CustomMarker;
import io.typebrook.fiveminsmore.model.CustomRenderer;
import io.typebrook.fiveminsmore.res.TileList;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static io.typebrook.fiveminsmore.Constant.REQUEST_CODE_PICK_MAPSFORGE_FILE;
import static io.typebrook.fiveminsmore.res.TileList.DIGITALGLOBE_URL_FORMAT;
import static io.typebrook.fiveminsmore.res.TileList.HappyMan2_URL_FORMAT;
import static io.typebrook.fiveminsmore.res.TileList.MAPSFORGE_FORMAT;
import static io.typebrook.fiveminsmore.res.TileList.OSM_URL_FORMAT;
import static io.typebrook.fiveminsmore.res.TileList.SINICA_URL_FORMAT;

/**
 * Created by pham on 2017/4/10.
 */

public class MapsManager implements
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnCameraMoveListener {
    private static final String TAG = "MapsManager";

    protected static final int MAP_CODE_MAIN = 0;
    protected static final int MAP_CODE_SUB = 1;
    protected static int currentMapCode = MAP_CODE_MAIN;

    // 航跡樣式
    // static final List<PatternItem> dashedPattern = Arrays.asList(new Dash(50), new Gap(30));
    static final PolylineOptions TRK_STYLE = new PolylineOptions()
            .color(Color.BLUE)
            .zIndex(100);

    // 航跡點樣式
    static final MarkerOptions TRKPTS_STYLE = new MarkerOptions()
            .zIndex(100)
            .anchor(0.5f, 0.5f)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_trkpt));

    // Boundaries of TAIWAN
    private final LatLngBounds TAIWAN_BOUNDS =
            new LatLngBounds(new LatLng(21.8, 119.2), new LatLng(25.5, 122));
    // ZOOM restriction of TAIWAN
    private final int TAIWAN_ZOOM_MIN = 7;
    private final int TAIWAN_ZOOM_MAX = 20;

    // ZOOM Level when creating map
    private final int STARTING_ZOOM = 8;

    // 標示縮放大小
    private TextView mZoomNumber;
    private TextView mIndicator;

    // Context
    private Context mContext;

    // GoogleMap object
    private List<GoogleMap> mMaps = new ArrayList<>();
    private List<TileOverlay> mMapTiles = new ArrayList<>();
    private List<TileOverlay> mMapAddTiles = new ArrayList<>();
    private List<ClusterManager<CustomMarker>> mClusterManagers = new ArrayList<>();

    // Temporary marker
    Marker mMarker;

    // Boundary of Main map
    PolygonOptions boundaryMain;
    Polygon boundaryMainPolygon;

    private boolean isMapsSync = false;

    MapsManager(Context context, GoogleMap map) {
        mContext = context;
        mMaps.add(MAP_CODE_MAIN, map);
        mMapTiles.add(MAP_CODE_MAIN, null);
        mMapAddTiles.add(MAP_CODE_MAIN, null);
        mZoomNumber = (TextView) ((Activity) context).findViewById(R.id.zoom_number);
        mIndicator = (TextView) ((Activity) context).findViewById(R.id.indicator);

        // 註冊畫面縮放的監聽
        map.setOnCameraMoveListener(this);

        // Set the Listener
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnMarkerDragListener(this);

        // 在Activity和Map物件註冊ClusterManager
        mClusterManagers.add(MAP_CODE_MAIN, new ClusterManager<CustomMarker>(mContext, map));
        // The Rule about Cluster Managing
        mClusterManagers.get(MAP_CODE_MAIN).setRenderer(
                new CustomRenderer(mContext, map, mClusterManagers.get(MAP_CODE_MAIN)));
        mClusterManagers.get(MAP_CODE_MAIN).setOnClusterClickListener(
                (CustomRenderer) mClusterManagers.get(MAP_CODE_MAIN).getRenderer());
        // Click on marker to open infoWindow
        map.setOnMarkerClickListener(mClusterManagers.get(MAP_CODE_MAIN));
        // Click on Cluster to zoom to Markers
        map.setOnCameraIdleListener(mClusterManagers.get(MAP_CODE_MAIN));
    }

    // Set the TileOverlay
    private static TileOverlayOptions getTileSetting(final String tileUrl) {
        TileProvider provider = new UrlTileProvider(1024, 1024) {
            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                String s = String.format(Locale.US, tileUrl, zoom, x, y);
                Log.i(TAG, "tile url: " + s);
                URL url;
                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
                return url;
            }
        };

        return new TileOverlayOptions().tileProvider(provider);
    }

    // Add SubMap for contrast
    public void enableSubMap(GoogleMap map) {
        mMaps.add(MAP_CODE_SUB, map);
        mMapTiles.add(MAP_CODE_SUB, null);
        mMapAddTiles.add(MAP_CODE_SUB, null);

        LatLngBounds latLngBounds = mMaps.get(MAP_CODE_MAIN).getProjection().getVisibleRegion().latLngBounds;
        boundaryMain = new PolygonOptions().addAll(getBounds(latLngBounds)).strokeColor(Color.YELLOW);
        boundaryMainPolygon = map.addPolygon(boundaryMain);

        map.setMapType(MAP_TYPE_HYBRID);
        map.moveCamera(CameraUpdateFactory.newCameraPosition(mMaps.get(MAP_CODE_MAIN).getCameraPosition()));

        // 在Activity和Map物件註冊ClusterManager
        mClusterManagers.add(MAP_CODE_SUB, new ClusterManager<CustomMarker>(mContext, map));
        // The Rule about Cluster Managing
        mClusterManagers.get(MAP_CODE_SUB).setRenderer(
                new CustomRenderer(mContext, map, mClusterManagers.get(MAP_CODE_SUB)));
        // Click on marker to open infoWindow
        map.setOnMarkerClickListener(mClusterManagers.get(MAP_CODE_SUB));
        // Click on Cluster to zoom to Markers
        map.setOnCameraIdleListener(mClusterManagers.get(MAP_CODE_SUB));
    }

    public void disableSubMap() {
        mMaps.get(MAP_CODE_SUB).setMapType(GoogleMap.MAP_TYPE_NONE);
        setCurrentMap(MAP_CODE_MAIN);

        mMaps.remove(MAP_CODE_SUB);
        mMapTiles.remove(MAP_CODE_SUB);
        mMapAddTiles.remove(MAP_CODE_SUB);
        mClusterManagers.remove(MAP_CODE_SUB);
    }

    public void setCurrentMap(int code) {
        currentMapCode = code;
    }

    public int getCurrentMapCode() {
        return currentMapCode;
    }

    public GoogleMap getCurrentMap() {
        return mMaps.get(currentMapCode);
    }

    public List<TileOverlay> getMapTiles() {
        return mMapTiles;
    }

    public ClusterManager<CustomMarker> getCurrentClusterManager() {
        return mClusterManagers.get(currentMapCode);
    }

    // 設定鏡頭可動範圍
    public void setTaiwanBoundaries() {
        mMaps.get(MAP_CODE_MAIN).setLatLngBoundsForCameraTarget(TAIWAN_BOUNDS);
        mMaps.get(MAP_CODE_MAIN).setMinZoomPreference(TAIWAN_ZOOM_MIN);
        mMaps.get(MAP_CODE_MAIN).setMaxZoomPreference(TAIWAN_ZOOM_MAX);
        LatLng taiwan = new LatLng(24.027002, 121.514085);
        mMaps.get(MAP_CODE_MAIN).moveCamera(CameraUpdateFactory.newLatLngZoom(taiwan, STARTING_ZOOM));

        String currentZoom = Integer.toString(STARTING_ZOOM);
        mZoomNumber.setText(currentZoom);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
    }

    // 長按就加人waypoint，使用CustomMarker
    @Override
    public void onMapLongClick(LatLng latLng) {
        if (mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
        String lat = String.format(Locale.getDefault(), "%.6f", latLng.latitude);
        String lon = String.format(Locale.getDefault(), "%.6f", latLng.longitude);
        mMarker = mMaps.get(MAP_CODE_MAIN).addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("點選位置")
                        .snippet("北緯" + lat + "度，東經" + lon + "度")
                        .draggable(true)
//                        .anchor(0.5f, 0.5f)
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
        );

        mMarker.showInfoWindow();
        mMaps.get(MAP_CODE_MAIN).animateCamera(CameraUpdateFactory.newLatLng(latLng));

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        LatLng latLng = marker.getPosition();

        String lat = String.format(Locale.getDefault(), "%.6f", latLng.latitude);
        String lon = String.format(Locale.getDefault(), "%.6f", latLng.longitude);

        String url = "http://map.happyman.idv.tw/twmap/api/waypoints.php?x=" + lon + "&y=" + lat +
                "&r=50&detail=1#";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        mContext.startActivity(i);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.hideInfoWindow();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng latLng = marker.getPosition();
        String lat = String.format(Locale.getDefault(), "%.6f", latLng.latitude);
        String lon = String.format(Locale.getDefault(), "%.6f", latLng.longitude);

        marker.setSnippet("北緯" + lat + "度，東經" + lon + "度");
        marker.showInfoWindow();
        mMaps.get(MAP_CODE_MAIN).animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onCameraMove() {

        CameraPosition cameraPosition = mMaps.get(MAP_CODE_MAIN).getCameraPosition();
        float currentZoomNumber = cameraPosition.zoom;

//        String currentZoom = Integer.toString(Math.round(currentZoomNumber));
        String currentZoom = "" + (int) currentZoomNumber;
        mZoomNumber.setText(currentZoom);

        LatLng latLng = cameraPosition.target;
        String lat = String.format(Locale.getDefault(), "%.6f", latLng.latitude);
        String lon = String.format(Locale.getDefault(), "%.6f", latLng.longitude);
        mIndicator.setText(lat + ", " + lon);

        if (mMaps.size() > 1) {
            if (isMapsSync) {
                mMaps.get(MAP_CODE_SUB).moveCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            }else {
                LatLngBounds latLngBounds = mMaps.get(MAP_CODE_MAIN).getProjection().getVisibleRegion().latLngBounds;
                boundaryMain = new PolygonOptions().addAll(getBounds(latLngBounds)).strokeColor(Color.YELLOW);
                boundaryMainPolygon.remove();
                boundaryMainPolygon = mMaps.get(MAP_CODE_SUB).addPolygon(boundaryMain);
            }

        }
    }

    public List<LatLng> getBounds(LatLngBounds b) {
        List<LatLng> list = new ArrayList<>();
        list.add(b.northeast);
        list.add(new LatLng(b.northeast.latitude, b.southwest.longitude));
        list.add(b.southwest);
        list.add(new LatLng(b.southwest.latitude, b.northeast.longitude));

        return list;
    }

    public void changeSyncMaps(){
        isMapsSync = ! isMapsSync;
        if (isMapsSync)
            boundaryMainPolygon.remove();
    }

    public void setTileOverlay() {
        CharSequence[] onlineMaps = TileList.onlineMaps;

        new AlertDialog.Builder(mContext)
                .setTitle("線上圖資")
                .setItems(onlineMaps, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {

                        if (which < 4) {
                            mMaps.get(currentMapCode).setMapType(GoogleMap.MAP_TYPE_NONE);
                            if (mMapTiles.get(currentMapCode) != null) {
                                mMapTiles.get(currentMapCode).remove();
                                mMapTiles.get(currentMapCode).clearTileCache();
                                mMapTiles.set(currentMapCode, null);
                            }
                        }

                        switch (which) {
                            case 0:
                                mMaps.get(currentMapCode).setMapType(MAP_TYPE_HYBRID);
                                break;

                            case 1:
                                mMapTiles.set(currentMapCode,
                                        mMaps.get(currentMapCode).addTileOverlay(getTileSetting(SINICA_URL_FORMAT)));
                                mMapTiles.get(currentMapCode).setZIndex(-10);
                                break;

                            case 2:
                                mMapTiles.set(currentMapCode,
                                        mMaps.get(currentMapCode).addTileOverlay(getTileSetting(OSM_URL_FORMAT)));
                                mMapTiles.get(currentMapCode).setZIndex(-10);
                                break;

                            case 3:
                                mMapTiles.set(currentMapCode,
                                        mMaps.get(currentMapCode).addTileOverlay(getTileSetting(DIGITALGLOBE_URL_FORMAT)));
                                mMapTiles.get(currentMapCode).setZIndex(-10);
                                break;

                            case 4:
                                Intent pickOfflineMapIntent = new Intent(mContext, CustomFilePickActivity.class);
                                pickOfflineMapIntent.putExtra(Constant.MAX_NUMBER, 1);
                                pickOfflineMapIntent.putExtra(NormalFilePickActivity.SUFFIX, new String[]{MAPSFORGE_FORMAT});
                                ((Activity) mContext).startActivityForResult(pickOfflineMapIntent,
                                        REQUEST_CODE_PICK_MAPSFORGE_FILE);
                                break;

                            case 5:
                                if (mMapAddTiles.get(currentMapCode) != null) {
                                    mMapAddTiles.get(currentMapCode).remove();
                                    mMapAddTiles.get(currentMapCode).clearTileCache();
                                    mMapAddTiles.set(currentMapCode, null);
                                } else {
                                    mMapAddTiles.set(currentMapCode,
                                            mMaps.get(currentMapCode).addTileOverlay(getTileSetting(HappyMan2_URL_FORMAT)));
                                    mMapAddTiles.get(currentMapCode).setZIndex(-1);
                                }
                                break;

                            case 6:
                                mMaps.get(currentMapCode).clear();
                                mMaps.get(currentMapCode).setMapType(MAP_TYPE_HYBRID);
                                break;
                        }
                    }
                }).show();
    }
}
