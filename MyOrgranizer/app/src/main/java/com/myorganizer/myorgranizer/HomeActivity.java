package com.myorganizer.myorgranizer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class HomeActivity extends Activity
{
    private static final String CLIENT_ID = "772912633150-i2n10flv608mt1clc2dikfktf9spdpqu.apps.googleusercontent.com";
    private static final String USERPROFILE= "https://www.googleapis.com/auth/userinfo.profile";
    private final static String GPLUS_SCOPE
        = "https://www.googleapis.com/auth/userinfo.email";
//    private static final String SCOPE = "audience:server:client_id:" + CLIENT_ID;
    private static final String SCOPE = "oauth2:" + USERPROFILE + " " + GPLUS_SCOPE;

    private static final int AUTH_REQUEST_CODE = 1;
    private Account mAccount;
    private Activity mActivity;

    protected void onCreate(Bundle savedInstanceState) {
        mActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAccount = AccountManager.get(mActivity).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)[0];
        new GetAuthToken().execute(mAccount.name);
    }

    public void clicker(){
        new GetAuthToken().execute(mAccount.name);
    }

    protected void log(String msg) {
//        TextView tv = (TextView) mActivity.findViewById(R.id.textView);
//        tv.setText(tv.getText() + "\n" + msg);
        Log.e("test", msg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                new GetAuthToken().execute(mAccount.name);
            }
        }
    }

    private class GetAuthToken extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                // Retrieve a token for the given account and scope. It will always return either
                // a non-empty String or throw an exception.
                String email = params[0];
                Log.e("test", email);
                String token = GoogleAuthUtil.getToken(mActivity, email, SCOPE);
                return token;
            } catch (GooglePlayServicesAvailabilityException playEx) {
                Dialog alert = GooglePlayServicesUtil.getErrorDialog(playEx.getConnectionStatusCode(), mActivity, AUTH_REQUEST_CODE);
                return "error - Play Services needed " + playEx;
            } catch (UserRecoverableAuthException userAuthEx) {
                // Start the user recoverable action using the intent returned by
                // getIntent()
                mActivity.startActivityForResult(userAuthEx.getIntent(), AUTH_REQUEST_CODE);
                return "error - Autorization needed " + userAuthEx;
            } catch (IOException transientEx) {
                // network or server error, the call is expected to succeed if you try again later.
                // Don't attempt to call again immediately - the request is likely to
                // fail, you'll hit quotas or back-off.
                return "error - Network error " + transientEx;
            } catch (GoogleAuthException authEx) {
                // Failure. The call is not expected to ever succeed so it should not be
                // retried.
                return "error - Other auth error " + authEx;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.startsWith("error -")) {
                log(result);
            } else {
                log("Obtained token : " + result);
                new GetAuthedUserName().execute(result);
            }
        }
    }

    private class GetAuthedUserName extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String token = params[0];
                URL url = new URL("https://organizer-python-758.appspot.com/api/user");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization",  token);
//                conn.setRequestProperty("Authorization", "Bearer " + token);
//                conn.addRequestProperty("Authorization",  "OAuth " + token);
                InputStream istream = conn.getInputStream();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    return sb.toString();
                } catch (IOException e) {
                    return "error - Unable to read from the connection";
                }
            } catch (MalformedURLException e) {
                return "error - Malformed URL " + e;
            } catch (IOException e) {
                return "error - IO error " + e;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.startsWith("error -")) {
                log(result);
            } else {
                log("Request result : " + result);
            }
        }
    }
}