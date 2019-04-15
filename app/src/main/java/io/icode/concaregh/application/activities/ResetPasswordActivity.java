package io.icode.concaregh.application.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import io.icode.concaregh.application.R;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import maes.tech.intentanim.CustomIntent;

public class ResetPasswordActivity extends AppCompatActivity {

    // class variables
    ProgressBar progressBar;

    CoordinatorLayout coordinatorLayout;

    FirebaseAuth mAuth;

    private EditText editTextEmail;

    // object creation
    private Animation shake;

    private Button btn_reset_password;
    private Button btn_back;

    AdView adView, adView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // firebase instance
        mAuth = FirebaseAuth.getInstance();

        // getting references to the views
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        editTextEmail = findViewById(R.id.email);

        btn_reset_password = findViewById(R.id.btn_reset_password);

        btn_back = findViewById(R.id.btn_back);

        // getting reference to the views
        progressBar = findViewById(R.id.progressBar);

        shake = AnimationUtils.loadAnimation(ResetPasswordActivity.this, R.anim.anim_shake);

        // method call to create banner
        createBanner();

    }

    // method to create banner ad in app
    private void createBanner() {

        MobileAds.initialize(this,getString(R.string.admob_app_id));

        // getting reference to AdView
        adView = findViewById(R.id.adView);
        adView1 = findViewById(R.id.adView1);
        /*
         * Create an ad request.
         */
        AdRequest adRequest = new AdRequest.Builder().build();

        /*
         * Start loading the ad in the background.
         */
        try {
            adView.loadAd(adRequest);
            adView1.loadAd(adRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method to send user back to login Activity
    public void goBackButton(View view) {

        // add an animation to anim_shake the button
        btn_back.setAnimation(shake);

        // starts the Login activity
        startActivity(new Intent(ResetPasswordActivity.this,SignInActivity.class));

        // Add a custom animation ot the activity
        CustomIntent.customType(ResetPasswordActivity.this,"fadein-to-fadeout");

        // finish the activity
        finish();

    }

    // onClick listener method for reset password Button
    public void resetPasswordButton(View view) {

        String email = editTextEmail.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            // set animation and error
            editTextEmail.setAnimation(shake);
            editTextEmail.setError(getString(R.string.email_registered));
            editTextEmail.requestFocus();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // set animation and error
            editTextEmail.setAnimation(shake);
            editTextEmail.setError(getString(R.string.email_valid_registered));
            editTextEmail.requestFocus();
        }
        else{

            // add an animation to anim_shake the button
            btn_reset_password.setAnimation(shake);

            //method call
            resetPassword();

        }

    }

    // method to reset password
    private void resetPassword(){

        // displays the progressBar
        progressBar.setVisibility(View.VISIBLE);

        // getting text from user
        String email = editTextEmail.getText().toString().trim();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            // display successful message along with instructions to reset password
                            Snackbar.make(coordinatorLayout,getString(R.string.reset_password_instruction)
                                    ,Snackbar.LENGTH_LONG).show();
                        }
                        else{
                            // displays an error message
                            Snackbar.make(coordinatorLayout,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();
                        }

                        // dismiss the progressBar
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // starts the Login activity
        startActivity(new Intent(ResetPasswordActivity.this,SignInActivity.class));

        // Add a custom animation ot the activity
        CustomIntent.customType(ResetPasswordActivity.this,"fadein-to-fadeout");

        // finish the activity
        finish();

    }
}
