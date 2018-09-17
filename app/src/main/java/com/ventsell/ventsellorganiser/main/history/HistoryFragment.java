package com.ventsell.ventsellorganiser.main.history;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.main.history.database.objects.HistoryItem;
import com.ventsell.ventsellorganiser.ticket.objects.Result;
import com.ventsell.ventsellorganiser.utils.Constants;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private static final String TAG = HistoryFragment.class.getSimpleName();
    private AppCompatTextView empty;
    private ArrayList<HistoryItem> mDataSet = new ArrayList<>();
    private HistoryItemAdapter mAdapter;
    private RecyclerView myRecyclerView;

    private HistoryManager mHistoryManager;

    public interface HistoryItemClickCallback{
        void setTitle(String title);
    }

    private HistoryItemClickCallback mHistoryItemClickCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mHistoryItemClickCallback == null){
            mHistoryItemClickCallback = (HistoryItemClickCallback) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);
        empty = (AppCompatTextView) view.findViewById(R.id.empty);

        myRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new HistoryItemAdapter(mDataSet, (AppCompatActivity) getActivity());
        myRecyclerView.setAdapter(mAdapter);
        registerForContextMenu(myRecyclerView);

        mHistoryManager = new HistoryManager(getActivity());
        mHistoryManager.trimHistory();

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadHistoryItems();
    }

    private void reloadHistoryItems() {
        Iterable<HistoryItem> items = mHistoryManager.buildHistoryItems();
        mDataSet.clear();
        for (HistoryItem item : items) {
            mDataSet.add(item);
        }
        mHistoryItemClickCallback.setTitle(String.valueOf(mAdapter.getItemCount()));
        if (mDataSet.isEmpty()) {
            myRecyclerView.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            empty.setText(getString(R.string.history_empty_text));
        } else {
            myRecyclerView.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mHistoryManager.hasHistoryItems()) {
            inflater.inflate(R.menu.history, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_history_send:
                sendHistory();
                break;
            case R.id.menu_history_clear_text:
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(R.string.msg_sure);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i2) {
                        mHistoryManager.clearHistory();
                        reloadHistoryItems();
                        dialog.dismiss();
                        Snackbar.make(myRecyclerView, "History Cleared", Snackbar.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.button_cancel, null);
                builder.show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = item.getItemId();
        mHistoryManager.deleteHistoryItem(position);
        reloadHistoryItems();
        return true;
    }

    private void sendHistory() {
        CharSequence history = mHistoryManager.buildHistory();
        Parcelable historyFile = HistoryManager.saveHistory(history.toString());
        if (historyFile == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.msg_unmount_usb);
            builder.setPositiveButton(R.string.button_ok, null);
            builder.show();
        } else {
            Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            String subject = getResources().getString(R.string.history_email_title);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, subject);
            intent.putExtra(Intent.EXTRA_STREAM, historyFile);
            intent.setType("ticketId/csv");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException anfe) {
                Log.w(TAG, anfe.toString());
            }
        }
    }

}
