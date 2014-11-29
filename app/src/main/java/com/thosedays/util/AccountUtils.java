package com.thosedays.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.thosedays.util.LogUtils.LOGD;
import static com.thosedays.util.LogUtils.makeLogTag;

/**
 * Created by joey on 14/11/8.
 */
public class AccountUtils {
    private static final String TAG = makeLogTag(AccountUtils.class);
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "com.thosedays";
    private static final String PREF_ACTIVE_ACCOUNT = "chosen_account";

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
        if (account == null)
            return null;
        return getSharedPreferences(context).getString(makeAccountSpecificPrefKey(account.name, PREFIX_PREF_AUTH_TOKEN), null);
    }

    public static String getActiveAccountName(final Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getString(PREF_ACTIVE_ACCOUNT, null);
    }

    public static Account getActiveAccount(final Context context) {
        String account = getActiveAccountName(context);
        if (account != null) {
            return new Account(account, ACCOUNT_TYPE);
        } else {
            return null;
        }
    }

    public static boolean setActiveAccount(final Context context, final String accountName) {
        LOGD(TAG, "Set active account to: " + accountName);
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(PREF_ACTIVE_ACCOUNT, accountName).commit();
        return true;
    }

    private static String makeAccountSpecificPrefKey(String accountName, String prefix) {
        return prefix + accountName;
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
