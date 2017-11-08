package com.orangesunshine.moritwitter.mention_tab;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.orangesunshine.moritwitter.Tweet;
import com.simleman.moritwitter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hayatomoritani on 6/2/17.
 */


public class MentionTabFragmentAdapter extends RecyclerView.Adapter<MentionTabFragmentAdapter.Holder> {
    private Context context;
    private List<Tweet> tweets;
    private View.OnClickListener itemListener;
    private MentionMediaClickedListener mediaClickedListener;
    private MentionIconClickListener iconClickListener;

    public MentionTabFragmentAdapter(Context context,List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.fragment_mention_tab_item,parent,false);
        if (itemListener!=null){
            v.setOnClickListener(itemListener);
        }
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(final Holder viewHolder, int position) {

        Tweet o = tweets.get(position);
        if (o.getMedia_vidoes()==null){
            viewHolder.videoPlayButton.setVisibility(View.GONE);
        }else {
            viewHolder.videoPlayButton.setVisibility(View.VISIBLE);
        }

        viewHolder.content.setText(o.getContent());
        viewHolder.userName.setText(o.getUser());
        viewHolder.screenName.setText(o.getScreenName());
        viewHolder.timeView.setText(getTimeAgo(o,context));
        Picasso.with(context).load(o.getProf_image()).resize(140,140).into(viewHolder.prof_pic);
        if (iconClickListener!=null){
            viewHolder.prof_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iconClickListener.userIconClicked(viewHolder.getAdapterPosition());
                }
            });
        }

        ArrayList<String> mediaList = o.getMedia_images();

        viewHolder.timeLineMedia1.setImageDrawable(null);
        viewHolder.timeLineMedia2.setImageDrawable(null);
        viewHolder.timeLineMedia3.setImageDrawable(null);
        viewHolder.timeLineMedia4.setImageDrawable(null);
        viewHolder.thumbContainer.setVisibility(View.GONE);
        viewHolder.videoView.setVisibility(View.GONE);
        if (mediaList != null && mediaList.size() > 0){
            Picasso.with(context).load(mediaList.get(0)).into(viewHolder.timeLineMedia1);
            if (mediaClickedListener != null) {
                viewHolder.timeLineMedia1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {mediaClickedListener.onMediaClicked(viewHolder.getAdapterPosition(),0);}
                });
            }
            if (mediaList.size()>1){
                Picasso.with(context).load(mediaList.get(1)).into(viewHolder.timeLineMedia2);
                if (mediaClickedListener != null) {
                    viewHolder.timeLineMedia2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {mediaClickedListener.onMediaClicked(viewHolder.getAdapterPosition(),1);}
                    });
                }
                if (mediaList.size()>2){
                    Picasso.with(context).load(mediaList.get(2)).into(viewHolder.timeLineMedia3);
                    if (mediaClickedListener != null) {
                        viewHolder.timeLineMedia3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {mediaClickedListener.onMediaClicked(viewHolder.getAdapterPosition(),2);}
                        });
                    }
                    if (mediaList.size()>3){
                        Picasso.with(context).load(mediaList.get(3)).into(viewHolder.timeLineMedia4);
                        if (mediaClickedListener != null) {
                            viewHolder.timeLineMedia3.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {mediaClickedListener.onMediaClicked(viewHolder.getAdapterPosition(),3);}
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
                    public void onClick(View v) {mediaClickedListener.onMediaClicked(viewHolder.getAdapterPosition(),0);}
                });
            }
        }

        if (o.getYoutubeId() != null) {
            viewHolder.thumbContainer.setVisibility(View.VISIBLE);
            String YTThumb = "https://img.youtube.com/vi/"+o.getYoutubeId()+"/hqdefault.jpg";
            Picasso.with(context).load(YTThumb).into(viewHolder.thumbView);
            viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaClickedListener!=null){
                        mediaClickedListener.onMediaClicked(viewHolder.getAdapterPosition(),4);
                    }
                }
            });
        }
    }

    private String getTimeAgo(Tweet tweet, Context context) {
        long timeRepliedInMillis = tweet.getTime().getTime();
        long now = System.currentTimeMillis();
        long resolution = DateUtils.MINUTE_IN_MILLIS; // less than a minuet will be zer
        String diff;
        if (DateUtils.isToday(timeRepliedInMillis)) {
            diff = DateUtils.getRelativeTimeSpanString(timeRepliedInMillis, now, resolution).toString();
        } else {
            long transitionRes = DateUtils.WEEK_IN_MILLIS; // show date if more than 7days
            diff = DateUtils.getRelativeDateTimeString(context, timeRepliedInMillis, resolution, transitionRes, 0).toString();
        }
        return diff;
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    interface MentionMediaClickedListener {
        void onMediaClicked(int position, int mediaPos);
    }


    void setOnIconClickListener(MentionIconClickListener listener){
        iconClickListener = listener;
    }

    interface MentionIconClickListener{
        void userIconClicked(int position);
    }
    void setMediaClickedListener(MentionMediaClickedListener listener) {
        mediaClickedListener = listener;
    }

    void setItemClickListener(View.OnClickListener listener){
        itemListener = listener;
    }
    static class Holder extends RecyclerView.ViewHolder {
        TextView content;
        TextView userName;
        TextView screenName;
        TextView timeView;
        ImageView prof_pic;

        ImageView timeLineMedia1;
        ImageView timeLineMedia2;
        ImageView timeLineMedia3;
        ImageView timeLineMedia4;

        VideoView videoView;
        ImageView videoPlayButton;

        RelativeLayout thumbContainer;
        ImageView thumbView;
        ImageView playButton;
        public Holder(View convertView) {
            super(convertView);
            content = (TextView) convertView.findViewById(R.id.mention_content);
            userName= (TextView) convertView.findViewById(R.id.mention_name);
            screenName= (TextView) convertView.findViewById(R.id.mention_screen_name);
            timeView= (TextView) convertView.findViewById(R.id.mention_time);
            prof_pic = (ImageView) convertView.findViewById(R.id.mention_icon);
            timeLineMedia1 = (ImageView)convertView.findViewById(R.id.mention_media1);
            timeLineMedia2 = (ImageView)convertView.findViewById(R.id.mention_media2);
            timeLineMedia3 = (ImageView)convertView.findViewById(R.id.mention_media3);
            timeLineMedia4 = (ImageView)convertView.findViewById(R.id.mention_media4);
            videoView = (VideoView) convertView.findViewById(R.id.mention_video);
            videoPlayButton = (ImageView) convertView.findViewById(R.id.mention_video_play_button);
            thumbContainer = (RelativeLayout) convertView.findViewById(R.id.mention_youtube_container);
            thumbView = (ImageView) convertView.findViewById(R.id.mention_youtube_thumb);
            playButton =(ImageView) convertView.findViewById(R.id.mention_youtube_play_button);
        }
    }
}
