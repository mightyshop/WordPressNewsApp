package com.app.wordpressnewsapps.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.wordpressnewsapps.R;
import com.app.wordpressnewsapps.database.prefs.SharedPref;
import com.app.wordpressnewsapps.model.PostComment;
import com.app.wordpressnewsapps.rest.ApiInterface;
import com.app.wordpressnewsapps.rest.RestAdapter;
import com.app.wordpressnewsapps.util.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** @noinspection deprecation*/
public class ActivityCommentSend extends AppCompatActivity {

    int post;
    int parent;
    String authorName;
    EditText edtName;
    EditText edtEmail;
    EditText edtPost;
    EditText edtParent;
    EditText edtContent;
    SharedPref sharedPref;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_comment_send);
        Tools.setNavigation(this);

        sharedPref = new SharedPref(this);
        progressDialog = new ProgressDialog(this);

        if (getIntent() != null) {
            post = getIntent().getIntExtra("post_id", 0);
            parent = getIntent().getIntExtra("parent", 0);
            authorName = getIntent().getStringExtra("reply");
        }
        initView();
        setupToolbar();
    }

    private void initView() {
        edtName = findViewById(R.id.edt_name);
        edtEmail = findViewById(R.id.edt_email);
        edtPost = findViewById(R.id.edt_post);
        edtParent = findViewById(R.id.edt_parent);
        edtContent = findViewById(R.id.edt_content);

        edtName.setText(sharedPref.getAuthorName());
        edtEmail.setText(sharedPref.getAuthorEmail());
        edtPost.setText("" + post);
        edtParent.setText("" + parent);

        if (!authorName.equals("")) {
            edtContent.setText("@" + authorName + " ");
            edtContent.requestFocus();
        } else {
            edtContent.setText("");
        }
    }

    private void postComment() {

        String name = edtName.getText().toString();
        String email = edtEmail.getText().toString();
        String content = edtContent.getText().toString();
        int postId = Integer.parseInt(edtPost.getText().toString());
        int parent = Integer.parseInt(edtParent.getText().toString());

        if (!name.equals("") && !email.equals("") && !content.equals("")) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.msg_send_confirm))
                    .setPositiveButton(getString(R.string.dialog_option_ok), (dialog, which) -> {
                        showProgress();
                        ApiInterface apiInterface = RestAdapter.createAPI("default", sharedPref.getSiteUrl());
                        Call<PostComment> call = apiInterface.postComment(name, email, content, postId, parent);
                        call.enqueue(new Callback<PostComment>() {
                            @Override
                            public void onResponse(@NonNull Call<PostComment> call, @NonNull Response<PostComment> response) {
                                Tools.postDelayed(() -> {
                                    showDialog(getString(R.string.title_comment_sent), getString(R.string.msg_comment_sent));
                                    sharedPref.setAuthorName(name);
                                    sharedPref.setAuthorEmail(email);
                                    hideProgress();
                                }, 1000);
                            }

                            @Override
                            public void onFailure(@NonNull Call<PostComment> call, @NonNull Throwable t) {
                                Tools.postDelayed(() -> {
                                    showDialog("", getString(R.string.msg_comment_failed));
                                    hideProgress();
                                }, 1000);
                            }
                        });
                    })
                    .setNegativeButton(getString(R.string.dialog_option_cancel), null)
                    .show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_fill_comment), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void setupToolbar() {
        Tools.setupToolbar(this, findViewById(R.id.appbar_layout), findViewById(R.id.toolbar), getString(R.string.title_post_comment), true);
    }

    private void showDialog(String title, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.dialog_option_ok), (dialogInterface, i) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Tools.postDelayed(this::onBackPressed, 300);
            return true;
        } else if (menuItem.getItemId() == R.id.post_comment) {
            postComment();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void showProgress() {
        progressDialog.setMessage(getString(R.string.msg_send_comment));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgress() {
        progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
