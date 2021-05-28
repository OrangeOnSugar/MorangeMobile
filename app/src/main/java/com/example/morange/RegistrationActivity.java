package com.example.morange;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.morange.ModeJS.UserINFO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    Button regdone;
    EditText loginreg, mailreg, passreg;
    TextView gotoauthoriz;

    FirebaseAuth authException;
    DatabaseReference reference;
    boolean VoidPer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_page);

        regdone = findViewById(R.id.Registrbu);
        loginreg = findViewById(R.id.login_reg);
        mailreg = findViewById(R.id.mail_reg);
        passreg = findViewById(R.id.passw_reg);
        gotoauthoriz = findViewById(R.id.autorizgo);

        authException = FirebaseAuth.getInstance();

        gotoauthoriz.setOnClickListener(v -> startActivity(new Intent(RegistrationActivity.this,AuthorizaActivity.class)));

        regdone.setOnClickListener(v -> {
            if (!loginreg.getText().toString().trim().equals("") && !mailreg.getText().toString().trim().equals("")
                    && !passreg.getText().toString().trim().equals("")) {
                if(passreg.getText().toString().trim().equals(passreg.getText().toString().trim())) {
                    String LoginR = loginreg.getText().toString().trim();
                    String PasswordR = passreg.getText().toString().trim();
                    String EmailR = mailreg.getText().toString().trim();
                    if(PasswordR.length() < 10) {
                        Toast.makeText(RegistrationActivity.this, "Длина пароля должна быть больше 10-ти символов", Toast.LENGTH_SHORT).show();
                    } else {
                        registeR(LoginR,EmailR,PasswordR);
                    }
                }
            }
        });
        loginreg.setOnFocusChangeListener((v, hasFocus) -> loginreg.setBackgroundResource(hasFocus==true?R.drawable.editabg:R.drawable.edita));
        mailreg.setOnFocusChangeListener((v, hasFocus) -> mailreg.setBackgroundResource(hasFocus==true?R.drawable.editabg:R.drawable.edita));
        passreg.setOnFocusChangeListener((v, hasFocus) -> passreg.setBackgroundResource(hasFocus==true?R.drawable.editabg:R.drawable.edita));
    }

    private void endRegister(final String login, final String email, String password){
        if(VoidPer) {
            Toast.makeText(RegistrationActivity.this, "Данный логин уже занят", Toast.LENGTH_SHORT).show();
            return;
        }
        authException.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                            FirebaseUser firebaseUser = authException.getCurrentUser();
                            assert firebaseUser != null;
                            final String userid = firebaseUser.getUid();
                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("lastseen", ServerValue.TIMESTAMP);
                            hashMap.put("email",email);
                            hashMap.put("login", login);
                            hashMap.put("username",login);
                            hashMap.put("ImageURL", "default");
                            hashMap.put("status", "офлайн");
                            hashMap.put("Rdate",ServerValue.TIMESTAMP);

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(RegistrationActivity.this,AuthorizaActivity.class);
                                        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                        Toast.makeText(RegistrationActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Вы не можете зарегистрировать аккаунт с такой почтой или паролем", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registeR(final String login, final String email, final String password) {
        VoidPer = false;
        Query query = FirebaseDatabase.getInstance().getReference("Users");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshots : dataSnapshot.getChildren()) {
                    UserINFO user = dataSnapshots.getValue(UserINFO.class);
                    assert user != null;
                    if (user.getLogin().equals(login)) {
                        VoidPer = true;
                        break;
                    }
                }
                endRegister(login, email, password);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
