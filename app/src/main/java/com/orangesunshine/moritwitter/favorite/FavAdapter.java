package com.orangesunshine.moritwitter.favorite;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.orangesunshine.moritwitter.Tweet;
import com.simleman.moritwitter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import twitter4j.Status;


public class FavAdapter extends BaseAdapter {

    private Context context;
    private List<Tweet> tweets;
    private FavMediaClickedListener mediaClickedListener;
    private FavIconClickListener iconClickListener;
    private FavTweetActionListener actionListener;

    public FavAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    @Override
    public int getCount() {
        return tweets.size();
    }

    @Override
    public Object getItem(int position) {
        return tweets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Tweet o = tweets.get(position);
        ViewHolder viewHolder;
        if (convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if (o.getMedia_vidoes()==null){
            viewHolder.videoPlayButton.setVisibility(View.GONE);
        }else {
            viewHolder.videoPlayButton.setVisibility(View.VISIBLE);
        }
        if (o.isRetweet()){ // if its retweet
            viewHolder.reTweetedByContainer.setVisibility(View.VISIBLE);
            String name = "retweeted by "+o.getReTweetedByName();
            viewHolder.reTweetedBy.setText(name);
        }else viewHolder.reTweetedByContainer.setVisibility(View.GONE);
        if (o.isReTweeted()){ // if retweeted by me
            viewHolder.retweetButton.setImageResource(R.drawable.ic_undo_retweet_button);
        }else {
            viewHolder.retweetButton.setImageResource(R.drawable.ic_retweet_button);
        }

        viewHolder.tt.setText(o.getContent());
        viewHolder.bt.setText(o.getUser());
        viewHolder.ScreenNameView.setText(o.getScreenName());
        viewHolder.timeView.setText(getTimeAgo(o,context));
        viewHolder.replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionListener!=null){actionListener.onClick(position,0);}
            }
        });
        viewHolder.retweetNum.setText(o.getRetweetNum());
        viewHolder.retweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionListener!=null){actionListener.onClick(position,1);}
            }
        });

        viewHolder.favNum.setText(o.getLikeNum());
        if (o.isFaved()){viewHolder.favButton.setImageResource(R.drawable.ic_favarit_on);}
        else {viewHolder.favButton.setImageResource(R.drawable.ic_favarit_off);}
        viewHolder.favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionListener!=null){actionListener.onClick(position,2);}
            }
        });
        if (o.isQuoted()){
            viewHolder.quoteContainer.setVisibility(View.VISIBLE);
            viewHolder.quoteContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(actionListener!=null){actionListener.onClick(position,3);} // 3 means quate
                }
            });
            viewHolder.quoteName.setText(o.getQuoteName());
            viewHolder.quoteContent.setText(o.getQuoteContent());
        }else {
            viewHolder.quoteContainer.setVisibility(View.GONE);
        }
        Picasso.with(context).load(o.getProf_image()).into(viewHolder.prof_pic);
        if (iconClickListener!=null){
            viewHolder.prof_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iconClickListener.userIconClicked(position);
                }
            });
        }

        ArrayList<String> mediaList = o.getMedia_images();

        viewHolder.timeLineMedia1.setVisibility(View.GONE);
        viewHolder.timeLineMedia2.setVisibility(View.GONE);
        viewHolder.timeLineMedia3.setVisibility(View.GONE);
        viewHolder.timeLineMedia4.setVisibility(View.GONE);
        viewHolder.thumbContainer.setVisibility(View.GONE);
        viewHolder.videoView.setVisibility(View.GONE);
        if (mediaList != null && mediaList.size() > 0){
            viewHolder.timeLineMedia1.setVisibility(View.VISIBLE);
            Picasso.with(context).load(mediaList.get(0)).placeholder(R.drawable.lv_placeholder).into(viewHolder.timeLineMedia1);
            if (mediaClickedListener != null) {
                viewHolder.timeLineMedia1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {mediaClickedListener.onMediaClicked(position,0);}
                });
            }
            if (mediaList.size()>1){
                viewHolder.timeLineMedia2.setVisibility(View.VISIBLE);
                Picasso.with(context).load(mediaList.get(1)).placeholder(R.drawable.lv_placeholder).into(viewHolder.timeLineMedia2);
                if (mediaClickedListener != null) {
                    viewHolder.timeLineMedia2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {mediaClickedListener.onMediaClicked(position,1);}
                    });
                }
                if (mediaList.size()>2){
                    viewHolder.timeLineMedia3.setVisibility(View.VISIBLE);
                    Picasso.with(context).load(mediaList.get(2)).placeholder(R.drawable.lv_placeholder).into(viewHolder.timeLineMedia3);
                    if (mediaClickedListener != null) {
                        viewHolder.timeLineMedia3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {mediaClickedListener.onMediaClicked(position,2);}
                        });
                    }
                    if (mediaList.size()>3){
                        viewHolder.timeLineMedia4.setVisibility(View.VISIBLE);
                        Picasso.with(context).load(mediaList.get(3)).placeholder(R.drawable.lv_placeholder).into(viewHolder.timeLineMedia4);
                        if (mediaClickedListener != null) {
                            viewHolder.timeLineMedia3.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {mediaClickedListener.onMediaClicked(position,3);}
                            });
                        }
                    }
                }
            }
        }else if (o.getMedia_gifs() != null){
            final VideoView myVideoView = viewHolder.videoView;
            myVideoView.setVisibility(View.VISIBLE);
            String gifUrl = o.getMedia_gifs().replace("tweet_video_thumb", "tweet_video").replace(".png", ".mp4").replace(".jpg", ".mp4").replace(".jpeg", ".mp4");
            myVideoView.setVideoURI(Uri.parse(gifUrl));
            viewHolder.videoView.requestFocus();
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
        }else if (o.getInstaUrl()!=null){
            Picasso.with(context).load(o.getInstaUrl()).into(viewHolder.timeLineMedia1);
            if (mediaClickedListener != null) {
                viewHolder.timeLineMedia1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {mediaClickedListener.onMediaClicked(position,0);}
                });
            }
        }

        if (o.getYoutubeId() != null&&!o.getYoutubeId().contains("//")) {
            viewHolder.thumbContainer.setVisibility(View.VISIBLE);
            String YTThumb = "https://img.youtube.com/vi/"+o.getYoutubeId()+"/hqdefault.jpg";
            Picasso.with(context).load(YTThumb).into(viewHolder.thumbView);
            viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaClickedListener!=null){
                        mediaClickedListener.onMediaClicked(position,4);
                    }
                }
            });
        }

        return convertView;
    }

    static class ViewHolder{

        @BindView(R.id.toptext) TextView tt;
        @BindView(R.id.bottomtext)TextView bt;
        @BindView(R.id.tl_screen_name)TextView ScreenNameView;
        @BindView(R.id.tl_time)TextView timeView;
        @BindView(R.id.prof_pic_tl)ImageView prof_pic;
        @BindView(R.id.tl_reply_button)ImageView replyButton;
        @BindView(R.id.tl_retweet_num)TextView retweetNum; @BindView(R.id.tl_retweet_button)ImageView retweetButton;
        @BindView(R.id.tl_fav_num)TextView favNum; @BindView(R.id.tl_fav_button)ImageView favButton;

        @BindView(R.id.timeline_media1)ImageView timeLineMedia1;
        @BindView(R.id.timeline_media2)ImageView timeLineMedia2;
        @BindView(R.id.timeline_media3)ImageView timeLineMedia3;
        @BindView(R.id.timeline_media4)ImageView timeLineMedia4;

        @BindView(R.id.tl_retweeted_by_container)RelativeLayout reTweetedByContainer;
        @BindView(R.id.tl_retweeted_by)TextView reTweetedBy;

        @BindView(R.id.tl_video)VideoView videoView;
        @BindView(R.id.tl_video_play_button)ImageView videoPlayButton;
        @BindView(R.id.tl_youtube_container)RelativeLayout thumbContainer;
        @BindView(R.id.tl_youtube_thumb)ImageView thumbView;
        @BindView(R.id.tl_youtube_play_button)ImageView playButton;

        @BindView(R.id.tl_quote_container)RelativeLayout quoteContainer;
        @BindView(R.id.tl_quote_name)TextView quoteName;
        @BindView(R.id.tl_quote_content)TextView quoteContent;

        ViewHolder(View view) {
            ButterKnife.bind(this,view);
        }
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

    void swapData(List<Tweet> tweets){
        this.tweets = tweets;
        notifyDataSetChanged();
    }

     interface FavMediaClickedListener {void onMediaClicked(int position, int mediaPos);}
    public void setMediaClickedListener(FavMediaClickedListener listener) {mediaClickedListener = listener;}

     interface FavIconClickListener{void userIconClicked(int position);}
    public void setOnIconClickListener(FavIconClickListener listener){iconClickListener = listener;}

     interface FavTweetActionListener{void onClick(int position,int type);}
    public void setOnTweetActionListener(FavTweetActionListener listener){actionListener = listener;}

}
