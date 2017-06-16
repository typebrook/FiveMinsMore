package io.typebrook.fiveminsmore.Cluster;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.TextView;

import io.typebrook.fiveminsmore.Cluster.CustomMarker;
import io.typebrook.fiveminsmore.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
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
    private Activity mActivity;
    private GoogleMap mMap;

    public CustomRenderer(Activity activity, GoogleMap map, ClusterManager<CustomMarker> clusterManager) {
        super(activity, map, clusterManager);
        mActivity = activity;
        mMap = map;

        // Define Custom icon
        mIconGenerator = new IconGenerator(activity);
        mIconGenerator.setBackground(activity.getResources().getDrawable(R.drawable.ic_waypt));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<CustomMarker> cluster) {
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
        TextView tv = (TextView) mActivity.getLayoutInflater().inflate(R.layout.view_cluster_item, null);
        tv.setText(item.getTitle());
        mIconGenerator.setContentView(tv);
        Bitmap icon = mIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }
}
