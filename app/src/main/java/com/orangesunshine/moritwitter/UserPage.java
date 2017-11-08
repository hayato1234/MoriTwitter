package com.orangesunshine.moritwitter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.gson.Gson;
import com.orangesunshine.moritwitter.FollowList.FollowListFromUserActivity;
import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.orangesunshine.moritwitter.ShowMedia.ShowMediaActivity;
import com.orangesunshine.moritwitter.conversation.ReplyActivity;
import com.orangesunshine.moritwitter.favorite.FavActivity;
import com.orangesunshine.moritwitter.media.MediaActivity;
import com.simleman.moritwitter.BuildConfig;
import com.simleman.moritwitter.R;
import com.orangesunshine.moritwitter.ShowMedia.VideoActivity;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import twitter4j.*;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class UserPage extends AppCompatActivity implements UserPageListAdapter.MediaClickedListener, UserPageListAdapter.IconClickListener, UserPageListAdapter.TweetActionListener {

    public static final String SCREEN_NAME = "OnDjELCLgb";
    public static final String SELECTED_TWEET = "KLiCNoh5iN";
    public static final String SELECTED_PREVIOUS_TWEET = "gZbDjfaYrx";
    public static final String SELECTED_PROFILE_URL = "yJ81m4TEYm";
    public static final String TWEET_ID = "71Hn5ji4XA";
    private static SharedPreferences mSharedPreferences;
    ArrayList<Tweet> tweets;

    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;

    ListView listView;
    ImageView showMoreInfoButton;
    ImageView showLessInfoButton;
    ImageView followSwitchButton;
    ImageView likesButton;
    ImageView mediaButton;
    ImageView locationIcon;
    ImageView linkIcon;
    LinearLayout moreInfoContainer1;
    LinearLayout moreInfoContainer2;
    TextView followerCount;
    TextView followerText;
    TextView followingCount;
    TextView followingText;
    TextView location;
    TextView link;

    User user;
    String appUserScreenName;
    Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;
    String screenName;
    String access_token;
    String access_token_secret;
    AccessToken accessToken;
    Twitter twitter;
    int loadCount = 0; // how many times tweets loaded
    boolean loading;
    boolean isFollowing = true;
    boolean noMoreTweets;

    private int accountNumber;

    private ProgressDialog progressDialog;

    UserPageListAdapter userListAdapter;
    //x get time
    //x fix time
    //x hour ago, days ago
    // x get like, retweet
    // button for like retweet
    //x clickable image
    //x image slide
    // tweets save to db ?
    // omit duplicated tweets use comparetor
    // changable text size
    //x load videos
    // x show on tweet
    //x loading image error
    //  - caused by reuse of view?, set imageview null when less than a number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_page);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSharedPreferences = getApplicationContext().getSharedPreferences(
                LogInActivity.PREFERENCE_NAME, 0);

        accountNumber = mSharedPreferences.getInt(LogInActivity.PREF_CURRENT_ACCOUNT, 0);
        // check if youtube app is installed
//        final YouTubeInitializationResult result = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
//        if (result != YouTubeInitializationResult.SUCCESS) {
//            //If there are any issues we can show an error dialog.
//            result.getErrorDialog(this, 0).show();
//        }

        tweets = new ArrayList<>();

        listView = (ListView) findViewById(R.id.user_page_list_view);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.user_page_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                tweets = new ArrayList<>();
                loadCount = 0;

                if (!loading) {
                    loading = true;
                    loadTweets();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        View header = getLayoutInflater().inflate(R.layout.user_page_header,null );
        listView.addHeaderView(header);

        screenName = getIntent().getExtras().getString(SCREEN_NAME);
        appUserScreenName = mSharedPreferences.getString(FollowListMain.USER_NAME_LIST[accountNumber], "");
        if (screenName.equals(appUserScreenName)) {
            ImageView followSwitch = (ImageView) header.findViewById(R.id.follow_switch_button);
            followSwitch.setVisibility(View.GONE);
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 1) {
                    Tweet selectedTweet = tweets.get(position - 1);
                    long previousTweedId; // set this to previous tweet id if it exists, else assign 0;
                    if (position >= 2) {
                        previousTweedId = tweets.get(position - 2).getTweetId();
                    } else {
                        previousTweedId = 0L;
                    }
                    Gson gson = new Gson();
                    String selectedTweetJson = gson.toJson(selectedTweet);
                    Log.d("tag", "userpage onItemClick: " + selectedTweet.getScreenName());
                    Intent i = new Intent(UserPage.this, TweetDetailActivity.class);
                    i.putExtra(SELECTED_TWEET, selectedTweetJson);
                    i.putExtra(SELECTED_PREVIOUS_TWEET, previousTweedId);
//                    i.putExtra(SCREEN_NAME, screenName);
//                    i.putExtra(SELECTED_PROFILE_URL, user.getProfileImageURL());
//                    i.putExtra(TWEET_ID, selectedTweet.getTweetId());
                    startActivity(i);
                }
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                boolean atEnd = firstVisibleItem + visibleItemCount >= totalItemCount;
                boolean listHasValue = totalItemCount > 0;
                if (atEnd && listHasValue) { // load more tweets if at the end of listview
                    if (!loading&&!noMoreTweets) {
                        loading = true;
                        loadMoreTweets();
                    }
                }
            }
        });

        setViews();
        loadTweets();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(UserPage.this)
                        .title("tweeting")
                        .input("type here", null, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                sendTweet(input.toString());

                            }
                        }).show();
            }
        });
    }

    private void setViews() {
        moreInfoContainer1 = (LinearLayout) findViewById(R.id.more_info_container);
        moreInfoContainer2 = (LinearLayout) findViewById(R.id.more_info_container2);
        followSwitchButton = (ImageView) findViewById(R.id.follow_switch_button);
        showLessInfoButton = (ImageView) findViewById(R.id.less_info_button);
        showMoreInfoButton = (ImageView) findViewById(R.id.more_info_button);
        followerCount = (TextView) findViewById(R.id.user_page_follower_count);
        followerText = (TextView) findViewById(R.id.user_page_follower_text);
        followingCount = (TextView) findViewById(R.id.user_page_following_count);
        followingText = (TextView) findViewById(R.id.user_page_following_text);
        likesButton = (ImageView) findViewById(R.id.user_page_likes_page);
        mediaButton = (ImageView) findViewById(R.id.user_page_media_page);
        location = (TextView) findViewById(R.id.user_page_location);
        locationIcon = (ImageView) findViewById(R.id.user_page_location_img);
        link = (TextView) findViewById(R.id.user_page_link);
        linkIcon = (ImageView) findViewById(R.id.user_page_link_img);
        followerCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followerOnClick();
            }
        });
        followerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followerOnClick();
            }
        });
        followingCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followingOnClick();
            }
        });
        followingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followingOnClick();
            }
        });

        showMoreInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreInfoButton.setVisibility(View.GONE);
                showLessInfoButton.setVisibility(View.VISIBLE);
                moreInfoContainer1.setVisibility(View.VISIBLE);
                moreInfoContainer2.setVisibility(View.VISIBLE);
            }
        });
        showLessInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreInfoButton.setVisibility(View.VISIBLE);
                showLessInfoButton.setVisibility(View.GONE);
                moreInfoContainer1.setVisibility(View.GONE);
                moreInfoContainer2.setVisibility(View.GONE);
            }
        });
        followSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollowing) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(UserPage.this);
                    adb.setTitle(getString(R.string.stop_following) + user.getName() + "?");
                    adb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switchFollowState();
                        }
                    });
                    adb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    adb.show();
                } else switchFollowState();
            }
        });
        likesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),FavActivity.class);
                intent.putExtra(FavActivity.SCREEN_NAME,screenName);
                startActivity(intent);

            }
        });
        mediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MediaActivity.class);
                intent.putExtra(MediaActivity.SCREEN_NAME,screenName);
                startActivity(intent);
            }
        });
    }

    private void loadTweets() {
        new AsyncTask<Void, Void, Void>() {

            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(UserPage.this,
                        "", getString(R.string.loading_massage), true);
            }

            @Override
            protected Void doInBackground(Void... params) {

                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_SECRET);
                builder.setTweetModeExtended(true);
                access_token = mSharedPreferences.getString(FollowListMain.TOKEN_LIST[accountNumber], "");
                access_token_secret = mSharedPreferences.getString(FollowListMain.SECRET_LIST[accountNumber], "");
                accessToken = new AccessToken(access_token, access_token_secret);
                twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                Paging pg = new Paging(++loadCount, 30); // 1 page, 30 tweets

                try {
                    user = twitter.showUser(screenName);
                    isFollowing = twitter.showFriendship(appUserScreenName, screenName).isSourceFollowingTarget();

                    ResponseList<twitter4j.Status> statuses = twitter.getUserTimeline(user.getScreenName(), pg); // loading each tweet and put in array

                    if (statuses.size()<30) noMoreTweets = true;
                    for (twitter4j.Status status : statuses) { // taking one tweet from the array
                        Tweet eachTweet = new Tweet(); //tweet object
                        if (status.isRetweetedByMe()) {
                            eachTweet.setReTweeted(true);
                            eachTweet.setRetweetedId(status.getCurrentUserRetweetId()); // this status is retweeted status
                        }
                        if (status.isRetweet()) {
                            status = status.getRetweetedStatus();
                            eachTweet.setRetweet(true);
                        }
                        eachTweet.setProf_image(status.getUser().getProfileImageURL());
                        eachTweet.setUser(status.getUser().getName());
                        eachTweet.setScreenName(status.getUser().getScreenName());
                        eachTweet.setContent(status.getText());
                        eachTweet.setTime(status.getCreatedAt());
                        eachTweet.setTweetId(status.getId());

                        int reCount = status.getRetweetCount();
                        if (reCount > 999999) {
                            eachTweet.setRetweetNum(String.valueOf(reCount / 1000000) + "M");
                        } else if (reCount > 999) {
                            eachTweet.setRetweetNum(NumberFormat.getInstance().format(reCount));
                        } else eachTweet.setRetweetNum(String.valueOf(reCount));
                        int favCount = status.getFavoriteCount();
                        eachTweet.setFaved(status.isFavorited());
                        if (favCount > 999999) {
                            eachTweet.setLikeNum(String.valueOf(favCount / 1000000) + "M");
                        } else if (favCount > 999) {
                            eachTweet.setLikeNum(NumberFormat.getInstance().format(favCount));
                        } else eachTweet.setLikeNum(String.valueOf(favCount));

                        if (status.getQuotedStatus() != null) {
                            eachTweet.setQuoted(true);
                            eachTweet.setQuoteId(status.getQuotedStatus().getId());
                            eachTweet.setQuoteName(status.getQuotedStatus().getUser().getName());
                            eachTweet.setQuoteContent(status.getQuotedStatus().getText());
                        }

                        MediaEntity[] mediaEntities = status.getMediaEntities();
                        if (mediaEntities.length > 0) { //if there are media
                            MediaEntity firstMediaEntity = mediaEntities[0];
                            switch (firstMediaEntity.getType()) {
                                case "video":
                                    String videoURL = "";
                                    int bitrate = 0;
                                    //getting highest resolution(bitrate) video from all variant
                                    for (MediaEntity.Variant v : firstMediaEntity.getVideoVariants()) {
                                        if (v.getBitrate() > bitrate) {
                                            bitrate = v.getBitrate();
                                            videoURL = v.getUrl();
                                        }
                                    }
                                    eachTweet.setMedia_vidoes(videoURL);

                                case "photo":
                                    ArrayList<String> mediaURLs = new ArrayList<String>();
                                    for (MediaEntity mediaEntity : mediaEntities) {
                                        mediaURLs.add(mediaEntity.getMediaURL()); // save thumb as well if video
                                    }

                                    eachTweet.setMedia_images(mediaURLs);
                                    break;
                                case "animated_gif":
                                    eachTweet.setMedia_gifs(firstMediaEntity.getMediaURL());
                                    break;
                                default:
                                    break;
                            }
                        }


                        //need change on more and fragment
                        if (status.getURLEntities() != null && status.getURLEntities().length > 0) {
                            URLType type = judgeURLEntities(status.getURLEntities());
                            if (type != null) {
                                String url = status.getURLEntities()[0].getExpandedURL();
                                switch (type) {
                                    case YOUTUBE:
                                        eachTweet.setYoutubeId(getYoutubeURL(url));
                                        break;
                                    case INSTAGRAM:
                                        eachTweet.setInstaUrl(getInstagramURL(url));
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        //Log.d("tag", "userpage onPostExecute: "+);
                        tweets.add(eachTweet);

                    }
                } catch (TwitterException e) {
                    Log.d("tag", "user page doInBackground error: " + e.getMessage());
                    dismissProgressDialog();
                    if (e.isCausedByNetworkIssue()) {
                        notifyUser(getString(R.string.network_error), UserPage.this);
                        cancel(true);
                    } else {
                        notifyUser("Cannot access this account", UserPage.this);
                        cancel(true);
                    }
                    //e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                String userName = user.getName();
                userListAdapter = new UserPageListAdapter(getApplicationContext(), tweets, userName);
                //Log.d("tag", "onPostExecute: "+tweets.get(21).getLikeNum());
                userListAdapter.setMediaClickedListener(UserPage.this);
                userListAdapter.setOnIconClickListener(UserPage.this);
                userListAdapter.setOnTweetActionListener(UserPage.this);
                listView.setAdapter(userListAdapter);

                ImageView icon = (ImageView) findViewById(R.id.user_page_icon1);
                ImageView headerImage = (ImageView) findViewById(R.id.user_page_header);
                TextView description = (TextView) findViewById(R.id.user_page_description);
                setMoreInfo();

                Picasso.with(UserPage.this).load(user.getBiggerProfileImageURL()).fit().into(icon);
                String iconURL = user.getProfileBannerURL();
                if (iconURL != null) {
                    Picasso.with(UserPage.this).load(user.getProfileBannerURL()).fit().into(headerImage);
                } else {
                    headerImage.setVisibility(View.GONE);
                }

                if (!isFollowing) {
                    followSwitchButton.setImageResource(R.drawable.ic_follow);
                    followSwitchButton.setBackgroundColor(getResources().getColor(R.color.tw__solid_white));
                }

                description.setText(user.getDescription());
                toolbar.setTitle(userName);
                dismissProgressDialog();
                loading = false;
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    private void setMoreInfo() {
        if (followingCount != null) { //check if setView() is loaded
            int followings = user.getFriendsCount();
            String followingsString;
            if (followings > 999999) {
                followingsString = String.valueOf(followings / 1000000) + "M";
            } else if (followings > 999) {
                followingsString = String.valueOf(followings / 1000) + "K";
            } else followingsString = String.valueOf(followings);
            followingCount.setText(followingsString);

            int followers = user.getFollowersCount();
            String followersString;
            if (followers > 999999) {
                followersString = String.valueOf(followers / 1000000) + "M";
            } else if (followers > 999) {
                followersString = String.valueOf(followers / 1000) + "K";
            } else followersString = String.valueOf(followers);

            followerCount.setText(followersString);

            if (user.getLocation() != null) {
                location.setText(user.getLocation());
            } else locationIcon.setVisibility(View.GONE);
            if (user.getURL() != null) {
                link.setText(user.getURL());
            } else linkIcon.setVisibility(View.GONE);
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void loadMoreTweets() {
        new AsyncTask<Void, Void, List<Status>>() {
            @Override
            protected List<twitter4j.Status> doInBackground(Void... params) {
                try {
                    Paging paging = new Paging(++loadCount, 30);
                    Log.d("tag", "UP lmoreT backgroung: " + loadCount);

                    return twitter.getUserTimeline(user.getScreenName(), paging);
                } catch (TwitterException e) {
                    e.printStackTrace();
                    dismissProgressDialog();
                    if (e.isCausedByNetworkIssue()) {
                        notifyUser(getString(R.string.network_error), UserPage.this);
                        cancel(true);
                    } else {
                        notifyUser("This account is private", UserPage.this);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<twitter4j.Status> result) {
                if (result != null) {
                    if (result.size()<30) noMoreTweets = true;
                    ArrayList<Tweet> newTweets = new ArrayList<>();
                    for (twitter4j.Status status : result) {
                        Tweet addTweet = new Tweet();
                        if (status.isRetweetedByMe()) {
                            addTweet.setReTweeted(true);
                            addTweet.setRetweetedId(status.getCurrentUserRetweetId()); // this status is retweeted status
                        }
                        if (status.isRetweet()) {
                            status = status.getRetweetedStatus();
                            addTweet.setRetweet(true);
                        }
                        addTweet.setProf_image(status.getUser().getBiggerProfileImageURL());
                        addTweet.setUser(status.getUser().getName());
                        addTweet.setScreenName(status.getUser().getScreenName());
                        addTweet.setContent(status.getText());
                        addTweet.setTime(status.getCreatedAt());
                        addTweet.setTweetId(status.getId());

                        if (status.getQuotedStatus() != null) {
                            addTweet.setQuoted(true);
                            addTweet.setQuoteId(status.getQuotedStatus().getId());
                            addTweet.setQuoteName(status.getQuotedStatus().getUser().getName());
                            addTweet.setQuoteContent(status.getQuotedStatus().getText());
                        }

                        int reCount = status.getRetweetCount();
                        if (reCount > 999999) {
                            addTweet.setRetweetNum(String.valueOf(reCount / 1000000) + "M");
                        } else if (reCount > 999) {
                            addTweet.setRetweetNum(NumberFormat.getInstance().format(reCount));
                        } else addTweet.setRetweetNum(String.valueOf(reCount));
                        int favCount = status.getFavoriteCount();
                        addTweet.setFaved(status.isFavorited());
                        if (favCount > 999999) {
                            addTweet.setLikeNum(String.valueOf(favCount / 1000000) + "M");
                        } else if (favCount > 999) {
                            addTweet.setLikeNum(NumberFormat.getInstance().format(favCount));
                        } else addTweet.setLikeNum(String.valueOf(favCount));

                        MediaEntity[] mediaEntities = status.getMediaEntities();
                        if (mediaEntities.length > 0) { //if there are media
                            MediaEntity firstMediaEntity = mediaEntities[0];
                            switch (firstMediaEntity.getType()) {
                                case "video":
                                    String videoURL = "";
                                    int bitrate = 0;
                                    //getting highest resolution(bitrate) video from all variant
                                    for (MediaEntity.Variant v : firstMediaEntity.getVideoVariants()) {
                                        if (v.getBitrate() > bitrate) {
                                            bitrate = v.getBitrate();
                                            videoURL = v.getUrl();
                                        }
                                    }
                                    addTweet.setMedia_vidoes(videoURL);
                                case "photo":
                                    ArrayList<String> mediaURLs = new ArrayList<String>();
                                    for (MediaEntity mediaEntity : mediaEntities) {
                                        mediaURLs.add(mediaEntity.getMediaURL()); // save thumb as well if video
                                    }
                                    addTweet.setMedia_images(mediaURLs);
                                    break;
                                case "animated_gif":
                                    addTweet.setMedia_gifs(firstMediaEntity.getMediaURL());
                                    break;
                                default:
                                    break;
                            }
                        }

                        if (status.getURLEntities() != null && status.getURLEntities().length > 0) {
                            URLType type = judgeURLEntities(status.getURLEntities());
                            if (type != null) {
                                String url = status.getURLEntities()[0].getExpandedURL();
                                switch (type) {
                                    case YOUTUBE:
                                        addTweet.setYoutubeId(getYoutubeURL(url));
                                        break;
                                    case INSTAGRAM:
                                        addTweet.setInstaUrl(getInstagramURL(url));
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }

                        newTweets.add(addTweet);
                    }

                    tweets.addAll(newTweets);
//                    userListAdapter.setMediaClickedListener(UserPage.this);
//                    userListAdapter.setOnIconClickListener(UserPage.this);
//                    userListAdapter.setOnTweetActionListener(UserPage.this);
                    //userListAdapter.addItems(newTweets);
                    //Log.d("tag", "UP lmoreT backgroung: " + newTweets.get(0).getContent());
                    userListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UserPage.this, "fail to get more tweets", Toast.LENGTH_SHORT).show();
                }
                //Log.d("tag", "onPostExecute: "+tweets.get(21).getUser());
//                dismissProgressDialog();
                loading = false;
            }
        }.execute();
    }

    @Override
    public void onMediaClicked(int position, int mediaPos) {
        if (mediaPos == 4) {//means it's youtube
            final YouTubeInitializationResult result = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
            if (result != YouTubeInitializationResult.SUCCESS) {
                result.getErrorDialog(this, 0).show();
            }else {
                if (YouTubeIntents.canResolvePlayVideoIntentWithOptions(this)) {
                    startActivity(YouTubeIntents.createPlayVideoIntentWithOptions(this, tweets.get(position).getYoutubeId(), false, true));
                }
            }
        } else { // means picture, video,gif
            String videoURL = tweets.get(position).getMedia_vidoes();
            String instaURL = tweets.get(position).getInstaUrl();

            if (videoURL != null) { // video
                Intent videoIntent = new Intent(this, VideoActivity.class);
                videoIntent.putExtra(VideoActivity.MEDIA_VIDEO, videoURL);
                startActivity(videoIntent);
            } else { // picture
                Intent intent = new Intent(this, ShowMediaActivity.class);
                String[] urls;
                if (instaURL != null) {
                    urls = new String[]{instaURL};
                } else {
                    ArrayList<String> arrayList = tweets.get(position).getMedia_images();
                    urls = arrayList.toArray(new String[arrayList.size()]);
                }
                intent.putExtra(ShowMediaActivity.MEDIA_POSITION, urls);
                intent.putExtra(ShowMediaActivity.MEDIA_POS, mediaPos);
                startActivity(intent);
            }
        }

    }

    private URLType judgeURLEntities(URLEntity[] urlEntities) {
        URLType type = null;
        for (URLEntity entity : urlEntities) {
            String URL = entity.getExpandedURL();
            if (URL != null) {
                if (URL.contains("youtu")) {
                    type = URLType.YOUTUBE;
                    break;
                } else if (URL.contains("insta")) {
                    type = URLType.INSTAGRAM;
                    break;
                } else type = null;
            } else type = null;
        }
        return type;
    }

    private String getInstagramURL(String url) {
        return url.concat("media/?size=l");
    }

    private String getYoutubeURL(String url) {
        if (url.toLowerCase().contains("youtub") && !(url.contains("channel") || url.contains("user")) || url.contains("embed")) {
            int start = url.indexOf("v=") + 2;
            int end = url.length();
            if (url.substring(start).contains("&")) {
                end = url.indexOf("&");
            } else if (url.substring(start).contains("?")) {
                end = url.indexOf("?");
            }
            try {
                return url.substring(start, end);
            } catch (Exception e) {
                return url.substring(start, url.length() - 1);
            }
        } else if (url.toLowerCase().contains("youtu.be")) {
            int start = url.indexOf(".be/") + 4;
            int end = url.length();
            if (url.substring(start).contains("&")) {
                end = url.indexOf("&");
            } else if (url.substring(start).contains("?")) {
                end = url.indexOf("?");
            }
            try {
                return url.substring(start, end);
            } catch (Exception e) {
                return url.substring(start, url.length() - 1);
            }
        }

        return null;
    }

    @Override
    public void userIconClicked(int position) {
        Intent i = new Intent(this, UserPage.class);
        i.putExtra(UserPage.SCREEN_NAME, tweets.get(position).getScreenName());
        startActivity(i);
    }

    private void switchFollowState() {

        new AsyncTask<Boolean, Void, Void>() {
            @Override
            protected Void doInBackground(Boolean... params) {
                boolean isFollowing = params[0];
                try {
                    if (isFollowing) {// following and unfollow
                        twitter.destroyFriendship(screenName);
                    } else { //not following and follow
                        twitter.createFriendship(screenName);
                    }
                } catch (TwitterException te) {
                    Log.d("tag", "UserPage switchFS: " + te);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                if (isFollowing) {// following and unfollow
                    followSwitchButton.setImageResource(R.drawable.ic_follow);
                    followSwitchButton.setBackgroundColor(getResources().getColor(R.color.tw__solid_white));
                    isFollowing = false;
                } else { //not following and follow
                    followSwitchButton.setImageResource(R.drawable.ic_unfollow);
                    followSwitchButton.setBackgroundColor(getResources().getColor(R.color.green));
                    isFollowing = true;
                }

                super.onPostExecute(aVoid);
            }
        }.execute(isFollowing);
    }

    public void followingOnClick() {
        Intent i = new Intent(UserPage.this, FollowListFromUserActivity.class);
        i.putExtra(FollowListFromUserActivity.FROM_SCREEN_NAME, screenName);
        i.putExtra(FollowListFromUserActivity.FROM_NAME, user.getName());
        i.putExtra(FollowListFromUserActivity.FROM_IS_FOLLOWER, false);
        startActivity(i);
    }

    public void followerOnClick() {
        Intent i = new Intent(UserPage.this, FollowListFromUserActivity.class);
        i.putExtra(FollowListFromUserActivity.FROM_SCREEN_NAME, screenName);
        i.putExtra(FollowListFromUserActivity.FROM_NAME, user.getName());
        i.putExtra(FollowListFromUserActivity.FROM_IS_FOLLOWER, true);
        startActivity(i);
    }

    @Override
    public void onClick(int position, int type) {
        switch (type) {
            case 3:
                goQuote(position);
                break;
            case 2:
                favTweet(position);
                break;
            case 1:
                reTweet(position);
                break;
            case 0:
                replyTweet(position);
                break;
            default:
                Log.d("tag", "user page onClick: in default ");
        }
    }

    private void goQuote(final int position) {
        progressDialog = ProgressDialog.show(this, "", "Loading...", true);
        new AsyncTask<Void, Void, Void>() {
            Tweet detailTweet = new Tweet();

            @Override
            protected Void doInBackground(Void... params) {
                if (twitter != null) {
                    detailTweet = new Tweet();
                    try {
                        twitter4j.Status status = twitter.showStatus(tweets.get(position).getQuoteId());
                        detailTweet.setUser(status.getUser().getName());
                        detailTweet.setScreenName(status.getUser().getScreenName());
                        detailTweet.setProf_image(status.getUser().getBiggerProfileImageURL());
                        detailTweet.setTweetId(status.getId());
                        detailTweet.setRetweetNum(String.valueOf(status.getRetweetCount()));
                        detailTweet.setLikeNum(String.valueOf(status.getFavoriteCount()));
                        detailTweet.setContent(status.getText());
                        detailTweet.setTime(status.getCreatedAt());
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Gson gson = new Gson();
                String selectedTweetJson = gson.toJson(detailTweet);
                dismissProgressDialog();
                Intent i = new Intent(UserPage.this, TweetDetailActivity.class);
                i.putExtra(SELECTED_TWEET, selectedTweetJson);
                startActivity(i);
                super.onPostExecute(aVoid);
            }
        }.execute();

    }

    private void sendTweet(final String text) {
        Log.d("tag", "sendTweet " + text);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    twitter.updateStatus(text);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    private void replyTweet(final int position) {
        if (twitter != null) {
            new AsyncTask<Void, Void, Status>() {
                @Override
                protected twitter4j.Status doInBackground(Void... voids) {
                    twitter4j.Status status = null;
                    try {
                        status = twitter.showStatus(tweets.get(position).getTweetId());
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                    if (status != null) return status;
                    else return null;
                }

                @Override
                protected void onPostExecute(twitter4j.Status status) {
                    Intent intent = new Intent(UserPage.this, ReplyActivity.class);
                    intent.putExtra(ReplyActivity.TWEET_STATUS, status);
                    startActivity(intent);
                    super.onPostExecute(status);
                }
            }.execute();
        }

    }

    private void favTweet(final int position) {
        final Tweet t = tweets.get(position);
        if (twitter != null) {
            if (t.isFaved()) {
                t.setFaved(false);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            twitter.createFavorite(t.getTweetId());
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        userListAdapter.notifyDataSetChanged();
                        super.onPostExecute(aVoid);
                    }
                }.execute();

            } else {
                t.setFaved(true);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            twitter.destroyFavorite(t.getTweetId());
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        userListAdapter.notifyDataSetChanged();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }

        }
    }

    private void reTweet(final int position) {
        Tweet tweet = tweets.get(position);
        if (twitter != null) {
            if (tweet.isReTweeted()) {
                new AlertDialog.Builder(this)
                        .setPositiveButton(R.string.undo_retweet, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        try {
                                            //Log.d("tag", "TLTFrag reTweet() "+retweets.get(0).getText());
                                            twitter.destroyStatus(tweets.get(position).getRetweetedId());
                                            tweets.get(position).setReTweeted(false);
                                        } catch (TwitterException e) {
                                            Log.d("tag", "TLTFrag reTweet() " + e);
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        userListAdapter.notifyDataSetChanged();
                                        super.onPostExecute(aVoid);
                                    }
                                }.execute();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setPositiveButton(R.string.retweet, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        try {
                                            twitter4j.Status s = twitter.retweetStatus(tweets.get(position).getTweetId());
                                            tweets.get(position).setRetweetedId(s.getId());
                                            tweets.get(position).setReTweeted(true);
                                        } catch (TwitterException e) {
                                            Log.d("tag", "TLTFrag reTweet() " + e);
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        userListAdapter.notifyDataSetChanged();
                                        super.onPostExecute(aVoid);
                                    }
                                }.execute();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }
    }

    public enum URLType {INSTAGRAM, YOUTUBE}

    public void notifyUser(final String error, final Context context) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }
}
