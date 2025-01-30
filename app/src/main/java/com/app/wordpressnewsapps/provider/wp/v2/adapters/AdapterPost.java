package com.app.wordpressnewsapps.provider.wp.v2.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.activity.ActivityCategoryDetail;
import com.app.wordpressnewsapps.database.prefs.AdsPref;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.provider.wp.v2.models.Post;
import com.app.wordpressnewsapps.util.Constant;
import com.app.wordpressnewsapps.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.divider.MaterialDivider;
import com.solodroid.ads.sdk.format.NativeAdViewHolder;

import java.util.List;

public class AdapterPost extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_HEAD = 1;
    private final int VIEW_ITEM = 2;
    private final int VIEW_AD = 3;
    List<Post> posts;
    Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    boolean scrolling = false;
    AdsPref adsPref;
    SharedPref sharedPref;
    AdapterLabel adapterLabel;
    boolean showPostHeader;
    boolean isRelated;

    public interface OnItemClickListener {
        void onItemClick(View view, Post post, int position);
    }

    public interface OnItemOverflowClickListener {
        void onItemOverflowClick(View view, Post post, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemOverflowClickListener mItemOverflowClickListener) {
        this.mOnItemOverflowClickListener = mItemOverflowClickListener;
    }

    public AdapterPost(Context context, RecyclerView view, List<Post> posts, boolean showPostHeader, boolean isRelated) {
        this.posts = posts;
        this.context = context;
        this.adsPref = new AdsPref(context);
        this.sharedPref = new SharedPref(context);
        this.showPostHeader = showPostHeader;
        this.isRelated = isRelated;
        lastItemViewDetector(view);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView postTitle;
        public TextView postExcerpt;
        public TextView postDate;
        public ImageView icComment;
        public LinearLayout lytComment;
        public TextView txtCommentCount;
        public RecyclerView recyclerViewCategory;
        public RelativeLayout lytImage;
        public ImageView postImage;
        public LinearLayout lytParent;
        public LinearLayout itemView;
        public ImageView btnOverflow;
        public MaterialDivider materialDivider;

        public OriginalViewHolder(View v) {
            super(v);
            postTitle = v.findViewById(R.id.post_title);
            postExcerpt = v.findViewById(R.id.post_excerpt);
            postDate = v.findViewById(R.id.post_date);
            lytComment = v.findViewById(R.id.lyt_comment);
            icComment = v.findViewById(R.id.ic_comment);
            txtCommentCount = v.findViewById(R.id.txt_comment_count);
            recyclerViewCategory = v.findViewById(R.id.post_categories);
            lytImage = v.findViewById(R.id.lyt_image);
            postImage = v.findViewById(R.id.post_image);
            lytParent = v.findViewById(R.id.lyt_parent);
            itemView = v.findViewById(R.id.item_view);
            btnOverflow = v.findViewById(R.id.btn_overflow);
            materialDivider = v.findViewById(R.id.view_divider);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {

        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }

    }

    public static class NativeViewHolder extends NativeAdViewHolder {

        public MaterialDivider materialDivider;

        public NativeViewHolder(View v) {
            super(v);
            materialDivider = v.findViewById(R.id.view_divider);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if (viewType == VIEW_HEAD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_large, parent, false);
            viewHolder = new OriginalViewHolder(view);
        } else if (viewType == VIEW_ITEM) {
            View view;
            if (sharedPref.getIsPostListInLargeStyle()) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_large, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_default, parent, false);
            }
            viewHolder = new OriginalViewHolder(view);
        } else if (viewType == VIEW_AD) {
            View view;
            if (adsPref.getNativeAdStylePostList().equals("small")) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad_small, parent, false);
            } else if (adsPref.getNativeAdStylePostList().equals("medium")) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad_medium, parent, false);
            } else if (adsPref.getNativeAdStylePostList().equals("large")) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad_large, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad_medium, parent, false);
            }
            viewHolder = new NativeViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            viewHolder = new ProgressViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Post p = posts.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.postTitle.setText(Html.fromHtml(p.title.rendered));
            vItem.postExcerpt.setText(Html.fromHtml(p.excerpt.rendered));

            if (sharedPref.getIsShowPostListExcerpt()) {
                vItem.postTitle.setMaxLines(2);
                vItem.postExcerpt.setVisibility(View.VISIBLE);
            } else {
                vItem.postTitle.setMaxLines(4);
                vItem.postExcerpt.setVisibility(View.GONE);
            }

            if (sharedPref.getIsEnableCommentFeature()) {
                if (sharedPref.getIsShowPostListComment()) {
                    int postComment;
                    if (p.comment_status.equals("open")) {
                        if (!p._embedded.replies.toString().equals("[]")) {
                            postComment = p._embedded.replies.get(0).size();
                        } else {
                            postComment = 0;
                        }
                        vItem.txtCommentCount.setText("" + postComment);
                        vItem.lytComment.setVisibility(View.VISIBLE);
                    } else {
                        if (!p._embedded.replies.toString().equals("[]")) {
                            postComment = p._embedded.replies.get(0).size();
                            if (postComment > 0) {
                                vItem.txtCommentCount.setText("" + postComment);
                                vItem.lytComment.setVisibility(View.VISIBLE);
                            } else {
                                vItem.lytComment.setVisibility(View.GONE);
                            }
                        } else {
                            vItem.lytComment.setVisibility(View.GONE);
                        }
                    }
                } else {
                    vItem.lytComment.setVisibility(View.GONE);
                }
            } else {
                vItem.lytComment.setVisibility(View.GONE);
            }

            if (sharedPref.getIsShowPostDate()) {
                vItem.postDate.setVisibility(View.VISIBLE);
                vItem.postDate.setText(Tools.convertDateTime(p.date_gmt, null));
            } else {
                vItem.postDate.setVisibility(View.GONE);
            }

            vItem.recyclerViewCategory.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            adapterLabel = new AdapterLabel(context, p._embedded.wp_term.get(0));
            vItem.recyclerViewCategory.setAdapter(adapterLabel);
            adapterLabel.setOnItemClickListener((view, items, pos) -> {
                Intent intent = new Intent(context, ActivityCategoryDetail.class);
                intent.putExtra(Constant.EXTRA_ID, items.get(pos).id);
                intent.putExtra(Constant.EXTRA_NAME, items.get(pos).name);
                context.startActivity(intent);
            });

            if (sharedPref.getIsShowPostListCategories()) {
                vItem.recyclerViewCategory.setVisibility(View.VISIBLE);
            } else {
                vItem.recyclerViewCategory.setVisibility(View.GONE);
            }

            if (!p._embedded.wp_featured_media.toString().equals("[]")) {
                if (p._embedded.wp_featured_media.get(0).media_details != null) {
                    String image_url = p._embedded.wp_featured_media.get(0).media_details.sizes.full.source_url;
                    if (image_url != null) {
                        Glide.with(context)
                                .load(image_url.replace(" ", "%20"))
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(vItem.postImage);
                        vItem.lytImage.setVisibility(View.VISIBLE);
                    } else {
                        vItem.lytImage.setVisibility(View.GONE);
                        vItem.postImage.setImageResource(R.drawable.ic_no_image);
                    }
                } else {
                    vItem.lytImage.setVisibility(View.GONE);
                    vItem.postImage.setImageResource(R.drawable.ic_no_image);
                }
            } else {
                vItem.lytImage.setVisibility(View.GONE);
                vItem.postImage.setImageResource(R.drawable.ic_no_image);
            }

            vItem.lytParent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });

            if (sharedPref.getIsDarkTheme()) {
                vItem.btnOverflow.setColorFilter(ContextCompat.getColor(context, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                vItem.icComment.setColorFilter(ContextCompat.getColor(context, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                vItem.postExcerpt.setTextColor(ContextCompat.getColor(context, R.color.color_dark_text));
                vItem.postDate.setTextColor(ContextCompat.getColor(context, R.color.color_dark_text));
                vItem.txtCommentCount.setTextColor(ContextCompat.getColor(context, R.color.color_dark_text));
            } else {
                vItem.btnOverflow.setColorFilter(ContextCompat.getColor(context, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                vItem.icComment.setColorFilter(ContextCompat.getColor(context, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                vItem.postExcerpt.setTextColor(ContextCompat.getColor(context, R.color.color_light_text));
                vItem.postDate.setTextColor(ContextCompat.getColor(context, R.color.color_light_text));
                vItem.txtCommentCount.setTextColor(ContextCompat.getColor(context, R.color.color_light_text));
            }

            vItem.btnOverflow.setOnClickListener(view -> {
                if (mOnItemOverflowClickListener != null) {
                    mOnItemOverflowClickListener.onItemOverflowClick(view, p, position);
                }
            });

            if (isRelated) {
                if (p.id == sharedPref.getPostId()) {
                    vItem.itemView.setVisibility(View.GONE);
                } else {
                    vItem.itemView.setVisibility(View.VISIBLE);
                }
            }

            if (sharedPref.getIsShowPostListLineDivider()) {
                vItem.materialDivider.setVisibility(View.VISIBLE);
            } else {
                vItem.materialDivider.setVisibility(View.GONE);
            }

        } else if (holder instanceof NativeViewHolder) {

            final NativeViewHolder vItem = (NativeViewHolder) holder;

            if (adsPref.getAdStatus()) {
                vItem.loadNativeAd(context,
                        "1",
                        1,
                        adsPref.getMainAds(),
                        adsPref.getBackupAds(),
                        adsPref.getAdMobNativeId(),
                        adsPref.getAdManagerNativeId(),
                        adsPref.getFanNativeId(),
                        adsPref.getApplovinMaxNativeId(),
                        adsPref.getApplovinDiscoveryMrecZoneId(),
                        adsPref.getWortiseNativeId(),
                        sharedPref.getIsDarkTheme(),
                        Constant.LEGACY_GDPR,
                        adsPref.getNativeAdStylePostList(),
                        R.color.color_light_native_ad_background,
                        R.color.color_dark_native_ad_background
                );
            }

            if (sharedPref.getIsShowPostListLineDivider()) {
                vItem.materialDivider.setVisibility(View.VISIBLE);
            } else {
                vItem.materialDivider.setVisibility(View.GONE);
                vItem.setNativeAdMargin(
                        context.getResources().getDimensionPixelOffset(R.dimen.no_spacing),
                        context.getResources().getDimensionPixelOffset(R.dimen.spacing_small),
                        context.getResources().getDimensionPixelOffset(R.dimen.no_spacing),
                        context.getResources().getDimensionPixelOffset(R.dimen.spacing_small)
                );
            }

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        layoutParams.setFullSpan(getItemViewType(position) == VIEW_PROG || getItemViewType(position) == VIEW_AD);

    }

    public void insertData(List<Post> items) {
        setLoaded();
        int positionStart = getItemCount();
        if (adsPref.getIsNativePostList()) {
            if (items.size() >= adsPref.getNativeAdIndex()) {
                items.add(adsPref.getNativeAdIndex(), new Post());
            }
        }
        int itemCount = items.size();
        this.posts.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setListData(List<Post> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.posts.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (posts.get(i) == null) {
                posts.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.posts.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.posts.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (isRelated) {
            return Math.min(posts.size(), 5);
        } else {
            return posts.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        Post post = posts.get(position);
        if (post != null) {
            if (showPostHeader) {
                if (position == 0) {
                    return VIEW_HEAD;
                }
            }
            if (post.title == null || post.title.rendered.equals("")) {
                return VIEW_AD;
            }
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page;
                        if (adsPref.getIsNativePostList()) {
                            current_page = getItemCount() / (sharedPref.getPostsPerPage() + 1);
                        } else {
                            current_page = getItemCount() / (sharedPref.getPostsPerPage());
                        }
                        onLoadMoreListener.onLoadMore(current_page);
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int last_idx = into[0];
        for (int i : into) {
            if (last_idx < i) last_idx = i;
        }
        return last_idx;
    }

}