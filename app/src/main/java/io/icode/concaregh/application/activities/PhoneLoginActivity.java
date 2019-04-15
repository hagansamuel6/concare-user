package io.icode.concaregh.application.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import io.icode.concaregh.application.R;

public class PhoneLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
    }

    // onClickListener for button to handle sending of verification code
    public void onContinueButtonClick(View view) {

    }
}
