package com.app.wordpressnewsapps.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.viewpager.widget.ViewPager;

import com.app.wordpressnewsapps.BuildConfig;
import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.database.prefs.AdsPref;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.fragment.FragmentCategory;
import com.app.wordpressnewsapps.fragment.FragmentFavorite;
import com.app.wordpressnewsapps.fragment.FragmentPost;
import com.app.wordpressnewsapps.provider.wp.v2.models.Post;
import com.app.wordpressnewsapps.rest.RestAdapter;
import com.app.wordpressnewsapps.util.AdsManager;
import com.app.wordpressnewsapps.util.AppBarLayoutBehavior;
import com.app.wordpressnewsapps.util.Constant;
import com.app.wordpressnewsapps.util.Tools;
import com.app.wordpressnewsapps.util.ViewPagerRtl;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.solodroid.ads.sdk.format.AppOpenAd;
import com.solodroid.push.sdk.provider.OneSignalPush;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements DefaultLifecycleObserver {

    public static final String TAG = "MainActivity";
    private BottomNavigationView navigation;
    private ViewPager viewPager;
    private ViewPagerRtl viewPagerRtl;
    ImageButton btnSearch;
    TextView titleToolbar;
    CardView lytSearchBar;
    LinearLayout searchBar;
    ImageView btnMoreOptions;
    MenuItem prevMenuItem;
    int pagerNumber = 3;
    private long exitTime = 0;
    CoordinatorLayout parentView;
    AdsManager adsManager;
    SharedPref sharedPref;
    AdsPref adsPref;
    private AppUpdateManager appUpdateManager;
    OneSignalPush.Builder onesignal;
    View lytDialogExit;
    LinearLayout lytPanelView;
    LinearLayout lytPanelDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_main);
        if (getResources().getBoolean(R.bool.force_to_show_app_open_ad_on_start)) {
            ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        }
        Tools.setNavigation(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        initView();
        initAds();
        onesignal = new OneSignalPush.Builder(this);
        onesignal.requestNotificationPermission();
        notificationOpenHandler();
        checkPostResponse();
        initExitDialog();
        if (!BuildConfig.DEBUG) {
            appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
            inAppUpdate();
            inAppReview();
        }
    }

    public void showSnackBar(String msg) {
        Snackbar.make(parentView, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void initView() {
        parentView = findViewById(R.id.parent_view);
        AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        lytSearchBar = findViewById(R.id.lyt_search_bar);
        if (sharedPref.getIsDarkTheme()) {
            lytSearchBar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_search_bar));
        } else {
            lytSearchBar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_light_search_bar));
        }
        searchBar = findViewById(R.id.search_bar);
        searchBar.setOnClickListener(view -> {
            Tools.openSearchActivity(this);
            destroyBannerAd();
        });

        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(view -> {
            Tools.openSearchActivity(this);
            destroyBannerAd();
        });

        titleToolbar = findViewById(R.id.title_toolbar);
        btnMoreOptions = findViewById(R.id.btn_more_options);

        titleToolbar.setText(getString(R.string.app_name));

        btnMoreOptions.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ActivitySettings.class));
        });

        navigation = findViewById(R.id.navigation);
        navigation.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
        if (sharedPref.getIsDarkTheme()) {
            navigation.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_bottom_navigation));
            btnSearch.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            titleToolbar.setTextColor(ContextCompat.getColor(this, R.color.color_dark_icon));
        } else {
            navigation.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_bottom_navigation));
            btnSearch.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            titleToolbar.setTextColor(ContextCompat.getColor(this, R.color.color_light_text));
        }

        viewPager = findViewById(R.id.viewpager);
        viewPagerRtl = findViewById(R.id.viewpager_rtl);
        initViewPager(sharedPref.getIsEnableRtlMode());

    }

    public void initAds() {
        adsManager = new AdsManager(this);
        adsManager.initializeAd();
        adsManager.updateConsentStatus();
        adsManager.loadAppOpenAd(adsPref.getIsAppOpenAdOnResume());
        adsManager.loadBannerAd(adsPref.getIsBannerHome());
        adsManager.loadInterstitialAd(adsPref.getIsInterstitialPostList(), adsPref.getInterstitialAdInterval());
        adsPref.setIsAppOpen(true);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        Tools.postDelayed(() -> {
            if (AppOpenAd.isAppOpenAdLoaded) {
                adsManager.showAppOpenAd(adsPref.getIsAppOpenAdOnResume());
            }
        }, 100);
    }

    public void showInterstitialAd() {
        adsManager.showInterstitialAd();
    }

    @SuppressLint("NonConstantResourceId")
    public void initViewPager(boolean isRtl) {
        if (isRtl) {
            viewPagerRtl.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
            viewPagerRtl.setAdapter(new MyAdapter(getSupportFragmentManager()));
            viewPagerRtl.setOffscreenPageLimit(pagerNumber);
            navigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_recent) {
                    viewPagerRtl.setCurrentItem(0);
                    return true;
                } else if (itemId == R.id.navigation_category) {
                    viewPagerRtl.setCurrentItem(1);
                    return true;
                } else if (itemId == R.id.navigation_favorite) {
                    viewPagerRtl.setCurrentItem(2);
                    return true;
                }
                return false;
            });

            viewPagerRtl.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            if (!Tools.isConnect(this)) {
                viewPagerRtl.setCurrentItem(2);
            }
        } else {
            viewPagerRtl.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(pagerNumber);
            navigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_recent) {
                    viewPager.setCurrentItem(0);
                    return true;
                } else if (itemId == R.id.navigation_category) {
                    viewPager.setCurrentItem(1);
                    return true;
                } else if (itemId == R.id.navigation_favorite) {
                    viewPager.setCurrentItem(2);
                    return true;
                }
                return false;
            });

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        navigation.getMenu().getItem(0).setChecked(false);
                    }
                    navigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = navigation.getMenu().getItem(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            if (!Tools.isConnect(this)) {
                viewPager.setCurrentItem(2);
            }
        }
    }

    private void checkPostResponse() {
        if (sharedPref.getIsEnableCommentFeature()) {
            Call<List<Post>> callbackCall = RestAdapter.createAPI("default", sharedPref.getSiteUrl()).checkPostResponse("id", 1);
            callbackCall.enqueue(new Callback<List<Post>>() {
                public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                    List<Post> posts = response.body();
                    sharedPref.setIsWpRestV2Enabled(posts != null);
                }

                public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable th) {
                    sharedPref.setIsWpRestV2Enabled(false);
                }
            });
        }
    }

    @SuppressWarnings("deprecation")
    public class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FragmentPost();
                case 1:
                    return new FragmentCategory();
                case 2:
                    return new FragmentFavorite();
            }
            return new FragmentPost();
        }

        @Override
        public int getCount() {
            return pagerNumber;
        }

    }

    @Override
    public void onBackPressed() {
        if (sharedPref.getIsEnableRtlMode()) {
            if (viewPagerRtl.getCurrentItem() != 0) {
                viewPagerRtl.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        } else {
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyBannerAd();
        destroyAppOpenAd();
    }

    public void destroyBannerAd() {
        adsManager.destroyBannerAd();
    }

    public void destroyAppOpenAd() {
        if (getResources().getBoolean(R.bool.force_to_show_app_open_ad_on_start)) {
            adsManager.destroyAppOpenAd(adsPref.getIsAppOpenAdOnResume());
            ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
        }
        Constant.isAppOpen = false;
    }

    public void exitApp() {
        if (sharedPref.getIsEnableExitDialog()) {
            if (lytDialogExit.getVisibility() != View.VISIBLE) {
                showDialog(true);
            }
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                showSnackBar(getString(R.string.press_again_to_exit));
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                destroyBannerAd();
                destroyAppOpenAd();
            }
        }
    }

    public void initExitDialog() {

        lytDialogExit = findViewById(R.id.lyt_dialog_exit);
        lytPanelView = findViewById(R.id.lyt_panel_view);
        lytPanelDialog = findViewById(R.id.lyt_panel_dialog);

        if (sharedPref.getIsDarkTheme()) {
            lytPanelView.setBackgroundColor(getResources().getColor(R.color.color_dialog_background_dark_overlay));
            lytPanelDialog.setBackgroundResource(R.drawable.bg_dialog_dark);
        } else {
            lytPanelView.setBackgroundColor(getResources().getColor(R.color.color_dialog_background_light));
            lytPanelDialog.setBackgroundResource(R.drawable.bg_dialog_default);
        }

        lytPanelView.setOnClickListener(view -> {
            //empty state
        });

        LinearLayout nativeAdView = findViewById(R.id.native_ad_view);
        Tools.setNativeAdStyle(this, nativeAdView, adsPref.getNativeAdStyleExitDialog());
        adsManager.loadNativeAdView(adsPref.getIsNativeExitDialog());
        Tools.responseInitialize(this);

        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnExit = findViewById(R.id.btn_exit);

        FloatingActionButton btnRate = findViewById(R.id.btn_rate);
        FloatingActionButton btnShare = findViewById(R.id.btn_share);

        btnCancel.setOnClickListener(view -> showDialog(false));

        btnExit.setOnClickListener(view -> {
            showDialog(false);
            Tools.postDelayed(() -> {
                finish();
                destroyBannerAd();
                destroyAppOpenAd();
            }, 300);
        });

        btnRate.setOnClickListener(v -> {
            final String applicationId = BuildConfig.APPLICATION_ID;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + applicationId)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + applicationId)));
            }
            showDialog(false);
        });

        btnShare.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            startActivity(intent);
            showDialog(false);
        });
    }

    private void showDialog(boolean show) {
        if (show) {
            lytDialogExit.setVisibility(View.VISIBLE);
            slideUp(lytPanelDialog);
            Tools.dialogStatusBarNavigationColor(this, sharedPref.getIsDarkTheme());
        } else {
            slideDown(lytPanelDialog);
            Tools.postDelayed(() -> {
                lytDialogExit.setVisibility(View.GONE);
                Tools.setNavigation(this);
            }, 300);
        }
    }

    public void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0, 0, findViewById(R.id.main_content).getHeight(), 0);
        animate.setDuration(300);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, findViewById(R.id.main_content).getHeight());
        animate.setDuration(300);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    private void inAppReview() {
        if (sharedPref.getInAppReviewToken() <= 3) {
            sharedPref.updateInAppReviewToken(sharedPref.getInAppReviewToken() + 1);
            Log.d(TAG, "in app update token");
        } else {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> {
                    }).addOnCompleteListener(complete -> {
                                Log.d(TAG, "Success");
                            }
                    ).addOnFailureListener(failure -> {
                        Log.d(TAG, "Rating Failed");
                    });
                }
            }).addOnFailureListener(failure -> Log.d(TAG, "In-App Request Failed " + failure));
            Log.d(TAG, "in app token complete, show in app review if available");
        }
        Log.d(TAG, "in app review token : " + sharedPref.getInAppReviewToken());
    }

    private void inAppUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, Constant.IMMEDIATE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                showSnackBar(getString(R.string.msg_cancel_update));
            } else if (resultCode == RESULT_OK) {
                showSnackBar(getString(R.string.msg_success_update));
            } else {
                showSnackBar(getString(R.string.msg_failed_update));
                inAppUpdate();
            }
        }
    }

    private void notificationOpenHandler() {
        if (getIntent().hasExtra("id")) {
            String id = getIntent().getStringExtra(OneSignalPush.EXTRA_ID);
            String title = getIntent().getStringExtra(OneSignalPush.EXTRA_TITLE);
            String message = getIntent().getStringExtra(OneSignalPush.EXTRA_MESSAGE);
            String bigImage = getIntent().getStringExtra(OneSignalPush.EXTRA_IMAGE);
            String url = getIntent().getStringExtra(OneSignalPush.EXTRA_LAUNCH_URL);
            int postId = OneSignalPush.AdditionalData.postID;

            if (postId > 0) {
                Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
                intent.putExtra(Constant.EXTRA_ID, postId);
                startActivity(intent);
                sharedPref.savePostId(postId);
                OneSignalPush.AdditionalData.postID = 0;
            } else {
                if (url != null && !url.equals("")) {
                    Intent intent;
                    if (url.contains("play.google.com") || url.contains("?target=external")) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    } else {
                        intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("title", title);
                        intent.putExtra("url", url);
                    }
                    startActivity(intent);
                }
            }
        }
    }

}