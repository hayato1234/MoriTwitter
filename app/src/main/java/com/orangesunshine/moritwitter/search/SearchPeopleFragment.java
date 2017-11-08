package com.orangesunshine.moritwitter.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.orangesunshine.moritwitter.TweetDetailActivity;
import com.orangesunshine.moritwitter.UserPage;
import com.simleman.moritwitter.BuildConfig;
import com.simleman.moritwitter.R;
import com.orangesunshine.moritwitter.search.dummy.DummyContent;
import com.orangesunshine.moritwitter.search.dummy.DummyContent.DummyItem;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import static com.orangesunshine.moritwitter.LogInActivity.PREFERENCE_NAME;
import static com.orangesunshine.moritwitter.LogInActivity.PREF_CURRENT_ACCOUNT;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SearchPeopleFragment extends Fragment implements SearchTweetAdapter.STIconClickListener, SearchTweetAdapter.STTweetActionListener, SearchTweetAdapter.STItemClickListener, SearchPeopleRecyclerViewAdapter.OnUserSelectedListener {

    // TODO: Customize parameters
    private int mColumnCount = 1;
    private Context context;
    private static SharedPreferences mSharedPreferences;
    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;
    private static final String OPTION = "jlagkjdhgfilaefa";
    Twitter twitter;


    private int accountNumber;
    private int optionNumber;
    private ResponseList<User> userList;
    private List<Status> tweetList;
    private View view;
    private ProgressDialog progressDialog;

    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SearchPeopleFragment() {
    }

    public static SearchPeopleFragment newIntstance(int searchOption) {
        SearchPeopleFragment fragment = new SearchPeopleFragment();
        Bundle args = new Bundle();
        args.putInt(OPTION, searchOption);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        optionNumber = getArguments().getInt(OPTION, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_people_item_list, container, false);

        context = getActivity();
        mSharedPreferences = context.getApplicationContext().getSharedPreferences(
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

        if (optionNumber == 0) {
            loadUserSuggestions();
        } else {
            loadTweetSuggestions();
        }
        return view;
    }

    private void loadUserSuggestions() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        if (SearchActivity.searchWord != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        userList = twitter.searchUsers(SearchActivity.searchWord, 1);
                        if (!(userList.size() < 20)) {
                            List<User> adding = twitter.searchUsers(SearchActivity.searchWord, 2);
                            for (User user : adding) {
                                userList.add(user);
                            }
                            if (!(userList.size() < 40)){
                                List<User> adding2 = twitter.searchUsers(SearchActivity.searchWord,3);
                                for (User user : adding2) {
                                    userList.add(user);
                                }
                            }
                        }
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (view instanceof RecyclerView) {
                        Context context = view.getContext();
                        RecyclerView recyclerView = (RecyclerView) view;
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        SearchPeopleRecyclerViewAdapter peopleAdapter = new SearchPeopleRecyclerViewAdapter(userList, mListener, context);
                        peopleAdapter.setOnUserSelectedListener(SearchPeopleFragment.this);
                        recyclerView.setAdapter(peopleAdapter);
                    }
                    dismissProgressDialog();
                    super.onPostExecute(aVoid);
                }
            }.execute();
        }
    }

    private void loadTweetSuggestions() {
        if (SearchActivity.searchWord != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {

                    try {
                        Query q = new Query(SearchActivity.searchWord);
                        q.setCount(100);
                        q.setResultType(Query.MIXED);
                        QueryResult result = twitter.search(q);
                        tweetList = result.getTweets();
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (view instanceof RecyclerView) {
                        Context context = view.getContext();
                        RecyclerView recyclerView = (RecyclerView) view;
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        SearchTweetAdapter stAdapter = new SearchTweetAdapter(tweetList, context);
                        stAdapter.setOnIconClickListener(SearchPeopleFragment.this);
                        stAdapter.setOnTweetActionListener(SearchPeopleFragment.this);
                        stAdapter.setOnSTItemClickListener(SearchPeopleFragment.this);
                        recyclerView.setAdapter(stAdapter);
                    }
                    super.onPostExecute(aVoid);
                }
            }.execute();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void userIconClicked(int position) {

    }

    @Override
    public void onClick(int position, int type) {

    }

    @Override
    public void itemClicked(int position) {
        Log.d("tag", "SPFragment, itemClicked: " + position);
        Status tweet = tweetList.get(position);
        Intent i = new Intent(getActivity(), TweetDetailActivity.class);
        i.putExtra(TweetDetailActivity.SELECTED_TWEET_4J, tweet);
        startActivity(i);
    }

    @Override
    public void onUserSelected(User user) {
        Log.d("tag", "SPFragment, itemClicked: " + user.getName());
        Intent i = new Intent(context, UserPage.class);
        i.putExtra(UserPage.SCREEN_NAME, user.getScreenName());
        startActivity(i);
    }


    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(User user);
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
