package io.typebrook.fiveminsmore.gpx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.johnkil.print.PrintView;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.unnamed.b.atv.model.TreeNode;

import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;
import io.typebrook.fiveminsmore.MapsActivity;
import io.typebrook.fiveminsmore.MapsManager;
import io.typebrook.fiveminsmore.R;
import io.typebrook.fiveminsmore.model.CustomMarker;
import io.typebrook.fiveminsmore.utils.MapUtils;

/**
 * Created by pham on 2017/4/30.
 */

public class GpxHolder extends TreeNode.BaseNodeViewHolder<GpxHolder.GpxTreeItem> {
    private static final String TAG = "GpxHolder";
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
                        MapUtils.zoomToPolyline(manager.getCurrentMap(),
                                value.polylines[0]);
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
                for (TreeNode n : node.getChildren()) {
                    getTreeView().selectNode(n, isChecked);
                }

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

        // attribute for GPX
        public Gpx gpx;
        public String gpxPath;

        // attribute for WayPoint
        public CustomMarker marker;
        public boolean[] isMarkers = {false, false};

        // attribute for Track
        public PolylineOptions trkOpts;
        public Polyline[] polylines = {null, null};

        public GpxTreeItem(Builder builder) {
            type = builder.type;
            icon = builder.icon;
            gpxName = builder.gpxName;
            gpx = builder.gpx;
            gpxPath = builder.gpxPath;
            marker = builder.marker;
            trkOpts = builder.trkOpts;
        }

        public static class Builder {
            private int type;
            private int icon;
            private String gpxName;
            private Gpx gpx;
            private String gpxPath;
            private CustomMarker marker;
            private PolylineOptions trkOpts;

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

            public GpxTreeItem build() {
                return new GpxTreeItem(this);
            }
        }
    }

    public static final int ITEM_TYPE_GPX = 1;
    public static final int ITEM_TYPE_TRACK = 2;
    public static final int ITEM_TYPE_WAYPOINT = 3;
    public static final int ITEM_TYPE_WAYPOINTS = 4;
    public static final int ITEM_ICON_GPX = R.drawable.ic_folder_black_24dp;
    public static final int ITEM_ICON_WAYPOINT = R.drawable.ic_place_black_24dp;
    public static final int ITEM_ICON_TRACK = R.drawable.ic_timeline_black_24dp;

}