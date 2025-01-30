package com.app.wordpressnewsapps.model;

import com.app.wordpressnewsapps.model.entities.Category;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomCategory implements Serializable {

    public boolean status;
    public List<Category> categories = new ArrayList<>();

}
