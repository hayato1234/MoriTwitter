package com.orangesunshine.moritwitter.FollowList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.orangesunshine.moritwitter.BasicUserInfo;
import com.orangesunshine.moritwitter.LogInActivity;
import com.orangesunshine.moritwitter.TweetDataBase;
import com.orangesunshine.moritwitter.UserComparator;
import com.orangesunshine.moritwitter.UserPage;
import com.simleman.moritwitter.BuildConfig;
import com.simleman.moritwitter.R;
import com.orangesunshine.moritwitter.SNSTimeLimeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import twitter4j.PagableResponseList;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * A placeholder fragment containing a simple view.
 */
public class FollowListMainFragment extends Fragment {

    ListView listView;
    private static SharedPreferences mSharedPreferences;
    public static final String FIRST_TIME = "first";
    static final String DISABLED_LIST = "disabled_list";
    private static final String TAG = "tag";
    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;


    private ArrayList<BasicUserInfo> basicInfo;
    Map<String, Integer> tweetCount;
    private TweetDataBase tweetDataBase;
    Twitter twitter;
    private ProgressDialog progressDialog;
    SwipeRefreshLayout swipeRefreshLayout;
    SNSTimeLimeAdapter adapter;
    Set<String> disabledList;

    private int currentAccountNumber;

    AccessToken accessToken;
    ConfigurationBuilder builder;

    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_follow_list_main, container, false);
        basicInfo = new ArrayList<>();
        context = getActivity();
        tweetDataBase = new TweetDataBase(context);
        listView = (ListView) view.findViewById(R.id.follow_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BasicUserInfo user = basicInfo.get(position);
                resetUserTweetCount(user);
                Intent i = new Intent(context, UserPage.class);
                i.putExtra(UserPage.SCREEN_NAME, user.getScreenName());
                startActivity(i);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) { //do not count new tweets
                if (disabledList!=null){
                    final BasicUserInfo user = basicInfo.get(position);
                    if (disabledList.contains(user.getScreenName())){
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.ask_able_count)
                                .setPositiveButton(R.string.show, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        tweetDataBase.changeIsDisable(user.getScreenName(), currentAccountNumber);
                                        disabledList.remove(user.getScreenName());
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putStringSet(DISABLED_LIST, disabledList);
                                        editor.apply();
                                    }
                                })
                                .show();
                    }else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.ask_disable_count)
                                .setPositiveButton(R.string.stop, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        tweetDataBase.changeIsDisable(user.getScreenName(),currentAccountNumber);
                                        disabledList.add(user.getScreenName());
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putStringSet(DISABLED_LIST, disabledList);
                                        editor.apply();
                                    }
                                })
                                .show();
                    }
                }

                return true;
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.follow_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                basicInfo = new ArrayList<>();
                tweetDataBase.deleteTableData(FollowListMain.DB_TABLE_LIST[currentAccountNumber]);

                loadFollowingList(currentAccountNumber);
            }
        });
        mSharedPreferences = context.getApplicationContext().getSharedPreferences(
                LogInActivity.PREFERENCE_NAME, 0);

        currentAccountNumber = mSharedPreferences.getInt(LogInActivity.PREF_CURRENT_ACCOUNT,0);


        if (mSharedPreferences.getBoolean(FIRST_TIME, true)) {  // check if db exist
            //Log.d(TAG, "FLMainF onCreate: first time");
            disabledList = new HashSet<>();
            progressDialog = ProgressDialog.show(context,
                        "", "Loading. Please wait...", true);
            loadFollowingList(currentAccountNumber); // first time opening app
        } else {  // not first time
            if (mSharedPreferences.getStringSet(DISABLED_LIST, null) == null) { // no ones added to the list
                disabledList = new HashSet<>();
            } else {
                disabledList = mSharedPreferences.getStringSet(DISABLED_LIST, null);
            }
            setFollowingList();
        }

        return view;
    }

    private void loadFollowingList(final int accNum) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_SECRET);
                String access_token;
                String access_token_secret;

                switch (accNum){
                    case 3:
                        access_token = mSharedPreferences.getString(LogInActivity.PREF_KEY_OAUTH_TOKEN4, "");
                        access_token_secret = mSharedPreferences.getString(LogInActivity.PREF_KEY_OAUTH_SECRET4, "");
                        break;
                    case 2:
                        access_token = mSharedPreferences.getString(LogInActivity.PREF_KEY_OAUTH_TOKEN3, "");
                        access_token_secret = mSharedPreferences.getString(LogInActivity.PREF_KEY_OAUTH_SECRET3, "");
                        break;
                    case 1:
                        access_token = mSharedPreferences.getString(LogInActivity.PREF_KEY_OAUTH_TOKEN2, "");
                        access_token_secret = mSharedPreferences.getString(LogInActivity.PREF_KEY_OAUTH_SECRET2, "");
                        break;
                    default:
                        access_token = mSharedPreferences.getString(LogInActivity.PREF_KEY_OAUTH_TOKEN, "");
                        access_token_secret = mSharedPreferences.getString(LogInActivity.PREF_KEY_OAUTH_SECRET, "");
                        break;
                }

                accessToken = new AccessToken(access_token, access_token_secret);

                twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
                twitter.addRateLimitStatusListener(new RateLimitStatusListener() {
                    @Override
                    public void onRateLimitStatus(RateLimitStatusEvent rateLimitStatusEvent) {}
                    @Override
                    public void onRateLimitReached(RateLimitStatusEvent rateLimitStatusEvent) {
                        notifyUser("you've reached the limit",context);
                        cancel(true);
                    }
                });

                long cursor = -1;
                PagableResponseList<User> pageableFollowings;
                try {

                    do {
                        // long userId, long cursor, int count, boolean skipStatus, boolean includeUserEntities; 200 max,
                        pageableFollowings = twitter.getFriendsList(twitter.getId(), cursor,200,false,false);

                        Log.d("FollowListMainFragment", "sns,doInBackground: do called"+pageableFollowings.size());
                        for (User user : pageableFollowings) { //get each user
                            BasicUserInfo basicUserInfo = new BasicUserInfo();
                            basicUserInfo.setNameB(user.getName());
                            basicUserInfo.setScreenName(user.getScreenName());
                            basicUserInfo.setIconURL(user.getProfileImageURL());
                            if (tweetCount != null && tweetCount.size() != 0) { //it would be null if firstTIme, will be 0 if nothing added to tweetcount (app deleted etc)
                                // called when not first time
                                if (disabledList.contains(user.getScreenName())) { // check if disabled
                                    //disabled
                                    basicUserInfo.setDifference(0);
                                    basicUserInfo.setIsDisabled(1);
                                    basicUserInfo.setTotalTweet(user.getStatusesCount());
                                } else {
                                    if (tweetCount.get(user.getScreenName())!=null){ // sometimes null
                                        int lastTotalCount = tweetCount.get(user.getScreenName());
                                        basicUserInfo.setDifference(user.getStatusesCount() - lastTotalCount);
                                        basicUserInfo.setTotalTweet(lastTotalCount);  //put same number
                                    }
                                }

                            } else {
//                                if (disabledList.contains(user.getScreenName())) {  // need this?
//                                    basicUserInfo.setIsDisabled(0);
//                                } else {
//                                    basicUserInfo.setIsDisabled(1);
//                                }
                                basicUserInfo.setTotalTweet(user.getStatusesCount()); //firstTime
                                basicUserInfo.setDifference(0);
                            }
                            basicInfo.add(basicUserInfo);
                        }
                    } while ((cursor = pageableFollowings.getNextCursor()) != 0);
                } catch (TwitterException e) {
                    Log.d("FollowListMainFragment", "sns,doInBackground: " + e.toString());
                    if (e.isCausedByNetworkIssue()) {
                        notifyUser(getString(R.string.network_error), context);
                    } else {
                        notifyUser(getString(R.string.error), context);
                    }
                    cancel(true);
                }
                Collections.sort(basicInfo, new UserComparator());

                tweetDataBase.saveFollowings(basicInfo,currentAccountNumber);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if (mSharedPreferences.getBoolean(FIRST_TIME, true)) {
                    editor.putBoolean(FIRST_TIME, false);
                    editor.apply();
                }
                setFollowingList();
                if (swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
                dismissProgressDialog();
                super.onPostExecute(aVoid);
            }
        }.execute();
    }



    private void setFollowingList() {
        basicInfo = tweetDataBase.getFollowings(currentAccountNumber);
        setTweetCount();

        //Log.d(TAG, "FLMainF setFlist: "+currentAccountNumber);
        adapter = new SNSTimeLimeAdapter(context, basicInfo);
        listView.setAdapter(adapter);
    }

    private void setTweetCount() {
        tweetCount = new HashMap<>();
        //basicInfo = tweetDataBase.getFollowings(); //call specific user

        for (BasicUserInfo user : basicInfo) {
            tweetCount.put(user.getScreenName(), user.getTotalTweet());
        }
    }

    private void resetUserTweetCount(BasicUserInfo myUser) {
        int currentCount = myUser.getTotalTweet() + myUser.getDifference();
        myUser.setDifference(0);
        myUser.setTotalTweet(currentCount);
        tweetCount.put(myUser.getScreenName(), currentCount);
        tweetDataBase.changeTweetCount(myUser,currentAccountNumber);
        adapter.notifyDataSetChanged();
    }

    public void resetAllUserTweetCount() {
        tweetDataBase.deleteTableData(FollowListMain.DB_TABLE_LIST[currentAccountNumber]);
        for (BasicUserInfo user : basicInfo) {
            int currentCount = user.getTotalTweet() + user.getDifference();
            user.setDifference(0);
            user.setTotalTweet(currentCount);
            tweetCount.put(user.getScreenName(), currentCount);
        }
        tweetDataBase.saveFollowings(basicInfo,currentAccountNumber);
        adapter.notifyDataSetChanged();
    }


    private void notifyUser(final String error, final Context context){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
                if (swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    @Override
    public void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }
}
