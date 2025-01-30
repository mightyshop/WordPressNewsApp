package com.app.wordpressnewsapps.database.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.wordpressnewsapps.model.Ads;
import com.app.wordpressnewsapps.model.entities.Placement;

public class AdsPref {

    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdsPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("ads_setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveAds(Ads ads) {
        setAds(
                ads.status,
                ads.main_ads,
                ads.backup_ads,
                ads.global.interstitial_ad_interval,
                ads.global.native_ad_index,
                ads.global.native_ad_style_exit_dialog,
                ads.global.native_ad_style_post_list,
                ads.global.native_ad_style_post_details,
                ads.global.native_ad_post_details_position
        );
        setAdMobId(
                ads.ads_unit_id.admob.publisher_id,
                ads.ads_unit_id.admob.app_open_id,
                ads.ads_unit_id.admob.banner_id,
                ads.ads_unit_id.admob.interstitial_id,
                ads.ads_unit_id.admob.native_id
        );
        setAdManagerId(
                ads.ads_unit_id.google_ad_manager.app_open_id,
                ads.ads_unit_id.google_ad_manager.banner_id,
                ads.ads_unit_id.google_ad_manager.interstitial_id,
                ads.ads_unit_id.google_ad_manager.native_id
        );
        setMetaAudienceNetworkId(
                ads.ads_unit_id.facebook.banner_id,
                ads.ads_unit_id.facebook.interstitial_id,
                ads.ads_unit_id.facebook.native_id
        );
        setApplovinMaxId(
                ads.ads_unit_id.applovin_max.app_open_id,
                ads.ads_unit_id.applovin_max.banner_id,
                ads.ads_unit_id.applovin_max.interstitial_id,
                ads.ads_unit_id.applovin_max.native_id
        );
        setApplovinDiscoveryId(
                ads.ads_unit_id.applovin_discovery.banner_zone_id,
                ads.ads_unit_id.applovin_discovery.mrec_zone_id,
                ads.ads_unit_id.applovin_discovery.interstitial_zone_id
        );
        setStartAppId(ads.ads_unit_id.startapp.app_id);
        setUnityAdsId(
                ads.ads_unit_id.unity.game_id,
                ads.ads_unit_id.unity.banner_id,
                ads.ads_unit_id.unity.interstitial_id
        );
        setIronSourceId(
                ads.ads_unit_id.ironsource.app_key,
                ads.ads_unit_id.ironsource.banner_id,
                ads.ads_unit_id.ironsource.interstitial_id
        );
        setWortiseId(
                ads.ads_unit_id.wortise.app_id,
                ads.ads_unit_id.wortise.app_open_id,
                ads.ads_unit_id.wortise.banner_id,
                ads.ads_unit_id.wortise.interstitial_id,
                ads.ads_unit_id.wortise.native_id
        );
    }

    public void setAds(boolean adStatus, String mainAds, String backupAds, int interstitialAdInterval, int nativeAdIndex, String nativeAdStyleExitDialog, String nativeAdStylePostList, String nativeAdStylePostDetails, String nativeAdPostDetailsPosition) {
        editor.putBoolean("ad_status", adStatus);
        editor.putString("main_ads", mainAds);
        editor.putString("backup_ads", backupAds);
        editor.putInt("interstitial_ad_interval", interstitialAdInterval);
        editor.putInt("native_ad_index", nativeAdIndex);
        editor.putString("native_ad_style_exit_dialog", nativeAdStyleExitDialog);
        editor.putString("native_ad_style_post_list", nativeAdStylePostList);
        editor.putString("native_ad_style_post_details", nativeAdStylePostDetails);
        editor.putString("native_ad_post_details_position", nativeAdPostDetailsPosition);
        editor.apply();
    }

    public boolean getAdStatus() {
        return sharedPreferences.getBoolean("ad_status", true);
    }

    public String getMainAds() {
        return sharedPreferences.getString("main_ads", "0");
    }

    public String getBackupAds() {
        return sharedPreferences.getString("backup_ads", "none");
    }

    public int getInterstitialAdInterval() {
        return sharedPreferences.getInt("interstitial_ad_interval", 0);
    }

    public int getNativeAdIndex() {
        return sharedPreferences.getInt("native_ad_index", 0);
    }

    public String getNativeAdStyleExitDialog() {
        return sharedPreferences.getString("native_ad_style_exit_dialog", "");
    }

    public String getNativeAdStylePostList() {
        return sharedPreferences.getString("native_ad_style_post_list", "");
    }

    public String getNativeAdStylePostDetails() {
        return sharedPreferences.getString("native_ad_style_post_details", "");
    }

    public String getNativeAdPostDetailsPosition() {
        return sharedPreferences.getString("native_ad_post_details_position", "");
    }

    public void setAdMobId(String admobPublisherId, String admobAppOpenId, String admobBannerId, String admobInterstitialId, String admobNativeId) {
        editor.putString("admob_publisher_id", admobPublisherId);
        editor.putString("admob_app_open_id", admobAppOpenId);
        editor.putString("admob_banner_id", admobBannerId);
        editor.putString("admob_interstitial_id", admobInterstitialId);
        editor.putString("admob_native_id", admobNativeId);
        editor.apply();
    }

    public String getAdMobPublisherId() {
        return sharedPreferences.getString("admob_publisher_id", "");
    }

    public String getAdMobAppOpenId() {
        return sharedPreferences.getString("admob_app_open_id", "");
    }

    public String getAdMobBannerId() {
        return sharedPreferences.getString("admob_banner_id", "");
    }

    public String getAdMobInterstitialId() {
        return sharedPreferences.getString("admob_interstitial_id", "");
    }

    public String getAdMobNativeId() {
        return sharedPreferences.getString("admob_native_id", "");
    }

    public void setAdManagerId(String adManagerAppOpenId, String adManagerBannerId, String adManagerInterstitialId, String adManagerNativeId) {
        editor.putString("ad_manager_app_open_id", adManagerAppOpenId);
        editor.putString("ad_manager_banner_id", adManagerBannerId);
        editor.putString("ad_manager_interstitial_id", adManagerInterstitialId);
        editor.putString("ad_manager_native_id", adManagerNativeId);
        editor.apply();
    }

    public String getAdManagerAppOpenId() {
        return sharedPreferences.getString("ad_manager_app_open_id", "");
    }

    public String getAdManagerBannerId() {
        return sharedPreferences.getString("ad_manager_banner_id", "");
    }

    public String getAdManagerInterstitialId() {
        return sharedPreferences.getString("ad_manager_interstitial_id", "");
    }

    public String getAdManagerNativeId() {
        return sharedPreferences.getString("ad_manager_native_id", "");
    }

    public void setMetaAudienceNetworkId(String fanBannerId, String fanInterstitialId, String fanNativeId) {
        editor.putString("fan_banner_id", fanBannerId);
        editor.putString("fan_interstitial_id", fanInterstitialId);
        editor.putString("fan_native_id", fanNativeId);
        editor.apply();
    }

    public String getFanBannerId() {
        return sharedPreferences.getString("fan_banner_id", "");
    }

    public String getFanInterstitialId() {
        return sharedPreferences.getString("fan_interstitial_id", "");
    }

    public String getFanNativeId() {
        return sharedPreferences.getString("fan_native_id", "");
    }

    public void setApplovinMaxId(String applovinMaxAppOpenId, String applovinMaxBannerId, String applovinMaxInterstitialId, String applovinMaxNativeId) {
        editor.putString("applovin_max_app_open_id", applovinMaxAppOpenId);
        editor.putString("applovin_max_banner_id", applovinMaxBannerId);
        editor.putString("applovin_max_interstitial_id", applovinMaxInterstitialId);
        editor.putString("applovin_max_native_id", applovinMaxNativeId);
        editor.apply();
    }

    public String getApplovinMaxAppOpenId() {
        return sharedPreferences.getString("applovin_max_app_open_id", "");
    }

    public String getApplovinMaxBannerId() {
        return sharedPreferences.getString("applovin_max_banner_id", "");
    }

    public String getApplovinMaxInterstitialId() {
        return sharedPreferences.getString("applovin_max_interstitial_id", "");
    }

    public String getApplovinMaxNativeId() {
        return sharedPreferences.getString("applovin_max_native_id", "");
    }

    public void setApplovinDiscoveryId(String applovinDiscoveryBannerZoneId, String applovinDiscoveryMrecZoneId, String applovinDiscoveryInterstitialZoneId) {
        editor.putString("applovin_discovery_banner_zone_id", applovinDiscoveryBannerZoneId);
        editor.putString("applovin_discovery_mrec_zone_id", applovinDiscoveryMrecZoneId);
        editor.putString("applovin_discovery_interstitial_zone_id", applovinDiscoveryInterstitialZoneId);
        editor.apply();
    }

    public String getApplovinDiscoveryBannerZoneId() {
        return sharedPreferences.getString("applovin_discovery_banner_zone_id", "");
    }

    public String getApplovinDiscoveryMrecZoneId() {
        return sharedPreferences.getString("applovin_discovery_mrec_zone_id", "");
    }

    public String getApplovinDiscoveryInterstitialZoneId() {
        return sharedPreferences.getString("applovin_discovery_interstitial_zone_id", "");
    }

    public void setStartAppId(String startappAppId) {
        editor.putString("startapp_app_id", startappAppId);
        editor.apply();
    }

    public String getStartAppId() {
        return sharedPreferences.getString("startapp_app_id", "");
    }

    public void setUnityAdsId(String unityGameId, String unityBannerId, String unityInterstitialId) {
        editor.putString("unity_game_id", unityGameId);
        editor.putString("unity_banner_id", unityBannerId);
        editor.putString("unity_interstitial_id", unityInterstitialId);
        editor.apply();
    }

    public String getUnityGameId() {
        return sharedPreferences.getString("unity_game_id", "");
    }

    public String getUnityBannerId() {
        return sharedPreferences.getString("unity_banner_id", "");
    }

    public String getUnityInterstitialId() {
        return sharedPreferences.getString("unity_interstitial_id", "");
    }

    public void setIronSourceId(String ironsourceAppKey, String ironsourceBannerId, String ironsourceInterstitialId) {
        editor.putString("ironsource_app_key", ironsourceAppKey);
        editor.putString("ironsource_banner_id", ironsourceBannerId);
        editor.putString("ironsource_interstitial_id", ironsourceInterstitialId);
        editor.apply();
    }

    public String getIronSourceAppKey() {
        return sharedPreferences.getString("ironsource_app_key", "");
    }

    public String getIronSourceBannerId() {
        return sharedPreferences.getString("ironsource_banner_id", "");
    }

    public String getIronSourceInterstitialId() {
        return sharedPreferences.getString("ironsource_interstitial_id", "");
    }

    public void setWortiseId(String wortiseAppId, String wortiseAppOpenId, String wortiseBannerId, String wortiseInterstitialId, String wortiseNativeId) {
        editor.putString("wortise_app_id", wortiseAppId);
        editor.putString("wortise_app_open_id", wortiseAppOpenId);
        editor.putString("wortise_banner_id", wortiseBannerId);
        editor.putString("wortise_interstitial_id", wortiseInterstitialId);
        editor.putString("wortise_native_id", wortiseNativeId);
        editor.apply();
    }

    public String getWortiseAppId() {
        return sharedPreferences.getString("wortise_app_id", "");
    }

    public String getWortiseAppOpenId() {
        return sharedPreferences.getString("wortise_app_open_id", "");
    }

    public String getWortiseBannerId() {
        return sharedPreferences.getString("wortise_banner_id", "");
    }

    public String getWortiseInterstitialId() {
        return sharedPreferences.getString("wortise_interstitial_id", "");
    }

    public String getWortiseNativeId() {
        return sharedPreferences.getString("wortise_native_id", "");
    }

    public void saveAdPlacements(Placement placement) {
        setAdPlacements(
                placement.banner_home,
                placement.banner_post_details,
                placement.banner_category_details,
                placement.banner_search,
                placement.interstitial_post_list,
                placement.interstitial_post_details,
                placement.native_exit_dialog,
                placement.native_post_list,
                placement.native_post_details,
                placement.app_open_ad_on_start,
                placement.app_open_ad_on_resume
        );
    }

    public void setAdPlacements(boolean bannerHome, boolean bannerPostDetails, boolean bannerCategoryDetails, boolean bannerSearch, boolean interstitialPostList, boolean interstitialPostDetails, boolean nativeExitDialog, boolean nativePostList, boolean nativePostDetails, boolean appOpenAdOnStart, boolean appOpenAdOnResume) {
        editor.putBoolean("banner_home", bannerHome);
        editor.putBoolean("banner_post_details", bannerPostDetails);
        editor.putBoolean("banner_category_details", bannerCategoryDetails);
        editor.putBoolean("banner_search", bannerSearch);
        editor.putBoolean("interstitial_post_list", interstitialPostList);
        editor.putBoolean("interstitial_post_details", interstitialPostDetails);
        editor.putBoolean("native_exit_dialog", nativeExitDialog);
        editor.putBoolean("native_post_list", nativePostList);
        editor.putBoolean("native_post_details", nativePostDetails);
        editor.putBoolean("app_open_ad_on_start", appOpenAdOnStart);
        editor.putBoolean("app_open_ad_on_resume", appOpenAdOnResume);
        editor.apply();
    }

    public boolean getIsBannerHome() {
        return sharedPreferences.getBoolean("banner_home", true);
    }

    public boolean getIsBannerPostDetails() {
        return sharedPreferences.getBoolean("banner_post_details", true);
    }

    public boolean getIsBannerCategoryDetails() {
        return sharedPreferences.getBoolean("banner_category_details", true);
    }

    public boolean getIsBannerSearch() {
        return sharedPreferences.getBoolean("banner_search", true);
    }

    public boolean getIsInterstitialPostList() {
        return sharedPreferences.getBoolean("interstitial_post_list", true);
    }

    public boolean getIsInterstitialPostDetails() {
        return sharedPreferences.getBoolean("interstitial_post_details", true);
    }

    public boolean getIsNativeExitDialog() {
        return sharedPreferences.getBoolean("native_exit_dialog", true);
    }

    public boolean getIsNativePostList() {
        return sharedPreferences.getBoolean("native_post_list", true);
    }

    public boolean getIsNativePostDetails() {
        return sharedPreferences.getBoolean("native_post_details", true);
    }

    public boolean getIsAppOpenAdOnStart() {
        return sharedPreferences.getBoolean("app_open_ad_on_start", true);
    }

    public boolean getIsAppOpenAdOnResume() {
        return sharedPreferences.getBoolean("app_open_ad_on_resume", true);
    }

    public void saveCounter(int counter) {
        editor.putInt("counter", counter);
        editor.apply();
    }

    public int getCounter() {
        return sharedPreferences.getInt("counter", 1);
    }

    public void setIsAppOpen(boolean isAppOpen) {
        editor.putBoolean("open_ads", isAppOpen);
        editor.apply();
    }

    public boolean getIsAppOpen() {
        return sharedPreferences.getBoolean("open_ads", false);
    }

}
