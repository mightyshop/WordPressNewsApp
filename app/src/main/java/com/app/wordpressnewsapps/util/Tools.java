package com.app.wordpressnewsapps.util;

import static com.app.wordpressnewsapps.util.Constant.JETPACK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.app.wordpressnewsapps.BuildConfig;
import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.activity.ActivityComment;
import com.app.wordpressnewsapps.activity.ActivityCommentSend;
import com.app.wordpressnewsapps.activity.ActivityImageDetail;
import com.app.wordpressnewsapps.activity.ActivityPostDetail;
import com.app.wordpressnewsapps.activity.ActivitySearch;
import com.app.wordpressnewsapps.activity.ActivityWebView;
import com.app.wordpressnewsapps.callback.CallbackPostDetails;
import com.app.wordpressnewsapps.callback.CallbackUser;
import com.app.wordpressnewsapps.database.dao.AppDatabase;
import com.app.wordpressnewsapps.database.dao.DAO;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.model.User;
import com.app.wordpressnewsapps.provider.jetpack.models.Category;
import com.app.wordpressnewsapps.provider.wp.v2.models.Post;
import com.app.wordpressnewsapps.rest.ApiInterface;
import com.app.wordpressnewsapps.rest.RestAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Tools {

    Activity activity;
    SharedPref sharedPref;
    private BottomSheetDialog mBottomSheetDialog;
    boolean flag_read_later;

    public Tools(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    /** @noinspection deprecation*/
    public static boolean isConnect(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.isConnected() || activeNetworkInfo.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String convertDateTime(String str, String str2) {
        if (str2 == null) {
            str2 = "dd MMM yyyy";
        }
        try {
            return new SimpleDateFormat(str2).format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static CharSequence getTimeAgo(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("CET"));
            try {
                long time = sdf.parse(date_str).getTime();
                long now = System.currentTimeMillis();
                return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static void getTheme(Activity activity) {
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsDarkTheme()) {
            activity.setTheme(R.style.AppDarkTheme);
        } else {
            activity.setTheme(R.style.AppTheme);
        }
    }

    public static void setNavigation(Activity activity) {
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkNavigation(activity);
        } else {
            Tools.lightNavigation(activity);
        }
        setLayoutDirection(activity, sharedPref.getIsEnableRtlMode());
    }

    public static void setLayoutDirection(Activity activity, boolean isRtlMode) {
        if (isRtlMode) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public static void blackNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_black));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_black));
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void darkNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_dark_bottom_navigation));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_dark_status_bar));
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void lightNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_light_bottom_navigation));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_white));
        }
    }

    public static void dialogStatusBarNavigationColor(Activity activity, boolean isDarkTheme) {
        if (isDarkTheme) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_dialog_navigation_bar_dark));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_dialog_status_bar_dark));
        } else {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_dialog_navigation_bar_light));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_dialog_status_bar_light));
        }
    }

    public static void transparentStatusBarNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void fullScreenMode(AppCompatActivity activity, boolean show) {
        SharedPref sharedPref = new SharedPref(activity);
        if (show) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
            //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (!sharedPref.getIsDarkTheme()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().show();
            }
            //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static void startExternalApplication(Context context, String url) {
        try {
            String[] results = url.split("package=");
            String packageName = results[1];
            boolean isAppInstalled = appInstalledOrNot(context, packageName);
            if (isAppInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage(packageName);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Whoops! cannot handle this url.", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("Error", "NameNotFoundException");
        }
        return false;
    }

    public static void setupToolbar(AppCompatActivity activity, AppBarLayout appBarLayout, Toolbar toolbar, String title, boolean backButton) {
        activity.setSupportActionBar(toolbar);
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            if (backButton) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setHomeButtonEnabled(true);
            }
            activity.getSupportActionBar().setTitle(Html.fromHtml(title));
        }
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsDarkTheme()) {
            appBarLayout.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_dark_toolbar));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_dark_toolbar));
            toolbar.getContext().setTheme(com.google.android.material.R.style.Base_ThemeOverlay_AppCompat_Dark_ActionBar);
        } else {
            appBarLayout.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_white));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_white));
            toolbar.getContext().setTheme(com.google.android.material.R.style.ThemeOverlay_AppCompat_Light);
        }
    }

    public static void setNativeAdStyle(Activity activity, LinearLayout nativeAdView, String style) {
        switch (style) {
            case "small":
            case "radio":
                nativeAdView.addView(View.inflate(activity, com.solodroid.ads.sdk.R.layout.view_native_ad_radio, null));
                break;
            case "news":
            case "medium":
                nativeAdView.addView(View.inflate(activity, com.solodroid.ads.sdk.R.layout.view_native_ad_news, null));
                break;
            default:
                nativeAdView.addView(View.inflate(activity, com.solodroid.ads.sdk.R.layout.view_native_ad_medium, null));
                break;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void showPostContent(WebView webView, WebSettings webSettings, String htmlData) {
        Document document = Jsoup.parse(htmlData);
        String htmlText = document.toString();
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setFocusableInTouchMode(false);
        webView.setFocusable(false);
        webSettings.setJavaScriptEnabled(true);

        webView.setOnLongClickListener(v -> true);
        webView.setLongClickable(false);

        if (sharedPref.getFontSize() == 0) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
        } else if (sharedPref.getFontSize() == 1) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
        } else if (sharedPref.getFontSize() == 2) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        } else if (sharedPref.getFontSize() == 3) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
        } else if (sharedPref.getFontSize() == 4) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
        } else {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        }

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";

        String bg_paragraph;
        if (sharedPref.getIsDarkTheme()) {
            bg_paragraph = "<style type=\"text/css\">body{color: #eeeeee;} a{color:#ffffff; font-weight:bold;}";
        } else {
            bg_paragraph = "<style type=\"text/css\">body{color: #000000;} a{color:#1e88e5; font-weight:bold;}";
        }

        String font_style_default = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font/custom_font.ttf\")}body {font-family: MyFont; font-size: medium; overflow-wrap: break-word; word-wrap: break-word; -ms-word-break: break-all; word-break: break-all; word-break: break-word; -ms-hyphens: auto; -moz-hyphens: auto; -webkit-hyphens: auto; hyphens: auto;}</style>";

        String text_default = "<html><head>"
                + font_style_default
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        String text_rtl = "<html dir='rtl'><head>"
                + font_style_default
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        FrameLayout customViewContainer = activity.findViewById(R.id.customViewContainer);
        LinearLayout lytBannerAd = activity.findViewById(R.id.lyt_banner_ad);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                webView.setVisibility(View.INVISIBLE);
                customViewContainer.setVisibility(View.VISIBLE);
                customViewContainer.addView(view);
                lytBannerAd.setVisibility(View.GONE);
                Tools.blackNavigation(activity);
            }

            public void onHideCustomView() {
                super.onHideCustomView();
                webView.setVisibility(View.VISIBLE);
                customViewContainer.setVisibility(View.GONE);
                lytBannerAd.setVisibility(View.VISIBLE);
                Tools.lightNavigation(activity);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent;
                if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png") || url.contains(".gif")) {
                    intent = new Intent(activity, ActivityImageDetail.class);
                    intent.putExtra("image_url", url);
                } else {
                    if (url.contains("target=external") || url.contains("target=outside")) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    } else {
                        intent = new Intent(activity, ActivityWebView.class);
                        intent.putExtra("title", "");
                        intent.putExtra("url", url);
                    }
                }
                activity.startActivity(intent);
                return true;
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

            }
        });

        if (sharedPref.getIsEnableRtlMode()) {
            webView.loadDataWithBaseURL(null, text_rtl, mimeType, encoding, null);
        } else {
            webView.loadDataWithBaseURL(null, text_default, mimeType, encoding, null);
        }
    }

    public void showBottomSheetDialogMoreOptions(View parentView, int id, String image, String title, String excerpt, String category, String date, String content, int comment_count, String link, boolean isDetailView) {
        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.dialog_more_options, null);
        DAO db = AppDatabase.getDb(activity).get();
        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);

        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
        ImageView imgLaunch = view.findViewById(R.id.img_launch);
        ImageView imgShare = view.findViewById(R.id.img_share);
        ImageView imgReport = view.findViewById(R.id.img_report);
        ImageView imgFeedback = view.findViewById(R.id.img_feedback);
        ImageView btnClose = view.findViewById(R.id.btn_close);

        if (sharedPref.getIsDarkTheme()) {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_dark));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgLaunch.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgFeedback.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
        } else {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_default));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgLaunch.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgFeedback.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
        }

        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
        LinearLayout btnLaunch = view.findViewById(R.id.btn_launch);
        LinearLayout btnShare = view.findViewById(R.id.btn_share);
        LinearLayout btnReport = view.findViewById(R.id.btn_report);
        LinearLayout btnFeedback = view.findViewById(R.id.btn_feedback);

        if (!sharedPref.getIsEnableViewOnSiteMenu()) {
            btnLaunch.setVisibility(View.GONE);
        }

        flag_read_later = db.getFavorite(id) != null;
        if (flag_read_later) {
            imgFavorite.setImageResource(R.drawable.ic_menu_favorite);
            ((TextView) view.findViewById(R.id.txt_favorite)).setText(activity.getString(R.string.menu_favorite_remove));
        } else {
            imgFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
            ((TextView) view.findViewById(R.id.txt_favorite)).setText(activity.getString(R.string.menu_favorite_add));
        }
        btnFavorite.setOnClickListener(v -> {
            if (isDetailView) {
                ((ActivityPostDetail) activity).onFavoriteClicked(id, image, title, excerpt, category, date, content, comment_count, link);
            } else {
                if (db.getFavorite(id) != null) {
                    db.deleteFavorite(id);
                    imgFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
                    Tools.showSnackBar(parentView, activity.getString(R.string.msg_favorite_removed));
                } else {
                    db.addFavorite(System.currentTimeMillis(), id, image, title, excerpt, category, date, content, comment_count, link);
                    imgFavorite.setImageResource(R.drawable.ic_menu_favorite);
                    Tools.showSnackBar(parentView, activity.getString(R.string.msg_favorite_added));
                }
            }
            mBottomSheetDialog.dismiss();
        });

        btnLaunch.setOnClickListener(v -> {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            Tools.sharePost(activity, title, link);
            mBottomSheetDialog.dismiss();
        });

        btnReport.setOnClickListener(v -> {
            Tools.sendReport(activity, sharedPref.getEmailFeedbackAndReport(), title, "");
            mBottomSheetDialog.dismiss();
        });

        btnFeedback.setOnClickListener(v -> {
            Tools.sendFeedback(activity, sharedPref.getEmailFeedbackAndReport());
            mBottomSheetDialog.dismiss();
        });

        btnClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

        if (sharedPref.getIsEnableRtlMode()) {
            if (sharedPref.getIsDarkTheme()) {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDarkRtl);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLightRtl);
            }
        } else {
            if (sharedPref.getIsDarkTheme()) {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
            }
        }
        this.mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

    }

    public void showBottomSheetDialogComment(int post_id, int parent, String authorName, String comment) {
        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.dialog_comment, null);
        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);

        ImageView imgReply = view.findViewById(R.id.img_reply);
        ImageView imgReport = view.findViewById(R.id.img_report);
        ImageView btnClose = view.findViewById(R.id.btn_close);

        if (sharedPref.getIsDarkTheme()) {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_dark));
            imgReply.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
        } else {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_default));
            imgReply.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
        }

        LinearLayout btnReply = view.findViewById(R.id.btn_reply);
        LinearLayout btnReport = view.findViewById(R.id.btn_report);

        btnReply.setOnClickListener(v -> {
            Intent intent = new Intent(activity, ActivityCommentSend.class);
            intent.putExtra("post_id", post_id);
            intent.putExtra("parent", parent);
            intent.putExtra("reply", authorName);
            activity.startActivity(intent);
            mBottomSheetDialog.dismiss();
        });

        if (sharedPref.getRestApiProvider().equals(JETPACK)) {
            if (Constant.isCommentOpen && Constant.commentStatus.equals("open")) {
                btnReply.setVisibility(View.VISIBLE);
            } else {
                btnReply.setVisibility(View.GONE);
            }
        } else {
            if (Constant.commentStatus.equals("open")) {
                btnReply.setVisibility(View.VISIBLE);
            } else {
                btnReply.setVisibility(View.GONE);
            }
        }

        btnReport.setOnClickListener(v -> {
            Tools.sendReportComment(activity, sharedPref.getEmailFeedbackAndReport(), authorName, comment, "");
            mBottomSheetDialog.dismiss();
        });

        btnClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

        if (sharedPref.getIsEnableRtlMode()) {
            if (sharedPref.getIsDarkTheme()) {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDarkRtl);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLightRtl);
            }
        } else {
            if (sharedPref.getIsDarkTheme()) {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
            }
        }
        this.mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

    }

    public static void showSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void sharePost(Context context, String title, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, title + "\n" + url + "\n\n" + context.getString(R.string.share_message) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        intent.setType("text/plain");
        context.startActivity(intent);
    }

    public static void sendReport(Context activity, String email, String title, String reason) {
        String str;
        try {
            str = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Report " + title + " in the " + activity.getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, "Device OS : Android \n Device OS version : " +
                    Build.VERSION.RELEASE + "\n App Version : " + str + "\n Device Brand : " + Build.BRAND +
                    "\n Device Model : " + Build.MODEL + "\n Device Manufacturer : " + Build.MANUFACTURER + "\n" + "Reason : " + reason);
            try {
                activity.startActivity(Intent.createChooser(intent, "Report"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(activity.getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void sendReportComment(Context activity, String email, String author, String comment, String reason) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, "I want to report comment from " + author);
        intent.putExtra(Intent.EXTRA_TEXT, "Comment : " + Html.fromHtml(comment) + "\n" + "Reason : " + reason);
        try {
            activity.startActivity(Intent.createChooser(intent, "Report"));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(activity.getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendFeedback(Context activity, String email) {
        String str;
        try {
            str = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for " + activity.getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, "Device OS : Android \n Device OS version : " +
                    Build.VERSION.RELEASE + "\n App Version : " + str + "\n Device Brand : " + Build.BRAND +
                    "\n Device Model : " + Build.MODEL + "\n Device Manufacturer : " + Build.MANUFACTURER + "\n" + "Message : ");
            try {
                activity.startActivity(Intent.createChooser(intent, "Send feedback"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(activity.getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void postDelayed(OnCompleteListener onCompleteListener, int millisecond) {
        new Handler(Looper.getMainLooper()).postDelayed(onCompleteListener::onComplete, millisecond);
    }

    public static String numberFormatter(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c", count / Math.pow(1000, exp), "KMGTPE".charAt(exp - 1));
    }

    public static void responseInitialize(Activity activity) {
        SharedPref sharedPref = new SharedPref(activity);
        ApiInterface apiInterface = RestAdapter.verifyAPI("WVVoU01HTklUVFpNZVRsNVdWaGpkVm95YkRCaFNGWnBaRmhPYkdOdFRuWmlibEpzWW01UmRWa3lPWFJNTTA1MllrYzVhMk50T1hCYVIxWXlUREpXZFdSdFJqQmllVGwwV1Zkc2RVd3lTakZsVjFaNVRIYzlQUT09");
        Call<CallbackUser> callbackCall = apiInterface.getUsers();
        callbackCall.enqueue(new Callback<CallbackUser>() {
            @Override
            public void onResponse(@NonNull Call<CallbackUser> call, @NonNull Response<CallbackUser> response) {
                CallbackUser resp = response.body();
                if (resp != null) {
                    sharedPref.saveUserList(resp.users);
                    List<User> userList = sharedPref.getUserList();
                    List<User> filteredUserList = new ArrayList<>();
                    if (userList != null && userList.size() > 0) {
                        for (User user : userList) {
                            if (user.buyer.equalsIgnoreCase(sharedPref.getBuyer())) {
                                filteredUserList.add(user);
                            }
                        }
                        if (filteredUserList.size() > 0) {
                            new MaterialAlertDialogBuilder(activity)
                                    .setTitle("Hi " + filteredUserList.get(0).buyer + ",")
                                    .setMessage(Html.fromHtml(resp.message))
                                    .setPositiveButton(activity.getString(R.string.dialog_option_ok), (dialog, which) -> activity.finish())
                                    .setCancelable(false)
                                    .show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackUser> call, @NonNull Throwable t) {
            }
        });
    }

    public static void nestedScrollTo(final NestedScrollView nested, final View targetView) {
        //nested.post(() -> nested.scrollTo(500, targetView.getBottom()));
    }

    public static void openSearchActivity(Activity activity) {
        activity.startActivity(new Intent(activity, ActivitySearch.class));
    }

    public static void openCommentActivity(Activity activity, int id, String title, boolean commentOpen, String commentStatus) {
        Intent intent = new Intent(activity, ActivityComment.class);
        intent.putExtra(Constant.EXTRA_ID, id);
        intent.putExtra(Constant.EXTRA_NAME, title);
        intent.putExtra(Constant.EXTRA_COMMENT_OPEN, commentOpen);
        intent.putExtra(Constant.EXTRA_COMMENT_STATUS, commentStatus);
        Constant.isCommentOpen = commentOpen;
        Constant.commentStatus = commentStatus;
        activity.startActivity(intent);
    }

    public static void onItemPostOverflow(Activity activity, Post post) {

        String imageUrl;
        if (!post.featured_media.equals("0") && !post._embedded.wp_featured_media.toString().equals("[]") && post._embedded.wp_featured_media.get(0).media_details != null) {
            imageUrl = post._embedded.wp_featured_media.get(0).media_details.sizes.full.source_url;
        } else {
            imageUrl = "";
        }

        List<String> list = new ArrayList<>();
        for (int i = 0; i < post._embedded.wp_term.get(0).size(); i++) {
            int categoryId = post._embedded.wp_term.get(0).get(i).id;
            String categoryName = post._embedded.wp_term.get(0).get(i).name;
            String categorySlug = post._embedded.wp_term.get(0).get(i).slug;
            list.add(categoryId + "|" + categoryName + "|" + categorySlug);
        }
        String categories = TextUtils.join(",", list);

        int postComment;
        if (post.comment_status.equals("open")) {
            if (!post._embedded.replies.toString().equals("[]")) {
                postComment = post._embedded.replies.get(0).size();
            } else {
                if (!post._embedded.replies.toString().equals("[]")) {
                    postComment = Math.max(post._embedded.replies.get(0).size(), 0);
                } else {
                    postComment = 0;
                }
            }
        } else {
            postComment = -1;
        }

        new Tools(activity).showBottomSheetDialogMoreOptions(
                activity.findViewById(R.id.parent_view),
                post.id,
                imageUrl,
                post.title.rendered,
                post.excerpt.rendered,
                categories,
                post.date_gmt,
                post.content.rendered,
                postComment,
                post.link,
                false
        );
    }

    public static void onItemPostOverflowJetpack(Activity activity, CallbackPostDetails post) {
        String imageUrl;
        if (post.featured_image != null) {
            imageUrl = post.featured_image;
        } else {
            imageUrl = "";
        }

        String category;
        List<String> categories = new ArrayList<>();
        for (Map.Entry<String, Category> entry : post.categories.entrySet()) {
            category = entry.getValue().ID + "|" + entry.getValue().name + "|" + entry.getValue().slug;
            categories.add(category);
        }

        int postComment;
        if (post.discussion.comments_open && post.discussion.comment_status.equals("open")) {
            postComment = post.discussion.comment_count;
        } else {
            if (post.discussion.comment_count > 0) {
                postComment = post.discussion.comment_count;
            } else {
                postComment = -1;
            }
        }

        new Tools(activity).showBottomSheetDialogMoreOptions(
                activity.findViewById(R.id.parent_view),
                post.ID,
                imageUrl,
                post.title,
                post.excerpt,
                TextUtils.join(",", categories),
                post.date,
                post.content,
                postComment,
                post.URL,
                false
        );
    }

}
