package com.app.wordpressnewsapps.provider.wp.v2.models;

import com.app.wordpressnewsapps.provider.wp.v2.models.entities.AuthorAvatarUrls;
import com.app.wordpressnewsapps.provider.wp.v2.models.entities.Content;
import com.app.wordpressnewsapps.provider.wp.v2.models.entities.Embedded;

import java.io.Serializable;

public class Comment implements Serializable {

    public int id;
    public int post;
    public int parent;
    public int author;
    public String author_name = "";
    public String author_url = "";
    public String date = "";
    public String date_gmt = "";
    public Content content = null;
    public String link = "";
    public String status = "";
    public String type = "";
    public AuthorAvatarUrls author_avatar_urls = null;
    public Embedded _embedded = null;

}
