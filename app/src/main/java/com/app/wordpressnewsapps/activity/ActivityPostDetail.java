package com.app.wordpressnewsapps.activity;

import static com.app.wordpressnewsapps.util.Constant.JETPACK;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.adapter.AdapterFavoriteLabel;
import com.app.wordpressnewsapps.callback.CallbackPostDetails;
import com.app.wordpressnewsapps.callback.CallbackRelated;
import com.app.wordpressnewsapps.database.dao.AppDatabase;
import com.app.wordpressnewsapps.database.dao.DAO;
import com.app.wordpressnewsapps.database.prefs.AdsPref;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.model.PostEntity;
import com.app.wordpressnewsapps.provider.jetpack.adapters.AdapterRelated;
import com.app.wordpressnewsapps.provider.jetpack.models.Category;
import com.app.wordpressnewsapps.provider.wp.v2.adapters.AdapterLabel;
import com.app.wordpressnewsapps.provider.wp.v2.adapters.AdapterPost;
import com.app.wordpressnewsapps.provider.wp.v2.models.Post;
import com.app.wordpressnewsapps.rest.ApiInterface;
import com.app.wordpressnewsapps.rest.RestAdapter;
import com.app.wordpressnewsapps.util.AdsManager;
import com.app.wordpressnewsapps.util.AppBarLayoutBehavior;
import com.app.wordpressnewsapps.util.Constant;
import com.app.wordpressnewsapps.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPostDetail extends AppCompatActivity {

    Call<Post> callbackCall = null;
    Call<CallbackPostDetails> callbackCallJetpack = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout lytShimmer;
    private View lytMainContent;
    private View lytUncategorized;
    TextView postTitle;
    TextView postDate;
    RelativeLayout lytImage;
    ImageView postImage;
    RecyclerView recyclerViewCategories;
    TextView txtCommentCountIcon;
    TextView txtCommentCountButton;
    LinearLayout btnComment;
    ImageView icComment;
    AdapterLabel adapterLabel;
    private WebView postContent;
    private WebSettings webSettings;
    private String singleChoiceSelected;
    ImageButton btnSearch;
    ImageButton btnFontSize;
    ImageButton btnFavorite;
    ImageButton btnOverflow;
    RecyclerView recyclerViewRelated;
    LinearLayout lytRelated;
    CoordinatorLayout parentView;
    AdapterPost adapterPost;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;
    Tools tools;
    int postId;
    DAO db;
    boolean flag_read_later;
    String restApiProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_post_detail);
        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        tools = new Tools(this);
        sharedPref = new SharedPref(this);
        restApiProvider = sharedPref.getRestApiProvider();

        db = AppDatabase.getDb(this).get();
        Tools.setNavigation(this);

        postId = getIntent().getIntExtra(Constant.EXTRA_ID, 0);

        parentView = findViewById(R.id.parent_view);
        postTitle = findViewById(R.id.post_title);
        postDate = findViewById(R.id.post_date);
        lytImage = findViewById(R.id.lyt_image);
        postImage = findViewById(R.id.post_image);
        recyclerViewCategories = findViewById(R.id.recycler_view_categories);
        postContent = findViewById(R.id.post_content);
        webSettings = postContent.getSettings();
        btnComment = findViewById(R.id.btn_comment);
        icComment = findViewById(R.id.ic_comment);
        txtCommentCountIcon = findViewById(R.id.txt_comment_count_icon);
        txtCommentCountButton = findViewById(R.id.txt_comment_count_button);
        recyclerViewRelated = findViewById(R.id.recycler_view_related);
        lytRelated = findViewById(R.id.lyt_related);
        btnSearch = findViewById(R.id.btn_search);
        btnFontSize = findViewById(R.id.btn_font_size);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnOverflow = findViewById(R.id.btn_overflow);

        lytMainContent = findViewById(R.id.lyt_main_content);
        lytUncategorized = findViewById(R.id.view_uncategorized);

        lytShimmer = findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytMainContent.setVisibility(View.GONE);
            if (Tools.isConnect(this)) {
                requestAction();
            } else {
                Tools.postDelayed(()-> {
                    swipeProgress(false);
                    lytMainContent.setVisibility(View.VISIBLE);
                    displayDataOffline(db.getFavorite(postId));
                }, 1000);
            }
        });

        if (Tools.isConnect(this)) {
            requestAction();
        } else {
            displayDataOffline(db.getFavorite(postId));
        }
        setupToolbar();

    }

    private void setupToolbar() {
        final AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());
        Tools.setupToolbar(this, appBarLayout, toolbar, "", true);
        if (sharedPref.getIsDarkTheme()) {
            btnSearch.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            btnFontSize.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            btnOverflow.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            icComment.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
        } else {
            btnSearch.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            btnFontSize.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            btnOverflow.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            icComment.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
        }
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        Tools.postDelayed(this::requestPostData, Constant.DELAY_REFRESH);
    }

    private void requestPostData() {
        if (restApiProvider.equals(JETPACK)) {
            this.callbackCallJetpack = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getPostDetail(postId);
            this.callbackCallJetpack.enqueue(new Callback<CallbackPostDetails>() {
                public void onResponse(@NonNull Call<CallbackPostDetails> call, @NonNull Response<CallbackPostDetails> response) {
                    CallbackPostDetails resp = response.body();
                    if (resp != null) {
                        displayDataJetpack(resp);
                        swipeProgress(false);
                        lytMainContent.setVisibility(View.VISIBLE);
                        if (sharedPref.getIsShowRelatedPosts()) {
                            displayRelatedPosts(postId);
                        }
                    } else {
                        onFailRequest();
                    }
                }

                public void onFailure(@NonNull Call<CallbackPostDetails> call, @NonNull Throwable th) {
                    if (!call.isCanceled()) {
                        onFailRequest();
                    }
                }
            });
        } else {
            this.callbackCall = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getPostDetail(postId, true);
            this.callbackCall.enqueue(new Callback<Post>() {
                public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                    Post resp = response.body();
                    if (resp != null) {
                        displayData(resp);
                        swipeProgress(false);
                        lytMainContent.setVisibility(View.VISIBLE);
                    } else {
                        onFailRequest();
                    }
                }

                public void onFailure(@NonNull Call<Post> call, @NonNull Throwable th) {
                    if (!call.isCanceled()) {
                        onFailRequest();
                    }
                }
            });
        }
    }

    private void onFailRequest() {
        swipeProgress(false);
        lytMainContent.setVisibility(View.GONE);
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            lytMainContent.setVisibility(View.VISIBLE);
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytMainContent.setVisibility(View.GONE);
        });
    }

    private void displayData(Post post) {

        postTitle.setText(Html.fromHtml(post.title.rendered));

        if (sharedPref.getIsShowPostDate()) {
            postDate.setText(Tools.convertDateTime(post.date_gmt, "dd MMMM yyyy, HH:mm"));
        } else {
            postDate.setVisibility(View.GONE);
        }

        String imageUrl;
        if (!post.featured_media.equals("0") && !post._embedded.wp_featured_media.toString().equals("[]") && post._embedded.wp_featured_media.get(0).media_details != null) {
            imageUrl = post._embedded.wp_featured_media.get(0).media_details.sizes.full.source_url;
            Glide.with(getApplicationContext())
                    .load(imageUrl.replace(" ", "%20"))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(postImage);
            postImage.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(), ActivityImageDetail.class);
                intent.putExtra("image_url", imageUrl);
                startActivity(intent);
                showInterstitialAdCounter();
            });
        } else {
            imageUrl = "";
            postImage.setImageResource(R.drawable.ic_no_image);
            lytImage.setVisibility(View.GONE);
        }

        int postComment;
        if (sharedPref.getIsEnableCommentFeature()) {
            if (post.comment_status.equals("open")) {
                if (!post._embedded.replies.toString().equals("[]")) {
                    postComment = post._embedded.replies.get(0).size();
                    showComment(true, postComment);
                } else {
                    postComment = 0;
                    showComment(true, postComment);
                }
            } else {
                if (!post._embedded.replies.toString().equals("[]")) {
                    postComment = post._embedded.replies.get(0).size();
                    showComment(postComment > 0, postComment);
                } else {
                    postComment = -1;
                    showComment(false, postComment);
                }
            }

            btnComment.setOnClickListener(view -> {
                Tools.openCommentActivity(this, post.id, post.title.rendered, true, post.comment_status);
                showInterstitialAdCounter();
            });

            txtCommentCountButton.setOnClickListener(view -> {
                Tools.openCommentActivity(this, post.id, post.title.rendered, true, post.comment_status);
                showInterstitialAdCounter();
            });
        } else {
            postComment = -1;
            showComment(false, postComment);
        }

        List<String> list = new ArrayList<>();
        String categories;
        if (post._embedded.wp_term.get(0).size() > 0) {
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(ActivityPostDetail.this);
            layoutManager.setFlexDirection(FlexDirection.ROW);
            layoutManager.setAlignItems(AlignItems.FLEX_START);
            recyclerViewCategories.setLayoutManager(layoutManager);
            adapterLabel = new AdapterLabel(ActivityPostDetail.this, post._embedded.wp_term.get(0));
            recyclerViewCategories.setAdapter(adapterLabel);

            if (sharedPref.getIsShowRelatedPosts()) {
                displayRelatedPosts(post._embedded.wp_term.get(0).get(0).id);
            }

            adapterLabel.setOnItemClickListener((view, items, position) -> {
                Intent intent = new Intent(getApplicationContext(), ActivityCategoryDetail.class);
                intent.putExtra(Constant.EXTRA_ID, items.get(position).id);
                intent.putExtra(Constant.EXTRA_NAME, items.get(position).name);
                startActivity(intent);
                showInterstitialAdCounter();
            });

            for (int i = 0; i < post._embedded.wp_term.get(0).size(); i++) {
                int categoryId = post._embedded.wp_term.get(0).get(i).id;
                String categoryName = post._embedded.wp_term.get(0).get(i).name;
                list.add(categoryId + "|" + categoryName);
            }
            categories = TextUtils.join(",", list);
        } else {
            categories = "";
            lytUncategorized.setVisibility(View.VISIBLE);
        }

        tools.showPostContent(postContent, webSettings, post.content.rendered);

        btnSearch.setOnClickListener(view -> {
            Tools.openSearchActivity(this);
            adsManager.destroyBannerAd();
        });

        btnFontSize.setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(ActivityPostDetail.this);
            dialog.setTitle(getString(R.string.title_dialog_font_size));
            dialog.setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i]);
            dialog.setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                webSettings = postContent.getSettings();
                if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                    sharedPref.updateFontSize(0);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                    sharedPref.updateFontSize(1);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                    sharedPref.updateFontSize(2);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                    sharedPref.updateFontSize(3);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                    sharedPref.updateFontSize(4);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
                } else {
                    sharedPref.updateFontSize(2);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                }
                dialogInterface.dismiss();
            });
            dialog.show();
        });

        flag_read_later = db.getFavorite(post.id) != null;
        if (flag_read_later) {
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
        }
        btnFavorite.setOnClickListener(view -> onFavoriteClicked(post.id, imageUrl, post.title.rendered, post.excerpt.rendered, categories, post.date_gmt, post.content.rendered, postComment, post.link));

        btnOverflow.setOnClickListener(view -> {
            tools.showBottomSheetDialogMoreOptions(
                    parentView,
                    post.id,
                    imageUrl,
                    post.title.rendered,
                    post.excerpt.rendered,
                    categories,
                    post.date_gmt,
                    post.content.rendered,
                    postComment,
                    post.link,
                    true
            );
        });

        loadAds(adsManager);

    }

    private void displayDataJetpack(CallbackPostDetails post) {

        postTitle.setText(Html.fromHtml(post.title));

        if (sharedPref.getIsShowPostDate()) {
            postDate.setText(Tools.convertDateTime(post.date, "dd MMMM yyyy, HH:mm"));
        } else {
            postDate.setVisibility(View.GONE);
        }

        String imageUrl;
        if (post.featured_image != null) {
            imageUrl = post.featured_image;
            if (!imageUrl.equals("")) {
                Glide.with(getApplicationContext())
                        .load(imageUrl.replace(" ", "%20"))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(postImage);
                postImage.setOnClickListener(view -> {
                    Intent intent = new Intent(getApplicationContext(), ActivityImageDetail.class);
                    intent.putExtra("image_url", imageUrl);
                    startActivity(intent);
                    showInterstitialAdCounter();
                });
            } else {
                lytImage.setVisibility(View.GONE);
            }
        } else {
            imageUrl = "";
            postImage.setImageResource(R.drawable.ic_no_image);
            lytImage.setVisibility(View.GONE);
        }

        int postComment;
        if (sharedPref.getIsEnableCommentFeature()) {
            if (post.discussion.comments_open && post.discussion.comment_status.equals("open")) {
                postComment = post.discussion.comment_count;
                showComment(true, postComment);
            } else {
                if (post.discussion.comment_count > 0) {
                    postComment = post.discussion.comment_count;
                    showComment(true, postComment);
                } else {
                    postComment = -1;
                    showComment(false, postComment);
                }
            }

            btnComment.setOnClickListener(view -> {
                Tools.openCommentActivity(this, post.ID, post.title, post.discussion.comments_open, post.discussion.comment_status);
                showInterstitialAdCounter();
            });

            txtCommentCountButton.setOnClickListener(view -> {
                Tools.openCommentActivity(this, post.ID, post.title, post.discussion.comments_open, post.discussion.comment_status);
                showInterstitialAdCounter();
            });
        } else {
            postComment = -1;
            showComment(false, postComment);
        }

        tools.showPostContent(postContent, webSettings, post.content);

        btnSearch.setOnClickListener(view -> {
            Tools.openSearchActivity(this);
            adsManager.destroyBannerAd();
        });

        btnFontSize.setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(ActivityPostDetail.this);
            dialog.setTitle(getString(R.string.title_dialog_font_size));
            dialog.setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i]);
            dialog.setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                webSettings = postContent.getSettings();
                if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                    sharedPref.updateFontSize(0);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                    sharedPref.updateFontSize(1);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                    sharedPref.updateFontSize(2);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                    sharedPref.updateFontSize(3);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                    sharedPref.updateFontSize(4);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
                } else {
                    sharedPref.updateFontSize(2);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                }
                dialogInterface.dismiss();
            });
            dialog.show();
        });

        List<String> categories = new ArrayList<>();
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(ActivityPostDetail.this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setAlignItems(AlignItems.FLEX_START);
        recyclerViewCategories.setLayoutManager(layoutManager);
        if (!(post.categories == null || post.categories.size() == 0)) {
            for (Map.Entry<String, Category> entry : post.categories.entrySet()) {
                String values = entry.getValue().ID + "|" + entry.getValue().name + "|" + entry.getValue().slug;
                categories.add(values);
            }
        }
        AdapterFavoriteLabel adapterLabel = new AdapterFavoriteLabel(this, categories);
        recyclerViewCategories.setAdapter(adapterLabel);
        adapterLabel.setOnItemClickListener((view, items, position) -> {
            String[] data = items.get(position).split("\\|");
            Intent intent = new Intent(getApplicationContext(), ActivityCategoryDetail.class);
            intent.putExtra(Constant.EXTRA_ID, Integer.parseInt(data[0]));
            intent.putExtra(Constant.EXTRA_NAME, data[1]);
            intent.putExtra(Constant.EXTRA_SLUG, data[2]);
            startActivity(intent);
            showInterstitialAdCounter();
        });

        flag_read_later = db.getFavorite(post.ID) != null;
        if (flag_read_later) {
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
        }
        btnFavorite.setOnClickListener(view -> onFavoriteClicked(post.ID, imageUrl, post.title, post.excerpt, TextUtils.join(",", categories), post.date, post.content, postComment, post.URL));

        btnOverflow.setOnClickListener(view -> {
            tools.showBottomSheetDialogMoreOptions(
                    parentView,
                    post.ID,
                    imageUrl,
                    post.title,
                    post.excerpt,
                    TextUtils.join(",", categories),
                    post.date,
                    post.content,
                    postComment,
                    post.URL,
                    true
            );
        });

        loadAds(adsManager);

    }

    private void displayDataOffline(PostEntity post) {
        postTitle.setText(Html.fromHtml(post.title));

        if (sharedPref.getIsShowPostDate()) {
            postDate.setText(Tools.convertDateTime(post.date, "dd MMMM yyyy, HH:mm"));
        } else {
            postDate.setVisibility(View.GONE);
        }

        String imageUrl;
        if (post.image != null) {
            imageUrl = post.image;
            if (!imageUrl.equals("")) {
                Glide.with(getApplicationContext())
                        .load(imageUrl.replace(" ", "%20"))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(postImage);
                postImage.setOnClickListener(view -> {
                    Intent intent = new Intent(getApplicationContext(), ActivityImageDetail.class);
                    intent.putExtra("image_url", imageUrl);
                    startActivity(intent);
                });
            } else {
                lytImage.setVisibility(View.GONE);
            }
        } else {
            imageUrl = "";
            postImage.setImageResource(R.drawable.ic_no_image);
            lytImage.setVisibility(View.GONE);
        }

        int postComment;
        if (sharedPref.getIsEnableCommentFeature()) {
            if (post.comment_count > 0) {
                postComment = post.comment_count;
                showComment(true, postComment);
            } else {
                postComment = -1;
                showComment(false, postComment);
            }
        } else {
            postComment = -1;
            showComment(false, postComment);
        }

        tools.showPostContent(postContent, webSettings, post.content);

        btnSearch.setOnClickListener(view -> {
            Tools.openSearchActivity(this);
            adsManager.destroyBannerAd();
        });

        btnFontSize.setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(ActivityPostDetail.this);
            dialog.setTitle(getString(R.string.title_dialog_font_size));
            dialog.setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i]);
            dialog.setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                webSettings = postContent.getSettings();
                if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                    sharedPref.updateFontSize(0);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                    sharedPref.updateFontSize(1);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                    sharedPref.updateFontSize(2);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                    sharedPref.updateFontSize(3);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                    sharedPref.updateFontSize(4);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
                } else {
                    sharedPref.updateFontSize(2);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                }
                dialogInterface.dismiss();
            });
            dialog.show();
        });

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(ActivityPostDetail.this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setAlignItems(AlignItems.FLEX_START);
        recyclerViewCategories.setLayoutManager(layoutManager);
        List<String> categories = new ArrayList<>(Arrays.asList(post.category.replace(", ", ",").split(",")));
        AdapterFavoriteLabel adapterLabel = new AdapterFavoriteLabel(this, categories);
        recyclerViewCategories.setAdapter(adapterLabel);
        adapterLabel.setOnItemClickListener((view, items, position) -> {
            String[] data = items.get(position).split("\\|");
            Intent intent = new Intent(getApplicationContext(), ActivityCategoryDetail.class);
            intent.putExtra(Constant.EXTRA_ID, Integer.parseInt(data[0]));
            intent.putExtra(Constant.EXTRA_NAME, data[1]);
            intent.putExtra(Constant.EXTRA_SLUG, data[2]);
            startActivity(intent);
        });

        flag_read_later = db.getFavorite(post.id) != null;
        if (flag_read_later) {
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
        }
        btnFavorite.setOnClickListener(view -> onFavoriteClicked(post.id, imageUrl, post.title, post.excerpt, post.category, post.date, post.content, postComment, post.link));

        btnOverflow.setOnClickListener(view -> {
            tools.showBottomSheetDialogMoreOptions(
                    parentView,
                    post.id,
                    imageUrl,
                    post.title,
                    post.excerpt,
                    TextUtils.join(",", categories),
                    post.date,
                    post.content,
                    postComment,
                    post.link,
                    true
            );
        });
    }

    private void showComment(boolean show, int count) {
        if (show) {
            txtCommentCountIcon.setText("" + count);
            btnComment.setVisibility(View.VISIBLE);
            Tools.postDelayed(() -> {
                if (count > 0) {
                    txtCommentCountButton.setText(getString(R.string.txt_read) + " " + count + " " + getString(R.string.txt_comment));
                } else {
                    txtCommentCountButton.setText(getString(R.string.txt_no_comment));
                }
                txtCommentCountButton.setVisibility(View.VISIBLE);
            }, 500);
        } else {
            btnComment.setVisibility(View.GONE);
            txtCommentCountButton.setVisibility(View.GONE);
        }
    }

    public void onFavoriteClicked(int id, String image, String title, String excerpt, String category, String date, String content, int comment_count, String link) {
        if (db.getFavorite(id) != null) {
            db.deleteFavorite(id);
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
            Tools.showSnackBar(parentView, getString(R.string.msg_favorite_removed));
        } else {
            db.addFavorite(System.currentTimeMillis(), id, image, title, excerpt, category, date, content, comment_count, link);
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite);
            Tools.showSnackBar(parentView, getString(R.string.msg_favorite_added));
        }
    }

    private void displayRelatedPosts(int id) {
        if (restApiProvider.equals(JETPACK)) {
            recyclerViewRelated.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
            AdapterRelated adapterRelated = new AdapterRelated(this, new ArrayList<>());
            recyclerViewRelated.setAdapter(adapterRelated);
            ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl());
            Call<CallbackRelated> callbackCall = apiInterface.getRelatePost(postId);
            callbackCall.enqueue(new Callback<CallbackRelated>() {
                @Override
                public void onResponse(@NonNull Call<CallbackRelated> call, @NonNull Response<CallbackRelated> response) {
                    CallbackRelated resp = response.body();
                    if (resp != null) {
                        adapterRelated.insertData(resp.hits);
                        lytRelated.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CallbackRelated> call, @NonNull Throwable th) {
                }
            });
        } else {
            recyclerViewRelated.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
            adapterPost = new AdapterPost(this, recyclerViewRelated, new ArrayList<>(), false, true);
            recyclerViewRelated.setAdapter(adapterPost);
            Call<List<Post>> callbackCall = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getPostsByCategory(id, true, 1, sharedPref.getMaxRelatedPosts());
            callbackCall.enqueue(new Callback<List<Post>>() {
                public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                    List<Post> posts = response.body();
                    if (posts != null) {
                        adapterPost.setListData(posts);
                        lytRelated.setVisibility(View.VISIBLE);
                    } else {
                        lytRelated.setVisibility(View.GONE);
                    }
                }

                public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable th) {
                    if (!call.isCanceled()) {
                        lytRelated.setVisibility(View.GONE);
                    }
                }
            });
            adapterPost.setOnItemClickListener((view, post, position) -> {
                Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
                intent.putExtra(Constant.EXTRA_ID, post.id);
                startActivity(intent);
                sharedPref.savePostId(post.id);
                showInterstitialAdCounter();
            });
            adapterPost.setOnItemOverflowClickListener((view, post, position) -> Tools.onItemPostOverflow(this, post));
        }
    }

    private void loadAds(AdsManager adsManager) {
        adsManager.loadBannerAd(adsPref.getIsBannerPostDetails());
        adsManager.loadInterstitialAd(adsPref.getIsInterstitialPostDetails(), 1);
        LinearLayout nativeAdView;
        if (adsPref.getNativeAdPostDetailsPosition().equals("top")) {
            nativeAdView = findViewById(R.id.native_ad_view_top);
        } else {
            nativeAdView = findViewById(R.id.native_ad_view_bottom);
        }
        Tools.setNativeAdStyle(this, nativeAdView, adsPref.getNativeAdStylePostDetails());
        adsManager.loadNativeAd(adsPref.getIsNativePostDetails());
    }

    public void showInterstitialAdCounter() {
        if (adsPref.getCounter() >= adsPref.getInterstitialAdInterval()) {
            adsPref.saveCounter(1);
            adsManager.showInterstitialAd();
        } else {
            adsPref.saveCounter(adsPref.getCounter() + 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        destroyBannerAd();
    }

    @Override
    public void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(adsPref.getIsBannerPostDetails());
    }

    private void destroyBannerAd() {
        adsManager.destroyBannerAd();
    }

}
