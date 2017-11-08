package com.orangesunshine.moritwitter.FollowList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.orangesunshine.moritwitter.LogInActivity;
import com.simleman.moritwitter.BuildConfig;
import com.simleman.moritwitter.R;
import com.orangesunshine.moritwitter.UserPage;

import twitter4j.PagableResponseList;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class FollowListFromUserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private static SharedPreferences mSharedPreferences;
    private static final String TAG = "tag";
    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;
    public static final String FROM_SCREEN_NAME = "fjkhabuervaflu";
    public static final String FROM_NAME = "lajsrviqwebriewu";
    public static final String FROM_IS_FOLLOWER = "fiaehofjhaedfe";


    private Twitter twitter;
    private ProgressDialog progressDialog;
    private FollowListFromUserActivityAdapter adapter;
    private PagableResponseList<User> pageableFollowings;

    private AccessToken accessToken;
    private ConfigurationBuilder builder;

    private Context context;
    private String screenName;
    private String name;
    private boolean isFollower;
    private long cursor = -1;
    private int itemCount;
    private boolean loading;
    private boolean hasNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list_from_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        context = this;
        screenName = getIntent().getStringExtra(FROM_SCREEN_NAME);
        name = getIntent().getStringExtra(FROM_NAME);
        isFollower = getIntent().getBooleanExtra(FROM_IS_FOLLOWER, true);

        if (getSupportActionBar()!=null){
            if (isFollower) {
                getSupportActionBar().setTitle("Followers: " + name);
            } else {
                getSupportActionBar().setTitle("Following: " + name);
            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.follow_user_recycler);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean listHasValue = itemCount > 0;
                if (hasNext&& !loading && listHasValue ){ // load only if more people available
                    int visibleItemCount = recyclerView.getChildCount();
                    LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int firstVisibleItem = manager.findFirstVisibleItemPosition();
                    boolean atEnd = firstVisibleItem + visibleItemCount >= itemCount;
                    if (atEnd) { // load more tweets if at the end of listview
                        loading = true;
                        loadMoreFollowList();
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mSharedPreferences = context.getApplicationContext().getSharedPreferences(
                LogInActivity.PREFERENCE_NAME, 0);

        loadFollowingList();
    }

    private void loadFollowingList() {

        new AsyncTask<Void, Void, Void>() {

            protected void onPreExecute() {
//                progressDialog = ProgressDialog.show(context,
//                        "", "Loading. Please wait...", true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_SECRET);
                String access_token = mSharedPreferences.getString(LogInActivity.PREF_KEY_OAUTH_TOKEN, "");
                String access_token_secret = mSharedPreferences.getString(LogInActivity.PREF_KEY_OAUTH_SECRET, "");
                accessToken = new AccessToken(access_token, access_token_secret);

                twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                twitter.addRateLimitStatusListener(new RateLimitStatusListener() {
                    @Override
                    public void onRateLimitStatus(RateLimitStatusEvent rateLimitStatusEvent) {
                    }

                    @Override
                    public void onRateLimitReached(RateLimitStatusEvent rateLimitStatusEvent) {
                        FollowListFromUserActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(FollowListFromUserActivity.this, "Too many requests are sent, please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        cancel(true);
                    }
                });

                try {
                    if (isFollower){
                        pageableFollowings = twitter.getFollowersList(screenName, cursor, 30);
                    }else {
                        pageableFollowings = twitter.getFriendsList(screenName, cursor, 30);
                    }
                    hasNext = pageableFollowings.hasNext();
                } catch (TwitterException e) {
                    Log.d("tag", "sns,doInBackground: " + e.toString());
                    if (e.isCausedByNetworkIssue()) {
                        notifyUser(getString(R.string.network_error), context);
                        cancel(true);
                    } else {
                        notifyUser(getString(R.string.error), context);
                    }
                    cancel(true);
                }
                itemCount = pageableFollowings.size();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                cursor = pageableFollowings.getNextCursor();
                loading = false;
                setFollowingList();
                super.onPostExecute(aVoid);
            }
        }.execute();
    }
    private PagableResponseList<User> newList;
    private void loadMoreFollowList(){
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int start = pageableFollowings.size();
                try {
                    if (isFollower){
                        newList = twitter.getFollowersList(screenName, cursor, 30);
                    }else {
                        newList = twitter.getFriendsList(screenName, cursor, 30);
                    }
                    hasNext = newList.hasNext();
                    pageableFollowings.addAll(newList);

                    itemCount=pageableFollowings.size();
                } catch (TwitterException e) {
                    Log.d("tag", "sns,doInBackground: " + e.toString());
                    if (e.isCausedByNetworkIssue()) {
                        notifyUser(getString(R.string.network_error), context);
                        cancel(true);
                    } else {
                        notifyUser(getString(R.string.warning_private_account), context);
                    }
                    cancel(true);
                }
                return start;
            }
            @Override
            protected void onPostExecute(Integer start) {

                cursor = newList.getNextCursor();
                adapter.notifyItemRangeChanged(start,pageableFollowings.size()-start);
                loading = false;
                super.onPostExecute(start);
            }
        }.execute();
    }


    private void setFollowingList() {

        adapter = new FollowListFromUserActivityAdapter(context, pageableFollowings);
        adapter.setItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                Intent i = new Intent(context, UserPage.class);
                i.putExtra(UserPage.SCREEN_NAME, pageableFollowings.get(position).getScreenName());
                startActivity(i);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void notifyUser(final String error, final Context context){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
