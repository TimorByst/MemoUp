package com.example.memoup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class Activity_OnlineGameMenu extends AppCompatActivity {

    private MaterialButton online_BTN_create;
    private MaterialButton online_BTN_join;
    private EditText online_TXT_code;
    private String code = "null";
    private MyUser player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game_menu);
        Intent previous = getIntent();
        player = (MyUser) previous.getSerializableExtra(MyUtility.PLAYER_1);
        findViews();
        initViews();

    }

    private void initViews() {
        initButton(online_BTN_create);
        initButton(online_BTN_join);
    }

    private void initButton(MaterialButton button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                code = online_TXT_code.getText().toString();
                if(!code.equalsIgnoreCase("null") && !code.equalsIgnoreCase("")){
                    player.setSessionKey(code);
                    Log.d(MyUtility.LOG_TAG, player.getUsername() + " session key is " + player.getSessionKey());
                    Intent intent;
                    if(button.getId() == online_BTN_create.getId()) {
                        player.isCreator = true;
                        intent = new Intent(Activity_OnlineGameMenu.this, Activity_GameLevel.class);
                    }else if(button.getId() == online_BTN_join.getId()){
                        player.isCreator = false;
                        intent = new Intent(Activity_OnlineGameMenu.this, Activity_Online.class);
                    }else{
                        return;
                    }
                    intent.putExtra(MyUtility.PLAYER_1, player);
                    startActivity(intent);
                }else{
                    MySignal.getInstance().frenchToast("Please enter a valid code");
                }
            }
        });
    }

    private void findViews() {
        online_TXT_code = findViewById(R.id.online_TXT_code);
        online_BTN_create = findViewById(R.id.online_BTN_create);
        online_BTN_join = findViewById(R.id.online_BTN_join);
    }
}