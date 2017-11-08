package com.orangesunshine.moritwitter.mention_tab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.gson.Gson;
import com.orangesunshine.moritwitter.ShowMedia.ShowMediaActivity;
import com.simleman.moritwitter.BuildConfig;
import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.simleman.moritwitter.R;
import com.orangesunshine.moritwitter.ShowMedia.VideoActivity;
import com.orangesunshine.moritwitter.Tweet;
import com.orangesunshine.moritwitter.TweetDetailActivity;
import com.orangesunshine.moritwitter.UserPage;

import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
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

/**
 * Created by hayatomoritani on 6/2/17.
 */

public class MentionTabFragment extends Fragment implements MentionTabFragmentAdapter.MentionMediaClickedListener, MentionTabFragmentAdapter.MentionIconClickListener {

    private static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    private static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET;
    SwipeRefreshLayout swipeRefreshLayout;
    private static SharedPreferences mSharedPreferences;
    private RecyclerView recyclerView;
    private MentionTabFragmentAdapter adapter;
    private ArrayList<Tweet> tweets;
    private Context context;
    private int loadCount; // how many times tweets loaded
    private boolean loading;
    private int accountNumber;
    private int itemCount;
    private boolean hasNext;
    Twitter twitter;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mention_tab, container, false);
        context = getActivity();
        mSharedPreferences = context.getApplicationContext().getSharedPreferences(
                PREFERENCE_NAME, 0);
        tweets = new ArrayList<>();
        accountNumber = mSharedPreferences.getInt(PREF_CURRENT_ACCOUNT, 0);
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_SECRET);
        builder.setTweetModeExtended(true); // so that text not cut off
        String access_token = mSharedPreferences.getString(FollowListMain.TOKEN_LIST[accountNumber], "");
        String access_token_secret = mSharedPreferences.getString(FollowListMain.SECRET_LIST[accountNumber], "");
        AccessToken accessToken = new AccessToken(access_token, access_token_secret);
        twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

        setUpListView(view);


        hasNext = true;
        loadMention();
        return view;
    }

    private void setUpListView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.mention_recycler_view);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean listHasValue = itemCount > 15;
                if (hasNext && !loading && listHasValue) { // load only if more people available
                    int visibleItemCount = recyclerView.getChildCount();
                    LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int firstVisibleItem = manager.findFirstVisibleItemPosition();
                    boolean atEnd = firstVisibleItem + visibleItemCount >= itemCount;
                    if (atEnd) { // load more tweets if at the end of listview
                        loading = true;
                        loadMention();
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.mention_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tweets = new ArrayList<>();
                loadCount = 0;

                if (!loading) {
                    loading = true;
                    loadMention();
                }
            }
        });
    }

    private void loadMention() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Paging pg = new Paging(++loadCount, 40); // 1 page, 40 tweets
                    ResponseList<twitter4j.Status> homeTl = twitter.getMentionsTimeline(pg);// loading each tweet and put in array
                    if (homeTl.size()<0){
                        hasNext = false;
                        cancel(true);
                    }

                    for (twitter4j.Status status : homeTl) { // taking one tweet from the array
                        Tweet tweet1 = new Tweet(); //tweet object

                        User user = status.getUser();
                        tweet1.setUser(user.getName());
                        tweet1.setScreenName(user.getScreenName());
                        tweet1.setProf_image(user.getBiggerProfileImageURL());
                        tweet1.setContent(status.getText());
                        tweet1.setTime(status.getCreatedAt());
                        tweet1.setTweetId(status.getId());
                        tweet1.setLikeNum(String.valueOf(status.getFavoriteCount()));
                        tweet1.setRetweetNum(String.valueOf(status.getRetweetCount()));

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
                        notifyUser("This account is private", context);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                if (loadCount == 1) {
                    adapter = new MentionTabFragmentAdapter(getContext(), tweets);
                    adapter.setItemClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = recyclerView.getChildAdapterPosition(v);
                            Gson gson = new Gson();
                            Tweet t = tweets.get(position);
                            String selectedTweetJson = gson.toJson(t);
                            Intent i = new Intent(getContext(), TweetDetailActivity.class);
                            i.putExtra(SELECTED_TWEET, selectedTweetJson);
                            //i.putExtra(SELECTED_PREVIOUS_TWEET, 0l);
                            startActivity(i);
                        }
                    });
                    adapter.setMediaClickedListener(MentionTabFragment.this);
                    adapter.setOnIconClickListener(MentionTabFragment.this);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else adapter.notifyDataSetChanged();
                itemCount=tweets.size();
                loading = false;
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
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

    @Override
    public void onMediaClicked(int position, int mediaPos) {
        if (mediaPos==4){//means it's youtube
            if (YouTubeIntents.canResolvePlayVideoIntentWithOptions(getContext())){
                startActivity(YouTubeIntents.createPlayVideoIntentWithOptions(getContext(),tweets.get(position).getYoutubeId(),false,true));
            }
        }else { // means picture, video,gif
            String videoURL = tweets.get(position).getMedia_vidoes();
            String instaURL = tweets.get(position).getInstaUrl();

            if (videoURL != null) { // video
                Intent videoIntent = new Intent(getContext(), VideoActivity.class);
                videoIntent.putExtra(VideoActivity.MEDIA_VIDEO,videoURL);
                startActivity(videoIntent);
            } else { // picture
                Intent intent = new Intent(getContext(), ShowMediaActivity.class);
                String[] urls;
                if (instaURL!=null){
                    urls = new String[]{instaURL};
                }else {
                    ArrayList<String> arrayList = tweets.get(position).getMedia_images();
                    urls = arrayList.toArray(new String[arrayList.size()]);
                }
                intent.putExtra(ShowMediaActivity.MEDIA_POSITION, urls);
                intent.putExtra(ShowMediaActivity.MEDIA_POS,mediaPos);
                startActivity(intent);
            }
        }
    }

    @Override
    public void userIconClicked(int position) {
        Intent i = new Intent(getContext(), UserPage.class);
        i.putExtra(SCREEN_NAME, tweets.get(position).getScreenName());
        startActivity(i);
    }

    private void notifyUser(final String error, final Context context){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,error,Toast.LENGTH_SHORT).show();
            }
        });
    }

}
