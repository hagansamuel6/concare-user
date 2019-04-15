package io.icode.concaregh.application.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.icode.concaregh.application.R;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concaregh.application.constants.Constants;
import io.icode.concaregh.application.models.Admin;
import io.icode.concaregh.application.models.Groups;
import io.icode.concaregh.application.models.Users;
import maes.tech.intentanim.CustomIntent;

public class SignUpActivity extends AppCompatActivity {


    // instance variables
    //private CircleImageView circleImageView;
    private EditText editTextEmail;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextPhoneNumber;

    Button ButtonSignUp;
    Button ButtonLoginLink;

    private AppCompatSpinner spinnerGender;
    private ArrayAdapter<CharSequence> spinnerAdapter;

    private Animation shake;

    private NestedScrollView nestedScrollView;

    //instance of firebase Authentication
    FirebaseAuth mAuth;

    FirebaseDatabase userdB;

    DatabaseReference userRef;

    DatabaseReference adminRef;

    DatabaseReference groupRef;

    DatabaseReference genderGroupRef;

    DatabaseReference allUsersGroupRef;

    //FirebaseStorage storage;

    // progressBar to load image uploading to database
    //ProgressBar progressBar;

    // progressBar to load signUp user
    ProgressBar progressBar1;

    private CircleImageView circleImageView;

    private CircleImageView app_logo;

    Uri uriProfileImage;

    String profileImageUrl;

    Users users;

    Admin admin;

    Groups my_groups;

    private static final int  REQUEST_CODE = 1;

    private String CHANNEL_ID = "notification_channel_id";

    private int notificationId = 0;

    // string variable to store the time and date at which a group was created
    String currentDate;

    String currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nestedScrollView = findViewById(R.id.nestedScrollView);

        // line to enable sending of sms
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //initialization of the view objects
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);

        ButtonSignUp = findViewById(R.id.buttonSignUp);
        ButtonLoginLink = findViewById(R.id.buttonLoginLink);

        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner_item_sign_up);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_sign_up);
        spinnerGender.setAdapter(spinnerAdapter);

        //circleImageView = findViewById(R.id.circularImageView);

        mAuth = FirebaseAuth.getInstance();

        users = new Users();

        admin = new Admin();

        my_groups = new Groups();

        userdB = FirebaseDatabase.getInstance();

        userRef = userdB.getReference("Users");

        adminRef = FirebaseDatabase.getInstance().getReference("Admin");

        genderGroupRef = FirebaseDatabase.getInstance().getReference(Constants.GROUP_REF);

        allUsersGroupRef = FirebaseDatabase.getInstance().getReference(Constants.GROUP_REF);

        progressBar1 = findViewById(R.id.progressBar1);
        // sets a custom color on progressBar
        //progressBar1.getIndeterminateDrawable().setColorFilter(0xFE5722,PorterDuff.Mode.MULTIPLY);

        shake = AnimationUtils.loadAnimation(SignUpActivity.this, R.anim.anim_shake);

        app_logo = findViewById(R.id.circularImageView);

        // a method call to the chooseImage method
        //chooseImage();

        animateLogo();

        // method call to get the current time and data
        getTimeAndDate();

    }

    // method to animate the app logo
    public void animateLogo(){
        // animate Logo when imageView is clicked
        app_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.RotateIn).playOn(app_logo);
            }
        });
    }

    //Sign Up Button Method
    public void onSignUpButtonClick(View view){

        //gets text from the editTExt fields
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String phone = editTextPhoneNumber.getText().toString().trim();

        /*
         * Input validation
         */
        if(TextUtils.isEmpty(email)){
            editTextEmail.clearAnimation();
            editTextEmail.startAnimation(shake);
            editTextEmail.setError(getString(io.icode.concaregh.application.R.string.error_empty_email));
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.clearAnimation();
            editTextEmail.startAnimation(shake);
            editTextEmail.setError(getString(io.icode.concaregh.application.R.string.email_invalid));
        }
        else if(TextUtils.isEmpty(username)) {
            editTextUsername.clearAnimation();
            editTextUsername.startAnimation(shake);
            editTextUsername.setError(getString(io.icode.concaregh.application.R.string.error_empty_username));
            editTextUsername.requestFocus();
        }
        else if(TextUtils.isEmpty(password)){
            editTextPassword.clearAnimation();
            editTextPassword.startAnimation(shake);
            editTextPassword.setError(getString(io.icode.concaregh.application.R.string.error_empty_password));
            editTextPassword.requestFocus();
        }
        else if(password.length() < 6 ){
            editTextPassword.clearAnimation();
            editTextPassword.startAnimation(shake);
            editTextPassword.setError(getString(R.string.error_password_length));
            editTextPassword.requestFocus();
        }
        else if(TextUtils.isEmpty(phone)){
            editTextPhoneNumber.clearAnimation();
            editTextPhoneNumber.startAnimation(shake);
            editTextPhoneNumber.setError(getString(R.string.error_empty_phone));
            editTextPhoneNumber.requestFocus();
        }
        else if(phone.length() != 10){
            editTextPhoneNumber.clearAnimation();
            editTextPhoneNumber.startAnimation(shake);
            editTextPhoneNumber.setError(getString(R.string.phone_invalid));
            editTextPhoneNumber.requestFocus();
        }
        else{
            //method call
            signUp();
        }

    }

    // method to save username and profile image
    private void saveUsername(){

        String _username = editTextUsername.getText().toString().trim();

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(_username)
                    .build();

            // updates user info with the passed username and image
            user.updateProfile(userProfileChangeRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                // dismiss progress bar
                                //progressBar1.setVisibility(View.GONE);
                            }
                            else{
                                // display an error message
                                Snackbar.make(nestedScrollView,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
            }

    }

    // signUp method
    public void signUp(){

        // displays the progressBar
        progressBar1.setVisibility(View.VISIBLE);

        //gets text from the editTExt fields
        final String email = editTextEmail.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String gender = spinnerGender.getSelectedItem().toString().trim();
        final String phone = editTextPhoneNumber.getText().toString().trim();

        // register user details into firebase database
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            final FirebaseUser user = mAuth.getCurrentUser();

                            assert user != null;

                            users.setEmail(email);
                            users.setUsername(username);
                            users.setGender(gender);
                            users.setPhoneNumber(phone);
                            users.setUid(user.getUid());
                            users.setImageUrl("");
                            users.setStatus("offline"); // set status to offline by default
                            users.setSearch(username.toLowerCase());

                            userRef.child(user.getUid()).setValue(users)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        // save username
                                        saveUsername();

                                        // method to add user to male or female broadcast group based on gender
                                        addUserToGenderGroups();

                                        // method to add user to "All Users" broadcast group
                                        addUserToAllUsersGroup();

                                        // Sends a notification to the user after signing up successfully
                                        // Creating an explicit intent for the activity in the app
                                        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(SignUpActivity.this, 0, intent, 0);

                                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(SignUpActivity.this, CHANNEL_ID)
                                                .setSmallIcon(R.mipmap.app_logo_round)
                                                .setContentTitle(getString(R.string.app_name))
                                                .setContentText("Sign Up Successful" + " -> " + "(" + username + ")" + "." +
                                                        " Please proceed to login and " + "Thank you for joining us!")

                                                .setStyle(new NotificationCompat.BigTextStyle()
                                                .bigText("Sign Up Successful" + " -> " + "(" + username + ")" + "." +
                                                        " Please proceed to login and " + "Thank you for joining us!"))
                                                // Set the intent that will fire when the user taps the notification
                                                .setWhen(System.currentTimeMillis())
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                                .setContentIntent(pendingIntent)
                                                .setAutoCancel(true);
                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SignUpActivity.this);
                                        notificationManager.notify(notificationId,mBuilder.build());

                                        // sends a message to admin after user signs up
                                        sendSMSMessageToAdmin();

                                        // display a success message and verification sent
                                        Snackbar.make(nestedScrollView,getString(R.string.sign_up_successful),Snackbar.LENGTH_LONG).show();

                                        // sign out user
                                        // after signing Up successfully
                                        mAuth.signOut();

                                        //clears text Fields
                                        clearTextFields();

                                        // schedules to navigate user back to login page in 3 secs
                                        Timer timer = new Timer();
                                        timer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                // start the login Activity after Sign Up is successful
                                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));

                                                // Add a custom animation ot the activity
                                                CustomIntent.customType(SignUpActivity.this,getString(R.string.fadein_to_fadeout));

                                                // finish the activity
                                                finish();

                                            }
                                        },3000);


                                    }
                                    else {

                                        // display a message if there is an error
                                        Snackbar.make(nestedScrollView,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();

                                        // sign out user
                                        mAuth.signOut();

                                    }
                                }
                            });



                        }
                        else{
                            // display a message if there is an error
                            Snackbar.make(nestedScrollView,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();

                            // sign out user
                            mAuth.signOut();
                        }

                        // dismisses the progressBar
                        progressBar1.setVisibility(View.GONE);

                    }
                });


    }

    // Method to add new user to male or female group based on the gender of the user
    private void addUserToGenderGroups(){

        final String gender = spinnerGender.getSelectedItem().toString().trim();

        final FirebaseUser user = mAuth.getCurrentUser();

        assert user != null;

        // adding user to group based on gender
        // adding user to group if user is a male
        groupRef = FirebaseDatabase.getInstance()
                .getReference(Constants.GROUP_REF).child(gender)
                .child("groupMembersIds");

        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Long my_num = dataSnapshot.getChildrenCount();

                int my_id = Integer.parseInt(my_num.toString());

                String userPositionId = String.valueOf(my_id);

                // code to add user to respective gender type group
                genderGroupRef.child(gender)
                        .child("groupMembersIds").child(userPositionId)
                        .setValue(user.getUid());

                // sign out user
                mAuth.signOut();

                return;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Snackbar.make(nestedScrollView,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });


    }

    // Method to add new user to all users group irrespective of the gender of the user
    private void addUserToAllUsersGroup(){

        final FirebaseUser user = mAuth.getCurrentUser();

        assert user != null;

        // code to add user to "All Users" group
        groupRef =  FirebaseDatabase.getInstance()
                .getReference(Constants.GROUP_REF).child(Constants.GROUP_ALL_USERS)
                .child("groupMembersIds");

        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // get total count of uids in list of groupMembers
                Long _my_num = dataSnapshot.getChildrenCount();

                int _my_id = Integer.parseInt(_my_num.toString());

                String _userPositionId = String.valueOf(_my_id);

                // code to add user to "All Users" group
                allUsersGroupRef.child(Constants.GROUP_ALL_USERS)
                        .child("groupMembersIds").child(_userPositionId)
                        .setValue(user.getUid());

                // sign out user
                mAuth.signOut();

                return;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Snackbar.make(nestedScrollView,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }

    // gets the current date and time
    private void getTimeAndDate(){

        // gets the current date
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new  SimpleDateFormat("MMM dd,yyyy");
        currentDate = currentDateFormat.format(calendarDate.getTime());

        // get the current time
        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calendarTime.getTime());

    }

    // Method to send sms to admin(icode)
    // after user signs up
    private void sendSMSMessageToAdmin(){

        //gets text from the editTExt fields
        String user_email = editTextEmail.getText().toString().trim();
        String user_name = editTextUsername.getText().toString().trim();
        String phone_number = editTextPhoneNumber.getText().toString().trim();

        String username = "zent-concare";
        // password that is to be used along with username

        String password = "concare1";
        // Message content that is to be transmitted

        String message =  user_name + " has signed up as a new user on ConCare GH app on " + currentDate + " at " + currentTime;

        /**
         * What type of the message that is to be sent
         * <ul>
         * <li>0:means plain text</li>
         * <li>1:means flash</li>
         * <li>2:means Unicode (Message content should be in Hex)</li>
         * <li>6:means Unicode Flash (Message content should be in Hex)</li>
         * </ul>
         */
        String type = "0";
        /**
         * Require DLR or not
         * <ul>
         * <li>0:means DLR is not Required</li>
         * <li>1:means DLR is Required</li>
         * </ul>
         */
        String dlr = "1";
        /**
         * Destinations to which message is to be sent For submitting more than one

         * destination at once destinations should be comma separated Like
         * 91999000123,91999000124
         */

        // getting mobile number from EditText
        String destination = "233209062445";
        //String destination = "233209062445";

        // Sender Id to be used for submitting the message
        String source = getString(R.string.app_name);

        // To what server you need to connect to for submission
        final String server = "rslr.connectbind.com";

        // Port that is to be used like 8080 or 8000
        int port = 8080;

        try {
            // Url that will be called to submit the message
            URL sendUrl = new URL("http://" + server + ":" + port + "/bulksms/bulksms?");
            HttpURLConnection httpConnection = (HttpURLConnection) sendUrl
                    .openConnection();
            // This method sets the method type to POST so that
            // will be send as a POST request
            httpConnection.setRequestMethod("POST");
            // This method is set as true wince we intend to send
            // input to the server
            httpConnection.setDoInput(true);
            // This method implies that we intend to receive data from server.
            httpConnection.setDoOutput(true);
            // Implies do not use cached data
            httpConnection.setUseCaches(false);
            // Data that will be sent over the stream to the server.
            DataOutputStream dataStreamToServer = new DataOutputStream( httpConnection.getOutputStream());
            dataStreamToServer.writeBytes("username="
                    + URLEncoder.encode(username, "UTF-8") + "&password="
                    + URLEncoder.encode(password, "UTF-8") + "&type="
                    + URLEncoder.encode(type, "UTF-8") + "&dlr="
                    + URLEncoder.encode(dlr, "UTF-8") + "&destination="
                    + URLEncoder.encode(destination, "UTF-8") + "&source="
                    + URLEncoder.encode(source, "UTF-8") + "&message="
                    + URLEncoder.encode(message, "UTF-8"));
            dataStreamToServer.flush();
            dataStreamToServer.close();
            // Here take the output value of the server.
            BufferedReader dataStreamFromUrl = new BufferedReader( new InputStreamReader(httpConnection.getInputStream()));
            String dataFromUrl = "", dataBuffer = "";
            // Writing information from the stream to the buffer
            while ((dataBuffer = dataStreamFromUrl.readLine()) != null) {
                dataFromUrl += dataBuffer;
            }
/**
 * Now dataFromUrl variable contains the Response received from the
 * server so we can parse the response and process it accordingly.
 */
            dataStreamFromUrl.close();
            System.out.println("Response: " + dataFromUrl);
            //Toast.makeText(context, dataFromUrl, Toast.LENGTH_SHORT).show();
        }
        catch (Exception ex) {
            Toast.makeText(SignUpActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    // Method to send verification link to email to user after sign Up
    private void sendVerificationEmail(){

        FirebaseUser user = mAuth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    /* sign user out
                      after verification
                      link is sent successfully
                     */
                    mAuth.signOut();

                }
                else {

                    /* sign user out
                       after verification
                       link is sent successfully
                     */
                    mAuth.signOut();

                    // display error message
                    Snackbar.make(nestedScrollView,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();

                }
            }
        });

    }

    //link from the Sign Up page to the Login Page
    public void onLoginLinkButtonClick(View view){

        // starts this activity
        startActivity(new Intent(SignUpActivity.this,SignInActivity.class));

        // Add a custom animation ot the activity
        CustomIntent.customType(SignUpActivity.this,"fadein-to-fadeout");

        // finish activity
        finish();

    }

    //clears the textfields
    public void clearTextFields(){
        editTextEmail.setText(null);
        editTextUsername.setText(null);
        editTextPassword.setText(null);
        editTextPhoneNumber.setText(null);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // starts this activity
        startActivity(new Intent(SignUpActivity.this,SignInActivity.class));

        // Add a custom animation ot the activity
        CustomIntent.customType(SignUpActivity.this,getString(R.string.fadein_to_fadeout));

        // finishes the activity
        finish();

    }
}
