package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class LoginActivity extends AppCompatActivity {

    // Normal Sign In
    private EditText email,password;
    private boolean email_field, password_field;
    private ProgressBar load;
    private boolean verified;
    // Google Sign in

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001; //9001;
    private GoogleApiClient mGoogleApiClient;

    private Button googleBtn;

    // Firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        load = (ProgressBar) findViewById(R.id.load);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    SharedPreferences.Editor editor = getSharedPreferences("Tour", 0).edit();
                    editor.putBoolean("visited", true);
                    editor.apply();
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    load.setVisibility(View.INVISIBLE);
                    Intent i = new Intent(LoginActivity.this,MapsActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        // ...

        googleBtn = (Button) findViewById(R.id.googleSignIn);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener(){
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this,"You Got an Error",Toast.LENGTH_LONG).show();
                    }

                }).addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        load.setVisibility(View.VISIBLE);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void register(View view){
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
        //finish();
    }

    public void login(View view){

        load.setVisibility(View.VISIBLE);
        String email_text,password_text;

        email_text = email.getText().toString();
        password_text = password.getText().toString();

        email_field = email_text.length() != 0
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email_text).matches();

        password_field = password_text.length() >= 8;
        verified = verifyFields(email_field, password_field);

        if (verified) {
            startActivity(new Intent(LoginActivity.this,MapsActivity.class));
            SharedPreferences.Editor editor = getSharedPreferences("Session", 0).edit();
            editor.putBoolean("logged",true);
            editor.apply();
            finish();
        } else {
            load.setVisibility(View.INVISIBLE);
            //Toast.makeText(getBaseContext(),"Fields must be filled",Toast.LENGTH_SHORT).show();
        }
    }

    public void restore(View view){
        startActivity(new Intent(LoginActivity.this,PasswordRestoreActivity.class));
        finish();
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public boolean verifyFields(boolean email_field, boolean password_field){
        if(!email_field)
            email.setError(getString(R.string.email_valid));
        if(!password_field)
            password.setError(getString(R.string.password_valid));
        //if(!country_field)
        // Toast.makeText(getBaseContext(),"Choose a Country",Toast.LENGTH_SHORT).show();
        //if(!city_field)
        //Toast.makeText(getBaseContext(),"Choose a City",Toast.LENGTH_SHORT).show();
        return (email_field);
    }
}