package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.login.LoginManager;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class UserProfileActivity extends AppCompatActivity {

    private static final int SELECT_FILE = 1;
    private static final int REQUEST_CAMERA = 0;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private TabLayout tabLayout;
    private ViewPager mViewPager;
    private TextView badge;
    private PorterShapeImageView profilePic;
    private TextView profileFullname, profileEmail, profileUsername;
    private Context context;
    private SharedPreferences pref_session;
    private ToggleButton edit;
    private ImageView camera;
    private int id;
    private String imageName;
    private String username, email;
    private Bitmap image;
    private ProgressBar progressBar;

//    private boolean enable;
    UserProfileGeneralFragment general;
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
        profileEmail = (TextView) findViewById(R.id.emailDisplay);
        profileUsername = (TextView) findViewById(R.id.usernameDisplay);
        badge = (TextView) findViewById(R.id.badgeName);
        edit = (ToggleButton) findViewById(R.id.edit);
        camera = (ImageView) findViewById(R.id.cameraLogo);


        // User
        GetUser g = new GetUser();
        g.execute();

        pref_session = getSharedPreferences("Session", 0);

        email = pref_session.getString("email",null);
        username = pref_session.getString("username",null);
        String fullname = pref_session.getString("fullname",null);
        final String profilepic = pref_session.getString("photo",null);
        Integer game_points = pref_session.getInt("game_points",0);
        String badge_name = pref_session.getString("badge",null);

        id = pref_session.getInt("id",0);
        if (profilepic!=null) {
            Uri photo = Uri.parse(profilepic);
            Picasso.with(context).load(photo).into(profilePic);
        }
        profileFullname.setText(fullname);
        profileEmail.setText(email);
        profileUsername.setText("@"+username);

        String userData = this.getResources().getString(R.string.badge_name, formatter.format(game_points), badge_name);
        CharSequence styledText = Html.fromHtml(userData);
        badge.setText(styledText);

        //Tabs

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        edit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                final EditText[] fields = getFragment().getFields();
                final ArrayList<String> data = new ArrayList<String>();

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
                        Log.d("DATA FIELDS", field.getText().toString());
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
                                        password = "1234";

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
                            //startActivity(getIntent());

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

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        image = (Bitmap) data.getExtras().get("data");
        /*ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assert thumbnail != null;
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        */
        profilePic.setImageBitmap(image);
/*
        image = BitmapFactory.decodeFile(
                Environment.getExternalStorageDirectory()+
                        "/CanopyVerde/profile"+imageName);
        profilePic.setImageBitmap(image);
        //Uri image = () data.getStringExtra(MediaStore.EXTRA_OUTPUT);
        //data.getClipData()
        //Uri image = data.getExtras().get(EXTRA_);
        //Picasso.with(context).load(image).into(profilePic);
*/
    }

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


    public void backToMap(View view){
        onBackPressed();
    }

    public void editProfile (View view){
        //Intent i = new Intent(UserProfileActivity.this, EditProfileActivity.class);
        //startActivity(i);
        //edit.setImageResource(android.R.drawable.ic_menu_save);

    }

    public void gameProfile(View view){
        Intent i = new Intent(UserProfileActivity.this, GameProfileActivity.class);
        startActivity(i);
    }

    public void logout(View view){
        SharedPreferences.Editor session_preferences = getSharedPreferences("Session", 0).edit().clear();
        session_preferences.apply();
        //MapStateManager map_preferences = new MapStateManager(getBaseContext());
        //map_preferences.deletePreferences();
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
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0 : general = new UserProfileGeneralFragment(); //UserProfileGeneralFragment.newInstance(enable);
                //general.setArguments(args);
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

    public UserProfileGeneralFragment getFragment() {
        return general;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // AsyncTask. Sends Log In's data to the server's API and process the response.


    private class PutUser extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection = null;
            Integer result = -1;

            try {

                url = new URL("http://192.168.1.85:8000/profile/"+id+"/");
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
                        Log.d("OK", response.getBody().toString());
                        result = 0;
                    } else if (response.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        Log.d("BAD", "BAD");
                        Log.d("OK", response.getBody().toString());
                        result = 1;
                    } else if (response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) {
                        Log.d("NOT", "FOUND");
                        Log.d("OK", response.getBody().toString());
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
            String message;
            switch (anInt) {
                case (-1):
                    message = "Ha habido un problema conectando con el servidor, intente de nuevo más tarde";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    finish();
                    progressBar.setVisibility(View.GONE);
                    break;
                case (0):

                    message = "¡User Updated!";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    finish();
                    progressBar.setVisibility(View.GONE);
                    break;
                case (1):
                    message = "Invalid Data";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    finish();
                    progressBar.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }


    public class GetUser extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject response_body = new JSONObject();
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL("http://192.168.1.85:8000/profile/"+id+"/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(10000);

                APIResponse response = JSONResponseController.getJsonResponse(urlConnection,true);

                if (response != null) {
                    Log.w("RESPONSE", String.valueOf(response.getBody()));
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
                //Log.d("RESPONSE POST",String.valueOf(response.getInt("status")));

            try {
                if (response.getInt("status") == 0) {
                    int points_j =  response.getInt("game_points");
                    String badge_j = response.getString("badge");
                    String pic = response.getString("profile_pic");
                    String name = response.getString("fullname");
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
                    Toast.makeText(UserProfileActivity.this, "FailtoLoad", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);
        }
    }
}
