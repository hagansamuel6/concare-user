package io.icode.concaregh.application.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
//import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import io.icode.concaregh.application.R;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concaregh.application.models.Users;
import maes.tech.intentanim.CustomIntent;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class EditProfileActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks{

    AdView adView;

    // class variables
    private CircleImageView circleImageView;
    private EditText username;

    Uri uriProfileImage;

    String profileImageUrl;

    ProgressBar progressBar;

    ProgressBar progressBar1;

    ConstraintLayout constraintLayout;

    FirebaseAuth mAuth;

    FirebaseUser currentUser;

    DatabaseReference userRef;

    Users users;

    private static final int PERMISSION_CODE = 124;

    private static final int  REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.icode.concaregh.application.R.layout.activity_edit_profile);

        constraintLayout = findViewById(R.id.constraintLayout);

        circleImageView = findViewById(R.id.circularImageView);
        username = findViewById(R.id.editTextUsername);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(getString(R.string.title_edit_profile));
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        users = new Users();

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        progressBar = findViewById(R.id.progressBar);

        progressBar1 = findViewById(R.id.progressBar1);

        // a method call to the chooseImage method
        chooseImage();

        // method call to create banner
        createBanner();

    }

    // method to create banner ad in app
    private void createBanner() {

        MobileAds.initialize(this,getString(R.string.admob_app_id));

        // getting reference to AdView
        adView = findViewById(R.id.adView);
        /*
         * Create an ad request.
         */
        AdRequest adRequest = new AdRequest.Builder().build();

        /*
         * Start loading the ad in the background.
         */
        try {
            adView.loadAd(adRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method to select image from gallery
    public void chooseImage(){

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Adds a custom animation to the view using Library
                YoYo.with(Techniques.FlipInX).playOn(circleImageView);

                // method to open user's phone gallery
                openGallery();
            }
        });

    }

    // another method to create a gallery intent to choose image from gallery
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openGallery(){

        String[] perms = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        // checks if permission is already enabled
        if(EasyPermissions.hasPermissions(EditProfileActivity.this,perms)){
            // opens camera if permission has been granted already
            Intent intentPick = new Intent();
            intentPick.setType("image/*");
            intentPick.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intentPick,"Select Profile Picture"), REQUEST_CODE);

            // add a fading animation when opening gallery
            CustomIntent.customType(EditProfileActivity.this,"fadein-to-fadeout");
        }
        else{
            EasyPermissions.requestPermissions(EditProfileActivity.this,
                    getString(R.string.text_permission_select_profile), PERMISSION_CODE,perms);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriProfileImage = data.getData();

            try {
                // sets the picked image to the imageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uriProfileImage);
                circleImageView.setImageBitmap(bitmap);
                uploadImage();

            } catch (IOException e) {
                Snackbar.make(constraintLayout,e.getMessage(),Snackbar.LENGTH_LONG).show();
            }

        }

    }

    // method to upload image to database
    private void uploadImage(){

        final StorageReference profileImageRef = FirebaseStorage.getInstance()
                .getReference("Users Profile Pictures/" + currentUser.getDisplayName()
                        + "/" + System.currentTimeMillis() + ".jpg");

        if(uriProfileImage != null){
            // displays the progressBar
            progressBar.setVisibility(View.VISIBLE);

            profileImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            profileImageRef.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                          Uri downloadUrl = uri;
                                          /*  sets the ProfileImageUrl and setImageUrl
                                            ** method of the Users class to the URL and
                                            */
                                          profileImageUrl = downloadUrl.toString();
                                        }
                                    });

                            // hides progressBar
                            progressBar.setVisibility(View.GONE);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // hides progressBar
                    progressBar.setVisibility(View.GONE);
                    // display error message
                    Snackbar.make(constraintLayout,e.getMessage(),Snackbar.LENGTH_LONG).show();
                }
            });
        }

    }

    // save button listener
    public void onUpdateButtonClick(View view) {
        saveUserInfo(); // method call
    }

    // method to save username and profile image
    private void saveUserInfo(){

        // getting text from editText
        final String _username = username.getText().toString().trim();

        // checks if the field is not left empty
        if(_username.isEmpty()) {
            YoYo.with(Techniques.Shake).playOn(username);
            username.setError(getString(R.string.error_empty_field));
            username.requestFocus();
            return;
        }

        // progressBar for update Button
        progressBar1.setVisibility(View.VISIBLE);

        if(currentUser != null && profileImageUrl != null){
            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(_username)
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();

            // updates user info with the passed username and image
            currentUser.updateProfile(userProfileChangeRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
                                // updating these fields if task is successful
                                HashMap<String, Object> updateInfo = new HashMap<>();
                                updateInfo.put("imageUrl",profileImageUrl);
                                updateInfo.put("username", _username);
                                userRef.updateChildren(updateInfo);

                                // display a success message
                                Toast.makeText(EditProfileActivity.this,"Profile Updated Successfully",Toast.LENGTH_LONG).show();
                            }
                            else{
                                // display an error message
                                Snackbar.make(constraintLayout,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();
                            }

                            // dismiss progress bar
                            progressBar1.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    // dismiss progress bar
                    progressBar1.setVisibility(View.GONE);

                    // display an error message
                    Snackbar.make(constraintLayout,e.getMessage(),Snackbar.LENGTH_LONG).show();

                }
            });
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:

                // finishes the current activity
                finish();

                default:
                    break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // finishes the current activity
        finish();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}
