package com.app.wordpressnewsapps.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.app.wordpressnewsapps.BuildConfig;
import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.adapter.AdapterSearch;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.util.Tools;
import com.app.wordpressnewsapps.util.ViewAnimation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DecimalFormat;

public class ActivitySettings extends AppCompatActivity {

    private static final String TAG = "Settings";
    MaterialSwitch switchTheme;
    LinearLayout btnSwitchTheme;

    private String singleChoiceSelected;
    TextView txt_cache_size;
    TextView txt_user_name;
    TextView txt_user_email;
    LinearLayout btn_user_name;
    LinearLayout btn_user_email;
    SharedPref sharedPref;
    private LinearLayout btn_toggle_general;
    private LinearLayout btn_toggle_cache;
    private LinearLayout btn_toggle_privacy;
    private LinearLayout btn_toggle_about;
    private View lyt_expand_general;
    private View lyt_expand_cache;
    private View lyt_expand_privacy;
    private View lyt_expand_about;
    NestedScrollView nested_scroll_view;
    ImageButton btn_arrow_general;
    ImageButton btn_arrow_cache;
    ImageButton btn_arrow_privacy;
    ImageButton btn_arrow_about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_settings);
        Tools.setNavigation(this);
        sharedPref = new SharedPref(this);
        initView();
        initToggleView();
        setupToolbar();
    }

    public void setupToolbar() {
        Tools.setupToolbar(this, findViewById(R.id.appbar_layout), findViewById(R.id.toolbar), getString(R.string.title_settings), true);
    }

    private void initView() {
        switchTheme = findViewById(R.id.switch_theme);
        switchTheme.setChecked(sharedPref.getIsDarkTheme());
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPref.setIsDarkTheme(isChecked);
            Tools.postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }, 350);
        });

        btnSwitchTheme = findViewById(R.id.btn_switch_theme);
        btnSwitchTheme.setOnClickListener(v -> {
            if (switchTheme.isChecked()) {
                sharedPref.setIsDarkTheme(false);
                switchTheme.setChecked(false);
            } else {
                sharedPref.setIsDarkTheme(true);
                switchTheme.setChecked(true);
            }
            Tools.postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }, 350);
        });

        findViewById(R.id.btn_text_size).setOnClickListener(v -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            new MaterialAlertDialogBuilder(ActivitySettings.this)
                    .setTitle(getString(R.string.title_dialog_font_size))
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                    .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                        if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                            sharedPref.updateFontSize(0);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                            sharedPref.updateFontSize(1);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                            sharedPref.updateFontSize(2);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                            sharedPref.updateFontSize(3);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                            sharedPref.updateFontSize(4);
                        } else {
                            sharedPref.updateFontSize(2);
                        }
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        findViewById(R.id.btn_notification).setOnClickListener(v -> {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID);
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", BuildConfig.APPLICATION_ID);
                intent.putExtra("app_uid", getApplicationInfo().uid);
            }
            startActivity(intent);
        });

        txt_cache_size = findViewById(R.id.txt_cache_size);
        initializeCache();

        findViewById(R.id.btn_clear_cache).setOnClickListener(v -> clearCache());

        findViewById(R.id.btn_clear_search_history).setOnClickListener(view -> {
            AdapterSearch adapterSearch = new AdapterSearch(this);
            if (adapterSearch.getItemCount() > 0) {
                adapterSearch.clearSearchHistory();
                new Handler().postDelayed(() -> {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_search_history_cleared), Snackbar.LENGTH_SHORT).show();
                }, 200);
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_search_history_empty), Snackbar.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
            intent.putExtra("title", getString(R.string.title_settings_privacy));
            intent.putExtra("url", sharedPref.getPrivacyPolicyUrl());
            startActivity(intent);
        });

        findViewById(R.id.btn_terms_conditions).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
            intent.putExtra("title", getString(R.string.title_settings_terms));
            intent.putExtra("url", sharedPref.getTermsConditionsUrl());
            startActivity(intent);
        });

        findViewById(R.id.btn_publisher_info).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
            intent.putExtra("title", getString(R.string.title_settings_publisher_info));
            intent.putExtra("url", sharedPref.getPublisherInfoUrl());
            startActivity(intent);
        });

        findViewById(R.id.btn_rate).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID))));

        findViewById(R.id.btn_share).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            startActivity(intent);
        });

        findViewById(R.id.btn_more_apps).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPref.getMoreAppsUrl()))));

        findViewById(R.id.btn_about).setOnClickListener(v -> {
            AlertDialog alertDialog;
            if (sharedPref.getIsDarkTheme()) {
                alertDialog = new MaterialAlertDialogBuilder(this, R.style.RoundedMaterialAlertDialogDark)
                        .setView(R.layout.dialog_about)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                alertDialog = new MaterialAlertDialogBuilder(this, R.style.RoundedMaterialAlertDialogLight)
                        .setView(R.layout.dialog_about)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
            TextView txtAppVersion = alertDialog.findViewById(R.id.txt_app_version);
            txtAppVersion.setText(getString(R.string.msg_about_version) + " " + BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")");

        });

    }

    private void initToggleView() {
        nested_scroll_view = findViewById(R.id.nested_scroll_view);

        btn_arrow_general = findViewById(R.id.btn_arrow_general);
        btn_arrow_cache = findViewById(R.id.btn_arrow_cache);
        btn_arrow_privacy = findViewById(R.id.btn_arrow_privacy);
        btn_arrow_about = findViewById(R.id.btn_arrow_about);

        if (sharedPref.getIsDarkTheme()) {
            btn_arrow_general.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon));
            btn_arrow_cache.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon));
            btn_arrow_privacy.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon));
            btn_arrow_about.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon));
        } else {
            btn_arrow_general.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon));
            btn_arrow_cache.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon));
            btn_arrow_privacy.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon));
            btn_arrow_about.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon));
        }

        btn_toggle_general = findViewById(R.id.btn_toggle_general);
        btn_toggle_cache = findViewById(R.id.btn_toggle_cache);
        btn_toggle_privacy = findViewById(R.id.btn_toggle_privacy);
        btn_toggle_about = findViewById(R.id.btn_toggle_about);

        lyt_expand_general = findViewById(R.id.lyt_expand_general);
        lyt_expand_cache = findViewById(R.id.lyt_expand_cache);
        lyt_expand_privacy = findViewById(R.id.lyt_expand_privacy);
        lyt_expand_about = findViewById(R.id.lyt_expand_about);

        btn_toggle_general.setOnClickListener(view -> {
            toggleSection(btn_toggle_general, lyt_expand_general, btn_arrow_general);
        });

        btn_toggle_cache.setOnClickListener(view -> {
            toggleSection(btn_toggle_cache, lyt_expand_cache, btn_arrow_cache);
        });

        btn_toggle_privacy.setOnClickListener(view -> {
            toggleSection(btn_toggle_privacy, lyt_expand_privacy, btn_arrow_privacy);
        });

        btn_toggle_about.setOnClickListener(view -> {
            toggleSection(btn_toggle_about, lyt_expand_about, btn_arrow_about);
        });

        btn_user_name = findViewById(R.id.btn_user_name);
        btn_user_name.setOnClickListener(view -> showDialogUpdateData("Update name", "name"));
        txt_user_name = findViewById(R.id.txt_user_name);
        if (sharedPref.getAuthorName().equals("")) {
            txt_user_name.setText(getString(R.string.txt_no_fill));
        } else {
            txt_user_name.setText(sharedPref.getAuthorName());
        }


        btn_user_email = findViewById(R.id.btn_user_email);
        btn_user_email.setOnClickListener(view -> showDialogUpdateData("Update email", "email"));
        txt_user_email = findViewById(R.id.txt_user_email);
        if (sharedPref.getAuthorEmail().equals("")) {
            txt_user_email.setText(getString(R.string.txt_no_fill));
        } else {
            txt_user_email.setText(sharedPref.getAuthorEmail());
        }

        if (!sharedPref.getIsEnableCommentFeature()) {
            btn_user_name.setVisibility(View.GONE);
            btn_user_email.setVisibility(View.GONE);
        } else {
            if (!sharedPref.getIsWpRestV2Enabled()) {
                btn_user_name.setVisibility(View.GONE);
                btn_user_email.setVisibility(View.GONE);
            }
        }

    }

    TextInputEditText editText;
    private void showDialogUpdateData(String title, String type) {
        AlertDialog alertDialog;
        if (sharedPref.getIsDarkTheme()) {
            alertDialog = new MaterialAlertDialogBuilder(this, R.style.RoundedMaterialAlertDialogDark)
                    .setView(R.layout.dialog_update)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> {
                        String data = editText.getText().toString();
                        if (!data.equals("")) {
                            if (type.equals("name")) {
                                txt_user_name.setText(data);
                                sharedPref.setAuthorName(data);
                                showSnackBar("Name updated");
                                dialog.dismiss();
                            } else if (type.equals("email")) {
                                txt_user_email.setText(data);
                                sharedPref.setAuthorEmail(data);
                                showSnackBar("Email updated");
                                dialog.dismiss();
                            }
                        } else {
                            showSnackBar("Data cannot be empty!");
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.dialog_option_cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            alertDialog = new MaterialAlertDialogBuilder(this, R.style.RoundedMaterialAlertDialogLight)
                    .setView(R.layout.dialog_update)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> {
                        String data = editText.getText().toString();
                        if (!data.equals("")) {
                            if (type.equals("name")) {
                                txt_user_name.setText(data);
                                sharedPref.setAuthorName(data);
                                showSnackBar("Name updated");
                                dialog.dismiss();
                            } else if (type.equals("email")) {
                                txt_user_email.setText(data);
                                sharedPref.setAuthorEmail(data);
                                showSnackBar("Email updated");
                                dialog.dismiss();
                            }
                        } else {
                            showSnackBar("Data cannot be empty!");
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.dialog_option_cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        }

        TextInputLayout textInputLayout = alertDialog.findViewById(R.id.text_input_layout);

        TextView dialogTitle = alertDialog.findViewById(R.id.dialog_title);
        assert dialogTitle != null;
        dialogTitle.setText(title);

        editText = alertDialog.findViewById(R.id.edt_name);
        assert editText != null;
        assert textInputLayout != null;
        if (type.equals("name")) {
            editText.setText(sharedPref.getAuthorName());
            textInputLayout.setHint("Name");
        } else if (type.equals("email")) {
            editText.setText(sharedPref.getAuthorEmail());
            textInputLayout.setHint("Email");
        }
        editText.requestFocus();

        ImageView btn_close = alertDialog.findViewById(R.id.btn_close);
        assert btn_close != null;
        btn_close.setOnClickListener(view -> alertDialog.dismiss());

    }

    private void toggleSection(View view, View layout, ImageButton imageButton) {
        boolean show = toggleArrow(view, imageButton);
        viewAnimation(show, layout);
    }

    public boolean toggleArrow(View view, ImageButton imageButton) {
        if (view.getRotation() == 0) {
            view.animate().setDuration(100).rotation((float) 0.001);
            imageButton.setImageResource(R.drawable.ic_arrow_expand);
            return true;
        } else {
            view.animate().setDuration(100).rotation(0);
            imageButton.setImageResource(R.drawable.ic_arrow_collapse);
            return false;
        }
    }

    public void viewAnimation(boolean show, View view) {
        if (show) {
            ViewAnimation.expand(view, () -> Tools.nestedScrollTo(nested_scroll_view, view));
        } else {
            ViewAnimation.collapse(view);
        }
    }

    private void clearCache() {
        FileUtils.deleteQuietly(getCacheDir());
        FileUtils.deleteQuietly(getExternalCacheDir());
        txt_cache_size.setText(getString(R.string.sub_settings_clear_cache_start) + " 0 Bytes " + getString(R.string.sub_settings_clear_cache_end));
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_cache_cleared), Snackbar.LENGTH_SHORT).show();
    }

    private void initializeCache() {
        txt_cache_size.setText(getString(R.string.sub_settings_clear_cache_start) + " " + readableFileSize((0 + getDirSize(getCacheDir())) + getDirSize(getExternalCacheDir())) + " " + getString(R.string.sub_settings_clear_cache_end));
    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0 Bytes";
        }
        String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double d = (double) size;
        double pow = Math.pow(1024.0d, (double) digitGroups);
        Double.isNaN(d);
        stringBuilder.append(decimalFormat.format(d / pow));
        stringBuilder.append(" ");
        stringBuilder.append(units[digitGroups]);
        return stringBuilder.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

}
