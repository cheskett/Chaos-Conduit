package org.chaosconduit;

import android.util.Log;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Cameron on 3/7/2015.
 */
public class Spells {

    public static List Flare(Map<String, Object> mapSelf, Map<String, Object> mapEnemy, int redAmp) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        int enemyHealth = Integer.parseInt(mapEnemy.get("health").toString());
        // need to parse mana amounts then decrement it.
        //int selfManaRed = Integer.parseInt(mapSelf.get("manaAmt").toString());
        int damage = 6, chance = 3;
        int flareCount = 0;

        if (redAmp == 0) {
            damage = 6;
        }
        else if (redAmp == 1) {
            damage = 8;
        }
        else if (redAmp == 2) {
            damage = 10;
        }

        Random flareChance = new Random();
        while (chance == 3){
            enemyHealth = enemyHealth - damage;
            chance = flareChance.nextInt(3) + 1;
            flareCount++;
            //Log.w("FLARE","Flare Triggered " + flareCount + " times");
            //Toast.makeText(getBaseContext(), ("Flare Triggered %d times", flareCount), Toast.LENGTH_SHORT.show());
        }

        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
        long selfRedMana = selfMana.get(0);
        long selfYellowMana = selfMana.get(1);
        long selfBlueMana = selfMana.get(2);
        selfRedMana = selfRedMana - (3 + redAmp);
//        selfYellowMana = selfYellowMana - (0 + yellowAmp);
//        selfBlueMana = selfBlueMana - (0 + blueAmp);
        selfMana.set(0, selfRedMana);
//        selfMana.set(1, selfYellowMana);
//        selfMana.set(2, selfBlueMana);
        mapSelf.put("manaAmt",selfMana);

        mapEnemy.put("health", enemyHealth);
        maps.add(mapSelf);
        maps.add(mapEnemy);
        return maps;
    }

    public static List Bolt(Map<String, Object> mapSelf, Map<String, Object> mapEnemy, int yellowAmp) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        int enemyHealth = Integer.parseInt(mapEnemy.get("health").toString());
        int damage = 10;
        if (yellowAmp == 1) {
            damage = 15;
        }
        else if (yellowAmp == 2) {
            damage = 20;
        }

        enemyHealth = enemyHealth - damage;

        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
//        long selfRedMana = selfMana.get(0);
        long selfYellowMana = selfMana.get(1);
//        long selfBlueMana = selfMana.get(2);
//        selfRedMana = selfRedMana - (0 + redAmp);
        selfYellowMana = selfYellowMana - (3 + yellowAmp);
//        selfBlueMana = selfBlueMana - (0 + blueAmp);
//        selfMana.set(0, selfRedMana);
        selfMana.set(1, selfYellowMana);
//        selfMana.set(2, selfBlueMana);
        mapSelf.put("manaAmt",selfMana);

        mapEnemy.put("health", enemyHealth);
        //add no heal buff for 1 turn.
        maps.add(mapSelf);
        maps.add(mapEnemy);
        return maps;
    }

    public static List Enlighten(Map<String, Object> mapSelf,
                                 Map<String, Object> mapEnemy,
                                 int blueAmp) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
        long selfRedMana = selfMana.get(0);
        long selfYellowMana = selfMana.get(1);
        long selfBlueMana = selfMana.get(2);

        int rollAmount = 7;
        if (blueAmp == 1) {
            rollAmount = 10;
        }
        else if (blueAmp == 2) {
            rollAmount = 15;
        }

        Random randMana = new Random();
        for (int i = 0; i < rollAmount; i++) {
            int rolledMana = randMana.nextInt(3) + 1;
            switch (rolledMana) {
                case 1:
                    selfRedMana++;
                    break;
                case 2:
                    selfYellowMana++;
                    break;
                case 3:
                    selfBlueMana++;
                    break;
            }
        }

        selfRedMana = selfRedMana - (0);
        selfYellowMana = selfYellowMana - (0);
        selfBlueMana = selfBlueMana - (3 + blueAmp);
        selfMana.set(0, selfRedMana);
        selfMana.set(1, selfYellowMana);
        selfMana.set(2, selfBlueMana);
        mapSelf.put("manaAmt",selfMana);

        maps.add(mapSelf);
        maps.add(mapEnemy);
        return maps;
    }

    public static List Explosion(Map<String, Object> mapSelf,
                                 Map<String, Object> mapEnemy,
                                 int redAmp,
                                 int yellowAmp) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        int enemyHealth = Integer.parseInt(mapEnemy.get("health").toString());
        int selfHealth = Integer.parseInt(mapSelf.get("health").toString());

        int damage = 15, damageSelf = 10;

        if (redAmp == 1) {
            damage = 20;
        }
        else if (redAmp == 2) {
            damage = 25;
        }

        if (yellowAmp == 1) {
            damageSelf = 7;
        }
        else if (yellowAmp == 2) {
            damageSelf = 3;
        }

        enemyHealth = enemyHealth - damage;
        selfHealth = selfHealth - damageSelf;
        mapEnemy.put("health", enemyHealth);
        mapSelf.put("health", selfHealth);

        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
        long selfRedMana = selfMana.get(0);
        long selfYellowMana = selfMana.get(1);
//        long selfBlueMana = selfMana.get(2);
        selfRedMana = selfRedMana - (1 + redAmp);
        selfYellowMana = selfYellowMana - (1 + yellowAmp);
//        selfBlueMana = selfBlueMana - (1 + blueAmp);
        selfMana.set(0, selfRedMana);
        selfMana.set(1, selfYellowMana);
//        selfMana.set(2, selfBlueMana);
        mapSelf.put("manaAmt",selfMana);

        maps.add(mapSelf);
        maps.add(mapEnemy);
        return maps;
    }

//    public static List Sunburst(Map<String, Object> mapSelf,
//                                Map<String, Object> mapEnemy,
//                                int redAmp,
//                                int yellowAmp) {
//        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
//        int enemyHealth = Integer.parseInt(mapEnemy.get("health").toString());
//        int selfSunburstHist = Integer.parseInt(mapSelf.get("sunburstHist").toString());
//        int damage = 20;
//
//        if (selfSunburstHist == 1) {
//            damage = 0;
//        }
//
//        enemyHealth = enemyHealth - damage;
//        mapEnemy.put("health", enemyHealth);
//
//        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
//        long selfRedMana = selfMana.get(0);
//        long selfYellowMana = selfMana.get(1);
//        selfRedMana = selfRedMana - (1 + redAmp);
//        selfYellowMana = selfYellowMana - (1 + yellowAmp);
//        selfMana.set(1, selfRedMana);
//        selfMana.set(2, selfYellowMana);
//        mapSelf.put("manaAmt",selfMana);
//
//        mapSelf.put("sunburstHist", "1");
//        maps.add(mapSelf);
//        maps.add(mapEnemy);
//        return maps;
//    }

    public static List Sunburst(Map<String, Object> mapSelf,
                                Map<String, Object> mapEnemy,
                                int redAmp,
                                int yellowAmp) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        int selfHealth = Integer.parseInt(mapSelf.get("health").toString());
        int heal = 20;

        selfHealth = selfHealth + heal;
        mapSelf.put("health", selfHealth);

        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
        long selfRedMana = selfMana.get(0);
        long selfYellowMana = selfMana.get(1);
        selfRedMana = selfRedMana - (1 + redAmp);
        selfYellowMana = selfYellowMana - (2 + yellowAmp);
        selfMana.set(1, selfRedMana);
        selfMana.set(2, selfYellowMana);
        mapSelf.put("manaAmt",selfMana);

        maps.add(mapSelf);
        maps.add(mapEnemy);
        return maps;
    }

    public static List ManaCombustion(Map<String, Object> mapSelf,
                                 Map<String, Object> mapEnemy,
                                 int redAmp,
                                 int blueAmp) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        int enemyHealth = Integer.parseInt(mapEnemy.get("health").toString());
        int damage = 3;
        ArrayList<Long> enemyMana = (ArrayList<Long>) mapEnemy.get("manaAmt");
        int enemyManaSum = (int) (enemyMana.get(0) + enemyMana.get(1) + enemyMana.get(2));

        if (redAmp == 1) {
            damage = 4;
        }
        else if (redAmp == 2) {
            damage = 5;
        }
        damage = damage * enemyManaSum;

        enemyHealth = enemyHealth - damage;
        mapEnemy.put("health", enemyHealth);

        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
        long selfRedMana = selfMana.get(0);
        long selfBlueMana = selfMana.get(2);
        selfRedMana = selfRedMana - (2 + redAmp);
        selfBlueMana = selfBlueMana - (1 + blueAmp);
        selfMana.set(0, selfRedMana);
        selfMana.set(2, selfBlueMana);
        mapSelf.put("manaAmt",selfMana);

        maps.add(mapSelf);
        maps.add(mapEnemy);
        return maps;
    }

    public static List ForcedExtraction(Map<String, Object> mapSelf,
                                 Map<String, Object> mapEnemy,
                                 int redAmp,
                                 int blueAmp) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        int enemyHealth = Integer.parseInt(mapEnemy.get("health").toString());
        int damage = 5;

        if (redAmp == 1) {
            damage = 7;
        }
        else if (redAmp == 2) {
            damage = 10;
        }

        enemyHealth = enemyHealth - damage;
        mapEnemy.put("health", enemyHealth);

        ArrayList<Long> enemyMana = (ArrayList<Long>) mapEnemy.get("manaAmt");
        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
        int steal = 0, found = 0;
        Random randomSteal = new Random();
        while (found == 0){
            if ((enemyMana.get(0) == 0) && (enemyMana.get(1) == 0) && (enemyMana.get(2) == 0)) {
                found = 1;
            }
            steal = randomSteal.nextInt(3);
            long tryEnemyMana = enemyMana.get(steal);
            if (tryEnemyMana > 0) {
                found = 1;
                enemyMana.set(steal, tryEnemyMana - 1);
                selfMana.set(steal, selfMana.get(steal) + 1);
            }
        }

        long selfRedMana = selfMana.get(0);
        long selfBlueMana = selfMana.get(2);
        selfRedMana = selfRedMana - (1 + redAmp);
        selfBlueMana = selfBlueMana - (2 + blueAmp);
        selfMana.set(0, selfRedMana);
        selfMana.set(2, selfBlueMana);
        mapSelf.put("manaAmt",selfMana);

        maps.add(mapSelf);
        maps.add(mapEnemy);
        return maps;
    }

    public static List TransformerBolt(Map<String, Object> mapSelf,
                                 Map<String, Object> mapEnemy,
                                 int anyAmp) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        int enemyHealth = Integer.parseInt(mapEnemy.get("health").toString());
        int damage = 5;
        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
        long selfRedMana = selfMana.get(0);
        long selfYellowMana = selfMana.get(1);
        long selfBlueMana = selfMana.get(2);

        if (anyAmp == 1) {
            damage = 10;
        }
        else if (anyAmp == 2) {
            damage = 16;
        }

        selfRedMana = selfRedMana - (0);
        selfBlueMana = selfBlueMana - (2);
        selfYellowMana = selfYellowMana - (1);
        int selfManaSum = (int) (selfRedMana + selfBlueMana + selfYellowMana);
        damage = damage * selfManaSum;

        enemyHealth = enemyHealth - damage;
        mapEnemy.put("health", enemyHealth);

        selfRedMana = 0;
        selfYellowMana = 0;
        selfBlueMana = 0;
        selfMana.set(0, selfRedMana);
        selfMana.set(1, selfYellowMana);
        selfMana.set(2, selfBlueMana);
        mapSelf.put("manaAmt",selfMana);

        maps.add(mapSelf);
        maps.add(mapEnemy);
        return maps;
    }

    public static List ElectricPulse(Map<String, Object> mapSelf,
                                 Map<String, Object> mapEnemy,
                                 int yellowAmp,
                                 int blueAmp) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        int enemyHealth = Integer.parseInt(mapEnemy.get("health").toString());
        //get enemyMana and remove it
        int damage = 5;
        int removeMana = 5;

        if (yellowAmp == 1) {
            damage = 7;
        }
        else if (yellowAmp == 2) {
            damage = 10;
        }
        if (blueAmp == 1) {
            removeMana = 3;
        }
        else if (blueAmp == 2) {
            removeMana = 4;
        }

        ArrayList<Long> enemyMana = (ArrayList<Long>) mapEnemy.get("manaAmt");
        Random randomSteal = new Random();
        int found = 0, steal = 0;
        while (removeMana > 0) {
            steal = 0;
            found = 0;
            while (found == 0) {
                if ((enemyMana.get(0) == 0) && (enemyMana.get(1) == 0) && (enemyMana.get(2) == 0)) {
                    found = 1;
                    removeMana = 0;
                }
                steal = randomSteal.nextInt(3);
                long tryEnemyMana = enemyMana.get(steal);
                if (tryEnemyMana > 0) {
                    found = 1;
                    enemyMana.set(steal, tryEnemyMana - 1);
                }
            }
            removeMana--;
        }

        enemyHealth = enemyHealth - damage;
        mapEnemy.put("health", enemyHealth);

        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
        long selfRedMana = selfMana.get(0);
        long selfYellowMana = selfMana.get(1);
        long selfBlueMana = selfMana.get(2);
//        selfRedMana = selfRedMana - (0 + redAmp);
        selfYellowMana = selfYellowMana - (1 + yellowAmp);
        selfBlueMana = selfBlueMana - (2 + blueAmp);
        selfMana.set(0, selfRedMana);
        selfMana.set(1, selfYellowMana);
        selfMana.set(2, selfBlueMana);
        mapSelf.put("manaAmt",selfMana);

        maps.add(mapSelf);
        maps.add(mapEnemy);
        return maps;
    }

    public static List Overtap(Map<String, Object> mapSelf,
                                     Map<String, Object> mapEnemy,
                                     int redAmp,
                                     int yellowAmp,
                                     int blueAmp) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        int enemyHealth = Integer.parseInt(mapEnemy.get("health").toString());
        //get enemyMana and remove it
        int redDamage = 13, yellowDamage = 13;
        int damage = 26;
//        int overtapFatigue = 2;

        if (redAmp == 1) {
            redDamage = 9;
        }
        else if (redAmp == 2) {
            redDamage = 12;
        }
        if (yellowAmp == 1) {
            yellowDamage = 9;
        }
        else if (yellowAmp == 2) {
            yellowDamage = 12;
        }
//        if (blueAmp == 1) {
//            overtapFatigue = 1;
//        }
//        else if (blueAmp == 2) {
//            overtapFatigue = 0;
//        }

        damage = redDamage + yellowDamage;

        enemyHealth = enemyHealth - damage;
        mapEnemy.put("health", enemyHealth);
//        mapSelf.put("overtapFatigue", overtapFatigue);

        ArrayList<Long> selfMana = (ArrayList<Long>) mapSelf.get("manaAmt");
        long selfRedMana = selfMana.get(0);
        long selfYellowMana = selfMana.get(1);
        long selfBlueMana = selfMana.get(2);
        selfRedMana = selfRedMana - (2 + redAmp);
        selfYellowMana = selfYellowMana - (2 + yellowAmp);
        selfBlueMana = selfBlueMana - (2 + blueAmp);
        selfMana.set(0, selfRedMana);
        selfMana.set(1, selfYellowMana);
        selfMana.set(2, selfBlueMana);
        mapSelf.put("manaAmt",selfMana);

        maps.add(mapSelf);
        maps.add(mapEnemy);
        return maps;
    }

}
