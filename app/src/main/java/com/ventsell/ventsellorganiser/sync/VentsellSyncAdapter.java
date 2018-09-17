/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ventsell.ventsellorganiser.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

import com.ventsell.ventsellorganiser.AppController;
import com.ventsell.ventsellorganiser.database.VentsellContract;
import com.ventsell.ventsellorganiser.database.objects.EventObject;
import com.ventsell.ventsellorganiser.database.objects.VentsellError;

import java.util.ArrayList;
import java.util.List;

/**
 * VentsellSyncAdapter implementation for syncing sample VentsellSyncAdapter contacts to the
 * platform ContactOperations provider.  This sample events a basic 2-way
 * sync between the client and a sample server.  It also contains an
 * example of how to update the contacts' status messages, which
 * would be useful for a messaging or social networking client.
 */
public class VentsellSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "VentsellSyncAdapter";

    public VentsellSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              final ContentProviderClient provider, final SyncResult syncResult) {

        // NOTE onPerformSync will not be called when network is unavailable

        // obtain events from server and sync with local
        // Building a print of the extras we got
        StringBuilder sb = new StringBuilder();
        if (extras != null) {
            for (String key : extras.keySet()) {
                sb.append(key).append("[").append(extras.get(key)).append("] ");
            }
        }

        Log.i("ventsell", TAG + "> onPerformSync for account[" + account.name + "]. Extras: "+sb.toString());

        VentsellEventsAccessor.EventsAccessorResponseCallBack callBack = new VentsellEventsAccessor.EventsAccessorResponseCallBack() {
            @Override
            public void onObtainEvents(List<EventObject.Event> remoteEvents) {

                Log.i("ventsell", TAG + "> remote Events" + remoteEvents);

                // obtain events from assets and sync with local
                try {
                    // Get events from remote
                    Log.i("ventsell", TAG + "> Get remote Events complete. Found " + remoteEvents.size() + " events");

                    ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                    SparseArray<EventObject.Event> eventMap = new SparseArray<>(remoteEvents.size());
                    for (EventObject.Event e : remoteEvents) {
                        Log.i("ventsell", TAG + "> ------");
                        eventMap.put(e.getId(), e);
                        Log.i("ventsell", TAG + "> to events map");
                    }

                    Log.i("ventsell", TAG + "> Get local Events");
                    // Get events from local
                    Cursor curEvents = provider.query(VentsellContract.Event.CONTENT_URI, null, null, null, null);
                    Log.i("ventsell", TAG + "> Found " + (curEvents != null && curEvents.getCount() != 0 ? curEvents.getCount() + " local events. Computing merge solution..." : "no local events."));

                    Log.i("ventsell", TAG + "> curEvents " + (curEvents != null));
                    if (curEvents != null) {
                        // Find stale data
                        while (curEvents.moveToNext()) {
                            int primaryKey = curEvents.getInt(curEvents.getColumnIndex(VentsellContract.Event._ID));
                            Uri checkedUri = VentsellContract.Event.CONTENT_URI.buildUpon()
                                    .appendPath(Integer.toString(primaryKey)).build();
                            syncResult.stats.numEntries++;
                            EventObject.Event localEvent = EventObject.Event.fromCursor(curEvents);
                            Log.i("ventsell", TAG + "> local Event "+localEvent);
                            EventObject.Event match = eventMap.get(localEvent.getId());
                            if (match != null) {
                                // CrowdFunding exists. Remove from event map to prevent insert later.
                                eventMap.remove(localEvent.getId());
                                // Check to see if the event needs to be updated
                                if (!match.equals(localEvent)) {
                                    // Update existing record
                                    Log.i("ventsell", TAG +"> Scheduling update: " + checkedUri);
                                    batch.add(ContentProviderOperation.newUpdate(checkedUri)
                                            .withValues(match.getContentValues())
                                            .build());
                                    syncResult.stats.numUpdates++;
                                } else {
                                    Log.i("ventsell", TAG +"> No action: " + checkedUri);
                                }
                            } else {
                                // CrowdFunding doesn't exist. Remove it from the database.
                                Log.i("ventsell", TAG +"> Scheduling delete: " + checkedUri);
                                batch.add(ContentProviderOperation.newDelete(checkedUri).build());
                                syncResult.stats.numDeletes++;
                            }
                        }
                        curEvents.close();
                        Log.i("ventsell", TAG + "> closed");
                    }
                    // Add new items
                    for (int i = 0; i < eventMap.size() ; i++) {
                        Log.i("ventsell", TAG +"> Scheduling insert: event_id=" + eventMap.valueAt(i));
                        batch.add(ContentProviderOperation.newInsert(VentsellContract.Event.CONTENT_URI)
                                .withValues(eventMap.valueAt(i).getContentValues())
                                .build());
                        syncResult.stats.numInserts++;
                    }
                    Log.i("ventsell", TAG +"> Merge solution ready. Applying batch update");
                    provider.applyBatch(batch);

                } catch (RemoteException | OperationApplicationException e) {
                    Log.e("ventsell", TAG +"> Error updating database: " + e);
                    syncResult.databaseError = true;
                }
            }

            @Override
            public void onError(VentsellError error) {
                Log.e("ventsell", TAG +"> Error obtaining events: " + error);
                syncResult.stats.numParseExceptions++;
            }
        };

        Log.i("ventsell", TAG + "> Get remote Events");
        new VentsellEventsAccessor().obtainEvents(AppController.getInstance().getCurrentUser().getId(), callBack);
    }
}

