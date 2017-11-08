package com.orangesunshine.moritwitter;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.simleman.moritwitter.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by hayatomoritani on 11/16/16.
 */

public class SNSTimeLimeAdapter extends BaseAdapter {

    List<BasicUserInfo> users;
    Context context;

    public SNSTimeLimeAdapter(Context context, List<BasicUserInfo> users){
        this.context = context;
        this.users = users;
    }


    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.content_snstime_lime_list_item,parent,false);
            holder = new Holder();

            holder.profPicView = (ImageView)convertView.findViewById(R.id.sns_prof_pic);
            holder.UserNameView = (TextView)convertView.findViewById(R.id.sns_user_name);
            holder.ScreenNameView = (TextView)convertView.findViewById(R.id.sns_screen_name);
            holder.newTweets = (TextView)convertView.findViewById(R.id.new_tweets);
            holder.disableIcon = (ImageView)convertView.findViewById(R.id.disabled_icon);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        BasicUserInfo user = users.get(position);
        Picasso.with(context).load(user.getIconURL()).into(holder.profPicView);
        holder.UserNameView.setText(user.getNameB());
        holder.ScreenNameView.setText(user.getScreenName());
//        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("new_tweet_count_switch",true)&&user.getDifference() > 0) {
//            String a = String.valueOf(user.getDifference());
//            holder.newTweets.setText(a);
//            convertView.setBackgroundColor(Color.parseColor("#DCFFDC"));// set background green if there are new tweets
//            holder.newTweets.setVisibility(View.VISIBLE);
//            holder.disableIcon.setVisibility(View.GONE);
//        }else {//not showing counts
//            holder.newTweets.setVisibility(View.GONE);
//            holder.disableIcon.setVisibility(View.VISIBLE);
//            convertView.setBackgroundColor(Color.parseColor("#ffffff"));
//        }

        if (user.getIsDisabled()==1){
            //if count disabled
            holder.newTweets.setVisibility(View.GONE);
            holder.disableIcon.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(Color.parseColor("#ffffff"));
        }else if (user.getDifference() < 1){
            //if count is less than 0
            holder.newTweets.setVisibility(View.GONE);
            holder.disableIcon.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.parseColor("#ffffff"));
        }else {
            //there are new tweets for this user
            String a = String.valueOf(user.getDifference());
            holder.newTweets.setText(a);
            convertView.setBackgroundColor(Color.parseColor("#DCFFDC"));// set background green if there are new tweets
            holder.newTweets.setVisibility(View.VISIBLE);
            holder.disableIcon.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class Holder{
        ImageView profPicView;
        TextView UserNameView;
        TextView ScreenNameView;
        TextView newTweets;
        ImageView disableIcon;
    }

}
