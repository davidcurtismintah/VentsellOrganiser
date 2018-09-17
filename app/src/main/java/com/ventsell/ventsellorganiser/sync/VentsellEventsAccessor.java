package com.ventsell.ventsellorganiser.sync;

import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ventsell.ventsellorganiser.AppController;
import com.ventsell.ventsellorganiser.database.objects.EventObject;
import com.ventsell.ventsellorganiser.database.objects.VentsellError;
import com.ventsell.ventsellorganiser.utils.StringTypeAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is intended to encapsulate all the actions against obtaining events from Ventsell.com.
 */
public class VentsellEventsAccessor {

    private final String TAG = VentsellEventsAccessor.class.getSimpleName();

    private static final String ERROR_OBTAINING_EVENTS = "Error obtaining events";

    private final static String APP_ID = "b6z0m9pgpDB1k5NSon8umL9rX72ahsb3";
    private static final String EVENTS_BASE_URL = "https://ventsell.com/api/events/" + APP_ID + "/";

    private JsonArrayRequest mEventsRequest;
    private EventsAccessorResponseCallBack callBack;
    private RequestFuture<JSONArray> eventsRequestFuture = RequestFuture.newFuture();

    public interface EventsAccessorResponseCallBack {
        void onObtainEvents(List<EventObject.Event> result);
        void onError(VentsellError error);
    }

    private Response.Listener<JSONArray> eventsAccessorResponseListener = new Response.Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray response) {
            VolleyLog.d("ventsell: %s > %s", TAG, response);
            List<EventObject.Event> remoteEvents = new ArrayList<>();
            checkResponse(remoteEvents, response);
            mEventsRequest = null;
            callBack.onObtainEvents(remoteEvents);
        }
    };

    private Response.ErrorListener eventsAccessorErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            VolleyLog.d("ventsell: %s > %s", TAG, error);
            mEventsRequest = null;
            callBack.onError(new VentsellError(ERROR_OBTAINING_EVENTS));
        }
    };

    private void checkResponse(List<EventObject.Event> remoteEvents, JSONArray response) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(String.class, new StringTypeAdapter());
        Gson gson = gsonBuilder.create();
        try {
            if (response.getJSONObject(0).has("error")) {// TODO find out what exact error is sent
                EventsAccessorError eventsAccessorError = gson.fromJson(response.toString(), EventsAccessorError.class);
                VolleyLog.e("ventsell: %s > Error getting events [ %s ]", TAG, eventsAccessorError.error);
            } else {
                Type eventListType = new TypeToken<ArrayList<EventObject>>() {
                }.getType();
                List<EventObject> remoteEventObjects = gson.fromJson(response.toString(), eventListType);
                for (EventObject eventObject : remoteEventObjects){
                    remoteEvents.add(eventObject.data);
                }
                VolleyLog.d("ventsell: %s > Obtained remote events [ %s ]", TAG, remoteEvents);
            }
        } catch (JSONException e) {
            VolleyLog.e("ventsell: %s > Could not get events. Error in response", TAG);
            e.printStackTrace();
        }
    }

    /**
     *  Obtain events synchronously on the calling thread
     *  @param id event id
     *  @return list of events
     */
    @NonNull
    List<EventObject.Event> obtainEvents(@NonNull String id) {
        List<EventObject.Event> remoteEvents = new ArrayList<>();
        if (mEventsRequest != null) {
            return remoteEvents;
        }

        String urlString = EVENTS_BASE_URL + id;
        mEventsRequest = new JsonArrayRequest(Request.Method.GET, urlString, null, eventsRequestFuture, eventsRequestFuture);
        AppController.getInstance().addToRequestQueue(mEventsRequest);

        try {
            JSONArray response = eventsRequestFuture.get(30, TimeUnit.SECONDS);
            VolleyLog.e("ventsell: %s > events api call success. [ %s ]", TAG, response);
            checkResponse(remoteEvents, response);
        } catch (InterruptedException e) {
            VolleyLog.e("ventsell: %s > events api call interrupted. [ %s ]", TAG, e);
        } catch (ExecutionException e) {
            VolleyLog.e("ventsell: %s > events api call failed. [ %s ]", TAG, e);
        } catch (TimeoutException e) {
            VolleyLog.e("ventsell: %s > events api call timed out. [ %s ]", TAG, e);
        }
        mEventsRequest = null;
        return remoteEvents;
    }

    /**
     *  Obtain events asynchronously on a background thread
     *  @param id event id
     *  @param callBack Callback to use when list of events have been obtained
     */
    public void obtainEvents(@NonNull String id, @NonNull EventsAccessorResponseCallBack callBack) {

        if (mEventsRequest != null) {
            return;
        }
        String urlString = EVENTS_BASE_URL + id;
        mEventsRequest = new JsonArrayRequest(Request.Method.GET, urlString, null, eventsAccessorResponseListener, eventsAccessorErrorListener);
        AppController.getInstance().addToRequestQueue(mEventsRequest);
        this.callBack = callBack;
    }

    private static class EventsAccessorError implements Serializable {
        String error;
    }
}

