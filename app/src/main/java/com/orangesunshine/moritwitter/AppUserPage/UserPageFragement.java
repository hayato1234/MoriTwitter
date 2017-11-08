package com.orangesunshine.moritwitter.AppUserPage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.gson.Gson;
import com.orangesunshine.moritwitter.favorite.FavActivity;
import com.orangesunshine.moritwitter.media.MediaActivity;
import com.simleman.moritwitter.BuildConfig;
import com.orangesunshine.moritwitter.FollowList.FollowListFromUserActivity;
import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.simleman.moritwitter.R;
import com.orangesunshine.moritwitter.ShowMedia.ShowMediaActivity;
import com.orangesunshine.moritwitter.ShowMedia.VideoActivity;
import com.orangesunshine.moritwitter.Tweet;
import com.orangesunshine.moritwitter.TweetDetailActivity;
import com.orangesunshine.moritwitter.UserPage;
import com.orangesunshine.moritwitter.UserPageListAdapter;
import com.orangesunshine.moritwitter.conversation.ReplyActivity;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import static com.orangesunshine.moritwitter.LogInActivity.PREFERENCE_NAME;
import static com.orangesunshine.moritwitter.LogInActivity.PREF_CURRENT_ACCOUNT;

public class UserPageFragement extends Fragment implements UserPageListAdapter.MediaClickedListener, UserPageListAdapter.TweetActionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    //    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
    private String mParam1;
//    private String mParam2;

//    public static final String SCREEN_NAME = "aih6VlhSkc";
//    public static final String SELECTED_TWEET = "SkTz1WcUUb";
//    public static final String SELECTED_PROFILE_URL = "34vRWomuQ2";
//    public static final String TWEET_ID = "QbYO7BHBH6";
    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;

    private static SharedPreferences mSharedPreferences;
    ArrayList<Tweet> tweets;

    User user;
    ListView listView;
    SwipeRefreshLayout swipeRefreshLayout;
    String screenName;
    String access_token;
    String access_token_secret;
    AccessToken accessToken;
    Twitter twitter;
    private int loadCount = 0; // how many times tweets loaded
    boolean loading = false;
    UserPageListAdapter userListAdapter;
    Context context;
    View view;
    private int accountNumber;

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
    boolean isFollowing = true;

    private ProgressDialog progressDialog;

//    private OnFragmentInteractionListener mListener;

    public UserPageFragement() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserPageFragement.
     */
    // TODO: Rename and change types and number of parameters
    public static UserPageFragement newInstance(String param1) {
        UserPageFragement fragment = new UserPageFragement();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        view = inflater.inflate(R.layout.fragment_user_page_fragement, container, false);
        mSharedPreferences = context.getApplicationContext().getSharedPreferences(
                PREFERENCE_NAME, 0);

        tweets = new ArrayList<>();
        //tweetIds = new ArrayList<>();
        listView = (ListView) view.findViewById(R.id.user_page_list_view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.user_page_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                tweets = new ArrayList<>();
                loadCount = 0;

                if (!loading) {
                    loading = true;
                    loadTweets(accountNumber);
                }
            }
        });
        View header = getActivity().getLayoutInflater().inflate(R.layout.user_page_header, null);

        listView.addHeaderView(header);
        accountNumber = mSharedPreferences.getInt(PREF_CURRENT_ACCOUNT, 0);
        screenName = mSharedPreferences.getString(FollowListMain.USER_NAME_LIST[accountNumber], "me");

        ImageView followSwitch = (ImageView) header.findViewById(R.id.follow_switch_button);
        followSwitch.setVisibility(View.GONE);

        loadTweets(accountNumber);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 1) {
                    Tweet selectedTweet = tweets.get(position - 1);
                    Gson gson = new Gson();
                    String selectedTweetJson = gson.toJson(selectedTweet);
                    Log.d("tag", "onItemClick: "+selectedTweetJson);
                    Intent i = new Intent(context, TweetDetailActivity.class);
                    i.putExtra(UserPage.SELECTED_TWEET, selectedTweetJson);
                    i.putExtra(UserPage.SCREEN_NAME, screenName);
                    i.putExtra(UserPage.SELECTED_PROFILE_URL, user.getProfileImageURL());
                    i.putExtra(UserPage.TWEET_ID, selectedTweet.getTweetId());
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
                boolean listHasValue = totalItemCount > 15;
                if (atEnd && listHasValue) { // load more tweets if at the end of listview
                    if (!loading) {
                        loading = true;
                        loadMoreTweets();
                    }
                }
            }
        });
        setViews(view);
        return view;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
    private void loadTweets(final int accNum) {
        //Log.d("tag", "UPFragment loadTweets: "+loadCount);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_SECRET);
                builder.setTweetModeExtended(true);
                access_token = mSharedPreferences.getString(FollowListMain.TOKEN_LIST[accNum], "");
                access_token_secret = mSharedPreferences.getString(FollowListMain.SECRET_LIST[accNum], "");
                accessToken = new AccessToken(access_token, access_token_secret);
                twitter = new TwitterFactory(builder.build()).getInstance(accessToken);


                Paging pg = new Paging(++loadCount, 20); // 1 page, 20 tweets

                try {
                    user = twitter.showUser(screenName);
                    ResponseList<twitter4j.Status> statuses = twitter.getUserTimeline(user.getScreenName(), pg); // loading each tweet and put in array

                    for (twitter4j.Status status : statuses) { // taking one tweet from the array
                        Tweet eachTweet = new Tweet(); //tweet object
                        //getting all media (pics, gif, video)
                        MediaEntity[] mediaEntities = status.getMediaEntities();

                        if (status.isRetweet()) {
                            eachTweet.setReTweeted(true);
                            eachTweet.setRetweetedId(status.getId()); // this status is retweeted status
                            //Log.d("tag", "TLTFrag background " + status.getText());
                            status = status.getRetweetedStatus();
                            eachTweet.setRetweet(true);
                        }
                        eachTweet.setProf_image(status.getUser().getProfileImageURL());
                        eachTweet.setUser(status.getUser().getName());
                        eachTweet.setScreenName(status.getUser().getScreenName());

                        if (status.getQuotedStatus() != null) {
                            eachTweet.setQuoted(true);
                            eachTweet.setQuoteId(status.getQuotedStatus().getId());
                            eachTweet.setQuoteName(status.getQuotedStatus().getUser().getName());
                            eachTweet.setQuoteContent(status.getQuotedStatus().getText());
                        }

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

                        //つぶやきのユーザーIDの取得
                        //String userName = status.getUser().getScreenName();
                        //つぶやきの取得
                        String tweet = status.getText();
                        eachTweet.setTweetId(status.getId());
                        //String profilePictureURL = status.getUser().getProfileImageURL();

                        if (mediaEntities.length > 0) { //if there are media
                            //for (MediaEntity mediaEntity : mediaEntities) {
                            //String url = mediaEntity.getMediaURL();
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
                        eachTweet.setContent(tweet);
                        eachTweet.setTime(status.getCreatedAt());
                        /*tweet1.setUser(userName);
                        tweet1.setContent(tweet);
                        tweet1.setProf_image(profilePictureURL);*/
                        tweets.add(eachTweet);
                    }
                } catch (TwitterException e) {
                    Log.d("tag", "doInBackground: error" + e);
                    e.printStackTrace();
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                userListAdapter = new UserPageListAdapter(context, tweets, user.getName());
                userListAdapter.setMediaClickedListener(UserPageFragement.this);
                userListAdapter.setOnTweetActionListener(UserPageFragement.this);
                listView.setAdapter(userListAdapter);

                ImageView icon = (ImageView) view.findViewById(R.id.user_page_icon1);
                ImageView headerImage = (ImageView) view.findViewById(R.id.user_page_header);
                TextView description = (TextView) view.findViewById(R.id.user_page_description);

                Picasso.with(context).load(user.getBiggerProfileImageURL()).fit().into(icon);
                String iconURL = user.getProfileBannerURL();
                if (iconURL != null) {
                    Picasso.with(context).load(user.getProfileBannerURL()).fit().into(headerImage);
                } else {
                    headerImage.setVisibility(View.GONE);
                }
                description.setText(user.getDescription());
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                setMoreInfo();
                super.onPostExecute(aVoid);
                loading = false;
            }
        }.execute();
    }

    private void setViews(View view) {
        moreInfoContainer1 = (LinearLayout) view.findViewById(R.id.more_info_container);
        moreInfoContainer2 = (LinearLayout) view.findViewById(R.id.more_info_container2);
        followSwitchButton = (ImageView) view.findViewById(R.id.follow_switch_button);
        showLessInfoButton = (ImageView) view.findViewById(R.id.less_info_button);
        showMoreInfoButton = (ImageView) view.findViewById(R.id.more_info_button);
        followerCount = (TextView) view.findViewById(R.id.user_page_follower_count);
        followerText = (TextView) view.findViewById(R.id.user_page_follower_text);
        followingCount = (TextView) view.findViewById(R.id.user_page_following_count);
        followingText = (TextView) view.findViewById(R.id.user_page_following_text);
        likesButton = (ImageView) view.findViewById(R.id.user_page_likes_page);
        mediaButton = (ImageView) view.findViewById(R.id.user_page_media_page);
        location = (TextView) view.findViewById(R.id.user_page_location);
        locationIcon = (ImageView) view.findViewById(R.id.user_page_location_img);
        link = (TextView) view.findViewById(R.id.user_page_link);
        linkIcon = (ImageView) view.findViewById(R.id.user_page_link_img);
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
                    AlertDialog.Builder adb = new AlertDialog.Builder(context);
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
                Intent intent = new Intent(new Intent(getContext(),FavActivity.class));
                intent.putExtra(FavActivity.SCREEN_NAME,screenName);
                startActivity(intent);
            }
        });
        mediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MediaActivity.class);
                intent.putExtra(MediaActivity.SCREEN_NAME,screenName);
                startActivity(intent);
            }
        });
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

    private void loadMoreTweets() {
        new AsyncTask<Void, Void, List<Status>>() {

            @Override
            protected List<twitter4j.Status> doInBackground(Void... params) {
                try {
                    Paging paging = new Paging(++loadCount, 20);
                    return twitter.getUserTimeline(user.getScreenName(), paging);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<twitter4j.Status> result) {
                if (result != null) {

                    ArrayList<Tweet> newTweets = new ArrayList<>();

                    for (twitter4j.Status status : result) {
                        Tweet addTweet = new Tweet();

                        if (status.isRetweet()) {
                            addTweet.setReTweeted(true);
                            addTweet.setRetweetedId(status.getId()); // this status is retweeted status
                            status = status.getRetweetedStatus(); // this is original tweet
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
                            UserPage.URLType type = judgeURLEntities(status.getURLEntities());
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
                    //userListAdapter.addItems(newTweets);
                    userListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "fail to get more tweets", Toast.LENGTH_SHORT).show();
                }
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                //dismissProgressDialog();
                loading = false;
            }
        }.execute();
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

    private String getInstagramURL(String url) {
        return url.concat("media/?size=l");
    }

    private String getYoutubeURL(String url) {
        int start = url.indexOf(".be/") + 4;
        return url.substring(start);
    }

    @Override
    public void onMediaClicked(int position, int mediaPos) {
        if (mediaPos == 4) {//means it's youtube
            if (YouTubeIntents.canResolvePlayVideoIntentWithOptions(context)) {
                startActivity(YouTubeIntents.createPlayVideoIntentWithOptions(context, tweets.get(position).getYoutubeId(), false, true));
            }
        } else { // means picture, video,gif
            String videoURL = tweets.get(position).getMedia_vidoes();
            if (videoURL != null) { // video
                Intent videoIntent = new Intent(context, VideoActivity.class);
                videoIntent.putExtra(videoURL, VideoActivity.MEDIA_VIDEO);
                startActivity(videoIntent);
            } else { // picture
                Intent intent = new Intent(context, ShowMediaActivity.class);
                ArrayList<String> arrayList = tweets.get(position).getMedia_images();
                String[] urls = arrayList.toArray(new String[arrayList.size()]);
                intent.putExtra(ShowMediaActivity.MEDIA_POSITION, urls);
                intent.putExtra(ShowMediaActivity.MEDIA_POS, mediaPos);
                startActivity(intent);
            }
        }
    }

    private UserPage.URLType judgeURLEntities(URLEntity[] urlEntities) {
        UserPage.URLType type = null;
        for (URLEntity entity : urlEntities) {
            String URL = entity.getExpandedURL();
            if (URL != null) {
                if (URL.contains("youtu")) {
                    type = UserPage.URLType.YOUTUBE;
                    break;
                } else if (URL.contains("insta")) {
                    type = UserPage.URLType.INSTAGRAM;
                    break;
                } else type = null;
            } else type = null;
        }
        return type;
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
        }
    }

    private void goQuote(final int position) {
        progressDialog = ProgressDialog.show(context, "", "Loading...", true);
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
                Intent i = new Intent(context, TweetDetailActivity.class);
                i.putExtra(UserPage.SELECTED_TWEET, selectedTweetJson);
                startActivity(i);
                super.onPostExecute(aVoid);
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
                    Intent intent = new Intent(context, ReplyActivity.class);
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
                new AlertDialog.Builder(context)
                        .setPositiveButton(R.string.undo_retweet, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        try {
                                            Log.d("tag", "TLTFrag reTweet() undoing");
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
                new AlertDialog.Builder(context)
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
        Intent i = new Intent(context, FollowListFromUserActivity.class);
        i.putExtra(FollowListFromUserActivity.FROM_SCREEN_NAME, screenName);
        i.putExtra(FollowListFromUserActivity.FROM_NAME, user.getName());
        i.putExtra(FollowListFromUserActivity.FROM_IS_FOLLOWER, false);
        startActivity(i);
    }

    public void followerOnClick() {
        Intent i = new Intent(context, FollowListFromUserActivity.class);
        i.putExtra(FollowListFromUserActivity.FROM_SCREEN_NAME, screenName);
        i.putExtra(FollowListFromUserActivity.FROM_NAME, user.getName());
        i.putExtra(FollowListFromUserActivity.FROM_IS_FOLLOWER, true);
        startActivity(i);
    }

    @Override
    public void onDestroyView() {
        loadCount = 0;
        super.onDestroyView();
    }
}
