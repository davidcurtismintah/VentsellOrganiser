package com.ventsell.ventsellorganiser.main.events;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ventsell.ventsellorganiser.AppController;
import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.database.VentsellContract;
import com.ventsell.ventsellorganiser.main.MainActivity;
import com.ventsell.ventsellorganiser.sync.SyncUtils;
import com.ventsell.ventsellorganiser.sync.VentsellAccountAuthenticator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = EventsListFragment.class.getSimpleName();

    public static final String BUNDLE_RECYCLER_LAYOUT = "EventsListFragment.recycler.layout";

    private AppCompatTextView empty;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat dateFormat2 = new SimpleDateFormat("MMMM d'suffix' yyyy", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();
    private Date date;

    private Object mSyncObserverHandle;

    /**
     * Projection for querying the content provider.
     */
    private static final String[] PROJECTION = new String[]{
            VentsellContract.Event.COLUMN_NAME_EVENT_BANNER,
            VentsellContract.Event.COLUMN_NAME_TITLE,
            VentsellContract.Event.COLUMN_NAME_CATEGORY,
            VentsellContract.Event.COLUMN_NAME_BLURB,
            VentsellContract.Event.COLUMN_NAME_LOCATION,
            VentsellContract.Event.COLUMN_NAME_START_DATE,
            VentsellContract.Event.COLUMN_NAME_END_DATE,
            VentsellContract.Event.COLUMN_NAME_START_TIME,
            VentsellContract.Event.COLUMN_NAME_END_TIME,
            VentsellContract.Event.COLUMN_NAME_TICKETS
    };

    // Column indexes. The index of a column in the Cursor is the same as its relative position in
    // the projection.
    /**
     * Column index for event_banner
     */
    private static final int COLUMN_EVENT_BANNER = 0;
    /**
     * Column index for title
     */
    private static final int COLUMN_TITLE = 1;
    /**
     * Column index for category
     */
    private static final int COLUMN_CATEGORY = 2;
    /**
     * Column index for description
     */
    private static final int COLUMN_BLURB = 3;
    /**
     * Column index for location
     */
    private static final int COLUMN_LOCATION = 4;
    /**
     * Column index for start_date
     */
    private static final int COLUMN_START_DATE = 5;
    /**
     * Column index for end_date
     */
    private static final int COLUMN_END_DATE = 6;
    /**
     * Column index for start_time
     */
    private static final int COLUMN_START_TIME = 7;
    /**
     * Column index for end_time
     */
    private static final int COLUMN_END_TIME = 8;
    /**
     * Column index for tickets
     */
    private static final int COLUMN_TICKETS = 9;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events_list, container, false);

        empty = (AppCompatTextView) view.findViewById(R.id.empty);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncUtils.TriggerRefresh();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);

        Uri mUri = new Uri.Builder()
                .scheme("content://")
                .authority(VentsellContract.CONTENT_AUTHORITY)
                .path(VentsellContract.Event.TABLE_NAME)
                .build();
        TableObserver observer = new TableObserver(null);
        getActivity().getContentResolver().registerContentObserver(mUri, true, observer);

        SyncUtils.TriggerRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    /**
     * Create a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If a sync is active or pending, the Refresh is shown
     * by an indeterminate ProgressBar.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(final int which) {
            getActivity().runOnUiThread(new Runnable() {
                /**
                 * The VentsellSyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = VentsellAccountAuthenticator.GetAccount();

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, VentsellContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, VentsellContract.CONTENT_AUTHORITY);

                    Log.d("ventsell", TAG + "> syncActive: " + syncActive + " syncPending: " + syncPending);
                    if (syncActive || syncPending) {
                        swipeRefreshLayout.setRefreshing(true);
                        empty.setText(getString(R.string.loading_paid_events_text));
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                                empty.setText(getString(R.string.empty_events_text));
                            }
                        }, 500);
                    }
                    Log.d("ventsell", TAG + "> mSyncStatusObserver: " + which);
                }
            });
        }
    };

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }


    public class TableObserver extends ContentObserver {

        TableObserver(Handler handler) {
            super(handler);
        }

        /*
                 * Define a method that's called when data in the
                 * observed content provider changes.
                 * This method signature is provided for compatibility with
                 * older platforms.
                 */
        @Override
        public void onChange(boolean selfChange) {
            /*
             * Invoke the method signature available as of
             * Android platform version 4.1, with a null URI.
             */
            onChange(selfChange, null);
        }

        /*
         * Define a method that's called when data in the
         * observed content provider changes.
         */
        @Override
        public void onChange(boolean selfChange, Uri changeUri) {
            /*
             * Ask the framework to run your sync adapter.
             * To maintain backward compatibility, assume that
             * changeUri is null.
             */
            ContentResolver.requestSync(VentsellAccountAuthenticator.GetAccount(), VentsellContract.CONTENT_AUTHORITY, null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri queryUri = VentsellContract.Event.CONTENT_URI;
        return new CursorLoader(getActivity(), queryUri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);

            mSyncStatusObserver.onStatusChanged(0);

        } else {
            recyclerView.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        }
        recyclerAdapter.setEvents(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerAdapter.setEvents(null);
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RowController> {

        private Cursor events;

        @Override
        public RowController onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_event, viewGroup, false);
            return new RowController(v);
        }

        @Override
        public void onBindViewHolder(RowController rowController, int position) {
            events.moveToPosition(position);
            rowController.bindModel(events);
        }

        @Override
        public int getItemCount() {
            if (events == null) {
                return 0;
            }
            return events.getCount();
        }

        public void setEvents(Cursor events) {
            this.events = events;
            notifyDataSetChanged();
        }

        class RowController extends RecyclerView.ViewHolder {

            private NetworkImageView eventBanner;
            private TextView eventTitle;
            private TextView eventCategory;
            private TextView eventDescription;
            private TextView eventLocation;
            private TextView eventDate;
            private TextView eventTimeToGo;
            private TextView ticketAmount;
            private ProgressBar progressBar;

            RowController(View row) {
                super(row);
                row.findViewById(R.id.event_details_linear_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction trans = getFragmentManager().beginTransaction();
                        trans.replace(R.id.events_root_frame, new EventInsightsFragment());
                        trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        trans.addToBackStack("InsightsFragmentBackStack");
                        trans.commit();

                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null) {
                            activity.setNavigationHomeSelected(false);
                        }
                    }
                });
                row.setClickable(true);
                eventBanner = (NetworkImageView) row.findViewById(R.id.eventBanner);
                eventBanner.setDefaultImageResId(R.drawable.default_event_image);
                eventBanner.setErrorImageResId(R.drawable.default_event_image);

                eventTitle = (TextView) row.findViewById(R.id.eventTitle);
                eventCategory = (TextView) row.findViewById(R.id.eventCategory);
                eventDescription = (TextView) row.findViewById(R.id.eventDescription);
                eventLocation = (TextView) row.findViewById(R.id.eventLocation);
                eventDate = (TextView) row.findViewById(R.id.eventDate);
                eventTimeToGo = (TextView) row.findViewById(R.id.eventTimeToGo);
                ticketAmount = (TextView) row.findViewById(R.id.ticketAmount);
                progressBar = (ProgressBar) row.findViewById(R.id.progressBar);
                progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            }

            void bindModel(Cursor row) {
                eventBanner.setImageUrl(getString(R.string.ventsell_base_url) + row.getString(COLUMN_EVENT_BANNER), AppController.getInstance().getImageLoader());
                Log.d("ventsell", TAG + " Event banner > " + getString(R.string.ventsell_base_url) + row.getString(COLUMN_EVENT_BANNER));
                eventTitle.setText(row.getString(COLUMN_TITLE));
                eventCategory.setText(row.getString(COLUMN_CATEGORY));
                eventDescription.setText(row.getString(COLUMN_BLURB));
                eventLocation.setText(row.getString(COLUMN_LOCATION));

                try {
                    date = dateFormat.parse(row.getString(COLUMN_START_DATE));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date != null){
                    String dateString = dateFormat2.format(date);
                    calendar.setTime(date);
                    eventDate.setText(dateString.replace("suffix", getDayOfMonthSuffix(calendar.get(Calendar.DAY_OF_MONTH)))); //TODO verify what event date represents
                }

//                eventTimeToGo.setText(row.getString(COLUMN_END_DATE) + " at " + row.getString(COLUMN_END_TIME)); //TODO verify
                eventTimeToGo.setText(""); //TODO verify
                ticketAmount.setText(String.format(Locale.getDefault(), "GH¢ %d", row.getInt(COLUMN_TICKETS))); //TODO verify
                progressBar.setProgress(30); //TODO verify
            }

            String getDayOfMonthSuffix(final int n) {
                if (n < 1 || n > 31) {
                    throw new IllegalArgumentException("Illegal day of month");
                }

                if (n >= 11 && n <= 13) {
                    return "th";
                }

                switch (n % 10) {
                    case 1:  return "st";
                    case 2:  return "nd";
                    case 3:  return "rd";
                    default: return "th";
                }
            }
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }

        /*LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            if (savedInstanceState != null) {
                layoutManager.scrollToPosition(savedInstanceState.getInt(BUNDLE_RECYCLER_LAYOUT, 0));
                Toast.makeText(getActivity(), ""+savedInstanceState.getInt(BUNDLE_RECYCLER_LAYOUT, 0), Toast.LENGTH_SHORT).show();
            }
        }*/
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());

        /*LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null){
            outState.putInt(BUNDLE_RECYCLER_LAYOUT, layoutManager.findFirstVisibleItemPosition());
            Toast.makeText(getActivity(), ""+layoutManager.findFirstVisibleItemPosition(), Toast.LENGTH_SHORT).show();
        }*/
    }
}
