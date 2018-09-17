/*
 * Copyright 2013 Google Inc.
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

package com.ventsell.ventsellorganiser.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/** Service to handle sync requests.
 *
 * <p>This service is invoked in response to Intents with action android.content.VentsellSyncAdapter, and
 * returns a Binder connection to VentsellSyncAdapter.
 *
 * <p>For performance, only one sync adapter will be initialized within this application's context.
 *
 * <p>Note: The SyncService itself is not notified when a new sync occurs. It's role is to
 * manage the lifecycle of our {@link VentsellSyncAdapter} and provide a handle to said VentsellSyncAdapter to the
 * OS on request.
 */
public class VentsellSyncService extends Service {
    private static final String TAG = "SyncService";

    private static final Object sSyncAdapterLock = new Object();
    private static VentsellSyncAdapter sVentsellSyncAdapter = null;

    /**
     * Thread-safe constructor, creates static {@link VentsellSyncAdapter} instance.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        synchronized (sSyncAdapterLock) {
            if (sVentsellSyncAdapter == null) {
                sVentsellSyncAdapter = new VentsellSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    /**
     * Logging-only destructor.
     */
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    /**
     * Return Binder handle for IPC communication with {@link VentsellSyncAdapter}.
     *
     * <p>New sync requests will be sent directly to the VentsellSyncAdapter using this channel.
     *
     * @param intent Calling intent
     * @return Binder handle for {@link VentsellSyncAdapter}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sVentsellSyncAdapter.getSyncAdapterBinder();
    }
}
