package com.idbcgroup.canopyverde;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.widget.Toast.LENGTH_SHORT;

public class EditProfile extends AppCompatActivity {

    private ImageButton editPic;
    private Button save;
    private Button delete;
    private ProgressBar load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editPic = (ImageButton) findViewById(R.id.editpic);
        save = (Button) findViewById(R.id.save);
        delete = (Button) findViewById(R.id.deleteAccount);
        load = (ProgressBar) findViewById(R.id.load);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CharSequence colors[] = new CharSequence[] {"red", "green", "blue", "black"};
                final CharSequence uploadType[] = new CharSequence[] {"Take a Picture","Upload from Gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
                //builder.setTitle("Pick a color");
                builder.setItems(uploadType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                       if (which == 0){
                           Intent i = new Intent(EditProfile.this,CameraActivity.class);
//                           i.putExtra("From","Edit");
                           startActivity(i);
                       }else{
                           Toast.makeText(EditProfile.this,"Gallery",LENGTH_SHORT).show();

                       }
                    }
                });
                builder.show();

            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                    deleteAccount();
                                }
                            });

                        } else {
                            deleteAccount();
                        }

                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

// Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
    }

    public void deleteAccount (){
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

}
