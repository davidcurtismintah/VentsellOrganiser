package com.ventsell.ventsellorganiser.authenticate;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.ventsell.ventsellorganiser.R;
import com.ventsell.ventsellorganiser.utils.TextHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.Context.INPUT_METHOD_SERVICE;

public class AuthenticateSignInFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int REQUEST_GET_ACCOUNTS = 1;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private LoginButton mLoginButton;

    interface SignInCallbackListener {
        void emailButtonClicked(String email, String password);

        void gmailButtonClicked();

        CallbackManager getFacebookCallbackManager();

        FacebookCallback<LoginResult> getFacebookCallback();
    }

    private SignInCallbackListener signInCallbackListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SignInCallbackListener) {
            signInCallbackListener = (SignInCallbackListener) context;
        } else
            throw new RuntimeException("must implement SignInCallbackListener");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        AppCompatTextView forgotPass = (AppCompatTextView) view.findViewById(R.id.forgot_password);
        forgotPass.setText(TextHelper.createLinkedText(getContext(), getString(R.string.forgot_password_text), 0, 21, getString(R.string.authenticate_url)));
        forgotPass.setMovementMethod(LinkMovementMethod.getInstance());

        AppCompatTextView termsOfUse = (AppCompatTextView) view.findViewById(R.id.terms_of_use);
        termsOfUse.setText(TextHelper.createLinkedText(getContext(), getString(R.string.terms_reminder), 32, 44, getString(R.string.terms_and_conditions_url)));
        termsOfUse.setMovementMethod(LinkMovementMethod.getInstance());

        mLoginFormView = view.findViewById(R.id.login_form);
        mProgressView = view.findViewById(R.id.login_progress);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) view.findViewById(R.id.full_name);
        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.next_field || id == EditorInfo.IME_ACTION_NEXT) {
                    if (isEmailValidAndNoError(mEmailView.getText().toString())) {
                        mPasswordView.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });
        mEmailView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!TextUtils.isEmpty(mPasswordView.getText().toString())) {
                        isPasswordValidAndNoError(mPasswordView.getText().toString());
                    } else {
                        mPasswordView.setError(null);
                    }
                }
            }
        });
        // set key listener
        populateAutoComplete();

        mPasswordView = (EditText) view.findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.login || id == EditorInfo.IME_NULL) {
                    if (isPasswordValidAndNoError(mPasswordView.getText().toString())) {
                        checkInputAndSignInWithEmail();
                    }
                    return true;
                }
                return false;
            }
        });
        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!TextUtils.isEmpty(mEmailView.getText().toString())) {
                        isEmailValidAndNoError(mEmailView.getText().toString());
                    } else {
                        mEmailView.setError(null);
                    }
                }
            }
        });

        Button mEmailSignInButton = (Button) view.findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInputAndSignInWithEmail();
            }
        });

        mLoginButton = (LoginButton) view.findViewById(R.id.facebook_sign_in_button);
        mLoginButton.setReadPermissions("email");
//        mLoginButton.setFragment(this);
        mLoginButton.registerCallback(signInCallbackListener.getFacebookCallbackManager(),
                signInCallbackListener.getFacebookCallback());

        SignInButton signInButton = (SignInButton) view.findViewById(R.id.google_sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signInCallbackListener != null) {
                    signInCallbackListener.gmailButtonClicked();
                }
            }
        });

        return view;
    }

    private void populateAutoComplete() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (!contactsRequestGranted()) {
                return;
            }
            getLoaderManager().initLoader(0, null, this);
        } else {
            if (!accountsRequestGranted()) {
                return;
            }
            Pattern emailPattern = Patterns.EMAIL_ADDRESS;
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Account[] accounts = AccountManager.get(getActivity()).getAccounts();
            List<String> emails = new ArrayList<>();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    emails.add(account.name);
                }
            }
            addEmailsToAutoComplete(emails);
        }
    }

    private boolean contactsRequestGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ActivityCompat.checkSelfPermission(getContext(), READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale_contacts, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    private boolean accountsRequestGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale_Accounts, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_GET_ACCOUNTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_GET_ACCOUNTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
        if (requestCode == REQUEST_GET_ACCOUNTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    private boolean isEmailValidAndNoError(String email) {
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            return false;
        }
        if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            return false;
        }
        mEmailView.setError(null);
        return true;
    }

    private boolean isPasswordValidAndNoError(String password) {
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            return false;
        }
//        if (!isPasswordValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            return false;
//        }
        mPasswordView.setError(null);
        return true;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
//        return password.length() > 6;
        return true;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            // potentially filter on ProfileQuery.IS_PRIMARY
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void checkInputAndSignInWithEmail() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!isPasswordValidAndNoError(password)) {
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (!isEmailValidAndNoError(email)) {
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            if (signInCallbackListener != null) {
                signInCallbackListener.emailButtonClicked(email, password);
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }

        InputMethodManager mgr = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(mLoginFormView.getWindowToken(), 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("EMAIL_TEXT", mEmailView.getText().toString());
        outState.putString("PASSWORD_TEXT", mPasswordView.getText().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mEmailView.setText(savedInstanceState.getString("EMAIL_TEXT", ""));
            mPasswordView.setText(savedInstanceState.getString("PASSWORD_TEXT", ""));
        }
    }
}
