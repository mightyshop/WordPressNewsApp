package com.app.wordpressnewsapps.model;

import java.io.Serializable;

public class Settings implements Serializable {

    public String more_apps_url = "";
    public String privacy_policy_url = "";
    public String terms_conditions_url = "";
    public String publisher_info_url = "";
    public String email_feedback_and_report = "";
    public int category_column_count;
    public boolean post_list_in_large_style;
    public boolean show_post_list_header;
    public boolean show_post_list_excerpt;
    public boolean show_post_list_categories;
    public boolean show_post_list_comment;
    public boolean show_post_list_line_divider;
    public boolean show_post_date;
    public boolean show_post_count_in_category_list;
    public boolean show_related_posts;
    public boolean enable_comment_feature;
    public boolean enable_view_on_site_menu;
    public boolean enable_exit_dialog;
    public boolean enable_rtl_mode;
    public boolean enable_dark_mode_as_default_theme;

}
