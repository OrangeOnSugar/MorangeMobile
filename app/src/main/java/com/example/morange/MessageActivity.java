package com.example.morange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.morange.Adapter.MessageAdapter;
import com.example.morange.ModeJS.Chat;
import com.example.morange.ModeJS.UserINFO;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView userlog, seenstatus;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    ImageButton but_send, button_image_send;
    EditText mess_text;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;

    RelativeLayout UserInformation;

    Intent intent;

    String userid;

    ValueEventListener valueEventListener;

    private MediaRecorder recorder;
    private static String fileName = null;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageURI;
    private StorageTask uploadTask;

    String datetime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, Activity_Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        recyclerView = findViewById(R.id.recycler_viewd);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        UserInformation = findViewById(R.id.UserInformation);
        UserInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, profile.class);
                intent.putExtra("UserID",userid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MessageActivity.this.startActivity(intent);
            }
        });

        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();

        profile_image = findViewById(R.id.profile_image);
        userlog = findViewById(R.id.profile_name);
        but_send = findViewById(R.id.message_send);
        button_image_send = findViewById(R.id.image_send);
        mess_text = findViewById(R.id.message_text);
        seenstatus = findViewById(R.id.last_seen);

        mess_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mess_text.getText().toString().equals(""))
                {
                    but_send.setBackgroundResource(R.drawable.ic_baseline_mic_24);
                }
                else
                {
                    but_send.setBackgroundResource(R.drawable.ic_action_name);
                }
            }
        });

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        but_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mess = mess_text.getText().toString().trim();
                if (!mess.equals("")) {
                    SendMessange(firebaseUser.getUid(), userid, mess,"text");
                } else {
                    Toast.makeText(MessageActivity.this, "Вы не ввели сообщение", Toast.LENGTH_SHORT).show();
                }
                mess_text.setText("");
            }
        });

        /*but_send.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mess_text.getText().toString().trim().equals(""))
                {
                    switch (event.getActionMasked())
                    {
                        case MotionEvent.ACTION_DOWN:
                            fileName = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(new Date());
                            startRecording();
                            break;
                        case MotionEvent.ACTION_UP:
                            stopRecording();
                            break;
                    }
                }
                return true;
            }
        });*/

        button_image_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        storageReference = FirebaseStorage.getInstance().getReference("uploads/"+firebaseUser.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserINFO userINFO = dataSnapshot.getValue(UserINFO.class);
                userlog.setText(userINFO.getUsername());
                if (userINFO.getStatus().equals("офлайн")) {
                    seenstatus.setText("был(а) в сети недавно");
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

    private void openImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage()
    {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Загрузка");
        pd.show();

        if(imageURI != null)
        {
            final StorageReference storageReferense = storageReference.child(System.currentTimeMillis()+
                    "."+getFileExtension(imageURI));

            uploadTask = storageReferense.putFile(imageURI);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return storageReferense.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
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
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
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
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
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
                if (dataSnapshot.hasChild("Chats/" + userid + "|" + firebaseUser.getUid())) {
                    seenmassageyes(FirebaseDatabase.getInstance().getReference("Chats").child(userid + "|" + firebaseUser.getUid()), userid);
                } else {
                    seenmassageyes((FirebaseDatabase.getInstance().getReference("Chats").child(firebaseUser.getUid() + "|" + userid)), userid);
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
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(receiver + "|" + firebaseUser.getUid());
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        reference.push().setValue(hashMap);
                    }
                    else
                    {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats").child(firebaseUser.getUid() + "|" + receiver);
                        ref.push().setValue(hashMap);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

/*    public String getTimeDate() {
        String url = "https://worldtimeapi.org/api/timezone/WET";
        datetime = "";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response!=null){
                            try {
                                datetime = response.getString("datetime");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            Toast.makeText(MessageActivity.this, "Ничего не возращает", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        Volley.newRequestQueue(this).add(request);
        return datetime;
    }*/



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

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(userid + "|" + myid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    readMessagesToAdapter(reference,imageurl);
                }
                else
                {
                    readMessagesToAdapter(FirebaseDatabase.getInstance().getReference("Chats").child(myid + "|" + userid),imageurl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("онлайн");
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(valueEventListener);
        status("офлайн");
    }
}
