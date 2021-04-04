package com.example.morange;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;


public class AuthorizaActivity extends Activity {

    FirebaseAuth auth;
    DatabaseReference reference;

    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null)
        {
            Intent intent = new Intent(AuthorizaActivity.this,Activity_Main.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authorization_page);
        final EditText milo = findViewById(R.id.mail_au), parol = findViewById(R.id.pass_au);
        Button okey = findViewById(R.id.Authorizbu);
        TextView registergod = findViewById(R.id.registrationgo);

        TextView gotoPassForgot = findViewById(R.id.forget_pass);
        gotoPassForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AuthorizaActivity.this, forgetpass.class));
            }
        });

        auth = FirebaseAuth.getInstance();

        registergod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AuthorizaActivity.this,RegistrationActivity.class));
            }
        });

        okey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mailau = milo.getText().toString().trim(), passau = parol.getText().toString().trim();

                if (TextUtils.isEmpty(mailau) || TextUtils.isEmpty(passau))
                {
                    Toast.makeText(AuthorizaActivity.this, "Почта и пароль обязательны к заполнению", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    auth.signInWithEmailAndPassword(mailau,passau)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful())
                                    {
                                        Intent intent = new Intent(AuthorizaActivity.this,Activity_Main.class);
                                        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else 
                                    {
                                        Toast.makeText(AuthorizaActivity.this, "Авторизация прошла неудачно", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        milo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus==true)
                {
                    milo.setBackgroundResource(R.drawable.editabg);
                }
                else
                {
                    milo.setBackgroundResource(R.drawable.edita);
                }
            }
        });

        parol.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus==true)
                {
                    parol.setBackgroundResource(R.drawable.editabg);
                }
                else
                {
                    parol.setBackgroundResource(R.drawable.edita);
                }
            }
        });

    }


}
