package com.example.morange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class profile extends AppCompatActivity {

    TextView Email,Login,Lastseen,Username;
    Button ChangeUsername;
    ImageButton ChangeImageProfile;
    CircleImageView ImageProfile;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    String UserID;

    Intent intent;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageURI;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        intent = getIntent();
        UserID = intent.getStringExtra("UserID");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (UserID.equals(firebaseUser.getUid()))
                {
                    startActivity(new Intent(profile.this, Activity_Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
                else
                {
                    Intent intent = new Intent(profile.this, MessageActivity.class);
                    intent.putExtra("userid",UserID);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    profile.this.startActivity(intent);
                }
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(UserID);

        Email = findViewById(R.id.profile_email_text);
        Login = findViewById(R.id.profile_login_text);
        Lastseen = findViewById(R.id.profile_lastseen);
        Username = findViewById(R.id.profile_nickname);

        ChangeUsername = findViewById(R.id.change_nickname);

        ChangeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserNameChange(Username.getText().toString());
            }
        });

        ChangeImageProfile = findViewById(R.id.change_image);

        ImageProfile = findViewById(R.id.profile_image);

        ChangeImageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserINFO userINFO = dataSnapshot.getValue(UserINFO.class);

                Email.setText(userINFO.getEmail());
                Username.setText(userINFO.getUsername());
                Login.setText(userINFO.getLogin());

                if(UserID.equals(firebaseUser.getUid()))
                {
                    ChangeImageProfile.setVisibility(View.VISIBLE);
                    ChangeUsername.setVisibility(View.VISIBLE);
                }
                else
                {
                    Lastseen.setText("был(а) в сети недавно");
                    Lastseen.setVisibility(View.VISIBLE);
                }
                if (userINFO.getImageURL().equals("default")) {
                    ImageProfile.setImageResource(R.drawable.userbackds);
                } else {
                    Glide.with(getApplicationContext()).load(userINFO.getImageURL()).into(ImageProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void UserNameChange(String username)
    {
        AlertDialog.Builder usernamechange = new AlertDialog.Builder(this);
        usernamechange.setTitle("Изменение никнейма");
        final EditText usernameText = new EditText(this);
        usernameText.setText(username);
        usernamechange.setView(usernameText);
        usernamechange.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!usernameText.getText().toString().trim().equals("") && usernameText.length()>2){
                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("username", usernameText.getText().toString().trim());

                    databaseReference.updateChildren(hashMap);
                }
                else{
                    Toast.makeText(profile.this, "Длина никнейма должна быть больше 2 символов", Toast.LENGTH_SHORT).show();
                }
            }
        });
        usernamechange.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        usernamechange.show();
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

                        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("ImageURL", mUri);
                        databaseReference.updateChildren(map);

                        pd.dismiss();
                    }
                    else
                    {
                        Toast.makeText(profile.this, "Ошибка", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
        else
        {
            Toast.makeText(profile.this, "Изображение не выбранно", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(profile.this, "Загрузка в процессе", Toast.LENGTH_SHORT).show();
            }
            else
            {
                uploadImage();
            }

        }
    }

    private void status(String status)
    {
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
        status("офлайн");
    }
}