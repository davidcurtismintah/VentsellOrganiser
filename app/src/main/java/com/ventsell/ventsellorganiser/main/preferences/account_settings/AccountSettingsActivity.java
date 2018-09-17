package com.ventsell.ventsellorganiser.main.preferences.account_settings;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;

import com.android.volley.toolbox.NetworkImageView;
import com.ventsell.ventsellorganiser.AppController;
import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.utils.UserImageHelper;

public class AccountSettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private NetworkImageView userImageView;
    private AppCompatTextView fullNameTextView;
    private AppCompatTextView emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        findViews();

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        updateUi();
    }

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        userImageView = (NetworkImageView) findViewById(R.id.user_image_view);
        fullNameTextView = (AppCompatTextView) findViewById(R.id.full_name);
        emailTextView = (AppCompatTextView) findViewById(R.id.email);
    }

    private void updateUi() {
        String imageUrl = AppController.getInstance().getCurrentUser().getImage();
        UserImageHelper.setUserImage(userImageView, imageUrl);

        fullNameTextView.setText(AppController.getInstance().getCurrentUser().getFullName());
        emailTextView.setText(AppController.getInstance().getCurrentUser().getEmail());
    }

}
