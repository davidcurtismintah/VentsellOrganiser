package com.ventsell.ventsellorganiser.authenticate;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.ventsell.ventsellorganiser.AppController;
import com.ventsell.ventsellorganiser.authenticate.objects.UserObject;
import com.ventsell.ventsellorganiser.utils.Constants;

import org.json.JSONObject;

import java.io.Serializable;

public class VentsellServerAuthenticator implements ServerAuthenticator {

    public static final String TAG = VentsellServerAuthenticator.class.getSimpleName();

    static final String ERROR_INCORRECT_LOGIN_DETAILS = "Incorrect login details";
    static final String ERROR_USER_DOES_NOT_EXIST = "User does not exist";
    static final String ERROR_FAILED_TO_LOG_IN_USER = "Failed to log in user";

    private final static String APP_ID = "b6z0m9pgpDB1k5NSon8umL9rX72ahsb3";
    private static final String AUTH_EMAIL_BASE_URL = "https://ventsell.com/api/oauth/" + APP_ID + "/";
    private static final String AUTH_SOCIAL_BASE_URL = "https://ventsell.com/api/oauth/social/" + APP_ID + "/";

    private int loginType;

    private JsonObjectRequest mAuthRequest;
    private AuthenticateResponseCallBack callBack;

    private Response.Listener<JSONObject> authResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (response.has("error")){
                VentsellComWrongPasswordErrorHolder ventsellComWrongPasswordErrorHolder = new Gson().fromJson(response.toString(), VentsellComWrongPasswordErrorHolder.class);
                mAuthRequest = null;
                callBack.errorCallBack(ERROR_INCORRECT_LOGIN_DETAILS);
                VolleyLog.d("ventsell: %s > Error signing-in : %s [ %s ]", TAG, ERROR_INCORRECT_LOGIN_DETAILS, ventsellComWrongPasswordErrorHolder);
            } else if (response.has("success")){
                VentsellComNewUserErrorHolder ventsellComNewUserErrorHolder = new Gson().fromJson(response.toString(), VentsellComNewUserErrorHolder.class);
                mAuthRequest = null;
                callBack.errorCallBack(ERROR_USER_DOES_NOT_EXIST);
                VolleyLog.d("ventsell: %s > Error signing-in : %s [ %s ]", TAG, ERROR_USER_DOES_NOT_EXIST, ventsellComNewUserErrorHolder);
            } else {
                UserObject userObject = new Gson().fromJson(response.toString(), UserObject.class);
                mAuthRequest = null;
                callBack.successCallBack(userObject);
                VolleyLog.d("ventsell: %s > Signed-in [ %s ]", TAG, userObject);
            }
        }
    };

    private Response.ErrorListener authErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            VolleyLog.d("ventsell: %s > Error signing-in : %s [ %s ]", TAG,  ERROR_FAILED_TO_LOG_IN_USER, error);
            mAuthRequest = null;
            callBack.errorCallBack(ERROR_FAILED_TO_LOG_IN_USER);
        }
    };

    @Override
    public void userSignUp(String name, String email, String pass, AuthenticateResponseCallBack callBack) {

    }

    @Override
    public void userSignInEmail(String email, String password, AuthenticateResponseCallBack callBack){

        if (isAuthenticating()){
            return;
        }
        loginType = Constants.LOGIN_TYPE_EMAIL;
        Log.d("ventsell", TAG + "> verifyETicketId");

        String urlString = AUTH_EMAIL_BASE_URL + email + "/" + password;
        Log.d("ventsell", TAG + " > " + urlString);
        authenticate(urlString);
        this.callBack = callBack;
    }

    @Override
    public void userSignInFacebook(String email, String authToken, AuthenticateResponseCallBack callBack){

        if (isAuthenticating()){
            return;
        }
        loginType = Constants.LOGIN_TYPE_FACEBOOK;
        Log.d("ventsell", TAG + "> userSignInFacebook");

        String urlString = AUTH_SOCIAL_BASE_URL + email + "/" + authToken;
        Log.d("ventsell", TAG + " > " + urlString);
        authenticate(urlString);
        this.callBack = callBack;
    }

    @Override
    public void userSignInGooglePlus(String email, String authToken, AuthenticateResponseCallBack callBack){

        if (isAuthenticating()){
            return;
        }
        loginType = Constants.LOGIN_TYPE_GOOGLE_PLUS;
        Log.d("ventsell", TAG + "> userSignInGooglePlus");

        String urlString = AUTH_SOCIAL_BASE_URL + email + "/" + authToken;
        Log.d("ventsell", TAG + " > " + urlString);
        authenticate(urlString);
        this.callBack = callBack;
    }

    private void authenticate(String url) {
        Log.d("ventsell", TAG + "> authenticate method");
        mAuthRequest = new JsonObjectRequest(Request.Method.GET, url, null, authResponseListener, authErrorListener);
        AppController.getInstance().addToRequestQueue(mAuthRequest, TAG);
    }

    public int getLoginType() {
        return loginType;
    }

    public boolean isAuthenticating() {
        return mAuthRequest != null;
    }

    public void resetAuthenticateStatus(){
        AppController.getInstance().cancelPendingRequests(VentsellServerAuthenticator.TAG);
        mAuthRequest = null;
    }

    private static class VentsellComWrongPasswordErrorHolder implements Serializable{
        VentsellComWrongPasswordError error;

        @Override
        public String toString() {
            return "VentsellComWrongPasswordErrorHolder{" +
                    "error=" + error +
                    '}';
        }

        private class VentsellComWrongPasswordError implements Serializable {
            String text;

            @Override
            public String toString() {
                return "VentsellComWrongPasswordError{" +
                        "ticketId='" + text + '\'' +
                        '}';
            }
        }
    }

    private static class VentsellComNewUserErrorHolder implements Serializable{
        VentsellComNewUserError success;

        @Override
        public String toString() {
            return "VentsellComNewUserErrorHolder{" +
                    "success=" + success +
                    '}';
        }

        private class VentsellComNewUserError implements Serializable {
            String text;

            @Override
            public String toString() {
                return "VentsellComNewUserError{" +
                        "ticketId='" + text + '\'' +
                        '}';
            }
        }
    }

}
