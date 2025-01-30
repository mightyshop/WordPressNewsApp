package com.app.wordpressnewsapps.provider.jetpack.models;

import java.io.Serializable;

public class Discussion implements Serializable {

    public boolean comments_open;
    public String comment_status;
    public boolean pings_open;
    public String ping_status;
    public int comment_count;

}
