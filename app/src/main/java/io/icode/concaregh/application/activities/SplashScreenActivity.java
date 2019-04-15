package io.icode.concaregh.application.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.icode.concaregh.application.R;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import io.icode.concaregh.application.constants.Constants;
import io.icode.concaregh.application.notifications.Token;
import maes.tech.intentanim.CustomIntent;

import static java.lang.Thread.sleep;

@SuppressWarnings("ALL")
public class SplashScreenActivity extends AppCompatActivity {

    private TextView app_title;

    private TextView watermark;

    private final int SPLASH_SCREEN_DISPLAY_TIME = 4000;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private FirebaseAuth mAuth;

    ProgressBar progressBar;

    ProgressBar progressBar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // method call to check google play services is available on device
        checkPlayServices();

        // getting reference to the first Top progressBar
        progressBar = findViewById(R.id.progressBar);
        // changes color of progressBar to you desired color
        progressBar.getIndeterminateDrawable().setColorFilter(0xff676767,PorterDuff.Mode.MULTIPLY);

        progressBar1 = findViewById(R.id.progressBar1);
        // changes color of progressBar to you desired color
        progressBar1.getIndeterminateDrawable().setColorFilter(0xff676767,PorterDuff.Mode.MULTIPLY);

        // displays the progressBar
        progressBar.setVisibility(View.VISIBLE);

        // displays the progressBar
        progressBar1.setVisibility(View.VISIBLE);

        // firebase instance
        mAuth = FirebaseAuth.getInstance();

        // method call
        runAnimation();

        // update user's device token
        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    @Override
    protected void onResume() {
        super.onResume();
        // update user's device token
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    // checks for availability of Google Play Services
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                //Log.i(TAG, "This device is not supported.");
                // display a toast
                Toast.makeText(SplashScreenActivity.this,"This device is not supported.",Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    // Update currentUser's  device token
    private void updateToken(String token){

        if(mAuth.getCurrentUser() != null){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.TOKENS_REF);
            Token token1 = new Token(token);
            reference.child(mAuth.getCurrentUser().getUid()).setValue(token1);
        }


    }

    // subscribe users of the app to a topic for push notification from firebase console
    private void subScribeToReceiveBroadcast(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.CHAT_REF)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribe);
                        if(!task.isSuccessful()){
                            msg = getString(R.string.msg_subscription_failed);
                        }

                        //Toast.makeText(HomeActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){

            // starts the activity
            startActivity(new Intent(SplashScreenActivity.this,HomeActivity.class));

            // Add a custom animation ot the activity
            CustomIntent.customType(SplashScreenActivity.this,"fadein-to-fadeout");

            // finishes the activity
            finish();
        }
        else{
            // open splash screen first
            splashScreen();
        }
    }

    //class to the handle the splash screen activity
    public void splashScreen() {

        Thread timer = new Thread() {
            @Override
            public void run() {
                try {

                    sleep(SPLASH_SCREEN_DISPLAY_TIME);

                    //Creates and start the intent of the next activity
                    startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));

                    // Add a custom animation ot the activity
                    CustomIntent.customType(SplashScreenActivity.this,"fadein-to-fadeout");

                    // finishes the activity
                    finish(); // this prevents the app from going back to the splash screen

                    super.run();
                }
                catch (InterruptedException e) {
                    // displays a toast
                    Toast.makeText(SplashScreenActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        };
        //starts the timer
        timer.start();
    }

    // method to set animation on textViews
    private  void runAnimation(){

        // setting animation for the App Title on the splashScreen
        TextView app_title = findViewById(R.id.splash_screen_text);

        //add an animation using the YoYo Library
        YoYo.with(Techniques.FlipInX)
                .duration(1000)
                .repeat(1)
                .playOn(app_title);


        // setting animation for the App watermark on the splashScreen
        TextView watermark = findViewById(R.id.water_mark);

        //add an animation using the YoYo Library
        YoYo.with(Techniques.BounceInUp)
                .duration(1000)
                .repeat(1)
                .playOn(watermark);


    }

    @Override
    public void finish() {
        super.finish();
        // Add a custom animation ot the activity
        CustomIntent.customType(SplashScreenActivity.this,"fadein-to-fadeout");
    }
}