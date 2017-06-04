package io.typebrook.fiveminsmore.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.typebrook.fiveminsmore.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

/**
 * Created by pham on 2017/4/5.
 */

public class CustomRenderer extends DefaultClusterRenderer<CustomMarker> implements
        ClusterManager.OnClusterClickListener<CustomMarker> {

    private final IconGenerator mIconGenerator;
    private Context mContext;
    private GoogleMap mMap;

    public CustomRenderer(Context context, GoogleMap map, ClusterManager<CustomMarker> clusterManager) {
        super(context, map, clusterManager);
        mContext = context;
        mMap = map;

        // Define Custom icon
        mIconGenerator = new IconGenerator(context);
        mIconGenerator.setBackground(context.getResources().getDrawable(R.drawable.ic_waypt));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<CustomMarker> cluster) {
        LatLng pos1 = new LatLng(0, 0);
        LatLng pos2 = new LatLng(0, 0);

        // Always render clusters.
        for (CustomMarker item : cluster.getItems()) {
            if (pos1 == pos2)
                pos1 = item.getPosition();
            else
                pos2 = item.getPosition();
        }

//        if (SphericalUtil.computeDistanceBetween(pos1, pos2) > 20 && cluster.getSize() == 2){
//            return false;
//        }

        return cluster.getSize() > 1;
    }

    // When click on cluster, zoom to proper bound
    @Override
    public boolean onClusterClick(Cluster<CustomMarker> cluster) {
        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.
        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (CustomMarker item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    // For Custom icon
    @Override
    protected void onBeforeClusterItemRendered(CustomMarker item, MarkerOptions markerOptions) {
        // Set the info window to show their name.
//        Bitmap icon = mIconGenerator.makeIcon();
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());
        TextView tv = (TextView) ((Activity) mContext).getLayoutInflater().inflate(R.layout.cluster_item_text, null);
        tv.setText(item.getTitle());
        mIconGenerator.setContentView(tv);
        Bitmap icon = mIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onClusterItemRendered(CustomMarker clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
    }
}
