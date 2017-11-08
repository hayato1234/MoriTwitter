package com.orangesunshine.moritwitter.FollowList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.simleman.moritwitter.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import twitter4j.User;

/**
 * Created by hayatomoritani on 5/24/17.
 */

public class FollowListFromUserActivityAdapter extends RecyclerView.Adapter<FollowListFromUserActivityAdapter.Holder> {

    private Context context;
    private List<User> users;
    private View.OnClickListener itemListener;

    FollowListFromUserActivityAdapter(Context context,List<User> users){
        this.context = context;
        this.users = users;
    }

    void setItemClickListener(View.OnClickListener listener){
        itemListener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.content_snstime_lime_list_item,parent,false);
        if (itemListener!=null){
            v.setOnClickListener(itemListener);
        }
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        User user = users.get(position);
        holder.time.setVisibility(View.GONE);
        holder.UserNameView.setText(user.getName());
        holder.ScreenNameView.setText(user.getScreenName());
        Picasso.with(context).load(user.getBiggerProfileImageURL()).into(holder.profPicView);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    //protected void addItems(List<User> newUsers){users.addAll(newUsers);notifyDataSetChanged();};

    static class Holder extends RecyclerView.ViewHolder {
        ImageView profPicView;
        TextView UserNameView;
        TextView ScreenNameView;
        TextView time;
        public Holder(View itemView) {
            super(itemView);
            profPicView = (ImageView)itemView.findViewById(R.id.sns_prof_pic);
            UserNameView = (TextView)itemView.findViewById(R.id.sns_user_name);
            ScreenNameView = (TextView)itemView.findViewById(R.id.sns_screen_name);
            time = (TextView)itemView.findViewById(R.id.new_tweets);
        }
    }
}
