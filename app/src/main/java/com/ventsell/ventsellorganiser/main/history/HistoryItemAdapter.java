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

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.ticket.ScanResultFragment;
import com.ventsell.ventsellorganiser.main.history.database.objects.HistoryItem;
import com.ventsell.ventsellorganiser.ticket.objects.Result;

import java.util.List;

import static android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

final class HistoryItemAdapter extends RecyclerView.Adapter<HistoryItemAdapter.ViewHolder> {

    private static final String TAG = HistoryItemAdapter.class.getSimpleName();

    static class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout historyItemView;
        private TextView titleTextView;
        private TextView timestampTextView;
        private TextView detailsTextView;
        private int position;

        ViewHolder(View itemView, final List<HistoryItem> items, final AppCompatActivity activity, final HistoryItemAdapter historyItemAdapter) {
            super(itemView);
            historyItemView = (RelativeLayout) itemView.findViewById(R.id.history_list_relative_layout);
            historyItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ventsell", TAG + " item click");
                    int adapterPosition = getAdapterPosition();
                    final HistoryItem item = items.get(adapterPosition);
                    final Result result = item.getResult();
                    if (result != null) {
                        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.history_content);
                        if (fragment == null) {
                            fragment = ScanResultFragment.newInstance(adapterPosition);
                            activity.getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.history_content, fragment, "ScanResultFragment")
                                    .addToBackStack("ScanResultFragmentBackStack")
                                    .commit();
                        }
                    }
                }
            });
            historyItemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    setPosition(getAdapterPosition());
                    return false;
                }
            });
            historyItemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    int position = getItemPosition();
                    if (position >= historyItemAdapter.getItemCount() || items.get(position).getResult() != null) {
                        menu.add(Menu.NONE, position, position, R.string.history_clear_one_history_text);
                    }
                }
            });
//            historyItemView.setClickable(true);
            titleTextView = (TextView) historyItemView.findViewById(R.id.history_title);
            timestampTextView = (TextView) historyItemView.findViewById(R.id.history_timestamp);
            detailsTextView = (TextView) historyItemView.findViewById(R.id.history_detail);
        }

        void setPosition(int position) {
            this.position = position;
        }

        int getItemPosition() {
            return position;
        }
    }

    private List<HistoryItem> mItems;
    private AppCompatActivity mActivity;

    HistoryItemAdapter(List<HistoryItem> dataSet, AppCompatActivity activity) {
        mItems = dataSet;
        mActivity = activity;
    }

    @Override
    public HistoryItemAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_history, viewGroup, false);
        return new HistoryItemAdapter.ViewHolder(v, mItems, mActivity, this);
    }

    @Override
    public void onBindViewHolder(final HistoryItemAdapter.ViewHolder viewHolder, int i) {
        final HistoryItem item = mItems.get(i);
        final Result result = item.getResult();

        CharSequence title = result.input.ticketId;
        long timestamp = result.timestamp;
        CharSequence details = item.getDisplayAndDetails();

        viewHolder.titleTextView.setText(title);
        viewHolder.timestampTextView.setText(DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), MINUTE_IN_MILLIS, FORMAT_ABBREV_RELATIVE));
        viewHolder.detailsTextView.setText(details);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }
}
