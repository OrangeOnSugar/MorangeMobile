package com.example.morange;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.morange.Fragments.ChatFragment;
import com.example.morange.Fragments.ProfileFragment;
import com.example.morange.Fragments.UsersFragment;
import com.example.morange.ModeJS.UserINFO;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView pro_i;
    TextView login_s;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbarD = findViewById(R.id.toolbar);
        setSupportActionBar(toolbarD);
        getSupportActionBar().setTitle("");

        pro_i = findViewById(R.id.profile_image);
        login_s = findViewById(R.id.profile_name);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserINFO userINFO = dataSnapshot.getValue(UserINFO.class);
                login_s.setText(userINFO.getLogin());
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

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager pager = findViewById(R.id.view_pager);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        pagerAdapter.addFragment(new ChatFragment(),"Диалоги");
        pagerAdapter.addFragment(new UsersFragment(),"Друзья");
        pagerAdapter.addFragment(new ProfileFragment(),"Профиль");

        pager.setAdapter(pagerAdapter);

        tabLayout.setupWithViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_r,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this,AuthorizaActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;

            case R.id.addfriends:
                startActivity(new Intent(MainActivity.this,AddFriends_Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
        }
        return false;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm)
        {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }


        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        public void addFragment(Fragment fragment, String title)
        {
            fragments.add(fragment);
            titles.add(title);
        }
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
