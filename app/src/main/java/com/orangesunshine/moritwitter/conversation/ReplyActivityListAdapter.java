package com.orangesunshine.moritwitter.conversation;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.orangesunshine.moritwitter.FollowList.FollowListMain;
import com.orangesunshine.moritwitter.LogInActivity;
import com.simleman.moritwitter.R;
import com.orangesunshine.moritwitter.Tweet;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by hayatomoritani on 5/11/17.
 */

public class ReplyActivityListAdapter extends RecyclerView.Adapter<ReplyActivityListAdapter.MyViewHolder> {

    private static final int typeOther = 1;
    private static final int typeMe = 2;
    private List<Tweet> conversation;
   // private View.OnClickListener myListener;
    private Context context;
    private static SharedPreferences sharedPreferences;
    private static String currentUserScreenName;
    private OnReplyIconClickListener iconClickListener;

    ReplyActivityListAdapter(List<Tweet> conversation, Context context){
        this.conversation = conversation;
        this.context = context;
        sharedPreferences = context.getSharedPreferences(LogInActivity.PREFERENCE_NAME,0);
        currentUserScreenName = sharedPreferences.getString(FollowListMain.USER_NAME_LIST[sharedPreferences.getInt(LogInActivity.PREF_CURRENT_ACCOUNT,0)],"");
    }

//    void setMyListener(View.OnClickListener myListener){
//        this.myListener = myListener;
//    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType==typeMe){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_activity_me_item,parent, false);
        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_activty_item,parent, false);
        }
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Tweet status = conversation.get(position);

        Picasso.with(context).load(status.getProf_image()).fit().into(holder.userIcon);
        if (iconClickListener!=null){
            holder.userIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("tag", "RALAdapter onBindViewHolder: onclick");
                    iconClickListener.userIconClicked(holder.getAdapterPosition());
                }
            });
        }else {
            Log.d("tag", "RALAdapter onBindViewHolder: null");
        }
        holder.userName.setText(status.getUser());
        holder.time.setText(getTimeAgo(status,context));
        holder.content.setText(status.getContent());
    }

    private String getTimeAgo(Tweet tweet, Context context) {
        long timeRepliedInMillis = tweet.getTime().getTime();
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

    void addItem(Tweet t){
        conversation.add(t);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (conversation.get(position).getScreenName().equals(currentUserScreenName)){
            return typeMe;
        }else {
            return typeOther;
        }
    }

    @Override
    public int getItemCount() {
        return conversation.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageButton userIcon;
        TextView userName;
        TextView time;
        TextView content;

        public MyViewHolder(View itemView) {
            super(itemView);
            userIcon = (ImageButton) itemView.findViewById(R.id.reply_user_icon);
            userName = (TextView) itemView.findViewById(R.id.reply_user_name);
            time = (TextView) itemView.findViewById(R.id.reply_time);
            content = (TextView) itemView.findViewById(R.id.reply_content);
        }
    }

    void setOnIconClickListener(OnReplyIconClickListener listener){
        iconClickListener = listener;
    }

    interface OnReplyIconClickListener{
        void userIconClicked(int position);
    }
}
