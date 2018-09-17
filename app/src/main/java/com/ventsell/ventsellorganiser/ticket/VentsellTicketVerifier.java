package com.ventsell.ventsellorganiser.ticket;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.ventsell.ventsellorganiser.AppController;
import com.ventsell.ventsellorganiser.ticket.objects.VentsellETicketObject;

import org.json.JSONObject;

import java.io.Serializable;

public class VentsellTicketVerifier {

    public static final String TAG = VentsellTicketVerifier.class.getSimpleName();

    private final static String APP_ID = "b6z0m9pgpDB1k5NSon8umL9rX72ahsb3";
    private static final String VERIFIER_BASE_URL = "https://ventsell.com/api/ti-check/" + APP_ID + "/";

    public static final String ERROR_FAILED_TO_VERIFY_TICKET = "Failed to verify ventsell ticket, please try again";
    public static final String ERROR_INVALID_QR_CODE = "Invalid ventsell ticket";

    private TicketVerifierResponseCallBack callBack;

    public interface TicketVerifierResponseCallBack {
        void successCallBack(VentsellETicketObject result);
        void errorCallBack(String error);
    }

    private Response.Listener<JSONObject> verifyTicketResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (response.has("error")){
                VentsellComInvalidTicketErrorHolder ventsellComInvalidTicketErrorHolder = new Gson().fromJson(response.toString(), VentsellComInvalidTicketErrorHolder.class);
                callBack.errorCallBack(ERROR_INVALID_QR_CODE);
                VolleyLog.d("ventsell: %s > Error verifying ticket : %s [ %s ]", TAG, ERROR_INVALID_QR_CODE, ventsellComInvalidTicketErrorHolder);
            } else {
                VentsellETicketObject ticket = new Gson().fromJson(response.toString(), VentsellETicketObject.class);
                callBack.successCallBack(ticket);
                VolleyLog.d("ventsell: %s > Verified ticket [ %s ]", TAG, ticket);
            }
        }
    };

    private Response.ErrorListener authErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            VolleyLog.d("ventsell: %s > Error verifying ticket : %s [ %s ]", TAG,  ERROR_FAILED_TO_VERIFY_TICKET, error);
            callBack.errorCallBack(ERROR_FAILED_TO_VERIFY_TICKET);
        }
    };

    public void verifyTicket(String ticketId, TicketVerifierResponseCallBack callBack){

        Log.d("ventsell", TAG + "> verifyETicketId");

        String urlString = VERIFIER_BASE_URL + ticketId.trim().toUpperCase() ;
        Log.d("ventsell", TAG + " > " + urlString);
        verify(urlString);
        this.callBack = callBack;
    }

    private void verify(String url) {
        Log.d("ventsell", TAG + "> verify method");
        JsonObjectRequest verifyTicketRequest = new JsonObjectRequest(Request.Method.GET, url, null, verifyTicketResponseListener, authErrorListener);
        AppController.getInstance().addToRequestQueue(verifyTicketRequest, TAG);
    }

    private static class VentsellComInvalidTicketErrorHolder implements Serializable{
        VentsellComInvalidTicketError error;

        @Override
        public String toString() {
            return "VentsellComInvalidTicketErrorHolder{" +
                    "error=" + error +
                    '}';
        }

        private class VentsellComInvalidTicketError implements Serializable {
            String text;

            @Override
            public String toString() {
                return "VentsellComInvalidTicketError{" +
                        "ticketId='" + text + '\'' +
                        '}';
            }
        }
    }

}
