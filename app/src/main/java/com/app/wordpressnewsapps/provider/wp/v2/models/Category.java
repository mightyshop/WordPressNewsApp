package com.app.wordpressnewsapps.provider.wp.v2.models;

import java.io.Serializable;

public class Category implements Serializable {

    public int id;
    public long count;
    public String description;
    public String link;
    public String name;
    public String slug;
    public String taxonomy;
    public int parent;

}
