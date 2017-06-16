package io.typebrook.fiveminsmore.model;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import io.typebrook.fiveminsmore.R;
import io.typebrook.fiveminsmore.utils.ProjFuncs;

/**
 * Created by pham on 2017/6/5.
 */

public class DetailDialog extends DialogFragment implements View.OnClickListener {
    private String mTitle;
    private LatLng mLatLng;

    public void setArgs(Context context, Marker marker) {
        this.mTitle = marker.getTitle();
        this.mLatLng = marker.getPosition();
    }

    @Override
    public Dialog onCreateDialog(Bundle values) {
        if (values != null){
            mTitle = values.getString("title");
            mLatLng = new LatLng(values.getDouble("latitude"), values.getDouble("longitude"));
        }

        // See the example: https://developer.android.com/guide/topics/ui/dialogs.html?hl=zh-tw#DialogFragment
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout detailView = (LinearLayout) inflater.inflate(R.layout.view_cluster_item_detail, null);
        builder.setView(detailView);

        ((TextView) detailView.findViewById(R.id.title)).setText(mTitle);
        String coor_wgs84 = ProjFuncs.latLng2DString(mLatLng);
        ((TextView) detailView.findViewById(R.id.wgs84)).setText(coor_wgs84);
        String coor_twd97 = ProjFuncs.twd2String(ProjFuncs.latlon2twd97(mLatLng));
        ((TextView) detailView.findViewById(R.id.twd97)).setText(coor_twd97);
        String coor_twd67 = ProjFuncs.twd2String(ProjFuncs.latlon2twd67(mLatLng));
        ((TextView) detailView.findViewById(R.id.twd67)).setText(coor_twd67);

        detailView.findViewById(R.id.download_trk_file).setOnClickListener(this);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", mTitle);
        outState.putDouble("latitude", mLatLng.latitude);
        outState.putDouble("longitude", mLatLng.longitude);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download_trk_file:
                String url = "http://map.happyman.idv.tw/twmap/api/waypoints.php?x=" +
                        ProjFuncs.simpleLatLng(mLatLng.longitude) + "&y=" +
                        ProjFuncs.simpleLatLng(mLatLng.latitude) +
                        "&r=100&detail=1#";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                getActivity().startActivity(i);
                break;

            case R.id.goto_twmap:
                break;
        }
    }
}
