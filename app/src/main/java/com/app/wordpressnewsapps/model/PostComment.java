package com.app.wordpressnewsapps.model;

import java.io.Serializable;

public class PostComment implements Serializable {

    public String author_name = "";
    public String author_email = "";
    public Object content = "";
    public int post;
    public int parent;

}
