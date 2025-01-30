package com.app.wordpressnewsapps.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class ActivityImageDetail extends AppCompatActivity {

    PhotoView postImage;
    String strImage;
    public int PERMISSIONS_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppDarkTheme);
        Tools.transparentStatusBarNavigation(this);
        setContentView(R.layout.activity_image_detail);
        strImage = getIntent().getStringExtra("image_url");
        postImage = findViewById(R.id.image);
        Glide.with(this)
                .load(strImage.replace(" ", "%20"))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(postImage);

        setupToolbar();
    }

    private void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_close) {
            Tools.postDelayed(this::onBackPressed, 200);
            return true;
        } else if (menuItem.getItemId() == R.id.action_download) {
            downloadImage();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void downloadImage() {
        String imageName = getString(R.string.app_name).toLowerCase().replace(" ", "_");
        if (ContextCompat.checkSelfPermission(ActivityImageDetail.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSIONS_REQUEST);
            } else {
                downloadImage(imageName + "_" + System.currentTimeMillis(), strImage, "image/jpeg");
            }
        } else {
            downloadImage(imageName + "_" + System.currentTimeMillis(), strImage, "image/jpeg");
        }
    }

    public void downloadImage(String filename, String downloadUrlOfImage, String mimeType) {
        try {
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType(mimeType)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename + ".jpg");
            dm.enqueue(request);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_image_download_start), Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_image_download_failed), Snackbar.LENGTH_SHORT).show();
        }
    }

}
