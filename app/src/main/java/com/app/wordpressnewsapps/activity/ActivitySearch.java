package com.app.wordpressnewsapps.activity;

import static com.app.wordpressnewsapps.util.Constant.JETPACK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.adapter.AdapterSearch;
import com.app.wordpressnewsapps.callback.CallbackPost;
import com.app.wordpressnewsapps.database.dao.AppDatabase;
import com.app.wordpressnewsapps.database.dao.DAO;
import com.app.wordpressnewsapps.database.prefs.AdsPref;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.provider.jetpack.adapters.AdapterPostJetpack;
import com.app.wordpressnewsapps.provider.wp.v2.adapters.AdapterPost;
import com.app.wordpressnewsapps.provider.wp.v2.models.Post;
import com.app.wordpressnewsapps.rest.RestAdapter;
import com.app.wordpressnewsapps.util.AdsManager;
import com.app.wordpressnewsapps.util.Constant;
import com.app.wordpressnewsapps.util.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySearch extends AppCompatActivity {

    private static final String TAG = "ActivitySearch";
    private EditText edtSearch;
    private ImageButton btnClear;
    private AdapterSearch adapterSearch;
    private LinearLayout lytSuggestion;
    RecyclerView recyclerView;
    RecyclerView recyclerViewSuggestion;
    Call<List<Post>> callbackCall;
    Call<CallbackPost> callbackCallJetpack;
    AdapterPost adapterPost;
    AdapterPostJetpack adapterPostJetpack;
    SharedPref sharedPref;
    AdsPref adsPref;
    Tools tools;
    AdsManager adsManager;
    CoordinatorLayout parentView;
    ShimmerFrameLayout lytShimmer;
    LinearLayout lytBannerAd;
    private int postTotal = 0;
    private int failedPage = 0;
    DAO db;
    String restApiProvider;
    boolean isShowEmptySearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_search);
        Tools.setNavigation(this);
        db = AppDatabase.getDb(this).get();
        sharedPref = new SharedPref(this);
        restApiProvider = sharedPref.getRestApiProvider();
        adsPref = new AdsPref(this);
        tools = new Tools(this);
        adsManager = new AdsManager(this);
        adsManager.loadBannerAd(adsPref.getIsBannerSearch());
        adsManager.loadInterstitialAd(adsPref.getIsInterstitialPostList(), adsPref.getInterstitialAdInterval());
        initView();
        initShimmerView();
        setupToolbar();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {

        parentView = findViewById(R.id.parent_view);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        lytBannerAd = findViewById(R.id.lyt_banner_ad);

        edtSearch = findViewById(R.id.et_search);
        btnClear = findViewById(R.id.bt_clear);
        btnClear.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        setRecyclerViewAdapter();

        edtSearch.addTextChangedListener(textWatcher);

        lytSuggestion = findViewById(R.id.lyt_suggestion);
        recyclerViewSuggestion = findViewById(R.id.recycler_view_suggestion);
        recyclerViewSuggestion.setLayoutManager(new LinearLayoutManager(this));

        adapterSearch = new AdapterSearch(this);
        recyclerViewSuggestion.setAdapter(adapterSearch);
        showSuggestionSearch();
        adapterSearch.setOnItemClickListener((view, viewModel, pos) -> {
            edtSearch.setText(viewModel);
            edtSearch.setSelection(viewModel.length());
            lytSuggestion.setVisibility(View.GONE);
            if (restApiProvider.equals(JETPACK)) {
                adapterPostJetpack.resetListData();
            } else {
                adapterPost.resetListData();
            }
            hideKeyboard();
            searchAction(1);
        });

        adapterSearch.setOnItemActionClickListener((view, viewModel, pos) -> {
            edtSearch.setText(viewModel);
            edtSearch.setSelection(viewModel.length());
        });

        btnClear.setOnClickListener(v -> {
            edtSearch.setText("");
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                searchAction(1);
                return true;
            }
            return false;
        });

        edtSearch.setOnTouchListener((view, motionEvent) -> {
            if (adapterSearch.getItemCount() > 0) {
                showSuggestionSearch();
            } else {
                lytSuggestion.setVisibility(View.GONE);
                showEmptySearch();
            }
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return false;
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

        swipeProgress(false);

        if (adapterSearch.getItemCount() <= 0) {
            lytSuggestion.setVisibility(View.GONE);
            showEmptySearch();
        }

    }

    private void setRecyclerViewAdapter() {
        if (restApiProvider.equals(JETPACK)) {
            adapterPostJetpack = new AdapterPostJetpack(this, recyclerView, new ArrayList<>(), false, false);
            recyclerView.setAdapter(adapterPostJetpack);
            adapterPostJetpack.setOnItemClickListener((view, post, position) -> {
                Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
                intent.putExtra(Constant.EXTRA_ID, post.ID);
                startActivity(intent);
                sharedPref.savePostId(post.ID);
                adsManager.showInterstitialAd();
                destroyBannerAd();
            });
            adapterPostJetpack.setOnItemOverflowClickListener((view, post, position) -> Tools.onItemPostOverflowJetpack(this, post));
            adapterPostJetpack.setOnLoadMoreListener(current_page -> {
                if (adsPref.getIsNativePostList()) {
                    int totalItemBeforeAds = (adapterPostJetpack.getItemCount() - current_page);
                    if (postTotal > totalItemBeforeAds && current_page != 0) {
                        int next_page = current_page + 1;
                        searchAction(next_page);
                    } else {
                        adapterPostJetpack.setLoaded();
                    }
                } else {
                    if (postTotal > adapterPostJetpack.getItemCount() && current_page != 0) {
                        int next_page = current_page + 1;
                        searchAction(next_page);
                    } else {
                        adapterPostJetpack.setLoaded();
                    }
                }
            });
        } else {
            adapterPost = new AdapterPost(this, recyclerView, new ArrayList<>(), false, false);
            recyclerView.setAdapter(adapterPost);
            adapterPost.setOnItemClickListener((view, post, position) -> {
                Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
                intent.putExtra(Constant.EXTRA_ID, post.id);
                startActivity(intent);
                sharedPref.savePostId(post.id);
                adsManager.showInterstitialAd();
                destroyBannerAd();
            });
            adapterPost.setOnItemOverflowClickListener((view, post, position) -> Tools.onItemPostOverflow(this, post));
            adapterPost.setOnLoadMoreListener(current_page -> {
                if (adsPref.getIsNativePostList()) {
                    int totalItemBeforeAds = (adapterPost.getItemCount() - current_page);
                    if (postTotal > totalItemBeforeAds && current_page != 0) {
                        int next_page = current_page + 1;
                        searchAction(next_page);
                    } else {
                        adapterPost.setLoaded();
                    }
                } else {
                    if (postTotal > adapterPost.getItemCount() && current_page != 0) {
                        int next_page = current_page + 1;
                        searchAction(next_page);
                    } else {
                        adapterPost.setLoaded();
                    }
                }
            });
        }
    }

    private void setupToolbar() {
        Tools.setupToolbar(this, findViewById(R.id.appbar_layout), findViewById(R.id.toolbar), "", true);
        if (sharedPref.getIsDarkTheme()) {
            edtSearch.setTextColor(ContextCompat.getColor(this, R.color.color_white));
            btnClear.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            lytSuggestion.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_background));
        } else {
            btnClear.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            lytSuggestion.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_background));
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                btnClear.setVisibility(View.GONE);
            } else {
                btnClear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void requestAPI(String query, final int page_no) {
        if (restApiProvider.equals(JETPACK)) {
            callbackCallJetpack = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getSearch(query, page_no, sharedPref.getPostsPerPage());
            callbackCallJetpack.enqueue(new Callback<CallbackPost>() {
                @Override
                public void onResponse(@NonNull Call<CallbackPost> call, @NonNull Response<CallbackPost> response) {
                    CallbackPost resp = response.body();
                    if (resp != null) {
                        postTotal = resp.found;
                        adapterPostJetpack.insertData(resp.posts);
                        if (resp.posts.size() > 0) {
                            showNotFoundView(false);
                            lytBannerAd.setVisibility(View.VISIBLE);
                        } else {
                            showNotFoundView(true);
                        }
                        swipeProgress(false);
                    } else {
                        onFailRequest(page_no);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CallbackPost> call, @NonNull Throwable th) {
                    if (!call.isCanceled()) {
                        onFailRequest(page_no);
                    }
                }
            });
        } else {
            callbackCall = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getSearch(query, true, page_no, sharedPref.getPostsPerPage());
            callbackCall.enqueue(new Callback<List<Post>>() {
                public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                    List<Post> posts = response.body();
                    Headers headers = response.headers();
                    if (posts != null) {
                        String _post_total = headers.get("X-WP-Total");
                        assert _post_total != null;
                        postTotal = Integer.parseInt(_post_total);
                        adapterPost.insertData(posts);
                        if (posts.size() > 0) {
                            showNotFoundView(false);
                            lytBannerAd.setVisibility(View.VISIBLE);
                        } else {
                            showNotFoundView(true);
                        }
                        swipeProgress(false);
                    } else {
                        onFailRequest(page_no);
                    }
                }

                public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable th) {
                    if (!call.isCanceled()) {
                        onFailRequest(page_no);
                    }
                }
            });
        }
    }

    private void searchAction(final int page_no) {
        showFailedView(false, "");
        showNotFoundView(false);
        lytSuggestion.setVisibility(View.GONE);
        final String query = edtSearch.getText().toString().trim();
        if (!query.equals("")) {
            if (page_no == 1) {
                if (restApiProvider.equals(JETPACK)) {
                    adapterPostJetpack.resetListData();
                } else {
                    adapterPost.resetListData();
                }
                adapterSearch.addSearchHistory(query);
                swipeProgress(true);
            } else {
                if (restApiProvider.equals(JETPACK)) {
                    adapterPostJetpack.setLoading();
                } else {
                    adapterPost.setLoading();
                }
            }
            new Handler(Looper.getMainLooper()).postDelayed(() -> requestAPI(query, page_no), Constant.DELAY_REFRESH);
        } else {
            Snackbar.make(parentView, getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT).show();
            swipeProgress(false);
        }
    }

    private void onFailRequest(int page_no) {
        failedPage = page_no;
        if (restApiProvider.equals(JETPACK)) {
            adapterPostJetpack.setLoaded();
        } else {
            adapterPost.setLoaded();
        }
        swipeProgress(false);
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showSuggestionSearch() {
        adapterSearch.refreshItems();
        lytSuggestion.setVisibility(View.VISIBLE);
    }

    private void swipeProgress(boolean show) {
        if (show) {
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        } else {
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
        }
    }

    private void initShimmerView() {
        ViewStub stub = findViewById(R.id.shimmer_view_post);
        stub.setLayoutResource(R.layout.shimmer_post_list_default);
        stub.inflate();
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
        findViewById(R.id.failed_retry).setOnClickListener(view -> searchAction(failedPage));
    }

    private void showNotFoundView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.msg_no_search_results);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void showEmptySearch() {
        if (!isShowEmptySearch) {
            View lytNotFound = findViewById(R.id.lyt_no_item);
            ((TextView) findViewById(R.id.no_item_title)).setText(getString(R.string.title_search));
            ((TextView) findViewById(R.id.no_item_message)).setText(getString(R.string.msg_search));
            lytNotFound.setVisibility(View.VISIBLE);
            isShowEmptySearch = true;
        }
    }

    public void hideKeyboard() {
        try {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (edtSearch.length() > 0) {
            edtSearch.setText("");
        } else {
            super.onBackPressed();
            destroyBannerAd();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(adsPref.getIsBannerSearch());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyBannerAd();
    }

    private void destroyBannerAd() {
        adsManager.destroyBannerAd();
    }

}
