package com.app.wordpressnewsapps.provider.wp.v2.models;

import com.app.wordpressnewsapps.provider.wp.v2.models.entities.Content;
import com.app.wordpressnewsapps.provider.wp.v2.models.entities.Embedded;
import com.app.wordpressnewsapps.provider.wp.v2.models.entities.Excerpt;
import com.app.wordpressnewsapps.provider.wp.v2.models.entities.Guid;
import com.app.wordpressnewsapps.provider.wp.v2.models.entities.Title;

import java.io.Serializable;

public class Post implements Serializable {

    public int id;
    public String date;
    public String date_gmt;
    public Guid guid = null;
    public String modified;
    public String modified_gmt;
    public String slug;
    public String status;
    public String type;
    public String link;
    public Title title = null;
    public Content content = null;
    public Excerpt excerpt = null;
    public String author;
    public String featured_media;
    public String comment_status;
    public String ping_status;
    public boolean sticky;
    public String template;
    public String format;
    public Embedded _embedded = null;

}
