package com.idbcgroup.canopyverde;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    // Facebook Sign in
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

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    String id_token = user.getUid();
                    String email = user.getEmail();
                    String fullname = user.getDisplayName();
                    String photo = String.valueOf(user.getPhotoUrl());
                    AttemptLogin l = new AttemptLogin();
                    l.execute(email,id_token, String.valueOf(true),fullname,photo);

                } else {
                    // User is signed out

                }
            }
        };

        // Facebook
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(FacebookException error) {

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
     * @param token user's access token
     */
    private void handleFacebookAccessToken(AccessToken token) {


        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                        }
                    }
                });
    }

    /**
     * This method authenticate a Google user account with Firebase
     * @param account the Google account who will be authenticated
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {


        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

     /**
     * Manage the result of the Intent
     * @param requestCode identifies who's calling the Intent
     * @param resultCode identifies the result of the called Intent
     * @param data the data retrieved from the called Intent
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
     * Invokes the Google Sign in Method
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Execute the login of the Django Server
     * @param view the button to login
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
            l.execute(email_text,password_text, String.valueOf(false));
        }
    }

    /**
     * Shows the user if the filled fields are syntactically correct and
     * @param email_field if the email is valid
     * @param password_field if the password is valid
     * @return boolean that represents if all the fields are valid
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
     * The login with Facebook via custom button using the method of the button provided by Facebook
     * @param view the custom facebook button
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
     * Calls an Intent to UserRegisterActivity
     * @param view the register button of the view
     */
    public void register(View view){
        startActivity(new Intent(LoginActivity.this,UserRegisterActivity.class));
    }

    /**
     * Method of the Calligraphy Library to insert the font family in the context of the Activity
     * @param newBase the new base context of the Activity
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // AsyncTask. Sends
    private class AttemptLogin extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Sends validated
        // integer value ([-1..1):
        // * -1, if an error occurred during the communication
        // * 0, if everything went OK (redirecting to MainActivity and updating SharedPreferences afterwards)
        // * 1, if the credentials provided aren't valid
        @Override
        protected Integer doInBackground(String... strings) {

            Integer result = -1;
            try {
                // Defining and initializing server's communication's variables

                JSONObject user = new JSONObject();
                user.put("email",strings[0]);
                user.put("password",strings[1]);
                user.put("is_social",strings[2]);
                JSONObject profile = new JSONObject();
                if (strings[2].equals("true")) {
                    profile.put("fullname",strings[3]);
                    profile.put("photo",strings[4]);

                }
                else {
                    profile.put("not_social","not_social");
                }
                user.put("social",profile);

                URL url = new URL("https://canopy-verde.herokuapp.com/api-token-auth/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(10000);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(String.valueOf(user));
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
                        return 0;

                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        JSONObject response_body = response.getBody();
                        int error = response_body.getInt("error");
                        if (error == 430)
                            result = 2;
                        else
                            result = 1;
                    } else {

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
                case (2):
                    message = 12345;
                    Toast.makeText(getBaseContext(), "usuario existente", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            progressBar.setVisibility(View.GONE);
        }
    }

}