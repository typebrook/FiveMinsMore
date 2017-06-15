package io.typebrook.fiveminsmore.InfoWindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import io.typebrook.fiveminsmore.R;

/**
 * Created by pham on 2017/6/15.
 */

public class CustomAdapter implements GoogleMap.InfoWindowAdapter {
    private LayoutInflater inflater;
    private Context context;

    public CustomAdapter(Context context){
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Get info window view from the layout file
        View v = inflater.inflate(R.layout.view_info_window, null);

        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView address = (TextView) v.findViewById(R.id.snippet);
        address.setText(marker.getSnippet());

        return v;
    }
}
