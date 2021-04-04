package com.example.morange.Adapter;

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
import com.example.morange.MainActivity;
import com.example.morange.MessageActivity;
import com.example.morange.ModeJS.Friends;
import com.example.morange.ModeJS.UserINFO;
import com.example.morange.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class FriendsAdapter  extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private Context mcontext;
    private List<UserINFO> msue;
    private boolean ischat;

    public FriendsAdapter(Context mcontext, List<UserINFO> msue, boolean ischat) {
        this.mcontext = mcontext;
        this.msue = msue;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.user_item_go,parent,false);

        return new FriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.ViewHolder holder, int position) {

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
                Intent intent = new Intent(mcontext, MessageActivity.class);
                intent.putExtra("userid",userINFO.getId());
                mcontext.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Dialog(userINFO.getLogin(),userINFO.getId());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return msue.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder
    {

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
    public void Dialog(final String login, final String id)
    {
        AlertDialog.Builder did = new AlertDialog.Builder(mcontext);
        did.setMessage("Удалить из друзей "+login+"?");
        did.setNegativeButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                Query lol = ref.child("Friends");

                lol.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            Friends friends = appleSnapshot.getValue(Friends.class);
                            assert friends != null;
                            assert fuser != null;
                            if (friends.getДобавил().equals(fuser.getUid()) && friends.getДобавленный().equals(id))
                            {
                                appleSnapshot.getRef().removeValue();
                                Toast.makeText(mcontext, login + " удалён из друзей", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(mcontext, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                mcontext.startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        });
        did.setPositiveButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        did.create().show();
    }
}
