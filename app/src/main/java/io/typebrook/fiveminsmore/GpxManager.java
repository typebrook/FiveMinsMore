package io.typebrook.fiveminsmore;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import io.typebrook.fiveminsmore.gpx.GpxHolder;
import io.typebrook.fiveminsmore.gpx.GpxUtils;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import io.ticofab.androidgpxparser.parser.domain.Gpx;

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

    public GpxManager(Activity context) {
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
        layoutParams.setMargins(40, 40, 40, 40);

        managerView.setLayoutParams(layoutParams);

        Button importGpxBtn = (Button) managerView.findViewById(R.id.btn_pick_gpx_files);
        importGpxBtn.setOnClickListener((MapsActivity) mContext);
        Button leaveDialog = (Button) managerView.findViewById(R.id.leave_gpx_manager);
        leaveDialog.setOnClickListener((MapsActivity) mContext);

        ViewGroup container = ((ViewGroup) mContext.findViewById(R.id.container));
        container.addView(managerView, container.getChildCount());
    }

    void renewDialog() {
        treeView.selectAll(true);
        treeView.expandAll();
        treeView.collapseAll();
    }

    void removeDialog() {
        isShowingDialog = false;
        ((ViewGroup) mContext.findViewById(R.id.container)).removeView(managerView);
    }

    public Gpx add(File file, MapsManager manager) {
        try {
            Gpx gpx = GpxUtils.parseGpx(new FileInputStream(file));

            TreeNode gpxRoot = GpxUtils.getTreeNode(file.getName(), gpx);
            gpxRoot.setViewHolder(new GpxHolder(mContext));

            treeView.addNode(root, gpxRoot);

            for (TreeNode node : gpxRoot.getChildren()) {
                node.setViewHolder(new GpxHolder(mContext, manager));
            }

            return gpx;
        } catch (Exception e) {
            Log.d(TAG, "Fail to load GPX file " + file.getName());
            return null;
        }
    }

    public boolean isShowingDialog() {
        return isShowingDialog;
    }

    public TreeNode getGpxTree(){
        return root;
    }
}
