package com.orangesunshine.moritwitter.media;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.simleman.moritwitter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class MediaListAdapter extends BaseAdapter {

    private Context context;
    private List<String> imageURL;
    private List<String> tweetTexts;

    MediaListAdapter(Context context) {
        this.context = context;
        imageURL = new ArrayList<>();
        tweetTexts = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return imageURL.size();
    }

    @Override
    public Object getItem(int position) {
        return imageURL.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_media,parent,false);
            holder = new Holder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (Holder) convertView.getTag();
        }

        holder.textView.setText(tweetTexts.get(position));

        Picasso.with(context)
                .load(imageURL.get(position))
                .placeholder(R.drawable.place_holder_white)
                .into(holder.imageView);
        if (onImageClickListener!=null){
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onImageClickListener.imageClicked(position);
                }
            });
        }
        return convertView;
    }

    void swapData(List<String> imageUrl, List<String> tweetTexts){
        this.imageURL = imageUrl;
        this.tweetTexts = tweetTexts;
        notifyDataSetChanged();
    }

    static class Holder{
        @BindView(R.id.media_image_view)
        ImageView imageView;
        @BindView(R.id.media_content_text)
        TextView textView;

        Holder(View view) {
            ButterKnife.bind(this,view);
        }
    }

    OnImageClickListener onImageClickListener;
    interface OnImageClickListener{void imageClicked(int position);}
    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }
}
