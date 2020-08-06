package com.hudzah.wearamask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.facebook.ParseFacebookUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Collection;

public class LoginActivity extends AppCompatActivity {

    ScrollView layout;
    CardView cardView;
    TextInputLayout usernameInput;
    TextInputLayout passwordInput;
    String username;
    String password;
    TextView registerTextView;
    TextView forgotPasswordTextView;
    private SignInButton signInGoogleButton;
    private int RC_SIGN_IN = 0;
    GoogleSignInClient mGoogleSignInClient;
    Button signInFacebookButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        layout = (ScrollView) findViewById(R.id.layout);
        cardView = (CardView) findViewById(R.id.loginCardView);
        usernameInput = (TextInputLayout) findViewById(R.id.usernameInput);
        passwordInput = (TextInputLayout) findViewById(R.id.passwordInput);
        registerTextView = (TextView) findViewById(R.id.registerButton);
        forgotPasswordTextView = (TextView)findViewById(R.id.forgotButton);
        signInGoogleButton = (SignInButton) findViewById(R.id.signInGoogleButton);
        signInFacebookButton = (Button) findViewById(R.id.signInFacebookButton);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        signInGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithGoogle();
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        signInFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookSignUp();
            }
        });

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if(getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void login(){
        username = usernameInput.getEditText().getText().toString();
        password = passwordInput.getEditText().getText().toString();
        if(validateData()){
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e == null){
                       goToMaps();
                    }
                    else{
                        // TODO: 7/28/2020 Display dialog
                    }
                }
            });
        }
    }

    private boolean validateData(){
        boolean verified = false;
        if(usernameInput.getEditText().getText().toString().equals("")){
            verified = false;
            usernameInput.setError("Field cannot be empty!");
        }
        if(passwordInput.getEditText().getText().toString().equals("")){
            verified = false;
            passwordInput.setError("Field cannot be empty!");
        }
        else{
            verified = true;
        }

        return verified;
    }

    private void register(){
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void loginWithGoogle(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void forgotPassword(){

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void facebookSignUp() {
        Collection<String> permissions = Arrays.asList("public_profile", "email");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (err != null) {
                    ParseUser.logOut();
                    Log.e("err", "err", err);
                }
                if (user == null) {
                    ParseUser.logOut();
                    Toast.makeText(LoginActivity.this, "The user cancelled the Facebook login.", Toast.LENGTH_LONG).show();
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Toast.makeText(LoginActivity.this, "User signed up and logged in through Facebook.", Toast.LENGTH_LONG).show();
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                    getUserDetailFromFB();
                } else {
                    Toast.makeText(LoginActivity.this, "User logged in through Facebook.", Toast.LENGTH_LONG).show();
                    Log.d("MyApp", "User logged in through Facebook!");
                    goToMaps();

                    // TODO: 8/6/2020 ALERT for login account already exists and to login instead
                }
            }
        });
    }

    void getUserDetailFromFB(){
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),new  GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                final ParseUser user = ParseUser.getCurrentUser();
                try{
                    user.setUsername(object.getString("name"));
                    user.put("signUpMethod", "facebook");

                }catch(JSONException e){
                    e.printStackTrace();
                }
                try{
                    user.setEmail(object.getString("email"));
                }catch(JSONException e){
                    e.printStackTrace();
                }
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        goToMaps();
                        //alertDisplayer("First Time Login", "Welcome!");
                    }
                });
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }


    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(this, "Code " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
        }

    }

    private void goToMaps(){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}