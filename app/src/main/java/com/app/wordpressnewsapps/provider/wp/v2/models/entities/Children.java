package com.app.wordpressnewsapps.provider.wp.v2.models.entities;

import java.io.Serializable;

public class Children implements Serializable {

    public int id;
    public int parent;
    public int author;
    public String author_name = "";
    public String author_url = "";
    public String date = "";
    public Content content = null;
    public String link = "";
    public String type = "";
    public AuthorAvatarUrls author_avatar_urls = null;

}
