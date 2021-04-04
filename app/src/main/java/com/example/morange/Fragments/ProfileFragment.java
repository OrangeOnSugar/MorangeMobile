package com.example.morange.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import static android.app.Activity.RESULT_OK;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.morange.ModeJS.UserINFO;
import com.example.morange.R;
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

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;



public class ProfileFragment extends Fragment {

    CircleImageView image_prof;
    TextView login;
    ImageButton image_change;

    DatabaseReference reference;
    FirebaseUser user;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageURI;
    private StorageTask uploadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        image_prof = view.findViewById(R.id.profile_image);
        login = view.findViewById(R.id.login);
        image_change = view.findViewById(R.id.change_image);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    UserINFO user = dataSnapshot.getValue(UserINFO.class);
                    login.setText(user.getLogin());
                    if (user.getImageURL().equals("default")) {
                        image_prof.setImageResource(R.drawable.userbackds);
                    } else {
                        Glide.with(getContext()).load(user.getImageURL()).into(image_prof);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        image_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        return view;
    }

    private void openImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage()
    {
        final ProgressDialog pd = new ProgressDialog(getContext());
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

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("ImageURL", mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    }
                    else 
                    {
                        Toast.makeText(getContext(), "Ошибка", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Изображение не выбранно", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Загрузка в процессе", Toast.LENGTH_SHORT).show();
            }
            else
            {
                uploadImage();
            }

        }
    }
}
