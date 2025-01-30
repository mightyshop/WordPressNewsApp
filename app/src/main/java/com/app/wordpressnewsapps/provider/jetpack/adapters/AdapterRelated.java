package com.app.wordpressnewsapps.provider.jetpack.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.activity.ActivityCategoryDetail;
import com.app.wordpressnewsapps.activity.ActivityPostDetail;
import com.app.wordpressnewsapps.adapter.AdapterFavoriteLabel;
import com.app.wordpressnewsapps.callback.CallbackPostDetails;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.provider.jetpack.models.Category;
import com.app.wordpressnewsapps.provider.jetpack.models.Hits;
import com.app.wordpressnewsapps.rest.RestAdapter;
import com.app.wordpressnewsapps.util.Constant;
import com.app.wordpressnewsapps.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.divider.MaterialDivider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterRelated extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Call<CallbackPostDetails> callbackCall = null;
    List<Hits> items;
    Context context;
    private OnItemClickListener mOnItemClickListener;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Hits obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterRelated(Context context, List<Hits> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView postTitle;
        public TextView postExcerpt;
        public TextView postDate;
        public ImageView icComment;
        public LinearLayout lytComment;
        public TextView txtCommentCount;
        public RecyclerView recyclerViewCategory;
        public View txt_label_uncategorized;
        public TextView txt_label;
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
            txt_label_uncategorized = v.findViewById(R.id.txt_label_uncategorized);
            txt_label = v.findViewById(R.id.txt_label);
            lytImage = v.findViewById(R.id.lyt_image);
            postImage = v.findViewById(R.id.post_image);
            lytParent = v.findViewById(R.id.lyt_parent);
            itemView = v.findViewById(R.id.item_view);
            btnOverflow = v.findViewById(R.id.btn_overflow);
            materialDivider = v.findViewById(R.id.view_divider);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view;
        if (sharedPref.getIsPostListInLargeStyle()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_related_large, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_related_default, parent, false);
        }
        viewHolder = new OriginalViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Hits hits = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            this.callbackCall = RestAdapter.createAPI(sharedPref.getRestApiProvider(), sharedPref.getSiteUrl()).getPostDetail(hits.fields.post_id);
            this.callbackCall.enqueue(new Callback<CallbackPostDetails>() {
                public void onResponse(@NonNull Call<CallbackPostDetails> call, @NonNull Response<CallbackPostDetails> response) {
                    CallbackPostDetails resp = response.body();
                    if (resp != null) {
                        displayData(vItem, resp);
                    } else {
                        vItem.itemView.setVisibility(View.GONE);
                    }
                }

                public void onFailure(@NonNull Call<CallbackPostDetails> call, @NonNull Throwable th) {
                }
            });
        }
    }

    private void displayData(OriginalViewHolder vItem, CallbackPostDetails post) {

        vItem.postTitle.setText(Html.fromHtml(post.title));
        vItem.postExcerpt.setText(Html.fromHtml(post.excerpt));

        if (sharedPref.getIsShowPostListExcerpt()) {
            vItem.postTitle.setMaxLines(2);
            vItem.postExcerpt.setVisibility(View.VISIBLE);
        } else {
            vItem.postTitle.setMaxLines(4);
            vItem.postExcerpt.setVisibility(View.GONE);
        }

        if (sharedPref.getIsShowPostDate()) {
            vItem.postDate.setVisibility(View.VISIBLE);
            vItem.postDate.setText(Tools.convertDateTime(post.date, null));
        } else {
            vItem.postDate.setVisibility(View.GONE);
        }

        if (sharedPref.getIsEnableCommentFeature()) {
            if (sharedPref.getIsShowPostListComment()) {
                vItem.lytComment.setVisibility(View.VISIBLE);
                int postComment;
                if (post.discussion.comment_status.equals("open")) {
                    postComment = post.discussion.comment_count;
                    vItem.txtCommentCount.setText("" + postComment);
                } else {
                    vItem.lytComment.setVisibility(View.GONE);
                }
            } else {
                vItem.lytComment.setVisibility(View.GONE);
            }
        } else {
            vItem.lytComment.setVisibility(View.GONE);
        }

        if (post.featured_image != null) {
            if (!post.featured_image.equals("")) {
                Glide.with(context.getApplicationContext())
                        .load(post.featured_image.replace(" ", "%20"))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(vItem.postImage);
            } else {
                vItem.lytImage.setVisibility(View.GONE);
            }
        } else {
            vItem.lytImage.setVisibility(View.GONE);
        }

        if (sharedPref.getIsShowPostListCategories()) {
            displayCategory(post.categories, vItem);
        } else {
            vItem.recyclerViewCategory.setVisibility(View.GONE);
        }

        vItem.lytParent.setOnClickListener(view -> {
            Intent intent = new Intent(context, ActivityPostDetail.class);
            intent.putExtra(Constant.EXTRA_ID, post.ID);
            context.startActivity(intent);
            sharedPref.savePostId(post.ID);
            ((ActivityPostDetail) context).showInterstitialAdCounter();
        });

        vItem.btnOverflow.setOnClickListener(view -> {
            Tools.onItemPostOverflowJetpack((Activity) context, post);
        });

        if (sharedPref.getIsShowPostListLineDivider()) {
            vItem.materialDivider.setVisibility(View.VISIBLE);
        } else {
            vItem.materialDivider.setVisibility(View.GONE);
        }

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

    }

    private void displayCategory(Map<String, Category> map, OriginalViewHolder vItem) {
        List<String> categories = new ArrayList<>();
        vItem.recyclerViewCategory.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        if (!(map == null || map.size() == 0)) {
            for (Map.Entry<String, Category> entry : map.entrySet()) {
                String values = entry.getValue().ID + "|" + entry.getValue().name + "|" + entry.getValue().slug;
                categories.add(values);
            }
        }
        AdapterFavoriteLabel adapterLabel = new AdapterFavoriteLabel(context, categories);
        vItem.recyclerViewCategory.setAdapter(adapterLabel);
        adapterLabel.setOnItemClickListener((view, items, position) -> {
            String[] data = items.get(position).split("\\|");
            Intent intent = new Intent(context, ActivityCategoryDetail.class);
            intent.putExtra(Constant.EXTRA_ID, Integer.parseInt(data[0]));
            intent.putExtra(Constant.EXTRA_NAME, data[1]);
            intent.putExtra(Constant.EXTRA_SLUG, data[2]);
            context.startActivity(intent);
        });
    }

    public void insertData(List<Hits> items) {
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
        return Math.min(items.size(), sharedPref.getMaxRelatedPosts());
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
                        int current_page = getItemCount() / (sharedPref.getCategoriesPerPage() + 1);
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