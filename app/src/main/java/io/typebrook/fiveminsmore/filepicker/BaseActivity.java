package io.typebrook.fiveminsmore.filepicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Vincent Woo
 * Date: 2016/10/12
 * Time: 16:21
 */

public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int RC_READ_EXTERNAL_STORAGE = 123;
    private static final String TAG = com.vincent.filepicker.activity.BaseActivity.class.getName();

    abstract void permissionGranted();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readExternalStorage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Read external storage file
     */
    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE)
    private void readExternalStorage() {
        boolean isGranted = EasyPermissions.hasPermissions(this, "android.permission.READ_EXTERNAL_STORAGE");
        if (isGranted) {
            permissionGranted();
        } else {
            EasyPermissions.requestPermissions(this, getString(com.vincent.filepicker.R.string.rationale_storage),
                    RC_READ_EXTERNAL_STORAGE, "android.permission.READ_EXTERNAL_STORAGE");
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        permissionGranted();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        // If Permission permanently denied, ask user again
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            if (EasyPermissions.hasPermissions(this, "android.permission.READ_EXTERNAL_STORAGE")) {
                permissionGranted();
            } else {
                finish();
            }
        }
    }
}
