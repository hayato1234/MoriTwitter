package com.orangesunshine.moritwitter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.gson.Gson;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkOnClickListener;
import com.luseen.autolinklibrary.AutoLinkTextView;
import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.orangesunshine.moritwitter.ShowMedia.ShowMediaActivity;
import com.orangesunshine.moritwitter.adapters.DetailRecyclerViewAdapter;
import com.orangesunshine.moritwitter.conversation.ReplyActivity;
import com.orangesunshine.moritwitter.search.SearchActivity;
import com.simleman.moritwitter.BuildConfig;
import com.simleman.moritwitter.R;
import com.orangesunshine.moritwitter.ShowMedia.VideoActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import static com.orangesunshine.moritwitter.UserPage.SELECTED_TWEET;

public class TweetDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private Tweet tweet;
    private Status tweet4j;

    private ImageView userIcon;
    private TextView userName;
    private AutoLinkTextView content;
    private TextView time;
    private TextView likesText;
    private TextView retweetsText;
    private ImageView replyButton;
    private ImageView retweetButton;
    private ImageView likeButton;
    private ImageView deleteButton;

    private LinearLayout imageLinearLayout1;
    private LinearLayout imageLinearLayout2;
    private ImageView timeLineMedia1;
    private ImageView timeLineMedia2;
    private ImageView timeLineMedia3;
    private ImageView timeLineMedia4;

    private VideoView videoView;
    private ImageView videoPlayButton;
    private RelativeLayout thumbContainer;
    private ImageView thumbView;
    private ImageView playButton;

    private RecyclerView recyclerView;
    private RecyclerView repliedRecyclerView;
    private ProgressBar repliedProgress;
    private ProgressBar replyProgress;
    private DetailRecyclerViewAdapter adapter;
    private DetailRecyclerViewAdapter repliedAdapter;
    private List<Status> replyList;
    private List<Status> repliedList;

    private String screenName;
    private String profilePicURL;
    private SharedPreferences mSharedPreferences;
    private Twitter mTwitter;

    private twitter4j.Status status;
    private long tweetId;
    private long previousTweetId;
    private String numberOfLikes, numberOfRetweets;
    private boolean liked;
    private boolean withMedia;


    //like and retweet
    //x status number
    //replay
    // replay list
    private static final  String TAG="tag";
    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;
    public static final String SELECTED_TWEET_4J = "oiebto347bd83h";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.d(TAG, "TDtailA onCreate:");
        setContentView(R.layout.activity_tweet_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbar.setNavigationIcon(R.drawable.ic_tab_home);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TweetDetailActivity.this, FollowListMain.class));
            }
        });
        findViews();

        //recyclerView.setHasFixedSize(true);

        replyList = new ArrayList<>();
        repliedList = new ArrayList<>();


        tweet4j = (Status) getIntent().getSerializableExtra(SELECTED_TWEET_4J);
        if (tweet4j!=null){
            screenName = tweet4j.getUser().getScreenName();
            Log.d(TAG, "TDtailA onCreate:"+screenName);
            profilePicURL = tweet4j.getUser().getBiggerProfileImageURL();
            tweetId = tweet4j.getId();
            numberOfLikes = String.valueOf(tweet4j.getFavoriteCount());
            numberOfRetweets = String.valueOf(tweet4j.getRetweetCount());
            previousTweetId = 0L;
            tweet = new Tweet();
            tweet.setContent(tweet4j.getText());
            tweet.setTime(tweet4j.getCreatedAt());
            tweet.setFaved(tweet4j.isFavorited());
            tweet.setReTweeted(tweet4j.isRetweetedByMe());
        }else {
            String tweetInJson = getIntent().getExtras().getString(UserPage.SELECTED_TWEET);
//            Log.d(TAG, "TDtailA onCreate: "+tweetInJson);
            if (tweetInJson!=null){
                Gson gson = new Gson();
                tweet = gson.fromJson(tweetInJson,Tweet.class);
            }

            screenName = tweet.getScreenName();//getIntent().getExtras().getString(UserPage.SCREEN_NAME);
            profilePicURL = tweet.getProf_image(); //getIntent().getExtras().getString(UserPage.SELECTED_PROFILE_URL);
            tweetId = tweet.getTweetId(); //getIntent().getExtras().getLong(UserPage.TWEET_ID);
            numberOfLikes = tweet.getLikeNum();
            numberOfRetweets = tweet.getRetweetNum();
            previousTweetId = getIntent().getExtras().getLong(UserPage.SELECTED_PREVIOUS_TWEET,0L);
            // also need faved? liked? time, content
        }

        mSharedPreferences = getApplicationContext().getSharedPreferences(LogInActivity.PREFERENCE_NAME,0);

        //chech if owner of app = this tweet's composer
        if (screenName.equals(mSharedPreferences.getString(FollowListMain.USER_NAME_LIST[mSharedPreferences.getInt(LogInActivity.PREF_CURRENT_ACCOUNT,0)],""))){
            deleteButton.setVisibility(View.VISIBLE);
        }
        setViews();
    }

    private void findViews() {
        userIcon = (ImageView)findViewById(R.id.tweet_user_icon);
        userName = (TextView)findViewById(R.id.tweet_user_name);
        content = (AutoLinkTextView) findViewById(R.id.tweet_content);
        time = (TextView)findViewById(R.id.tweet_time);
        likesText = (TextView)findViewById(R.id.like_number);
        retweetsText = (TextView)findViewById(R.id.retweet_number);
        replyButton = (ImageView)findViewById(R.id.reply_button);
        retweetButton = (ImageView)findViewById(R.id.retweet_button);
        likeButton = (ImageView)findViewById(R.id.like_button);
        deleteButton = (ImageView)findViewById(R.id.delete_button);

        replyButton.setOnClickListener(this);
        retweetButton.setOnClickListener(this);
        likeButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);

        timeLineMedia1 = (ImageView) findViewById(R.id.detail_media1);
        timeLineMedia2 = (ImageView) findViewById(R.id.detail_media2);
        timeLineMedia3 = (ImageView) findViewById(R.id.detail_media3);
        timeLineMedia4 = (ImageView) findViewById(R.id.detail_media4);

        imageLinearLayout1 = (LinearLayout) findViewById(R.id.detail_image_linear_1);
        imageLinearLayout2 = (LinearLayout) findViewById(R.id.detail_image_linear_2);

        videoView = (VideoView) findViewById(R.id.detail_video);
        videoPlayButton = (ImageView) findViewById(R.id.detail_video_play_button);
        thumbView = (ImageView) findViewById(R.id.detail_youtube_thumb);
        playButton =(ImageView) findViewById(R.id.detail_youtube_play_button);
        thumbContainer = (RelativeLayout) findViewById(R.id.detail_youtube_container);

        recyclerView = (RecyclerView) findViewById(R.id.detail_recycler_view);
        repliedRecyclerView = (RecyclerView)findViewById(R.id.detail_replied_recycler);
        repliedProgress = (ProgressBar)findViewById(R.id.detail_progress_bar);
        replyProgress = (ProgressBar)findViewById(R.id.detail_reply_progress_bar);

    }

    private void setViews() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(TweetDetailActivity.this)
                        .title("tweeting")
                        .input("type here", null, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                sendTweet(input.toString());

                            }
                        }).show();
            }
        });

        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TweetDetailActivity.this, UserPage.class);
                i.putExtra(UserPage.SCREEN_NAME, screenName);
                startActivity(i);
            }
        });

        Picasso.with(this).load(profilePicURL).resize(200,200).into(userIcon);
        if (getSupportActionBar()!=null)getSupportActionBar().setTitle(tweet.getUser());
        userName.setText(screenName);
        setUpContent();
        time.setText(tweet.getTime().toString());
        likesText.setText(String.valueOf(numberOfLikes));
        retweetsText.setText(String.valueOf(numberOfRetweets));
        if (tweet.isFaved()){
            likeButton.setImageResource(R.drawable.ic_favarit_on);
            liked = true;
        }
        if (tweet.isReTweeted())retweetButton.setImageResource(R.drawable.ic_undo_retweet_button);

        loadTweetInfo();

        final ArrayList<String> mediaList = tweet.getMedia_images(); // get pictures
        if (mediaList != null && mediaList.size() > 0) { // picture or video
            withMedia = true;

        } else if (tweet.getMedia_gifs() != null) { // if gif as media
            withMedia = true;
        } else if (tweet.getInstaUrl()!=null){
            withMedia = true;
        }else { // if no media
            imageLinearLayout1.setVisibility(View.GONE);
            imageLinearLayout2.setVisibility(View.GONE);
            thumbContainer.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            withMedia = false;
        }
//        if (tweet.getYoutubeId() != null) {
//            thumbContainer.setVisibility(View.VISIBLE);
//            String YTThumb = "https://img.youtube.com/vi/"+tweet.getYoutubeId()+"/hqdefault.jpg";
//            Picasso.with(this).load(YTThumb).into(thumbView);
//            playButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    openMediaActivity(4);
//                }
//            });
//        } else {
//            thumbContainer.setVisibility(View.GONE);
//        }
    }

    private void setUpContent() {
        content.addAutoLinkMode(AutoLinkMode.MODE_URL,AutoLinkMode.MODE_MENTION,AutoLinkMode.MODE_HASHTAG);
        content.setMentionModeColor(ContextCompat.getColor(this, R.color.tw__blue_default));
        content.setHashtagModeColor(ContextCompat.getColor(this, R.color.green));
        content.enableUnderLine();
        content.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
            @Override
            public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                Log.d(TAG, "onAutoLinkTextClick: "+autoLinkMode.toString()+" , "+matchedText);
                switch (autoLinkMode){
                    case MODE_MENTION:
                        Intent i = new Intent(TweetDetailActivity.this,UserPage.class);
                        i.putExtra(UserPage.SCREEN_NAME,matchedText.substring(1));
//                        Log.d(TAG, "onAutoLinkTextClick: "+matchedText.substring(1));
                        startActivity(i);
                        break;
                    case MODE_URL:
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(matchedText.substring(1)));
                        startActivity(browserIntent);
                        break;
                    case MODE_HASHTAG:
                        Intent hashI = new Intent(TweetDetailActivity.this, SearchActivity.class);
                        hashI.putExtra(SearchActivity.SEARCH_WORD,matchedText);
                        startActivity(hashI);
                        break;
                }
            }
        });
        content.setAutoLinkText(tweet.getContent());
    }

    private void setUpMedia(){
        final ArrayList<String> mediaList = tweet.getMedia_images(); // get pictures
        if (mediaList != null && mediaList.size() > 0) { // picture or video
            imageLinearLayout1.setVisibility(View.VISIBLE);
            if (tweet.getMedia_vidoes()==null){
                videoPlayButton.setVisibility(View.GONE);
            }else {
                videoPlayButton.setVisibility(View.VISIBLE);
            }
            Picasso.with(this).load(mediaList.get(0)).into(timeLineMedia1);
            timeLineMedia1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMediaActivity(0);
                }
            });
            if (mediaList.size() > 1) {
                Picasso.with(this).load(mediaList.get(1)).into(timeLineMedia2);
                timeLineMedia2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openMediaActivity(1);
                    }
                });
                if (mediaList.size() > 2) {
                    imageLinearLayout2.setVisibility(View.VISIBLE);
                    Picasso.with(this).load(mediaList.get(2)).into(timeLineMedia3);
                    timeLineMedia3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openMediaActivity(2);
                        }
                    });
                    if (mediaList.size() > 3) {
                        timeLineMedia4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openMediaActivity(3);
                            }
                        });
                        Picasso.with(this).load(mediaList.get(3)).into(timeLineMedia4);
                    }
                }
            }
        } else if (tweet.getMedia_gifs() != null) { // if gif as media
            final VideoView myVideoView = videoView;
            myVideoView.setVisibility(View.VISIBLE);
            String gifUrl = tweet.getMedia_gifs().replace("tweet_video_thumb", "tweet_video").replace(".png", ".mp4").replace(".jpg", ".mp4").replace(".jpeg", ".mp4");
            myVideoView.setVideoURI(Uri.parse(gifUrl));
            videoView.requestFocus();
            myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    myVideoView.start();
                }
            });
            myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    myVideoView.start();
                }
            });
        } else if (tweet.getInstaUrl()!=null){
            imageLinearLayout1.setVisibility(View.VISIBLE);
            Picasso.with(this).load(tweet.getInstaUrl()).into(timeLineMedia1);
            timeLineMedia1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMediaActivity(5);
                }
            });
        }else { // if no media
            imageLinearLayout1.setVisibility(View.GONE);
            imageLinearLayout2.setVisibility(View.GONE);
            thumbContainer.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            withMedia = false;
        }
        if (tweet.getYoutubeId() != null) {
            thumbContainer.setVisibility(View.VISIBLE);
            String YTThumb = "https://img.youtube.com/vi/"+tweet.getYoutubeId()+"/hqdefault.jpg";
            Picasso.with(this).load(YTThumb).into(thumbView);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMediaActivity(4);
                }
            });
        } else {
            thumbContainer.setVisibility(View.GONE);
        }
    }

    private void openMediaActivity(int mediaPos) {
        if (mediaPos == 6) { //means video
            Intent videoIntent = new Intent(this, VideoActivity.class);
            videoIntent.putExtra(VideoActivity.MEDIA_VIDEO, tweet.getMedia_vidoes());
            startActivity(videoIntent);
        }else if (mediaPos==5){//means instagram
            String[] urls = new String[]{tweet.getInstaUrl()};
            Intent intent = new Intent(TweetDetailActivity.this, ShowMediaActivity.class);
            intent.putExtra(ShowMediaActivity.MEDIA_POSITION, urls);
            intent.putExtra(ShowMediaActivity.MEDIA_POS, mediaPos);
            startActivity(intent);
        }else if (mediaPos==4){//means youtube
            if (YouTubeIntents.canResolvePlayVideoIntentWithOptions(this)) {
                startActivity(YouTubeIntents.createPlayVideoIntentWithOptions(this,tweet.getYoutubeId(), false, true));
            }
        }else { // means video or pic
            if (tweet.getMedia_vidoes() != null) { // video
                Intent videoIntent = new Intent(this, VideoActivity.class);
                videoIntent.putExtra(VideoActivity.MEDIA_VIDEO, tweet.getMedia_vidoes());
                startActivity(videoIntent);
            }else { //pic
                ArrayList<String> arrayList = tweet.getMedia_images();
                String[] urls = arrayList.toArray(new String[arrayList.size()]);

                Intent intent = new Intent(TweetDetailActivity.this, ShowMediaActivity.class);
                intent.putExtra(ShowMediaActivity.MEDIA_POSITION, urls);
                intent.putExtra(ShowMediaActivity.MEDIA_POS, mediaPos);
                startActivity(intent);
            }
        }
    }

    private void loadTweetInfo(){
        replyProgress.setVisibility(View.VISIBLE);
        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_SECRET);
                builder.setTweetModeExtended(true);
                String access_token = mSharedPreferences.getString(FollowListMain.TOKEN_LIST[FollowListMain.accountNumber], "");
                String access_token_secret = mSharedPreferences.getString(FollowListMain.SECRET_LIST[FollowListMain.accountNumber], "");
                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                mTwitter = new TwitterFactory(builder.build()).getInstance(accessToken);
                mTwitter.addRateLimitStatusListener(new RateLimitStatusListener() {
                    @Override
                    public void onRateLimitStatus(RateLimitStatusEvent rateLimitStatusEvent) {

                    }

                    @Override
                    public void onRateLimitReached(RateLimitStatusEvent rateLimitStatusEvent) {
                        notifyUser(getString(R.string.error),TweetDetailActivity.this);
                        cancel(true);
                    }
                });

                boolean isReply = false;
                try {
                    status = mTwitter.showStatus(tweetId);

                    if (!withMedia && status.getMediaEntities()!=null && status.getMediaEntities().length>0){
                        Log.d(TAG, "TDtailA doInBackground: media found");
                        //if intent didnt come with media but it actually has a media
                        MediaEntity firstMediaEntity = status.getMediaEntities()[0];
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
                                tweet.setMedia_vidoes(videoURL);
                            case "photo":
                                ArrayList<String> mediaURLs = new ArrayList<String>();
                                for (MediaEntity mediaEntity : status.getMediaEntities()) {
                                    mediaURLs.add(mediaEntity.getMediaURL()); // save thumb as well if video
                                }

                                tweet.setMedia_images(mediaURLs);
                                break;
                            case "animated_gif":
                                tweet.setMedia_gifs(firstMediaEntity.getMediaURL());
                                break;
                            default:
                                break;
                        }
                    }
                    isReply = status.getInReplyToScreenName()!=null;
                    if (screenName.equals(mSharedPreferences.getString(FollowListMain.USER_NAME_LIST[FollowListMain.accountNumber],""))){ // main user rply doesnt work with search
                        List<twitter4j.Status> statuses = mTwitter.getMentionsTimeline(new Paging(tweetId));
                        for (twitter4j.Status ss:statuses){
                            if (ss.getInReplyToStatusId() == tweetId) replyList.add(ss);
                        }
                        //Log.d(TAG, "DetailA doInBackground: "+statuses.size()+" , ");
                    }else {


                        Query query;
                        if (previousTweetId != 0) {
                            query = new Query("to:"+screenName+" max_id:"+previousTweetId);
                        }else {
                            query = new Query("to:"+screenName+" since_id:" + tweetId);
                        }
                        query.setCount(40);
                        QueryResult result = mTwitter.search(query);

                        //replyList = result.getTweets().stream().filter(t->t.getInReplyToStatusId()==tweetId).collect(Collectors.toList());
                        //stream api after minSDK 24
                        int count=0;
                        do {
                            count++;
                            for (twitter4j.Status reply :result.getTweets()){
                                if (reply.getInReplyToStatusId() == tweetId) {
                                    replyList.add(reply);
                                }
                            }
                            query = result.nextQuery();
                            if (query!=null&&replyList.size()<20){
                                result = mTwitter.search(query);
                            } else count=11;
                        }while (count<2);

                        //doing below takes too much time?
//                        if (replyList.size()<20&&previousTweetId != 0){ // if there are less than 20 replies between next tweet and this tweet, get replies after next tweet
//                            query = new Query("to:"+screenName+" since_id:"+previousTweetId);
//                            query.setCount(40);
//                            result = mTwitter.search(query);
//                            do {
//
//                                for (twitter4j.Status reply :result.getTweets()){
//                                    if (reply.getInReplyToStatusId() == tweetId) {
//                                        replyList.add(reply);
//                                    }
//                                }
//                                query = result.nextQuery();
//                                if (query!=null&&replyList.size()<20){
//                                    hasNext = true;
//                                    result = mTwitter.search(query);
//                                } else hasNext = false;
//
//                            }while (hasNext);
//                        }
                    }
                } catch (TwitterException e) {
                    Log.d(TAG, "DetailA doInBackground:error "+e.toString());
                }

                return isReply;
            }

            @Override
            protected void onPostExecute(Boolean isReply) {
                replyProgress.setVisibility(View.GONE);
                adapter = new DetailRecyclerViewAdapter(replyList,TweetDetailActivity.this);
                adapter.setOnReplyClickListener(new DetailRecyclerViewAdapter.OnReplyClickListener() {
                    @Override
                    public void onReplyClick(int position) {
                        //Log.d(TAG, "DetailA loadTI post "+position);
                        twitter4j.Status statusR = replyList.get(position);
                        Tweet tweet = new Tweet();
                        tweet.setContent(statusR.getText());
                        tweet.setScreenName(statusR.getUser().getScreenName());
                        tweet.setProf_image(statusR.getUser().getBiggerProfileImageURL());
                        tweet.setTweetId(statusR.getId());
                        tweet.setLikeNum(String.valueOf(statusR.getFavoriteCount()));
                        tweet.setRetweetNum(String.valueOf(statusR.getRetweetCount()));
                        tweet.setTime(statusR.getCreatedAt());
                        tweet.setFaved(statusR.isFavorited());
                        tweet.setRetweet(statusR.isRetweetedByMe());
                        String selectedTweetJson = new Gson().toJson(tweet);
                        Intent i = new Intent(TweetDetailActivity.this, TweetDetailActivity.class);
                        i.putExtra(SELECTED_TWEET, selectedTweetJson);
                        startActivity(i);
                    }
                });
                recyclerView.setAdapter(adapter);
                setUpMedia();
                if (isReply) loadRepliedTweet(tweetId);
                //progressDialog.dismiss();
                super.onPostExecute(isReply);
            }
        }.execute();
    }

    private void loadRepliedTweet(final Long id){
        Log.d(TAG, "DetailA loadRT "+id);
        repliedProgress.setVisibility(View.VISIBLE);
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                try {
                    twitter4j.Status s = mTwitter.showStatus(id);
                    if (s.getInReplyToStatusId()>0){ // means this is reply
                        twitter4j.Status repliedS = mTwitter.showStatus(s.getInReplyToStatusId());
                        repliedList.add(repliedS);
                        if (repliedS.getInReplyToStatusId()>0){ // means the reply is reply
                            twitter4j.Status repliedSS = mTwitter.showStatus(repliedS.getInReplyToStatusId());
                            repliedList.add(0,repliedSS);
                        }
                        //Log.d(TAG, "DetailA loadRT back "+repliedS.getText());
                        return repliedS.getId();
                    }

                } catch (TwitterException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return 0L;
            }

            @Override
            protected void onPostExecute(Long id) {
//                if (id!=0L){
//                    loadRepliedTweet(id);
//                }else {
//                }
                //set click listener, loading only two replied tweet -> original tweet needed to be put in list -> otherwise original move to bottom
                repliedProgress.setVisibility(View.GONE);
                repliedAdapter = new DetailRecyclerViewAdapter(repliedList,TweetDetailActivity.this);
                repliedAdapter.setOnReplyClickListener(new DetailRecyclerViewAdapter.OnReplyClickListener() {
                    @Override
                    public void onReplyClick(int position) {
                        twitter4j.Status statusR = repliedList.get(position);
                        Tweet tweet = new Tweet();
                        tweet.setContent(statusR.getText());
                        tweet.setScreenName(statusR.getUser().getScreenName());
                        tweet.setProf_image(statusR.getUser().getBiggerProfileImageURL());
                        tweet.setTweetId(statusR.getId());
                        tweet.setLikeNum(String.valueOf(statusR.getFavoriteCount()));
                        tweet.setRetweetNum(String.valueOf(statusR.getRetweetCount()));
                        tweet.setTime(statusR.getCreatedAt());
                        tweet.setFaved(statusR.isFavorited());
                        tweet.setRetweet(statusR.isRetweetedByMe());
                        String selectedTweetJson = new Gson().toJson(tweet);
                        Intent i = new Intent(TweetDetailActivity.this, TweetDetailActivity.class);
                        i.putExtra(SELECTED_TWEET, selectedTweetJson);
                        startActivity(i);
                    }
                });
                repliedRecyclerView.setAdapter(repliedAdapter);
                repliedAdapter.notifyDataSetChanged();

                super.onPostExecute(id);
            }
        }.execute();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.reply_button:
                replyToTweet();
                break;
            case R.id.retweet_button:
                reTweet();
                break;
            case R.id.like_button:
                likeTweet();
                break;
            case R.id.delete_button:
                deleteTweet();
            default:
                Log.d(TAG, "onClick: no match");
        }
    }

    private void deleteTweet() {
        if (mTwitter==null){
            return;
        }

        AlertDialog.Builder buidler = new AlertDialog.Builder(this);
        buidler.setMessage(R.string.delete_question);
        buidler.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            mTwitter.destroyStatus(tweetId);
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        //Toast.makeText(TweetDetailActivity.this, R.string.deleted,Toast.LENGTH_SHORT).show();
                        notifyUser(getString(R.string.deleted),TweetDetailActivity.this);
                        onBackPressed();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });
        buidler.show();

    }

    public void sendTweet(final String text){
        Log.d("tag", "sendTweet " + text);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mTwitter.updateStatus(text);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    private void replyToTweet(){
        Intent intent = new Intent(this,ReplyActivity.class);
        if (status!=null){
            intent.putExtra(ReplyActivity.TWEET_STATUS,status);
        }
        startActivity(intent);
    }

    private void reTweet() {
        if (mTwitter != null) {
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
                                            mTwitter.destroyStatus(tweet.getRetweetedId());
                                            tweet.setReTweeted(false);
                                        } catch (TwitterException e) {
                                            Log.d("tag", "TLTFrag reTweet() " + e);
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        retweetButton.setImageResource(R.drawable.ic_retweet_button);
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
                                            twitter4j.Status s = mTwitter.retweetStatus(tweet.getTweetId());
                                            tweet.setRetweetedId(s.getId());
                                            tweet.setReTweeted(true);
                                        } catch (TwitterException e) {
                                            Log.d("tag", "TLTFrag reTweet() " + e);
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        retweetButton.setImageResource(R.drawable.ic_undo_retweet_button);
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

    private void likeTweet(){
        if (mTwitter==null){
            return;
        }
        if (!liked){ // like tweet
            liked = true;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        mTwitter.createFavorite(tweetId);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
            likeButton.setImageResource(R.drawable.ic_favarit_on);
        }else { //unlike tweet
            liked = false;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        mTwitter.destroyFavorite(tweetId);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
            likeButton.setImageResource(R.drawable.ic_favarit_off);
        }
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
