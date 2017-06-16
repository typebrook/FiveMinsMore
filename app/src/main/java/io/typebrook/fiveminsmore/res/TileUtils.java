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
import io.typebrook.fiveminsmore.offlinetile.CoorTileProvider;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;
import static io.typebrook.fiveminsmore.Constant.ZINDEX_BASEMAP;
import static io.typebrook.fiveminsmore.res.WmtsTile.URL_FORMAT_HAPPYMAN;
import static io.typebrook.fiveminsmore.res.WmtsTile.URL_FORMAT_NLSC_PHOTO2;
import static io.typebrook.fiveminsmore.res.WmtsTile.URL_FORMAT_NLSC_TOWN;
import static io.typebrook.fiveminsmore.res.WmtsTile.URL_FORMAT_OSM;
import static io.typebrook.fiveminsmore.res.WmtsTile.URL_FORMAT_SINICA_JM50K_1916;
import static io.typebrook.fiveminsmore.res.WmtsTile.URL_FORMAT_SINICA_TM25K_2001;

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
            "疊加圖層"
    };

//    public static final CharSequence[] ONLINE_TILES = {
//            "Google衛星街道混合圖",
//            "經建三版地形圖",
//            "OpenStreetMap",
//            "正射影像圖",
//            "離線地圖",
//            "HappyMan",
//            "clear",
//            "Tile Grid"};

    private static final CharSequence[] ONLINE_TILES = {
            "Google衛星街道混合圖",
            "2001-經建三版地形圖",
            "1916-日治蕃地地形圖-1:50,000",
            "正射影像圖",
            "OpenStreetMap"};

    private static final CharSequence[] URL_ONLINE_TILES = {
            null,
            URL_FORMAT_SINICA_TM25K_2001,
            URL_FORMAT_SINICA_JM50K_1916,
            URL_FORMAT_NLSC_PHOTO2,
            URL_FORMAT_OSM};

    private static final CharSequence[] ADDITION_TILES = {
            "地圖產生器-航跡航點",
            "圖磚範圍",
            "縣市界",
          "清空"};

    private static final CharSequence[] URL_ADDITIONAL_TILES = {
            URL_FORMAT_HAPPYMAN,
            null,
            URL_FORMAT_NLSC_TOWN};

    public static void chooseTileType(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("選擇圖層類型")
                .setItems(TILE_TYPES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case TYPE_ONLINE:
                                chooseOnlineTile(context);
                                break;

                            case TYPE_ADDITIONAL:
                                chooseAdditionalTile(context);

                            default:
                        }
                    }
                });
        builder.show();
    }

    // 選取線上底圖
    public static void chooseOnlineTile(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("選擇線上底圖")
                .setItems(ONLINE_TILES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MapsManager manager = ((MapsActivity) context).getMapsManager();
                        GoogleMap map = manager.getCurrentMap();

                        TileOverlay lastTile = manager.getCurrentMapTile();
                        if (lastTile != null)
                            lastTile.remove();

                        if (which == 0)
                            map.setMapType(MAP_TYPE_HYBRID);
                        else {
                            TileOverlay newTile = map.addTileOverlay(
                                    getTileSetting(URL_ONLINE_TILES[which].toString()));
                            newTile.setZIndex(ZINDEX_BASEMAP);
                            manager.setCurrentMapTile(newTile);
                            map.setMapType(MAP_TYPE_NONE);
                        }
                    }
                });
        builder.show();
    }

    // 選取線上疊加圖層
    public static void chooseAdditionalTile(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("選擇疊加圖層")
                .setItems(ADDITION_TILES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MapsManager manager = ((MapsActivity) context).getMapsManager();
                        GoogleMap map = manager.getCurrentMap();
                        switch (which) {
                            case 0:
                                manager.addMapAddTiles(map.addTileOverlay(
                                        getTileSetting(URL_ADDITIONAL_TILES[which].toString())));
                                break;

                            case 1:
                                manager.addMapAddTiles(map.addTileOverlay(
                                        new TileOverlayOptions().tileProvider(
                                                new CoorTileProvider(context))));
                                break;

                            case 2:
                                manager.addMapAddTiles(map.addTileOverlay(
                                        getTileSetting(URL_ADDITIONAL_TILES[which].toString())));
                                break;

                            case 3:
                                manager.clearCurrentMapAddTiles();
                                break;
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