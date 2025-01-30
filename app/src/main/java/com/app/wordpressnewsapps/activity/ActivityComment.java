package com.app.wordpressnewsapps.activity;

import static com.app.wordpressnewsapps.util.Constant.JETPACK;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.callback.CallbackComment;
import com.app.wordpressnewsapps.database.prefs.AdsPref;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.provider.jetpack.adapters.AdapterCommentJetpack;
import com.app.wordpressnewsapps.provider.wp.v2.adapters.AdapterComment;
import com.app.wordpressnewsapps.provider.wp.v2.models.Comment;
import com.app.wordpressnewsapps.rest.RestAdapter;
import com.app.wordpressnewsapps.util.AdsManager;
import com.app.wordpressnewsapps.util.Constant;
import com.app.wordpressnewsapps.util.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityComment extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "ActivityComment";
    private Call<List<Comment>> callbackCall = null;
    private Call<CallbackComment> callbackCallJetpack = null;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout lyt_shimmer;
    private AdapterComment adapterComment;
    private AdapterCommentJetpack adapterCommentJetpack;
    private int postTotal = 0;
    private int failedPage = 0;
    private int postId;
    private String postTitle;
    private boolean commentOpen;
    private String commentStatus;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;
    CoordinatorLayout parentView;
    ExtendedFloatingActionButton extendedFloatingActionButton;
    String restApiProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_comment);
        Tools.setNavigation(this);
        postId = getIntent().getIntExtra(Constant.EXTRA_ID, 0);
        postTitle = getIntent().getStringExtra(Constant.EXTRA_NAME);
        commentOpen = getIntent().getBooleanExtra(Constant.EXTRA_COMMENT_OPEN, false);
        commentStatus = getIntent().getStringExtra(Constant.EXTRA_COMMENT_STATUS);

        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        sharedPref = new SharedPref(this);
        restApiProvider = sharedPref.getRestApiProvider();

        adsManager.loadBannerAd(adsPref.getIsBannerPostDetails());

        initView();
        requestAction(1);
        setupToolbar();
    }

    private void setupToolbar() {
        Tools.setupToolbar(this, findViewById(R.id.appbar_layout), findViewById(R.id.toolbar), getString(R.string.title_comment), true);
    }

    public void initView() {
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);

        parentView = findViewById(R.id.parent_view);
        extendedFloatingActionButton = findViewById(R.id.fab_post_comment);
        if (!sharedPref.getIsWpRestV2Enabled()) {
            extendedFloatingActionButton.setVisibility(View.GONE);
        }
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        setRecyclerViewAdapter();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 10 && extendedFloatingActionButton.isExtended()) {
                    extendedFloatingActionButton.shrink();
                }
                if (dy < -10 && !extendedFloatingActionButton.isExtended()) {
                    extendedFloatingActionButton.extend();
                }
                if (!recyclerView.canScrollVertically(-1)) {
                    extendedFloatingActionButton.extend();
                }
            }
        });

    }

    private void setRecyclerViewAdapter() {
        if (restApiProvider.equals(JETPACK)) {
            adapterCommentJetpack = new AdapterCommentJetpack(this, recyclerView, new ArrayList<>());
            recyclerView.setAdapter(adapterCommentJetpack);
            adapterCommentJetpack.setOnLoadMoreListener(current_page -> {
                if (postTotal > adapterCommentJetpack.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    adapterCommentJetpack.setLoaded();
                }
            });
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (callbackCallJetpack != null && callbackCallJetpack.isExecuted()) callbackCallJetpack.cancel();
                adapterCommentJetpack.resetListData();
                requestAction(1);
            });
        } else {
            adapterComment = new AdapterComment(ActivityComment.this, recyclerView, new ArrayList<>());
            recyclerView.setAdapter(adapterComment);
            adapterComment.setOnLoadMoreListener(current_page -> {
                if (postTotal > adapterComment.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    adapterComment.setLoaded();
                }
            });
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
                adapterComment.resetListData();
                requestAction(1);
            });
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            if (restApiProvider.equals(JETPACK)) {
                adapterCommentJetpack.setLoading();
            } else {
                adapterComment.setLoading();
            }
        }
        Tools.postDelayed(() -> requestPostAPI(page_no), 0);
    }

    private void requestPostAPI(final int page_no) {
        if (restApiProvider.equals(JETPACK)) {
            callbackCallJetpack = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getComments(postId, page_no, 100, true, "ASC");
            callbackCallJetpack.enqueue(new Callback<CallbackComment>() {
                @Override
                public void onResponse(@NonNull Call<CallbackComment> call, @NonNull Response<CallbackComment> response) {
                    CallbackComment resp = response.body();
                    if (resp != null) {
                        adapterCommentJetpack.insertData(resp.comments);
                        swipeProgress(false);
                        if (resp.comments.size() == 0) {
                            showNoItemView(true);
                        }
                        extendedFloatingActionButton.setOnClickListener(view -> {
                            if (sharedPref.getIsWpRestV2Enabled()) {
                                if (commentOpen && commentStatus.equals("open")) {
                                    Intent intent = new Intent(getApplicationContext(), ActivityCommentSend.class);
                                    intent.putExtra("post_id", postId);
                                    intent.putExtra("parent", 0);
                                    intent.putExtra("reply", "");
                                    startActivity(intent);
                                } else {
                                    Snackbar.make(parentView, getString(R.string.msg_comment_closed), Snackbar.LENGTH_SHORT).show();
                                }
                            } else {
                                Snackbar.make(parentView, getString(R.string.msg_comment_not_allowed), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        onFailRequest(page_no);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CallbackComment> call, @NonNull Throwable th) {
                    onFailRequest(page_no);
                }
            });
        } else {
            callbackCall = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getComments(postId, page_no, sharedPref.getPostsPerPage(), 0, true, "asc");
            callbackCall.enqueue(new Callback<List<Comment>>() {
                public void onResponse(@NonNull Call<List<Comment>> call, @NonNull Response<List<Comment>> response) {
                    List<Comment> posts = response.body();
                    Headers headers = response.headers();
                    if (posts != null) {
                        String _post_total = headers.get("X-WP-Total");
                        assert _post_total != null;
                        postTotal = Integer.parseInt(_post_total);
                        adapterComment.insertData(posts);
                        swipeProgress(false);
                        if (posts.size() == 0) {
                            showNoItemView(true);
                        }
                        extendedFloatingActionButton.setOnClickListener(view -> {
                            if (sharedPref.getIsWpRestV2Enabled()) {
                                if (commentStatus.equals("open")) {
                                    Intent intent = new Intent(getApplicationContext(), ActivityCommentSend.class);
                                    intent.putExtra("post_id", postId);
                                    intent.putExtra("parent", 0);
                                    intent.putExtra("reply", "");
                                    startActivity(intent);
                                } else {
                                    Snackbar.make(parentView, getString(R.string.msg_comment_closed), Snackbar.LENGTH_SHORT).show();
                                }
                            } else {
                                Snackbar.make(parentView, getString(R.string.msg_comment_not_allowed), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        onFailRequest(page_no);
                    }
                }

                public void onFailure(@NonNull Call<List<Comment>> call, @NonNull Throwable th) {
                    if (!call.isCanceled()) {
                        onFailRequest(page_no);
                    }
                }
            });
        }
    }

    private void onFailRequest(int page_no) {
        failedPage = page_no;
        if (restApiProvider.equals(JETPACK)) {
            adapterCommentJetpack.setLoaded();
        } else {
            adapterComment.setLoaded();
        }
        swipeProgress(false);
        if (Tools.isConnect(ActivityComment.this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failedPage));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_comments_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (restApiProvider.equals(JETPACK)) {
            if (callbackCallJetpack != null && callbackCallJetpack.isExecuted()) {
                callbackCallJetpack.cancel();
            }
        } else {
            if (callbackCall != null && callbackCall.isExecuted()) {
                callbackCall.cancel();
            }
        }
        lyt_shimmer.stopShimmer();
        destroyBannerAd();
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
