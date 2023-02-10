package com.example.memoup;


import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;

class MyAnimationListener implements Animation.AnimationListener {

    private final Animation nextAnimation;
    private final View nextView;
    private final Intent intent;
    private final Activity activity;

    public MyAnimationListener(Animation nextAnimation,
                               View nextView,
                               Intent intent,
                               Activity activity) {
        this.nextAnimation = nextAnimation;
        this.nextView = nextView;
        this.intent = intent;
        this.activity = activity;
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(nextAnimation != null){
            if(nextView.getVisibility() == View.INVISIBLE){
                nextView.setVisibility(View.VISIBLE);
            }
            nextView.startAnimation(nextAnimation);
        }
        if (intent != null) {
            activity.startActivity(intent);
            activity.finish();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}
