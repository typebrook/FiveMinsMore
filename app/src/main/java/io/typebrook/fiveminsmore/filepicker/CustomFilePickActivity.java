package io.typebrook.fiveminsmore.filepicker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.vincent.filepicker.Constant;
import com.vincent.filepicker.DividerListItemDecoration;
import com.vincent.filepicker.adapter.OnSelectStateListener;
import com.vincent.filepicker.filter.FileFilter;
import com.vincent.filepicker.filter.callback.FilterResultCallback;
import com.vincent.filepicker.filter.entity.Directory;
import com.vincent.filepicker.filter.entity.NormalFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pham on 2017/5/7.
 */

public class CustomFilePickActivity extends BaseActivity {
    public static final int DEFAULT_MAX_NUMBER = 9;
    public static final String SUFFIX = "Suffix";
    private int mMaxNumber;
    private Toolbar mTbImagePick;
    private RecyclerView mRecyclerView;
    private CustomFilePickAdapter mAdapter;
    private ArrayList<NormalFile> mSelectedList = new ArrayList<>();
    private ProgressBar mProgressBar;
    private String[] mSuffix;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(com.vincent.filepicker.R.layout.activity_file_pick);

        mMaxNumber = getIntent().getIntExtra(Constant.MAX_NUMBER, DEFAULT_MAX_NUMBER);
        mSuffix = getIntent().getStringArrayExtra(SUFFIX);

        super.onCreate(savedInstanceState);
    }

    @Override
    void permissionGranted() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, 1000);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        mTbImagePick = (Toolbar) findViewById(com.vincent.filepicker.R.id.tb_file_pick);
        mTbImagePick.setTitle("請選擇檔案");
        setSupportActionBar(mTbImagePick);
        mTbImagePick.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(com.vincent.filepicker.R.id.rv_file_pick);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerListItemDecoration(this,
                LinearLayoutManager.VERTICAL, com.vincent.filepicker.R.drawable.divider_rv_file));
        mAdapter = new CustomFilePickAdapter(this, mMaxNumber);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnSelectStateListener(new OnSelectStateListener<NormalFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, NormalFile file) {
                if (state)
                    mSelectedList.add(file);
                else
                    mSelectedList.remove(file);
            }
        });

        mProgressBar = (ProgressBar) findViewById(com.vincent.filepicker.R.id.pb_file_pick);
    }

    private void loadData() {
        FileFilter.getFiles(this, new FilterResultCallback<NormalFile>() {
            @Override
            public void onResult(List<Directory<NormalFile>> directories) {
                mProgressBar.setVisibility(View.GONE);
                List<NormalFile> list = new ArrayList<>();
                for (Directory<NormalFile> directory : directories) {
                    list.addAll(directory.getFiles());
                }

                for (NormalFile file : mSelectedList) {
                    int index = list.indexOf(file);
                    if (index != -1) {
                        list.get(index).setSelected(true);
                    }
                }

                mAdapter.refresh(list);
            }
        }, mSuffix);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.vincent.filepicker.R.menu.menu_image_pick, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == com.vincent.filepicker.R.id.action_done) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Constant.RESULT_PICK_FILE, mSelectedList);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
