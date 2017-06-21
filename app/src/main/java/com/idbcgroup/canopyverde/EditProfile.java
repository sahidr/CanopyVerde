package com.idbcgroup.canopyverde;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.LENGTH_SHORT;

public class EditProfile extends AppCompatActivity {

    private static final int SELECT_FILE = 1;
    private static final int REQUEST_CAMERA = 0;
    private CircleImageView editPic;
    private Button save;
    private Button delete;
    private ProgressBar load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editPic = (CircleImageView) findViewById(R.id.editpic);
        save = (Button) findViewById(R.id.save);
        delete = (Button) findViewById(R.id.deleteAccount);
        load = (ProgressBar) findViewById(R.id.load);
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
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        editPic.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        editPic.setImageBitmap(bm);
    }

    public void editProfilePicture(View view){
        final CharSequence uploadType[] = new CharSequence[] {"Take a Picture","Upload from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
        builder.setItems(uploadType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    cameraIntent();
                }else{
                    galleryIntent();
                }
            }
        });
        builder.show();
    }

    public void deleteAccount (View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
        builder.setMessage("Estas seguro de querer eliminar la cuenta?");
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
                SharedPreferences.Editor session_preferences = getSharedPreferences("Session", 0).edit().clear();
                session_preferences.apply();
                SharedPreferences.Editor tour_preferences = getSharedPreferences("Tour", 0).edit().clear();
                tour_preferences.apply();
                startActivity(new Intent(EditProfile.this, MainActivity.class)
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
