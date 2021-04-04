package com.example.morange;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.morange.Adapter.UserAdapter;
import com.example.morange.ModeJS.Chat;
import com.example.morange.ModeJS.UserINFO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Main extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<UserINFO> mUsers;

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
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.addfriends)
                {
                    startActivity(new Intent(Activity_Main.this,AddFriends_Activity.class));
                }
                else if (menuItem.getItemId() == R.id.settings)
                {
                    startActivity(new Intent(Activity_Main.this,settings.class));
                }
                else if (menuItem.getItemId() == R.id.exit)
                {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(Activity_Main.this,AuthorizaActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
                return false;
            }
        });
        View header = navigationView.getHeaderView(0);
        pro_i = header.findViewById(R.id.userLogo);
        pro_i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_Main.this, profile.class);
                intent.putExtra("UserID",firebaseUser.getUid());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Activity_Main.this.startActivity(intent);
            }
        });
        login_s = header.findViewById(R.id.userLogin);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserINFO userINFO = dataSnapshot.getValue(UserINFO.class);
                login_s.setText(userINFO.getUsername());
                if (userINFO.getImageURL().equals("default"))
                {
                    pro_i.setImageResource(R.drawable.userbackds);
                }
                else
                {
                    Glide.with(getApplicationContext()).load(userINFO.getImageURL()).into(pro_i);
                }
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
                        //Chat chat = snapshot.getValue(Chat.class);

                        if (snapshot.getKey().contains("|"+firebaseUser.getUid()))
                        {
                            userList.add(snapshot.getKey().replace("|"+firebaseUser.getUid(),""));
                            senderorreceiver = true;
                        }
                        else if(snapshot.getKey().contains(firebaseUser.getUid()+"|"))
                        {
                            userList.add(snapshot.getKey().replace(firebaseUser.getUid()+"|",""));
                            senderorreceiver = false;
                        }
                        /*if (chat.getSender().equals(firebaseUser.getUid())) {
                            userList.add(chat.getReceiver());
                        }

                        if (chat.getReceiver().equals(firebaseUser.getUid())) {
                            userList.add(chat.getSender());
                        }*/

                        readChats();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readChats()
    {
        mUsers = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserINFO user =  snapshot.getValue(UserINFO.class);

                    for (String id : userList)
                    {
                        assert user != null;
                        if( user.getId().equals(id) && !mUsers.contains(user))
                        {
                            mUsers.add (user);
                        }
                    }
                }

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity__main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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