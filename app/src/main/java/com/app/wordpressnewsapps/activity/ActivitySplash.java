package com.app.wordpressnewsapps.activity;

import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_MAX;
import static com.solodroid.ads.sdk.util.Constant.GOOGLE_AD_MANAGER;
import static com.solodroid.ads.sdk.util.Constant.WORTISE;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.wordpressnewsapps.BuildConfig;
import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.application.MyApplication;
import com.app.wordpressnewsapps.callback.CallbackConfig;
import com.app.wordpressnewsapps.database.prefs.AdsPref;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.rest.RestAdapter;
import com.app.wordpressnewsapps.util.AdsManager;
import com.app.wordpressnewsapps.util.Constant;
import com.app.wordpressnewsapps.util.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    private static final String TAG = "ActivitySplash";
    ImageView imgSplash;
    Call<CallbackConfig> callbackCall = null;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;
    boolean isForceOpenAds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_splash);
        Tools.setNavigation(this);
        isForceOpenAds = getResources().getBoolean(R.bool.force_to_show_app_open_ad_on_start);
        sharedPref = new SharedPref(this);
        adsManager = new AdsManager(this);
        adsManager.initializeAd();
        adsPref = new AdsPref(this);
        imgSplash = findViewById(R.id.img_splash);
        if (sharedPref.getIsDarkTheme()) {
            imgSplash.setImageResource(R.drawable.bg_splash_dark);
        } else {
            imgSplash.setImageResource(R.drawable.bg_splash_default);
        }
        Tools.postDelayed(this::requestConfig, Constant.DELAY_SPLASH);
    }

    private void requestConfig() {
        if (getString(R.string.access_key).contains("XXXXX")) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("App not configured")
                    .setMessage("Please put your generated Access Key to res/values/config.xml, you can see the documentation for more detailed instructions.")
                    .setPositiveButton("OK", (dialogInterface, i) -> startMainActivity())
                    .setCancelable(false)
                    .show();
        } else {
            String data = com.solodroid.ads.sdk.util.Tools.decode(getString(R.string.access_key));
            String[] results = data.split("_applicationId_");
            String baseUrl = results[0].replace("http://localhost", Constant.LOCALHOST_ADDRESS);
            String applicationId = results[1];
            if (applicationId.equals(BuildConfig.APPLICATION_ID)) {
                if (results.length > 2) {
                    sharedPref.setBuyer(results[2]);
                    requestAPI(baseUrl);
                } else {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Access Key Update Required")
                            .setMessage("You are still using the old Access Key mechanism, please regenerate your Access Key, the generated Access Key link can be found in the documentation.")
                            .setPositiveButton("Ok", (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                }
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Error")
                        .setMessage("Whoops! invalid Access Key or applicationId, please check your configuration")
                        .setPositiveButton("Ok", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
            Log.d(TAG, "Start request config: " + baseUrl);
        }
    }

    private void requestAPI(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            if (url.contains("https://drive.google.com")) {
                String driveUrl = url.replace("https://", "").replace("http://", "");
                List<String> data = Arrays.asList(driveUrl.split("/"));
                String googleDriveFileId = data.get(3);
                callbackCall = RestAdapter.createAPI().getDriveJsonFileId(googleDriveFileId);
            } else {
                callbackCall = RestAdapter.createAPI().getJsonUrl(url);
            }
        } else {
            callbackCall = RestAdapter.createAPI().getDriveJsonFileId(url);
        }
        callbackCall.enqueue(new Callback<CallbackConfig>() {
            public void onResponse(@NonNull Call<CallbackConfig> call, @NonNull Response<CallbackConfig> response) {
                CallbackConfig resp = response.body();
                if (resp != null) {
                    displayApiResults(resp);
                } else {
                    showAppOpenAdIfAvailable();
                }
                Log.d(TAG, "request config success");
            }

            public void onFailure(@NonNull Call<CallbackConfig> call, @NonNull Throwable th) {
                showAppOpenAdIfAvailable();
                Log.d(TAG, "request config failed : " + th.getMessage());
            }
        });
    }

    private void displayApiResults(CallbackConfig resp) {
        if (resp != null) {
            sharedPref.saveWordPressConfig(resp.wordpress);
            sharedPref.saveSettings(resp.settings);
            sharedPref.saveCustomCategory(resp.custom_category.status);
            if (resp.custom_category.status) {
                sharedPref.saveCustomCategoryList(resp.custom_category.categories);
            }
            adsPref.saveAds(resp.ads);
            adsPref.saveAdPlacements(resp.ads.placement);
            Tools.postDelayed(() -> {
                if (!resp.apps.status) {
                    Intent intent = new Intent(getApplicationContext(), ActivityRedirect.class);
                    intent.putExtra("redirect_url", resp.apps.redirect_url);
                    startActivity(intent);
                    finish();
                } else {
                    showAppOpenAdIfAvailable();
                }
            }, 100);
            Log.d(TAG, "initialize success");
        } else {
            showAppOpenAdIfAvailable();
            Log.d(TAG, "initialize failed");
        }

    }

    private void showAppOpenAdIfAvailable() {
        if (isForceOpenAds) {
            if (adsPref.getIsAppOpen()) {
                adsManager.loadAppOpenAd(adsPref.getIsAppOpenAdOnStart(), this::startMainActivity);
            } else {
                startMainActivity();
            }
        } else {
            if (adsPref.getAdStatus() && adsPref.getIsAppOpenAdOnStart()) {
                Application application = getApplication();
                switch (adsPref.getMainAds()) {
                    case ADMOB:
                        if (!adsPref.getAdMobAppOpenId().equals("0")) {
                            ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::startMainActivity);
                        } else {
                            startMainActivity();
                        }
                        break;
                    case GOOGLE_AD_MANAGER:
                        if (!adsPref.getAdManagerAppOpenId().equals("0")) {
                            ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::startMainActivity);
                        } else {
                            startMainActivity();
                        }
                        break;
                    case APPLOVIN:
                    case APPLOVIN_MAX:
                        if (!adsPref.getApplovinMaxAppOpenId().equals("0")) {
                            ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::startMainActivity);
                        } else {
                            startMainActivity();
                        }
                        break;
                    case WORTISE:
                        if (!adsPref.getWortiseAppOpenId().equals("0")) {
                            ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::startMainActivity);
                        } else {
                            startMainActivity();
                        }
                        break;
                    default:
                        startMainActivity();
                        break;
                }
            } else {
                startMainActivity();
            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}
