package io.typebrook.fiveminsmore.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.vincent.filepicker.Constant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import io.typebrook.fiveminsmore.MapsActivity;
import io.typebrook.fiveminsmore.MapsManager;
import io.typebrook.fiveminsmore.R;
import io.typebrook.fiveminsmore.filepicker.CustomFilePickActivity;
import io.typebrook.fiveminsmore.offlinetile.CoorTileProvider;
import io.typebrook.fiveminsmore.offlinetile.MapsForgeTilesProvider;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static io.typebrook.fiveminsmore.Constant.REQUEST_CODE_PICK_MAPSFORGE_FILE;
import static io.typebrook.fiveminsmore.Constant.REQUEST_CODE_PICK_MAPSFORGE_THEME_FILE;
import static io.typebrook.fiveminsmore.Constant.SUFFIX_MAPSFORGE;
import static io.typebrook.fiveminsmore.Constant.SUFFIX_THEME;
import static io.typebrook.fiveminsmore.Constant.ZINDEX_ADDTILE;
import static io.typebrook.fiveminsmore.Constant.ZINDEX_BASEMAP;
import static io.typebrook.fiveminsmore.MapsActivity.currentDialog;
import static io.typebrook.fiveminsmore.MapsActivity.mapFile;
import static io.typebrook.fiveminsmore.MapsActivity.themeFile;
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
            "Google道路地圖",
            "2001-經建三版地形圖",
            "1916-日治蕃地地形圖-1:50,000",
            "正射影像圖",
            "OpenStreetMap"};

    private static final CharSequence[] URL_ONLINE_TILES = {
            null,
            null,
            URL_FORMAT_SINICA_TM25K_2001,
            URL_FORMAT_SINICA_JM50K_1916,
            URL_FORMAT_NLSC_PHOTO2,
            URL_FORMAT_OSM};

    private static final CharSequence[] ADDITION_TILES = {
            "地圖產生器-航跡航點",
            "圖磚範圍(黑)",
            "圖磚範圍(白)",
            "鄉鎮界",
            "清空"};

    private static final CharSequence[] URL_ADDITIONAL_TILES = {
            URL_FORMAT_HAPPYMAN,
            null,
            URL_FORMAT_NLSC_TOWN,
            null};

    public static void chooseTileType(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("選擇圖層類型")
                .setItems(TILE_TYPES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case TYPE_ONLINE:
                                pickOnlineTile(context);
                                break;

                            case TYPE_OFFLINE:
                                pickOfflineMap(context);
                                break;

                            case TYPE_ADDITIONAL:
                                pickAdditionalTile(context);
                        }
                    }
                });
        builder.show();
    }

    // 選取線上底圖
    public static void pickOnlineTile(final Context context) {
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

                        switch (which) {
                            case 0:
                                map.setMapType(MAP_TYPE_HYBRID);
                                break;

                            case 1:
                                map.setMapType(MAP_TYPE_NORMAL);
                                break;

                            default:
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

    public static void pickOfflineMap(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View pickOfflineMapView =
                ((Activity) context).getLayoutInflater().inflate(R.layout.view_offline_map, null);
        builder.setView(pickOfflineMapView);
        builder.setTitle("離線底圖");

        // 目前使用的*.map檔案
        final TextView currentMapFile = (TextView) pickOfflineMapView
                .findViewById(R.id.current_map_file);
        currentMapFile.setText(mapFile != null ? new File(mapFile).getName() : "");

        // 目前使用的風格檔案
        final TextView currentThemeFile = (TextView) pickOfflineMapView
                .findViewById(R.id.current_theme_file);
        currentThemeFile.setText(themeFile != null ? new File(themeFile).getName() : "");

        // 選擇*.map檔案
        final Button pickMapFile = (Button) pickOfflineMapView
                .findViewById(R.id.pick_map_file);

        // 選擇風格檔案
        final Button pickThemeFile = (Button) pickOfflineMapView
                .findViewById(R.id.pick_theme_file);

        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mapFile != null && themeFile != null)
                    setMapFile((MapsActivity) context);
            }
        });

        currentDialog = builder.show();

        pickMapFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickMapFile(context);
            }
        });

        pickThemeFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickThemeFile(context);
            }
        });
    }

    // 選取線上疊加圖層
    public static void pickAdditionalTile(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("選擇疊加圖層")
                .setItems(ADDITION_TILES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MapsManager manager = ((MapsActivity) context).getMapsManager();
                        GoogleMap map = manager.getCurrentMap();
                        switch (which) {
                            case 1:
                                manager.addMapAddTiles(map.addTileOverlay(
                                        new TileOverlayOptions().tileProvider(
                                                new CoorTileProvider(context, Color.BLACK))));
                                break;

                            case 2:
                                manager.addMapAddTiles(map.addTileOverlay(
                                        new TileOverlayOptions().tileProvider(
                                                new CoorTileProvider(context, Color.WHITE))));
                                break;

                            case 4:
                                manager.clearCurrentMapAddTiles();
                                break;

                            default:
                                TileOverlay tileOverlay = map.addTileOverlay(
                                        getTileSetting(URL_ADDITIONAL_TILES[which].toString()));
                                tileOverlay.setZIndex(ZINDEX_ADDTILE);
                                manager.addMapAddTiles(tileOverlay);
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

    public static void pickMapFile(Context context) {
        Intent pickOfflineMapIntent = new Intent(context, CustomFilePickActivity.class);
        pickOfflineMapIntent.putExtra(Constant.MAX_NUMBER, 1);
        pickOfflineMapIntent.putExtra(CustomFilePickActivity.SUFFIX, new String[]{SUFFIX_MAPSFORGE});
        ((Activity) context).startActivityForResult(pickOfflineMapIntent,
                REQUEST_CODE_PICK_MAPSFORGE_FILE);
    }

    public static void pickThemeFile(Context context) {
        Intent pickOfflineMapIntent = new Intent(context, CustomFilePickActivity.class);
        pickOfflineMapIntent.putExtra(Constant.MAX_NUMBER, 1);
        pickOfflineMapIntent.putExtra(CustomFilePickActivity.SUFFIX, new String[]{SUFFIX_THEME});
        ((Activity) context).startActivityForResult(pickOfflineMapIntent,
                REQUEST_CODE_PICK_MAPSFORGE_THEME_FILE);
    }

    public static void setMapFile(MapsActivity context) {
        MapsForgeTilesProvider provider;
        try {
             provider = new MapsForgeTilesProvider(
                     context.getApplication(), new File(mapFile), new File(themeFile));
        } catch (Exception e) {
            mapFile = null;
            e.printStackTrace();
            Toast.makeText(context, "無法開啟檔案", Toast.LENGTH_SHORT).show();
            return;
        }

        TileOverlay lastTile = context.getMapsManager().getCurrentMapTile();
        if (lastTile != null)
            lastTile.remove();

        MapsManager manager = context.getMapsManager();
        manager.setCurrentMapTile(manager.getCurrentMap().addTileOverlay(
                new TileOverlayOptions().tileProvider(provider)));
        manager.getCurrentMapTile().setZIndex(ZINDEX_BASEMAP);
        manager.getCurrentMap().setMapType(GoogleMap.MAP_TYPE_NONE);
    }
}