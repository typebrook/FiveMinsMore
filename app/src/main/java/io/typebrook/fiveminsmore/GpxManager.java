package io.typebrook.fiveminsmore;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import io.typebrook.fiveminsmore.gpx.GpxHolder;
import io.typebrook.fiveminsmore.gpx.GpxUtils;

import com.google.maps.android.data.kml.KmlLayer;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by pham on 2017/4/30.
 */

public class GpxManager {
    private final static String TAG = "GpxManager";

    private Activity mContext;

    private RelativeLayout managerView;
    private TreeNode root = TreeNode.root();
    private AndroidTreeView treeView;
    private boolean isShowingDialog = false;

    GpxManager(Activity context) {
        this.mContext = context;

        managerView = (RelativeLayout) mContext.getLayoutInflater().inflate(R.layout.manager_gpx, null);

        treeView = new AndroidTreeView(mContext, root);
        treeView.setDefaultAnimation(true);
        treeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        treeView.setUse2dScroll(true);
        treeView.setSelectionModeEnabled(true);

        LinearLayout contentView = (LinearLayout) managerView.findViewById(R.id.gpx_content);
        contentView.addView(treeView.getView());
    }

    void showDialog() {
        isShowingDialog = true;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        layoutParams.setMargins(50, 50, 50, 50);

        managerView.setLayoutParams(layoutParams);

        Button importGpxBtn = (Button) managerView.findViewById(R.id.btn_pick_gpx_files);
        importGpxBtn.setOnClickListener((MapsActivity) mContext);
        Button leaveDialog = (Button) managerView.findViewById(R.id.leave_gpx_manager);
        leaveDialog.setOnClickListener((MapsActivity) mContext);

        ViewGroup container = ((ViewGroup) mContext.findViewById(R.id.layout_container));
        container.addView(managerView, container.getChildCount());
    }

    void refreshDialog() {
        treeView.expandAll();
        treeView.collapseAll();
        treeView.selectAll(true);
    }

    void removeDialog() {
        isShowingDialog = false;
        ((ViewGroup) mContext.findViewById(R.id.layout_container)).removeView(managerView);
    }

    public void addGpxFile(File file, MapsManager manager) {
        try {
            TreeNode gpxRoot = GpxUtils.gpxFile2TreeNode(file);
            gpxRoot.setViewHolder(new GpxHolder(mContext, manager));

            treeView.addNode(root, gpxRoot);

            for (TreeNode node : gpxRoot.getChildren()) {
                node.setViewHolder(new GpxHolder(mContext, manager));
                for (TreeNode n : node.getChildren()) {
                    n.setViewHolder(new GpxHolder(mContext, manager));
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "Fail to load GPX file " + file.getName());
        }
    }

    public void addKmlFile(File file, MapsManager manager) {
        try {
            // TODO temporary method to deal with kml file
            GpxHolder.GpxTreeItem.Builder kml_builder = new GpxHolder.GpxTreeItem.Builder();
            InputStream kmlStream = new FileInputStream(file);
            TreeNode kmlRoot = new TreeNode(kml_builder
                    .setType(GpxHolder.ITEM_TYPE_KML)
                    .setIcon(GpxHolder.ITEM_ICON_GPX)
                    .setName(file.getName())
                    .setPath(file.getPath())
                    .setKmllayer(new KmlLayer(manager.getCurrentMap(), kmlStream, mContext))
                    .build());
            kmlRoot.setViewHolder(new GpxHolder(mContext, manager));

            treeView.addNode(root, kmlRoot);

        } catch (Exception e) {
            Log.d(TAG, "Fail to load GPX file " + file.getName());
        }
    }

    public void addGpxFile(TreeNode node, MapsManager manager) {
        node.setViewHolder(new GpxHolder(mContext, manager));
        treeView.addNode(root, node);

    }

    public boolean isShowingDialog() {
        return isShowingDialog;
    }

    public TreeNode getGpxTree() {
        return root;
    }

    public Set<String> getGpxList() {
        Set<String> list = new TreeSet<>();
        for (TreeNode node : root.getChildren()){
            list.add(((GpxHolder.GpxTreeItem)node.getValue()).path);
        }
        return list;
    }
}
