package io.typebrook.fiveminsmore.res;

/**
 * Created by pham on 2017/4/9.
 */

public class TileList {

    public static final CharSequence[] onlineMaps = {
            "Google衛星街道混合圖",
            "經建三版地形圖",
            "OpenStreetMap",
            "DigitalGlobe",
            "離線地圖",
            "HappyMan",
            "clear"};

    // onlineMaps
    public static final String SINICA_URL_FORMAT = "http://gis.sinica.edu.tw/tileserver/file-exists.php?img=TM25K_2001-jpg-%d-%d-%d";
    public static final String OSM_URL_FORMAT = "http://c.tile.openstreetmap.org/%d/%d/%d.png";
    public static final String DIGITALGLOBE_URL_FORMAT = "https://b.tiles.mapbox.com/v4/digitalglobe.0a8e44ba/%d/%d/%d.png?access_token=pk.eyJ1IjoiZGlnaXRhbGdsb2JlIiwiYSI6ImNqMmFxcGJ2MjAwOHEzMm9nZmF2c3luZWkifQ.HsF19zOlj8PeOxo5BhNqyQ";
    public static final String HappyMan2_URL_FORMAT = "http://rs.happyman.idv.tw/map/gpxtrack/%d/%d/%d.png";

    // offlinemap format
    public static final String MAPSFORGE_FORMAT = ".map";
}