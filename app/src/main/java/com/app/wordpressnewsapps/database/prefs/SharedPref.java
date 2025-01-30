package com.app.wordpressnewsapps.database.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.wordpressnewsapps.model.Settings;
import com.app.wordpressnewsapps.model.User;
import com.app.wordpressnewsapps.model.Wordpress;
import com.app.wordpressnewsapps.model.entities.Category;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPref {

    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("apps_setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public Boolean getIsWpRestV2Enabled() {
        return sharedPreferences.getBoolean("wp_rest_v2", false);
    }

    public void setIsWpRestV2Enabled(Boolean isWpRestV2) {
        editor.putBoolean("wp_rest_v2", isWpRestV2);
        editor.apply();
    }

    public Boolean getIsDarkTheme() {
        return sharedPreferences.getBoolean("theme", getIsEnableDarkModeAsDefaultTheme());
    }

    public void setIsDarkTheme(Boolean isDarkTheme) {
        editor.putBoolean("theme", isDarkTheme);
        editor.apply();
    }

    public void saveCategoryId(int category_id) {
        editor.putInt("category_id", category_id);
        editor.apply();
    }

    public Integer getCategoryId() {
        return sharedPreferences.getInt("category_id", 0);
    }

    public void saveWordPressConfig(Wordpress wordpress) {
        setWordpressConfig(
                wordpress.site_url,
                wordpress.rest_api_provider,
                wordpress.posts_per_page,
                wordpress.categories_per_page,
                wordpress.max_related_posts
        );
    }

    private void setWordpressConfig(String siteUrl, String restApiProvider, int postsPerPage, int categoriesPerPage, int maxRelatedPosts) {
        editor.putString("site_url", siteUrl);
        editor.putString("rest_api_provider", restApiProvider);
        editor.putInt("posts_per_page", postsPerPage);
        editor.putInt("categories_per_page", categoriesPerPage);
        editor.putInt("max_related_posts", maxRelatedPosts);
        editor.apply();
    }

    public String getSiteUrl() {
        return sharedPreferences.getString("site_url", "");
    }

    public String getRestApiProvider() {
        return sharedPreferences.getString("rest_api_provider", "");
    }

    public int getPostsPerPage() {
        return sharedPreferences.getInt("posts_per_page", 10);
    }

    public int getCategoriesPerPage() {
        return sharedPreferences.getInt("categories_per_page", 20);
    }

    public int getMaxRelatedPosts() {
        return sharedPreferences.getInt("max_related_posts", 5);
    }

    public void saveSettings(Settings settings) {
        setSettings(
                settings.more_apps_url,
                settings.privacy_policy_url,
                settings.terms_conditions_url,
                settings.publisher_info_url,
                settings.email_feedback_and_report,
                settings.category_column_count,
                settings.post_list_in_large_style,
                settings.show_post_list_header,
                settings.show_post_list_excerpt,
                settings.show_post_list_categories,
                settings.show_post_list_comment,
                settings.show_post_list_line_divider,
                settings.show_post_date,
                settings.show_post_count_in_category_list,
                settings.show_related_posts,
                settings.enable_comment_feature,
                settings.enable_view_on_site_menu,
                settings.enable_exit_dialog,
                settings.enable_rtl_mode,
                settings.enable_dark_mode_as_default_theme
        );
    }

    private void setSettings(String moreAppsUrl, String privacyPolicyUrl, String termsConditionsUrl, String publisherInfoUrl, String emailFeedbackAndReport, int categoryColumnCount, boolean postListInLargeStyle, boolean showPostListHeader, boolean showPostListExcerpt, boolean showPostListCategories, boolean showPostListComment, boolean showPostListLineDivider, boolean showPostDate, boolean showPostCountInCategoryList, boolean showRelatedPosts, boolean enableCommentFeature, boolean enableViewOnSiteMenu, boolean enableExitDialog, boolean enableRtlMode, boolean enableDarkModeAsDefaultTheme) {
        editor.putString("more_apps_url", moreAppsUrl);
        editor.putString("privacy_policy_url", privacyPolicyUrl);
        editor.putString("terms_conditions_url", termsConditionsUrl);
        editor.putString("publisher_info_url", publisherInfoUrl);
        editor.putString("email_feedback_and_report", emailFeedbackAndReport);
        editor.putInt("category_column_count", categoryColumnCount);
        editor.putBoolean("post_list_in_large_style", postListInLargeStyle);
        editor.putBoolean("show_post_list_header", showPostListHeader);
        editor.putBoolean("show_post_list_excerpt", showPostListExcerpt);
        editor.putBoolean("show_post_list_categories", showPostListCategories);
        editor.putBoolean("show_post_list_comment", showPostListComment);
        editor.putBoolean("show_post_list_line_divider", showPostListLineDivider);
        editor.putBoolean("show_post_date", showPostDate);
        editor.putBoolean("show_post_count_in_category_list", showPostCountInCategoryList);
        editor.putBoolean("show_related_posts", showRelatedPosts);
        editor.putBoolean("enable_comment_feature", enableCommentFeature);
        editor.putBoolean("enable_view_on_site_menu", enableViewOnSiteMenu);
        editor.putBoolean("enable_exit_dialog", enableExitDialog);
        editor.putBoolean("enable_rtl_mode", enableRtlMode);
        editor.putBoolean("enable_dark_mode_as_default_theme", enableDarkModeAsDefaultTheme);
        editor.apply();
    }

    public String getMoreAppsUrl() {
        return sharedPreferences.getString("more_apps_url", "");
    }

    public String getPrivacyPolicyUrl() {
        return sharedPreferences.getString("privacy_policy_url", "");
    }

    public String getTermsConditionsUrl() {
        return sharedPreferences.getString("terms_conditions_url", "");
    }

    public String getPublisherInfoUrl() {
        return sharedPreferences.getString("publisher_info_url", "");
    }

    public String getEmailFeedbackAndReport() {
        return sharedPreferences.getString("email_feedback_and_report", "");
    }

    public int getCategoryColumnCount() {
        return sharedPreferences.getInt("category_column_count", 3);
    }

    public boolean getIsPostListInLargeStyle() {
        return sharedPreferences.getBoolean("post_list_in_large_style", false);
    }

    public boolean getIsShowPostListHeader() {
        return sharedPreferences.getBoolean("show_post_list_header", true);
    }

    public boolean getIsShowPostListExcerpt() {
        return sharedPreferences.getBoolean("show_post_list_excerpt", true);
    }

    public boolean getIsShowPostListCategories() {
        return sharedPreferences.getBoolean("show_post_list_categories", true);
    }

    public boolean getIsShowPostListComment() {
        return sharedPreferences.getBoolean("show_post_list_comment", true);
    }

    public boolean getIsShowPostListLineDivider() {
        return sharedPreferences.getBoolean("show_post_list_line_divider", false);
    }

    public boolean getIsShowPostDate() {
        return sharedPreferences.getBoolean("show_post_date", true);
    }

    public boolean getIsShowPostCountInCategoryList() {
        return sharedPreferences.getBoolean("show_post_count_in_category_list", true);
    }

    public boolean getIsShowRelatedPosts() {
        return sharedPreferences.getBoolean("show_related_posts", true);
    }

    public boolean getIsEnableCommentFeature() {
        return sharedPreferences.getBoolean("enable_comment_feature", true);
    }

    public boolean getIsEnableViewOnSiteMenu() {
        return sharedPreferences.getBoolean("enable_view_on_site_menu", true);
    }

    public boolean getIsEnableExitDialog() {
        return sharedPreferences.getBoolean("enable_exit_dialog", true);
    }

    public boolean getIsEnableRtlMode() {
        return sharedPreferences.getBoolean("enable_rtl_mode", false);
    }

    public boolean getIsEnableDarkModeAsDefaultTheme() {
        return sharedPreferences.getBoolean("enable_dark_mode_as_default_theme", false);
    }

    public void saveCustomCategory(boolean customCategory) {
        editor.putBoolean("custom_category", customCategory);
        editor.apply();
    }

    public boolean getIsCustomCategory() {
        return sharedPreferences.getBoolean("custom_category", false);
    }

    public void saveCustomCategoryList(List<Category> categories) {
        Gson gson = new Gson();
        String json = gson.toJson(categories);
        editor.putString("categories", json);
        editor.apply();
    }

    public List<Category> getCustomCategoryList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("categories", null);
        Type type = new TypeToken<ArrayList<Category>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public int getPostId() {
        return sharedPreferences.getInt("post_id", 0);
    }

    public void savePostId(int post_id) {
        editor.putInt("post_id", post_id);
        editor.apply();
    }

    public Integer getFontSize() {
        return sharedPreferences.getInt("font_size", 2);
    }

    public void updateFontSize(int font_size) {
        editor.putInt("font_size", font_size);
        editor.apply();
    }

    public Integer getInAppReviewToken() {
        return sharedPreferences.getInt("in_app_review_token", 0);
    }

    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }

    public String getAuthorName() {
        return sharedPreferences.getString("author_name", "");
    }

    public void setAuthorName(String name) {
        editor.putString("author_name", name);
        editor.apply();
    }

    public String getAuthorEmail() {
        return sharedPreferences.getString("author_email", "");
    }

    public void setAuthorEmail(String email) {
        editor.putString("author_email", email);
        editor.apply();
    }

    public void saveUserList(List<User> users) {
        Gson gson = new Gson();
        String json = gson.toJson(users);
        editor.putString("key_user", json);
        editor.apply();
    }

    public List<User> getUserList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("key_user", null);
        Type type = new TypeToken<ArrayList<User>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public String getBuyer() {
        return sharedPreferences.getString("buyer", "");
    }

    public void setBuyer(String buyer) {
        editor.putString("buyer", buyer);
        editor.apply();
    }

}
