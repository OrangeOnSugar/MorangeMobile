package com.example.morange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgetpass extends AppCompatActivity {

    EditText email;
    TextView gotoAuth;
    Button sendmes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpass);
        email = findViewById(R.id.mail_forg);
        gotoAuth = findViewById(R.id.authgo);
        gotoAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(forgetpass.this,AuthorizaActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        sendmes = findViewById(R.id.SendMesButton);
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus==true)
                {
                    email.setBackgroundResource(R.drawable.editabg);
                }
                else
                {
                    email.setBackgroundResource(R.drawable.edita);
                }
            }
        });
        sendmes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!email.getText().toString().trim().equals(""))
                {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.sendPasswordResetEmail(email.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Intent intent = new Intent(forgetpass.this,AuthorizaActivity.class);
                                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                Toast.makeText(forgetpass.this, "Письмо с инструкциями отправлено вам на почту", Toast.LENGTH_LONG).show();
                            }
                            else if(task.isCanceled())
                            {
                                Toast.makeText(forgetpass.this, "Неверна указана почта", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(forgetpass.this, "Неверна указана почта или аккаунт с данной почтой не существует", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}