package com.app.wordpressnewsapps.util;

import static com.solodroid.ads.sdk.util.Constant.IRONSOURCE;

import android.app.Activity;

import com.app.wordpressnewsapps.BuildConfig;
import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.database.prefs.AdsPref;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.solodroid.ads.sdk.format.AdNetwork;
import com.solodroid.ads.sdk.format.AppOpenAd;
import com.solodroid.ads.sdk.format.BannerAd;
import com.solodroid.ads.sdk.format.InterstitialAd;
import com.solodroid.ads.sdk.format.NativeAd;
import com.solodroid.ads.sdk.format.NativeAdFragment;
import com.solodroid.ads.sdk.format.NativeAdView;
import com.solodroid.ads.sdk.gdpr.GDPR;
import com.solodroid.ads.sdk.gdpr.LegacyGDPR;
import com.solodroid.ads.sdk.util.OnShowAdCompleteListener;

public class AdsManager {

    Activity activity;
    AdNetwork.Initialize adNetwork;
    AppOpenAd.Builder appOpenAd;
    BannerAd.Builder bannerAd;
    InterstitialAd.Builder interstitialAd;
    NativeAd.Builder nativeAd;
    NativeAdView.Builder nativeAdView;
    SharedPref sharedPref;
    AdsPref adsPref;
    LegacyGDPR legacyGDPR;
    GDPR gdpr;

    public AdsManager(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
        this.adsPref = new AdsPref(activity);
        this.legacyGDPR = new LegacyGDPR(activity);
        this.gdpr = new GDPR(activity);
        adNetwork = new AdNetwork.Initialize(activity);
        appOpenAd = new AppOpenAd.Builder(activity);
        bannerAd = new BannerAd.Builder(activity);
        interstitialAd = new InterstitialAd.Builder(activity);
        nativeAd = new NativeAd.Builder(activity);
        nativeAdView = new NativeAdView.Builder(activity);
    }

    public void initializeAd() {
        if (adsPref.getAdStatus()) {
            adNetwork.setAdStatus("1")
                    .setAdNetwork(adsPref.getMainAds())
                    .setBackupAdNetwork(adsPref.getBackupAds())
                    .setStartappAppId(adsPref.getStartAppId())
                    .setUnityGameId(adsPref.getUnityGameId())
                    .setIronSourceAppKey(adsPref.getIronSourceAppKey())
                    .setWortiseAppId(adsPref.getWortiseAppId())
                    .setDebug(BuildConfig.DEBUG)
                    .build();
        }
    }

    public void loadAppOpenAd(boolean placement, OnShowAdCompleteListener onShowAdCompleteListener) {
        if (placement) {
            if (adsPref.getAdStatus()) {
                appOpenAd = new AppOpenAd.Builder(activity)
                        .setAdStatus("1")
                        .setAdNetwork(adsPref.getMainAds())
                        .setBackupAdNetwork(adsPref.getBackupAds())
                        .setAdMobAppOpenId(adsPref.getAdMobAppOpenId())
                        .setAdManagerAppOpenId(adsPref.getAdManagerAppOpenId())
                        .setApplovinAppOpenId(adsPref.getApplovinMaxAppOpenId())
                        .setWortiseAppOpenId(adsPref.getWortiseAppOpenId())
                        .build(onShowAdCompleteListener);
            } else {
                onShowAdCompleteListener.onShowAdComplete();
            }
        } else {
            onShowAdCompleteListener.onShowAdComplete();
        }
    }

    public void loadAppOpenAd(boolean placement) {
        if (placement) {
            if (adsPref.getAdStatus()) {
                appOpenAd = new AppOpenAd.Builder(activity)
                        .setAdStatus("1")
                        .setAdNetwork(adsPref.getMainAds())
                        .setBackupAdNetwork(adsPref.getBackupAds())
                        .setAdMobAppOpenId(adsPref.getAdMobAppOpenId())
                        .setAdManagerAppOpenId(adsPref.getAdManagerAppOpenId())
                        .setApplovinAppOpenId(adsPref.getApplovinMaxAppOpenId())
                        .setWortiseAppOpenId(adsPref.getWortiseAppOpenId())
                        .build();
            }
        }
    }

    public void showAppOpenAd(boolean placement) {
        if (placement) {
            if (adsPref.getAdStatus()) {
                appOpenAd.show();
            }
        }
    }

    public void destroyAppOpenAd(boolean placement) {
        if (placement) {
            if (adsPref.getAdStatus()) {
                appOpenAd.destroyOpenAd();
            }
        }
    }

    public void loadBannerAd(boolean placement) {
        if (placement) {
            if (adsPref.getAdStatus()) {
                bannerAd.setAdStatus("1")
                        .setAdNetwork(adsPref.getMainAds())
                        .setBackupAdNetwork(adsPref.getBackupAds())
                        .setAdMobBannerId(adsPref.getAdMobBannerId())
                        .setGoogleAdManagerBannerId(adsPref.getAdManagerBannerId())
                        .setFanBannerId(adsPref.getFanBannerId())
                        .setUnityBannerId(adsPref.getUnityBannerId())
                        .setAppLovinBannerId(adsPref.getApplovinMaxBannerId())
                        .setAppLovinBannerZoneId(adsPref.getApplovinDiscoveryBannerZoneId())
                        .setIronSourceBannerId(adsPref.getIronSourceBannerId())
                        .setWortiseBannerId(adsPref.getWortiseBannerId())
                        .setDarkTheme(sharedPref.getIsDarkTheme())
                        .setPlacementStatus(1)
                        .setLegacyGDPR(Constant.LEGACY_GDPR)
                        .build();
            }
        }
    }

    public void loadInterstitialAd(boolean placement, int interval) {
        if (placement) {
            if (adsPref.getAdStatus()) {
                interstitialAd.setAdStatus("1")
                        .setAdNetwork(adsPref.getMainAds())
                        .setBackupAdNetwork(adsPref.getBackupAds())
                        .setAdMobInterstitialId(adsPref.getAdMobInterstitialId())
                        .setGoogleAdManagerInterstitialId(adsPref.getAdManagerInterstitialId())
                        .setFanInterstitialId(adsPref.getFanInterstitialId())
                        .setUnityInterstitialId(adsPref.getUnityInterstitialId())
                        .setAppLovinInterstitialId(adsPref.getApplovinMaxInterstitialId())
                        .setAppLovinInterstitialZoneId(adsPref.getApplovinDiscoveryInterstitialZoneId())
                        .setIronSourceInterstitialId(adsPref.getIronSourceInterstitialId())
                        .setWortiseInterstitialId(adsPref.getWortiseInterstitialId())
                        .setInterval(interval)
                        .setPlacementStatus(1)
                        .setLegacyGDPR(Constant.LEGACY_GDPR)
                        .build();
            }
        }
    }

    public void loadNativeAd(boolean placement) {
        if (placement) {
            if (adsPref.getAdStatus()) {
                nativeAd.setAdStatus("1")
                        .setAdNetwork(adsPref.getMainAds())
                        .setBackupAdNetwork(adsPref.getBackupAds())
                        .setAdMobNativeId(adsPref.getAdMobNativeId())
                        .setAdManagerNativeId(adsPref.getAdManagerNativeId())
                        .setFanNativeId(adsPref.getFanNativeId())
                        .setAppLovinNativeId(adsPref.getApplovinMaxNativeId())
                        .setAppLovinDiscoveryMrecZoneId(adsPref.getApplovinDiscoveryMrecZoneId())
                        .setWortiseNativeId(adsPref.getWortiseNativeId())
                        .setPlacementStatus(1)
                        .setDarkTheme(sharedPref.getIsDarkTheme())
                        .setLegacyGDPR(false)
                        .setNativeAdStyle(adsPref.getNativeAdStylePostDetails())
                        .setNativeAdBackgroundColor(R.color.color_light_native_ad_background, R.color.color_dark_native_ad_background)
                        .build();
                nativeAd.setNativeAdPadding(
                        activity.getResources().getDimensionPixelSize(R.dimen.spacing_small),
                        activity.getResources().getDimensionPixelSize(R.dimen.spacing_small),
                        activity.getResources().getDimensionPixelSize(R.dimen.spacing_small),
                        activity.getResources().getDimensionPixelSize(R.dimen.spacing_small)
                );
                nativeAd.setNativeAdMargin(
                        activity.getResources().getDimensionPixelSize(R.dimen.no_spacing),
                        activity.getResources().getDimensionPixelSize(R.dimen.spacing_large),
                        activity.getResources().getDimensionPixelSize(R.dimen.no_spacing),
                        activity.getResources().getDimensionPixelSize(R.dimen.spacing_large)
                );
            }
        }
    }

    public void loadNativeAdView(boolean placement) {
        if (placement) {
            if (adsPref.getAdStatus()) {
                nativeAd.setAdStatus("1")
                        .setAdNetwork(adsPref.getMainAds())
                        .setBackupAdNetwork(adsPref.getBackupAds())
                        .setAdMobNativeId(adsPref.getAdMobNativeId())
                        .setAdManagerNativeId(adsPref.getAdManagerNativeId())
                        .setFanNativeId(adsPref.getFanNativeId())
                        .setAppLovinNativeId(adsPref.getApplovinMaxNativeId())
                        .setAppLovinDiscoveryMrecZoneId(adsPref.getApplovinDiscoveryMrecZoneId())
                        .setWortiseNativeId(adsPref.getWortiseNativeId())
                        .setPlacementStatus(1)
                        .setDarkTheme(sharedPref.getIsDarkTheme())
                        .setLegacyGDPR(false)
                        .setNativeAdBackgroundColor(R.color.color_light_native_ad_background, R.color.color_dark_native_ad_background)
                        .setNativeAdStyle(adsPref.getNativeAdStyleExitDialog())
                        .build();
            }
        }
    }

    public void showInterstitialAd() {
        if (adsPref.getAdStatus()) {
            interstitialAd.show();
        }
    }

    public void destroyBannerAd() {
        if (adsPref.getAdStatus()) {
            bannerAd.destroyAndDetachBanner();
        }
    }

    public void resumeBannerAd(boolean placement) {
        if (adsPref.getAdStatus() && !adsPref.getIronSourceBannerId().equals("0")) {
            if (adsPref.getMainAds().equals(IRONSOURCE) || adsPref.getBackupAds().equals(IRONSOURCE)) {
                loadBannerAd(placement);
            }
        }
    }

    public void updateConsentStatus() {
        if (Constant.LEGACY_GDPR) {
            legacyGDPR.updateLegacyGDPRConsentStatus(adsPref.getAdMobPublisherId(), sharedPref.getPrivacyPolicyUrl());
        } else {
            gdpr.updateGDPRConsentStatus(adsPref.getMainAds(), false, false);
        }
    }

}
