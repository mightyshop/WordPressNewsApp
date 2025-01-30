package com.app.wordpressnewsapps.provider.jetpack.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.database.prefs.AdsPref;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.provider.jetpack.models.Comment;
import com.app.wordpressnewsapps.util.Constant;
import com.app.wordpressnewsapps.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.solodroid.ads.sdk.format.NativeAdViewHolder;

import java.util.List;

public class AdapterCommentJetpack extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_AD = 2;
    private List<Comment> items;
    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    boolean scrolling = false;
    SharedPref sharedPref;
    AdsPref adsPref;
    Tools tools;

    public interface OnItemClickListener {
        void onItemClick(View view, Comment obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterCommentJetpack(Context context, RecyclerView view, List<Comment> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.adsPref = new AdsPref(context);
        this.tools = new Tools((Activity) context);
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

        public ImageView authorAvatar;
        public TextView authorName;
        public TextView commentDate;
        public TextView commentMessage;
        public ImageView btnOverflow;
        public RelativeLayout lytParent;
        public View viewCommentChild;

        public OriginalViewHolder(View v) {
            super(v);
            authorAvatar = v.findViewById(R.id.author_avatar);
            authorName = v.findViewById(R.id.author_name);
            commentDate = v.findViewById(R.id.comment_date);
            commentMessage = v.findViewById(R.id.comment_message);
            viewCommentChild = v.findViewById(R.id.view_comment_child);
            btnOverflow = v.findViewById(R.id.btn_overflow);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {

        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_jetpack, parent, false);
            vh = new OriginalViewHolder(v);
        } else if (viewType == VIEW_AD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(com.solodroid.ads.sdk.R.layout.view_native_ad_radio, parent, false);
            vh = new NativeAdViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Comment comment = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.authorName.setText(comment.author.name);
            vItem.commentDate.setText(Tools.convertDateTime(comment.date, null));

            vItem.commentMessage.setText(Html.fromHtml(comment.content));
            vItem.commentMessage.setMovementMethod(LinkMovementMethod.getInstance());

            Glide.with(context)
                    .load(comment.author.avatar_URL.replace(" ", "%20"))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(vItem.authorAvatar);

            String parent = String.valueOf(comment.parent);
            if (parent.equals("false")) {
                vItem.viewCommentChild.setVisibility(View.GONE);
            } else {
                vItem.viewCommentChild.setVisibility(View.VISIBLE);
            }

            vItem.btnOverflow.setOnClickListener(view -> {
                tools.showBottomSheetDialogComment(comment.post.ID, comment.ID, comment.author.name, comment.content);
            });

            if (sharedPref.getIsDarkTheme()) {
                vItem.btnOverflow.setColorFilter(ContextCompat.getColor(context, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                vItem.commentMessage.setTextColor(ContextCompat.getColor(context, R.color.color_dark_text));
            } else {
                vItem.btnOverflow.setColorFilter(ContextCompat.getColor(context, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                vItem.commentMessage.setTextColor(ContextCompat.getColor(context, R.color.color_light_text));
            }

            if (!sharedPref.getIsWpRestV2Enabled()) {
                vItem.btnOverflow.setVisibility(View.GONE);
            }

        } else if (holder instanceof NativeAdViewHolder) {
            final NativeAdViewHolder vItem = (NativeAdViewHolder) holder;

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

            vItem.setNativeAdMargin(
                    context.getResources().getDimensionPixelOffset(R.dimen.no_spacing),
                    context.getResources().getDimensionPixelOffset(R.dimen.no_spacing),
                    context.getResources().getDimensionPixelOffset(R.dimen.no_spacing),
                    context.getResources().getDimensionPixelOffset(R.dimen.spacing_xlarge)
            );
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

        if (getItemViewType(position) == VIEW_PROG) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
        }

    }

    public void insertData(List<Comment> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Comment comment = items.get(position);
        if (comment != null) {
            if (comment.content == null || comment.content.equals("")) {
                return VIEW_AD;
            } else {
                return VIEW_ITEM;
            }
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
                        //int current_page = getItemCount() / (sharedPref.getCategoriesPerPage() + 1);
                        int current_page = getItemCount() / 100;
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