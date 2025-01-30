package com.app.wordpressnewsapps.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.database.prefs.SharedPref;

import java.util.List;

public class AdapterFavoriteLabel extends RecyclerView.Adapter<AdapterFavoriteLabel.ViewHolder> {

    Context context;
    List<String> items;
    private OnItemClickListener mOnItemClickListener;
    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, List<String> items, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterFavoriteLabel(Context context, List<String> items) {
        this.context = context;
        this.items = items;
        this.sharedPref = new SharedPref(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        LinearLayout lyt_label;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txt_label);
            lyt_label = view.findViewById(R.id.lyt_label);
        }
    }

    @NonNull
    @Override
    public AdapterFavoriteLabel.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_label, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterFavoriteLabel.ViewHolder vItem, int position) {
        final String term = items.get(position);

        String categoryName;
        if (term.contains("|")) {
            String[] data = term.split("\\|");
            categoryName = data[1];
        } else {
            categoryName = context.getResources().getString(R.string.txt_uncategorized);
        }

        vItem.title.setText(Html.fromHtml(categoryName));

        vItem.lyt_label.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, items, position);
            }
        });

        if (sharedPref.getIsDarkTheme()) {
            vItem.lyt_label.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chips_dark));
            vItem.title.setTextColor(ContextCompat.getColor(context, R.color.color_dark_text));
        } else {
            vItem.lyt_label.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chips_default));
            vItem.title.setTextColor(ContextCompat.getColor(context, R.color.color_light_text));
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}