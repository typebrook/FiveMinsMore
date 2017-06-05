package io.typebrook.fiveminsmore.filepicker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.typebrook.fiveminsmore.R;
import com.vincent.filepicker.ToastUtil;
import com.vincent.filepicker.Util;
import com.vincent.filepicker.adapter.BaseAdapter;
import com.vincent.filepicker.filter.entity.NormalFile;

import java.util.ArrayList;

/**
 * Created by Vincent Woo
 * Date: 2016/10/26
 * Time: 10:23
 */

public class CustomFilePickAdapter extends BaseAdapter<NormalFile, CustomFilePickAdapter.CustomFilePickViewHolder> {
    private int mMaxNumber;
    private int mCurrentNumber = 0;

    public CustomFilePickAdapter(Context ctx, int max) {
        this(ctx, new ArrayList<NormalFile>(), max);
    }

    public CustomFilePickAdapter(Context ctx, ArrayList<NormalFile> list, int max) {
        super(ctx, list);
        mMaxNumber = max;
    }

    @Override
    public CustomFilePickViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_normal_file_pick, parent, false);
        return new CustomFilePickViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CustomFilePickViewHolder holder, final int position) {
        final NormalFile file = mList.get(position);

        holder.mTvTitle.setText(Util.extractFileNameWithSuffix(file.getPath()));
        holder.mTvTitle.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        if (holder.mTvTitle.getMeasuredWidth() >
                Util.getScreenWidth(mContext) - Util.dip2px(mContext, 10 + 32 + 10 + 48 + 10 * 2)) {
            holder.mTvTitle.setLines(2);
        } else {
            holder.mTvTitle.setLines(1);
        }

        if (file.isSelected()) {
            holder.mCbx.setSelected(true);
        } else {
            holder.mCbx.setSelected(false);
        }

        holder.mTvPath.setText(convert2SdcardPath(Util.extractPathWithSeparator(file.getPath())));

        if (file.getPath().endsWith("gpx")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_gpx_96);
        } else if (file.getPath().endsWith("kml")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_kml_96);
        } else {
            holder.mIvIcon.setImageResource(R.drawable.ic_file);
        }

        holder.mCbx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isSelected() && isUpToMax()) {
                    ToastUtil.getInstance(mContext).showToast(R.string.up_to_max);
                    return;
                }

                if (v.isSelected()) {
                    holder.mCbx.setSelected(false);
                    mCurrentNumber--;
                } else {
                    holder.mCbx.setSelected(true);
                    mCurrentNumber++;
                }

                mList.get(holder.getAdapterPosition()).setSelected(holder.mCbx.isSelected());

                if (mListener != null) {
                    mListener.OnSelectStateChanged(holder.mCbx.isSelected(), mList.get(holder.getAdapterPosition()));
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class CustomFilePickViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvIcon;
        private TextView mTvTitle;
        private TextView mTvPath;
        private ImageView mCbx;

        public CustomFilePickViewHolder(View itemView) {
            super(itemView);
            mIvIcon = (ImageView) itemView.findViewById(R.id.ic_file);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_file_title);
            mTvPath = (TextView) itemView.findViewById(R.id.tv_file_path);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
        }
    }

    private boolean isUpToMax() {
        return mCurrentNumber >= mMaxNumber;
    }

    private String convert2SdcardPath(String path) {
        if (path.contains("/storage/emulated/0"))
            path = path.replace("/storage/emulated/0", "/sdcard");
        return path;
    }

}
