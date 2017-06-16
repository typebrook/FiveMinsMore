package io.typebrook.fiveminsmore.res;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import io.typebrook.fiveminsmore.Constant;
import io.typebrook.fiveminsmore.MapsActivity;

import static io.typebrook.fiveminsmore.Constant.COOR_WGS84_D;

/**
 * Created by pham on 2017/4/9.
 */

public class CoorSysList {
    public static int coorSetting;

    public static void setCoorSys(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("選擇座標系統")
                .setItems(Constant.COOR_METHODS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        coorSetting = which;
                        ((MapsActivity) context).getMapsManager().onCameraMove();
                        dialog.cancel();
                    }
                });

        builder.show();
    }
}