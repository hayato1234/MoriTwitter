package com.orangesunshine.moritwitter.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.simleman.moritwitter.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import twitter4j.Status;
import twitter4j.User;

/**
 * Created by hayatomoritani on 5/5/17.
 */

public class DetailRecyclerViewAdapter extends RecyclerView.Adapter<DetailRecyclerViewAdapter.MyViewHolder> {

    private List<Status> replyList;
    private OnReplyClickListener myListener;
    private Context context;
    private RecyclerView recyclerView;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public DetailRecyclerViewAdapter(List<Status> replyList, Context context){
        this.replyList = replyList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_detail_reply_list_item, parent, false);

        if (myListener != null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myListener.onReplyClick(recyclerView.getChildAdapterPosition(v));
                }
            });
        }
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Status tweet = replyList.get(position);
        User user = tweet.getUser();

        String picURL = user.getProfileImageURL();
        if (picURL!=null){
            Picasso.with(context).load(picURL).into(holder.profPic);
        }

        holder.userName.setText(user.getName());
        holder.screenName.setText(user.getScreenName());
        holder.content.setText(tweet.getText());

        String diff = getTimeAgo(tweet,context);
        holder.time.setText(diff);
    }


    private String getTimeAgo(Status tweet, Context context) {
        long timeRepliedInMillis = tweet.getCreatedAt().getTime();
        long now = System.currentTimeMillis();
        long resolution = DateUtils.MINUTE_IN_MILLIS; // less than a minuet will be zero

        String diff;

        if (DateUtils.isToday(timeRepliedInMillis)){
            diff = DateUtils.getRelativeTimeSpanString(timeRepliedInMillis,now,resolution).toString();
        }else {
            long transitionRes = DateUtils.WEEK_IN_MILLIS; // show date if more than 7days
            diff = DateUtils.getRelativeDateTimeString(context,timeRepliedInMillis,resolution,transitionRes,0).toString();
        }
        return diff;
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView profPic;
        TextView userName;
        TextView screenName;
        TextView content;
        TextView time;
        MyViewHolder(View view){
            super(view);
            profPic = (ImageView) view.findViewById(R.id.detail_prof_pic);
            userName = (TextView) view.findViewById(R.id.detail_user_name);
            screenName = (TextView) view.findViewById(R.id.detail_screen_name);
            content = (TextView)view.findViewById(R.id.detail_content);
            time =(TextView)view.findViewById(R.id.detail_reply_time);
        }
    }

    public interface OnReplyClickListener{
        void onReplyClick(int position);
    }
    public void setOnReplyClickListener(OnReplyClickListener myListener) {
        this.myListener=myListener;
    }
}
