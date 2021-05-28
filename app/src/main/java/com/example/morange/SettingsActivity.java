package com.example.morange;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.morange.HelpClasses.OfflineOfflineChecker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import yuku.ambilwarna.AmbilWarnaDialog;

public class SettingsActivity extends AppCompatActivity {

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
        OfflineOfflineChecker.status(firebaseUser);

        final SharedPreferences BackCo = this.getApplicationContext().getSharedPreferences("BackColor", 0);
        final SharedPreferences HeCo = this.getApplicationContext().getSharedPreferences("ColorHead", 0);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, Activity_Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

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


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}