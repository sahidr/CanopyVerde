package com.idbcgroup.canopyverde;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_SHORT;

public class EditProfile extends AppCompatActivity {

    private ImageButton editPic;
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editPic = (ImageButton) findViewById(R.id.editpic);
        save = (Button) findViewById(R.id.save);

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


    }
}
