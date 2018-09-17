package com.ventsell.ventsellorganiser.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.ventsell.ventsellorganiser.AppController;
import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.authenticate.AuthenticateActivity;
import com.ventsell.ventsellorganiser.main.history.HistoryActivity;
import com.ventsell.ventsellorganiser.main.history.HistoryManager;
import com.ventsell.ventsellorganiser.main.history.database.objects.HistoryItem;
import com.ventsell.ventsellorganiser.main.preferences.account_settings.AccountSettingsActivity;
import com.ventsell.ventsellorganiser.main.events.EventsListFragment;
import com.ventsell.ventsellorganiser.main.events.EventsRootFragment;
import com.ventsell.ventsellorganiser.utils.Constants;
import com.ventsell.ventsellorganiser.utils.UserImageHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int NAVIGATION_ITEM_INDEX_HOME = 0;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!AppController.getInstance().isLoggedIn()) {
            Intent startIntent = new Intent(this, AuthenticateActivity.class);
            startIntent.putExtra(AuthenticateActivity.EXTRA_ACTION, AuthenticateActivity.Action.LOGIN);
            startActivity(startIntent);
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SlidePagerAdapter adapter = new SlidePagerAdapter(this, getSupportFragmentManager());
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch(tab.getPosition()) {
                    case 0:
                        scrollToTop();
                        break;
                }
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        View headerView = mNavigationView.getHeaderView(0);
        NetworkImageView userImageView = (NetworkImageView) headerView.findViewById(R.id.user_image_view);
        String imageUrl = AppController.getInstance().getCurrentUser().getImage();
        UserImageHelper.setUserImage(userImageView, imageUrl);
        AppCompatTextView userNameTextView = (AppCompatTextView) headerView.findViewById(R.id.userName);
        userNameTextView.setText(AppController.getInstance().getCurrentUser().getFullName());
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @TargetApi(21)
            @SuppressWarnings("deprecation")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.navigation_item_home:
                        List<Fragment> pagerFragments = getSupportFragmentManager().getFragments();
                        for (Fragment pagerFragment : pagerFragments) {
                            if (pagerFragment instanceof EventsRootFragment) {
                                pagerFragment.getChildFragmentManager().popBackStackImmediate("InsightsFragmentBackStack", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                setNavigationHomeSelected(true);
                                break;
                            }
                        }
                        break;
                    case R.id.navigation_item_history:
                        intent = new Intent(Intent.ACTION_VIEW);
                        if (Build.VERSION.SDK_INT >= 21) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                        } else {
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        }
                        intent.setClassName(MainActivity.this, HistoryActivity.class.getName());
                        startActivity(intent);
                        break;
//                    case R.id.navigation_item_scan_settings:
//                        intent = new Intent(MainActivity.this, PreferencesActivity.class);
//                        startActivity(intent);
//                        break;
                    case R.id.navigation_item_account_settings:
                        intent = new Intent(MainActivity.this, AccountSettingsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.navigation_item_logout:
                        intent = new Intent(MainActivity.this, AuthenticateActivity.class);
                        intent.putExtra(AuthenticateActivity.EXTRA_ACTION, AuthenticateActivity.Action.LOGOUT);
                        startActivity(intent);
                        finish();
                    default:
                        break;

                }
                return true;
            }
        });


    }

    private void scrollToTop() {
        List<Fragment> pagerFragments = getSupportFragmentManager().getFragments();

        for (Fragment pagerFrag : pagerFragments) {
            if (pagerFrag != null && pagerFrag.getUserVisibleHint()) {
                if (pagerFrag instanceof EventsListFragment) {
                    RecyclerView recyclerView = ((EventsListFragment) pagerFrag).getRecyclerView();
                    if(recyclerView != null && recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0)
                        recyclerView.smoothScrollToPosition(0);
                }
            }
        }
    }

    public void setNavigationHomeSelected(boolean selected){
        mNavigationView.getMenu().getItem(NAVIGATION_ITEM_INDEX_HOME).setChecked(selected);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            /*case R.id.menu_refresh:
                SyncUtils.TriggerRefresh();
                return true;*/
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        List<Fragment> pagerFragments = getSupportFragmentManager().getFragments();

        boolean handled = false;
        for (Fragment pagerFrag : pagerFragments) {
            if (pagerFrag != null && pagerFrag.getUserVisibleHint()) {
                if (pagerFrag instanceof EventsRootFragment) {
                    handled = ((EventsRootFragment) pagerFrag).onBackPressed();
                }

                if (handled) {
                    break;
                }

            }
        }

        if (!handled) {
            super.onBackPressed();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AppController.getInstance().cancelPendingRequests();
    }

    //-----------------------------------------------------------------------------------

    private class SlidePagerAdapter extends FragmentPagerAdapter {

        static final int NUM_ITEMS = 1;

        private Context context;

        SlidePagerAdapter(Context c, FragmentManager fm) {
            super(fm);
            this.context = c;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new EventsRootFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return context.getString(R.string.events_tab_title_text);
                default:
                    return "";
            }
        }
    }

}
