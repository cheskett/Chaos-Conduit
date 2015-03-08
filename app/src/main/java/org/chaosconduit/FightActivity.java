package org.chaosconduit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;
import java.util.Random;


public class FightActivity extends ActionBarActivity {

    int permission = 1;     // 1 is permission allowed, 0 is not allowed, ie enemy turn.
    TextView enemyHealth;
    TextView selfMana1, selfMana2, selfMana3;
    Firebase firebase, gamesRef;
    String player, enemy;
    String gameID;
    Map<String,Object> player1Map, player2Map;

    public void setPlayer1Map(Map<String,Object> map){
        Log.w("MAP TEST", "PLAYER 1");
        player1Map = map;
    }

    public void setPlayer2Map(Map<String,Object> map){
        Log.w("MAP TEST", "PLAYER 2");
        player2Map = map;
    }

    public String getGameID(){
        return gameID;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fight);
        final Button selfAttack = (Button) findViewById(R.id.buttonAttack);
        Button enemyPass = (Button) findViewById(R.id.enemyButtonPass);

        ImageButton spell_111 = (ImageButton) findViewById(R.id.spellFlareButton);   //Flare
        ImageButton spell_112 = (ImageButton) findViewById(R.id.spellExploButton);     //Explo
        ImageButton spell_122 = (ImageButton) findViewById(R.id.spellSunburstButton);   //Sunburst
        ImageButton spell_222 = (ImageButton) findViewById(R.id.spellBoltButton);   //Bolt
        ImageButton spell_223 = (ImageButton) findViewById(R.id.spellTransformerButton);     //Transformer
        ImageButton spell_233 = (ImageButton) findViewById(R.id.spellPulseButton);   //Pulse
        ImageButton spell_333 = (ImageButton) findViewById(R.id.spellEnlightenButton);   //Enlighten
        ImageButton spell_133 = (ImageButton) findViewById(R.id.spellExtractButton);   //Extract
        ImageButton spell_113 = (ImageButton) findViewById(R.id.spellManacombustButton);   //Manacombust
        ImageButton spell_123 = (ImageButton) findViewById(R.id.spellOvertapButton);   //Overtap
        final ImageButton mainSpell = (ImageButton) findViewById(R.id.highlightSpellButton);   //Overtap
        final TextView mainSpellName = (TextView) findViewById(R.id.highlightSpellName);
        final TextView mainSpellDesc = (TextView) findViewById(R.id.highlightSpellDesc);

        enemyHealth = (TextView) findViewById(R.id.enemyHP);
        enemyHealth.setText(Integer.toString(60));
        selfMana1 = (TextView) findViewById(R.id.selfMana1);
        selfMana2 = (TextView) findViewById(R.id.selfMana2);
        selfMana3 = (TextView) findViewById(R.id.selfMana3);
        selfMana1.setText(Integer.toString(0));
        selfMana2.setText(Integer.toString(0));
        selfMana3.setText(Integer.toString(0));
        final String ID = getIntent().getStringExtra("ID");
        gameID = ID;
        Log.w("GAME ID in FIGHT", gameID);



        ImageView selfFace = (ImageView) findViewById(R.id.selfFace);
        ImageView enemyFace = (ImageView) findViewById(R.id.enemyFace);

        firebase = new Firebase(getResources().getString(R.string.firebase));
        gamesRef = firebase.child("games");


        player = getIntent().getStringExtra("Player");
        if (player.equals("1")) {
            enemy = "2";
            enemyFace.setImageResource(R.drawable.invoker_right2);
            selfFace.setImageResource(R.drawable.invoker_left);
        }
        else {
            enemy = "1";
            enemyFace.setImageResource(R.drawable.invoker_right);
            selfFace.setImageResource(R.drawable.invoker_left2);
        }

        //Log.w("Test MAPPING", player1Map.toString());
        //Log.w("Test MAPPING", player2Map.get("games").toString());

        gamesRef.child(ID).child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals(player)) {
                    //YOUR NEW TURN STARTS!
                    //updatePlayerMapsFromDB();
                    permission = 1;
                    selfAttack.setEnabled(true);
                    //alert player it's his turn
                    Toast.makeText(getBaseContext(), "It's your turn.", Toast.LENGTH_LONG).show();
                    int mana1 = Integer.parseInt(selfMana1.getText().toString());
                    //Map<String,Object> mana =(Map<String,Object>) player1Map.get("manaAmt");

                    //int mana1 = Integer.parseInt(mana.get("0").toString());
                    int mana2 = Integer.parseInt(selfMana2.getText().toString());
                    int mana3 = Integer.parseInt(selfMana3.getText().toString());
                    //start Roll mana
                    Random randMana = new Random();
                    for (int i = 0; i < 3; i++) {
                        int rolledMana = randMana.nextInt(3) + 1;
                        switch (rolledMana) {
                            case 1:
                                mana1++;
                                break;
                            case 2:
                                mana2++;
                                break;
                            case 3:
                                mana3++;
                                break;
                        }
                    }
                    selfMana1.setText(Integer.toString(mana1));
                    selfMana2.setText(Integer.toString(mana2));
                    selfMana3.setText(Integer.toString(mana3));
                    if (mana1 + mana2 + mana3 > 5) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
                        builder.setMessage("You have too much mana!");
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        selfAttack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission == 1) {
                    Map<String,Object> map;
                    if(player.equals("2")){
                        map = player1Map;
                    }else{
                        map = player2Map;
                    }


                    int damage = 3;
                    int currentHP = (int) map.get("health");//Integer.parseInt(enemyHealth.getText().toString());
                    int finalHP = currentHP - damage;
                    map.put("health",finalHP);

                    pushPlayerMapstoDB();
                    //String send = Integer.toString(finalHP);
                    enemyHealth.setText(Integer.toString(finalHP));
                    gamesRef.child(ID).child("turn").setValue(enemy);
                    selfAttack.setEnabled(false);
                    permission = 0;
                }
            }
        });

        spell_111.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpell.setImageResource(R.drawable.s02_flare);
                mainSpellName.setText("Flare");
                mainSpellDesc.setText("Deal 6(R8)(R10) damage.  There is a 33% chance that the same amplification of Flare will be cast again for free.");
                if (permission == 1) {
                    //set active spell to main
                    Toast.makeText(getBaseContext(), "Trying to cast Flare.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        spell_112.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpell.setImageResource(R.drawable.s09_explo);
                mainSpellName.setText("Explosion");
                mainSpellDesc.setText("Deal 15(R20)(R25) damage to your opponent and 10(Y9)(Y7) damage to yourself.");
                if (permission == 1){
                    //set active spell to main
                    Toast.makeText(getBaseContext(), "Trying to cast Explosion.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*enemyPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permission = 1;
                int mana1 = Integer.parseInt(selfMana1.getText().toString());
                int mana2 = Integer.parseInt(selfMana2.getText().toString());
                int mana3 = Integer.parseInt(selfMana3.getText().toString());
                //start Roll mana
                Random randMana = new Random();
                for (int i = 0; i < 3; i++) {
                    int rolledMana = randMana.nextInt(3) + 1;
                    switch (rolledMana) {
                        case 1:
                            mana1++;
                            break;
                        case 2:
                            mana2++;
                            break;
                        case 3:
                            mana3++;
                            break;
                    }
                }
                selfMana1.setText(Integer.toString(mana1));
                selfMana2.setText(Integer.toString(mana2));
                selfMana3.setText(Integer.toString(mana3));
                if (mana1 + mana2 + mana3 > 5) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
                    builder.setMessage("You have too much mana!");
                }
            }
        });*/

        updatePlayerMapsFromDB();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fight, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updatePlayerMapsFromDB(){
        final Firebase gamesRef = firebase.child("games").child(gameID);
        gamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
               try {
                   Map<String, Object> map = (Map<String, Object>) snapshot.child("player1").getValue();
                   Map<String, Object> map2 = (Map<String, Object>) snapshot.child("player2").getValue();
                   setPlayer1Map(map);
                   setPlayer2Map(map2);
               }catch(Exception e){
                   Log.w("UPDATE MAPS","TRY AGAIN!");
                   updatePlayerMapsFromDB();
               }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void pushPlayerMapstoDB(){
        final Firebase gamesRef = firebase.child("games").child(gameID);
        gamesRef.child("player1").setValue(player1Map);
        gamesRef.child("player2").setValue(player2Map);
    }

}
