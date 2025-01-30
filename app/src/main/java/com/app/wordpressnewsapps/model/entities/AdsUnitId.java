package com.app.wordpressnewsapps.model.entities;

import com.app.wordpressnewsapps.model.entities.ads.AdManager;
import com.app.wordpressnewsapps.model.entities.ads.AdMob;
import com.app.wordpressnewsapps.model.entities.ads.AppLovinDiscovery;
import com.app.wordpressnewsapps.model.entities.ads.AppLovinMax;
import com.app.wordpressnewsapps.model.entities.ads.Facebook;
import com.app.wordpressnewsapps.model.entities.ads.IronSource;
import com.app.wordpressnewsapps.model.entities.ads.StartApp;
import com.app.wordpressnewsapps.model.entities.ads.Unity;
import com.app.wordpressnewsapps.model.entities.ads.Wortise;

import java.io.Serializable;

public class AdsUnitId implements Serializable {

    public AdMob admob = null;
    public AdManager google_ad_manager = null;
    public Facebook facebook = null;
    public AppLovinMax applovin_max = null;
    public AppLovinDiscovery applovin_discovery = null;
    public StartApp startapp = null;
    public Unity unity = null;
    public IronSource ironsource = null;
    public Wortise wortise = null;

}
