package com.example.morange;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.morange.Adapter.AddFriendsAdapter;
import com.example.morange.Adapter.UserAdapter;
import com.example.morange.ModeJS.Friends;
import com.example.morange.ModeJS.UserINFO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddFriends_Activity extends AppCompatActivity {

    EditText search;
    RecyclerView recyclerView;
    List<UserINFO> musers;
    List<Friends> frienda;
    AddFriendsAdapter addFriendsAdapter;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddFriends_Activity.this,Activity_Main.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        recyclerView = findViewById(R.id.recycler_viewd);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        musers = new ArrayList<>();
        frienda = new ArrayList<>();

        readUsers();

        search = findViewById(R.id.search_user);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search_users(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("Friends");

        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                frienda.clear();
                for (DataSnapshot snapshot2 : dataSnapshot.getChildren())
                {
                    Friends friends = snapshot2.getValue(Friends.class);
                    assert firebaseUser != null;
                    assert friends != null;
                    if(friends.getДобавил().equals(firebaseUser.getUid()))
                    {
                        frienda.add(friends);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (search.getText().toString().equals(""))
                {
                    musers.clear();
                    int ssa=0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserINFO userINFO = snapshot.getValue(UserINFO.class);

                        for(int i=0;i<frienda.size();i++)
                        {
                            assert userINFO != null;
                            if (frienda.get(i).getДобавленный().equals(userINFO.getId())) {
                                    ssa = 1;
                                    break;
                                }
                        }
                        assert userINFO != null;
                        assert firebaseUser != null;
                        if(ssa==0 && !userINFO.getId().equals(firebaseUser.getUid()))
                        {
                            musers.add(userINFO);
                        }
                        ssa = 0;
                    }

                    addFriendsAdapter = new AddFriendsAdapter(AddFriends_Activity.this, musers, true);
                    recyclerView.setAdapter(addFriendsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void search_users(String toString) {

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("login")
                .startAt(toString)
                .endAt(toString+"\uf8ff");

        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("Friends");

        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                frienda.clear();
                for (DataSnapshot snapshot2 : dataSnapshot.getChildren())
                {
                    Friends friends = snapshot2.getValue(Friends.class);
                    assert firebaseUser != null;
                    assert friends != null;
                    if(friends.getДобавил().equals(fuser.getUid()))
                    {
                        frienda.add(friends);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                musers.clear();
                int ssa=0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    UserINFO userINFO = snapshot.getValue(UserINFO.class);

                    for(int i=0;i<frienda.size();i++)
                    {
                        if (frienda.get(i).getДобавленный().equals(userINFO.getId())) {
                            ssa = 1;
                            break;
                        }
                    }
                    if(ssa==0 && !userINFO.getId().equals(fuser.getUid()))
                    {
                        musers.add(userINFO);
                    }
                    ssa = 0;
                }

                addFriendsAdapter = new AddFriendsAdapter(AddFriends_Activity.this, musers, true);
                recyclerView.setAdapter(addFriendsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void status(String status)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
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
