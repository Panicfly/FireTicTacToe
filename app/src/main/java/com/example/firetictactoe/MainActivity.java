package com.example.firetictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout spieler1Layout, spieler2Layout;
    private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9;
    private TextView spieler1, spieler2;

    //Gewinn Kombinationsmöglichkeiten
    private final List<int[]> combinationsList = new ArrayList<>();
    private final List<String> doneBoxes = new ArrayList<>();

    //Player Unique ID
    private String playerUniqueId = "0";

    //Abholen der Firebase Reference von der URL
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://firetictactoe-2d5ae-default-rtdb.europe-west1.firebasedatabase.app");
    //Wird auf true gesetzt wenn ein Gegner gefunden ist
    private boolean opponentFound = false;

    //Unique ID des Gegners
    private String opponentUniqueId = "0";

    //Values müssen übereinstimmen sonst muss man warten
    private String status = "matching";

    private String playerTurn = "";

    //ConnectionId, die zeigt in welche connection ein Spieler beigetreten ist
    private String connectionId ="";

    //Generierung von Valueevent Listeners für die Firebase Datenbank
    ValueEventListener turnsEventListener, wonEventListener;

    //Ausgewählte Box des Spielers. Leere Felder werden mit den SpielerIDs gefüllt
    private final String[] boxSelectedBy = {"", "", "", "", "", "", "", "", ""};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spieler1Layout = findViewById(R.id.spieler1Layout);
        spieler2Layout = findViewById(R.id.spieler2Layout);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        image5 = findViewById(R.id.image5);
        image6 = findViewById(R.id.image6);
        image7 = findViewById(R.id.image7);
        image8 = findViewById(R.id.image8);
        image9 = findViewById(R.id.image9);

        spieler1 = findViewById(R.id.spieler1);
        spieler2 = findViewById(R.id.spieler2);

        //Spielername aus der PlayerName Klass nehmen
        final String getPlayerName = getIntent().getStringExtra("spielername");


        //Generierung der Gewinn Kombinationen
        combinationsList.add(new int[]{0, 1, 2});
        combinationsList.add(new int[]{3, 4, 5});
        combinationsList.add(new int[]{6, 7, 8});
        combinationsList.add(new int[]{0, 3, 6});
        combinationsList.add(new int[]{1, 4, 7});
        combinationsList.add(new int[]{2, 5, 8});
        combinationsList.add(new int[]{2, 4, 6});
        combinationsList.add(new int[]{0, 4, 8});

        //Spielerdialog während man auf den Gegner wartet
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Warten auf anderen Spieler");
        progressDialog.show();

        //Generierung der Spieler Unique ID
        playerUniqueId = String.valueOf(System.currentTimeMillis());

        //Setting Player Name to the Text View
        spieler1.setText(getPlayerName);

        databaseReference.child("connections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Überprüft ob der Gegner gefunden wurde oder nicht. Falls nicht wird gesucht.
                if(opponentFound)
                    //Es wird überprüft ob jemand in der Firebase Datenbannk ist
                    if(snapshot.hasChildren()){

                        //Schaut ob andere Spieler auf ein Spiel warten zum Spielen
                        for(DataSnapshot connections : snapshot.getChildren()) {
                            //Bekomm die einzigartige Verbindungs ID
                            String conId = connections.getKey();

                            //Es benötigt 2 Spieler zum spielen
                            //Bei einem Count von 1 wartet 1 Spieler auf ein Match
                            //Bei einem Count von 2 ist ein Match erstellt worden
                            int getPlayersCount = (int) connections.getChildrenCount();

                            //Warten auf andere Spieler
                            if (status.equals("waiting")) {
                                //Bei einem Count von 2 ist ein Match erstellt worden
                                if (getPlayersCount == 2) {

                                    playerTurn = playerUniqueId;
                                    applyPlayerTurn(playerTurn);

                                    //True wenn ein Spieler in der Connection gefunden wurde
                                    boolean playerFound = false;

                                    //Spieler der Connection hinzufügen
                                    for (DataSnapshot players : connections.getChildren()) {

                                        String getPlayerUniqueId = players.getKey();
                                        //Schaut ob die UserId dem des Erstellers entspricht
                                        if (getPlayerUniqueId.equals(playerUniqueId)) {
                                            playerFound = true;
                                        } else if (playerFound) {
                                            String getOpponentPlayerName = players.child("spielername").getValue(String.class);
                                            opponentUniqueId = players.getKey();

                                            //Gegnerischen Spielernamen auf der TextView anzeigen
                                            spieler2.setText(getOpponentPlayerName);

                                            connectionId = conId;
                                            opponentFound = true;

                                            //verbindet die eventlisteners für won und turns zu den Datenbank Referencen
                                            databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                            databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                            if (progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }

                                            databaseReference.child("connections").removeEventListener(this);
                                        }
                                    }
                                }//Dieser Fall tritt auf wenn der User keinen Raum erzeugt, da schon einer zum Joinen vorhanden ist
                            } else {
                                    if (getPlayersCount == 1) {

                                        connections.child(playerUniqueId).child("spielername").getRef().setValue(getPlayerName);


                                        for(DataSnapshot players : connections.getChildren()) {

                                            String getOpponentName = players.child("spielername").getValue(String.class);
                                            opponentUniqueId = players.getKey();
                                            //Spieler, der den Raum erstellt hat, startet
                                            playerTurn = opponentUniqueId;

                                            applyPlayerTurn(playerTurn);

                                            spieler2.setText(getOpponentName);

                                            connectionId = conId;
                                            opponentFound = true;

                                            //verbindet die eventlisteners für won und turns zu den Datenbank Referencen
                                            databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                            databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                            if (progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }

                                            databaseReference.child("connections").removeEventListener(this);

                                            break;
                                    }
                                }
                            }
                        }
                        //Überprüft ob ein Gegner nicht gefunden wurde und ob der Benutzer nicht mehr auf einen Gegner wartet dann wird eine neue Connection erstellt.
                        if(!opponentFound && status.equals("waiting")){
                            //Generierung der Unique ID der Verbindung
                            String connectionUniqueId = String.valueOf(System.currentTimeMillis());
                            //Füge ersten Spieler zur Verbindung hinzu.
                            snapshot.child(connectionUniqueId).child(playerUniqueId).child("spielername").getRef().setValue(getPlayerName);

                            status = "waiting";
                        }

                        //Wenn keine connection besteht erstell eine Neue in der Firebase Datenbank
                        //Also erstellen wir einen Raum um auf einen Spieler zu warten
                    } else {

                        //Generierung der Unique ID der Verbindung
                        String connectionUniqueId = String.valueOf(System.currentTimeMillis());
                        //Füge ersten Spieler zur Verbindung hinzu.
                        snapshot.child(connectionUniqueId).child(playerUniqueId).child("spielername").getRef().setValue(getPlayerName);

                        status = "waiting";
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        turnsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Bekommt alle Züge der Connection
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if(dataSnapshot.getChildrenCount() == 2) {

                        //Ausgewählte Boxposition des Users
                        final int getBoxPosition = Integer.parseInt(dataSnapshot.child("box position").getValue(String.class));
                        final String getPlayerId = dataSnapshot.child("player_id").getValue(String.class);

                        //Schaut ob der User die Box vorher ausgewählt hat
                        if(doneBoxes.contains(String.valueOf(getBoxPosition))) {

                            doneBoxes.add(String.valueOf(getBoxPosition));

                            if(getBoxPosition == 1) {
                                selectBox(image1, getBoxPosition, getPlayerId);
                            } else if (getBoxPosition == 2) {
                                selectBox(image2, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 3) {
                                selectBox(image3, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 4) {
                                selectBox(image4, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 5) {
                                selectBox(image5, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 6) {
                                selectBox(image6, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 7) {
                                selectBox(image7, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 8) {
                                selectBox(image8, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 9) {
                                selectBox(image9, getBoxPosition, getPlayerId);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        wonEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Überprüft ob ein Spieler gewonnen hat
                if(snapshot.hasChild("player_id")) {
                    String getWinPlayerId = snapshot.child("player_id").getValue(String.class);

                    final WinDialog winDialog;

                    if(getWinPlayerId.equals(playerUniqueId)) {
                        //Zeigt den Win Dialog
                        winDialog = new WinDialog(MainActivity.this, "Du hast gewonnen.");
                    } else {
                        winDialog = new WinDialog(MainActivity.this, "Dein Gegner hat gewonnen.");
                    }

                    winDialog.setCancelable(false);
                    winDialog.show();

                    databaseReference.child("turns").child(connectionId).removeEventListener(turnsEventListener);
                    databaseReference.child("won").child(connectionId).removeEventListener(wonEventListener);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        image1.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                //Überprüft ob eine Box ausgewählt ist
                if(!doneBoxes.contains("1") && playerTurn.equals(playerUniqueId)) {

                }
            }
        });
    }

    private void applyPlayerTurn(String playerUniqueId2) {
        if(playerUniqueId2.equals(playerUniqueId)) {
            spieler1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            spieler2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_20);
        } else {
            spieler2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            spieler1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_20);
        }
    }

    private void selectBox(ImageView imageView, int selectedBoxPosition, String selectedByPlayer) {

        boxSelectedBy[selectedBoxPosition - 1] = selectedByPlayer;

        if(selectedByPlayer.equals(playerUniqueId)){
            imageView.setImageResource(R.drawable.tictactoe_x);
            playerTurn = opponentUniqueId;
        } else {
            imageView.setImageResource(R.drawable.tictactoe_o);
            playerTurn = playerUniqueId;
        }

        applyPlayerTurn(playerTurn);

        //Schaut ob der Spieler das Spiel gewonnen hat
        if(checkPlayerWin(selectedByPlayer)) {
            //Schickt die ID des Gewinners an die Datenbank
            databaseReference.child("won").child(connectionId).child("player_id").setValue(selectedByPlayer);
        }
        //Das Spiel endet nach 9 zügen
        if(doneBoxes.size() == 9){
            final WinDialog winDialog = new WinDialog(MainActivity.this, "Unentschieden");
            winDialog.setCancelable(false);
            winDialog.show();
        }
    }

    private boolean checkPlayerWin(String playerId) {

        boolean isPlayerWon = false;

        //Vergleicht spieler züge mit möglichen Gewinnmöglichkeiten
        for(int i = 0; i < combinationsList.size(); i++) {

            final int[] combination = combinationsList.get(i);

            if (boxSelectedBy[combination[0]].equals(playerId) &&
                    boxSelectedBy[combination[1]].equals(playerId) &&
                    boxSelectedBy[combination[2]].equals(playerId)) {
                isPlayerWon = true;
            }
        }

        return isPlayerWon;
    }
}