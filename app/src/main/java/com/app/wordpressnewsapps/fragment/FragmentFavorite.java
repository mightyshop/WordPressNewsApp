package com.app.wordpressnewsapps.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.activity.ActivityPostDetail;
import com.app.wordpressnewsapps.activity.MainActivity;
import com.app.wordpressnewsapps.adapter.AdapterFavorite;
import com.app.wordpressnewsapps.database.dao.AppDatabase;
import com.app.wordpressnewsapps.database.dao.DAO;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.model.PostEntity;
import com.app.wordpressnewsapps.util.Constant;
import com.app.wordpressnewsapps.util.Tools;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    View rootView;
    RecyclerView recyclerView;
    AdapterFavorite adapterFavorite;
    Activity activity;
    SharedPref sharedPref;
    DAO db;
    Tools tools;
    private BottomSheetDialog mBottomSheetDialog;
    boolean flag_read_later;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        db = AppDatabase.getDb(activity).get();
        tools = new Tools(activity);
        sharedPref = new SharedPref(activity);
        initView();
        return rootView;
    }

    private void initView() {
        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        adapterFavorite = new AdapterFavorite(activity, new ArrayList<>());
        recyclerView.setAdapter(adapterFavorite);

        adapterFavorite.setOnItemClickListener((view, post, position) -> {
            Intent intent = new Intent(activity, ActivityPostDetail.class);
            intent.putExtra(Constant.EXTRA_ID, post.id);
            startActivity(intent);
            sharedPref.savePostId(post.id);

            ((MainActivity) activity).showInterstitialAd();
            ((MainActivity) activity).destroyBannerAd();
        });

        adapterFavorite.setOnItemOverflowClickListener((view, post, position) -> {
            showBottomSheetDialog(
                    activity.findViewById(R.id.parent_view),
                    post.id,
                    post.image,
                    post.title,
                    post.excerpt,
                    post.category,
                    post.date,
                    post.content,
                    post.comment_count,
                    post.link,
                    false
            );
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        displayData(db.getAllFavorite());
    }

    private void displayData(final List<PostEntity> posts) {
        adapterFavorite.resetListData();
        adapterFavorite.insertData(posts);
        View lytNoFavorite = rootView.findViewById(R.id.lyt_no_favorite);
        if (posts.size() > 0) {
            lytNoFavorite.setVisibility(View.GONE);
        } else {
            lytNoFavorite.setVisibility(View.VISIBLE);
        }
    }

    public void showBottomSheetDialog(View parentView, int id, String image, String title, String excerpt, String category, String date, String content, int comment_count, String link, boolean isDetailView) {
        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.dialog_more_options, null);
        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);

        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
        ImageView imgLaunch = view.findViewById(R.id.img_launch);
        ImageView imgShare = view.findViewById(R.id.img_share);
        ImageView imgReport = view.findViewById(R.id.img_report);
        ImageView imgFeedback = view.findViewById(R.id.img_feedback);
        ImageView btnClose = view.findViewById(R.id.btn_close);

        if (sharedPref.getIsDarkTheme()) {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_dark));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgLaunch.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgFeedback.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
        } else {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_default));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgLaunch.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgFeedback.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
        }

        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
        LinearLayout btnLaunch = view.findViewById(R.id.btn_launch);
        LinearLayout btnShare = view.findViewById(R.id.btn_share);
        LinearLayout btnReport = view.findViewById(R.id.btn_report);
        LinearLayout btnFeedback = view.findViewById(R.id.btn_feedback);

        if (!sharedPref.getIsEnableViewOnSiteMenu()) {
            btnLaunch.setVisibility(View.GONE);
        }

        flag_read_later = db.getFavorite(id) != null;
        if (flag_read_later) {
            imgFavorite.setImageResource(R.drawable.ic_menu_favorite);
            ((TextView) view.findViewById(R.id.txt_favorite)).setText(activity.getString(R.string.menu_favorite_remove));
        } else {
            imgFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
            ((TextView) view.findViewById(R.id.txt_favorite)).setText(activity.getString(R.string.menu_favorite_add));
        }
        btnFavorite.setOnClickListener(v -> {
            if (isDetailView) {
                ((ActivityPostDetail) activity).onFavoriteClicked(id, image, title, excerpt, category, date, content, comment_count, link);
            } else {
                if (db.getFavorite(id) != null) {
                    db.deleteFavorite(id);
                    imgFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
                    Tools.showSnackBar(parentView, activity.getString(R.string.msg_favorite_removed));
                    displayData(db.getAllFavorite());
                } else {
                    db.addFavorite(System.currentTimeMillis(), id, image, title, excerpt, category, date, content, comment_count, link);
                    imgFavorite.setImageResource(R.drawable.ic_menu_favorite);
                    Tools.showSnackBar(parentView, activity.getString(R.string.msg_favorite_added));
                }
            }
            mBottomSheetDialog.dismiss();
        });

        btnLaunch.setOnClickListener(v -> {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            Tools.sharePost(activity, title, link);
            mBottomSheetDialog.dismiss();
        });

        btnReport.setOnClickListener(v -> {
            Tools.sendReport(activity, sharedPref.getEmailFeedbackAndReport(), title, "");
            mBottomSheetDialog.dismiss();
        });

        btnFeedback.setOnClickListener(v -> {
            Tools.sendFeedback(activity, sharedPref.getEmailFeedbackAndReport());
            mBottomSheetDialog.dismiss();
        });

        btnClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

        if (sharedPref.getIsEnableRtlMode()) {
            if (sharedPref.getIsDarkTheme()) {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDarkRtl);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLightRtl);
            }
        } else {
            if (sharedPref.getIsDarkTheme()) {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
            }
        }
        this.mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

    }

}
