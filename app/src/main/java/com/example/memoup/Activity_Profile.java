package com.example.memoup;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class Activity_Profile extends AppCompatActivity {

    private TextView profile_TXT_solo;
    private TextView profile_TXT_multi;
    private TextView profile_TXT_win;
    private TextView profile_TXT_winRate;
    private TextView profile_TXT_username;
    private ImageView profile_IMG_user;
    private MyUser player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        MyUtility.hideSystemUI(this);
        Intent serviceIntent = new Intent(this, MyMusicService.class);
        startService(serviceIntent);
        Intent previous = getIntent();
        player = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER);
        FirebaseManager.getInstance().loadUser(
                player.getId(), new FirebaseManager.OnUserLoadedListener() {
                    @Override
                    public void onUserLoaded(MyUser user) {
                        player.setGamesPlayedMulti(user.getGamesPlayedMulti())
                                .setGamesPlayedSolo(user.getGamesPlayedSolo())
                                .setWins(user.getWins())
                                .setBestTime(user.getBestTime());
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.d(MyUtility.LOG_TAG,
                                "Error occurred while tying to load user. " + errorMessage);
                    }
                });
        findViews();
        initViews();
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
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(this, Activity_MainMenu.class);
        intent.putExtra(MyUtility.PLAYER, player);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    private void initViews() {
        String winRate = "0%";
        if (player.getWins() != 0 && player.getGamesPlayedMulti() != 0) {
            float wins = player.getWins();
            float total = player.getGamesPlayedMulti();
            winRate = "win rate: " + String.format("%.1f", wins / total * 100) + "%";
        }
        profile_TXT_solo.setText("Best time: " + player.getBestTime());
        profile_TXT_multi.setText("Games: " + player.getGamesPlayedMulti());
        profile_TXT_win.setText("Wins: " + player.getWins() + "");
        profile_TXT_winRate.setText(winRate);
        profile_TXT_username.setText(player.getUsername() + "'s profile");

        if (player.getUserImageResource() == 0) {
            Glide.with(this).load(R.drawable.default_avatar).into(profile_IMG_user);
        } else {
            profile_IMG_user.setImageResource(player.getUserImageResource());
        }

        profile_IMG_user.setOnClickListener(view -> showAvatarDialog());
    }

    private void findViews() {
        profile_TXT_solo = findViewById(R.id.profile_TXT_solo);
        profile_TXT_multi = findViewById(R.id.profile_TXT_multi);
        profile_TXT_win = findViewById(R.id.profile_TXT_win);
        profile_TXT_winRate = findViewById(R.id.profile_TXT_winRate);
        profile_IMG_user = findViewById(R.id.profile_IMG_user);
        profile_TXT_username = findViewById(R.id.profile_TXT_username);
    }

    private void showAvatarDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.avatar_dialog_layout);

        RecyclerView recyclerView = dialog.findViewById(R.id.avatar_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        AvatarAdapter adapter = new AvatarAdapter(this);
        adapter.setListener(avatarResourceId -> {
            updateAvatar(avatarResourceId);
            dialog.dismiss();
        });
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    private void updateAvatar(int avatarResourceId) {
        profile_IMG_user.setImageResource(avatarResourceId);
        player.setUserImageResource(avatarResourceId);
        FirebaseManager.getInstance().saveUser(player);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            MyUtility.hideSystemUI(this);
        }
    }
    public interface OnAvatarSelectedListener {
        void onAvatarSelected(int avatarResourceId);
    }

}