package io.icode.concaregh.application.chatApp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concaregh.application.R;
import io.icode.concaregh.application.adapters.MessageAdapter;
import io.icode.concaregh.application.constants.Constants;
import io.icode.concaregh.application.interfaces.APIService;
import io.icode.concaregh.application.models.Admin;
import io.icode.concaregh.application.models.Chats;
import io.icode.concaregh.application.models.Users;
import io.icode.concaregh.application.notifications.Client;
import io.icode.concaregh.application.notifications.Data;
import io.icode.concaregh.application.notifications.MyResponse;
import io.icode.concaregh.application.notifications.Sender;
import io.icode.concaregh.application.notifications.Token;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.icode.concaregh.application.constants.Constants.USER_REF;
import static io.icode.concaregh.application.constants.Constants.baseUrl;
import static io.icode.concaregh.application.constants.Constants.client_url;

@SuppressWarnings("ALL")
public class MessageActivity extends AppCompatActivity implements View.OnClickListener,
        MessageAdapter.OnItemClickListener {

    RelativeLayout relativeLayout;

    Toolbar toolbar;

    // fields to contain admin details
    CircleImageView profile_image;
    TextView username;
    TextView admin_status;

    FirebaseUser currentUser;

    DatabaseReference adminRef;

    TextView tv_no_messages;

    DatabaseReference chatRef;

    DatabaseReference userRef;

    // editText and Button to send Message
    EditText msg_to_send;
    ImageButton btn_send,img_emoji;

    // string to get intentExtras
    String admin_uid;

    String admin_username;

    //Variable to store status of the current user
    String status;

    // variable for MessageAdapter class
    MessageAdapter messageAdapter;

    List<Chats> mChats;

    RecyclerView recyclerView;

    ValueEventListener seenListener;

    ValueEventListener mDBListener;

    boolean notify = false;

    private boolean isChat;

    private String theLastMessage;

    // variable to store the current time
    String currentTime;

    // loading bar to load messages
    ProgressBar progressBar;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        relativeLayout = findViewById(R.id.relativeLayout);

        profile_image =  findViewById(R.id.profile_image);
        username =  findViewById(R.id.username);
        admin_status =  findViewById(R.id.admin_status);
        msg_to_send =  findViewById(R.id.editTextMessage);
        btn_send =  findViewById(R.id.btn_send);
        img_emoji =  findViewById(R.id.img_emoji);

        btn_send.setOnClickListener(this);
        img_emoji.setOnClickListener(this);

        tv_no_messages = findViewById(R.id.tv_no_messages);

        //getting reference to the recyclerview and setting it up
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //admin_uid = getIntent().getStringExtra("uid");
        //admin_username = getIntent().getStringExtra("username");
        // get the current status of admin
        //status = getIntent().getStringExtra("status");

        /*
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MessageActivity.this);
        admin_uid = preferences.getString("uid","");
        admin_username = preferences.getString("username","");
        status = preferences.getString("status","");
        */

        // set status of the admin on toolbar below the username in the message activity
        //admin_status.setText(status);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        adminRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_REF).child(admin_uid);

        progressBar =  findViewById(R.id.progressBar);

        // progressDialog to display before deleting message
        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage(getString(R.string.title_deleting_msg));

        // method to load admin details into the imageView
        // and TextView in the toolbar section of this activity's layout resource file
        getAdminDetails();

        //method call to seen message
        seenMessage(admin_uid);

        // mehtod call to get current time
        getCurrentTime();

        // update registration token
        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    // gets the current time
    private void getCurrentTime(){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = timeFormat.format(calendar.getTime());

    }

    // methpod to load admin details
    private void getAdminDetails(){

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Admin admin = dataSnapshot.getValue(Admin.class);

                username.setText(admin.getUsername());
                // set status of the admin on toolbar below the username in the message activity

                // making sure status of admin is always online
                if(admin.getStatus().equals(getString(R.string.text_offline))){
                    admin_status.setText(getString(R.string.text_online));
                }
                else{
                    admin_status.setText(admin.getStatus());
                }


                if(admin.getImageUrl() == null){
                    // sets a default placeholder into imageView if url is null
                    profile_image.setImageResource(R.drawable.app_logo);
                }
                else{
                    // loads imageUrl into imageView if url is not null
                    Glide.with(getApplicationContext())
                            .load(admin.getImageUrl()).into(profile_image);
                }

                // method call
                readMessages(currentUser.getUid(),admin_uid,admin.getImageUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(MessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.img_emoji:{

                Toast.makeText(MessageActivity.this,
                        getString(R.string.text_hi),Toast.LENGTH_LONG).show();

                break;
            }

            case R.id.btn_send:{

                // sets notify to true
                notify = true;

                String message  = msg_to_send.getText().toString();

                if(!message.equals("")){
                    // call to method to sendMessage and
                    sendMessage(currentUser.getUid(),admin_uid,message);
                }
                else{
                    Toast.makeText(MessageActivity.this,
                            getString(R.string.no_text_message),Toast.LENGTH_LONG).show();
                }
                // clear the field after message is sent
                msg_to_send.setText("");

                break;
            }
        }

    }

    private void sendMessage(String sender, final String receiver, final String message){

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver", receiver);
        hashMap.put("receivers", new ArrayList<String>(){{add(receiver);}});
        hashMap.put("message",message);
        hashMap.put("timeStamp",currentTime);
        hashMap.put("isseen", false);

        chatRef.child(Constants.CHAT_REF).push().setValue(hashMap);

        // variable to hold the message to be sent
        final String msg = message;

        userRef = FirebaseDatabase.getInstance().getReference(USER_REF).child(currentUser.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                assert users != null;
                if(notify) {
                    // method call to send notification to admin as soon as message is sent
                    sendNotification(receiver, users.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }


    // sends notification to admin as soon as message is sent
    private void sendNotification(final String receiver, final String username, final String message){

        final Data[] data = new Data[1];
        final Token[] token = new Token[1];

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constants.TOKENS_REF);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                     token[0] = snapshot.getValue(Token.class);
                     data[0] = new Data(currentUser.getUid(),R.mipmap.app_logo_round, username+": "+message,
                            getString(R.string.app_name),admin_uid);

                    assert token[0] != null;
                    Sender sender = new Sender(data[0], token[0].getToken());



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService api = retrofit.create(APIService.class);

        Call<ResponseBody> call = api.sendToGroup(Constants.ADMIN_REF, username ,message, data[0]);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // response to be handled here
                if (response.code() == 200){
                    Snackbar.make(relativeLayout, "sent", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(relativeLayout, "failed to send message", Snackbar.LENGTH_LONG).show();
            }
        });




    }

    // methopd to check if user or admin has seen the message
    private void seenMessage(final String admin_uid){

        chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHAT_REF);

        seenListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    assert chats != null;
                    if(chats.getReceiver().equals(currentUser.getUid()) && chats.getSender().equals(admin_uid)
                            || chats.getReceiver().equals(admin_uid) && chats.getSender().equals(currentUser.getUid())
                            || chats.getReceivers().contains(admin_uid) && chats.getSender().equals(currentUser.getUid())
                            || chats.getReceivers().contains(currentUser.getUid()) && chats.getSender().equals(admin_uid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }

    // method to readMessages from the system
    private void readMessages(final String myId, final String adminId, final String imageUrl){

        // display progressBar
        progressBar.setVisibility(View.VISIBLE);

        // array initialization
        mChats = new ArrayList<>();

        chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHAT_REF);

        mDBListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){

                    // displays a message to the user
                    tv_no_messages.setVisibility(View.VISIBLE);

                    // hides progressBar
                    progressBar.setVisibility(View.GONE);

                }

                else{

                    // clears the chats to avoid reading duplicate message
                    mChats.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Chats chats = snapshot.getValue(Chats.class);
                        // gets the unique keys of the chats
                        chats.setKey(snapshot.getKey());

                        assert chats != null;
                        if(chats.getReceiver().equals(myId) && chats.getSender().equals(adminId)
                                || chats.getReceiver().equals(adminId) && chats.getSender().equals(myId)
                                || chats.getReceiver().equals("") && chats.getReceivers().contains(myId)
                                && chats.getSender().equals(adminId)
                                || chats.getReceivers().contains(adminId)
                                && chats.getSender().equals(myId)){
                            mChats.add(chats);
                        }

                        // initializing the messageAdapter and setting adapter to recyclerView
                        messageAdapter = new MessageAdapter(MessageActivity.this,mChats,imageUrl);
                        // setting adapter
                        recyclerView.setAdapter(messageAdapter);
                        // notify data change in adapter
                        messageAdapter.notifyDataSetChanged();

                        // hides the text
                        tv_no_messages.setVisibility(View.GONE);

                        // dismiss progressBar
                        progressBar.setVisibility(View.GONE);

                        // setting on OnItemClickListener in this activity as an interface
                        messageAdapter.setOnItemClickListener(MessageActivity.this);

                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // dismiss progressBar
                progressBar.setVisibility(View.GONE);

                // display error message
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }


    /**handling ContextMenu
     Click Listeners in activity
     */
    @Override
    public void onItemClick(int position) {
        Toast.makeText(this,getString(R.string.delete_msg_instruct),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteClick(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
        builder.setTitle(getString(R.string.title_delete_message));
        builder.setMessage(getString(R.string.text_delete_message));

        builder.setPositiveButton(getString(R.string.text_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // show dialog
                progressDialog.show();

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // dismiss dialog
                        progressDialog.dismiss();

                        // gets the position of the selected message
                        Chats selectedMessage = mChats.get(position);

                        //gets the key at the selected position
                        String selectedKey = selectedMessage.getKey();

                        chatRef.child(selectedKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(MessageActivity.this,getString(R.string.msg_deleted),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MessageActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                },3000);

            }
        });

        builder.setNegativeButton(getString(R.string.text_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public void onCancelClick(int position) {
        // do nothing / close ContextMenu
    }

    // keeping track of the current user the admin is chatting to avoid sending notification everytime
    private void currentAdmin(String adminUid){
        SharedPreferences.Editor editor =  getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentadmin",adminUid);
        editor.apply();
    }

    // method to set user status to "online" or "offline"
    private void status(String status){

        userRef = FirebaseDatabase.getInstance().getReference(USER_REF)
                .child(currentUser.getUid());
        //.child(adminUid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        userRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        status(getString(R.string.status_online));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // set status to online
        status(getString(R.string.status_online));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // set status to online
        status(getString(R.string.status_online));

    }

    @Override
    protected void onPause() {
        super.onPause();
        // set status to offline
        status(getString(R.string.status_offline));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // set status to offline
        status(getString(R.string.status_offline));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // set status to offline if activity is destroyed
        status(getString(R.string.status_offline));
        // removes eventListeners when activity is destroyed
        if(seenListener != null && mDBListener != null){
            chatRef.removeEventListener(seenListener);
            chatRef.removeEventListener(mDBListener);
        }

    }

    // Update currentUser's  device token
    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.TOKENS_REF);
        Token token1 = new Token(token);
        reference.child(currentUser.getUid()).setValue(token1);
    }
}
