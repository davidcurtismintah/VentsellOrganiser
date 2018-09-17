package com.ventsell.ventsellorganiser.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

public class VentsellAccountAuthenticator extends AbstractAccountAuthenticator {
    // Value below must match the account type specified in res/xml/sync_adapter.xml
    public static final String ACCOUNT_TYPE = "com.ventsell.ventsellorganiser.account";
    public static final String ACCOUNT_NAME = "Account";

    public VentsellAccountAuthenticator(Context context) {
        super(context);
    }

    /**
     * Obtain a handle to the {@link Account} used for sync in this application.
     *
     * <p>It is important that the accountType specified here matches the value in your sync adapter
     * configuration XML file for android.accounts.AccountAuthenticator (often saved in
     * res/xml/syncadapter.xml). If this is not set correctly, you'll receive an error indicating
     * that "caller uid XXXXX is different than the authenticator's uid".
     *
     * @return Handle to application's account (not guaranteed to resolve unless CreateSyncAccount()
     *         has been called)
     */
    public static Account GetAccount() {
        // Note: Normally the account name is set to the user's identity (username or email
        // address). However, since we aren't actually using any user accounts, it makes more sense
        // to use a generic string in this case.
        //
        // This string should *not* be localized. If the user switches locale, we would not be
        // able to locate the old account, and may erroneously register multiple accounts.
        return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                 String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse,
                             String s, String s2, String[] strings, Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                     Account account, Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse,
                               Account account, String s, Bundle bundle)
            throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                    Account account, String s, Bundle bundle)
            throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse,
                              Account account, String[] strings)
            throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}
