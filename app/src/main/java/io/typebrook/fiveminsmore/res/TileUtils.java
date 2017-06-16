package io.typebrook.fiveminsmore.res;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import io.typebrook.fiveminsmore.MapsActivity;
import io.typebrook.fiveminsmore.MapsManager;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;
import static io.typebrook.fiveminsmore.Constant.ZINDEX_BASEMAP;
import static io.typebrook.fiveminsmore.res.WmtsTile.URL_FORMAT_NLSC;
import static io.typebrook.fiveminsmore.res.WmtsTile.URL_FORMAT_OSM;
import static io.typebrook.fiveminsmore.res.WmtsTile.URL_FORMAT_SINICA;

/**
 * Created by pham on 2017/4/9.
 */

public class TileUtils {
    // 圖層類型
    private static final int TYPE_ONLINE = 0;
    private static final int TYPE_OFFLINE = 1;
    private static final int TYPE_ADDITIONAL = 2;
    private static final CharSequence[] TILE_TYPES = {
            "線上底圖",
            "離線底圖",
            "層加圖層"
    };

//    public static final CharSequence[] ONLINE_MAPS = {
//            "Google衛星街道混合圖",
//            "經建三版地形圖",
//            "OpenStreetMap",
//            "正射影像圖",
//            "離線地圖",
//            "HappyMan",
//            "clear",
//            "Tile Grid"};

    public static final CharSequence[] ONLINE_MAPS = {
            "Google衛星街道混合圖",
            "經建三版地形圖",
            "正射影像圖",
            "OpenStreetMap"};

    public static final CharSequence[] ONLINE_URLS = {
            null,
            URL_FORMAT_SINICA,
            URL_FORMAT_NLSC,
            URL_FORMAT_OSM};

    public static void chooseTileType(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("選擇圖層類型")
                .setItems(TILE_TYPES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case TYPE_ONLINE:
                                dialog.cancel();
                                chooseOnlineTile(context);
                                break;
                            default:
                                dialog.cancel();
                        }
                    }
                });
        builder.show();
    }

    // 選取線上底圖
    public static void chooseOnlineTile(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("選擇線上底圖")
                .setItems(ONLINE_MAPS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MapsManager manager = ((MapsActivity) context).getMapsManager();
                        GoogleMap map = manager.getCurrentMap();

                        TileOverlay lastTile =
                                manager.getMapTiles().get(manager.getCurrentMapCode());
                        if (lastTile != null)
                            lastTile.remove();

                        if (which == 0)
                            map.setMapType(MAP_TYPE_HYBRID);
                        else {
                            TileOverlay newTile = map.addTileOverlay(
                                    getTileSetting(ONLINE_URLS[which].toString()));
                            newTile.setZIndex(ZINDEX_BASEMAP);
                            manager.getMapTiles().add(manager.getCurrentMapCode(), newTile);
                            map.setMapType(MAP_TYPE_NONE);
                        }
                    }
                });
        builder.show();
    }

    // Set the TileOverlay
    private static TileOverlayOptions getTileSetting(final String tileUrl) {
        TileProvider provider = new UrlTileProvider(1024, 1024) {
            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                String s = String.format(Locale.US, tileUrl, zoom, x, y);
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

    // mapsforge suffix
    public static final String MAPSFORGE_SUFFIX = ".map";
}