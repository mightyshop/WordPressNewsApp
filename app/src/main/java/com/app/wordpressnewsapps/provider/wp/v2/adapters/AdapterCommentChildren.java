package com.app.wordpressnewsapps.provider.wp.v2.adapters;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.provider.wp.v2.models.entities.Children;
import com.app.wordpressnewsapps.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class AdapterCommentChildren extends RecyclerView.Adapter<AdapterCommentChildren.ViewHolder> {

    Context context;
    List<Children> items;
    private OnItemClickListener mOnItemClickListener;
    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view,  Children obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterCommentChildren(Context context, List<Children> items) {
        this.context = context;
        this.items = items;
        this.sharedPref = new SharedPref(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView authorAvatar;
        public TextView authorName;
        public TextView commentDate;
        public TextView commentMessage;
        public ImageView btnOverflow;
        public RelativeLayout lytParent;

        public ViewHolder(View v) {
            super(v);
            authorAvatar = v.findViewById(R.id.children_author_avatar);
            authorName = v.findViewById(R.id.children_author_name);
            commentDate = v.findViewById(R.id.children_comment_date);
            commentMessage = v.findViewById(R.id.children_comment_message);
            btnOverflow = v.findViewById(R.id.children_btn_overflow);
            lytParent = v.findViewById(R.id.children_lyt_parent);
        }
    }

    @NonNull
    @Override
    public AdapterCommentChildren.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment_child, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterCommentChildren.ViewHolder vItem, int position) {
        final Children comment = items.get(position);

        vItem.authorName.setText(comment.author_name);
        vItem.commentDate.setText(Tools.convertDateTime(comment.date, null));

        vItem.commentMessage.setText(Html.fromHtml(comment.content.rendered));
        vItem.commentMessage.setMovementMethod(LinkMovementMethod.getInstance());

        Glide.with(context)
                .load(comment.author_avatar_urls.large.replace(" ", "%20"))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(vItem.authorAvatar);

        vItem.btnOverflow.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, comment, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}