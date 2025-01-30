package com.app.wordpressnewsapps.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

@Entity(tableName = "tbl_favorite")
public class PostEntity implements Serializable {

    @PrimaryKey
    public long saved_date = -1;

    @Expose
    @ColumnInfo(name = "id")
    public int id;

    @Expose
    @ColumnInfo(name = "image")
    public String image = "";

    @Expose
    @ColumnInfo(name = "title")
    public String title = "";

    @Expose
    @ColumnInfo(name = "excerpt")
    public String excerpt;

    @Expose
    @ColumnInfo(name = "category")
    public String category = "";

    @Expose
    @ColumnInfo(name = "date")
    public String date = "";

    @Expose
    @ColumnInfo(name = "content")
    public String content = "";

    @Expose
    @ColumnInfo(name = "comment_count")
    public int comment_count;

    @Expose
    @ColumnInfo(name = "link")
    public String link = "";

}
