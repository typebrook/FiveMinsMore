package io.typebrook.fiveminsmore.res;

/**
 * Created by pham on 2017/6/16.
 */

public class WmtsTile {
    // ONLINE_TILES
    // Sinica: http://gis.sinica.edu.tw/tileserver/
    public static final String URL_FORMAT_SINICA_TM25K_2001 =
            "http://gis.sinica.edu.tw/tileserver/file-exists.php?img=TM25K_2001-jpg-%d-%d-%d";

    public static final String URL_FORMAT_SINICA_JM50K_1916 =
            "http://gis.sinica.edu.tw/tileserver/file-exists.php?img=JM50K_1916-jpg-%d-%d-%d";

    public static final String URL_FORMAT_OSM = "http://c.tile.openstreetmap.org/%d/%d/%d.png";

    public static final String URL_FORMAT_HAPPYMAN =
            "http://rs.happyman.idv.tw/map/gpxtrack/%d/%d/%d.png";

    public static final String URL_FORMAT_NLSC_PHOTO2 =
            "http://wmts.nlsc.gov.tw/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0" +
                    "&LAYER=PHOTO2&STYLE=_null&TILEMATRIXSET=EPSG:3857" +
                    "&TILEMATRIX=EPSG:3857:%d&TILECOL=%d&TILEROW=%d&FORMAT=image/png";

    public static final String URL_FORMAT_NLSC_TOWN =
            "http://wmts.nlsc.gov.tw/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0" +
                    "&LAYER=TOWN&STYLE=_null&TILEMATRIXSET=EPSG:3857" +
                    "&TILEMATRIX=EPSG:3857:%d&TILECOL=%d&TILEROW=%d&FORMAT=image/png";
}
