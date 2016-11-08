package com.simleman.moritwitter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hayatomoritani on 10/28/16.
 */

public class TweetListAdaptor extends BaseAdapter{
    private ArrayList<Tweet> tweets;
    private Context context;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
        ViewHolder viewHolder = new ViewHolder();

        Tweet o = tweets.get(position);
        viewHolder.tt = (TextView) convertView.findViewById(R.id.toptext);
        viewHolder.bt= (TextView) convertView.findViewById(R.id.bottomtext);
        viewHolder.prof_pic = (ImageView) convertView.findViewById(R.id.prof_pic_tl);
        viewHolder.timeLineMedia1 = (ImageView)convertView.findViewById(R.id.timeline_media1);
        viewHolder.timeLineMedia2 = (ImageView)convertView.findViewById(R.id.timeline_media2);
        viewHolder.timeLineMedia3 = (ImageView)convertView.findViewById(R.id.timeline_media3);
        viewHolder.timeLineMedia4 = (ImageView)convertView.findViewById(R.id.timeline_media4);

        viewHolder.tt.setText(o.getContent());
        viewHolder.bt.setText(o.getUser());
        Picasso.with(context).load(o.getProf_image()).resize(140,140).into(viewHolder.prof_pic);

        ArrayList<String> mediaList = o.getMedia_images();

        if (mediaList!=null){
            Picasso.with(context).load(mediaList.get(0)).into(viewHolder.timeLineMedia1);
            if (mediaList.size()>1){
                Picasso.with(context).load(mediaList.get(1)).into(viewHolder.timeLineMedia2);
                if (mediaList.size()>2){
                    Picasso.with(context).load(mediaList.get(2)).into(viewHolder.timeLineMedia3);
                    if (mediaList.size()>3){
                        Picasso.with(context).load(mediaList.get(3)).into(viewHolder.timeLineMedia4);
                    }
                }
            }
        }

        return convertView;
    }

    private static class ViewHolder{
        TextView tt;
        TextView bt;
        ImageView prof_pic;
        ImageView timeLineMedia1;
        ImageView timeLineMedia2;
        ImageView timeLineMedia3;
        ImageView timeLineMedia4;
    }
}
