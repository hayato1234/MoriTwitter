package com.orangesunshine.moritwitter.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.orangesunshine.moritwitter.search.SearchPeopleFragment.OnListFragmentInteractionListener;
import com.orangesunshine.moritwitter.search.dummy.DummyContent.DummyItem;
import com.simleman.moritwitter.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import twitter4j.User;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SearchPeopleRecyclerViewAdapter extends RecyclerView.Adapter<SearchPeopleRecyclerViewAdapter.ViewHolder> {

    private final List<User> userList;
    private final OnListFragmentInteractionListener mListener;
    private OnUserSelectedListener userListener;
    private Context context;

    public SearchPeopleRecyclerViewAdapter(List<User> items, OnListFragmentInteractionListener listener, Context context) {
        userList = items;
        mListener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_snstime_lime_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.timeView.setVisibility(View.GONE);
        holder.user = userList.get(position);
        holder.userNameView.setText(userList.get(position).getName());
        holder.screenNameView.setText(userList.get(position).getScreenName());
        Picasso.with(context).load(userList.get(position).getBiggerProfileImageURL()).into(holder.profPicView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != userListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    //mListener.onListFragmentInteraction(holder.user);
                    userListener.onUserSelected(holder.user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View itemView;
        final ImageView profPicView;
        final TextView userNameView;
        final TextView screenNameView;
        final TextView timeView;
        public User user;

        public ViewHolder(View view) {
            super(view);
            itemView = view;
            profPicView = (ImageView)view.findViewById(R.id.sns_prof_pic);
            userNameView = (TextView)view.findViewById(R.id.sns_user_name);
            screenNameView = (TextView)view.findViewById(R.id.sns_screen_name);
            timeView = (TextView)view.findViewById(R.id.new_tweets);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + user.getName() + "'";
        }
    }

    interface OnUserSelectedListener{void onUserSelected(User user);}
    protected void setOnUserSelectedListener(OnUserSelectedListener listener){userListener=listener;}
}
