package org.chaosconduit;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by Derial on 3/7/2015.
 */
public class MatchmakingActivity extends ActionBarActivity implements View.OnClickListener {


    boolean found;
    Firebase firebase;
    String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_opponents);
        firebase = new Firebase(getResources().getString(R.string.firebase));
        AuthData auth = firebase.getAuth();
        UID = auth.getUid();

        Button findMatch = (Button) findViewById(R.id.find_match);
        Button createMatch = (Button) findViewById(R.id.create_match);
        createMatch.setOnClickListener(this);
        findMatch.setOnClickListener(this);

    }

    public void createNewMatch(){
        Firebase gamesRef = firebase.child("games");
        GameInfo gi = new GameInfo(UID);
        gamesRef.push().setValue(gi.toMap());
    }

    public void findMatch(){
        found = false;
        final Firebase gamesRef = firebase.child("games");
        gamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Iterable<DataSnapshot> iterator = snapshot.getChildren();
                for (DataSnapshot ds : iterator) {
                    String p2 = ds.child("status").getValue().toString();
                    if (p2.equals("open")) {
                        gamesRef.child(ds.getKey()).child("player2").setValue(UID);
                        gamesRef.child(ds.getKey()).child("status").setValue("closed");
                        found = true;
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.find_match:
                Toast.makeText(getBaseContext(),"Find Match",Toast.LENGTH_SHORT).show();
                findMatch();
                break;
            case R.id.create_match:
                createNewMatch();
                break;
        }
    }
}
