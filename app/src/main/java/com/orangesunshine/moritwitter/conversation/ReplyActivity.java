package com.orangesunshine.moritwitter.conversation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.simleman.moritwitter.R;
import com.orangesunshine.moritwitter.Tweet;
import com.orangesunshine.moritwitter.UserPage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import static com.simleman.moritwitter.BuildConfig.TWITTER_KEY;
import static com.simleman.moritwitter.BuildConfig.TWITTER_SECRET;
import static com.orangesunshine.moritwitter.LogInActivity.PREFERENCE_NAME;
import static com.orangesunshine.moritwitter.LogInActivity.PREF_CURRENT_ACCOUNT;
import static com.orangesunshine.moritwitter.UserPage.SCREEN_NAME;

public class ReplyActivity extends AppCompatActivity implements ReplyActivityListAdapter.OnReplyIconClickListener {

    public static final String TWEET_STATUS = "e3qff29iiS";

    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    EditText editText;
    Button replyButton;

    private int accountNumber;
    Twitter twitter;

    Status status;
    Tweet mainTweet;
    List<Tweet> statusList;
    ReplyActivityListAdapter adapter;
    private String a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        if(getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        statusList = new ArrayList<>();
        mainTweet = new Tweet();
        sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCE_NAME, 0);
        status = (Status) getIntent().getSerializableExtra(TWEET_STATUS);
        mainTweet.setUser(status.getUser().getName());
        mainTweet.setScreenName(status.getUser().getScreenName());
        mainTweet.setTime(status.getCreatedAt());
        mainTweet.setContent(status.getText());
        mainTweet.setProf_image(status.getUser().getBiggerProfileImageURL());
        mainTweet.setTweetId(status.getId());

        accountNumber = sharedPreferences.getInt(PREF_CURRENT_ACCOUNT,0);

        statusList.add(mainTweet);

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_SECRET);
        builder.setTweetModeExtended(true); // so that text not cut off
        String access_token = sharedPreferences.getString(FollowListMain.TOKEN_LIST[accountNumber], "");
        String access_token_secret = sharedPreferences.getString(FollowListMain.SECRET_LIST[accountNumber], "");
        AccessToken accessToken = new AccessToken(access_token, access_token_secret);
        twitter = new TwitterFactory(builder.build()).getInstance(accessToken);


        recyclerView = (RecyclerView) findViewById(R.id.reply_recycler_view);
        editText = (EditText) findViewById(R.id.reply_input);
        replyButton = (Button) findViewById(R.id.reply_tweet_button);

        adapter = new ReplyActivityListAdapter(statusList, this);
        adapter.setOnIconClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        a = "@" + status.getUser().getScreenName() + " ";
        editText.setText(a);
        editText.setSelection(editText.getText().length());

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        loadConversation();
        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String replyText = editText.getText().toString();
                StatusUpdate statusUpdate = new StatusUpdate(replyText);
                statusUpdate.setInReplyToStatusId(status.getId());
                reply(statusUpdate, replyText);
                editText.setText(a);
            }
        });
    }

    private void reply(final StatusUpdate update, final String replyText) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    twitter.updateStatus(update);
                } catch (TwitterException e) {
                    e.printStackTrace();
                    if (e.getStatusCode() == 403) {
                        notifyUser(getString(R.string.erro_duplicate_tweet));
                    } else {
                        notifyUser(getString(R.string.error));
                    }
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                makeNewTweet(replyText);
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void makeNewTweet(String text) {
        Tweet postedTweet = new Tweet();
        postedTweet.setUser(sharedPreferences.getString(FollowListMain.USER_NAME_LIST[accountNumber], ""));
        postedTweet.setScreenName(sharedPreferences.getString(FollowListMain.USER_NAME_LIST[accountNumber], ""));
        postedTweet.setProf_image(sharedPreferences.getString(FollowListMain.ICON_LIST[accountNumber], ""));
        postedTweet.setTime(Calendar.getInstance().getTime());
        postedTweet.setContent(text);
        //statusList.add(postedTweet);
        adapter.addItem(postedTweet);
//        adapter = new ReplyActivityListAdapter(statusList,this);
//        recyclerView.setAdapter(adapter);
        // Log.d("tag", "makeNewTweet: "+recyclerView.getChildCount());
    }

    private void loadConversation() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    twitter4j.Status s = twitter.showStatus(statusList.get(0).getTweetId());
                    //Log.d("tag", "ReplyA doInBackground: trying"+ s.getInReplyToScreenName());
                    if (s.getInReplyToStatusId()>0){ // means this is reply
                        //Log.d("tag", "ReplyA doInBackground: is true "+ s.getInReplyToStatusId());
                        twitter4j.Status repliedS = twitter.showStatus(s.getInReplyToStatusId());
                        Tweet otherTweet = new Tweet();
                        otherTweet.setUser(repliedS.getUser().getName());
                        otherTweet.setScreenName(repliedS.getUser().getScreenName());
                        otherTweet.setProf_image(repliedS.getUser().getBiggerProfileImageURL());
                        otherTweet.setTime(repliedS.getCreatedAt());
                        otherTweet.setContent(repliedS.getText());
                        otherTweet.setTweetId(repliedS.getId());
                        statusList.add(0,otherTweet);
                        return true;
                    }
                } catch (TwitterException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean){
                    loadConversation();
                }
                adapter.notifyDataSetChanged();
                super.onPostExecute(aBoolean);
            }
        }.execute();
    }

    private void notifyUser(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ReplyActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            //startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void userIconClicked(int position) {
        Intent i = new Intent(this, UserPage.class);
        i.putExtra(SCREEN_NAME, statusList.get(position).getScreenName());
        startActivity(i);
    }
}
