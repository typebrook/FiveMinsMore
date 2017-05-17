package io.typebrook.fiveminsmore.gpx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.johnkil.print.PrintView;
import com.google.android.gms.maps.model.Polyline;
import com.unnamed.b.atv.model.TreeNode;

import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;
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
    private CheckBox nodeSelector_main_map;
    private CheckBox nodeSelector_sub_map;
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
        textView.setText(value.text);

        arrowView = (PrintView) view.findViewById(R.id.arrow_icon);
        if (node.isLeaf()) {
            arrowView.setVisibility(View.GONE);
        }

        nodeSelector_main_map = (CheckBox) view.findViewById(R.id.node_selector);
        nodeSelector_main_map.setOnCheckedChangeListener(getCheckListener(node, value, 0));

        if (manager != null && manager.isSubMapOn()) {
            nodeSelector_sub_map = (CheckBox) view.findViewById(R.id.node_selector_sub_map);
            nodeSelector_sub_map.setVisibility(View.VISIBLE);
            nodeSelector_sub_map.setOnCheckedChangeListener(getCheckListener(node, value, 1));
        }

        view.findViewById(R.id.btn_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodeSelector_main_map.setChecked(false);
                if (nodeSelector_sub_map != null)
                   nodeSelector_sub_map.setChecked(false);
                getTreeView().removeNode(node);
            }
        });


        return view;
    }

    private CompoundButton.OnCheckedChangeListener getCheckListener
            (final TreeNode node, final GpxTreeItem value, final int mapCode){
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                node.setSelected(isChecked);
                for (TreeNode n : node.getChildren()) {
                    getTreeView().selectNode(n, isChecked);
                }

                switch (value.type) {
                    case ITEM_TYPE_GPX:
                        break;
                    case ITEM_TYPE_TRACK:
                        if (isChecked) {
                            if (value.polylines[mapCode] == null) {
                                value.polylines[mapCode] =
                                        GpxUtils.drawTrack(value.track, manager.getMap(mapCode));
                            } else {
                                value.polylines[mapCode].setVisible(true);
                            }
                            MapUtils.zoomToPolyline(manager.getMap(mapCode),
                                    value.polylines[mapCode]);
                        } else {
                            value.polylines[mapCode].setVisible(false);
                        }

                        break;

                    case ITEM_TYPE_WAYPOINT:
                        if (isChecked) {
                            value.markers[mapCode] =
                                    GpxUtils.drawWaypt(value.wpt, manager);
                            manager.getClusterManager(mapCode).cluster();

                            MapUtils.zoomToMarker(manager.getMap(mapCode),
                                    value.markers[mapCode]);
                        } else {
                            manager.getClusterManager(mapCode).removeItem(
                                    value.markers[mapCode]);
                            manager.getClusterManager(mapCode).cluster();
                            value.markers[mapCode] = null;
                        }

                        break;
                }
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
        nodeSelector_main_map.setChecked(mNode.isSelected());
        if (nodeSelector_sub_map != null)
            nodeSelector_sub_map.setChecked(mNode.isSelected());
    }

    public static class GpxTreeItem {
        public int type;
        public int icon;
        public String text;

        // attribute for WayPoint
        public WayPoint wpt;
        public CustomMarker[] markers = {null, null};

        // TODO add polylineOptions into attribute
        // attribute for Track
        public Track track;
        public Polyline[] polylines = {null, null};

        public GpxTreeItem(Builder builder) {
            type = builder.type;
            icon = builder.icon;
            text = builder.text;
            wpt = builder.wpt;
            track = builder.track;
        }

        public static class Builder {
            private int type;
            private int icon;
            private String text;
            private WayPoint wpt;
            private Track track;

            public Builder setType(int type) {
                this.type = type;
                return this;
            }

            public Builder setIcon(int icon) {
                this.icon = icon;
                return this;
            }

            public Builder setText(String text) {
                this.text = text;
                return this;
            }

            public Builder setWayPoint(WayPoint wpt) {
                this.wpt = wpt;
                return this;
            }

            public Builder setTrack(Track track) {
                this.track = track;
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
    public static final int ITEM_ICON_GPX = R.drawable.ic_folder_black_24dp;
    public static final int ITEM_ICON_WAYPOINT = R.drawable.ic_place_black_24dp;
    public static final int ITEM_ICON_TRACK = R.drawable.ic_timeline_black_24dp;

}