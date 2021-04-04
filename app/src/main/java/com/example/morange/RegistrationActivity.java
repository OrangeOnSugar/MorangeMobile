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

        gotoauthoriz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this,AuthorizaActivity.class));
            }
        });

        regdone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loginreg.getText().toString().trim().equals("") && !mailreg.getText().toString().trim().equals("")
                        && !passreg.getText().toString().trim().equals(""))
                {
                    if(passreg.getText().toString().trim().equals(passreg.getText().toString().trim()))
                    {
                        String LoginR = loginreg.getText().toString().trim();
                        String PasswordR = passreg.getText().toString().trim();
                        String EmailR = mailreg.getText().toString().trim();
                        if(PasswordR.length() < 10)
                        {
                            Toast.makeText(RegistrationActivity.this, "Длина пароля должна быть больше 10-ти символов", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            registeR(LoginR,EmailR,PasswordR);
                        }
                    }

                    else
                    {

                    }
                }

            }
        });
        loginreg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus==true)
                {
                    loginreg.setBackgroundResource(R.drawable.editabg);
                }
                else
                {
                    loginreg.setBackgroundResource(R.drawable.edita);
                }
            }
        });
        mailreg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus==true)
                {
                    mailreg.setBackgroundResource(R.drawable.editabg);
                }
                else
                {
                    mailreg.setBackgroundResource(R.drawable.edita);
                }
            }
        });
        passreg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus==true)
                {
                    passreg.setBackgroundResource(R.drawable.editabg);
                }
                else
                {
                    passreg.setBackgroundResource(R.drawable.edita);
                }
            }
        });
    }

    private void registeR(final String login, final String email, String password)
    {
        VoidPer = false;
        authException.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            FirebaseUser firebaseUser = authException.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                            Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("login");
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot dataSnapshots : dataSnapshot.getChildren())
                                    {
                                        UserINFO user = dataSnapshots.getValue(UserINFO.class);
                                        assert user != null;
                                        if (user.getLogin().equals(login))
                                        {
                                            VoidPer = true;
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            if(VoidPer)
                            {
                                Toast.makeText(RegistrationActivity.this, "Данный логин уже занят", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", userid);
                                hashMap.put("lastseen", ServerValue.TIMESTAMP);
                                hashMap.put("email",email);
                                hashMap.put("login", login);
                                hashMap.put("username",login);
                                hashMap.put("ImageURL", "default");
                                hashMap.put("status", "офлайн");

                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            Intent intent = new Intent(RegistrationActivity.this,AuthorizaActivity.class);
                                            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                            Toast.makeText(RegistrationActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                        else
                        {
                            Toast.makeText(RegistrationActivity.this, "Вы не можете зарегистрировать аккаунт с такой почтой или паролем", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
