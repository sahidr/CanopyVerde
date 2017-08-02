package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
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
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.facebook.login.LoginManager;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserProfileActivity extends AppCompatActivity implements
        UserProfileGeneralFragment.OnEditProfileInfo {

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
    private boolean enable = false;

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

        profilePic= (PorterShapeImageView) findViewById(R.id.profilepic);
        profileFullname = (TextView) findViewById(R.id.fullNameDisplay);
        profileEmail = (TextView) findViewById(R.id.emailDisplay);
        profileUsername = (TextView) findViewById(R.id.usernameDisplay);
        badge = (TextView) findViewById(R.id.badgeName);
        edit = (ToggleButton) findViewById(R.id.edit);
        camera = (ImageView) findViewById(R.id.cameraLogo);

        // User

        pref_session = getSharedPreferences("Session", 0);

        String name = pref_session.getString("name",null);
        String email = pref_session.getString("email",null);
        String username = pref_session.getString("username",null);
        String profilepic = pref_session.getString("photo",null);

        if (profilepic!=null) {
            Uri photo = Uri.parse(profilepic);
            Picasso.with(context).load(photo).into(profilePic);
        }

        if (username==null){
            String[] emailParts = email.split("@");
            String user =  emailParts[0];
            profileUsername.setText("@"+user);
        } else {
            profileUsername.setText(username);
        }

        profileFullname.setText(name);
        profileEmail.setText(email);

        float gamepoints = 2544;
        String points = getResources().getString(R.string.badge_name, formatter.format(gamepoints), getString(R.string.u_bagde));
        CharSequence styledText = Html.fromHtml(points);
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
                enable=isChecked;
                if (isChecked) {
                    edit.setChecked(true);
                    camera.setVisibility(View.VISIBLE);
                    profilePic.setColorFilter(ContextCompat.getColor(context,R.color.profile));

                } else {
                    edit.setChecked(false);
                    camera.setVisibility(View.INVISIBLE);
                    profilePic.setColorFilter(ContextCompat.getColor(context,R.color.colorTransparent));
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
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assert thumbnail != null;
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        profilePic.setImageBitmap(thumbnail);
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext()
                        .getContentResolver(), data.getData());
                profilePic.setImageBitmap(bm);
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

    @Override
    public void onProfileChange(String name, String email, String password, String country, String city) {

        Log.d("USER",name);
        Log.d("USER",email);
        Log.d("USER",password);
        Log.d("USER",country);
        Log.d("USER",city);

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
                case 0 : return new UserProfileGeneralFragment();
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
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
