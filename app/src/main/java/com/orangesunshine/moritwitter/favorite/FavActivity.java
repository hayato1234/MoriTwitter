package com.orangesunshine.moritwitter.favorite;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.gson.Gson;
import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.orangesunshine.moritwitter.ShowMedia.ShowMediaActivity;
import com.orangesunshine.moritwitter.ShowMedia.VideoActivity;
import com.orangesunshine.moritwitter.Tweet;
import com.orangesunshine.moritwitter.TweetDetailActivity;
import com.orangesunshine.moritwitter.UserPage;
import com.orangesunshine.moritwitter.conversation.ReplyActivity;
import com.simleman.moritwitter.BuildConfig;
import com.simleman.moritwitter.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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
import static com.orangesunshine.moritwitter.UserPage.SCREEN_NAME;
import static com.orangesunshine.moritwitter.UserPage.SELECTED_TWEET;

public class FavActivity extends AppCompatActivity implements FavAdapter.FavMediaClickedListener, FavAdapter.FavIconClickListener, FavAdapter.FavTweetActionListener {

    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;
    public static final String SCREEN_NAME = "flabbleruhgi";

    @BindView(R.id.fav_list_view)
    ListView listView;

    private Context context;
    private FavAdapter adapter;
    private List<Tweet> tweets;
    private int loadCount; // how many times tweets loaded
    private boolean loading;
    private int accountNumber;
    private String screenName;
    Twitter twitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav);
        ButterKnife.bind(this);
        context = FavActivity.this;
        tweets = new ArrayList<>(1);
        adapter = new FavAdapter(this,tweets);
        screenName = getIntent().getExtras().getString(SCREEN_NAME);

        initializeTwitter();

        loadTweets();
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
    }

    private void loadTweets(){
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (twitter==null){
                    initializeTwitter();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    Paging pg;
                    if (tweets.size() == 0) {//first time loading timeline
                        pg = new Paging(++loadCount, 50); // 1 page, 50 tweets
                    } else {//second or more
                        if (tweets.get(tweets.size() - 1).isRetweet()) {
                            ++loadCount;
                            pg = new Paging(1, 50, 1L, tweets.get(tweets.size() - 1).getRetweetedId() - 1);
                        } else {
                            ++loadCount;
                            pg = new Paging(1, 50, 1L, tweets.get(tweets.size() - 1).getTweetId() - 1);
                        }
//                        Log.d("tag", "TLTabF doInBackground: "+loadCount+" , "+tweets.get(tweets.size()-1).getContent());
                    }
                    ResponseList<twitter4j.Status> favList = twitter.getFavorites(screenName,pg);// loading each tweet and put in array;

                    for (twitter4j.Status status : favList) { // taking one tweet from the array
                        Tweet tweet1 = new Tweet(); //tweet object

                        User user = status.getUser();
                        if (status.isRetweet()) {
                            if (status.isRetweetedByMe()) {
                                tweet1.setRetweetedId(status.getCurrentUserRetweetId()); // this status is retweeted status
                            } else {
                                tweet1.setRetweetedId(status.getId());
                            }
                            status = status.getRetweetedStatus(); // this status is original tweet
                            User reUser = status.getUser();
                            tweet1.setRetweet(true);
                            tweet1.setReTweetedByName(user.getName());
                            tweet1.setProf_image(reUser.getProfileImageURL());
                            tweet1.setUser(reUser.getName());
                            tweet1.setScreenName(reUser.getScreenName());
                        } else {
                            tweet1.setUser(user.getName());
                            tweet1.setScreenName(user.getScreenName());
                            tweet1.setProf_image(user.getBiggerProfileImageURL());
                        }
                        tweet1.setTweetId(status.getId());
                        tweet1.setReTweeted(status.isRetweetedByMe());

                        if (status.getQuotedStatus() != null) {
                            tweet1.setQuoted(true);
                            tweet1.setQuoteId(status.getQuotedStatus().getId());
                            tweet1.setQuoteName(status.getQuotedStatus().getUser().getName());
                            tweet1.setQuoteContent(status.getQuotedStatus().getText());
                        }

                        tweet1.setFaved(status.isFavorited());
                        tweet1.setContent(status.getText());
                        tweet1.setTime(status.getCreatedAt());
                        int reCount = status.getRetweetCount();
                        if (reCount > 999999) {
                            tweet1.setRetweetNum(String.valueOf(reCount / 1000000) + "M");
                        } else if (reCount > 999) {
                            tweet1.setRetweetNum(String.valueOf(reCount / 1000) + "," + (reCount % 1000));
                        } else tweet1.setRetweetNum(String.valueOf(reCount));
                        int favCount = status.getFavoriteCount();
                        if (favCount > 999999) {
                            tweet1.setLikeNum(String.valueOf(favCount / 1000000) + "M");
                        } else if (favCount > 999) {
                            tweet1.setLikeNum(String.valueOf(favCount / 1000) + "," + (favCount % 1000));
                        } else tweet1.setLikeNum(String.valueOf(favCount));


                        //getting all media (pics, gif, video)
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
                                    tweet1.setMedia_vidoes(videoURL);
                                case "photo":
                                    ArrayList<String> mediaURLs = new ArrayList<String>();
                                    for (MediaEntity mediaEntity : mediaEntities) {
                                        mediaURLs.add(mediaEntity.getMediaURL()); // save thumb as well if video
                                    }

                                    tweet1.setMedia_images(mediaURLs);
                                    break;
                                case "animated_gif":
                                    tweet1.setMedia_gifs(firstMediaEntity.getMediaURL());
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
                                        tweet1.setYoutubeId(getYoutubeURL(url));
                                        //Log.d("tag", "TLTabF back: "+getYoutubeURL(url));
                                        break;
                                    case INSTAGRAM:
                                        tweet1.setInstaUrl(getInstagramURL(url));
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        tweets.add(tweet1);
                    }

                } catch (TwitterException e) {
                    e.printStackTrace();
                    if (e.isCausedByNetworkIssue()) {
                        notifyUser(getString(R.string.network_error), context);
                        cancel(true);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                if (swipeRefreshLayout.isRefreshing())
//                                    swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                        notifyUser("Error loading", context);
                        cancel(true);
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                if (loadCount == 1) {
                    //Log.d("tag", "TLTabF onpost: "+loadCount+" , "+tweets.get(tweets.size()-1).getTweetId());
                    adapter.swapData(tweets);
                    adapter.setMediaClickedListener(FavActivity.this);
                    adapter.setOnIconClickListener(FavActivity.this);
                    adapter.setOnTweetActionListener(FavActivity.this);
                    listView.setAdapter(adapter);
                } else {
                    if (adapter!=null) adapter.notifyDataSetChanged();
                    //Log.d("tag", "TLTabF onpost else: "+pos+" , "+top);
                }
                //dismissProgressDialog();
                loading = false;
//                if (swipeRefreshLayout.isRefreshing()) {
//                    swipeRefreshLayout.setRefreshing(false);
//                }
                super.onPostExecute(aVoid);
            }
        }.execute();
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

    private String getInstagramURL(String url) {
        return url.concat("media/?size=l");
    }

    private String getYoutubeURL(String url) {
        int start = url.indexOf(".be/") + 4;
        return url.substring(start);
    }

    private void notifyUser(final String error, final Context context){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onMediaClicked(int position, int mediaPos) {
        if (mediaPos == 4) {//means it's youtube
            if (YouTubeIntents.canResolvePlayVideoIntentWithOptions(context)) {
                startActivity(YouTubeIntents.createPlayVideoIntentWithOptions(context, tweets.get(position).getYoutubeId(), false, true));
            }
        } else { // means picture, video,gif
            String videoURL = tweets.get(position).getMedia_vidoes();
            String instaURL = tweets.get(position).getInstaUrl();

            if (videoURL != null) { // video
                Intent videoIntent = new Intent(context, VideoActivity.class);
                videoIntent.putExtra(VideoActivity.MEDIA_VIDEO, videoURL);
                startActivity(videoIntent);
            } else { // picture
                Intent intent = new Intent(context, ShowMediaActivity.class);
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

    @Override
    public void userIconClicked(int position) {
        Intent i = new Intent(context, UserPage.class);
        i.putExtra(SCREEN_NAME, tweets.get(position).getScreenName());
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
        }
    }

    private void goQuote(final int position) {
//        progressDialog = ProgressDialog.show(context,"", "Loading...", true);
        new AsyncTask<Void, Void, Void>() {
            Tweet detailTweet = new Tweet();
            @Override
            protected Void doInBackground(Void... params) {
                if (twitter!=null){
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
//                dismissProgressDialog();
                Intent i = new Intent(context, TweetDetailActivity.class);
                i.putExtra(SELECTED_TWEET,selectedTweetJson);
                startActivity(i);
                super.onPostExecute(aVoid);
            }
        }.execute();

    }
    private Status status;

    private void replyTweet(final int position) {
        if (twitter != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        status = twitter.showStatus(tweets.get(position).getTweetId());
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    Intent intent = new Intent(context, ReplyActivity.class);
                    intent.putExtra(ReplyActivity.TWEET_STATUS, status);
                    startActivity(intent);
                    super.onPostExecute(aVoid);
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
                        adapter.notifyDataSetChanged();
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
                        adapter.notifyDataSetChanged();
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
                                        adapter.notifyDataSetChanged();
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
                        .setCancelable(true)
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
                                            notifyUser(getString(R.string.could_not_retweet),context);
                                            cancel(true);
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        adapter.notifyDataSetChanged();
                                        notifyUser("retweet sent",context);
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
                        .setCancelable(true)
                        .show();
            }
        }
    }

}
