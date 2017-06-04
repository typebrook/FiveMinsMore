package io.typebrook.fiveminsmore.res;

/**
 * Created by pham on 2017/4/9.
 */

public class TileList {

    public static final CharSequence[] onlineMaps = {
            "Google衛星街道混合圖",
            "經建三版地形圖",
            "OpenStreetMap",
            "正射影像圖",
            "離線地圖",
            "HappyMan",
            "clear"};

    // onlineMaps
    public static final String SINICA_URL_FORMAT = "http://gis.sinica.edu.tw/tileserver/file-exists.php?img=TM25K_2001-jpg-%d-%d-%d";
    public static final String OSM_URL_FORMAT = "http://c.tile.openstreetmap.org/%d/%d/%d.png";
    public static final String HappyMan2_URL_FORMAT = "http://rs.happyman.idv.tw/map/gpxtrack/%d/%d/%d.png";
    public static final String NLSC_URL_FORMAT =
            "http://maps.nlsc.gov.tw/S_Maps/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSIO" +
            "N=1.0.0&LAYER=PHOTO2&STYLE=_null&TILEMATRIXSET=EPSG:3857&TILEMATRIX=" +
            "EPSG:3857:%d&TILECOL=%d&TILEROW=%d&FORMAT=image/png";

    // mapsforge suffix
    public static final String MAPSFORGE_SUFFIX = ".map";
}