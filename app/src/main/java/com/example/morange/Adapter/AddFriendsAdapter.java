package com.example.morange.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.morange.AddFriends_Activity;
import com.example.morange.MessageActivity;
import com.example.morange.ModeJS.Friends;
import com.example.morange.ModeJS.UserINFO;
import com.example.morange.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.morange.AddFriends_Activity;

import java.util.HashMap;
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
            if (userINFO.getImageURL().equals("default"))
            {
                holder.prof_ima.setImageResource(R.drawable.userbackds);
            }
            else
            {
                Glide.with(mcontext).load(userINFO.getImageURL()).into(holder.prof_ima);
            }

            if (ischat)
            {
                if(userINFO.getStatus().equals("онлайн"))
                {
                    holder.status_on.setVisibility(View.VISIBLE);
                    holder.status_off.setVisibility(View.GONE);
                }
                else
                {
                    holder.status_on.setVisibility(View.GONE);
                    holder.status_off.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                holder.status_on.setVisibility(View.GONE);
                holder.status_off.setVisibility(View.GONE);
            }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dialog(userINFO.getLogin(), userINFO.getId());
                Intent intent = new Intent(mcontext, MessageActivity.class);
                intent.putExtra("userid",userINFO.getId());
                mcontext.startActivity(intent);
            }
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

    /*public void Dialog(String login, final String id)
    {
        AlertDialog.Builder did = new AlertDialog.Builder(mcontext);
        did.setMessage("Добавить в друзья "+login+"?");
        did.setNegativeButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DatabaseReference addfriends = FirebaseDatabase.getInstance().getReference();
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("добавил", firebaseUser.getUid());
                hashMap.put("добавленный", id);

                addfriends.child("Friends").push().setValue(hashMap);
                Toast.makeText(mcontext, "Друг добавлен", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(mcontext, AddFriends_Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                mcontext.startActivity(intent);
            }
        });
        did.setPositiveButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        did.create().show();
    }*/

}
