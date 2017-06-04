package com.idbcgroup.canopyverde;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class PasswordRestoreActivity extends AppCompatActivity {

    private Button recover;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_restore);

        recover = (Button) findViewById(R.id.recover);
        back = (ImageButton) findViewById(R.id.back);
    }

    public void backToLogin (View view){

        startActivity(new Intent(PasswordRestoreActivity.this,LoginActivity.class));
        finish();
    }


}
