package com.ventsell.ventsellorganiser.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.toolbox.NetworkImageView;
import com.ventsell.ventsellorganiser.AppController;
import com.ventsell.ventsellorganiser.main.MainActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserImageHelper {

    public static void setUserImage(NetworkImageView userImageView, String imageUrl) {
        if (imageUrl.startsWith("/public/src/users/")){
            userImageView.setImageUrl("https://www.ventsell.com" + imageUrl, AppController.getInstance().getImageLoader());
        } else if (imageUrl.startsWith("https://graph.facebook.com")) {
            getFacebookImageUrl(userImageView, imageUrl);
        } else {
            userImageView.setImageUrl(imageUrl, AppController.getInstance().getImageLoader());
        }
    }

    public static void getFacebookImageUrl(final NetworkImageView imageView, final String imageUrl) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                HttpURLConnection con;
                Log.d("ventsell", MainActivity.TAG + "> Original URL: " + imageUrl);
                try {
                    con = (HttpURLConnection) new URL(imageUrl).openConnection();
                    con.setInstanceFollowRedirects(false);
                    con.connect();
                    if (con.getResponseCode() >= 300 && con.getResponseCode() < 400) {
                        String location = con.getHeaderField("Location");
                        Log.d("ventsell", MainActivity.TAG + "> Redirected URL: " + location);
                        return location;
                    }
                } catch (IOException e) {
                    Log.d("ventsell", MainActivity.TAG + "> Error getting redirected URL: " + e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String redirectImageUrl) {
                imageView.setImageUrl(redirectImageUrl, AppController.getInstance().getImageLoader());
            }
        }.execute();
    }

}
