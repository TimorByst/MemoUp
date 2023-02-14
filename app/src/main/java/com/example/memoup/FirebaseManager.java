package com.example.memoup;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;


public class FirebaseManager {

    interface OnUserLoadedListener {

        void onUserLoaded(MyUser user);

        void onError(String errorMessage);

    }
    private static FirebaseManager firebaseManager = null;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseAuth firebaseAuth;
    private final Gson gson = new Gson();
    private DatabaseReference databaseReference;

    private FirebaseManager() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static void init() {
        if (firebaseManager == null) {
            firebaseManager = new FirebaseManager();
        }
    }

    public static FirebaseManager getInstance() {
        return firebaseManager;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public void saveUser(MyUser user) {
        String json = gson.toJson(user);
        databaseReference = firebaseDatabase.getReference(MyUtility.USERS);
        databaseReference.child(user.getId()).setValue(json);
    }

    public void loadUser(String userId, final OnUserLoadedListener listener) {
        databaseReference = firebaseDatabase.getReference(MyUtility.USERS);
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userJson = dataSnapshot.getValue(String.class);
                    MyUser user = new Gson().fromJson(userJson, MyUser.class);
                    listener.onUserLoaded(user);
                } else {
                    listener.onError("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError.getMessage());
            }
        });
    }
}
