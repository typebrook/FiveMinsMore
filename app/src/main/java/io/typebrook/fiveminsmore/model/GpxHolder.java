package io.typebrook.fiveminsmore.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import io.typebrook.fiveminsmore.MapsManager;
import io.typebrook.fiveminsmore.R;
import com.github.johnkil.print.PrintView;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.clustering.ClusterManager;
import com.unnamed.b.atv.model.TreeNode;

import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;

/**
 * Created by pham on 2017/4/30.
 */

public class GpxHolder extends TreeNode.BaseNodeViewHolder<GpxHolder.IconTreeItem> {
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
    public View createNodeView(final TreeNode node, final IconTreeItem value) {
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

        nodeSelector = (CheckBox) view.findViewById(R.id.node_selector);
        nodeSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                            value.polyline = GpxUtils.drawTrack(value.track, manager.getCurrentMap());
                        } else if (value.polyline != null) {
                            value.polyline.remove();
                        }
                        break;

                    case ITEM_TYPE_WAYPOINT:
                        ClusterManager<CustomMarker> clusterManager = manager.getCurrentClusterManager();
                        if (isChecked) {
                            value.marker = GpxUtils.drawWaypt(value.wpt, clusterManager);
                            clusterManager.cluster();
                        } else if (value.marker != null) {
                            clusterManager.removeItem(value.marker);
                            clusterManager.cluster();
                        }
                        break;
                }
            }
        });
        nodeSelector.setChecked(node.isSelected());

        return view;
    }

    // 展開後讓箭頭向下
    @Override
    public void toggle(boolean active) {
        arrowView.setIconText(context.getResources().getString(active ? R.string.ic_keyboard_arrow_down : R.string.ic_keyboard_arrow_right));
    }

    // 增加CheckBox
    @Override
    public void toggleSelectionMode(boolean editModeEnabled) {
        nodeSelector.setVisibility(editModeEnabled ? View.VISIBLE : View.GONE);
        nodeSelector.setChecked(mNode.isSelected());
    }

    public static class IconTreeItem {
        public int type;
        public int icon;
        public String text;

        // attribute for WayPoint
        public WayPoint wpt;
        public CustomMarker marker;

        // attribute for Track
        public Track track;
        public Polyline polyline;

        public IconTreeItem(Builder builder) {
            type = builder.type;
            icon = builder.icon;
            text = builder.text;
            wpt = builder.wpt;
            track = builder.track;
            polyline = builder.polyline;
        }

        public static class Builder {
            private int type;
            private int icon;
            private String text;
            private WayPoint wpt;
            private Track track;
            private Polyline polyline;

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

            public Builder setPolyline(Polyline polyline) {
                this.polyline = polyline;
                return this;
            }

            public IconTreeItem build() {
                return new IconTreeItem(this);
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