package com.app.wordpressnewsapps.provider.jetpack.models;

import com.app.wordpressnewsapps.callback.CallbackPostDetails;

import java.io.Serializable;

public class Comment implements Serializable {

    public int ID;
    public CallbackPostDetails post = null;
    public Author author = null;
    public String date;
    public String URL;
    public String short_URL;
    public String content;
    public String raw_content;
    public String status;
    public Object parent;
    public String type;
    public int like_count;

}
