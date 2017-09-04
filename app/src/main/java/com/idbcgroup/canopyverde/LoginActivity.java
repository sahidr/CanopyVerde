package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity {

    // Server Sign In
    private EditText email,password;

    // Google Sign in
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        callbackManager = CallbackManager.Factory.create();

        progressBar = (ProgressBar) findViewById(R.id.load);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
        Button fb = (Button) findViewById(R.id.facebookSignIn);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    progressBar.setVisibility(View.INVISIBLE);
                    Intent i = new Intent(LoginActivity.this,MapActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // Facebook

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                progressBar.setVisibility(View.INVISIBLE);
            }

        });

        // Google

        Button googleBtn = (Button) findViewById(R.id.googleSignIn);

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

    /**
     * This method handle the Facebook Access token
     * @param token
     */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
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
                        } else {
                            Log.w("TASK", String.valueOf(task.getResult().getUser()));
                            String personId =task.getResult().getUser().getUid();
                            String personName=task.getResult().getUser().getDisplayName();
                            String personEmail=task.getResult().getUser().getEmail();
                            Uri personPhoto =task.getResult().getUser().getPhotoUrl();

                            String[] emailParts = personEmail != null ? personEmail.split("@") : new String[0];
                            String username =  emailParts[0];

                            Log.w("DATAAA", personName+personEmail+personId+personPhoto);
                            SharedPreferences.Editor editor = getSharedPreferences("Session", 0).edit();
                            editor.putBoolean("logged",true);
                            editor.putString("name",personName);
                            editor.putString("email",personEmail);
                            editor.putString("id",personId);
                            editor.putString("username","@"+username);
                            editor.putString("photo", String.valueOf(personPhoto));
                            editor.apply();
                        }
                    }
                });
    }

    /**
     * Google Handler
     * @param account
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
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

    /**
     * Login Result
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressBar.setVisibility(View.VISIBLE);
        //callbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                assert account != null;
                String personName = account.getDisplayName();
                String personEmail = account.getEmail();
                String personId = account.getId();
                Uri personPhoto = account.getPhotoUrl();

                String[] emailParts = personEmail != null ? personEmail.split("@") : new String[0];
                String username =  emailParts[0];

                SharedPreferences.Editor editor = getSharedPreferences("Session", 0).edit();
                editor.putBoolean("logged",true);
                editor.putString("name",personName);
                editor.putString("email",personEmail);
                editor.putString("id",personId);
                editor.putString("username","@"+username);
                editor.putString("photo", String.valueOf(personPhoto));
                editor.apply();

            }
        } else {
            // Facebook Sign In
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     *
     */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    /**
     *
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     *
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     *
     * @param view
     */
    public void login(View view){

        String email_text,password_text;
        email_text = email.getText().toString();
        password_text = password.getText().toString();

        boolean email_field = email_text.length() != 0
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email_text).matches();
        boolean password_field = password_text.length() >= 8;

        boolean verified = verifyFields(email_field, password_field);

        if (verified) {
            AttemptLogin l = new AttemptLogin();
            l.execute(email_text,password_text);
        }
    }

    /**
     *
     * @param email_field
     * @param password_field
     * @return
     */
    private boolean verifyFields(boolean email_field, boolean password_field){
        if(!email_field) {
            email.setError(getString(R.string.email_valid));
            email.setBackgroundResource(R.drawable.first_field_error);
        } else
            email.setBackgroundResource(R.drawable.first_field);
        if(!password_field) {
            password.setError(getString(R.string.password_valid));
            password.setBackgroundResource(R.drawable.last_field_error);
        }else
            password.setBackgroundResource(R.drawable.last_field);
        return (email_field && password_field);
    }

    /**
     *
     * @param view
     */
    public void fb_login(View view){
        loginButton.performClick();
    }

    /**
     * This method calls the intent of the PasswordRestoreActivity
     * @param view textview of the view
     */
    public void restore(View view){
        startActivity(new Intent(LoginActivity.this,PasswordRestoreActivity.class));
    }

    /**
     *
     * @param view
     */
    public void register(View view){
        startActivity(new Intent(LoginActivity.this,UserRegisterActivity.class));
    }

    /**
     *
     * @param newBase
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    // AsyncTask. Sends Log In's data to the server's API and process the response.
    private class AttemptLogin extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Sends validated Log In's data to the server's API and process the response. Returns an
        // integer value ([-1..1):
        // * -1, if an error occurred during the communication
        // * 0, if everything went OK (redirecting to MainActivity and updating SharedPreferences afterwards)
        // * 1, if the credentials provided aren't valid
        @Override
        protected Integer doInBackground(String... strings) {

            Integer result = -1;
            try {
                // Defining and initializing server's communication's variables
                String credentials = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8");
                credentials += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8");

                URL url = new URL("http://192.168.1.85:8000/api-token-auth/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(credentials);
                writer.flush();
                APIResponse response = JSONResponseController.getJsonResponse(connection,true);

                if (response != null){

                    if (response.getStatus()==HttpURLConnection.HTTP_OK) {
                        JSONObject response_body = response.getBody();

                        SharedPreferences.Editor editor = getSharedPreferences("Session", 0).edit();
                        editor.putBoolean("logged",true);
                        editor.putString("username",response_body.getString("username"));
                        editor.putInt("id",response_body.getInt("id"));
                        editor.putString("email",response_body.getString("email"));
                        editor.putString("token",response_body.getString("token"));
                        editor.putString("fullname",response_body.getString("fullname"));
                        editor.putString("country",response_body.getString("country"));
                        editor.putString("city",response_body.getString("city"));
                        editor.putString("badge",response_body.getString("badge"));
                        editor.putInt("game_points",response_body.getInt("points"));
                        editor.apply();

                        Log.d("OK", "OK");
                        return 0;

                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        Log.d("BAD", "BAD");
                        result = 1;
                    } else {
                        Log.d("NOT", "FOUND");
                        result = -1;
                    }
                }

            } catch (MalformedURLException e) {
                return result;
            } catch (IOException e) {
                return result;
            } catch (JSONException e) {
                return result;
            }
            return result;
        }

        // Process doInBackground() results
        @Override
        protected void onPostExecute(Integer anInt) {
            int message;
            switch (anInt) {
                case (-1):
                    message = R.string.error;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    break;
                case (0):
                    Intent intent = new Intent(getBaseContext(), MapActivity.class);
                    message = R.string.suscessful_login;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                    break;
                case (1):
                    message = R.string.invalid_data;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            progressBar.setVisibility(View.GONE);
        }
    }

}