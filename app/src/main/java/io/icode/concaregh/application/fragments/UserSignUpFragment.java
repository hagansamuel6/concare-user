package io.icode.concaregh.application.fragments;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import io.icode.concaregh.application.R;
import io.icode.concaregh.application.activities.SignInActivity;
import io.icode.concaregh.application.activities.SignUpActivity;
import io.icode.concaregh.application.activities.Sign_In_Sign_Up_Activity;
import io.icode.concaregh.application.models.Users;
import maes.tech.intentanim.CustomIntent;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserSignUpFragment extends Fragment implements View.OnClickListener{

    View view;

    ConstraintLayout constraintLayout;

    FirebaseAuth mAuth;

    FirebaseUser user;

    Users users;

    DatabaseReference userRef;

    ProgressBar progressBar;

    EditText editTextUsername;
    EditText editTextEmail;
    EditText editTextPassword;
    EditText editTextPhone;

    private AppCompatSpinner spinnerGender;
    private ArrayAdapter<CharSequence> spinnerAdapter;

    Button sign_up_button;

    private String CHANNEL_ID = "notification_channel_id";

    private int notificationId = 0;

    Sign_In_Sign_Up_Activity applicationContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        applicationContext = (Sign_In_Sign_Up_Activity) context;
    }


    public UserSignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_sign_up, container, false);

        constraintLayout = view.findViewById(R.id.constraintLayout);

        // getting reference to ids of views
        editTextUsername =  view.findViewById(R.id.editTextUsername);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextPhone = view.findViewById(R.id.editTextPhone);

        spinnerGender = view.findViewById(R.id.spinnerGender);
        spinnerAdapter = ArrayAdapter.createFromResource(applicationContext, R.array.gender, R.layout.spinner_item_sign_up);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_sign_up);
        spinnerGender.setAdapter(spinnerAdapter);

        sign_up_button = view.findViewById(R.id.sign_up_button);

        progressBar = view.findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        users = new Users();

        // setting onClickListener on this context
        sign_up_button.setOnClickListener(this);

        return view;

    }

    @Override
    public void onClick(View view) {

        // getting text from editText fields
        String username = editTextUsername.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String phone = editTextPhone.getText().toString();

        if(TextUtils.isEmpty(username)){
            YoYo.with(Techniques.Shake).playOn(editTextUsername);
            editTextUsername.setError(getString(R.string.error_empty_username));
        }

        else if(TextUtils.isEmpty(email)){
            YoYo.with(Techniques.Shake).playOn(editTextEmail);
            editTextEmail.setError(getString(R.string.error_empty_email));
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            YoYo.with(Techniques.Shake).playOn(editTextEmail);
            editTextEmail.setError(getString(R.string.email_invalid));
        }

        else if(TextUtils.isEmpty(password)){
            YoYo.with(Techniques.Shake).playOn(editTextPassword);
            editTextPassword.setError(getString(R.string.error_empty_password));
        }

        else if(password.length() < 6){
            YoYo.with(Techniques.Shake).playOn(editTextPassword);
            editTextPassword.setError(getString(R.string.error_password_length));
        }

        else if(phone.length() != 10){
            YoYo.with(Techniques.Shake).playOn(editTextPhone);
            editTextPhone.setError(getString(R.string.phone_invalid));
        }

        else{signUpUser();}

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
                                Snackbar.make(constraintLayout,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }


    // signUp method
    public void signUpUser(){

        // displays the progressBar
        progressBar.setVisibility(View.VISIBLE);

        //gets text from the editTExt fields
        final String email = editTextEmail.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String gender = spinnerGender.getSelectedItem().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();

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

                                                saveUsername();

                                                // Sends a notification to the user after signing up successfully
                                                // Creating an explicit intent for the activity in the app
                                                Intent intent = new Intent(applicationContext, SignInActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                PendingIntent pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0);

                                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(applicationContext, CHANNEL_ID)
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
                                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
                                                notificationManager.notify(notificationId,mBuilder.build());

                                                // Method call to sendVerification
                                                // link to users's email address
                                                //sendVerificationEmail();

                                                // display a success message and verification sent
                                                Snackbar.make(constraintLayout,getString(R.string.sign_up_successful),Snackbar.LENGTH_LONG).show();

                                                // sign out user
                                                // after signing Up successfully
                                                mAuth.signOut();

                                                //clears text Fields
                                                clearTextFields();


                                            }
                                            else {

                                                // display a message if there is an error
                                                Snackbar.make(constraintLayout,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();

                                                // sign out user
                                                mAuth.signOut();

                                            }
                                        }
                                    });



                        }
                        else{
                            // display a message if there is an error
                            Snackbar.make(constraintLayout,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();

                            // sign out user
                            mAuth.signOut();
                        }

                        // dismisses the progressBar
                        progressBar.setVisibility(View.GONE);

                    }
                });


    }

    //clears the textfields
    public void clearTextFields(){
        editTextEmail.setText(null);
        editTextUsername.setText(null);
        editTextPassword.setText(null);
        editTextPhone.setText(null);
    }

}
