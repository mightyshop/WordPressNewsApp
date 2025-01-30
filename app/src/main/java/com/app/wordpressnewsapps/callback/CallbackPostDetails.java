package com.app.wordpressnewsapps.callback;

import com.app.wordpressnewsapps.provider.jetpack.models.Author;
import com.app.wordpressnewsapps.provider.jetpack.models.Category;
import com.app.wordpressnewsapps.provider.jetpack.models.Discussion;

import java.io.Serializable;
import java.util.Map;

public class CallbackPostDetails implements Serializable {

    public int ID;
    public String URL;
    public Author author;
    public Map<String, Category> categories;
    public String content;
    public String date;
    public String excerpt;
    public String featured_image;
    public String guid;
    public String modified;
    public String short_URL;
    public int site_ID;
    public String slug;
    public String status;
    public boolean sticky;
    public String title;
    public String type;
    public Discussion discussion = null;

}
