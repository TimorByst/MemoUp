package com.example.memoup;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

public class MyUtility {

    public static final String PLAYER = "PLAYER";
    public static final String LOG_TAG = "MEMO_UP";
    public static final String GAME_SESSIONS = "GAME_SESSIONS";
    public static final String GAMES = "GAMES";
    public static final String USERS = "USERS";
    public static final String PLAYER_MOVE = "PlayerMove";
    public static final String SINGLE_PLAYER = "SINGLE_PLAYER";
    public static final String BOARD_SIZE = "BOARD_SIZE";

    public static void hideSystemUI(Activity activity) {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        // Dim the Status and Navigation Bars
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE);

        // Without - cut out display
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.getWindow().
                    getAttributes()
                    .layoutInDisplayCutoutMode
                    = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }
}
