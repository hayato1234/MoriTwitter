package com.simleman.moritwitter;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import static com.simleman.moritwitter.MainActivity.PREFERENCE_NAME;
import static com.simleman.moritwitter.MainActivity.PREF_KEY_OAUTH_SECRET;
import static com.simleman.moritwitter.MainActivity.PREF_KEY_OAUTH_TOKEN;
import static com.simleman.moritwitter.MainActivity.TWITTER_CONSUMER_KEY;
import static com.simleman.moritwitter.MainActivity.TWITTER_CONSUMER_SECRET;

public class TImeLine extends AppCompatActivity {

    ListView listView;
    private static SharedPreferences mSharedPreferences;
    private ArrayList<Tweet> tweets;
    private Bitmap mProfileBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mSharedPreferences = getApplicationContext().getSharedPreferences(
                PREFERENCE_NAME, 0);
        tweets = new ArrayList<>();

        listView = (ListView)findViewById(R.id.timeLineListView);
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog progressDialog;

            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(TImeLine.this,
                        "", "Loading. Please wait...", true);
            }
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    ConfigurationBuilder builder = new ConfigurationBuilder();
                    builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                    builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
                    //TLの取得
                    // Access Token
                    String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                    // Access Token Secret
                    String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

                    AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                    Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                    ResponseList<twitter4j.Status> homeTl = twitter.getHomeTimeline(); // loading each tweet and put in array

                    for (twitter4j.Status status : homeTl) { // taking one tweet from the array
                        Tweet tweet1 = new Tweet(); //tweet object
                        //getting all media (pics, gif, video)
                        MediaEntity[] mediaEntities = status.getExtendedMediaEntities();
                        //つぶやきのユーザーIDの取得
                        String userName = status.getUser().getScreenName();
                        //つぶやきの取得
                        String tweet = status.getText();
                        String profilePictureURL = status.getUser().getProfileImageURL();

                        if (mediaEntities.length >0){ //if there are media
                            ArrayList<String> mediaURLs = new ArrayList<String>();
                            for (int i = 0;i<mediaEntities.length;i++){
                                MediaEntity mediaEntity = mediaEntities[i]; // get each medium
                                mediaURLs.add(mediaEntity.getMediaURL());
                            }
                            tweet1.setMedia_images(mediaURLs);
                        }
                        tweet1.setUser(userName);
                        tweet1.setContent(tweet);
                        tweet1.setProf_image(profilePictureURL);
                        tweets.add(tweet1);
                    }

                } catch (TwitterException e) {
                    e.printStackTrace();
                    if(e.isCausedByNetworkIssue()){
                        Toast.makeText(getApplicationContext(), "ネットワークに接続して下さい", Toast.LENGTH_LONG);
                    }else{
                        Toast.makeText(getApplicationContext(), "エラーが発生しました。", Toast.LENGTH_LONG);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                TweetListAdaptor adapter = new TweetListAdaptor(getBaseContext(),tweets);
                listView.setAdapter(adapter);
                progressDialog.dismiss();
                super.onPostExecute(aVoid);
            }
        }.execute();
    }
}
