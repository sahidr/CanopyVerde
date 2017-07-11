package com.idbcgroup.canopyverde;

import android.content.Intent;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class CustomCameraActivity extends AppCompatActivity {
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private ImageButton imgBack;
    private ImageButton imgCapture;

    private String data;

    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        imgBack = (ImageButton)findViewById(R.id.imgBack);
        imgCapture = (ImageButton)findViewById(R.id.imgCapture);

//        data = getIntent().getExtras().getString("From");

//        Intent intent = getIntent();
//        message = intent.getStringExtra(MapsActivity.EXTRA_MESSAGE);

        String newString;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                newString= null;
            } else {
                newString= extras.getString("From");
            }
        } else {
            newString= (String) savedInstanceState.getSerializable("From");
        }


        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        //btn to close the application

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imgCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (message == "MAPS") {
                    Intent i = new Intent(CustomCameraActivity.this,GreenPointRegisterActivity.class);
                    startActivity(i);
                } else {
                    finish();
                }*/
                Intent i = new Intent(CustomCameraActivity.this,GreenPointRegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}
