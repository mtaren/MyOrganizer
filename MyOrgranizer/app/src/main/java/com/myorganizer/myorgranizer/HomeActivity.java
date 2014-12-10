package com.myorganizer.myorgranizer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class HomeActivity extends FragmentActivity implements HomeFragment.OnFragmentInteractionListener, NotificationFragment.OnFragmentInteractionListener
        ,searchFragment.OnFragmentInteractionListener, ViewPagerFragment.OnFragmentInteractionListener, addObjectFragment.OnFragmentInteractionListener, ItemFragment.OnFragmentInteractionListener
    ,CABCallbacks.OnFragmentInteractionListener, ContainerFragment.OnFragmentInteractionListener


{   //test
    private static final String CLIENT_ID = "772912633150-i2n10flv608mt1clc2dikfktf9spdpqu.apps.googleusercontent.com";
    private static final String USERPROFILE = "https://www.googleapis.com/auth/userinfo.profile";
    private final static String GPLUS_SCOPE
            = "https://www.googleapis.com/auth/userinfo.email";
    //    private static final String SCOPE = "audience:server:client_id:" + CLIENT_ID;
    private static final String SCOPE = "oauth2:" + USERPROFILE + " " + GPLUS_SCOPE;

    public static String domain;
//    private static final String addr = "/";
    private AsyncHttpClient httpClient = new AsyncHttpClient();
    private static String mToken;

    private static final int AUTH_REQUEST_CODE = 1;
    private Account mAccount;
    private Activity mActivity;


    private ViewPagerFragment mViewPagerFrag; // for turning pages on viewpager
    private HomeFragment mHomeFragment = null;
    private boolean mRedrawGrid;


    protected void onCreate(Bundle savedInstanceState) {


//        if(savedInstanceState == null){
            mActivity = this;
            domain = this.getString(R.string.domain);
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(false);
            super.onCreate(savedInstanceState);
    //        setContentView(R.layout.activity_home); //moved to after the token is reached
            mAccount = AccountManager.get(mActivity).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)[0];
            new GetAuthToken().execute(mAccount.name);

//        }else {
//            super.onCreate(savedInstanceState);
//        }

//
    }

    public void StartApp() {
        this.setContentView(R.layout.activity_home);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//            Fragment fr = new ViewStreamsFragment(); // change this to login

        mViewPagerFrag = new ViewPagerFragment();
//        Fragment fr = new ItemFragment();
        transaction.replace(R.id.container, mViewPagerFrag);
        transaction.addToBackStack(null);
        transaction.commit();


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
                new GetAuthToken().execute(mAccount.name); // this is an async task to do the http request
            }
        }
    }

    //Mlistener overrides TODO implement theses
    @Override
    public String getToken() {
        return mToken;
    }

    @Override
    public boolean isViewPagerHidden(){
        return mViewPagerFrag.isHidden();
    }

    @Override
    public void onBackPressed()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        if (mViewPagerFrag.isHidden()) {
            Log.e("test","hidden");
            Fragment currF = (Fragment)getFragmentManager().findFragmentByTag("newWindow");
            ft.remove(currF);
            ft.show(mViewPagerFrag);
            ft.commit();
            if(mRedrawGrid){
                mHomeFragment.onActivityResult(HomeFragment.REDRAWGRID, 0, null);
            }

        } else {
            Log.e("test","not");
            super.onBackPressed();
        }

    }

    @Override
    public void GoBack(){
        onBackPressed();
    }

    @Override
    public void ChangePage(int i){
        mViewPagerFrag.mViewPager.setCurrentItem(i);
    }

    @Override
    public void ChangeFragment(Fragment f, boolean backstack) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();


        if(backstack) {
            Log.e("test","added");
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.container, f);
        transaction.commit();
    }

    @Override
    public void ShowFragment(Fragment f) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.hide(mViewPagerFrag);
        transaction.add(R.id.container, f, "newWindow");
        transaction.commit();
    }

    @Override
    public void SetHomeFragment(HomeFragment f) {
        mHomeFragment = f;
    }
    @Override
    public void unSetHomeFragment() {
        mHomeFragment = null;
    }
    @Override
    public void RedrawGridOnShow(){
        mRedrawGrid = true;
    }

    @Override
    public void HomeFragmentGetContainer(String containerId) {
        Intent i = new Intent();
        i.putExtra("test", containerId);
        if(mHomeFragment != null) {
            mHomeFragment.onActivityResult(HomeFragment.SEARCHTOHOME,10,i);
        }else{
            Log.e("homeActivity", "homefragment is null on homefraggetcontainer mlistener call" );
        }
    }



//    @Override
//    public void BackOneFragment(){
//        FragmentManager fm = getFragmentManager();
//        fm.popBackStack();
//    }


    @Override
    public void PerformRequestPost(String addr, RequestParams params,AsyncHttpResponseHandler handler ) {
        httpClient.addHeader("Authorization", mToken);
        httpClient.post(domain + addr, params, handler);
    }

    public void PerformRequestGet(String addr, AsyncHttpResponseHandler handler ) {
        httpClient.addHeader("Authorization", mToken);
        httpClient.get(domain + addr, handler);
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
                mToken = token;
                Log.e("main_default",mToken);
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
//               new OnLoggedIn().execute(result);
                StartApp();


            }
        }
    }

}
