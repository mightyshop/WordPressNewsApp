package com.app.wordpressnewsapps.rest;

import com.app.wordpressnewsapps.callback.CallbackCategory;
import com.app.wordpressnewsapps.callback.CallbackComment;
import com.app.wordpressnewsapps.callback.CallbackConfig;
import com.app.wordpressnewsapps.callback.CallbackPost;
import com.app.wordpressnewsapps.callback.CallbackPostDetails;
import com.app.wordpressnewsapps.callback.CallbackRelated;
import com.app.wordpressnewsapps.callback.CallbackUser;
import com.app.wordpressnewsapps.model.PostComment;
import com.app.wordpressnewsapps.provider.wp.v2.models.Category;
import com.app.wordpressnewsapps.provider.wp.v2.models.Comment;
import com.app.wordpressnewsapps.provider.wp.v2.models.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: WordPress News App";
    String POST_ENDPOINT = "posts";
    String CATEGORY_ENDPOINT = "categories";
    String COMMENT_ENDPOINT = "comments";

    @Headers({CACHE, AGENT})
    @GET
    Call<CallbackConfig> getJsonUrl(@Url String url);

    @Headers({CACHE, AGENT})
    @GET("uc?export=download")
    Call<CallbackConfig> getDriveJsonFileId(
            @Query("id") String id
    );

    //wp rest v2 call
    @Headers({CACHE, AGENT})
    @GET(POST_ENDPOINT)
    Call<List<Post>> checkPostResponse(
            @Query("_fields") String _fields,
            @Query("per_page") int per_page
    );

    @Headers({CACHE, AGENT})
    @GET(POST_ENDPOINT)
    Call<List<Post>> getPosts(
            @Query("_embed") boolean _embed,
            @Query("page") int page,
            @Query("per_page") int per_page
    );

    @Headers({CACHE, AGENT})
    @GET(POST_ENDPOINT)
    Call<List<Post>> getSearch(
            @Query("search") String search,
            @Query("_embed") boolean _embed,
            @Query("page") int page,
            @Query("per_page") int per_page
    );

    @Headers({CACHE, AGENT})
    @GET(POST_ENDPOINT)
    Call<List<Post>> getPostsByCategory(
            @Query("categories") int categories,
            @Query("_embed") boolean _embed,
            @Query("page") int page,
            @Query("per_page") int per_page
    );

    @Headers({CACHE, AGENT})
    @GET(CATEGORY_ENDPOINT)
    Call<List<Category>> getAllCategories(
            @Query("page") int page,
            @Query("per_page") int per_page
    );

    @Headers({CACHE, AGENT})
    @GET(POST_ENDPOINT + "/{id}")
    Call<Post> getPostDetail(
            @Path("id") int id,
            @Query("_embed") boolean _embed
    );

    @Headers({CACHE, AGENT})
    @GET(COMMENT_ENDPOINT)
    Call<List<Comment>> getComments(
            @Query("post") int post,
            @Query("page") int page,
            @Query("per_page") int per_page,
            @Query("parent") int parent,
            @Query("_embed") boolean _embed,
            @Query("order") String order
    );

    @FormUrlEncoded
    @POST(COMMENT_ENDPOINT)
    Call<PostComment> postComment(
            @Field("author_name") String author_name,
            @Field("author_email") String author_email,
            @Field("content") String content,
            @Field("post") int post,
            @Field("parent") int parent
    );

    //jetpack call
    @Headers({CACHE, AGENT})
    @GET(POST_ENDPOINT)
    Call<CallbackPost> getPosts(
            @Query("page") int page,
            @Query("number") int number
    );

    @Headers({CACHE, AGENT})
    @GET(CATEGORY_ENDPOINT)
    Call<CallbackCategory> getCategories(
            @Query("page") int page,
            @Query("number") int number
    );

    @Headers({CACHE, AGENT})
    @GET(POST_ENDPOINT)
    Call<CallbackPost> getPostsByCategory(
            @Query("page") int page,
            @Query("number") int number,
            @Query("category") String category
    );

    @Headers({CACHE, AGENT})
    @GET(POST_ENDPOINT)
    Call<CallbackPost> getSearch(
            @Query("search") String search,
            @Query("page") int page,
            @Query("number") int number
    );

    @Headers({CACHE, AGENT})
    @GET(POST_ENDPOINT + "/{id}")
    Call<CallbackPostDetails> getPostDetail(
            @Path("id") int id
    );

    @Headers({CACHE, AGENT})
    @GET("posts/{id}/replies")
    Call<CallbackComment> getComments(
            @Path("id") int id,
            @Query("page") int page,
            @Query("number") int number,
            @Query("hierarchical") boolean hierarchical,
            @Query("order") String order
    );

    @Headers({CACHE, AGENT})
    @POST("posts/{id}/related")
    Call<CallbackRelated> getRelatePost(
            @Path("id") int id);

    @Headers({CACHE, AGENT})
    @GET("user.json")
    Call<CallbackUser> getUsers();


}