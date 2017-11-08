package com.orangesunshine.moritwitter;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.simleman.moritwitter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hayatomoritani on 12/7/16.
 */

public class UserPageListAdapter extends BaseAdapter {

    private Context context;
    private String userName;
    private ArrayList<Tweet> items;
    private MediaClickedListener mediaClickedListener;
    private IconClickListener iconClickListener;
    private TweetActionListener actionListener;


    public UserPageListAdapter(Context context, ArrayList<Tweet> items,String userName) {
        this.context = context;
        this.userName = userName;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Tweet getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MyViewHolder myViewHolder;
        Tweet tweet = items.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_page_list_item, parent, false);
            myViewHolder = new MyViewHolder();
            myViewHolder.tweetView = (TextView) convertView.findViewById(R.id.user_page_tweet);
            myViewHolder.tweetTime = (TextView) convertView.findViewById(R.id.user_page_time);
            myViewHolder.replyButton = (ImageView) convertView.findViewById(R.id.user_page_reply_button);
            myViewHolder.retweetNum = (TextView)convertView.findViewById(R.id.user_page_retweet_num);
            myViewHolder.retweetButton = (ImageView) convertView.findViewById(R.id.user_page_retweet_button);
            myViewHolder.favNum = (TextView)convertView.findViewById(R.id.user_page_fav_num);
            myViewHolder.favButton = (ImageView) convertView.findViewById(R.id.user_page_fav_button);
            myViewHolder.reTweetedByContainer = (RelativeLayout)convertView.findViewById(R.id.user_page_retweeted_by_container);
            myViewHolder.reTweetedBy = (TextView)convertView.findViewById(R.id.user_page_retweeted_by);
            myViewHolder.timeLineMedia1 = (ImageView) convertView.findViewById(R.id.user_page_media1);
            myViewHolder.timeLineMedia2 = (ImageView) convertView.findViewById(R.id.user_page_media2);
            myViewHolder.timeLineMedia3 = (ImageView) convertView.findViewById(R.id.user_page_media3);
            myViewHolder.timeLineMedia4 = (ImageView) convertView.findViewById(R.id.user_page_media4);

            myViewHolder.imageLinearLayout1 = (LinearLayout) convertView.findViewById(R.id.user_page_image_linear_1);
            myViewHolder.imageLinearLayout2 = (LinearLayout) convertView.findViewById(R.id.user_page_image_linear_2);

            myViewHolder.videoView = (ImageView) convertView.findViewById(R.id.user_page_video);
            myViewHolder.videoPlayButton = (ImageView) convertView.findViewById(R.id.user_page_video_play_button);
            myViewHolder.thumbView = (ImageView) convertView.findViewById(R.id.user_page_youtube_thumb);
            myViewHolder.playButton =(ImageView) convertView.findViewById(R.id.user_page_youtube_play_button);
            //myViewHolder.thumbView.setTag(tweet.getYoutubeId());
            myViewHolder.thumbContainer = (RelativeLayout) convertView.findViewById(R.id.user_page_youtube_container);

            myViewHolder.reTweetContainer = (RelativeLayout)convertView.findViewById(R.id.user_page_retweet_container);
            myViewHolder.reTweetedByContainer = (RelativeLayout)convertView.findViewById(R.id.user_page_retweeted_by_container);
            myViewHolder.reTweetedBy = (TextView)convertView.findViewById(R.id.user_page_retweeted_by);
            myViewHolder.reTweetIcon = (ImageView)convertView.findViewById(R.id.user_page_retweet_icon);
            myViewHolder.reTweetName= (TextView)convertView.findViewById(R.id.user_page_retweet_name);
            myViewHolder.reTweetScreenName= (TextView)convertView.findViewById(R.id.user_page_retweet_screen_name);

            myViewHolder.quoteContainer = (RelativeLayout)convertView.findViewById(R.id.user_page_quote_container);
            myViewHolder.quoteName = (TextView)convertView.findViewById(R.id.user_page_quote_name);
            myViewHolder.quoteContent = (TextView)convertView.findViewById(R.id.user_page_quote_content);

            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder) convertView.getTag();
        }

        //check if the tweet is retweeted tweet
        if (tweet.isRetweet()){
            myViewHolder.reTweetedByContainer.setVisibility(View.VISIBLE);
            myViewHolder.reTweetContainer.setVisibility(View.VISIBLE);
            myViewHolder.reTweetedBy.setVisibility(View.VISIBLE);
            String name = "retweeted by "+userName;
            myViewHolder.reTweetedBy.setText(name);
            myViewHolder.reTweetName.setText(tweet.getUser());
            myViewHolder.reTweetScreenName.setText(tweet.getScreenName());
            if (tweet.getRetweetedId()!=0){
                myViewHolder.retweetButton.setImageResource(R.drawable.ic_undo_retweet_button);
            }
            Picasso.with(context).load(tweet.getProf_image()).into(myViewHolder.reTweetIcon);
            myViewHolder.reTweetIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (iconClickListener!=null){
                        iconClickListener.userIconClicked(position);
                    }
                }
            });
        }else {
            myViewHolder.reTweetedByContainer.setVisibility(View.GONE);
            myViewHolder.reTweetContainer.setVisibility(View.GONE);
            myViewHolder.reTweetedBy.setVisibility(View.GONE);
            myViewHolder.reTweetIcon.setImageDrawable(null);
        }

        if (tweet.isQuoted()){
            myViewHolder.quoteContainer.setVisibility(View.VISIBLE);
            myViewHolder.quoteContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(actionListener!=null){actionListener.onClick(position,3);} // 3 means quate
                }
            });
            myViewHolder.quoteName.setText(tweet.getQuoteName());
            myViewHolder.quoteContent.setText(tweet.getQuoteContent());
        }else {
            myViewHolder.quoteContainer.setVisibility(View.GONE);
        }

        myViewHolder.tweetView.setText(tweet.getContent());
        myViewHolder.tweetTime.setText(getTimeAgo(tweet, context));
        myViewHolder.retweetNum.setText(tweet.getRetweetNum());
        myViewHolder.favNum.setText(tweet.getLikeNum());

        if (tweet.isReTweeted()){ // if retweeted by me
            myViewHolder.retweetButton.setImageResource(R.drawable.ic_undo_retweet_button);
        }else {
            myViewHolder.retweetButton.setImageResource(R.drawable.ic_retweet_button);
        }
        myViewHolder.replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionListener!=null){actionListener.onClick(position,0);}
            }
        });
        myViewHolder.retweetNum.setText(tweet.getRetweetNum());
        myViewHolder.retweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionListener!=null){actionListener.onClick(position,1);}
            }
        });
        myViewHolder.favNum.setText(tweet.getLikeNum());
        myViewHolder.favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionListener!=null){actionListener.onClick(position,2);}
            }
        });
        if (tweet.isFaved()){myViewHolder.favButton.setImageResource(R.drawable.ic_favarit_on);}
        else {myViewHolder.favButton.setImageResource(R.drawable.ic_favarit_off);}
        //if (position==0){Log.d("tag", "UPLAdapter getView: at0 "+tweet.getRetweetNum());}

        myViewHolder.timeLineMedia1.setImageDrawable(null);
        myViewHolder.timeLineMedia2.setImageDrawable(null);
        myViewHolder.timeLineMedia3.setImageDrawable(null);
        myViewHolder.timeLineMedia4.setImageDrawable(null);
        myViewHolder.videoView.setImageDrawable(null);
        final ArrayList<String> mediaList = tweet.getMedia_images(); // get pictures
        if (mediaList != null && mediaList.size() > 0) { // picture or video
            myViewHolder.imageLinearLayout1.setVisibility(View.VISIBLE);
            myViewHolder.videoView.setVisibility(View.GONE);
            if (tweet.getMedia_vidoes()==null){
                myViewHolder.videoPlayButton.setVisibility(View.GONE);
            }else {
                myViewHolder.videoPlayButton.setVisibility(View.VISIBLE);
            }
            Picasso.with(context).load(mediaList.get(0)).into(myViewHolder.timeLineMedia1);
            myViewHolder.timeLineMedia1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaClickedListener != null) {
                        mediaClickedListener.onMediaClicked(position, 0);
                    }
                }
            });
            if (mediaList.size() > 1) {
                Picasso.with(context).load(mediaList.get(1)).into(myViewHolder.timeLineMedia2);
                myViewHolder.timeLineMedia2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaClickedListener != null) {
                            mediaClickedListener.onMediaClicked(position, 1);
                        }
                    }
                });
                if (mediaList.size() > 2) {
                    myViewHolder.imageLinearLayout2.setVisibility(View.VISIBLE);
                    Picasso.with(context).load(mediaList.get(2)).into(myViewHolder.timeLineMedia3);
                    myViewHolder.timeLineMedia3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mediaClickedListener != null) {
                                mediaClickedListener.onMediaClicked(position, 2);
                            }
                        }
                    });
                    if (mediaList.size() > 3) {
                        myViewHolder.timeLineMedia4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mediaClickedListener != null) {
                                    mediaClickedListener.onMediaClicked(position, 3);
                                }
                            }
                        });
                        Picasso.with(context).load(mediaList.get(3)).into(myViewHolder.timeLineMedia4);
                    }
                } else myViewHolder.imageLinearLayout2.setVisibility(View.GONE);
            }else myViewHolder.imageLinearLayout2.setVisibility(View.GONE);
        } else if (tweet.getMedia_gifs() != null) { // if gif as media
            myViewHolder.imageLinearLayout1.setVisibility(View.GONE);
            myViewHolder.imageLinearLayout2.setVisibility(View.GONE);
            myViewHolder.thumbContainer.setVisibility(View.GONE);
            ImageView myVideoView = myViewHolder.videoView;
            myVideoView.setVisibility(View.VISIBLE);
            Picasso.with(context).load(tweet.getMedia_gifs()).into(myVideoView);

            //playing gif on videoview stops music from other app. show thumb
            //String gifUrl = tweet.getMedia_gifs().replace("tweet_video_thumb", "tweet_video").replace(".png", ".mp4").replace(".jpg", ".mp4").replace(".jpeg", ".mp4");
//            final VideoView myVideoView = myViewHolder.videoView;
//            myVideoView.setVisibility(View.VISIBLE);
//            String gifUrl = tweet.getMedia_gifs().replace("tweet_video_thumb", "tweet_video").replace(".png", ".mp4").replace(".jpg", ".mp4").replace(".jpeg", ".mp4");
//            myVideoView.setVideoURI(Uri.parse(gifUrl));
//            //myViewHolder.videoView.requestFocus();
//            myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    myVideoView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (myVideoView.isPlaying()){
//                                myVideoView.pause();
//                            }else {
//                                myVideoView.start();
//                            }
//                        }
//                    });
//                }
//            });




        } else if (tweet.getInstaUrl()!=null){
            myViewHolder.imageLinearLayout1.setVisibility(View.VISIBLE);
            myViewHolder.imageLinearLayout2.setVisibility(View.GONE);
            Picasso.with(context).load(tweet.getInstaUrl()).into(myViewHolder.timeLineMedia1);
            myViewHolder.timeLineMedia1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaClickedListener != null) {
                        mediaClickedListener.onMediaClicked(position, 0);
                    }
                }
            });
        }else { // if no media
            myViewHolder.imageLinearLayout1.setVisibility(View.GONE);
            myViewHolder.imageLinearLayout2.setVisibility(View.GONE);
            myViewHolder.thumbContainer.setVisibility(View.GONE);
            myViewHolder.videoView.setVisibility(View.GONE);
        }
        if (tweet.getYoutubeId() != null) {
            myViewHolder.thumbContainer.setVisibility(View.VISIBLE);
            String YTThumb = "https://img.youtube.com/vi/"+tweet.getYoutubeId()+"/hqdefault.jpg";
            //Log.d("tag", "UPLA getview: "+tweet.getYoutubeId());
            Picasso.with(context).load(YTThumb).into(myViewHolder.thumbView);
            myViewHolder.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaClickedListener!=null){
                        mediaClickedListener.onMediaClicked(position,4);
                    }
                }
            });
        } else {
            myViewHolder.thumbContainer.setVisibility(View.GONE);
        }

        return convertView;
    }


    private String getTimeAgo(Tweet tweet, Context context) {
        long timeRepliedInMillis = tweet.getTime().getTime();
        long now = System.currentTimeMillis();
        long resolution = DateUtils.MINUTE_IN_MILLIS; // less than a minuet will be zero

        String diff;

        if (DateUtils.isToday(timeRepliedInMillis)) {
            diff = DateUtils.getRelativeTimeSpanString(timeRepliedInMillis, now, resolution).toString();
        } else {
            long transitionRes = DateUtils.WEEK_IN_MILLIS; // show date if more than 7days
            diff = DateUtils.getRelativeDateTimeString(context, timeRepliedInMillis, resolution, transitionRes, 0).toString();
        }
        return diff;
    }

//    @Override
//    public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
//        mLoaders.put(youTubeThumbnailView, youTubeThumbnailLoader);
//        youTubeThumbnailLoader.setVideo((String) youTubeThumbnailView.getTag());
//        youTubeThumbnailLoader.release();
//    }
//
//    @Override
//    public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
//        Log.d("tag", "uplistAdp: yt fail "+youTubeInitializationResult);
//    }

    private static class MyViewHolder {
        TextView tweetView;
        TextView tweetTime;

        ImageView replyButton;
        TextView retweetNum;ImageView retweetButton;
        TextView favNum;ImageView favButton;


        LinearLayout imageLinearLayout1;
        LinearLayout imageLinearLayout2;
        ImageView timeLineMedia1;
        ImageView timeLineMedia2;
        ImageView timeLineMedia3;
        ImageView timeLineMedia4;

        ImageView videoView;
        ImageView videoPlayButton;
        RelativeLayout thumbContainer;
        ImageView thumbView;
        ImageView playButton;

        RelativeLayout reTweetedByContainer;
        TextView reTweetedBy;
        RelativeLayout reTweetContainer;
        TextView reTweetName;
        TextView reTweetScreenName;
        ImageView reTweetIcon;

        RelativeLayout quoteContainer;
        TextView quoteName;
        TextView quoteContent;
    }

    public interface MediaClickedListener {void onMediaClicked(int position, int mediaPos);}
    public void setMediaClickedListener(MediaClickedListener listener) {mediaClickedListener = listener;}

    interface IconClickListener{void userIconClicked(int position);}
    void setOnIconClickListener(IconClickListener listener){
        iconClickListener = listener;
    }

    public interface TweetActionListener{void onClick(int position, int type);}
    public void setOnTweetActionListener(TweetActionListener listener){actionListener = listener;}
}
