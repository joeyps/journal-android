package joey.thosedays;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.facebook.Session;
import com.facebook.SessionState;
import com.google.gson.Gson;
import com.thosedays.util.AccountUtils;
import com.thosedays.util.Worker;
import com.thosedays.model.AuthToken;
import com.thosedays.model.User;
import com.thosedays.sync.Config;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;

import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by joey on 14/11/5.
 */
public class WelcomeActivity extends Activity {

    private static String LOG_TAG = WelcomeActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Account account = getAccountIfExists();
        if (account != null) {
            launchMainActivity(account);
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);
        Bitmap bmpCover = BitmapFactory.decodeResource(getResources(), R.drawable.cover);
        findViewById(R.id.layout_content).setBackground(new CoverBitmapDrawable(getResources(), bmpCover));
        findViewById(R.id.button_fb_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithFacebook();
            }
        });
        printKeyHash();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    private void loginWithFacebook() {
        // start Facebook Login
        Session.openActiveSession(WelcomeActivity.this, true, new Session.StatusCallback() {

            // callback when session changes state
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {
                    final String token = session.getAccessToken();
                    Worker.get().post(new Runnable() {
                        @Override
                        public void run() {
                            Account account = authViaFb(token);
                            if (account != null) {
                                launchMainActivity(account);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }

    private void launchMainActivity(Account account) {
//        Toast.makeText(WelcomeActivity.this, "Hello " + user.getName() + "!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setClass(WelcomeActivity.this, ThoseDaysActivity.class);
        intent.putExtra(Config.EXTRA_ACCOUNT, account);
        startActivity(intent);
    }

    private Account authViaFb(String accessToken) {
        BasicHttpClient httpClient = new BasicHttpClient();
        HttpResponse response = httpClient.get(Config.AUTH_URL + "/fb?access_token=" + accessToken, null);
        if (response == null) {
            //LOGE(TAG, "Request for manifest returned null response.");
            //throw new IOException("Request for data manifest returned null response.");
        }

        int status = response.getStatus();
        if (status == HttpURLConnection.HTTP_OK) {
            String json = response.getBodyAsString();
            AuthToken authToken = new Gson().fromJson(json, AuthToken.class);
            User user = requestMe(authToken.token);
            if (user != null) {
                return createSyncAccount(this, user.id, authToken.token);
            }
        }
        return null;
    }

    private User requestMe(String token) {
        BasicHttpClient httpClient = new BasicHttpClient();
        httpClient.addHeader(Config.HEADER_AUTHORIZATION, "access_token=" + token);
        HttpResponse response = httpClient.get(Config.API_URL + "/me", null);
        if (response == null) {
            //LOGE(TAG, "Request for manifest returned null response.");
            //throw new IOException("Request for data manifest returned null response.");
        }

        int status = response.getStatus();
        if (status == HttpURLConnection.HTTP_OK) {
            String json = response.getBodyAsString();
            User user = new Gson().fromJson(json, User.class);
            Log.d("joey", "user=" + user.id + " name=" + user.name);
            return user;
        }
        return null;
    }

    private Account getAccountIfExists() {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) getSystemService(
                        ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByType(Config.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            Log.d(LOG_TAG, "loaded account " + accounts[0].name);
            return accounts[0];
        }
        return null;
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    private Account createSyncAccount(Context context, String accountName, String token) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                accountName, Config.ACCOUNT_TYPE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            AccountUtils.setAuthToken(this, newAccount, Config.AUTH_TYPE, token);
            Log.d(LOG_TAG, "added account " + newAccount.name);
            return newAccount;
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            return null;
        }
    }

    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "joey.thosedays",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
