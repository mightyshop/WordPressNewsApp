package com.app.wordpressnewsapps.fragment;

import static com.app.wordpressnewsapps.util.Constant.JETPACK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.activity.ActivityCategoryDetail;
import com.app.wordpressnewsapps.activity.MainActivity;
import com.app.wordpressnewsapps.adapter.AdapterCustomCategory;
import com.app.wordpressnewsapps.callback.CallbackCategory;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.provider.jetpack.adapters.AdapterCategoryJetpack;
import com.app.wordpressnewsapps.provider.wp.v2.adapters.AdapterCategory;
import com.app.wordpressnewsapps.provider.wp.v2.models.Category;
import com.app.wordpressnewsapps.rest.RestAdapter;
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

public class FragmentCategory extends Fragment {

    View rootView;
    SwipeRefreshLayout swipeRefreshLayout;
    private Call<List<Category>> callbackCall = null;
    private Call<CallbackCategory> callbackCallJetpack = null;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout lytShimmer;
    private AdapterCategory adapterCategory;
    private AdapterCategoryJetpack adapterCategoryJetpack;
    private AdapterCustomCategory adapterCustomCategory;
    private int postTotal = 0;
    private int failedPage = 0;
    private boolean isCustomCategory;
    SharedPref sharedPref;
    boolean isPostTotal = false;
    Activity activity;
    String restApiProvider;
    int categoryColumnCount;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_category, container, false);
        sharedPref = new SharedPref(activity);
        restApiProvider = sharedPref.getRestApiProvider();
        isCustomCategory = sharedPref.getIsCustomCategory();
        initView();
        initShimmerView();
        requestAction(1);
        return rootView;
    }

    public void initView() {
        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        categoryColumnCount = Math.min(sharedPref.getCategoryColumnCount(), 3);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(categoryColumnCount, StaggeredGridLayoutManager.VERTICAL));

        if (sharedPref.getCategoryColumnCount() > 1) {
            recyclerView.setPadding(
                    activity.getResources().getDimensionPixelSize(R.dimen.spacing_small),
                    activity.getResources().getDimensionPixelSize(R.dimen.spacing_small),
                    activity.getResources().getDimensionPixelSize(R.dimen.spacing_small),
                    activity.getResources().getDimensionPixelSize(R.dimen.spacing_small)
            );
        }

        setRecyclerViewAdapter();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

    }

    private void setRecyclerViewAdapter() {
        if (isCustomCategory) {
            adapterCustomCategory = new AdapterCustomCategory(activity, new ArrayList<>());
            recyclerView.setAdapter(adapterCustomCategory);
            adapterCustomCategory.setOnItemClickListener((view, obj, position) -> {
                Intent intent = new Intent(activity, ActivityCategoryDetail.class);
                intent.putExtra(Constant.EXTRA_ID, obj.id);
                intent.putExtra(Constant.EXTRA_NAME, obj.name);
                intent.putExtra(Constant.EXTRA_SLUG, obj.name.toLowerCase().replace(" ", "-"));
                startActivity(intent);
                ((MainActivity) activity).showInterstitialAd();
                ((MainActivity) activity).destroyBannerAd();
            });
            swipeRefreshLayout.setOnRefreshListener(() -> {
                swipeProgress(true);
                showFailedView(false, "");
                Tools.postDelayed(() -> {
                    adapterCustomCategory.resetListData();
                    requestAction(1);
                }, 50);
            });
        } else {
            if (restApiProvider.equals(JETPACK)) {
                adapterCategoryJetpack = new AdapterCategoryJetpack(activity, recyclerView, new ArrayList<>());
                recyclerView.setAdapter(adapterCategoryJetpack);
                adapterCategoryJetpack.setOnItemClickListener((view, obj, position) -> {
                    if (obj.post_count == 0) {
                        Snackbar.make(activity.findViewById(R.id.parent_view), getString(R.string.no_post_category_found), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(activity, ActivityCategoryDetail.class);
                        intent.putExtra(Constant.EXTRA_ID, obj.ID);
                        intent.putExtra(Constant.EXTRA_NAME, obj.name);
                        intent.putExtra(Constant.EXTRA_SLUG, obj.slug);
                        startActivity(intent);
                        ((MainActivity) activity).showInterstitialAd();
                        ((MainActivity) activity).destroyBannerAd();
                    }
                });
                adapterCategoryJetpack.setOnLoadMoreListener(current_page -> {
                    if (postTotal > adapterCategoryJetpack.getItemCount() && current_page != 0) {
                        int next_page = current_page + 1;
                        requestAction(next_page);
                    } else {
                        adapterCategoryJetpack.setLoaded();
                    }
                });
                swipeRefreshLayout.setOnRefreshListener(() -> {
                    if (callbackCallJetpack != null && callbackCallJetpack.isExecuted())
                        callbackCallJetpack.cancel();
                    adapterCategoryJetpack.resetListData();
                    requestAction(1);
                });
            } else {
                adapterCategory = new AdapterCategory(activity, recyclerView, new ArrayList<>());
                recyclerView.setAdapter(adapterCategory);
                adapterCategory.setOnItemClickListener((view, obj, position) -> {
                    Intent intent = new Intent(activity, ActivityCategoryDetail.class);
                    intent.putExtra(Constant.EXTRA_ID, obj.id);
                    intent.putExtra(Constant.EXTRA_NAME, obj.name);
                    intent.putExtra(Constant.EXTRA_SLUG, obj.slug);
                    startActivity(intent);
                    ((MainActivity) activity).showInterstitialAd();
                    ((MainActivity) activity).destroyBannerAd();
                });
                adapterCategory.setOnLoadMoreListener(current_page -> {
                    if (postTotal > adapterCategory.getItemCount() && current_page != 0) {
                        int next_page = current_page + 1;
                        requestAction(next_page);
                    } else {
                        adapterCategory.setLoaded();
                    }
                });
                swipeRefreshLayout.setOnRefreshListener(() -> {
                    if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
                    adapterCategory.resetListData();
                    requestAction(1);
                });
            }
        }
    }

    private void requestAction(final int page_no) {
        if (isCustomCategory) {
            if (Tools.isConnect(activity)) {
                showFailedView(false, "");
                List<com.app.wordpressnewsapps.model.entities.Category> customCategories = sharedPref.getCustomCategoryList();
                if (customCategories != null && customCategories.size() > 0) {
                    adapterCustomCategory.setListData(customCategories);
                    showNoItemView(false);
                } else {
                    showNoItemView(true);
                }
            } else {
                showFailedView(true, getString(R.string.failed_text));
            }
            swipeProgress(false);
        } else {
            showFailedView(false, "");
            showNoItemView(false);
            if (page_no == 1) {
                swipeProgress(true);
            } else {
                if (restApiProvider.equals(JETPACK)) {
                    adapterCategoryJetpack.setLoading();
                } else {
                    adapterCategory.setLoading();
                }
            }
            Tools.postDelayed(() -> requestCategoryAPI(page_no), 10);
        }
    }

    private void requestCategoryAPI(final int page_no) {
        if (restApiProvider.equals(JETPACK)) {
            callbackCallJetpack = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getCategories(page_no, sharedPref.getCategoriesPerPage());
            callbackCallJetpack.enqueue(new Callback<CallbackCategory>() {
                @Override
                public void onResponse(@NonNull Call<CallbackCategory> call, @NonNull Response<CallbackCategory> response) {
                    CallbackCategory resp = response.body();
                    if (resp != null && resp.found > 0) {
                        postTotal = resp.found;
                        adapterCategoryJetpack.insertData(resp.categories);
                        swipeProgress(false);
                        if (resp.categories.size() == 0) {
                            showNoItemView(true);
                        }
                    } else {
                        onFailRequest(page_no);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CallbackCategory> call, @NonNull Throwable th) {
                    if (!call.isCanceled()) {
                        onFailRequest(page_no);
                    }
                }
            });
        } else {
            callbackCall = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getAllCategories(page_no, sharedPref.getCategoriesPerPage());
            callbackCall.enqueue(new Callback<List<Category>>() {
                public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                    List<Category> categories = response.body();
                    Headers headers = response.headers();
                    if (categories != null) {
                        if (!isPostTotal) {
                            isPostTotal = true;
                            String _post_total = headers.get("X-WP-Total");
                            assert _post_total != null;
                            postTotal = Integer.parseInt(_post_total);
                        }
                        adapterCategory.insertData(categories);
                        swipeProgress(false);
                        if (categories.size() == 0) {
                            showNoItemView(true);
                        }
                    } else {
                        onFailRequest(page_no);
                    }
                }

                public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable th) {
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
            adapterCategoryJetpack.setLoaded();
        } else {
            adapterCategory.setLoaded();
        }
        swipeProgress(false);
        if (Tools.isConnect(activity)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = rootView.findViewById(R.id.lyt_failed);
        ((TextView) rootView.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failedPage));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = rootView.findViewById(R.id.lyt_no_item);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
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
            recyclerView.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            recyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

    private void initShimmerView() {
        ViewStub shimmerCategory = rootView.findViewById(R.id.shimmer_view_category);
        if (sharedPref.getCategoryColumnCount() == 2) {
            shimmerCategory.setLayoutResource(R.layout.shimmer_category_grid2);
        } else if (sharedPref.getCategoryColumnCount() == 3) {
            shimmerCategory.setLayoutResource(R.layout.shimmer_category_grid3);
        } else {
            shimmerCategory.setLayoutResource(R.layout.shimmer_category_list);
        }
        shimmerCategory.inflate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (!isCustomCategory) {
            if (restApiProvider.equals(JETPACK)) {
                if (callbackCallJetpack != null && callbackCallJetpack.isExecuted()) {
                    callbackCallJetpack.cancel();
                }
            } else {
                if (callbackCall != null && callbackCall.isExecuted()) {
                    callbackCall.cancel();
                }
            }
        }
        lytShimmer.stopShimmer();
    }

}
