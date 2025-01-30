package com.app.wordpressnewsapps.model;

import com.app.wordpressnewsapps.model.entities.AdsUnitId;
import com.app.wordpressnewsapps.model.entities.Global;
import com.app.wordpressnewsapps.model.entities.Placement;

import java.io.Serializable;

public class Ads implements Serializable {

    public boolean status;
    public String main_ads = "";
    public String backup_ads = "";
    public AdsUnitId ads_unit_id = null;
    public Placement placement = null;
    public Global global = null;

}
