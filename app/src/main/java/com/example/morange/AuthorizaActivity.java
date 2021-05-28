package com.example.morange;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AuthorizaActivity extends Activity {

    FirebaseAuth auth;

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
        gotoPassForgot.setOnClickListener(v -> startActivity(new Intent(AuthorizaActivity.this, ForgetPassActivity.class)));

        auth = FirebaseAuth.getInstance();

        registergod.setOnClickListener(v -> startActivity(new Intent(AuthorizaActivity.this,RegistrationActivity.class)));

        okey.setOnClickListener(v -> {
            String mailau = milo.getText().toString().trim(), passau = parol.getText().toString().trim();

            if (TextUtils.isEmpty(mailau) || TextUtils.isEmpty(passau)) {
                Toast.makeText(AuthorizaActivity.this, "Почта и пароль обязательны к заполнению", Toast.LENGTH_SHORT).show();
            } else {
                auth.signInWithEmailAndPassword(mailau,passau)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                startActivity(new Intent(AuthorizaActivity.this,Activity_Main.class).addFlags(new Intent().FLAG_ACTIVITY_CLEAR_TASK | new Intent().FLAG_ACTIVITY_NEW_TASK));
                            } else {
                                Toast.makeText(AuthorizaActivity.this, "Авторизация прошла неудачно", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        milo.setOnFocusChangeListener((v, hasFocus) -> milo.setBackgroundResource(hasFocus==true?R.drawable.editabg:R.drawable.edita));

        parol.setOnFocusChangeListener((v, hasFocus) -> parol.setBackgroundResource(hasFocus==true?R.drawable.editabg:R.drawable.edita));

    }


}
