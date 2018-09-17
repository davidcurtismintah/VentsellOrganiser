package com.ventsell.ventsellorganiser.database.objects;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.ventsell.ventsellorganiser.database.VentsellContract;

import java.io.Serializable;

public class EventObject implements Serializable {
    public Event data;

    @Override
    public String toString() {
        return "EventObject{" +
                "data=" + data +
                '}';
    }

    public static class Event implements Serializable {

        // Fields
        private int id;
        private String title;
        private String type;
        private String category;
        private String location;
        private String longitude;
        private String latitude;
        private String description;
        private String start_date;
        private String start_time;
        private String end_date;
        private String end_time;
        private int tickets;
        private String organizer_name;
        private String organizer_email;
        private String organizer_contact;
        private String organizer_contact1;
        private String organizer_contact2;
        private String artwork;
        private int ava_tickets;
        private int sel_tickets;
        private int organiser_id;
        private int page_views;
        private String blurb;
        private String about;
        private String esv_v;

        public Event(int id, String title, String type, String category, String location, String longitude,
                     String latitude, String description, String start_date, String start_time, String end_date,
                     String end_time, int tickets, String organizer_name, String organizer_email, String organizer_contact,
                     String organizer_contact1, String organizer_contact2, String artwork, int ava_tickets,
                     int sel_tickets, int organiser_id, int page_views, String blurb, String about, String esv_v) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.category = category;
            this.location = location;
            this.longitude = longitude;
            this.latitude = latitude;
            this.description = description;
            this.start_date = start_date;
            this.start_time = start_time;
            this.end_date = end_date;
            this.end_time = end_time;
            this.tickets = tickets;
            this.organizer_name = organizer_name;
            this.organizer_email = organizer_email;
            this.organizer_contact = organizer_contact;
            this.organizer_contact1 = organizer_contact1;
            this.organizer_contact2 = organizer_contact2;
            this.artwork = artwork;
            this.ava_tickets = ava_tickets;
            this.sel_tickets = sel_tickets;
            this.organiser_id = organiser_id;
            this.page_views = page_views;
            this.blurb = blurb;
            this.about = about;
            this.esv_v = esv_v;
        }

        // Create an CrowdFunding object from a cursor
        public static Event fromCursor(Cursor curEvents) {
            int id = curEvents.getInt(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_EVENT_ID));
            String title = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_TITLE));
            String type = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_TYPE));
            String category = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_CATEGORY));
            String location = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_LOCATION));
            String longitude = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_LONGITUDE));
            String latitude = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_LATITUDE));
            String description = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_DESCRIPTION));
            String start_date = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_START_DATE));
            String start_time = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_START_TIME));
            String end_date = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_END_DATE));
            String end_time = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_END_TIME));
            int tickets = curEvents.getInt(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_TICKETS));
            String organizer_name = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_ORGANIZER_NAME));
            String organizer_email = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_ORGANIZER_EMAIL));
            String organizer_contact = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_ORGANIZER_CONTACT));
            String organizer_contact1 = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_ORGANIZER_CONTACT1));
            String organizer_contact2 = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_ORGANIZER_CONTACT2));
            String artwork = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_EVENT_BANNER));
            int ava_tickets = curEvents.getInt(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_AVA_TICKETS));
            int sel_tickets = curEvents.getInt(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_SEL_TICKETS));
            int organiser_id = curEvents.getInt(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_ORGANIZER_ID));
            int page_views = curEvents.getInt(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_PAGE_VIEWS));
            String blurb = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_BLURB));
            String about = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_ABOUT));
            String esv_v = curEvents.getString(curEvents.getColumnIndex(VentsellContract.Event.COLUMN_NAME_ESV_V));

            return new Event(id, title, type, category, location, longitude, latitude, description, start_date, start_time,
                    end_date, end_time, tickets, organizer_name, organizer_email, organizer_contact, organizer_contact1,
                    organizer_contact2, artwork, ava_tickets, sel_tickets, organiser_id, page_views, blurb,
                    about, esv_v);
        }

        /**
         * Convenient method to get the objects data members in ContentValues object.
         * This will be useful for Content Provider operations,
         * which use ContentValues object to represent the data.
         *
         * @return ContentValues
         */
        public ContentValues getContentValues() {
            ContentValues values = new ContentValues();
            values.put(VentsellContract.Event.COLUMN_NAME_EVENT_ID, id);
            values.put(VentsellContract.Event.COLUMN_NAME_TITLE, !TextUtils.isEmpty(title) ? title : "");
            values.put(VentsellContract.Event.COLUMN_NAME_TYPE, !TextUtils.isEmpty(type) ? type : "");
            values.put(VentsellContract.Event.COLUMN_NAME_CATEGORY, !TextUtils.isEmpty(category) ? category : "");
            values.put(VentsellContract.Event.COLUMN_NAME_LOCATION, !TextUtils.isEmpty(location) ? location : "");
            values.put(VentsellContract.Event.COLUMN_NAME_LONGITUDE, !TextUtils.isEmpty(longitude) ? longitude : "");
            values.put(VentsellContract.Event.COLUMN_NAME_LATITUDE, !TextUtils.isEmpty(latitude) ? latitude : "");
            values.put(VentsellContract.Event.COLUMN_NAME_DESCRIPTION, !TextUtils.isEmpty(description) ? description : "");
            values.put(VentsellContract.Event.COLUMN_NAME_START_DATE, !TextUtils.isEmpty(start_date) ? start_date : "");
            values.put(VentsellContract.Event.COLUMN_NAME_START_TIME, !TextUtils.isEmpty(start_time) ? start_time : "");
            values.put(VentsellContract.Event.COLUMN_NAME_END_DATE, !TextUtils.isEmpty(end_date) ? end_date : "");
            values.put(VentsellContract.Event.COLUMN_NAME_END_TIME, !TextUtils.isEmpty(end_time) ? end_time : "");
            values.put(VentsellContract.Event.COLUMN_NAME_TICKETS, tickets);
            values.put(VentsellContract.Event.COLUMN_NAME_ORGANIZER_NAME, !TextUtils.isEmpty(organizer_name) ? organizer_name : "");
            values.put(VentsellContract.Event.COLUMN_NAME_ORGANIZER_EMAIL, !TextUtils.isEmpty(organizer_email) ? organizer_email : "");
            values.put(VentsellContract.Event.COLUMN_NAME_ORGANIZER_CONTACT, !TextUtils.isEmpty(organizer_contact) ? organizer_contact : "");
            values.put(VentsellContract.Event.COLUMN_NAME_ORGANIZER_CONTACT1, !TextUtils.isEmpty(organizer_contact1) ? organizer_contact1 : "");
            values.put(VentsellContract.Event.COLUMN_NAME_ORGANIZER_CONTACT2, !TextUtils.isEmpty(organizer_contact2) ? organizer_contact2 : "");
            values.put(VentsellContract.Event.COLUMN_NAME_EVENT_BANNER, !TextUtils.isEmpty(artwork) ? artwork : "");
            values.put(VentsellContract.Event.COLUMN_NAME_AVA_TICKETS, ava_tickets);
            values.put(VentsellContract.Event.COLUMN_NAME_SEL_TICKETS, sel_tickets);
            values.put(VentsellContract.Event.COLUMN_NAME_ORGANIZER_ID, organiser_id);
            values.put(VentsellContract.Event.COLUMN_NAME_PAGE_VIEWS, page_views);
            values.put(VentsellContract.Event.COLUMN_NAME_BLURB, !TextUtils.isEmpty(blurb) ? blurb : "");
            values.put(VentsellContract.Event.COLUMN_NAME_ABOUT, !TextUtils.isEmpty(about) ? about : "");
            values.put(VentsellContract.Event.COLUMN_NAME_ESV_V, !TextUtils.isEmpty(esv_v) ? esv_v : "");
            return values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Event event = (Event) o;

            return id == event.id && tickets == event.tickets && ava_tickets == event.ava_tickets && sel_tickets == event.sel_tickets && organiser_id == event.organiser_id && page_views == event.page_views && (title != null ? title.equals(event.title) : event.title == null && (type != null ? type.equals(event.type) : event.type == null && (category != null ? category.equals(event.category) : event.category == null && (location != null ? location.equals(event.location) : event.location == null && (longitude != null ? longitude.equals(event.longitude) : event.longitude == null && (latitude != null ? latitude.equals(event.latitude) : event.latitude == null && (description != null ? description.equals(event.description) : event.description == null && (start_date != null ? start_date.equals(event.start_date) : event.start_date == null && (start_time != null ? start_time.equals(event.start_time) : event.start_time == null && (end_date != null ? end_date.equals(event.end_date) : event.end_date == null && (end_time != null ? end_time.equals(event.end_time) : event.end_time == null && (organizer_name != null ? organizer_name.equals(event.organizer_name) : event.organizer_name == null && (organizer_email != null ? organizer_email.equals(event.organizer_email) : event.organizer_email == null && (organizer_contact != null ? organizer_contact.equals(event.organizer_contact) : event.organizer_contact == null && (organizer_contact1 != null ? organizer_contact1.equals(event.organizer_contact1) : event.organizer_contact1 == null && (organizer_contact2 != null ? organizer_contact2.equals(event.organizer_contact2) : event.organizer_contact2 == null && (artwork != null ? artwork.equals(event.artwork) : event.artwork == null && (blurb != null ? blurb.equals(event.blurb) : event.blurb == null && (about != null ? about.equals(event.about) : event.about == null && (esv_v != null ? esv_v.equals(event.esv_v) : event.esv_v == null))))))))))))))))))));

        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (title != null ? title.hashCode() : 0);
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (category != null ? category.hashCode() : 0);
            result = 31 * result + (location != null ? location.hashCode() : 0);
            result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
            result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (start_date != null ? start_date.hashCode() : 0);
            result = 31 * result + (start_time != null ? start_time.hashCode() : 0);
            result = 31 * result + (end_date != null ? end_date.hashCode() : 0);
            result = 31 * result + (end_time != null ? end_time.hashCode() : 0);
            result = 31 * result + tickets;
            result = 31 * result + (organizer_name != null ? organizer_name.hashCode() : 0);
            result = 31 * result + (organizer_email != null ? organizer_email.hashCode() : 0);
            result = 31 * result + (organizer_contact != null ? organizer_contact.hashCode() : 0);
            result = 31 * result + (organizer_contact1 != null ? organizer_contact1.hashCode() : 0);
            result = 31 * result + (organizer_contact2 != null ? organizer_contact2.hashCode() : 0);
            result = 31 * result + (artwork != null ? artwork.hashCode() : 0);
            result = 31 * result + ava_tickets;
            result = 31 * result + sel_tickets;
            result = 31 * result + organiser_id;
            result = 31 * result + page_views;
            result = 31 * result + (blurb != null ? blurb.hashCode() : 0);
            result = 31 * result + (about != null ? about.hashCode() : 0);
            result = 31 * result + (esv_v != null ? esv_v.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "CrowdFunding{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", type='" + type + '\'' +
                    ", category='" + category + '\'' +
                    ", location='" + location + '\'' +
                    ", longitude='" + longitude + '\'' +
                    ", latitude='" + latitude + '\'' +
                    ", description='" + description + '\'' +
                    ", start_date='" + start_date + '\'' +
                    ", start_time='" + start_time + '\'' +
                    ", end_date='" + end_date + '\'' +
                    ", end_time='" + end_time + '\'' +
                    ", tickets=" + tickets +
                    ", organizer_name='" + organizer_name + '\'' +
                    ", organizer_email='" + organizer_email + '\'' +
                    ", organizer_contact='" + organizer_contact + '\'' +
                    ", organizer_contact1='" + organizer_contact1 + '\'' +
                    ", organizer_contact2='" + organizer_contact2 + '\'' +
                    ", artwork='" + artwork + '\'' +
                    ", ava_tickets=" + ava_tickets +
                    ", sel_tickets=" + sel_tickets +
                    ", organiser_id=" + organiser_id +
                    ", page_views=" + page_views +
                    ", blurb='" + blurb + '\'' +
                    ", about='" + about + '\'' +
                    ", esv_v='" + esv_v + '\'' +
                    '}';
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStart_date() {
            return start_date;
        }

        public void setStart_date(String start_date) {
            this.start_date = start_date;
        }

        public String getStart_time() {
            return start_time;
        }

        public void setStart_time(String start_time) {
            this.start_time = start_time;
        }

        public String getEnd_date() {
            return end_date;
        }

        public void setEnd_date(String end_date) {
            this.end_date = end_date;
        }

        public String getEnd_time() {
            return end_time;
        }

        public void setEnd_time(String end_time) {
            this.end_time = end_time;
        }

        public int getTickets() {
            return tickets;
        }

        public void setTickets(int tickets) {
            this.tickets = tickets;
        }

        public String getOrganizer_name() {
            return organizer_name;
        }

        public void setOrganizer_name(String organizer_name) {
            this.organizer_name = organizer_name;
        }

        public String getOrganizer_email() {
            return organizer_email;
        }

        public void setOrganizer_email(String organizer_email) {
            this.organizer_email = organizer_email;
        }

        public String getOrganizer_contact() {
            return organizer_contact;
        }

        public void setOrganizer_contact(String organizer_contact) {
            this.organizer_contact = organizer_contact;
        }

        public String getOrganizer_contact1() {
            return organizer_contact1;
        }

        public void setOrganizer_contact1(String organizer_contact1) {
            this.organizer_contact1 = organizer_contact1;
        }

        public String getOrganizer_contact2() {
            return organizer_contact2;
        }

        public void setOrganizer_contact2(String organizer_contact2) {
            this.organizer_contact2 = organizer_contact2;
        }

        public String getArtwork() {
            return artwork;
        }

        public void setArtwork(String artwork) {
            this.artwork = artwork;
        }

        public int getAva_tickets() {
            return ava_tickets;
        }

        public void setAva_tickets(int ava_tickets) {
            this.ava_tickets = ava_tickets;
        }

        public int getSel_tickets() {
            return sel_tickets;
        }

        public void setSel_tickets(int sel_tickets) {
            this.sel_tickets = sel_tickets;
        }

        public int getOrganiser_id() {
            return organiser_id;
        }

        public void setOrganiser_id(int organiser_id) {
            this.organiser_id = organiser_id;
        }

        public int getPage_views() {
            return page_views;
        }

        public void setPage_views(int page_views) {
            this.page_views = page_views;
        }

        public String getBlurb() {
            return blurb;
        }

        public void setBlurb(String blurb) {
            this.blurb = blurb;
        }

        public String getAbout() {
            return about;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public String getEsv_v() {
            return esv_v;
        }

        public void setEsv_v(String esv_v) {
            this.esv_v = esv_v;
        }
    }
}
