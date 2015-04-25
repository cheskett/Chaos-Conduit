package org.chaosconduit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginActivity extends ActionBarActivity {

    private Firebase ref;
    private String PREF_NAME;
    private String userID;
    private boolean isRegistrationForm = false;
    int repeatPassEditTextID;
    int usernameEditTextID;
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_login);

        ref = new Firebase(getString(R.string.firebase));
        PREF_NAME = getResources().getString(R.string.prefs_name);

        //make form invisible
        setFormVisible(false);

        //attempt log in with saved prefs before enabling form
        attemptLoginFromPrefs();

        //log in with saved prefs failed. Present form
        setFormVisible(true);


        //Set up login button
        Button login_button = (Button) findViewById(R.id.logInButton);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFormEnabled(false);
                attemptLogin();
            }
        });

        //set up registration button
        Button reg_button = (Button)findViewById(R.id.signUpButton);
        reg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRegistrationForm)
                    setFormToRegistration();
                else {
                    registerUser();

                }
            }
        });

        //LinearLayout myLayout = (LinearLayout) findViewById(R.id.loginFrame);
        //myLayout.requestFocus();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void attemptLogin()
    {
        EditText email = (EditText)findViewById(R.id.emailEditText);
        EditText pass = (EditText)findViewById(R.id.passwordEditText);
        ref.authWithPassword(email.getText().toString(), pass.getText().toString(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                String id = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
                prefs.edit().putString("USER_ID", id).apply();
                id = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
                prefs.edit().putString("USER_PW",id).apply();

                //Toast.makeText(getBaseContext(), "SAVED PREFS", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("Uid", authData.getUid());
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                setFormEnabled(true);
                Toast.makeText(getBaseContext(), "Auth Failed: " + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attemptLoginFromPrefs() {
        String userEmail = getSavedEmail();
        String userPW = getSavedPassword();
        if (userEmail.equals("NOT_FOUND") && userPW.equals("NOT_FOUND")) {
            Toast.makeText(getBaseContext(), "Could not find saved prefs", Toast.LENGTH_LONG).show();
            setFormEnabled(true);
        }

        else {
            //Toast.makeText(getBaseContext(), "Logging in with saved preferences", Toast.LENGTH_LONG).show();
            //if (ref.getAuth() == null) {
            //String newString = new String("AUTH OKAY: " + userEmail);
          //  Toast.makeText(getBaseContext(), newString, Toast.LENGTH_LONG).show();
            ref.authWithPassword(userEmail, userPW, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    //Toast.makeText(getBaseContext(), "Logged in with user ID: " + userID, Toast.LENGTH_LONG).show();
                    userID = authData.getUid();

                    Toast.makeText(getBaseContext(), "AUTH OKAY", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra("Uid", authData.getUid());
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Toast.makeText(getBaseContext(), "Auth Failed: " + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                    setFormEnabled(true);
                }
            });

        }
    }

    public void setFormVisible(boolean b) {
        int visibility = b ? View.VISIBLE : View.INVISIBLE;
        int progressVisibility = b ? View.INVISIBLE : View.VISIBLE;

        findViewById(R.id.loginProgressBar).setVisibility(progressVisibility);
        findViewById(R.id.loginFrame).setVisibility(visibility);
        findViewById(R.id.loginButtonsLayout).setVisibility(visibility);
    }

    //TODO: Refactor for our layout
    public void setFormEnabled(boolean b)
    {
        findViewById(R.id.emailEditText).setEnabled(b);
        findViewById(R.id.passwordEditText).setEnabled(b);
        findViewById(R.id.logInButton).setEnabled(b);
        findViewById(R.id.signUpButton).setEnabled(b);
    }

    private String getSavedEmail()
    {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        return prefs.getString("USER_ID", "NOT_FOUND");
    }

    private String getSavedPassword()
    {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getString("USER_PW", "NOT_FOUND");
    }

    /*
        Function that animates the form from login to registration
     */
    private void setFormToRegistration() {
        LinearLayout loginForm = (LinearLayout)findViewById(R.id.loginFrame);
        ((RelativeLayout.LayoutParams)loginForm.getLayoutParams()).addRule(RelativeLayout.CENTER_VERTICAL, 0);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        loginForm.getLayoutParams().height = (int) (height * 0.7);

        Toast.makeText(getBaseContext(), "Hello", Toast.LENGTH_SHORT);

        ((TextView)findViewById(R.id.loginTitle)).setText("Register");


        TextView repeatPassLabel = new TextView(this);
        repeatPassLabel.setText("Repeat Password:");

        loginForm.addView(repeatPassLabel);

        //Set up password EditText
        EditText repeatPassEditText = new EditText(getBaseContext());
        repeatPassEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        repeatPassEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        repeatPassEditText.setTextColor(Color.BLACK);
        repeatPassEditText.setLinkTextColor(Color.BLACK);

        repeatPassEditTextID = generateViewId();
        repeatPassEditText.setId(repeatPassEditTextID);

        //add it to the view
        loginForm.addView(repeatPassEditText);

        TextView usernameLabel = new TextView(this);
        usernameLabel.setText("Username:");

        loginForm.addView(usernameLabel);

        EditText usernameEditText = new EditText(getBaseContext());
        usernameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        usernameEditText.setTextColor(Color.BLACK);

        usernameEditTextID = generateViewId();
        usernameEditText.setId(usernameEditTextID);

        loginForm.addView(usernameEditText);

        findViewById(R.id.logInButton).setVisibility(View.INVISIBLE);

        isRegistrationForm = true;
    }

    private void registerUser()
    {
        setFormEnabled(false);

        //EditTexts of email, passwords, and username
        EditText usernameEditText = (EditText) findViewById(usernameEditTextID);
        EditText emailEditText = (EditText)findViewById(R.id.emailEditText);
        final EditText pass = (EditText)findViewById(R.id.passwordEditText);
        EditText confirm_pass = (EditText)findViewById(repeatPassEditTextID);

        final String username = usernameEditText.getText().toString();
        final String email = emailEditText.getText().toString();
        final String password = pass.getText().toString();
        String confirm = confirm_pass.getText().toString();

        //Compare passwords make sure they match
        if(!password.equals(confirm)) {
            setFormEnabled(true);
            Toast.makeText(getBaseContext(), "Password fields must match!", Toast.LENGTH_LONG).show();
            return;
        }


        if(username.equals("") || (username == null))
        {
            setFormEnabled(true);
            Toast.makeText(getBaseContext(), "Provide a display name", Toast.LENGTH_LONG).show();
            return;
        }

        //Create the user
        ref.createUser(email, password, new Firebase.ValueResultHandler<Map<String,Object>>() {

            @Override
            public void onSuccess(Map<String,Object> result) {
                //Save those values
                SharedPreferences prefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
                prefs.edit().putString("USER_ID", email).apply();
                prefs.edit().putString("USER_PW", password).apply();

                String UID = (String)result.get("uid");

                //save user name and email
                Map<String, String> map = new HashMap<String, String>();
                map.put("email", email);
                map.put("username", username);
                ref.child("users").child(UID).setValue(map);

                attemptLoginFromPrefs();
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                setFormEnabled(true);
                Toast.makeText(getBaseContext(), "Auth Failed: " + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }

    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

}