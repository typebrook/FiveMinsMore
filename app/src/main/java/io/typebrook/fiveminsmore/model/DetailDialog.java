package io.typebrook.fiveminsmore.model;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.osgeo.proj4j.ProjCoordinate;

import io.typebrook.fiveminsmore.R;
import io.typebrook.fiveminsmore.utils.ProjFuncs;

/**
 * Created by pham on 2017/6/5.
 */

public class DetailDialog extends DialogFragment {
    String mTitle;
    LatLng mLatLng;

    public void setArgs(String title, LatLng latLng) {
        this.mTitle = title;
        this.mLatLng = latLng;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // See the example: https://developer.android.com/guide/topics/ui/dialogs.html?hl=zh-tw#DialogFragment
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout detailView = (LinearLayout) inflater.inflate(R.layout.view_cluster_item_detail, null);
        builder.setView(detailView);

        ((TextView) detailView.findViewById(R.id.title)).setText(mTitle);
        String coor_wgs84 = ProjFuncs.wgs2String(mLatLng);
        ((TextView) detailView.findViewById(R.id.wgs84)).setText(coor_wgs84);
        String coor_twd97 = ProjFuncs.twd2String(ProjFuncs.latlon2twd97(mLatLng));
        ((TextView) detailView.findViewById(R.id.twd97)).setText(coor_twd97);
        String coor_twd67 = ProjFuncs.twd2String(ProjFuncs.latlon2twd67(mLatLng));
        ((TextView) detailView.findViewById(R.id.twd67)).setText(coor_twd67);

        return builder.create();
    }
}
