package com.thosedays.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by joey on 14/11/8.
 */
public class AccountUtils {

    private static final String PREFIX_PREF_AUTH_TOKEN = "auth_token_";

    public static AccountManager getAccountManager(Context context) {
        // Get an instance of the Android account manager
        return (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    }

    public static void setAuthToken(Context context, Account account, String tokenType, String token) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(makeAccountSpecificPrefKey(account.name, PREFIX_PREF_AUTH_TOKEN), token).commit();
    }

    public static String getAuthToken(Context context, Account account, String tokenType) {
        return getSharedPreferences(context).getString(makeAccountSpecificPrefKey(account.name, PREFIX_PREF_AUTH_TOKEN), null);
    }

    private static String makeAccountSpecificPrefKey(String accountName, String prefix) {
        return prefix + accountName;
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
