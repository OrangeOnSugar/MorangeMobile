package com.example.morange.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.morange.Adapter.FriendsAdapter;
import com.example.morange.Adapter.UserAdapter;
import com.example.morange.ModeJS.Friends;
import com.example.morange.ModeJS.UserINFO;
import com.example.morange.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText search_user;
    private FriendsAdapter friendsAdapter;
    private List<UserINFO> musers;
    private List<Friends> frienda;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        musers = new ArrayList<>();
        frienda = new ArrayList<>();

        readUsers();

        search_user = view.findViewById(R.id.search_user);
        search_user.addTextChangedListener(new TextWatcher() {
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

        return view;
    }

    private void search_users(String toString) {

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("login")
                .startAt(toString)
                .endAt(toString.toLowerCase()+"\uf8ff");
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Friends");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    frienda.clear();
                    for (DataSnapshot snapshot2 : dataSnapshot.getChildren()) {
                        Friends friends = snapshot2.getValue(Friends.class);
                        assert friends != null;
                        assert fuser != null;
                        if (friends.getДобавил().equals(fuser.getUid())) {
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

                        for (int i = 0; i < frienda.size(); i++) {
                            assert userINFO != null;
                            if (frienda.get(i).getДобавленный().equals(userINFO.getId())) {
                                ssa = 1;
                                break;
                            }
                        }
                        assert userINFO != null;
                        assert fuser != null;
                        if (ssa == 1 && !userINFO.getId().equals(fuser.getUid())) {
                            musers.add(userINFO);
                        }
                        ssa = 0;
                    }

                    friendsAdapter = new FriendsAdapter(getContext(), musers, true);
                    recyclerView.setAdapter(friendsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        final DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("Friends");

        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    frienda.clear();
                    for (DataSnapshot snapshot2 : dataSnapshot.getChildren()) {
                        Friends friends = snapshot2.getValue(Friends.class);
                        assert firebaseUser != null;
                        assert friends != null;
                        if (friends.getДобавил().equals(firebaseUser.getUid())) {
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

                    if (search_user.getText().toString().equals("")) {
                        musers.clear();
                        int ssa = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            UserINFO userINFO = snapshot.getValue(UserINFO.class);

                            for (int i = 0; i < frienda.size(); i++) {
                                assert userINFO != null;
                                if (frienda.get(i).getДобавленный().equals(userINFO.getId())) {
                                    ssa = 1;
                                    break;
                                }
                            }
                            assert userINFO != null;
                            assert firebaseUser != null;
                            if (ssa == 1 && !userINFO.getId().equals(firebaseUser.getUid())) {
                                musers.add(userINFO);
                            }
                            ssa = 0;
                        }

                        friendsAdapter = new FriendsAdapter(getContext(), musers, true);
                        recyclerView.setAdapter(friendsAdapter);
                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
