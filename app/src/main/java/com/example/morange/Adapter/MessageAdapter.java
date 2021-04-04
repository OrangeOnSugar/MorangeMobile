package com.example.morange.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mcontext;
    private List<Chat> mchat;
    private String imageurl;

    FirebaseUser fuser;

    public MessageAdapter(Context mcontext, List<Chat> mchat, String imageurl)
    {
        this.mchat = mchat;
        this.mcontext = mcontext;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mcontext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(mcontext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    private String[] getTimeDateANDLocal(long time)
    {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ssZ");
        String DateTime = format.format(new Date(time));
        String[] date_and_time = {"",""};
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        try
        {
            Date value = format.parse(DateTime);
            SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm");
            newFormat.setTimeZone(TimeZone.getDefault());
            DateTime = newFormat.format(value);
            date_and_time[0] = DateTime.substring(DateTime.indexOf("T")+1);
            date_and_time[1] = DateTime.substring(0,DateTime.indexOf("T"));
        }
        catch (ParseException e) {
            e.printStackTrace();
            date_and_time = new String[]{"", ""};
        }

        return date_and_time;
    }


    private String Month(String dateString)
    {
        String date_cor;
        Date date = null;
        try {
            date = new SimpleDateFormat("dd-MM-yyyy").parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar queueDateCal = Calendar.getInstance();
        queueDateCal.setTime(date);
        switch (queueDateCal.get(Calendar.MONTH)+1)
        {
            case 1:
                date_cor = " ЯНВАРЯ ";
                break;
            case 2:
                date_cor = " ФЕВРАЛЯ ";
                break;
            case 3:
                date_cor = " МАРТА ";
                break;
            case 4:
                date_cor = " АПРЕЛЯ ";
                break;
            case 5:
                date_cor = " МАЯ ";
                break;
            case 6:
                date_cor = " ИЮНЯ ";
                break;
            case 7:
                date_cor = " ИЮЛЯ ";
                break;
            case 8:
                date_cor = " АВГУСТА ";
                break;
            case 9:
                date_cor = " СЕНТЯБРЯ ";
                break;
            case 10:
                date_cor = " ОКТЯБРЯ ";
                break;
            case 11:
                date_cor = " НОЯБРЯ ";
                break;
            case 12:
                date_cor = " ДЕКАБРЯ ";
                break;
            default:
                date_cor = "ошибочка";
                break;
        }
        return queueDateCal.get(Calendar.DAY_OF_MONTH)+date_cor+ queueDateCal.get(Calendar.YEAR)+ " г.";
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = mchat.get(position);

        String[] DateTime = getTimeDateANDLocal(chat.getDatetime());

        if(position == 0)
        {
            holder.message_date.setVisibility(View.VISIBLE);
            holder.message_date.setText(Month(DateTime[1]));
        }
        else
        {
            Chat chatlast = mchat.get(position-1);
            String[] DateTimeGO = getTimeDateANDLocal(chatlast.getDatetime());

            if (!Month(DateTime[1]).equals(Month(DateTimeGO[1])))
            {
                holder.message_date.setVisibility(View.VISIBLE);
                holder.message_date.setText(Month(DateTime[1]));
            }
        }


        if (chat.getMessagetype().equals("text"))
        {
            holder.message_type_image.setVisibility(View.GONE);
            holder.message_type_text.setVisibility(View.VISIBLE);
            holder.message_text_text.setText(chat.getMessage());
            holder.message_text_time.setText(DateTime[0]);
            if (chat.isIsseen())
            {
                holder.message_text_seen.setImageResource(R.drawable.notseen);
            }
            else
            {
                holder.message_text_seen.setImageResource(R.drawable.seenmessage);
            }
        }
        else if (chat.getMessagetype().equals("image"))
        {
            holder.message_type_image.setVisibility(View.VISIBLE);
            holder.message_image_time.setText(DateTime[0]);
            Glide.with(mcontext).load(chat.getMessage()).into(holder.message_image_image);
            if (chat.isIsseen())
            {
                holder.message_image_seen.setImageResource(R.drawable.notseen);
            }
            else
            {
                holder.message_image_seen.setImageResource(R.drawable.seenmessage);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mchat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        public TextView message_text_text, message_text_time, message_image_time, message_date;
        public ImageView prof_ima, message_image_image, message_text_seen, message_image_seen;
        public RelativeLayout message_type_text,message_type_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            message_date = itemView.findViewById(R.id.message_date);

            message_type_text = itemView.findViewById(R.id.type_text);
            message_type_image = itemView.findViewById(R.id.type_image);

            message_text_text = itemView.findViewById(R.id.message_text);
            message_image_image = itemView.findViewById(R.id.message_image);

            prof_ima = itemView.findViewById(R.id.profile_image);

            message_text_seen = itemView.findViewById(R.id.message_text_status);
            message_image_seen = itemView.findViewById(R.id.message_image_status);

            message_text_time = itemView.findViewById(R.id.message_text_time);
            message_image_time = itemView.findViewById(R.id.message_image_time);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mchat.get(position).getSender().equals(fuser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return  MSG_TYPE_LEFT;
        }
    }

}