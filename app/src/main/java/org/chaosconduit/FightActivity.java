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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class FightActivity extends ActionBarActivity {

    boolean start = true;
    int permission;     // 1 is permission allowed, 0 is not allowed, ie enemy turn.
    TextView enemyHealth, selfHealth;
    TextView selfMana1, selfMana2, selfMana3, enemyActivity;
    Firebase firebase, gamesRef;
    String player, enemy;
    String mainSpell = "000";
    String gameID;
    String lastSpellCast = "";
    Map<String,Object> player1Map, player2Map;

    public void setManaValues(String player){
        ArrayList<Long> list;
        if (player.equals("1")){
            list = (ArrayList) player1Map.get("manaAmt");
        }else{
            list = (ArrayList) player1Map.get("manaAmt");
        }
        selfMana1.setText(list.get(0).toString());
        selfMana2.setText(list.get(1).toString());
        selfMana3.setText(list.get(2).toString());
    }

    public static int safeLongToInt(long l) {
        return (int) Math.max(Math.min(Integer.MAX_VALUE, l), Integer.MIN_VALUE);
    }

    public void checkVictory(String player){
        int myHealth, enemyHealth;
        int h1 = 0, h2 = 0;

        if (player1Map.get("health").getClass().equals(Integer.class)){
            h1 = (int) player1Map.get("health");
            h2 = (int) player2Map.get("health");
        }else if (player1Map.get("health").getClass().equals(Long.class)){
            h1 = Integer.parseInt(player1Map.get("health").toString());
            h2 = Integer.parseInt(player2Map.get("health").toString());
        }


        if (player.equals("1")){
            myHealth = h1;
            enemyHealth = h2;
        } else{
            myHealth =  h2;
            enemyHealth = h1;
        }

        if (myHealth <= 0){
            String message = "YOU LOSE!";
            AlertDialog.Builder adb = new AlertDialog.Builder(FightActivity.this);
            adb.setTitle("Game Over");
            adb.setMessage(message);
            adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, close
                    // current activity
                    Intent intent = new Intent(getBaseContext(),MainActivity.class);
                    startActivity(intent);
                    FightActivity.this.finish();
                }
            });

            adb.show();

         }

        else if (enemyHealth <= 0){
            String message = "YOU WIN!";
            AlertDialog.Builder adb = new AlertDialog.Builder(FightActivity.this);
            adb.setTitle("Game Over");
            adb.setMessage(message);
            adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, close
                    // current activity
                    Intent intent = new Intent(getBaseContext(),MainActivity.class);
                    startActivity(intent);
                    FightActivity.this.finish();
                }

            });

            adb.show();

        }
    }

    public void setPlayer1Map(Map<String,Object> map){
        Log.w("MAP TEST", "PLAYER 1");
        player1Map = map;
        if (player != null){
            if(player.equals("1")){
                ArrayList<Long> list = (ArrayList<Long>) map.get("manaAmt");
                selfMana1.setText(list.get(0).toString());
                selfMana2.setText(list.get(1).toString());
                selfMana3.setText(list.get(2).toString());
                selfHealth.setText(player1Map.get("health").toString());
                enemyHealth.setText(player2Map.get("health").toString());
                //checkVictory(("1"));
            }
        }
    }

    public void setPlayer2Map(Map<String,Object> map){
        Log.w("MAP TEST", "PLAYER 2");
        player2Map = map;
        if (player != null){
            if(player.equals("2")){
                ArrayList<Long> list = (ArrayList<Long>) map.get("manaAmt");
                selfMana1.setText(list.get(0).toString());
                selfMana2.setText(list.get(1).toString());
                selfMana3.setText(list.get(2).toString());
                enemyHealth.setText(player1Map.get("health").toString());
                selfHealth.setText(player2Map.get("health").toString());
                //checkVictory(("2"));
            }
        }
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

        final ImageButton spell_111 = (ImageButton) findViewById(R.id.spellFlareButton);   //Flare
        final ImageButton spell_112 = (ImageButton) findViewById(R.id.spellExploButton);     //Explo
        final ImageButton spell_122 = (ImageButton) findViewById(R.id.spellSunburstButton);   //Sunburst
        final ImageButton spell_222 = (ImageButton) findViewById(R.id.spellBoltButton);   //Bolt
        final ImageButton spell_223 = (ImageButton) findViewById(R.id.spellTransformerButton);     //Transformer
        final ImageButton spell_233 = (ImageButton) findViewById(R.id.spellPulseButton);   //Pulse
        final ImageButton spell_333 = (ImageButton) findViewById(R.id.spellEnlightenButton);   //Enlighten
        final ImageButton spell_133 = (ImageButton) findViewById(R.id.spellExtractButton);   //Extract
        final ImageButton spell_113 = (ImageButton) findViewById(R.id.spellManacombustButton);   //Manacombust
        final ImageButton spell_123 = (ImageButton) findViewById(R.id.spellOvertapButton);   //Overtap
        final ImageButton mainSpellReady = (ImageButton) findViewById(R.id.highlightSpellButton);
        final TextView mainSpellName = (TextView) findViewById(R.id.highlightSpellName);
        final TextView mainSpellDesc = (TextView) findViewById(R.id.highlightSpellDesc);

        enemyActivity = (TextView) findViewById(R.id.enemyActivity);

        final ImageView manaCost1 = (ImageView) findViewById(R.id.manaCost1);
        final ImageView manaCost2 = (ImageView) findViewById(R.id.manaCost2);
        final ImageView manaCost3 = (ImageView) findViewById(R.id.manaCost3);
        final ImageView manaAmp1 = (ImageView) findViewById(R.id.manaAmp1);
        final ImageView manaAmp2 = (ImageView) findViewById(R.id.manaAmp2);
        final ImageView manaAmp3 = (ImageView) findViewById(R.id.manaAmp3);

        Map<String,Object> map = new HashMap<>();
        ArrayList<Long> manaCnt = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            manaCnt.add((long) 0);
        }
        map.put("manaAmt", manaCnt);
        map.put("health", 60);
        map.put("turn", 1);

        setPlayer1Map(map);
        setPlayer2Map(map);

        enemyHealth = (TextView) findViewById(R.id.enemyHP);
        enemyHealth.setText(Integer.toString(60));
        selfHealth = (TextView) findViewById(R.id.selfHP);
        selfHealth.setText(Integer.toString(60));
        selfMana1 = (TextView) findViewById(R.id.selfMana1);
        selfMana2 = (TextView) findViewById(R.id.selfMana2);
        selfMana3 = (TextView) findViewById(R.id.selfMana3);
        selfMana1.setText(Integer.toString(0));
        selfMana2.setText(Integer.toString(0));
        selfMana3.setText(Integer.toString(0));
        final String ID = getIntent().getStringExtra("ID");
        gameID = ID;
        Log.w("GAME ID in FIGHT", gameID);

        long mana1, mana2, mana3;



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
                    permission = 1;
                    selfAttack.setEnabled(true);

                    Map<String, Object> map;

                    //alert player it's his turn
//                    Map<String, Object> map;
//                    if (player.equals("1")) {
//                        map = player1Map;
//                    } else {
//                        map = player2Map;
//                    }
//                    int currentHP = Integer.parseInt(map.get("health").toString());
//                    selfHealth.setText(Integer.toString(currentHP));

                    Toast.makeText(getBaseContext(), "It's your turn.", Toast.LENGTH_SHORT).show();

                   // if(start == false) {

                        if (player.equals("1")) {
                            map = player1Map;
                        } else {
                            map = player2Map;
                        }
                        int currentHP = Integer.parseInt(map.get("health").toString());
                        selfHealth.setText(Integer.toString(currentHP));

                        ArrayList<Long> mana = (ArrayList<Long>) map.get("manaAmt");
                        long mana1 = mana.get(0);
                        long mana2 = mana.get(1);
                        long mana3 = mana.get(2);

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
                        //add Mana to map

                        selfMana1.setText(Long.toString(mana1));
                        selfMana2.setText(Long.toString(mana2));
                        selfMana3.setText(Long.toString(mana3));

                        mana.set(0, mana1);
                        mana.set(1, mana2);
                        mana.set(2, mana3);
                        map.put("manaAmt", mana);
                        if(player.equals("1")){
                            setPlayer1Map(map);
                        }else{
                            setPlayer2Map(map);
                        }

                        pushPlayerMapstoDB();

                        if (mana1 >= 3){
                            spell_111.setEnabled(true);
                            spell_111.setImageResource(R.drawable.s02_flare_small);
                        }
                        else {
                            spell_111.setEnabled(false);
                            spell_111.setImageResource(R.drawable.s02_flare_dim);
                        }
                        if ((mana1 >= 2) && (mana2 >= 1)){
                            spell_112.setEnabled(true);
                            spell_112.setImageResource(R.drawable.s09_explo_small);
                        }
                        else {
                            spell_112.setEnabled(false);
                            spell_112.setImageResource(R.drawable.s09_explo_dim);
                        }

                        if ((mana1 >= 1) && (mana2 >= 2)){
                            spell_122.setEnabled(true);
                            spell_122.setImageResource(R.drawable.s10_suburst_small);
                        }
                        else {
                            spell_122.setEnabled(false);
                            spell_122.setImageResource(R.drawable.s10_suburst_dim);
                        }

                        if (mana2 >= 3){
                            spell_222.setEnabled(true);
                            spell_222.setImageResource(R.drawable.s03_bolt_small);
                        }
                        else {
                            spell_222.setEnabled(false);
                            spell_222.setImageResource(R.drawable.s03_bolt_dim);
                        }
                        if ((mana2 >= 2) && (mana3 >= 1)){
                            spell_223.setEnabled(true);
                            spell_223.setImageResource(R.drawable.s27_transformer_small);
                        }
                        else {
                            spell_223.setEnabled(false);
                            spell_223.setImageResource(R.drawable.s27_transformer_dim);
                        }
                        if ((mana2 >= 1) && (mana3 >= 2)){
                            spell_233.setEnabled(true);
                            spell_233.setImageResource(R.drawable.s28_pulse_small);
                        }
                        else {
                            spell_233.setEnabled(false);
                            spell_233.setImageResource(R.drawable.s28_pulse_dim);
                        }
                        if (mana3 >= 3){
                            spell_333.setEnabled(true);
                            spell_333.setImageResource(R.drawable.s05_enlighten_small);
                        }
                        else {
                            spell_333.setEnabled(false);
                            spell_333.setImageResource(R.drawable.s05_enlighten_dim);
                        }
                        if ((mana1 >= 2) && (mana3 >= 1)){
                            spell_113.setEnabled(true);
                            spell_113.setImageResource(R.drawable.s13_manacombust_small);
                        }
                        else {
                            spell_113.setEnabled(false);
                            spell_113.setImageResource(R.drawable.s13_manacombust_dim);
                        }
                        if ((mana1 >= 1) && (mana3 >= 2)){
                            spell_133.setEnabled(true);
                            spell_133.setImageResource(R.drawable.s14_forcedextract_small);
                        }
                        else {
                            spell_133.setEnabled(false);
                            spell_133.setImageResource(R.drawable.s14_forcedextract_dim);
                        }
                        if ((mana1 >= 2) && (mana2 >= 2) && (mana3 >= 2)){
                            spell_123.setEnabled(true);
                            spell_123.setImageResource(R.drawable.s42_overtap_small);
                        }
                        else {
                            spell_123.setEnabled(false);
                            spell_123.setImageResource(R.drawable.s42_overtap_dim);
                        }
                   // }
                    start = false;



//                    if (mana1 + mana2 + mana3 > 5) {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
//                        builder.setMessage("You have too much mana!");
//                    }
                    //grey out spells that do not meet mana requirement
                }
                checkVictory(player);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        selfAttack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission == 1) {
                    Map<String,Object> mapSelf, mapEnemy;
                    if(player.equals("1")){
                        mapSelf = player1Map;
                        mapEnemy = player2Map;
                    }else{
                        mapSelf = player2Map;
                        mapEnemy = player1Map;
                    }


                    int damage = 3;
                    int currentEnemyHP = Integer.parseInt(mapEnemy.get("health").toString());//Integer.parseInt(enemyHealth.getText().toString());
                    int finalEnemyHP = currentEnemyHP - damage;
                    mapEnemy.put("health",finalEnemyHP);

                    pushPlayerMapstoDB();
                    //String send = Integer.toString(finalHP);
                    enemyHealth.setText(Integer.toString(finalEnemyHP));
                    gamesRef.child(ID).child("turn").setValue(enemy);
                    gamesRef.child(ID).child("lastSpell").setValue("Auto Attack");
                    selfAttack.setEnabled(false);
                    permission = 0;
                }
            }
        });

        mainSpellReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission == 1){
//                    Toast.makeText(getBaseContext(), "Trying to cast Flare.", Toast.LENGTH_SHORT).show();
                    Map<String,Object> mapSelf, mapEnemy;
                    if(player.equals("1")){
                        mapSelf = player1Map;
                        mapEnemy = player2Map;
                    }else{
                        mapSelf = player2Map;
                        mapEnemy = player1Map;
                    }

                    List<Map<String,Object>> maps;

                    switch (mainSpell) {
                        case "111":
                            maps = Spells.Flare(mapSelf, mapEnemy, 0);
                            if(player.equals("1")){
                                player1Map = maps.get(0);
                                player2Map = maps.get(1);
                            }else{
                                player2Map = maps.get(0);
                                player1Map = maps.get(1);
                            }
                            permission = 0;
                            gamesRef.child(ID).child("lastSpell").setValue("Flare");
                            break;
                        case "222":
                            maps = Spells.Bolt(mapSelf, mapEnemy, 0);
                            if(player.equals("1")){
                                player1Map = maps.get(0);
                                player2Map = maps.get(1);
                            }else{
                                player2Map = maps.get(0);
                                player1Map = maps.get(1);
                            }
                            permission = 0;
                            gamesRef.child(ID).child("lastSpell").setValue("Bolt");
                            break;
                        case "333":
                            maps = Spells.Enlighten(mapSelf, mapEnemy, 0);
                            if(player.equals("1")){
                                player1Map = maps.get(0);
                                player2Map = maps.get(1);
                            }else{
                                player2Map = maps.get(0);
                                player1Map = maps.get(1);
                            }
                            permission = 0;
                            gamesRef.child(ID).child("lastSpell").setValue("Enlighten");
                            break;
                        case "112":
                            maps = Spells.Explosion(mapSelf, mapEnemy, 0, 0);
                            if(player.equals("1")){
                                player1Map = maps.get(0);
                                player2Map = maps.get(1);
                            }else{
                                player2Map = maps.get(0);
                                player1Map = maps.get(1);
                            }
                            permission = 0;
                            gamesRef.child(ID).child("lastSpell").setValue("Explosion");
                            break;
                        case "122":
                            maps = Spells.Sunburst(mapSelf, mapEnemy, 0, 0);
                            if(player.equals("1")){
                                player1Map = maps.get(0);
                                player2Map = maps.get(1);
                            }else{
                                player2Map = maps.get(0);
                                player1Map = maps.get(1);
                            }
                            permission = 0;
                            break;
                        case "113":
                            maps = Spells.ManaCombustion(mapSelf, mapEnemy, 0, 0);
                            if(player.equals("1")){
                                player1Map = maps.get(0);
                                player2Map = maps.get(1);
                            }else{
                                player2Map = maps.get(0);
                                player1Map = maps.get(1);
                            }
                            permission = 0;
                            gamesRef.child(ID).child("lastSpell").setValue("Mana Combustion");
                            break;
                        case "133":
                            maps = Spells.ForcedExtraction(mapSelf, mapEnemy, 0, 0);
                            if(player.equals("1")){
                                player1Map = maps.get(0);
                                player2Map = maps.get(1);
                            }else{
                                player2Map = maps.get(0);
                                player1Map = maps.get(1);
                            }
                            permission = 0;
                            gamesRef.child(ID).child("lastSpell").setValue("Forced Extraction");
                            break;
                        case "223":
                            maps = Spells.TransformerBolt(mapSelf, mapEnemy, 0);
                            if(player.equals("1")){
                                player1Map = maps.get(0);
                                player2Map = maps.get(1);
                            }else{
                                player2Map = maps.get(0);
                                player1Map = maps.get(1);
                            }
                            permission = 0;
                            gamesRef.child(ID).child("lastSpell").setValue("Transformer Bolt");
                            break;
                        case "233":
                            maps = Spells.ElectricPulse(mapSelf, mapEnemy, 0, 0);
                            if(player.equals("1")){
                                player1Map = maps.get(0);
                                player2Map = maps.get(1);
                            }else{
                                player2Map = maps.get(0);
                                player1Map = maps.get(1);
                            }
                            permission = 0;
                            gamesRef.child(ID).child("lastSpell").setValue("Electric Pulse");
                            break;
                        case "123":
                            maps = Spells.Overtap(mapSelf, mapEnemy, 0, 0, 0);
                            if(player.equals("1")){
                                player1Map = maps.get(0);
                                player2Map = maps.get(1);
                            }else{
                                player2Map = maps.get(0);
                                player1Map = maps.get(1);
                            }
                            permission = 0;
                            gamesRef.child(ID).child("lastSpell").setValue("Overtap");
                            break;
                    }
                    pushPlayerMapstoDB();
                    setManaValues(player);
                    gamesRef.child(ID).child("turn").setValue(enemy);
                    selfAttack.setEnabled(false);
                    permission = 0;
                }
            }
        });

        spell_111.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpellReady.setImageResource(R.drawable.s02_flare);
                mainSpellName.setText("Flare");
                mainSpellDesc.setText("Deal 6(R8)(R10) damage.  There is a 33% chance that the same amplification of Flare will be cast again for free.");
                manaCost1.setImageResource(R.drawable.ml_red_small);
                manaCost2.setImageResource(R.drawable.ml_red_small);
                manaCost3.setImageResource(R.drawable.ml_red_small);
                manaAmp1.setImageResource(R.drawable.ml_gray_small);
                manaAmp2.setImageResource(R.drawable.ml_gray_small);
                manaAmp3.setImageResource(R.drawable.ml_gray_small);
                mainSpell = "111";
            }
        });
        spell_112.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpellReady.setImageResource(R.drawable.s09_explo);
                mainSpellName.setText("Explosion");
                mainSpellDesc.setText("Deal 15(R20)(R25) damage to your opponent and 10(Y9)(Y7) damage to yourself.");
                manaCost1.setImageResource(R.drawable.ml_red_small);
                manaCost2.setImageResource(R.drawable.ml_red_small);
                manaCost3.setImageResource(R.drawable.ml_yellow_small);
                manaAmp1.setImageResource(R.drawable.ml_gray_small);
                manaAmp2.setImageResource(R.drawable.ml_gray_small);
                manaAmp3.setImageResource(R.drawable.ml_gray_small);
                mainSpell = "112";
            }
        });

//        /*
        spell_122.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpellReady.setImageResource(R.drawable.s10_suburst);
                mainSpellName.setText("Suburst");
                mainSpellDesc.setText("Heal 20");
                manaCost1.setImageResource(R.drawable.ml_red_small);
                manaCost2.setImageResource(R.drawable.ml_yellow_small);
                manaCost3.setImageResource(R.drawable.ml_yellow_small);
                manaAmp1.setImageResource(R.drawable.ml_gray_small);
                manaAmp2.setImageResource(R.drawable.ml_gray_small);
                manaAmp3.setImageResource(R.drawable.ml_gray_small);
                mainSpell = "122";
            }
        });
        spell_222.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpellReady.setImageResource(R.drawable.s03_bolt);
                mainSpellName.setText("Bolt");
                mainSpellDesc.setText("Deal 10(Y15)(Y20) damage.  This damage cannot be blocked and your opponent cannot heal next turn.");
                manaCost1.setImageResource(R.drawable.ml_yellow_small);
                manaCost2.setImageResource(R.drawable.ml_yellow_small);
                manaCost3.setImageResource(R.drawable.ml_yellow_small);
                manaAmp1.setImageResource(R.drawable.ml_gray_small);
                manaAmp2.setImageResource(R.drawable.ml_gray_small);
                manaAmp3.setImageResource(R.drawable.ml_gray_small);
                mainSpell = "222";
            }
        });
        spell_223.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpellReady.setImageResource(R.drawable.s27_transformer);
                mainSpellName.setText("Transformer Bolt");
                mainSpellDesc.setText("Deal 5 damage multiply by your remaining mana pool.  Your pool is drained afterwards.");
                manaCost1.setImageResource(R.drawable.ml_yellow_small);
                manaCost2.setImageResource(R.drawable.ml_yellow_small);
                manaCost3.setImageResource(R.drawable.ml_blue_small);
                manaAmp1.setImageResource(R.drawable.ml_gray_small);
                manaAmp2.setImageResource(R.drawable.ml_gray_small);
                manaAmp3.setImageResource(R.drawable.ml_gray_small);
                mainSpell = "223";
            }
        });
        spell_233.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpellReady.setImageResource(R.drawable.s28_pulse);
                mainSpellName.setText("Electric Pulse");
//                mainSpellDesc.setText("Deal 5(Y7)(Y10) damage.  Your opponent must discard 2(B3)(B4) mana.");
                mainSpellDesc.setText("Deal 5 damage.  Your opponent must discard 2 mana.");
                manaCost1.setImageResource(R.drawable.ml_yellow_small);
                manaCost2.setImageResource(R.drawable.ml_blue_small);
                manaCost3.setImageResource(R.drawable.ml_blue_small);
                manaAmp1.setImageResource(R.drawable.ml_gray_small);
                manaAmp2.setImageResource(R.drawable.ml_gray_small);
                manaAmp3.setImageResource(R.drawable.ml_gray_small);
                mainSpell = "233";
            }
        });
        spell_333.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpellReady.setImageResource(R.drawable.s05_enlighten);
                mainSpellName.setText("Enlighten");
                mainSpellDesc.setText("Gain 7 mana.");
                manaCost1.setImageResource(R.drawable.ml_blue_small);
                manaCost2.setImageResource(R.drawable.ml_blue_small);
                manaCost3.setImageResource(R.drawable.ml_blue_small);
                manaAmp1.setImageResource(R.drawable.ml_gray_small);
                manaAmp2.setImageResource(R.drawable.ml_gray_small);
                manaAmp3.setImageResource(R.drawable.ml_gray_small);
                mainSpell = "333";
            }
        });
        spell_113.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpellReady.setImageResource(R.drawable.s13_manacombust);
                mainSpellName.setText("Mana Combustion");
                mainSpellDesc.setText("Deal 3 damage for each mana present in target's mana pool.");
                manaCost1.setImageResource(R.drawable.ml_red_small);
                manaCost2.setImageResource(R.drawable.ml_red_small);
                manaCost3.setImageResource(R.drawable.ml_blue_small);
                manaAmp1.setImageResource(R.drawable.ml_gray_small);
                manaAmp2.setImageResource(R.drawable.ml_gray_small);
                manaAmp3.setImageResource(R.drawable.ml_gray_small);
                mainSpell = "113";
            }
        });
        spell_133.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpellReady.setImageResource(R.drawable.s14_forcedextract);
                mainSpellName.setText("Forced Extraction");
                mainSpellDesc.setText("Steal 1 mana and deal 5 damage");
                manaCost1.setImageResource(R.drawable.ml_red_small);
                manaCost2.setImageResource(R.drawable.ml_blue_small);
                manaCost3.setImageResource(R.drawable.ml_blue_small);
                manaAmp1.setImageResource(R.drawable.ml_gray_small);
                manaAmp2.setImageResource(R.drawable.ml_gray_small);
                manaAmp3.setImageResource(R.drawable.ml_gray_small);
                mainSpell = "133";
            }
        });
        spell_123.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainSpellReady.setImageResource(R.drawable.s42_overtap);
                mainSpellName.setText("Overtap");
                mainSpellDesc.setText("Deal 26 damage.");
                manaCost1.setImageResource(R.drawable.ml_red_small);
                manaCost2.setImageResource(R.drawable.ml_yellow_small);
                manaCost3.setImageResource(R.drawable.ml_blue_small);
                manaAmp1.setImageResource(R.drawable.ml_red_small);
                manaAmp2.setImageResource(R.drawable.ml_yellow_small);
                manaAmp3.setImageResource(R.drawable.ml_blue_small);
                mainSpell = "123";
            }
        });
//        */

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

        gamesRef.child(ID).child("lastSpell").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    lastSpellCast = dataSnapshot.getValue().toString();
                    enemyActivity.setText("Last spell cast: " + lastSpellCast);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        gamesRef.child(ID).child("player1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    setPlayer1Map(map);
                } catch (Exception e) {
                    Log.w("UPDATE MAPS", "TRY AGAIN!");
                    //updatePlayerMapsFromDB();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        gamesRef.child(ID).child("player2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    setPlayer2Map(map);
                } catch (Exception e) {
                    Log.w("UPDATE MAPS", "TRY AGAIN!");
                    //updatePlayerMapsFromDB();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        //updatePlayerMapsFromDB();

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
        //gamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
        gamesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    Map<String, Object> map = (Map<String, Object>) snapshot.child("player1").getValue();
                    Map<String, Object> map2 = (Map<String, Object>) snapshot.child("player2").getValue();
                    setPlayer1Map(map);
                    setPlayer2Map(map2);
                } catch (Exception e) {
                    Log.w("UPDATE MAPS", "TRY AGAIN!");
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

//    public void setMainSpell(String spell){
//        mainSpell = spell;
//    }


}
