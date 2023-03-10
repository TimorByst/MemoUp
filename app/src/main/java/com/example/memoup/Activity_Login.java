package com.example.memoup;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class Activity_Login extends AppCompatActivity {
    private AppCompatImageView game_IMG_background;
    private ShapeableImageView online_IMG;
    private FirebaseManager firebaseManager;
    private MyUser myUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyUtility.hideSystemUI(this);
        setContentView(R.layout.activity_login);
        findViews();
        initViews();
        AppCompatImageView game_IMG_background = findViewById(R.id.game_IMG_background);
        Glide.with(this).load(R.drawable.memo_up_app_background).into(game_IMG_background);
        firebaseManager = FirebaseManager.getInstance();
        FirebaseUser user = firebaseManager.getFirebaseAuth().getCurrentUser();
        if (user == null) {
            prettyLogin();
        } else {
            myUser = new MyUser(user.getUid());
            firebaseManager.loadUser(user.getUid(),
                    new FirebaseManager.OnUserLoadedListener() {
                        @Override
                        public void onUserLoaded(MyUser user) {
                            myUser.setUsername(user.getUsername())
                                    .setGamesPlayedMulti(user.getGamesPlayedMulti())
                                    .setGamesPlayedSolo(user.getGamesPlayedSolo())
                                    .setWins(user.getWins())
                                    .setBestTime(user.getBestTime())
                                    .setUserImageResource(user.getUserImageResource());
                            Log.d(MyUtility.LOG_TAG, myUser.getUsername()
                                    + " is now online " + myUser.getGamesPlayedMulti());
                            Intent intent = new Intent(Activity_Login.this,
                                    Activity_MainMenu.class);
                            intent.putExtra(MyUtility.PLAYER, myUser);
                            Log.d(MyUtility.LOG_TAG, "User loaded successfully");
                            new Handler().postDelayed(() -> {
                                startActivity(intent);
                                finish();
                            }, 1000);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e(MyUtility.LOG_TAG,
                                    "Error while trying to read user from the database "
                                            + errorMessage);
                            if (errorMessage.equalsIgnoreCase("user not found")) {
                                prettyLogin();
                            }
                        }
                    }
            );
        }
    }

    private void findViews() {
        online_IMG = findViewById(R.id.online_IMG);
        game_IMG_background = findViewById(R.id.game_IMG_background);
    }

    private void initViews() {
        Glide.with(this).load(R.drawable.memo_up_logo).into(online_IMG);
        Glide.with(this).load(R.drawable.memo_up_app_background).into(game_IMG_background);
        ObjectAnimator scaleXAnimator = ObjectAnimator
                .ofFloat(online_IMG, "scaleX", 1f, 1.2f, 1f);
        scaleXAnimator.setDuration(1750);
        scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleXAnimator.setRepeatMode(ValueAnimator.REVERSE);
        ObjectAnimator scaleYAnimator = ObjectAnimator
                .ofFloat(online_IMG, "scaleY", 1f, 1.2f, 1f);
        scaleYAnimator.setDuration(1750);
        scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
        animatorSet.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    private void prettyLogin() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build());

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
            myUser = new MyUser(user.getUid());
            showUsernameDialog();
        } else {
            prettyLogin();
        }
    }

    private void showUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter username");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            myUser.setUsername(input.getText().toString());
            firebaseManager.saveUser(myUser);
            Intent intent = new Intent(Activity_Login.this, Activity_MainMenu.class);
            intent.putExtra(MyUtility.PLAYER, myUser);
            new Handler().postDelayed(() -> {
                startActivity(intent);
                finish();
            }, 2000);
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyUtility.hideSystemUI(this);
        }
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );


}