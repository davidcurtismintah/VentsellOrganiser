package com.ventsell.ventsellorganiser.authenticate;

import com.ventsell.ventsellorganiser.authenticate.objects.UserObject;

public interface ServerAuthenticator {

    interface AuthenticateResponseCallBack {
        void successCallBack(UserObject result);
        void errorCallBack(String error);
    }

    void userSignUp(final String name, final String email, final String pass, AuthenticateResponseCallBack callBack);

    void userSignInEmail(final String user, final String pass, AuthenticateResponseCallBack callBack);

    void userSignInFacebook(final String user, final String authToken, AuthenticateResponseCallBack callBack);

    void userSignInGooglePlus(final String user, final String authToken, AuthenticateResponseCallBack callBack);

    int getLoginType();

    boolean isAuthenticating();

    void resetAuthenticateStatus();
}
