package com.app.wordpressnewsapps.provider.wp.v2.models.entities;

import java.io.Serializable;

public class Replies implements Serializable {

    public long id = -1;
    public long parent = -1;
    public long author = -1;
    public String author_name = "";
    public String author_url = "";
    public String date = "";
    public Content content = null;
    public String link = "";
    public String type = "";
    public AuthorAvatarUrls author_avatar_urls = null;

}
