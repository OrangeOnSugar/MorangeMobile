package com.example.morange.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mcontext;
    private List<Chat> mchat;
    private String imageurl;

    FirebaseUser fuser;
    Handler seekHandler = new Handler();
    Runnable run;

    final MediaPlayer mediaPlayer = new MediaPlayer();

    public MessageAdapter(Context mcontext, List<Chat> mchat, String imageurl) {
        this.mchat = mchat;
        this.mcontext = mcontext;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mcontext).inflate((viewType == MSG_TYPE_RIGHT?R.layout.chat_item_right:R.layout.chat_item_left), parent, false);
        return new MessageAdapter.ViewHolder(view);
    }

    private String[] getTimeDateANDLocal(long time) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ssZ");
        String DateTime = format.format(new Date(time));
        String[] date_and_time = {"", ""};
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date value = format.parse(DateTime);
            SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm");
            newFormat.setTimeZone(TimeZone.getDefault());
            DateTime = newFormat.format(value);
            date_and_time[0] = DateTime.substring(DateTime.indexOf("T") + 1);
            date_and_time[1] = DateTime.substring(0, DateTime.indexOf("T"));
        } catch (ParseException e) {
            e.printStackTrace();
            date_and_time = new String[]{"", ""};
        }

        return date_and_time;
    }


    private String Month(String dateString) {
        String date_cor;
        Date date = null;
        try {
            date = new SimpleDateFormat("dd-MM-yyyy").parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar queueDateCal = Calendar.getInstance();
        queueDateCal.setTime(date);
        switch (queueDateCal.get(Calendar.MONTH) + 1) {
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
        return queueDateCal.get(Calendar.DAY_OF_MONTH) + date_cor + queueDateCal.get(Calendar.YEAR) + " г.";
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, final int position) {

        final Chat chat = mchat.get(position);
        String[] DateTime = getTimeDateANDLocal(chat.getDatetime());

        if (position == 0) {
            holder.message_date.setVisibility(View.VISIBLE);
            holder.message_date.setText(Month(DateTime[1]));
        } else {
            Chat chatlast = mchat.get(position - 1);
            String[] DateTimeGO = getTimeDateANDLocal(chatlast.getDatetime());

            if (!Month(DateTime[1]).equals(Month(DateTimeGO[1]))) {
                holder.message_date.setVisibility(View.VISIBLE);
                holder.message_date.setText(Month(DateTime[1]));
            }
        }


        if (chat.getMessagetype().equals("text")) {
            holder.message_type_text.setVisibility(View.VISIBLE);
            holder.message_type_image.setVisibility(View.GONE);
            holder.message_type_audio.setVisibility(View.GONE);
            holder.message_text_text.setText(chat.getMessage());
            holder.message_text_time.setText(DateTime[0]);
            holder.message_text_seen.setImageResource(chat.isIsseen() ? R.drawable.notseen : R.drawable.seenmessage);
        } else if (chat.getMessagetype().equals("image")) {
            holder.message_type_image.setVisibility(View.VISIBLE);
            holder.message_type_text.setVisibility(View.GONE);
            holder.message_type_audio.setVisibility(View.GONE);
            holder.message_image_time.setText(DateTime[0]);
            Glide.with(mcontext).load(chat.getMessage()).into(holder.message_image_image);
            holder.message_image_seen.setImageResource(chat.isIsseen() ? R.drawable.notseen : R.drawable.seenmessage);
        } else if (chat.getMessagetype().equals("audio")) {
            holder.message_type_audio.setVisibility(View.VISIBLE);
            holder.message_type_text.setVisibility(View.GONE);
            holder.message_type_image.setVisibility(View.GONE);
            holder.message_audio_time.setText(DateTime[0]);
            holder.message_audio_seen.setImageResource(chat.isIsseen() ? R.drawable.notseen : R.drawable.seenmessage);
            new Thread(() -> {
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    if (Build.VERSION.SDK_INT >= 14)
                        retriever.setDataSource(chat.getMessage(), new HashMap<String, String>());
                    else
                        retriever.setDataSource(chat.getMessage());
                    String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    long timeInmillisec = Long.parseLong(time);
                    holder.message_audio_duration.setText(milliSecondsToTimer(timeInmillisec));
                } catch (Exception e) {
                e.printStackTrace();
            }
            }).start();
            final Thread mediaplayerStart = new Thread(() -> {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(chat.getMessage());
                    mediaPlayer.prepareAsync();
                    }
                catch (IOException e) {
                    e.printStackTrace();
                    }
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    holder.message_audio_duration.setText("00:00 / " + milliSecondsToTimer(mediaPlayer.getDuration()));
                }
                });
                holder.message_audio_line.setMax(mediaPlayer.getDuration());
                holder.message_audio_line.setTag(position);
            });
            holder.message_audio_line.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mediaPlayer != null && fromUser) {
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mediaplayerStart.start();

            holder.message_audio_pause_and_stop.setOnClickListener(view -> {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    fuser = FirebaseAuth.getInstance().getCurrentUser();
                    holder.message_audio_pause_and_stop.setImageResource(chat.getSender().equals(fuser.getUid())?R.drawable.ic_baseline_pause_circle_button_right:R.drawable.ic_baseline_pause_circle_button_left);
                    run = new Runnable() {
                        @Override
                        public void run() {
                            holder.message_audio_line.setProgress(mediaPlayer.getCurrentPosition());
                            seekHandler.postDelayed(run, 1000);
                            int miliSeconds = mediaPlayer.getCurrentPosition();
                            if(miliSeconds!=0) {
                                long minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds);
                                long seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds);
                                if (minutes == 0) {
                                    holder.message_audio_duration.setText("00:" + seconds + "/" +milliSecondsToTimer(mediaPlayer.getDuration()));
                                } else {
                                    if (seconds >= 60) {
                                        long sec = seconds - (minutes * 60);
                                        holder.message_audio_duration.setText(minutes + ":" + sec+ " / " +milliSecondsToTimer(mediaPlayer.getDuration()));
                                    }
                                }
                            }else{
                                int totalTime=mediaPlayer.getDuration();
                                long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime);
                                long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime);
                                if (minutes == 0) {
                                    holder.message_audio_duration.setText("0:" + seconds);
                                } else {
                                    if (seconds >= 60) {
                                        long sec = seconds - (minutes * 60);
                                        holder.message_audio_duration.setText(minutes + ":" + sec);
                                    }
                                }
                            }
                        }

                    };
                    run.run();
                } else {
                    mediaPlayer.pause();
                    holder.message_audio_pause_and_stop.setImageResource(chat.getSender().equals(fuser.getUid())?R.drawable.ic_baseline_play_circle_button_right:R.drawable.ic_baseline_play_circle_button_left);
                }
            });
        }
    }

        private void prepareMediaPlayer(String url) {
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String milliSecondsToTimer(long milliSeconds) {
        String secondsString;

        int minutes = (int) ((milliSeconds % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) ((milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        secondsString = seconds < 10 ? "0" + seconds : "" + seconds;

        return minutes + ":" + secondsString;
    }

    @Override
    public int getItemCount() {
        return mchat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message_text_text, message_text_time, message_image_time, message_date, message_audio_duration, message_audio_time;
        public ImageView prof_ima, message_image_image, message_text_seen, message_image_seen, message_audio_seen;
        public RelativeLayout message_type_text, message_type_image, message_type_audio;
        public ImageButton message_audio_pause_and_stop;
        public SeekBar message_audio_line;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            message_date = itemView.findViewById(R.id.message_date);

            message_type_text = itemView.findViewById(R.id.type_text);
            message_type_image = itemView.findViewById(R.id.type_image);
            message_type_audio = itemView.findViewById(R.id.type_audio);

            message_audio_duration = itemView.findViewById(R.id.message_audio_duration);

            message_text_text = itemView.findViewById(R.id.message_text);
            message_image_image = itemView.findViewById(R.id.message_image);
            message_audio_line = itemView.findViewById(R.id.message_audio_bar);
            message_audio_line.setMax(100);

            prof_ima = itemView.findViewById(R.id.profile_image);
            message_audio_pause_and_stop = itemView.findViewById(R.id.message_audio_play_and_stop);

            message_text_seen = itemView.findViewById(R.id.message_text_status);
            message_image_seen = itemView.findViewById(R.id.message_image_status);
            message_audio_seen = itemView.findViewById(R.id.message_audio_status);

            message_text_time = itemView.findViewById(R.id.message_text_time);
            message_image_time = itemView.findViewById(R.id.message_image_time);
            message_audio_time = itemView.findViewById(R.id.message_audio_time);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mchat.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        else {
            return  MSG_TYPE_LEFT;
        }
    }

}