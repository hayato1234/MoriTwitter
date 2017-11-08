package com.orangesunshine.moritwitter.search;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.simleman.moritwitter.R;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.User;

/**
 * Created by hayatomoritani on 7/13/17.
 */

public class SearchTweetAdapter extends RecyclerView.Adapter<SearchTweetAdapter.ViewHolder> {

    private STIconClickListener iconClickListener;
    private STTweetActionListener actionListener;
    private STItemClickListener stItemClickListener;

    private List<Status> tweets;
    private Context context;
    public SearchTweetAdapter(List<Status> tweets,Context context){
        this.tweets = tweets;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stItemClickListener!=null){
                    stItemClickListener.itemClicked(holder.getAdapterPosition());
                }else Log.d("tag", "STadapter onClick: null!");
            }
        });
        Status o = tweets.get(position);
        User user = o.getUser();
        holder.videoPlayButton.setVisibility(View.GONE);
//        if (o.isRetweet()){ // if its retweet
//            holder.reTweetedByContainer.setVisibility(View.VISIBLE);
//            String name = "retweeted by "+o.getReTweetedByName();
//            holder.reTweetedBy.setText(name);
//        }else holder.reTweetedByContainer.setVisibility(View.GONE);
//        if (o.isReTweeted()){ // if retweeted by me
//            holder.retweetButton.setImageResource(R.drawable.ic_undo_retweet_button);
//        }else {
//            holder.retweetButton.setImageResource(R.drawable.ic_retweet_button);
//        }

        holder.tt.setText(o.getText());
        holder.bt.setText(user.getName());
        holder.ScreenNameView.setText(user.getScreenName());
        holder.timeView.setText(getTimeAgo(o,context));
        holder.replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionListener!=null){actionListener.onClick(holder.getAdapterPosition(),0);}
            }
        });
        holder.retweetNum.setText(String.valueOf(o.getRetweetCount()));
        holder.retweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionListener!=null){actionListener.onClick(holder.getAdapterPosition(),1);}
            }
        });

        holder.favNum.setText(String.valueOf(o.getFavoriteCount()));
        if (o.isFavorited()){holder.favButton.setImageResource(R.drawable.ic_favarit_on);}
        else {holder.favButton.setImageResource(R.drawable.ic_favarit_off);}
        holder.favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionListener!=null){actionListener.onClick(holder.getAdapterPosition(),2);}
            }
        });
        if (/*o.getQuotedStatus()!=null*/false){
            holder.quoteContainer.setVisibility(View.VISIBLE);
            holder.quoteContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if(actionListener!=null){actionListener.onClick(holder.getAdapterPosition(),3);} // 3 means quate
                }
            });
            holder.quoteName.setText(o.getQuotedStatus().getUser().getName());
            holder.quoteContent.setText(o.getQuotedStatus().getText());
        }else {
            holder.quoteContainer.setVisibility(View.GONE);
        }
        Picasso.with(context).load(o.getUser().getBiggerProfileImageURL()).into(holder.prof_pic);
        if (iconClickListener!=null){
            holder.prof_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iconClickListener.userIconClicked(holder.getAdapterPosition());
                }
            });
        }



        //dont need to show media in search..?
        holder.timeLineMedia1.setVisibility(View.GONE);
        holder.timeLineMedia2.setVisibility(View.GONE);
        holder.timeLineMedia3.setVisibility(View.GONE);
        holder.timeLineMedia4.setVisibility(View.GONE);
        holder.thumbContainer.setVisibility(View.GONE);
        holder.videoView.setVisibility(View.GONE);
//        ArrayList<String> mediaList = new ArrayList<>();
//        for (MediaEntity media: o.getMediaEntities()){
//            mediaList.add(media.getMediaURL());
//        }
//        if ( mediaList.size() > 0){
//            holder.timeLineMedia1.setVisibility(View.VISIBLE);
//            Picasso.with(context).load(mediaList.get(0)).into(holder.timeLineMedia1);
//            if (mediaClickedListener != null) {
//                holder.timeLineMedia1.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {mediaClickedListener.onMediaClicked(holder.getAdapterPosition(),0);}
//                });
//            }
//            if (mediaList.size()>1){
//                holder.timeLineMedia2.setVisibility(View.VISIBLE);
//                Picasso.with(context).load(mediaList.get(1)).into(holder.timeLineMedia2);
//                if (mediaClickedListener != null) {
//                    holder.timeLineMedia2.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {mediaClickedListener.onMediaClicked(holder.getAdapterPosition(),1);}
//                    });
//                }
//                if (mediaList.size()>2){
//                    holder.timeLineMedia3.setVisibility(View.VISIBLE);
//                    Picasso.with(context).load(mediaList.get(2)).into(holder.timeLineMedia3);
//                    if (mediaClickedListener != null) {
//                        holder.timeLineMedia3.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {mediaClickedListener.onMediaClicked(holder.getAdapterPosition(),2);}
//                        });
//                    }
//                    if (mediaList.size()>3){
//                        holder.timeLineMedia4.setVisibility(View.VISIBLE);
//                        Picasso.with(context).load(mediaList.get(3)).into(holder.timeLineMedia4);
//                        if (mediaClickedListener != null) {
//                            holder.timeLineMedia3.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {mediaClickedListener.onMediaClicked(holder.getAdapterPosition(),3);}
//                            });
//                        }
//                    }
//                }
//            }
//        }else if (o.getMedia_gifs() != null){
//            final VideoView myVideoView = holder.videoView;
//            myVideoView.setVisibility(View.VISIBLE);
//            String gifUrl = o.getMedia_gifs().replace("tweet_video_thumb", "tweet_video").replace(".png", ".mp4").replace(".jpg", ".mp4").replace(".jpeg", ".mp4");
//            myVideoView.setVideoURI(Uri.parse(gifUrl));
//            holder.videoView.requestFocus();
//            myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    myVideoView.start();
//                }
//            });
//            myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    myVideoView.start();
//                }
//            });
//        }else if (o.getInstaUrl()!=null){
//            Picasso.with(context).load(o.getInstaUrl()).into(holder.timeLineMedia1);
//            if (mediaClickedListener != null) {
//                holder.timeLineMedia1.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {mediaClickedListener.onMediaClicked(holder.getAdapterPosition(),0);}
//                });
//            }
//        }

//        if (o.getYoutubeId() != null&&!o.getYoutubeId().contains("//")) {
//            holder.thumbContainer.setVisibility(View.VISIBLE);
//            String YTThumb = "https://img.youtube.com/vi/"+o.getYoutubeId()+"/hqdefault.jpg";
//            Picasso.with(context).load(YTThumb).into(holder.thumbView);
//            holder.playButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mediaClickedListener!=null){
//                        mediaClickedListener.onMediaClicked(holder.getAdapterPosition(),4);
//                    }
//                }
//            });
//        }

    }
    private String getTimeAgo(Status tweet, Context context) {
        long timeRepliedInMillis = tweet.getCreatedAt().getTime();
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

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final View itemView;
        TextView tt;
        TextView bt;
        TextView ScreenNameView;
        TextView timeView;
        ImageView prof_pic;
        ImageView replyButton;
        TextView retweetNum; ImageView retweetButton;
        final TextView favNum; ImageView favButton;

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
        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            tt = (TextView) itemView.findViewById(R.id.toptext);
            bt= (TextView) itemView.findViewById(R.id.bottomtext);
            ScreenNameView= (TextView) itemView.findViewById(R.id.tl_screen_name);
            timeView= (TextView) itemView.findViewById(R.id.tl_time);
            replyButton = (ImageView)itemView.findViewById(R.id.tl_reply_button);
            retweetNum = (TextView)itemView.findViewById(R.id.tl_retweet_num);
            retweetButton = (ImageView)itemView.findViewById(R.id.tl_retweet_button);
            favNum = (TextView)itemView.findViewById(R.id.tl_fav_num);
            favButton = (ImageView)itemView.findViewById(R.id.tl_fav_button);
            prof_pic = (ImageView) itemView.findViewById(R.id.prof_pic_tl);
            timeLineMedia1 = (ImageView)itemView.findViewById(R.id.timeline_media1);
            timeLineMedia2 = (ImageView)itemView.findViewById(R.id.timeline_media2);
            timeLineMedia3 = (ImageView)itemView.findViewById(R.id.timeline_media3);
            timeLineMedia4 = (ImageView)itemView.findViewById(R.id.timeline_media4);

            reTweetedByContainer = (RelativeLayout)itemView.findViewById(R.id.tl_retweeted_by_container);
            reTweetedBy = (TextView)itemView.findViewById(R.id.tl_retweeted_by);

            videoView = (VideoView) itemView.findViewById(R.id.tl_video);
            videoPlayButton = (ImageView) itemView.findViewById(R.id.tl_video_play_button);

            thumbContainer = (RelativeLayout) itemView.findViewById(R.id.tl_youtube_container);
            thumbView = (ImageView) itemView.findViewById(R.id.tl_youtube_thumb);
            playButton =(ImageView) itemView.findViewById(R.id.tl_youtube_play_button);

            quoteContainer = (RelativeLayout)itemView.findViewById(R.id.tl_quote_container);
            quoteName = (TextView)itemView.findViewById(R.id.tl_quote_name);
            quoteContent = (TextView)itemView.findViewById(R.id.tl_quote_content);
        }
    }

    public interface STIconClickListener{void userIconClicked(int position);}
    public void setOnIconClickListener(STIconClickListener listener){iconClickListener = listener;}

    public interface STTweetActionListener{void onClick(int position,int type);}
    public void setOnTweetActionListener(STTweetActionListener listener){actionListener = listener;}

    interface STItemClickListener{void itemClicked(int position);}
    void setOnSTItemClickListener(STItemClickListener listener){
        stItemClickListener = listener;}
}
