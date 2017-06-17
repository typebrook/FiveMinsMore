package io.typebrook.fiveminsmore;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import io.typebrook.fiveminsmore.Cluster.CustomMarker;
import io.typebrook.fiveminsmore.Cluster.CustomRenderer;
import io.typebrook.fiveminsmore.InfoWindow.CustomAdapter;
import io.typebrook.fiveminsmore.model.DetailDialog;
import io.typebrook.fiveminsmore.model.ScaleBar;
import io.typebrook.fiveminsmore.utils.MapUtils;
import io.typebrook.fiveminsmore.utils.ProjFuncs;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;

/**
 * Created by pham on 2017/4/10.
 * This Manager manipulates the interaction with map fragment
 */

public class MapsManager implements
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnPoiClickListener {
    private static final String TAG = "MapsManager";

    protected static final int MAP_CODE_MAIN = 0;
    protected static final int MAP_CODE_SUB = 1;
    protected static int currentMapCode = MAP_CODE_MAIN;

    // 標示縮放大小
    private TextView mZoomNumber;
    private TextView mCrossCoor;

    // Context
    private Activity mContext;

    // GoogleMap object
    private List<GoogleMap> mMaps = new ArrayList<>();
    private List<TileOverlay> mMapTiles = new ArrayList<>();
    private Pair<List<TileOverlay>, List<TileOverlay>> mMapAddTiles =
            new Pair(new ArrayList<>(), new ArrayList<>());
    private List<ClusterManager<CustomMarker>> mClusterManagers = new ArrayList<>();

    // Boundary of Main map
    private PolygonOptions boundaryMain;
    private Polygon boundaryMainPolygon;

    // Temporary marker
    private Marker mTempMarker;

    // whether subMap's camera is sync
    private boolean isMapsSync = false;

    // markers from PoiSearchTask
    public List<CustomMarker> poiMarkers = new ArrayList<>();

    // Container of ScaleBar
    ScaleBar mScaleBar;

    MapsManager(Activity context, GoogleMap map) {
        mContext = context;

        // 地圖
        mMaps.add(MAP_CODE_MAIN, map);
        mMapTiles.add(MAP_CODE_MAIN, null);

        mZoomNumber = (TextView) context.findViewById(R.id.zoom_number);

        // 顯示準心座標
        mCrossCoor = (TextView) context.findViewById(R.id.tvCoord);
        mCrossCoor.setOnClickListener((MapsActivity) mContext);

        // 加入比例尺
        RelativeLayout container = (RelativeLayout) context.findViewById(R.id.layout_container);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(800, 800);
        mScaleBar = new ScaleBar(mContext, getMap(MAP_CODE_MAIN));
        mScaleBar.setLayoutParams(params);
        container.addView(mScaleBar);

        // 註冊畫面縮放的監聽
        map.setOnCameraMoveListener(this);

        // Set the Listener
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnMarkerDragListener(this);
        map.setOnCameraIdleListener(this);

        // 在Activity和Map物件註冊ClusterManager
        mClusterManagers.add(MAP_CODE_MAIN, new ClusterManager<CustomMarker>(mContext, map));
        // The Rule about Cluster Managing
        mClusterManagers.get(MAP_CODE_MAIN).setRenderer(
                new CustomRenderer(mContext, map, mClusterManagers.get(MAP_CODE_MAIN)));
        mClusterManagers.get(MAP_CODE_MAIN).setOnClusterClickListener(
                (CustomRenderer) mClusterManagers.get(MAP_CODE_MAIN).getRenderer());
        // Click on marker to open infoWindow
        map.setOnMarkerClickListener(mClusterManagers.get(MAP_CODE_MAIN));
        // POI in Google map
        map.setOnPoiClickListener(this);

        // Test for new info window
        map.setInfoWindowAdapter(new CustomAdapter(mContext));
    }

    // Add SubMap for contrast
    public void enableSubMap(GoogleMap subMap) {
        mMaps.add(MAP_CODE_SUB, subMap);
        mMapTiles.add(MAP_CODE_SUB, null);

        subMap.setMapType(MAP_TYPE_HYBRID);
        subMap.moveCamera(CameraUpdateFactory.newCameraPosition(mMaps.get(MAP_CODE_MAIN).getCameraPosition()));
        onCameraMove();

        // 在Activity和Map物件註冊ClusterManager
        mClusterManagers.add(MAP_CODE_SUB, new ClusterManager<CustomMarker>(mContext, subMap));
        // The Rule about Cluster Managing
        mClusterManagers.get(MAP_CODE_SUB).setRenderer(
                new CustomRenderer(mContext, subMap, mClusterManagers.get(MAP_CODE_SUB)));
        mClusterManagers.get(MAP_CODE_SUB).setOnClusterClickListener(
                (CustomRenderer) mClusterManagers.get(MAP_CODE_SUB).getRenderer());
        // Click on marker to open infoWindow
        subMap.setOnMarkerClickListener(mClusterManagers.get(MAP_CODE_SUB));
        // Click on Cluster to zoom to Markers
        subMap.setOnCameraIdleListener(this);

        // Test for new info window
        subMap.setInfoWindowAdapter(new CustomAdapter(mContext));
    }

    public void disableSubMap() {
        mMaps.get(MAP_CODE_SUB).setMapType(GoogleMap.MAP_TYPE_NONE);
        setCurrentMap(MAP_CODE_MAIN);

        mMaps.remove(MAP_CODE_SUB);
        mMapTiles.remove(MAP_CODE_SUB);
        mClusterManagers.remove(MAP_CODE_SUB);

        if (boundaryMainPolygon != null) {
            boundaryMainPolygon.remove();
            boundaryMainPolygon = null;
        }
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

    public GoogleMap getMap(int mapCode) {
        return mMaps.get(mapCode);
    }

    public int getMapsNum() {
        return mMaps.size();
    }

    public TileOverlay getCurrentMapTile() {
        return mMapTiles.get(currentMapCode);
    }

    public void setCurrentMapTile(TileOverlay tileOverlay) {
        mMapTiles.set(currentMapCode, tileOverlay);
    }

    public void addMapAddTiles(TileOverlay layer) {
        if (currentMapCode == MAP_CODE_MAIN)
            mMapAddTiles.first.add(layer);
        else if (currentMapCode == MAP_CODE_SUB)
            mMapAddTiles.second.add(layer);
    }

    public void clearCurrentMapAddTiles() {
        List<TileOverlay> addTiles;
        if (currentMapCode == MAP_CODE_MAIN)
            addTiles = mMapAddTiles.first;
        else
            addTiles = mMapAddTiles.second;

        for (TileOverlay layer : addTiles)
            layer.remove();
        addTiles.clear();
    }

    public List<ClusterManager<CustomMarker>> getClusterManagers() {
        return mClusterManagers;
    }

    public void clusterTheMarkers() {
        for (ClusterManager<CustomMarker> manager : mClusterManagers)
            manager.cluster();
    }

    public List<LatLng> getBounds(LatLngBounds b) {
        List<LatLng> list = new ArrayList<>();
        list.add(b.northeast);
        list.add(new LatLng(b.northeast.latitude, b.southwest.longitude));
        list.add(b.southwest);
        list.add(new LatLng(b.southwest.latitude, b.northeast.longitude));

        return list;
    }

    public void addTempMarker(String title, LatLng latLng) {
        mTempMarker = mMaps.get(MAP_CODE_MAIN).addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title == null ? "點選位置" : title)
                        .snippet(ProjFuncs.latLng2DString(latLng, false))
                        .draggable(true)
//                        .anchor(0.5f, 0.5f)
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
        );

        mTempMarker.showInfoWindow();
        mMaps.get(MAP_CODE_MAIN).animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mTempMarker != null) {
            mTempMarker.remove();
            mTempMarker = null;
        }
    }

    // 長按就加人waypoint，使用CustomMarker
    @Override
    public void onMapLongClick(LatLng latLng) {
        if (mTempMarker != null) {
            mTempMarker.remove();
            mTempMarker = null;
        } else {
            addTempMarker(null, latLng);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.hideInfoWindow();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        DetailDialog markerDetail = new DetailDialog();
        markerDetail.setArgs(mContext, marker);
        markerDetail.show(((MapsActivity) mContext).getSupportFragmentManager(), "");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng latLng = marker.getPosition();
        marker.setSnippet(ProjFuncs.latLng2DString(latLng, false));
        marker.showInfoWindow();
        mMaps.get(MAP_CODE_MAIN).animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onCameraMove() {
        // 顯示縮放層級
        CameraPosition cameraPosition = mMaps.get(MAP_CODE_MAIN).getCameraPosition();
        float currentZoomNumber = cameraPosition.zoom;
//        String currentZoom = Integer.twd2String(Math.round(currentZoomNumber));
        String currentZoom = "" + (int) currentZoomNumber;
        mZoomNumber.setText(currentZoom);


        LatLng latLng = cameraPosition.target;
        mCrossCoor.setText(ProjFuncs.getCurrentCoor(latLng, true));

        // 改變比例尺
        adjustScaleBar();

        // 次要地圖，若同步，則隨主要地圖移動畫面，若非同步，使用Polygon顯示主要地圖的範圍
        if (mMaps.size() > 1) {
            if (isMapsSync) {
                mMaps.get(MAP_CODE_SUB).moveCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            } else {
                LatLngBounds latLngBounds = MapUtils.getMapBounds(mMaps.get(MAP_CODE_MAIN));
                if (boundaryMainPolygon == null) {
                    boundaryMain = new PolygonOptions()
                            .addAll(getBounds(latLngBounds))
                            .strokeColor(Color.YELLOW)
                            .zIndex(10);
                    boundaryMainPolygon = mMaps.get(MAP_CODE_SUB).addPolygon(boundaryMain);
                } else {
                    boundaryMainPolygon.setPoints(getBounds(latLngBounds));
                }
            }
        }
    }

    @Override
    public void onCameraIdle() {
        Log.d(TAG, "onCameraIdle");
        adjustScaleBar();
        for (ClusterManager<CustomMarker> cm : getClusterManagers()) {
            cm.cluster();
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        if (mTempMarker != null) {
            mTempMarker.remove();
            mTempMarker = null;
        }

        addTempMarker(poi.name, poi.latLng);
    }

    void changeSyncMaps() {
        isMapsSync = !isMapsSync;
        if (isMapsSync) {
            boundaryMainPolygon.remove();
            boundaryMainPolygon = null;
        }
        onCameraMove();
    }

    public void adjustScaleBar() {
        mScaleBar.invalidate();
    }
}
