package com.orangesunshine.moritwitter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.orangesunshine.moritwitter.FollowList.FollowListMainFragment;
import com.simleman.moritwitter.R;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class LogInActivity extends AppCompatActivity {

//    public final static String TWITTER_CONSUMER_KEY = "OOPek21f1SbV3xr2tSHi1m0J1";
//    public final static String TWITTER_CONSUMER_SECRET = "WgYXSF9ZaFNOE6RXcsdSe3AAFdL6k0zr29jSGIfeh3WIxlOv9X";
    //static final String TWITTER_CALLBACK_URL = "oauth://callback";

    public static String PREFERENCE_NAME = "vRyd370KE4";
    public static final String PREF_KEY_OAUTH_TOKEN = "kJRo1truUy";
    public static final String PREF_KEY_OAUTH_SECRET = "ObVKkjsl5Q";
    public static final String PREF_KEY_TWITTER_LOGIN = "gDvupvZKI1";
    public static final String PREF_USER_NAME = "fjrRm9Q6Af";
    public static final String PREF_PROFILE_IMAGE = "EQEltvvhPj";

    public static final String PREF_KEY_OAUTH_TOKEN2 = "lvvcy1Nai2";
    public static final String PREF_KEY_OAUTH_SECRET2 = "5ARZfShQsq";
    public static final String PREF_KEY_TWITTER_LOGIN2 = "TwTq9sjZvc";
    public static final String PREF_USER_NAME2 = "W0OnHpj83C";
    public static final String PREF_PROFILE_IMAGE2 = "LsHw7MrpWz";

    public static final String PREF_KEY_OAUTH_TOKEN3 = "hH4QAhf5tU";
    public static final String PREF_KEY_OAUTH_SECRET3 = "dDpx2QS8Vv";
    public static final String PREF_KEY_TWITTER_LOGIN3 = "nJ8gQzZw8T";
    public static final String PREF_USER_NAME3 = "khrDSmI23D";
    public static final String PREF_PROFILE_IMAGE3 = "xEdtfK0KAM";

    public static final String PREF_KEY_OAUTH_TOKEN4 = "tMNQac0JHs";
    public static final String PREF_KEY_OAUTH_SECRET4 = "9hJjRCzTK4";
    public static final String PREF_KEY_TWITTER_LOGIN4 = "0lpGlHyZyp";
    public static final String PREF_USER_NAME4 = "91qJiOZ8Qa";
    public static final String PREF_PROFILE_IMAGE4 = "4o2hrySSES";

    public static final String PREF_ACCOUNT_COUNT = "UWJjbDPuu4";
    public static final String PREF_ACCOUNT_NUMBER = "ULnj9lcWqf";
    public static final String PREF_CURRENT_ACCOUNT = "IbcKYa3tSX";

    public static final String PREF_KEY_AT_LEAST_ONE_LOGIN = "yo7FbXEjPG";


    private static final String TWITTER_KEY = com.simleman.moritwitter.BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = com.simleman.moritwitter.BuildConfig.TWITTER_SECRET;

    ConfigurationBuilder builder;
    Configuration configuration;
    SharedPreferences sharedPreferences;
    Twitter twitter;

    private TwitterLoginButton loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getApplicationContext().getSharedPreferences(
                PREFERENCE_NAME, 0);


        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                saveTwitterInfo(result);
               }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });
    }

    void saveTwitterInfo(Result<TwitterSession> result){

        final int accountCount = sharedPreferences.getInt(PREF_ACCOUNT_COUNT,0);


        final TwitterSession session = result.data;
        TwitterAuthToken authToken = session.getAuthToken();
        final String token = authToken.token;
        final String secret = authToken.secret;
        AccessToken accessToken = new AccessToken(token,secret);

        builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_SECRET);
        configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance(accessToken);

        Log.d("tag", "LoginAct saveTwitterInfo: "+accountCount);

        if (!sharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN,false)){
            new AsyncTask<Void,Void,Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        User user = twitter.showUser(session.getUserId());
                        String username = user.getScreenName();
                        String profilePicture = user.getOriginalProfileImageURL();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(PREF_KEY_AT_LEAST_ONE_LOGIN,true);
                        editor.putString(PREF_KEY_OAUTH_TOKEN, token);
                        editor.putString(PREF_KEY_OAUTH_SECRET, secret);
                        editor.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                        editor.putString(PREF_USER_NAME, username);
                        editor.putString(PREF_PROFILE_IMAGE, profilePicture);
                        editor.putInt(PREF_ACCOUNT_COUNT,1);
                        editor.putInt(PREF_CURRENT_ACCOUNT,0);
                        editor.apply();
//                        Intent i = new Intent(LogInActivity.this, FollowListMain.class);
//                        i.putExtra(PREF_ACCOUNT_NUMBER,0);
                        startActivity(new Intent(LogInActivity.this, FollowListMain.class));
                    } catch (twitter4j.TwitterException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        }else if (!sharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN2,false)){
            new AsyncTask<Void,Void,Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        User user = twitter.showUser(session.getUserId());
                        String username = user.getScreenName();
                        String profilePicture = user.getOriginalProfileImageURL();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(PREF_KEY_AT_LEAST_ONE_LOGIN,true);
                        editor.putString(PREF_KEY_OAUTH_TOKEN2, token);
                        editor.putString(PREF_KEY_OAUTH_SECRET2, secret);
                        editor.putBoolean(PREF_KEY_TWITTER_LOGIN2, true);
                        editor.putString(PREF_USER_NAME2, username);
                        editor.putString(PREF_PROFILE_IMAGE2, profilePicture);
                        editor.putInt(PREF_ACCOUNT_COUNT,2);
                        editor.putInt(PREF_CURRENT_ACCOUNT,1);
                        editor.putBoolean(FollowListMainFragment.FIRST_TIME,true);
                        editor.apply();
//                        Intent i = new Intent(LogInActivity.this, FollowListMain.class);
//                        i.putExtra(PREF_ACCOUNT_NUMBER,1);
                        startActivity(new Intent(LogInActivity.this, FollowListMain.class));
                    } catch (twitter4j.TwitterException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        }else if (!sharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN3,false)){
            new AsyncTask<Void,Void,Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        User user = twitter.showUser(session.getUserId());
                        String username = user.getScreenName();
                        String profilePicture = user.getOriginalProfileImageURL();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(PREF_KEY_AT_LEAST_ONE_LOGIN,true);
                        editor.putString(PREF_KEY_OAUTH_TOKEN3, token);
                        editor.putString(PREF_KEY_OAUTH_SECRET3, secret);
                        editor.putBoolean(PREF_KEY_TWITTER_LOGIN3, true);
                        editor.putString(PREF_USER_NAME3, username);
                        editor.putString(PREF_PROFILE_IMAGE3, profilePicture);
                        editor.putInt(PREF_ACCOUNT_COUNT,3);
                        editor.putInt(PREF_CURRENT_ACCOUNT,2);
                        editor.putBoolean(FollowListMainFragment.FIRST_TIME,true);
                        editor.apply();
                        startActivity(new Intent(LogInActivity.this, FollowListMain.class));
                    } catch (twitter4j.TwitterException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        }else {
            new AsyncTask<Void,Void,Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        User user = twitter.showUser(session.getUserId());
                        String username = user.getScreenName();
                        String profilePicture = user.getOriginalProfileImageURL();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(PREF_KEY_AT_LEAST_ONE_LOGIN,true);
                        editor.putString(PREF_KEY_OAUTH_TOKEN4, token);
                        editor.putString(PREF_KEY_OAUTH_SECRET4, secret);
                        editor.putBoolean(PREF_KEY_TWITTER_LOGIN4, true);
                        editor.putString(PREF_USER_NAME4, username);
                        editor.putString(PREF_PROFILE_IMAGE4, profilePicture);
                        editor.putInt(PREF_ACCOUNT_COUNT,4);
                        editor.putInt(PREF_CURRENT_ACCOUNT,3);
                        editor.putBoolean(FollowListMainFragment.FIRST_TIME,true);
                        editor.apply();
                        startActivity(new Intent(LogInActivity.this, FollowListMain.class));
                    } catch (twitter4j.TwitterException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure that the loginButton hears the result from any
        // Activity that it triggered.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

}
