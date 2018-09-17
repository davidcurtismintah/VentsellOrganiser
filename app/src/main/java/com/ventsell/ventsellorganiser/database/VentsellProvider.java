/*
 * Copyright 2013 The Android Open Source Project
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

package com.ventsell.ventsellorganiser.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

public class VentsellProvider extends ContentProvider {
    EventsDatabase mDatabaseHelper;

    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = VentsellContract.CONTENT_AUTHORITY;

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /entries
     */
    public static final int ROUTE_ENTRIES = 1;

    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ROUTE_ENTRIES_ID = 2;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "events", ROUTE_ENTRIES);
        sUriMatcher.addURI(AUTHORITY, "events/*", ROUTE_ENTRIES_ID);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new EventsDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ENTRIES:
                return VentsellContract.Event.CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return VentsellContract.Event.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_ENTRIES_ID:
                // Return a single event, by ID.
                String id = uri.getLastPathSegment();
                builder.where(VentsellContract.Event._ID + "=?", id);
            case ROUTE_ENTRIES:
                // Return all known entries.
                builder.table(VentsellContract.Event.TABLE_NAME)
                       .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new event into the database.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_ENTRIES:
                long id = db.insertOrThrow(VentsellContract.Event.TABLE_NAME, null, values);
                result = Uri.parse(VentsellContract.Event.CONTENT_URI + "/" + id);
                break;
            case ROUTE_ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an event in database by URI.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(VentsellContract.Event.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(VentsellContract.Event.TABLE_NAME)
                       .where(VentsellContract.Event._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an event in the database by URI.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(VentsellContract.Event.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(VentsellContract.Event.TABLE_NAME)
                        .where(VentsellContract.Event._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * SQLite backend for @{link VentsellProvider}.
     *
     * Provides access to an disk-backed, SQLite datastore which is utilized by VentsellProvider. This
     * database should never be accessed by other parts of the application directly.
     */
    static class EventsDatabase extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 1;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "ventsell.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";
        /** SQL statement to create "event" table. */
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + VentsellContract.Event.TABLE_NAME + " (" +
                        VentsellContract.Event._ID + " INTEGER PRIMARY KEY," +
                        VentsellContract.Event.COLUMN_NAME_EVENT_ID + TYPE_INTEGER + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_TYPE    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_CATEGORY    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_LOCATION    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_LONGITUDE    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_LATITUDE    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_DESCRIPTION    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_START_DATE    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_START_TIME    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_END_DATE    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_END_TIME    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_TICKETS    + TYPE_INTEGER + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_ORGANIZER_NAME    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_ORGANIZER_EMAIL    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_ORGANIZER_CONTACT    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_ORGANIZER_CONTACT1    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_ORGANIZER_CONTACT2    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_EVENT_BANNER    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_AVA_TICKETS    + TYPE_INTEGER + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_SEL_TICKETS    + TYPE_INTEGER + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_ORGANIZER_ID    + TYPE_INTEGER + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_PAGE_VIEWS    + TYPE_INTEGER + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_BLURB    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_ABOUT    + TYPE_TEXT + COMMA_SEP +
                        VentsellContract.Event.COLUMN_NAME_ESV_V    + TYPE_TEXT + ")";

        /** SQL statement to drop "event" table. */
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + VentsellContract.Event.TABLE_NAME;

        public EventsDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // simply to discard the data and start over
            Log.w(EventsDatabase.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}
