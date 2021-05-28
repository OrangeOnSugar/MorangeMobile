package com.example.morange;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.morange.Adapter.UserAdapter;
import com.example.morange.HelpClasses.OfflineOfflineChecker;
import com.example.morange.ModeJS.UserINFO;
import com.example.morange.Notifications.Token;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Main extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<UserINFO> mUsers;
    private ShimmerFrameLayout shimmerFrameLayout;

    private List<String> userList;

    private boolean senderorreceiver;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    CircleImageView pro_i;
    TextView login_s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__main);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        OfflineOfflineChecker.status(firebaseUser);
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            if(menuItem.getItemId() == R.id.addfriends) {
                startActivity(new Intent(Activity_Main.this, UsersActivity.class));
            } else if (menuItem.getItemId() == R.id.settings) {
                startActivity(new Intent(Activity_Main.this, SettingsActivity.class));
            } else if (menuItem.getItemId() == R.id.exit) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Activity_Main.this,AuthorizaActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
            return false;
        });
        View header = navigationView.getHeaderView(0);
        pro_i = header.findViewById(R.id.userLogo);
        pro_i.setOnClickListener(v -> {
            Intent intent = new Intent(Activity_Main.this, ProfileActivity.class);
            intent.putExtra("UserID",firebaseUser.getUid());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Activity_Main.this.startActivity(intent);
        });

        login_s = header.findViewById(R.id.userLogin);

        shimmerFrameLayout = findViewById(R.id.shimmerLayout);
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserINFO userINFO = dataSnapshot.getValue(UserINFO.class);
                login_s.setText(userINFO.getUsername());
                Glide.with(getApplicationContext()).load(userINFO.getImageURL().equals("default")?R.drawable.userbackds:userINFO.getImageURL()).into(pro_i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recyclerView = findViewById(R.id.chats);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        userList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().contains("|"+firebaseUser.getUid())) {
                            userList.add(snapshot.getKey().replace("|"+firebaseUser.getUid(),""));
                            senderorreceiver = true;
                        }
                        else if(snapshot.getKey().contains(firebaseUser.getUid()+"|")) {
                            userList.add(snapshot.getKey().replace(firebaseUser.getUid()+"|",""));
                            senderorreceiver = false;
                        }
                        readChats();
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> UpdateToken(s));
    }

    private void UpdateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(firebaseUser.getUid())
                .child("tokens");
        Token newToken = new Token(token);
        HashMap<String,Object> key = new HashMap<>();
        key.put(newToken.getToken(),newToken.getToken());
        reference.setValue(key);
    }

    private void readChats() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserINFO user =  snapshot.getValue(UserINFO.class);
                    for (String id : userList) {
                        assert user != null;
                        if( user.getId().equals(id) && !mUsers.contains(user)) {
                            mUsers.add (user);
                        }
                    }
                }
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                userAdapter = new UserAdapter(getApplicationContext(), mUsers, true,senderorreceiver);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity__main, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        shimmerFrameLayout.startShimmer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmer();
    }
}