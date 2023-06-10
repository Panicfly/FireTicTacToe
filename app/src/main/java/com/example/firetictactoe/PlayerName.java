package com.example.firetictactoe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name);

        final EditText playerNameEt = findViewById(R.id.PlayerNameEt);
        final AppCompatButton startGameBtn = findViewById(R.id.startGameBtn);

        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //getting playerName and save it as a sting
                final String getPlayerName = playerNameEt.getText().toString();

                //Checking if playerName is empty
                if (getPlayerName.isEmpty()) {
                    Toast.makeText(PlayerName.this, "Bitte Spielernamen eingeben", Toast.LENGTH_SHORT).show();
                } else {
                    //Intent for opening MainActivity
                    Intent intent = new Intent(PlayerName.this, MainActivity.class);

                    //Adding player name
                    intent.putExtra("spielername" , getPlayerName);

                    //opening MainActivity
                    startActivity(intent);

                    //Destroy this Activity
                    finish();
                }
            }
        });
    }
}