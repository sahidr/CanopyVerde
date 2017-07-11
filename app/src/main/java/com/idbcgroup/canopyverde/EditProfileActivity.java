package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final int SELECT_FILE = 1;
    private static final int REQUEST_CAMERA = 0;
    private CircleImageView editPic;
    private Button save;
    private ProgressBar load;
    private Switch language, notifications;
    private SharedPreferences pref_session;
    private Context context;
    private Bitmap thumbnail;
    private int request;
    private EditText editName, editUser,editEmail,editPassword,confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        context = this;
        editPic = (CircleImageView) findViewById(R.id.editpic);
        save = (Button) findViewById(R.id.save);
        load = (ProgressBar) findViewById(R.id.load);
        language = (Switch) findViewById(R.id.languageSwitch);

        editName = (EditText) findViewById(R.id.editName);
        editUser = (EditText) findViewById(R.id.editUsername);
        editEmail = (EditText) findViewById(R.id.editEmail);

        pref_session = getSharedPreferences("Session", 0);

        String name = pref_session.getString("name",null);
        String email = pref_session.getString("email",null);
        String username = pref_session.getString("username",null);
        String profilepic = pref_session.getString("photo",null);

        String[] emailParts = username.split("@");
        String user =  emailParts[1];

        if (profilepic!=null) {
            Uri photo = Uri.parse(profilepic);
            Picasso.with(context).load(photo).into(editPic);
        }

        editUser.setText(user);
        editEmail.setText(email);
        editName.setText(name);



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
        super.onActivityResult(requestCode, resultCode, data);

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
        editPic.setImageBitmap(thumbnail);
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext()
                        .getContentResolver(), data.getData());
                editPic.setImageBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void editProfilePicture(View view){
        CharSequence uploadType[] = new CharSequence[] {
                getString(R.string.picture),getString(R.string.gallery) };
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
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

    public void deleteAccount (View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setMessage(R.string.confirm_message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                load.setVisibility(View.VISIBLE);
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseAuth.getInstance().signOut();
                        }
                    });
                }
                SharedPreferences.Editor session_preferences = getSharedPreferences("Session", 0)
                        .edit().clear();
                session_preferences.apply();
                SharedPreferences.Editor tour_preferences = getSharedPreferences("Tour", 0)
                        .edit().clear();
                tour_preferences.apply();
                startActivity(new Intent(EditProfileActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                load.setVisibility(View.INVISIBLE);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}