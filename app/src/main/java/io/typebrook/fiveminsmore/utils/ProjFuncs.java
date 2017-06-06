package io.typebrook.fiveminsmore.utils;

import android.content.Context;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import java.util.Locale;

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

    public static String twd2String(ProjCoordinate coor){
        return (int) coor.x + ", " + (int) coor.y;
    }

    public static String latLng2String(LatLng latLng){
        String lat = simpleLatLng(latLng.latitude);
        String lon = simpleLatLng(latLng.longitude);

        return "東經" + lon + "度，" + "北緯" + lat + "度";
    }

    public static String simpleLatLng(Double num){
        return String.format(Locale.getDefault(), "%.6f", num);
    }
}
