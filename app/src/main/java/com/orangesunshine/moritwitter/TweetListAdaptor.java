package com.orangesunshine.moritwitter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.simleman.moritwitter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hayatomoritani on 10/28/16.
 */

public class TweetListAdaptor extends BaseAdapter {
    private ArrayList<Tweet> tweets;
    private Context context;
    private TlMediaClickedListener mediaClickedListener;
    private TlIconClickListener iconClickListener;
    private TweetActionListener actionListener;

    public TweetListAdaptor(Context context, ArrayList<Tweet> items) {
        this.tweets = items;
        this.context = context;
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
            viewHolder = new ViewHolder();
            viewHolder.tt = (TextView) convertView.findViewById(R.id.toptext);
            viewHolder.bt= (TextView) convertView.findViewById(R.id.bottomtext);
            viewHolder.ScreenNameView= (TextView) convertView.findViewById(R.id.tl_screen_name);
            viewHolder.timeView= (TextView) convertView.findViewById(R.id.tl_time);
            viewHolder.replyButton = (ImageView)convertView.findViewById(R.id.tl_reply_button);
            viewHolder.retweetNum = (TextView)convertView.findViewById(R.id.tl_retweet_num);
            viewHolder.retweetButton = (ImageView)convertView.findViewById(R.id.tl_retweet_button);
            viewHolder.favNum = (TextView)convertView.findViewById(R.id.tl_fav_num);
            viewHolder.favButton = (ImageView)convertView.findViewById(R.id.tl_fav_button);
            viewHolder.prof_pic = (ImageView) convertView.findViewById(R.id.prof_pic_tl);
            viewHolder.timeLineMedia1 = (ImageView)convertView.findViewById(R.id.timeline_media1);
            viewHolder.timeLineMedia2 = (ImageView)convertView.findViewById(R.id.timeline_media2);
            viewHolder.timeLineMedia3 = (ImageView)convertView.findViewById(R.id.timeline_media3);
            viewHolder.timeLineMedia4 = (ImageView)convertView.findViewById(R.id.timeline_media4);

            viewHolder.reTweetedByContainer = (RelativeLayout)convertView.findViewById(R.id.tl_retweeted_by_container);
            viewHolder.reTweetedBy = (TextView)convertView.findViewById(R.id.tl_retweeted_by);

            viewHolder.videoView = (VideoView) convertView.findViewById(R.id.tl_video);
            viewHolder.videoPlayButton = (ImageView) convertView.findViewById(R.id.tl_video_play_button);

            viewHolder.thumbContainer = (RelativeLayout) convertView.findViewById(R.id.tl_youtube_container);
            viewHolder.thumbView = (ImageView) convertView.findViewById(R.id.tl_youtube_thumb);
            viewHolder.playButton =(ImageView) convertView.findViewById(R.id.tl_youtube_play_button);

            viewHolder.quoteContainer = (RelativeLayout)convertView.findViewById(R.id.tl_quote_container);
            viewHolder.quoteName = (TextView)convertView.findViewById(R.id.tl_quote_name);
            viewHolder.quoteContent = (TextView)convertView.findViewById(R.id.tl_quote_content);

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

    private static class ViewHolder{
        TextView tt;
        TextView bt;
        TextView ScreenNameView;
        TextView timeView;
        ImageView prof_pic;
        ImageView replyButton;
        TextView retweetNum; ImageView retweetButton;
        TextView favNum; ImageView favButton;

        ImageView timeLineMedia1;
        ImageView timeLineMedia2;
        ImageView timeLineMedia3;
        ImageView timeLineMedia4;

        RelativeLayout reTweetedByContainer;
        TextView reTweetedBy;

        VideoView videoView;
        ImageView videoPlayButton;
        RelativeLayout thumbContainer;
        ImageView thumbView;
        ImageView playButton;

        RelativeLayout quoteContainer;
        TextView quoteName;
        TextView quoteContent;
    }

    public interface TlMediaClickedListener {void onMediaClicked(int position, int mediaPos);}
    public void setMediaClickedListener(TlMediaClickedListener listener) {mediaClickedListener = listener;}

    public interface TlIconClickListener{void userIconClicked(int position);}
    public void setOnIconClickListener(TlIconClickListener listener){iconClickListener = listener;}

    public interface TweetActionListener{void onClick(int position,int type);}
    public void setOnTweetActionListener(TweetActionListener listener){actionListener = listener;}
}
