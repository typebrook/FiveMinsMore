package io.typebrook.fiveminsmore.gpx;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.johnkil.print.PrintView;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.kml.KmlLayer;
import com.unnamed.b.atv.model.TreeNode;

import java.util.Map;

import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;
import io.typebrook.fiveminsmore.MapsManager;
import io.typebrook.fiveminsmore.R;
import io.typebrook.fiveminsmore.Cluster.CustomMarker;
import io.typebrook.fiveminsmore.utils.MapUtils;

import static io.typebrook.fiveminsmore.Constant.CHOSEN_TRACK_COLOR;
import static io.typebrook.fiveminsmore.Constant.DEFAULT_TRACK_COLOR;
import static io.typebrook.fiveminsmore.Constant.ZINDEX_POLYLINE;
import static io.typebrook.fiveminsmore.Constant.ZINDEX_POLYLINE_CHOSEN;

/**
 * Created by pham on 2017/4/30.
 */

public class GpxHolder extends TreeNode.BaseNodeViewHolder<GpxHolder.GpxTreeItem> {
    public static final int ITEM_TYPE_GPX = 1;
    public static final int ITEM_TYPE_TRACK = 2;
    public static final int ITEM_TYPE_WAYPOINT = 3;
    public static final int ITEM_TYPE_WAYPOINTS = 4;
    public static final int ITEM_TYPE_KML = 5;
    public static final int ITEM_ICON_GPX = R.drawable.ic_folder_black_24dp;
    public static final int ITEM_ICON_WAYPOINT = R.drawable.ic_place_black_24dp;
    public static final int ITEM_ICON_TRACK = R.drawable.ic_timeline_black_24dp;
    private static final String TAG = "GpxHolder";
    public static Polyline lastClickedPolyline;
    private PrintView arrowView;
    private CheckBox nodeSelector;
    private MapsManager manager;

    public GpxHolder(Context context) {
        super(context);
    }

    public GpxHolder(Context context, MapsManager manager) {
        super(context);
        this.manager = manager;
    }

    @Override
    public View createNodeView(final TreeNode node, final GpxTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.holder_gpx_tree, null, false);

        ImageView iconView = (ImageView) view.findViewById(R.id.tree_item_icon);
        iconView.setImageResource(value.icon);

        TextView textView = (TextView) view.findViewById(R.id.tree_item_text);
        textView.setText(value.gpxName);

        arrowView = (PrintView) view.findViewById(R.id.arrow_icon);
        if (node.isLeaf()) {
            arrowView.setVisibility(View.GONE);
        }

        nodeSelector = (CheckBox) view.findViewById(R.id.node_selector);
        nodeSelector.setOnCheckedChangeListener(getCheckListener(node, value));

        view.findViewById(R.id.btn_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodeSelector.setChecked(false);
                getTreeView().removeNode(node);
                if (value.type == ITEM_TYPE_WAYPOINT || value.type == ITEM_TYPE_WAYPOINTS)
                    manager.clusterTheMarkers();
            }
        });

        if (node.isLeaf())
            view.setOnClickListener(getClickListener(value));

        return view;
    }

    private View.OnClickListener getClickListener(final GpxTreeItem value) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (value.type) {
                    case ITEM_TYPE_TRACK:
                        Polyline polyline = value.polylines[0];
                        MapUtils.zoomToPolyline(manager.getCurrentMap(), polyline);

                        value.polylines[0].setColor(CHOSEN_TRACK_COLOR);
                        value.polylines[0].setZIndex(ZINDEX_POLYLINE_CHOSEN);
                        if (lastClickedPolyline != null) {
                            lastClickedPolyline.setColor(DEFAULT_TRACK_COLOR);
                            lastClickedPolyline.setZIndex(ZINDEX_POLYLINE);
                        }
                        lastClickedPolyline = polyline;
                        break;
                    case ITEM_TYPE_WAYPOINT:
                        MapUtils.zoomToMarker(manager.getCurrentMap(),
                                value.marker);
                        break;
                }
            }
        };
    }

    private CompoundButton.OnCheckedChangeListener getCheckListener
            (final TreeNode node, final GpxTreeItem value) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                node.setSelected(isChecked);

                // 將子節點設為相同狀態
                for (TreeNode n : node.getChildren()) {
                    getTreeView().selectNode(n, isChecked);
                }
                Log.d(TAG, "type = " + value.type);
                for (int mapCode = 0; mapCode < manager.getMapsNum(); mapCode++) {
                    switch (value.type) {
                        case ITEM_TYPE_TRACK:
                            if (isChecked) {
                                value.polylines[mapCode] = manager.getMap(mapCode)
                                        .addPolyline(value.trkOpts);

                                MapUtils.zoomToPolyline(manager.getMap(mapCode),
                                        value.polylines[mapCode]);
                            } else if (value.polylines[mapCode] == null) {
                                break;
                            } else {
                                value.polylines[mapCode].remove();
                                value.polylines[mapCode] = null;
                            }
                            break;

                        case ITEM_TYPE_WAYPOINT:
                            if (isChecked) {
                                value.isMarkers[mapCode] = true;
                                manager.getClusterManagers().get(mapCode).addItem(value.marker);
                            } else if (!value.isMarkers[mapCode]) {
                                break;
                            } else {
                                value.isMarkers[mapCode] = false;
                                manager.getClusterManagers().get(mapCode).removeItem(value.marker);
                            }
                            break;

                        case ITEM_TYPE_KML:
                            if (value.kmllayer == null)
                                break;
                            if (isChecked) {
                                try {
                                    value.kmllayer.addLayerToMap();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                value.kmllayer.removeLayerFromMap();
                            }
                    }
                }
                manager.clusterTheMarkers();
            }
        };
    }

    // 展開後讓箭頭向下
    @Override
    public void toggle(boolean active) {
        arrowView.setIconText(context.getResources().getString(active ? R.string.ic_keyboard_arrow_down : R.string.ic_keyboard_arrow_right));
    }

    // 增加CheckBox
    @Override
    public void toggleSelectionMode(boolean editModeEnabled) {
        nodeSelector.setChecked(mNode.isSelected());
    }

    public static class GpxTreeItem {
        public int type;
        public int icon;
        public String gpxName;
        public String path;

        // attribute for GPX
        public Gpx gpx;

        // attribute for WayPoint
        public CustomMarker marker;
        public boolean[] isMarkers = {false, false};

        // attribute for Track
        public PolylineOptions trkOpts;
        public Polyline[] polylines = {null, null};

        // attribute for kml
        public KmlLayer kmllayer;

        public GpxTreeItem(Builder builder) {
            type = builder.type;
            icon = builder.icon;
            gpxName = builder.gpxName;
            gpx = builder.gpx;
            path = builder.gpxPath;
            marker = builder.marker;
            trkOpts = builder.trkOpts;
            kmllayer = builder.kmllayer;
        }

        public static class Builder {
            private int type;
            private int icon;
            private String gpxName;
            private Gpx gpx;
            private String gpxPath;
            private CustomMarker marker;
            private PolylineOptions trkOpts;
            private KmlLayer kmllayer;

            public GpxTreeItem build() {
                return new GpxTreeItem(this);
            }

            public Builder setType(int type) {
                this.type = type;
                return this;
            }

            public Builder setIcon(int icon) {
                this.icon = icon;
                return this;
            }

            public Builder setName(String gpxName) {
                this.gpxName = gpxName;
                return this;
            }

            public Builder setPath(String gpxPath) {
                this.gpxPath = gpxPath;
                return this;
            }

            public Builder setGpx(Gpx gpx) {
                this.gpx = gpx;
                return this;
            }

            public Builder setMarker(WayPoint wpt) {
                this.marker = GpxUtils.waypt2Marker(wpt);
                return this;
            }

            public Builder setTrkOpts(PolylineOptions opts) {
                this.trkOpts = opts;
                return this;
            }

            public Builder setKmllayer(KmlLayer layer) {
                this.kmllayer = layer;
                return this;
            }
        }
    }

}