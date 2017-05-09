package io.typebrook.fiveminsmore.model;

/**
 * Created by pham on 2017/4/9.
 */

public class TileList {

    // TODO put into source file
    public static final CharSequence[] onlineMaps = {
            "Google衛星街道混合圖",
            "經建三版地形圖",
            "OpenStreetMap",
            "離線地圖",
            "HappyMan",
            "clear"};

    // onlineMaps
    public static final String SINICA_URL_FORMAT = "http://gis.sinica.edu.tw/tileserver/file-exists.php?img=TM25K_2001-jpg-%d-%d-%d";
    public static final String OSM_URL_FORMAT = "http://c.tile.openstreetmap.org/%d/%d/%d.png";
//    public static final String HappyMan1_URL_FORMAT = "http://rs.happyman.idv.tw/map/moi_osm_gpx/%d/%d/%d.png";
    public static final String HappyMan2_URL_FORMAT = "http://rs.happyman.idv.tw/map/gpxtrack/%d/%d/%d.png";

    // offlinemap format
    public static final String MAPSFORGE_FORMAT = ".map";
}