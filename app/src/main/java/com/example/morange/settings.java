package com.example.morange;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import yuku.ambilwarna.AmbilWarnaDialog;

public class settings extends AppCompatActivity {

    ImageView colorHead, colorBackground;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    Toolbar toolbar;

    int DefaultHeadColor;
    int DefaultBackColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final SharedPreferences BackCo = this.getApplicationContext().getSharedPreferences("BackColor", 0);
        final SharedPreferences HeCo = this.getApplicationContext().getSharedPreferences("ColorHead", 0);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(settings.this, Activity_Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        colorBackground = findViewById(R.id.Color_Back_Dialog_Picker);
        colorHead = findViewById(R.id.Color_Head_Picker);

    }

    private void openColorDialog(int defaultHeadColor){
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, defaultHeadColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {

            }
        });
        dialog.show();
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