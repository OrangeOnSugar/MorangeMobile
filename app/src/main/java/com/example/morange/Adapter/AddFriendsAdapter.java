package com.example.morange.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.morange.MessageActivity;
import com.example.morange.ModeJS.UserINFO;
import com.example.morange.R;

import java.util.List;

public class AddFriendsAdapter extends RecyclerView.Adapter<AddFriendsAdapter.ViewHolder> {

    private Context mcontext;
    private List<UserINFO> msue;
    private boolean ischat;

    public AddFriendsAdapter(Context mcontext, List<UserINFO> msue, boolean ischat) {
        this.mcontext = mcontext;
        this.msue = msue;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.user_item,parent,false);
        return new AddFriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final UserINFO userINFO = msue.get(position);
            holder.userlogin.setText(userINFO.getLogin());
            Glide.with(mcontext).load(userINFO.getImageURL().equals("default")?R.drawable.userbackds:userINFO.getImageURL()).into(holder.prof_ima);

            if (ischat) {
                holder.status_on.setVisibility(userINFO.getStatus().equals("онлайн")?View.VISIBLE:View.GONE);
                holder.status_off.setVisibility(userINFO.getStatus().equals("онлайн")?View.GONE:View.VISIBLE);
            } else {
                holder.status_on.setVisibility(View.GONE);
                holder.status_off.setVisibility(View.GONE);
            }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mcontext, MessageActivity.class);
            intent.putExtra("userid",userINFO.getId());
            mcontext.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return msue.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView userlogin;
        public ImageView prof_ima;
        private ImageView status_on;
        private ImageView status_off;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userlogin = itemView.findViewById(R.id.userlogin);
            prof_ima = itemView.findViewById(R.id.profile_image);
            status_on = itemView.findViewById(R.id.bar_status_on);
            status_off = itemView.findViewById(R.id.bar_status_off);
        }
    }
}
