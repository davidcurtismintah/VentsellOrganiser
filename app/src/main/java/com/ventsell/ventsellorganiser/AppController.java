package com.ventsell.ventsellorganiser;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.ventsell.ventsellorganiser.authenticate.objects.UserObject;
import com.ventsell.ventsellorganiser.utils.Constants;

public class AppController extends Application {

    private static String TAG = AppController.class.getSimpleName();

    private static final String KEY_USER_PREFS = "com.ventsell.events.prefs";

    private static final String USERDATA_USER_OBJ_ID = "user_id";
    private static final String USERDATA_USER_OBJ_EMAIL = "user_email";
    private static final String USERDATA_USER_OBJ_PASSWORD = "user_password";
    private static final String USERDATA_USER_OBJ_FULL_NAME = "user_full_name";
    private static final String USERDATA_USER_OBJ_IMAGE = "user_image";

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private static AppController sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    public static synchronized AppController getInstance() {
        return sInstance;
    }

    // Volley stuff
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(mRequestQueue, new MemoryCache());
        }
        return mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        setRetryPolicy(req);
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        setRetryPolicy(req);
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    private <T> void setRetryPolicy(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void cancelPendingRequests() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

    // Login stuff below
    private SharedPreferences getUserPreferences() {
        return getSharedPreferences(KEY_USER_PREFS, MODE_PRIVATE);
    }

    public void setCurrentUser(UserObject currentUserObject) {
        getUserPreferences().edit()
                .putString(USERDATA_USER_OBJ_ID, currentUserObject.getId())
                .putString(USERDATA_USER_OBJ_EMAIL, currentUserObject.getEmail())
                .putString(USERDATA_USER_OBJ_PASSWORD, currentUserObject.getPassword())
                .putString(USERDATA_USER_OBJ_FULL_NAME, currentUserObject.getFullName())
                .putString(USERDATA_USER_OBJ_IMAGE, currentUserObject.getImage())
                .apply();
    }

    public UserObject getCurrentUser() {
        UserObject userObject = new UserObject();
        userObject.setId(getUserPreferences().getString(USERDATA_USER_OBJ_ID, ""));
        userObject.setEmail(getUserPreferences().getString(USERDATA_USER_OBJ_EMAIL, ""));
        userObject.setPassword(getUserPreferences().getString(USERDATA_USER_OBJ_PASSWORD, ""));
        userObject.setFullName(getUserPreferences().getString(USERDATA_USER_OBJ_FULL_NAME, ""));
        userObject.setImage(getUserPreferences().getString(USERDATA_USER_OBJ_IMAGE, ""));
        return userObject;
    }

    public int getLoginType() {
        return getUserPreferences().getInt(Constants.KEY_LOGIN_TYPE, Constants.LOGIN_TYPE_EMAIL);
    }

    public void setLoginType(int loginType) {
        getUserPreferences().edit()
                .putInt(Constants.KEY_LOGIN_TYPE, loginType)
                .apply();
    }

    public Boolean isLoggedIn() {
        return getUserPreferences().contains(USERDATA_USER_OBJ_ID);
    }

    public void logOut() {
        getUserPreferences().edit()
                .remove(Constants.KEY_LOGIN_TYPE)
                .remove(USERDATA_USER_OBJ_ID)
                .remove(USERDATA_USER_OBJ_EMAIL)
                .remove(USERDATA_USER_OBJ_PASSWORD)
                .remove(USERDATA_USER_OBJ_FULL_NAME)
                .remove(USERDATA_USER_OBJ_IMAGE)
                .apply();
    }


    //--------------------------------------------------------------------------------------------
    private static class MemoryCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

        public static int getDefaultLruCacheSize() {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            return maxMemory / 8;
        }

        public MemoryCache() {
            this(getDefaultLruCacheSize());
        }

        public MemoryCache(int sizeInKilobytes) {
            super(sizeInKilobytes);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            value.getByteCount();
            return value.getByteCount() / 1024;
        }

        @Override
        public Bitmap getBitmap(String url) {
            return get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            put(url, bitmap);
        }
    }
}
