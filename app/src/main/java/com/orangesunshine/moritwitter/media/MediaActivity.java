package com.orangesunshine.moritwitter.media;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.orangesunshine.moritwitter.ShowMedia.ShowMediaActivity;
import com.simleman.moritwitter.BuildConfig;
import com.simleman.moritwitter.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import static com.orangesunshine.moritwitter.LogInActivity.PREFERENCE_NAME;
import static com.orangesunshine.moritwitter.LogInActivity.PREF_CURRENT_ACCOUNT;


public class MediaActivity extends AppCompatActivity implements MediaListAdapter.OnImageClickListener {

    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;
    public static final String SCREEN_NAME = "flabbleruhgi";
    private static final String TAG = "tag";

    @BindView(R.id.media_list_view)
    ListView listView;
    private ProgressDialog progressDialog;

    MediaListAdapter adapter;
    private List<String> imageURLs;
    private List<String> tweetTexts;
    Context context;
    private int loadCount; // how many times tweets loaded
    private boolean loading;
    private int accountNumber;
    private String screenName;
    Twitter twitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());

        screenName = getIntent().getExtras().getString(SCREEN_NAME);


        imageURLs = new ArrayList<>();
        tweetTexts = new ArrayList<>();
        context = this;
        adapter = new MediaListAdapter(this);
        adapter.setOnImageClickListener(this);
        listView.setAdapter(adapter);

        initializeTwitter();
    }

    private void initializeTwitter() {
        SharedPreferences mSharedPreferences = context.getApplicationContext().getSharedPreferences(
                PREFERENCE_NAME, 0);
        accountNumber = mSharedPreferences.getInt(PREF_CURRENT_ACCOUNT, 0);
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_SECRET);
        builder.setTweetModeExtended(true); // so that text not cut off
        String access_token = mSharedPreferences.getString(FollowListMain.TOKEN_LIST[accountNumber], "");
        String access_token_secret = mSharedPreferences.getString(FollowListMain.SECRET_LIST[accountNumber], "");
        AccessToken accessToken = new AccessToken(access_token, access_token_secret);
        twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
        loadUserMedia();
    }

    private void loadUserMedia(){

        new AsyncTask<Void, Void, Void>() {

            boolean noMoreTweet;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (loadCount<1){
                    progressDialog = ProgressDialog.show(MediaActivity.this,
                            "", getString(R.string.loading_massage), true);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Paging pg = new Paging(++loadCount,100);
                    ResponseList<twitter4j.Status> userTimeline = twitter.getUserTimeline(screenName,pg);
                    Timber.d(userTimeline.get(0).getText());
                    for (twitter4j.Status status : userTimeline){ //for each tweet
                        if (status.getMediaEntities().length>0){ // if includes media
                            for (MediaEntity media:status.getMediaEntities()){ //for each media
                                if (media.getType()!=null&media.getType().equals("photo")){ //if media type is photo
                                    imageURLs.add(media.getMediaURL()); //add to list
                                    tweetTexts.add(status.getText());
                                }
                            }
                        }
                    }
                    if (userTimeline.size()<100){
                        noMoreTweet = true;
                    }
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                adapter.swapData(imageURLs,tweetTexts);
                dismissProgressDialog();
                if (!noMoreTweet&&imageURLs.size()<5){
//                    Timber.d("more tweets to load");
                    loadUserMedia();
                }
//                Timber.d("no more tweet");
            }
        }.execute();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void imageClicked(int position) {
        String[] imageURL = new String[]{imageURLs.get(position)};
        Intent intent = new Intent(this, ShowMediaActivity.class);
        intent.putExtra(ShowMediaActivity.MEDIA_POSITION,imageURL);
        startActivity(intent);
    }
}
