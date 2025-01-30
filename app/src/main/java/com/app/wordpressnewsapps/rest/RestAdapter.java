package com.app.wordpressnewsapps.rest;

import com.solodroid.ads.sdk.util.Tools;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestAdapter {

    public static ApiInterface createAPI() {
        new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://drive.google.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient())
                .build();
        return retrofit.create(ApiInterface.class);
    }

    public static ApiInterface createAPI(String restApiProvider, String wordpressUrl) {
        new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        Retrofit retrofit;
        if (restApiProvider.equals("jetpack")) {
            String baseUrl = "https://public-api.wordpress.com/rest/v1.1/sites/";
            String wpUrlPattern = wordpressUrl
                    .replace("https://", "")
                    .replace("http://", "")
                    .replace("https://www.", "")
                    .replace("http://www.", "");
            String siteUrl;
            if (wpUrlPattern.endsWith("/")) {
                siteUrl = wpUrlPattern.substring(0, wpUrlPattern.length() - 1);
            } else {
                siteUrl = wpUrlPattern;
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl + siteUrl + "/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient())
                    .build();
        } else {
            String baseUrl = "wp-json/wp/v2";
            retrofit = new Retrofit.Builder()
                    .baseUrl(wordpressUrl + "/" + baseUrl + "/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient())
                    .build();
        }
        return retrofit.create(ApiInterface.class);
    }

    public static ApiInterface verifyAPI(String key) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(null)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Tools.decode(key))
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        return retrofit.create(ApiInterface.class);

    }

    private static OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(null)
                .build();
    }

}
