package com.ventsell.ventsellorganiser.authenticate;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.ventsell.ventsellorganiser.AppController;
import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.authenticate.objects.UserObject;
import com.ventsell.ventsellorganiser.main.MainActivity;
import com.ventsell.ventsellorganiser.sync.SyncUtils;
import com.ventsell.ventsellorganiser.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Intent.ACTION_VIEW;
import static com.ventsell.ventsellorganiser.authenticate.VentsellServerAuthenticator.ERROR_FAILED_TO_LOG_IN_USER;
import static com.ventsell.ventsellorganiser.authenticate.VentsellServerAuthenticator.ERROR_INCORRECT_LOGIN_DETAILS;
import static com.ventsell.ventsellorganiser.authenticate.VentsellServerAuthenticator.ERROR_USER_DOES_NOT_EXIST;

/**
 * A login screen that offers login via email/password, facebook, and gmail.
 */
public class AuthenticateActivity extends AppCompatActivity implements AuthenticateSignInFragment.SignInCallbackListener {

    private static final String TAG = AuthenticateActivity.class.getSimpleName();

    public static final String EXTRA_USERDATA_USER_OBJ_ID = "com.ventsell.organiser.extra_user_id";
    public static final String EXTRA_ACTION = "com.ventsell.organiser.extra_login_action";

    public class Action {
        public static final int LOGIN = 2001;
        public static final int LOGOUT = 2002;
    }

    private int mLoginAction;

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 0;

    // Facebook callback
    private CallbackManager mFacebookCallbackManager;
    private FacebookCallback<LoginResult> mFacebookCallback;
    private ProfileTracker mFacebookProfileTracker;

    // Gmail client
    private GoogleApiClient mGoogleApiClient;

    private static final ServerAuthenticator SERVER_AUTHENTICATOR = new VentsellServerAuthenticator();
    private ServerAuthenticator.AuthenticateResponseCallBack authenticateResponseCallBack;

    private AlertDialog newUserDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate_root);

        mLoginAction = getIntent().getIntExtra(EXTRA_ACTION, Action.LOGIN);

        // Facebook
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookCallback = new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                String token = loginResult.getAccessToken().getToken();
                Log.d("ventsell", TAG + "> facebook:onSuccess:" + token);
                signInWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("ventsell", TAG + "> facebook:onCancel");
                showProgress(false);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("ventsell", TAG + "> facebook:onError", error);
                showProgress(false);
            }
        };
        mFacebookProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {

                if (currentProfile == null) {
                    mFacebookProfileTracker.stopTracking();
                    // logout from device
                    AppController.getInstance().logOut();
                    showProgress(false);
                }
            }
        };

        // Gmail
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(AuthenticateActivity.this)
                .enableAutoManage(AuthenticateActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d("ventsell", TAG + "> Could not connect:" + connectionResult.toString());
                        showProgress(false);
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if (mLoginAction == AuthenticateActivity.Action.LOGOUT) {
                            // logout
                            if (AppController.getInstance().getLoginType() == Constants.LOGIN_TYPE_GOOGLE_PLUS) {
                                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                        new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(@NonNull Status status) {
                                                // logout from device
                                                // TODO check logout success
                                                AppController.getInstance().logOut();
                                                showProgress(false);
                                            }
                                        });
                            }
                        } else {
                            // login
                            showProgress(true);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        // nothing specifically required here, onConnected will be called when connection resumes
                        showProgress(false);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        authenticateResponseCallBack = new ServerAuthenticator.AuthenticateResponseCallBack() {
            @Override
            public void successCallBack(UserObject userObject) {
                Log.d("ventsell", TAG + "> Current userObject: " + userObject);
                AppController.getInstance().setLoginType(SERVER_AUTHENTICATOR.getLoginType());
                AppController.getInstance().setCurrentUser(userObject);

                Intent startIntent = new Intent(AuthenticateActivity.this, MainActivity.class);
                startIntent.putExtra(AuthenticateActivity.EXTRA_USERDATA_USER_OBJ_ID, userObject.getId());
                startActivity(startIntent);
                AuthenticateActivity.this.finish();
            }

            @Override
            public void errorCallBack(String error) {
                Log.d("ventsell", TAG + "> Error signing-in [" + error + "]");

                switch (error) {
                    case ERROR_USER_DOES_NOT_EXIST:
                        newUserDialog.show();
                        break;
                    case ERROR_INCORRECT_LOGIN_DETAILS:
                        Toast.makeText(AuthenticateActivity.this, ERROR_INCORRECT_LOGIN_DETAILS, Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR_FAILED_TO_LOG_IN_USER:
                        Toast.makeText(AuthenticateActivity.this, ERROR_FAILED_TO_LOG_IN_USER, Toast.LENGTH_SHORT).show();
                        break;
                }

                logOutUser(AppController.getInstance().getLoginType());
            }
        };

        newUserDialog = new AlertDialog.Builder(AuthenticateActivity.this)
                .setTitle(ERROR_USER_DOES_NOT_EXIST)
                .setMessage("Do you want to create a new Ventsell account with this email")
                .setPositiveButton(R.string.new_user_dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PackageManager pm = AuthenticateActivity.this.getPackageManager();
                        String linkUrl = getString(R.string.terms_and_create_account_url);
                        Intent startIntent = new Intent(ACTION_VIEW, Uri.parse(linkUrl));
                        if (startIntent.resolveActivity(pm) != null) {
                            Log.d("ventsell", TAG + "> Create new user accepted");
                            AuthenticateActivity.this.startActivity(startIntent);
                        } else {
                            Log.d("ventsell", TAG + "> No Activity available to open: " + linkUrl);
                        }
                    }
                })
                .setNegativeButton(R.string.new_user_dialog_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("ventsell", TAG + "> Create new user canceled");
                        dialog.cancel();
                    }
                })
                .create();

        // Create account, if needed
        SyncUtils.CreateSyncAccount(this);

        if (AppController.getInstance().isLoggedIn()) {
            if (mLoginAction == Action.LOGOUT) {
                logOutUser(AppController.getInstance().getLoginType());
            } else {
                UserObject userObject = AppController.getInstance().getCurrentUser();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(EXTRA_USERDATA_USER_OBJ_ID, userObject.getId());
                startActivity(intent);
                finish();
            }
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.authenticate_container);
        if (fragment == null) {
            fragment = new AuthenticateWelcomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.authenticate_container, fragment, "AuthenticateWelcomeFragment")
                    .commit();
        }

    }

    private void logOutUser(int loginType) {
        Log.d("ventsell", TAG + "> logOutUser");
        showProgress(true);

        if (loginType == Constants.LOGIN_TYPE_FACEBOOK) {
            // logout from facebook
            mFacebookProfileTracker.startTracking();
            new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                    .Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {
                    LoginManager.getInstance().logOut();
                }
            }).executeAsync();
        } else if (loginType == Constants.LOGIN_TYPE_GOOGLE_PLUS) {
            // logout from gmail
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        } else if (loginType == Constants.LOGIN_TYPE_EMAIL) {
            // logout from device
            AppController.getInstance().logOut();
            // logout from ventsell.com
            showProgress(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(AuthenticateActivity.this);
            mGoogleApiClient.disconnect();
        }
        AppController.getInstance().cancelPendingRequests(VentsellServerAuthenticator.TAG);
    }

    private void signInWithFacebook(final AccessToken token) {
        if (!checkInternet()) {
            showProgress(false);
            Toast.makeText(AuthenticateActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        // connect to server with token.getToken() and request account details
        GraphRequest emailRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    if (object == null) {
                        return;
                    }
                    String authToken = token.getToken();
                    String email = object.getString("email");

                    Log.d("ventsell", TAG + "signInWithFacebook:" + "email=" + email + " token=" + authToken);

                    // connect to server and request account details
                    Log.d("ventsell", TAG + "> Started authenticating");
                    SERVER_AUTHENTICATOR.userSignInFacebook(email, authToken, authenticateResponseCallBack);
                } catch (JSONException e) {
                    Log.d("ventsell", TAG + "Error parsing facebook GraphResponse " + e);
                    showProgress(false);
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        emailRequest.setParameters(parameters);
        emailRequest.executeAsync();
    }

    private void checkInputAndSignInWithGmail() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!checkInternet()) {
            showProgress(false);
            Toast.makeText(AuthenticateActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            Log.d("ventsell", TAG + "> gmail:onActivityResult");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            signInWithGmail(result);
        } else {
            Log.d("ventsell", TAG + "> facebook:onActivityResult");
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void signInWithGmail(GoogleSignInResult result) {
        Log.d("ventsell", TAG + "> gmailButtonClicked:" + result.isSuccess());
        if (result.isSuccess()) {
            // signed in successfully
            Log.d("ventsell", TAG + "> getSignInAccount:" + result.getSignInAccount());
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {

                // user signed in
                final String authToken = acct.getIdToken();
                final String email = acct.getEmail();

                Log.d("ventsell", TAG + "> checkInputAndSignInWithGmail:" + email + "" + authToken);

                // connect to server and request account details
                Log.d("ventsell", TAG + "> Started authenticating");
                SERVER_AUTHENTICATOR.userSignInGooglePlus(email, authToken, authenticateResponseCallBack);
            } else {
                Log.d("ventsell", TAG + "> GoogleSignInAccount == null");
                showProgress(false);
            }
        } else {
            Log.d("ventsell", TAG + "> Sign in not successful");
            showProgress(false);
        }
    }

    private void signInWithEmail(String email, String password) {
        if (!checkInternet()) {
            showProgress(false);
            Toast.makeText(AuthenticateActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);

        // connect to server and request account details
        Log.d("ventsell", TAG + "> Started authenticating");
        SERVER_AUTHENTICATOR.userSignInEmail(email, password, authenticateResponseCallBack);
    }

    @Override
    public void emailButtonClicked(String email, String password) {
        signInWithEmail(email, password);
    }

    @Override
    public void gmailButtonClicked() {
        checkInputAndSignInWithGmail();
    }

    @Override
    public CallbackManager getFacebookCallbackManager() {
        return mFacebookCallbackManager;
    }

    @Override
    public FacebookCallback<LoginResult> getFacebookCallback() {
        return mFacebookCallback;
    }

    private boolean checkInternet() {
        ConnectivityManager connMgr = (ConnectivityManager)
                AuthenticateActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("AuthenticateSignInFragment");
        if (fragment instanceof AuthenticateSignInFragment) {
            ((AuthenticateSignInFragment) fragment).showProgress(show);
        }
    }

    @Override
    public void onBackPressed() {
        if (SERVER_AUTHENTICATOR.isAuthenticating()) {
            SERVER_AUTHENTICATOR.resetAuthenticateStatus();
            logOutUser(SERVER_AUTHENTICATOR.getLoginType());
            return;
        }
        super.onBackPressed();
    }
}