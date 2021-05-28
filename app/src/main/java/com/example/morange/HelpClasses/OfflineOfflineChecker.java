package com.example.morange.HelpClasses;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class OfflineOfflineChecker {

    public static void status(FirebaseUser firebaseUser) {

        final DatabaseReference connectionReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("connections");
        final DatabaseReference lastconnected = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        final DatabaseReference infoconnected = FirebaseDatabase.getInstance().getReference(".info/connected");

        infoconnected.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);

                if(connected){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("lastseen", ServerValue.TIMESTAMP);
                    DatabaseReference referenceConnection = connectionReference.push();
                    referenceConnection.onDisconnect().removeValue();
                    referenceConnection.setValue(true);
                    lastconnected.onDisconnect().updateChildren(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
