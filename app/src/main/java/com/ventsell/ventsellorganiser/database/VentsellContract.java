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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Field and table name constants for
 * {@link VentsellProvider}.
 */
public class VentsellContract {
    private VentsellContract() {
    }

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.ventsell.ventsellorganiser.provider";

    /**
     * Base URI. (content://com.ventsell.ventsellorganiser.provider)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "event"-type resources.
     */
    private static final String PATH_EVENTS = "events";

    /**
     * Columns supported by "events" records.
     */
    public static class Event implements BaseColumns {
        /**
         * MIME type for lists of events.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ventsell.provider.events";
        /**
         * MIME type for individual events.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ventsell.provider.event";

        /**
         * Fully qualified URI for "event" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        /**
         * Table name where records are stored for "event" resources.
         */
        public static final String TABLE_NAME = "event";
        /**
         * CrowdFunding ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_EVENT_ID = "event_id";
        /**
         * CrowdFunding title.
         */
        public static final String COLUMN_NAME_TITLE = "title";
        /**
         * CrowdFunding type.
         */
        public static final String COLUMN_NAME_TYPE = "type";
        /**
         * CrowdFunding category.
         */
        public static final String COLUMN_NAME_CATEGORY = "category";
        /**
         * CrowdFunding location.
         */
        public static final String COLUMN_NAME_LOCATION = "location";
        /**
         * CrowdFunding longitude.
         */
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        /**
         * CrowdFunding latitude.
         */
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        /**
         * CrowdFunding description.
         */
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        /**
         * CrowdFunding start_date.
         */
        public static final String COLUMN_NAME_START_DATE = "start_date";
        /**
         * CrowdFunding start_time.
         */
        public static final String COLUMN_NAME_START_TIME = "start_time";
        /**
         * CrowdFunding end_date.
         */
        public static final String COLUMN_NAME_END_DATE = "end_date";
        /**
         * CrowdFunding end_time.
         */
        public static final String COLUMN_NAME_END_TIME = "end_time";
        /**
         * CrowdFunding tickets.
         */
        public static final String COLUMN_NAME_TICKETS = "tickets";
        /**
         * CrowdFunding organizer_name.
         */
        public static final String COLUMN_NAME_ORGANIZER_NAME = "organizer_name";
        /**
         * CrowdFunding organizer_email.
         */
        public static final String COLUMN_NAME_ORGANIZER_EMAIL = "organizer_email";
        /**
         * CrowdFunding organizer_contact.
         */
        public static final String COLUMN_NAME_ORGANIZER_CONTACT = "organizer_contact";
        /**
         * CrowdFunding organizer_contact1.
         */
        public static final String COLUMN_NAME_ORGANIZER_CONTACT1 = "organizer_contact1";
        /**
         * CrowdFunding organizer_contact2.
         */
        public static final String COLUMN_NAME_ORGANIZER_CONTACT2 = "organizer_contact2";
        /**
         * CrowdFunding event_banner.
         */
        public static final String COLUMN_NAME_EVENT_BANNER = "event_banner";
        /**
         * CrowdFunding ava_tickets.
         */
        public static final String COLUMN_NAME_AVA_TICKETS = "ava_tickets";
        /**
         * CrowdFunding sel_tickets.
         */
        public static final String COLUMN_NAME_SEL_TICKETS = "sel_tickets";
        /**
         * CrowdFunding organiser_id.
         */
        public static final String COLUMN_NAME_ORGANIZER_ID = "organiser_id";
        /**
         * CrowdFunding page_views.
         */
        public static final String COLUMN_NAME_PAGE_VIEWS = "page_views";
        /**
         * CrowdFunding blurb.
         */
        public static final String COLUMN_NAME_BLURB = "blurb";
        /**
         * CrowdFunding about.
         */
        public static final String COLUMN_NAME_ABOUT = "about";
        /**
         * CrowdFunding esv_v.
         */
        public static final String COLUMN_NAME_ESV_V = "esv_v";
    }
}