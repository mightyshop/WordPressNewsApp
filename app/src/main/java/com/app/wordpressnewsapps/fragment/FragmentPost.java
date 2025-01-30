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
import com.app.wordpressnewsapps.activity.ActivityPostDetail;
import com.app.wordpressnewsapps.activity.MainActivity;
import com.app.wordpressnewsapps.callback.CallbackPost;
import com.app.wordpressnewsapps.database.prefs.AdsPref;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.provider.jetpack.adapters.AdapterPostJetpack;
import com.app.wordpressnewsapps.provider.wp.v2.adapters.AdapterPost;
import com.app.wordpressnewsapps.provider.wp.v2.models.Post;
import com.app.wordpressnewsapps.rest.RestAdapter;
import com.app.wordpressnewsapps.util.Constant;
import com.app.wordpressnewsapps.util.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentPost extends Fragment {

    private static final String TAG = "FragmentPost";
    View rootView;
    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout lytShimmer;
    Call<List<Post>> callbackCall;
    Call<CallbackPost> callbackCallJetpack;
    private AdapterPost adapterPost;
    private AdapterPostJetpack adapterPostJetpack;
    private int postTotal = 0;
    private int failedPage = 0;
    SharedPref sharedPref;
    AdsPref adsPref;
    Activity activity;
    Tools tools;
    String restApiProvider;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_post, container, false);
        tools = new Tools(activity);
        adsPref = new AdsPref(activity);
        sharedPref = new SharedPref(activity);
        restApiProvider = sharedPref.getRestApiProvider();
        initView();
        initShimmerView();
        requestAction(1);
        return rootView;
    }

    public void initView() {
        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        recyclerView = rootView.findViewById(R.id.recycler_view);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        setRecyclerViewAdapter();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

    }

    private void setRecyclerViewAdapter() {
        if (restApiProvider.equals(JETPACK)) {
            adapterPostJetpack = new AdapterPostJetpack(activity, recyclerView, new ArrayList<>(), sharedPref.getIsShowPostListHeader(), false);
            recyclerView.setAdapter(adapterPostJetpack);
            adapterPostJetpack.setOnItemClickListener((view, post, position) -> {
                Intent intent = new Intent(activity, ActivityPostDetail.class);
                intent.putExtra(Constant.EXTRA_ID, post.ID);
                startActivity(intent);
                sharedPref.savePostId(post.ID);
                ((MainActivity) activity).showInterstitialAd();
                ((MainActivity) activity).destroyBannerAd();
            });
            adapterPostJetpack.setOnItemOverflowClickListener((view, post, position) -> {
                Tools.onItemPostOverflowJetpack(activity, post);
            });
            adapterPostJetpack.setOnLoadMoreListener(current_page -> {
                if (adsPref.getIsNativePostList()) {
                    int totalItemBeforeAds = (adapterPostJetpack.getItemCount() - current_page);
                    if (postTotal > totalItemBeforeAds && current_page != 0) {
                        int next_page = current_page + 1;
                        requestAction(next_page);
                    } else {
                        adapterPostJetpack.setLoaded();
                    }
                } else {
                    if (postTotal > adapterPostJetpack.getItemCount() && current_page != 0) {
                        int next_page = current_page + 1;
                        requestAction(next_page);
                    } else {
                        adapterPostJetpack.setLoaded();
                    }
                }
            });
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (callbackCallJetpack != null && callbackCallJetpack.isExecuted())
                    callbackCallJetpack.cancel();
                adapterPostJetpack.resetListData();
                requestAction(1);
            });
        } else {
            adapterPost = new AdapterPost(activity, recyclerView, new ArrayList<>(), sharedPref.getIsShowPostListHeader(), false);
            recyclerView.setAdapter(adapterPost);
            adapterPost.setOnItemClickListener((view, post, position) -> {
                Intent intent = new Intent(activity, ActivityPostDetail.class);
                intent.putExtra(Constant.EXTRA_ID, post.id);
                startActivity(intent);
                sharedPref.savePostId(post.id);
                ((MainActivity) activity).showInterstitialAd();
                ((MainActivity) activity).destroyBannerAd();
            });
            adapterPost.setOnItemOverflowClickListener((view, post, position) -> {
                Tools.onItemPostOverflow(activity, post);
            });
            adapterPost.setOnLoadMoreListener(current_page -> {
                if (adsPref.getIsNativePostList()) {
                    int totalItemBeforeAds = (adapterPost.getItemCount() - current_page);
                    if (postTotal > totalItemBeforeAds && current_page != 0) {
                        int next_page = current_page + 1;
                        requestAction(next_page);
                    } else {
                        adapterPost.setLoaded();
                    }
                } else {
                    if (postTotal > adapterPost.getItemCount() && current_page != 0) {
                        int next_page = current_page + 1;
                        requestAction(next_page);
                    } else {
                        adapterPost.setLoaded();
                    }
                }
            });
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
                adapterPost.resetListData();
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
                adapterPostJetpack.setLoading();
            } else {
                adapterPost.setLoading();
            }
        }
        Tools.postDelayed(() -> requestPostAPI(page_no), 10);
    }

    private void requestPostAPI(final int page_no) {
        if (restApiProvider.equals(JETPACK)) {
            callbackCallJetpack = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getPosts(page_no, sharedPref.getPostsPerPage());
            callbackCallJetpack.enqueue(new Callback<CallbackPost>() {
                @Override
                public void onResponse(@NonNull Call<CallbackPost> call, @NonNull Response<CallbackPost> response) {
                    CallbackPost resp = response.body();
                    if (resp != null && resp.found > 0) {
                        postTotal = resp.found;
                        adapterPostJetpack.insertData(resp.posts);
                        showNoItemView(resp.posts.size() == 0);
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
            callbackCall = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getPosts(true, page_no, sharedPref.getPostsPerPage());
            callbackCall.enqueue(new Callback<List<Post>>() {
                public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                    List<Post> posts = response.body();
                    Headers headers = response.headers();
                    if (posts != null) {
                        String _post_total = headers.get("X-WP-Total");
                        assert _post_total != null;
                        postTotal = Integer.parseInt(_post_total);
                        adapterPost.insertData(posts);
                        showNoItemView(posts.size() == 0);
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

    private void onFailRequest(int page_no) {
        failedPage = page_no;
        if (restApiProvider.equals(JETPACK)) {
            adapterPostJetpack.setLoaded();
        } else {
            adapterPost.setLoaded();
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
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
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
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

    private void initShimmerView() {
        ViewStub shimmerPostHead = rootView.findViewById(R.id.shimmer_view_head);
        ViewStub shimmerPostList = rootView.findViewById(R.id.shimmer_view_post);

        if (sharedPref.getIsShowPostListHeader()) {
            shimmerPostHead.setLayoutResource(R.layout.shimmer_post_head);
        } else {
            shimmerPostHead.setLayoutResource(R.layout.shimmer_post_list_default);
        }
        shimmerPostHead.inflate();

        if (sharedPref.getIsPostListInLargeStyle()) {
            shimmerPostList.setLayoutResource(R.layout.shimmer_post_list_large);
        } else {
            shimmerPostList.setLayoutResource(R.layout.shimmer_post_list_default);
        }
        shimmerPostList.inflate();
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
        lytShimmer.stopShimmer();
    }

}
