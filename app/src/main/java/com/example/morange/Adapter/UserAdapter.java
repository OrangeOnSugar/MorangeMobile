package com.example.morange.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.morange.MessageActivity;
import com.example.morange.ModeJS.Chat;
import com.example.morange.ModeJS.UserINFO;
import com.example.morange.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mcontext;
    private List<UserINFO> msue;
    private boolean ischat;
    private boolean senderorreceiver;

    String thelastmessage;

    public UserAdapter(Context mcontext, List<UserINFO> msue, boolean ischat, boolean senderorreceiver)
    {
        this.msue = msue;
        this.mcontext = mcontext;
        this.ischat = ischat;
        this.senderorreceiver = senderorreceiver;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.user_item,parent,false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final UserINFO userINFO = msue.get(position);
        holder.userlogin.setText(userINFO.getUsername());
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
            lastMessage(userINFO.getId(), holder.last_message, holder.last_message_DateTime);
        }
        else
        {
            holder.last_message.setVisibility(View.GONE);
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
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mcontext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return msue.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        public TextView userlogin, last_message, last_message_DateTime;
        public ImageView prof_ima;
        private ImageView status_on;
        private ImageView status_off;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            last_message_DateTime = itemView.findViewById(R.id.last_message_timeDate);
            userlogin = itemView.findViewById(R.id.userlogin);
            last_message = itemView.findViewById(R.id.last_message);
            prof_ima = itemView.findViewById(R.id.profile_image);
            status_on = itemView.findViewById(R.id.bar_status_on);
            status_off = itemView.findViewById(R.id.bar_status_off);
        }
    }

    private String getTimeDateANDLocal(long time)
    {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ssZ");
        String DateTime = format.format(new Date(time));

        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        try
        {
            Date value = format.parse(DateTime);

            SimpleDateFormat newFormat = new SimpleDateFormat("HH:mm");
            newFormat.setTimeZone(TimeZone.getDefault());
            DateTime = newFormat.format(value);
        }
        catch (ParseException e) {
            e.printStackTrace();
            DateTime = "";
        }

        return  DateTime;
    }

    private void lastMessage(final String userid, final TextView last_message, final TextView message_timedate)
    {
        try {
            thelastmessage = "default";
            final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
            if (senderorreceiver)
            {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(userid+"|"+fuser.getUid());



                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try
                        {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                            {
                                Chat chat = dataSnapshot1.getValue(Chat.class);
                                assert chat != null;
                                assert fuser != null;
                                if(chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid))
                                {
                                    thelastmessage = chat.getMessage();
                                    message_timedate.setText(getTimeDateANDLocal(chat.getDatetime()));
                                    last_message.setTextColor(Color.parseColor("#8F8F8F"));
                                }
                                else if (chat.getReceiver().equals(userid) && chat.getSender().equals(fuser.getUid()))
                                {
                                    thelastmessage = "Вы: "+chat.getMessage();
                                    message_timedate.setText(getTimeDateANDLocal(chat.getDatetime()));
                                    last_message.setTextColor(Color.parseColor("#FF6800"));
                                }
                            }

                            switch (thelastmessage)
                            {
                                case "default":
                                    last_message.setText("Сообщений пока нет");
                                    break;

                                default:
                                    last_message.setText(thelastmessage);
                                    break;
                            }

                            thelastmessage = "default";
                        }
                        catch(Exception ex)
                        {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            else
            {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(fuser.getUid()+"|"+userid);

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try
                        {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                            {
                                Chat chat = dataSnapshot1.getValue(Chat.class);
                                assert chat != null;
                                assert fuser != null;
                                if(chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid))
                                {
                                    thelastmessage = chat.getMessage();
                                    message_timedate.setText(getTimeDateANDLocal(chat.getDatetime()));
                                    last_message.setTextColor(Color.parseColor("#8F8F8F"));
                                }
                                else if (chat.getReceiver().equals(userid) && chat.getSender().equals(fuser.getUid()))
                                {
                                    thelastmessage = "Вы: "+chat.getMessage();
                                    message_timedate.setText(getTimeDateANDLocal(chat.getDatetime()));
                                    last_message.setTextColor(Color.parseColor("#FF6800"));
                                }
                            }

                            switch (thelastmessage)
                            {
                                case "default":
                                    last_message.setText("Сообщений пока нет");
                                    break;

                                default:
                                    last_message.setText(thelastmessage);
                                    break;
                            }

                            thelastmessage = "default";
                        }
                        catch(Exception ex)
                        {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        catch (Exception ex) {

        }

    }

}
