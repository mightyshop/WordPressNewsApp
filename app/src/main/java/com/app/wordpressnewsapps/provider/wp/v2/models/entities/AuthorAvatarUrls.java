package com.app.wordpressnewsapps.provider.wp.v2.models.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AuthorAvatarUrls implements Serializable {

    @SerializedName("24")
    @Expose
    public String small = "";

    @SerializedName("48")
    @Expose
    public String medium = "";

    @SerializedName("96")
    @Expose
    public String large = "";

}
