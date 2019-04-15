package io.icode.concaregh.application.activities;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.icode.concaregh.application.R;
import io.icode.concaregh.application.models.Orders;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import io.icode.concaregh.application.models.Users;
import maes.tech.intentanim.CustomIntent;

@SuppressWarnings("ALL")
public class PlaceOrderActivity extends AppCompatActivity {

    AdView adView;

    AdView adView1;

    // animation class
    Animation shake;

    //objects of the View Classes
    EditText editTextPhoneNumber;
    private EditText editTextHostelName;
    private EditText editTextRoomNumber;

    private EditText editTextOtherLocation;
    private EditText editTextOtherContraceptive;

    //user's campus spinner view object
    private AppCompatSpinner spinnerCampus;
    private ArrayAdapter<CharSequence> arrayAdapterCampus;

    //user's location spinner view object
    private AppCompatSpinner spinnerLocation;
    private ArrayAdapter<CharSequence> arrayAdapterLocation;

    //user's residence spinner view object
    private AppCompatSpinner spinnerResidence;
    private ArrayAdapter<CharSequence> arrayAdapterResidence;

    //spinner for contraceptive type and its arrayAdapter
    private AppCompatSpinner spinnerContraceptive;
    private ArrayAdapter<CharSequence> arrayAdapterContraceptive;

    Button make_payment;
    Button cancel_payment;

    //objects of the classes
    private Orders orders;

    @SuppressWarnings("unused")
    private ProgressBar progressBar;

    private ProgressDialog progressDialog;

    private NestedScrollView nestedScrollView;

    private LinearLayout button_layout;

    FirebaseAuth mAuth;

    Users users;

    //textInput layouts
    private TextInputLayout textInputLayoutOtherLocation;
    //private TextInputLayout textInputLayoutOtherContraceptive;

    private String CHANNEL_ID = "notification_channel_id";

    private int notificationId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Place Order");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        shake = AnimationUtils.loadAnimation(PlaceOrderActivity.this, R.anim.anim_shake);

        nestedScrollView = findViewById(R.id.nestedScrollView);

        button_layout = findViewById(R.id.button_layout);

        editTextPhoneNumber = findViewById(R.id.editTextMobile);
        editTextHostelName = findViewById(R.id.editTextHostel);
        editTextRoomNumber = findViewById(R.id.editTextRoomNum);
        editTextOtherLocation = findViewById(R.id.editTextOtherLocation);
        editTextOtherContraceptive = findViewById(R.id.editTextOtherContraceptive);

        //spinner view campus
        spinnerCampus = findViewById(R.id.spinnerCampus);
        arrayAdapterCampus = ArrayAdapter.createFromResource(this, R.array.campus, R.layout.spinner_item);
        arrayAdapterCampus.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCampus.setAdapter(arrayAdapterCampus);

        spinnerLocation = findViewById(R.id.spinnerLocation);
        arrayAdapterLocation = ArrayAdapter.createFromResource(this, R.array.location, R.layout.spinner_item);
        arrayAdapterLocation.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerLocation.setAdapter(arrayAdapterLocation);

        spinnerResidence = findViewById(R.id.spinnerResidence);
        arrayAdapterResidence = ArrayAdapter.createFromResource(this, R.array.residence, R.layout.spinner_item);
        arrayAdapterResidence.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerResidence.setAdapter(arrayAdapterResidence);

        //reference to the spinner view and array adapter
        spinnerContraceptive = findViewById(R.id.spinnerContraceptive);

        String gender = getIntent().getExtras().get("gender").toString().trim();

        switch (gender){

            case "Male":
                arrayAdapterContraceptive = ArrayAdapter.createFromResource(this, R.array.con_male, R.layout.spinner_item);
                arrayAdapterContraceptive.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerContraceptive.setAdapter(arrayAdapterContraceptive);
                break;
            case "Female":
                arrayAdapterContraceptive = ArrayAdapter.createFromResource(this, R.array.con_female, R.layout.spinner_item);
                arrayAdapterContraceptive.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerContraceptive.setAdapter(arrayAdapterContraceptive);
                break;

        }

        if(spinnerLocation.getSelectedItem().toString().equals("Other")){
            textInputLayoutOtherLocation = findViewById(R.id.textInputLayoutOtherLocation);
            textInputLayoutOtherLocation.setVisibility(View.VISIBLE);
        }

        mAuth = FirebaseAuth.getInstance();

        users = new Users();

        // getting view to the buttons
        make_payment = findViewById(R.id.buttonPayment);
        cancel_payment = findViewById(R.id.buttonCancel);

        //object initialization
        orders = new Orders();

        progressBar = findViewById(R.id.progressBar);

        // method call to change ProgressDialog Background
        changeProgressDialogBg();

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

    // Adds a sliding animation to the buttons
    private void slide_button(){
        Animation slide = AnimationUtils.loadAnimation(this,android.R.anim.bounce_interpolator);
        button_layout.clearAnimation();
        button_layout.startAnimation(slide);
    }

    // method to change ProgressDialog background color based on the android version of user's phone
    private void changeProgressDialogBg(){

        // if the build sdk version >= android 5.0
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //sets the background color according to android version
            progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("Placing Order");
            progressDialog.setMessage("please wait...");
        }
        //else do this
        else{
            //sets the background color according to android version
            progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("Placing Order");
            progressDialog.setMessage("please wait...");
        }

    }

    // on click listener for placing order
    public void onPlaceOrderButtonClick(View view) {

        //getting input from the fields
        String phone_number = editTextPhoneNumber.getText().toString().trim();
        String hostel_name = editTextHostelName.getText().toString().trim();
        String room_number = editTextRoomNumber.getText().toString().trim();

        // checks if the fields are not empty
        if(TextUtils.isEmpty(phone_number)){
            // starts animation on this view
            editTextPhoneNumber.clearAnimation();
            editTextPhoneNumber.startAnimation(shake);
            editTextPhoneNumber.setError(getString(R.string.error_text_phone_number));
            editTextPhoneNumber.requestFocus();
        }
        else if(phone_number.length() != 10){
            // starts animation on this view
            editTextPhoneNumber.clearAnimation();
            editTextPhoneNumber.startAnimation(shake);
            editTextPhoneNumber.setError(getString(R.string.phone_invalid));
            editTextPhoneNumber.requestFocus();
        }
        else if(TextUtils.isEmpty(hostel_name)){
            // starts animation on this view
            editTextHostelName.clearAnimation();
            editTextHostelName.startAnimation(shake);
            editTextHostelName.setError(getString(io.icode.concaregh.application.R.string.error_text_hostel));
            editTextHostelName.requestFocus();
        }
        else if(TextUtils.isEmpty(room_number)){
            // starts animation on this view
            editTextRoomNumber.clearAnimation();
            editTextRoomNumber.startAnimation(shake);
            editTextRoomNumber.setError(getString(R.string.error_text_room_number));
            editTextRoomNumber.requestFocus();
        }
        else{
            placeOrder();
        }
    }

    //method for handling the placing of order
    public void placeOrder(){

            // displays the dialog
            //progressBar.setVisibility(View.VISIBLE);
            progressDialog.show();

            FirebaseUser user = mAuth.getCurrentUser();

            String username = user.getDisplayName();

            //getting input from the user
            String phone_number = editTextPhoneNumber.getText().toString().trim();
            String campus = spinnerCampus.getSelectedItem().toString().trim();
            String location = spinnerLocation.getSelectedItem().toString().trim();
            String other_location = editTextOtherLocation.getText().toString().trim();
            String residence = spinnerResidence.getSelectedItem().toString().trim();
            String contraceptive = spinnerContraceptive.getSelectedItem().toString().trim();
            String other_contraceptive = editTextOtherContraceptive.getText().toString().trim();
            String hostel_name = editTextHostelName.getText().toString().trim();
            String room_number = editTextRoomNumber.getText().toString().trim();

            // setting values to setter methods
            orders.setUsername(username);
            orders.setHostel_name(hostel_name);
            orders.setRoom_number(room_number);
            orders.setTelephone_Number(phone_number);
            orders.setCampus(campus);
            orders.setLocation(location);
            orders.setOther_location(other_location);
            orders.setResidence(residence);
            orders.setContraceptive(contraceptive);
            orders.setOther_contraceptive(other_contraceptive);

            FirebaseDatabase.getInstance().getReference("Orders")
                    .child(mAuth.getCurrentUser().getUid())
                    .setValue(orders).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        // display a snackbar after a successful order
                        Snackbar.make(nestedScrollView,
                                "You have successfully made an order.." +
                                        "One of our agents will deliver it " +
                                        "to you very soon",
                                Snackbar.LENGTH_LONG).show();

                        //clears the fields after order is placed
                        clearTextFields();

                        // Sends a notification to the user of placing order successfully
                        // Creating an explicit intent for the activity in the app
                        Intent intent = new Intent(PlaceOrderActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(PlaceOrderActivity.this, 0, intent, 0);

                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(PlaceOrderActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.app_logo_round)
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText(" You have successfully made an order for "  +
                                        orders.getContraceptive() + "." +
                                        " One of our agents will deliver it " +
                                        " to you very soon. Thank you... ")
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(" You have successfully made an order for "  +
                                                orders.getContraceptive() + "." +
                                                " One of our agents will deliver it " +
                                                " to you very soon. Thank you... "))
                                // Set the intent that will fire when the user taps the notification
                                .setWhen(System.currentTimeMillis())
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(PlaceOrderActivity.this);
                        notificationManager.notify(notificationId,mBuilder.build());

                        // Method call to sendSMS to admins phone numbers
                        // after users successfully place order
                        sendSMSMessageToAdmins();

                        // sends message to user after placing order
                        //sendSMSMessageToUser();

                        // creates an instance of the intent class and opens the signUpActivity
                        startActivity(new Intent(PlaceOrderActivity.this,HomeActivity.class));

                        // Add a custom animation ot the activity
                        CustomIntent.customType(PlaceOrderActivity.this,"fadein-to-fadeout");

                        // finish the activity
                        finish();


                    } else {
                        // display error message
                        Snackbar.make(nestedScrollView, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }

                    // hides the progressDialog if task is successful
                    //progressBar.setVisibility(View.GONE);
                    progressDialog.dismiss();

                }
            });

    }

    //clears the textfields
    public void clearTextFields(){
        editTextPhoneNumber.setText(null);
        editTextHostelName.setText(null);
        editTextRoomNumber.setText(null);
    }

    // Method to send sms to admin
    // after user places an order
    private void sendSMSMessageToAdmins(){

        //gets text or input from the user
        FirebaseUser user = mAuth.getCurrentUser();

        final String user_name = user.getDisplayName();

        //final String user_name = editTextUsername.getText().toString().trim();
        //getting input from the user
        String phone_number = orders.getTelephone_Number();
        String campus = orders.getCampus();
        String location = orders.getLocation();
        String other_location = orders.getOther_location();
        String residence = orders.getResidence();
        String contraceptive = orders.getContraceptive();
        String other_contraceptive = orders.getOther_contraceptive();
        String hostel_name = orders.getHostel_name();
        String room_number = orders.getRoom_number();

        String username = "zent-concare";
        // password that is to be used along with username

        String password = "concare1";
        // Message content that is to be transmitted

        String message =  user_name + " has placed an order for " + contraceptive + "." + " Location : " + location + "," + " Hostel name : " + hostel_name + "," + " Room Number : " + room_number + "," + " Mobile Number : " + phone_number;

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
        String destination = "233245134112,233501360324";

        //String destination = "233245134112";

        // Sender Id to be used for submitting the message
        String source = getString(R.string.app_name);

        // To what server you need to connect to for submission
        final String server = "rslr.connectbind.com";

        // Port that is to be used like 8080 or 8000
        int port = 2345;

        try {
            // Url that will be called to submit the message
            URL sendUrl = new URL("http://" + server + ":" + "/bulksms/bulksms?");
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
            // catches any error that occurs and outputs to the user
            Toast.makeText(PlaceOrderActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
        }

    }


    // Method to send sms to admin
    // after user places an order
    private void sendSMSMessageToUser(){

        String phone_number = editTextPhoneNumber.getText().toString().trim();

        String username = "zent-concare";
        // password that is to be used along with username

        String password = "concare1";
        // Message content that is to be transmitted

        String message = " Your order has been sent successfully. ";

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
        String destination = phone_number;
        //String destination = "233209062445";

        // Sender Id to be used for submitting the message
        String source = getString(R.string.app_name);

        // To what server you need to connect to for submission
        final String server = "rslr.connectbind.com";

        // Port that is to be used like 8080 or 8000
        int port = 2345;

        try {
            // Url that will be called to submit the message
            URL sendUrl = new URL("http://" + server + ":" + "/bulksms/bulksms?");
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
            Toast.makeText(PlaceOrderActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_share,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
       switch (item.getItemId()){
           case android.R.id.home:

               // finishes this activity
               finish();

               break;
           case R.id.menu_share:

               //method call
               shareIntent();

               default:
                   break;
       }
        return super.onOptionsItemSelected(item);
    }

    // method to share app link to other users
    public void shareIntent(){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String sharingSubject = "CONCARE GH";
        String sharingText = "https://play.google.com/store/apps/details?id=io.icode.concaregh.application";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT,sharingSubject);
        sharingIntent.putExtra(Intent.EXTRA_TEXT,sharingText);
        startActivity(Intent.createChooser(sharingIntent, "Share with"));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // finishes the activity
        finish();

    }

    public void onClearButtonClick(View view) {
        editTextPhoneNumber.setText(null);
        editTextOtherLocation.setText(null);
        editTextOtherContraceptive.setText(null);
        editTextHostelName.setText(null);
        editTextRoomNumber.setText(null);
    }
}
