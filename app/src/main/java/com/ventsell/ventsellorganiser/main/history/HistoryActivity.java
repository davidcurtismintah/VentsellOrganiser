/*
 * Copyright 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ventsell.ventsellorganiser.main.history;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.main.history.database.objects.HistoryItem;
import com.ventsell.ventsellorganiser.ticket.ScanResultFragment;

import java.util.ArrayList;

/**
 * The activity for interacting with the scan history.
 */
public final class HistoryActivity extends AppCompatActivity implements HistoryFragment.HistoryItemClickCallback {

    private static final String TAG = HistoryActivity.class.getSimpleName();

    private Toolbar toolbar;
    private CharSequence mOriginalTitle;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_history);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.history_content);
        if (fragment == null) {
            fragment = new HistoryFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.history_content, fragment, "HistoryFragment")
                    .commit();
        }

        mOriginalTitle = getTitle();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setTitle(mOriginalTitle);
    }

    @Override
    public void setTitle(String title) {
        toolbar.setTitle(mOriginalTitle + " (" + title + ')');
    }
}
