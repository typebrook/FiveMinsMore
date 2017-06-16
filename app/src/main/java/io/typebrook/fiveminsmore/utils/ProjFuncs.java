package io.typebrook.fiveminsmore.utils;

import com.google.android.gms.maps.model.LatLng;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import java.util.Locale;

import io.typebrook.fiveminsmore.res.CoorSysList;

import static io.typebrook.fiveminsmore.Constant.COOR_TWD67;
import static io.typebrook.fiveminsmore.Constant.COOR_TWD97;
import static io.typebrook.fiveminsmore.Constant.COOR_WGS84_D;
import static io.typebrook.fiveminsmore.Constant.COOR_WGS84_DMS;

/**
 * Created by pham on 2017/6/3.
 */

public class ProjFuncs {
    static CRSFactory mCsFactory = new CRSFactory();
    static CoordinateTransformFactory mCtFactory = new CoordinateTransformFactory();

    static String EPSG_WGS84 = "EPSG:4326";
    static String EPSG_TWD97 = "EPSG:3826";
    static String EPSG_TWD67 = "EPSG:3828";

    static String FUNC_WGS84 = "+proj=longlat +datum=WGS84 +no_defs";
    static String FUNC_TWD97 = "+proj=tmerc +lat_0=0 +lon_0=121 +k=0.9999 +x_0=250000 +y_0=0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=公尺 +no_defs";
    static String FUNC_TWD67 = "+proj=tmerc  +towgs84=-752,-358,-179,-.0000011698,.0000018398,.0000009822,.00002329 +lat_0=0 +lon_0=121 +x_0=250000 +y_0=0 +k=0.9999 +ellps=aust_SA  +units=公尺";

    public static ProjCoordinate latlon2twd97(LatLng latLng) {

        CoordinateReferenceSystem crs1 = mCsFactory.createFromParameters(EPSG_WGS84, FUNC_WGS84);
        CoordinateReferenceSystem crs2 = mCsFactory.createFromParameters(EPSG_TWD97, FUNC_TWD97);
        CoordinateTransform trans = mCtFactory.createTransform(crs1, crs2);
        ProjCoordinate p1 = new ProjCoordinate();
        ProjCoordinate p2 = new ProjCoordinate();
        p1.x = latLng.longitude;
        p1.y = latLng.latitude;
        trans.transform(p1, p2);

        return p2;
    }

    public static ProjCoordinate latlon2twd67(LatLng latLng) {

        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CRSFactory csFactory = new CRSFactory();
        CoordinateReferenceSystem crs1 = csFactory.createFromParameters(EPSG_WGS84, FUNC_WGS84);
        CoordinateReferenceSystem crs2 = csFactory.createFromParameters(EPSG_TWD67, FUNC_TWD67);
        CoordinateTransform trans = ctFactory.createTransform(crs1, crs2);
        ProjCoordinate p1 = new ProjCoordinate();
        ProjCoordinate p2 = new ProjCoordinate();
        p1.x = latLng.longitude;
        p1.y = latLng.latitude;
        trans.transform(p1, p2);

        return p2;
    }

    public static String twd2String(ProjCoordinate coor) {
        String x = (int) coor.x + "";
        x = x.substring(0, x.length() - 3) + "-" + x.substring(x.length() - 3);

        String y = (int) coor.y + "";
        y = y.substring(0, y.length() - 3) + "-" + y.substring(x.length() - 3);

        return x + ", " + y;
    }

    public static String latLng2DString(LatLng latLng) {
        return latLng2DString(latLng.latitude, latLng.longitude);
    }

    public static String latLng2DString(Double latitude, Double longitude) {
        String lat = simpleLatLng(latitude);
        String lon = simpleLatLng(longitude);

        return "北緯 " + lat + "\n" + "東經 " + lon;
    }

    public static String latLng2DmsString(LatLng latLng) {
        return latLng2DmsString(latLng.latitude, latLng.longitude);
    }

    public static String latLng2DmsString(Double latitude, Double longitude) {
        String lat = Degree2Dms(latitude);
        String lon = Degree2Dms(longitude);

        return "北緯 " + lat + "\n" + "東經 " + lon;
    }

    // Degree to Degree/Minute/Second format
    private static String Degree2Dms(Double d) {
        int dValue = d.intValue();
        int mValue = Double.valueOf((d - dValue) * 60).intValue();
        float minute2Degree = ((float) mValue) / 60;
        Double sValue = (d - dValue - minute2Degree) * 3600;

        return dValue + "度" + mValue + "分" +
                String.format(Locale.getDefault(), "%.1f", sValue) + "秒";
    }

    public static String simpleLatLng(Double num) {
        return String.format(Locale.getDefault(), "%.6f", num);
    }

    public static String showCurrentCoor(LatLng latLng) {
        switch (CoorSysList.current_coor_sys) {
            case COOR_WGS84_D:
                return latLng2DString(latLng);

            case COOR_WGS84_DMS:
                return latLng2DmsString(latLng);

            case COOR_TWD97:
                return twd2String(latlon2twd97(latLng));

            case COOR_TWD67:
                return twd2String(latlon2twd67(latLng));

            default:
                return latLng2DString(latLng);
        }
    }
}
