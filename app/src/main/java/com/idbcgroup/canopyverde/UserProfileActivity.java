package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.facebook.login.LoginManager;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserProfileActivity extends AppCompatActivity {

    private static final int SELECT_FILE = 1;
    private static final int REQUEST_CAMERA = 0;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private TabLayout tabLayout;
    private TextView badge;
    private PorterShapeImageView profilePic;
    private TextView profileFullname;
    private Context context;
    private ToggleButton edit;
    private ImageView camera;
    private int id;
    private String username;
    private Bitmap image;
    private ProgressBar progressBar;

//    private boolean enable;
private UserProfileGeneralFragment general;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/TitilliumWeb-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        context = this;
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ITALIAN);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        profilePic = (PorterShapeImageView) findViewById(R.id.profilepic);
        profileFullname = (TextView) findViewById(R.id.fullNameDisplay);
        TextView profileEmail = (TextView) findViewById(R.id.emailDisplay);
        TextView profileUsername = (TextView) findViewById(R.id.usernameDisplay);
        badge = (TextView) findViewById(R.id.badgeName);
        edit = (ToggleButton) findViewById(R.id.edit);
        camera = (ImageView) findViewById(R.id.cameraLogo);

        // User
        GetUser profile = new GetUser();
        profile.execute();

        SharedPreferences pref_session = getSharedPreferences("Session", 0);

        String email = pref_session.getString("email", null);
        username = pref_session.getString("username",null);
        String fullname = pref_session.getString("fullname",null);
        final String user_pic = pref_session.getString("photo",null);
        Integer game_points = pref_session.getInt("game_points",0);
        String badge_name = pref_session.getString("badge",null);

        id = pref_session.getInt("id",0);
        if (user_pic!=null) {
            Uri photo = Uri.parse(user_pic);
            Picasso.with(context).load(photo).into(profilePic);
        }
        profileFullname.setText(fullname);
        profileEmail.setText(email);
        profileUsername.setText("@"+username);

        String userData = this.getResources().getString(R.string.badge_name, formatter.format(game_points), badge_name);
        // The fromHtml method is used to process the underlined text of the string resource
        CharSequence styledText = Html.fromHtml(userData);
        badge.setText(styledText);

        //Tabs

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        edit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                final EditText[] fields = getFragment().getFields();
                final ArrayList<String> data = new ArrayList<>();

                if (isChecked) {
                    // Edit enable
                    // Can take picture and edit data

                    edit.setChecked(true);
                    camera.setVisibility(View.VISIBLE); // Take picture enable
                    profilePic.setColorFilter(ContextCompat.getColor(context,R.color.profile));
                    for (EditText field : fields) {
                        field.setEnabled(true);
                    }
                } else {
                    // Edit Disable Save data enable
                    // Form disable

                    edit.setChecked(false);
                    camera.setVisibility(View.INVISIBLE); // Take picture disable
                    profilePic.setColorFilter(ContextCompat.getColor(context,R.color.colorTransparent));

                    for (EditText field : fields) {
                        field.setEnabled(false);
                        data.add(String.valueOf(field.getText()));

                    }


                    final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Save Content?");//context.getResources().getString(R.string.gps_network_not_enabled));
                    //dialog.setMessage(context.getResources().getString(R.string.enable_location));
                    dialog.setPositiveButton(context.getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    //Load data into server

                                    PutUser p = new PutUser();
                                    String fullname = data.get(0);
                                    String email = data.get(1);
                                    String password = data.get(2);

                                    if (password.length()< 8)
                                        password = "user_default_password_key";

                                    String country = data.get(3);
                                    String city = data.get(4);
                                    if (image != null) {
                                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                        byte[] byte_arr = bytes.toByteArray();
                                        String image = Base64.encodeToString(byte_arr, 0);
                                        p.execute(fullname, email, password ,country, city ,image);
                                    } else
                                        p.execute(fullname, email, password ,country, city ,null);
                                }
                            });
                    dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int paramInt) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    });
                    dialog.show();
                }
            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        changeTabsFont();
    }

    /**
     * Method that changes the font family of the text in the tabs
     * Makes an iteration in the tabs of the tab layout
     * then takes the view child of the tabs and cast it into a TextView
     * and applies the new font
     */
    private void changeTabsFont() {
        Typeface tf = Typeface.createFromAsset(UserProfileActivity.this.getAssets(), "fonts/TitilliumWeb-Regular.ttf");
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(tf);
                }
            }
        }
    }

    /**
     * Obtains a picture of the device's gallery
     */
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    /**
     * Method that calls the Camera App of the device
     */
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent, REQUEST_CAMERA);
    }

    /**
     * Result of the Camera app or Successful point Register or Update
     * @param requestCode identifies who's calling the Intent
     * @param resultCode identifies the result of the called Intent
     * @param data the data retrieved from the called Intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    /**
     * Cast the data of the intent as a Bitmap and put it onto a ImageView
     * @param data the bitmap result taken by the camera
     */
    private void onCaptureImageResult(Intent data) {
        image = (Bitmap) data.getExtras().get("data");
        profilePic.setImageBitmap(image);
    }

    /**
     * Cast the data of the intent as a Bitmap and put it onto a ImageView
     * @param data the data obtained by the gallery
     */
    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            try {
                image = MediaStore.Images.Media.getBitmap(getApplicationContext()
                        .getContentResolver(), data.getData());
                profilePic.setImageBitmap(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Builds an Alert dialog that allows the user to choose an image the camera or the gallery
     * @param view the profile picture as a PortableShapeImageView
     */
    public void editProfilePicture(View view){
        CharSequence uploadType[] = new CharSequence[] {
                getString(R.string.picture),getString(R.string.gallery) };
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setItems(uploadType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int select) {
                if (select == 0){
                    cameraIntent();
                }else{
                    galleryIntent();
                }
            }
        });
        builder.show();
    }

    /**
     * Returns to the MapActivity
     * @param view the map button of the view
     */
    public void backToMap(View view){
        onBackPressed();
    }

    /**
     * Go to the User's Game Profile
     * @param view the link with the user's accumulated points
     */
    public void gameProfile(View view){
        Intent i = new Intent(UserProfileActivity.this, GameProfileActivity.class);
        startActivity(i);
    }

    /**
     * Close the session of the user and delete all the data in the Shared Preferences
     * @param view the logout button of the view
     */
    public void logout(View view){
        SharedPreferences.Editor session_preferences = getSharedPreferences("Session", 0).edit().clear();
        session_preferences.apply();
        MapStateManager map_preferences = new MapStateManager(getBaseContext());
        map_preferences.deletePreferences();
        Intent i = new Intent(UserProfileActivity.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        finish();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0 : general = new UserProfileGeneralFragment();
                return general;
                case 1 : return new UserProfileReportFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.general);
                case 1:
                    return getString(R.string.report);
            }
            return null;
        }
    }

    /**
     * Obtains the User's Profile Fragment for future processing of the Views
     * @return The fragment of the UserProfileGeneral
     */
    private UserProfileGeneralFragment getFragment() {
        return general;
    }

    /**
     * Method of the Calligraphy Library to insert the font family in the context of the Activity
     * @param newBase the new base context of the Activity
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // AsyncTask. get user's data to the server's API and process the response.
    private class PutUser extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection;
            Integer result = -1;

            try {

                url = new URL("https://canopy-verde.herokuapp.com/profile/"+id+"/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("PATCH");

                JSONObject user = new JSONObject();
                JSONObject profile = new JSONObject();

                user.put("username", username);
                user.put("email", strings[1]);
                user.put("password", strings[2]);
                profile.put("fk_user", user);
                profile.put("fullname", strings[0]);
                profile.put("country", strings[3]);
                profile.put("city", strings[4]);
                if (strings[5]!= null)
                    profile.put("profile_pic",strings[5]);

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(profile.toString());
                writer.flush();

                APIResponse response = JSONResponseController.getJsonResponse(urlConnection, true);

                if (response != null) {
                    if (response.getStatus() == HttpURLConnection.HTTP_OK || response.getStatus() == HttpURLConnection.HTTP_CREATED) {

                        result = 0;
                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {

                        if (response.getBody().getString("code").equals("email"))
                            result = 1;
                        else
                            result = 2;
                    } else if (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                        result = -1;
                    }
                }
            } catch (Exception e) {
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
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    break;
                case (0):
                    message = R.string.user_updated;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case (1):
                    message = R.string.invalid_email;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    break;
                case (2):
                    message = R.string.invalid_password;
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
            progressBar.setVisibility(View.GONE);
        }
    }


    /**
     * Obtains the User's profile data
     */
    private class GetUser extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject response_body = new JSONObject();
            URL url;
            HttpURLConnection urlConnection;
            try {
                url = new URL("https://canopy-verde.herokuapp.com/profile/"+id+"/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(10000);

                APIResponse response = JSONResponseController.getJsonResponse(urlConnection,true);

                if (response != null) {
                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                        response_body = response.getBody();
                        response_body.put("status",0);
                    } else {
                        response_body.put("status",1);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response_body;
        }

        // Process doInBackground() results
        @Override
        protected void onPostExecute(JSONObject response) {
            try {
                if (response.getInt("status") == 0) {
                    int points_j =  response.getInt("game_points");
                    String badge_j = response.getString("badge");
                    String pic = response.getString("profile_pic");
                    String name = response.getString("fullname");

                    //In case of Latin American format of the numbers use the Locate Italian
                    NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ITALIAN);
                    String userData = getResources().getString(R.string.badge_name, formatter.format(points_j), badge_j);

                    CharSequence styledText = Html.fromHtml(userData);
                    badge.setText(styledText);
                    profileFullname.setText(name);
                    Picasso.with(UserProfileActivity.this).load(pic).into(profilePic);

                    SharedPreferences.Editor editor = getSharedPreferences("Session", 0).edit();
                    editor.putString("badge",badge_j);
                    editor.putInt("game_points",points_j);
                    editor.putString("fullname",name);
                    editor.putString("photo",pic);
                    editor.apply();

                }else {
                    Toast.makeText(UserProfileActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);
        }
    }
}
