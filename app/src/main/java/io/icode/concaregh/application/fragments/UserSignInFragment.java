package io.icode.concaregh.application.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concaregh.application.R;
import io.icode.concaregh.application.activities.HomeActivity;
import io.icode.concaregh.application.activities.ResetPasswordActivity;
import io.icode.concaregh.application.activities.SignInActivity;
import io.icode.concaregh.application.activities.Sign_In_Sign_Up_Activity;
import io.icode.concaregh.application.models.Users;
import maes.tech.intentanim.CustomIntent;

public class UserSignInFragment extends Fragment implements View.OnClickListener{

    ProgressBar progressBar;

    private EditText editTextEmail;
    private EditText editTextPassword;

    Button forgot_password;
    Button button_sign_in;

    FirebaseAuth mAuth;

    private ConstraintLayout constraintLayout;

    Users users;

    public UserSignInFragment(){
    }

    Sign_In_Sign_Up_Activity applicationContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        applicationContext = (Sign_In_Sign_Up_Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

         View view = inflater.inflate(R.layout.fragment_user_sign_in,container,false);

         constraintLayout = view.findViewById(R.id.constraintLayout);


         // initialization of the objects of the views
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);

        // getting the ids of the views
        button_sign_in =  view.findViewById(R.id.login_button);
        forgot_password = view.findViewById(R.id.forgot_password);

        progressBar = view.findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        users = new Users();

        return view;

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_button:{
                // method call to check login information and login user
                inputValidation();
            }

            case R.id.forgot_password:{
                // method to open reset password activity
                resetPassword();
            }
        }
    }


    private void inputValidation(){

        // getting text from editText fields
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
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

        else{loginUser();}
    }

    // Method to handle user login
    public void loginUser(){

        // shows the progressBar
        progressBar.setVisibility(View.VISIBLE);

        //gets text from the editTExt fields
        final String _email = editTextEmail.getText().toString().trim();
        final String _password = editTextPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(_email,_password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            // display a successful login message
                            Toast.makeText(applicationContext,getString(R.string.login_successful),Toast.LENGTH_SHORT).show();

                            // clear the text fields
                            clearTextFields();

                            // start the home activity
                            startActivity(new Intent(applicationContext,HomeActivity.class));

                            // Add a custom animation ot the activity
                            CustomIntent.customType(applicationContext,"fadein-to-fadeout");

                            // finishes this activity(prevents user from going back to this activity when back button is pressed)
                            applicationContext.finish();

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

    // Method to clear text fields
    public void clearTextFields(){
        editTextEmail.setText(null);
        editTextPassword.setText(null);
    }

    // Forgot password method
    public void resetPassword() {

        // shakes the button when clicked
        YoYo.with(Techniques.FlipOutX).playOn(forgot_password);

        // start the ResetPassword Activity
        startActivity(new Intent(applicationContext,ResetPasswordActivity.class));

        // Add a custom animation ot the activity
        CustomIntent.customType(applicationContext,"fadein-to-fadeout");

        // finish the activity
        applicationContext.finish();
    }

}
