package com.example.morange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.morange.Adapter.MessageAdapter;
import com.example.morange.HelpClasses.DateTimeDifferent;
import com.example.morange.HelpClasses.SingleTapConfirm;
import com.example.morange.Interface.APIService;
import com.example.morange.ModeJS.Chat;
import com.example.morange.ModeJS.UserINFO;
import com.example.morange.Notifications.Client;
import com.example.morange.Notifications.Data;
import com.example.morange.Notifications.MyResponse;
import com.example.morange.Notifications.Sender;
import com.example.morange.Notifications.Token;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.vanniktech.emoji.EmojiPopup;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import com.example.morange.HelpClasses.OfflineOfflineChecker;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView userlog, seenstatus, audio_record_duration;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    ImageButton but_send, button_image_send, button_keyboard_or_emoji;
    EditText mess_text;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;

    RelativeLayout UserInformation, audio_record_panel;

    Intent intent;

    String userid;
    String currentUserId;

    ValueEventListener valueEventListener;
    private GestureDetector gestureDetector;

    private MediaRecorder recorder;
    private static String fileName = null;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageURI;
    private StorageTask uploadTask;
    String child;
    String emailReceiver;
    DatabaseReference chatRefrence;

    Timer timer;
    TimerTask timerTask;
    Double time = 0.00;

    APIService apiService;
    boolean notification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(MessageActivity.this, Activity_Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        audio_record_panel = findViewById(R.id.audio_record_panel);
        audio_record_duration = findViewById(R.id.audio_record_duration);

        timer = new Timer();

        recyclerView = findViewById(R.id.recycler_viewd);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        UserInformation = findViewById(R.id.UserInformation);
        UserInformation.setOnClickListener(v -> {
            Intent intent = new Intent(MessageActivity.this, ProfileActivity.class);
            intent.putExtra("UserID",userid);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MessageActivity.this.startActivity(intent);
        });

        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();

        profile_image = findViewById(R.id.profile_image);
        userlog = findViewById(R.id.profile_name);
        but_send = findViewById(R.id.message_send);
        button_image_send = findViewById(R.id.image_send);
        mess_text = findViewById(R.id.message_text);
        seenstatus = findViewById(R.id.last_seen);
        button_keyboard_or_emoji = findViewById(R.id.message_keyboard_or_emojiboard);

        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.message_activity_root)).build(mess_text);

        button_keyboard_or_emoji.setOnClickListener(v -> {
            button_keyboard_or_emoji.setImageDrawable((button_keyboard_or_emoji.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_baseline_keyboard).getConstantState())
                    ?getResources().getDrawable(R.drawable.ic_baseline_tag_smile):getResources().getDrawable(R.drawable.ic_baseline_keyboard));
            emojiPopup.toggle();
        });

        mess_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                but_send.setBackgroundResource(mess_text.getText().toString().equals("")?R.drawable.ic_baseline_mic_24:R.drawable.ic_action_name);
            }
        });

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        gestureDetector = new GestureDetector(this, new SingleTapConfirm());

        but_send.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(gestureDetector.onTouchEvent(event)){
                    notification = true;
                    String mess = mess_text.getText().toString().trim();
                    if (!mess.equals("")) {
                        SendMessange(firebaseUser.getUid(), userid, mess,"text");
                    } else {
                        Toast.makeText(MessageActivity.this, "Вы не ввели сообщение", Toast.LENGTH_SHORT).show();
                    }
                    mess_text.setText("");
                    return true;
                } else {
                    if (mess_text.getText().toString().trim().equals("")) {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Morange/audio/" + firebaseUser.getUid() + Calendar.getInstance().getTimeInMillis() + ".ogg";
                                File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Morange", "audio");
                                if (!path.exists()) {
                                    path.mkdirs();
                                }
                                startRecording();
                                break;
                            case MotionEvent.ACTION_UP:
                                stopRecording();
                                break;
                        }
                    }
                }
                return false;
            }
        });

        button_image_send.setOnClickListener(v -> openImage());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = firebaseUser.getUid();
        OfflineOfflineChecker.status(firebaseUser);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        storageReference = FirebaseStorage.getInstance().getReference("uploads/"+firebaseUser.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserINFO userINFO = dataSnapshot.getValue(UserINFO.class);
                userlog.setText(userINFO.getUsername());
                emailReceiver = userINFO.getEmail();
                if (userINFO.getStatus().equals("офлайн")) {
                    try {
                        hoursDiffernt(userINFO.getLastseen());
                    } catch (ParseException e) {
                        seenstatus.setText("был(а) в сети недавно");
                    }
                } else {
                    seenstatus.setText("онлайн");
                }
                if (userINFO.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.drawable.userbackds);
                } else {
                    Glide.with(getApplicationContext()).load(userINFO.getImageURL()).into(profile_image);
                }

                readMessages(firebaseUser.getUid(), userid, userINFO.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenmassage(userid);
    }


    private void hoursDiffernt(long timestamp) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ssZ");
        String DateTime = format.format(new Date(timestamp));
        Date value;
        String[] date_and_timeGet = {"",""};
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        try
        {
            value = format.parse(DateTime);
            SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm");
            newFormat.setTimeZone(TimeZone.getDefault());
            DateTime = newFormat.format(value);
            date_and_timeGet[0] = DateTime.substring(DateTime.indexOf("T")+1);
            date_and_timeGet[1] = DateTime.substring(0,DateTime.indexOf("T"));
            value = newFormat.parse(DateTime);
        }
        catch (ParseException e) {
            e.printStackTrace();
            date_and_timeGet = new String[]{"", ""};
            value = new Date();
        }
        dateDiffrent(value,date_and_timeGet);
    }

    private void dateDiffrent(Date date_time,String[] date_and_timeGet) throws ParseException {
        if(!date_time.equals("")){
            String currentDateTimeString = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm").format(System.currentTimeMillis());
            Date currentDate = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm").parse(currentDateTimeString);
            long different[];
            different = DateTimeDifferent.ReturnDifferent(currentDate,date_time);

            long hoursEnd = different[2];
            long minutesEnd = different[3];

            while (hoursEnd >= 10){
                hoursEnd -= 10;
            }
            while (minutesEnd >= 10){
                minutesEnd -= 10;
            }

            if(different.length > 0){
                if (different[1] == 1){
                    seenstatus.setText("был(а) в сети вчера в "+date_and_timeGet[0]);
                }
                else if(different[1] == 0 && different[2] == 1){
                    seenstatus.setText("был(а) в сети час назад");
                }
                else if(different[1] == 0 && (hoursEnd > 1 && hoursEnd < 5)){
                    seenstatus.setText("был(а) в сети "+different[2]+" часа назад");
                }
                else if(different[1] == 0 && different[2] !=0 && (hoursEnd == 0 || hoursEnd > 4 || (different[2] >=10 && different[2] <=20))){
                    seenstatus.setText("был(а) в сети "+different[2]+" часов назад");
                }
                else if(different[1] == 0 && different[2] == 0 && different[3] == 1){
                    seenstatus.setText("был(а) в сети минуту назад");
                }
                else if(different[1] == 0 && different[2] == 0 && (minutesEnd > 1 && minutesEnd < 5)){
                    seenstatus.setText("был(а) в сети "+different[3]+" минуты назад");
                }
                else if(different[1] == 0 && different[2] ==0 && different[3] !=0 && (minutesEnd == 0 || minutesEnd > 4 || (different[3] >=10 && different[3] <=20))){
                    seenstatus.setText("был(а) в сети "+different[3]+" минут назад");
                }
                else if(different[1] == 0 && different[2] == 0 && different[3] == 0){
                    seenstatus.setText("был(а) в сети только что");
                }
                else{
                    seenstatus.setText("был(а) в сети "+ date_and_timeGet[1]+" в " + date_and_timeGet[0]);
                }

            }
        }
        else{
            seenstatus.setText("был(а) в сети недавно");
        }
    }

    private void openImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Загрузка");
        pd.show();

        if(imageURI != null)
        {
            final StorageReference storageReferense = storageReference.child(System.currentTimeMillis()+
                    "."+getFileExtension(imageURI));

            uploadTask = storageReferense.putFile(imageURI);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if(!task.isSuccessful())
                {
                    throw task.getException();
                }

                return storageReferense.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if(task.isSuccessful())
                {
                    Uri downUri = task.getResult();
                    String mUri = downUri.toString();

                    SendMessange(firebaseUser.getUid(), userid, mUri,"image");

                    pd.dismiss();
                }
                else
                {
                    Toast.makeText(MessageActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        }
        else
        {
            Toast.makeText(MessageActivity.this, "Изображение не выбранно", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data!=null && data.getData() != null)
        {
            imageURI = data.getData();


            if(uploadTask != null && uploadTask.isInProgress())
            {
                Toast.makeText(MessageActivity.this, "Загрузка в процессе", Toast.LENGTH_SHORT).show();
            }
            else
            {
                uploadImage();
            }

        }
    }


    private void startRecording() {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(fileName);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.prepare();
            recorder.start();
            startTimer();
            audio_record_panel.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        timerTask.cancel();
        time = 0.0;
        audio_record_panel.setVisibility(View.GONE);
        audio_record_duration.setText(formatTime(0,0));
        CheckChatExist();
    }

    private void CheckChatExist(){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(userid + "|" + currentUserId).child("messages");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    child = "uploads/" + userid + "|" + currentUserId;
                }
                else {
                    child = "uploads/" + currentUserId + "|" + userid;
                }
                uploadVoiceMessage(fileName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void uploadVoiceMessage(String fileName) {

        final Uri file = Uri.parse(fileName);
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(child).child(firebaseUser.getUid()+Calendar.getInstance().getTimeInMillis()+getFileExtension(file));

        storageReference.putFile(Uri.fromFile(new File(fileName)))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                SendMessange(firebaseUser.getUid(), userid, uri.toString(), "audio");
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MessageActivity.this, "Ошибка отправки", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startTimer()
    {
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        time++;
                        audio_record_duration.setText(getTimerText());
                    }
                });
            }

        };
        timer.scheduleAtFixedRate(timerTask, 0 ,1000);
    }


    private String getTimerText()
    {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;

        return formatTime(seconds, minutes);
    }

    private String formatTime(int seconds, int minutes)
    {
        return String.format(String.format("%02d",minutes) + " : " + String.format("%02d",seconds));
    }


    private void setCurrentUser(String currentUser){
        SharedPreferences.Editor editor = getSharedPreferences("isHere", MODE_PRIVATE).edit();
        editor.putString("currentUser",currentUser);
        editor.apply();
    }

    private void seenmassageyes(DatabaseReference reference, final String useid) {
        valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Chat chat = dataSnapshot1.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(useid)) {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        dataSnapshot1.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenmassage(final String userid) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Chats/" + userid + "|" + firebaseUser.getUid()+"/messages")) {
                    seenmassageyes(FirebaseDatabase.getInstance().getReference("Chats").child(userid + "|" + firebaseUser.getUid()).child("messages"), userid);
                } else {
                    seenmassageyes((FirebaseDatabase.getInstance().getReference("Chats").child(firebaseUser.getUid() + "|" + userid)).child("messages"), userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendMessange(final String sender, final String receiver, final String message, final String type) {
            final HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", sender);
            hashMap.put("receiver", receiver);
            hashMap.put("message", message);
            hashMap.put("isseen", false);
            hashMap.put("datetime", ServerValue.TIMESTAMP);
            hashMap.put("messagetype", type);

            final DatabaseReference participants = FirebaseDatabase.getInstance().getReference("Chats").child(receiver + "|" + firebaseUser.getUid()).child("Participants");
            participants.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        HashMap<String,Object> hashMap1 = new HashMap<>();
                        hashMap1.put(firebaseUser.getUid(),firebaseUser.getEmail());
                        hashMap1.put(receiver, emailReceiver);
                        participants.setValue(hashMap1);
                    }
                    else {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats").child(firebaseUser.getUid() + "|" + receiver).child("Participants");
                        HashMap<String,Object> hashMap1 = new HashMap<>();
                        hashMap1.put(firebaseUser.getUid(),firebaseUser.getEmail());
                        hashMap1.put(receiver, emailReceiver);
                        ref.setValue(hashMap1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(receiver + "|" + firebaseUser.getUid()).child("messages");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        reference.push().setValue(hashMap);
                    }
                    else
                    {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats").child(firebaseUser.getUid() + "|" + receiver).child("messages");
                        ref.push().setValue(hashMap);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final String messege = message;

            final DatabaseReference newReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            newReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserINFO userINFO = dataSnapshot.getValue(UserINFO.class);
                    if(notification){
                        SendNotification(receiver,userINFO.getUsername(),messege);
                    }
                    notification = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    private void SendNotification(String receiver, String username, String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Users")
                .child(receiver)
                .child("tokens");
        tokens.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Token token = new Token(dataSnapshot1.getValue(String.class));
                    Data data = new Data(firebaseUser.getUid(), R.drawable.ic_vegetarian, username+": "+message,"Новое сообщение",userid);

                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(MessageActivity.this, "Ошибка!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Toast.makeText(MessageActivity.this, "Some mistake", Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessagesToAdapter(DatabaseReference ref, final String imageurl)
    {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    mchat.add(chat);
                }

                messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void readMessages(final String myid, final String userid, final String imageurl) {
        mchat = new ArrayList<>();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(userid + "|" + myid).child("messages");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    readMessagesToAdapter(reference,imageurl);
                }
                else
                {
                    readMessagesToAdapter(FirebaseDatabase.getInstance().getReference("Chats").child(myid + "|" + userid).child("messages"),imageurl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCurrentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(valueEventListener);
        setCurrentUser("None");
    }
}
