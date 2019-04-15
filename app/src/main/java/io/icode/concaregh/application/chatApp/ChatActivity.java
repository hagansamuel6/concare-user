package io.icode.concaregh.application.chatApp;

//import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TabLayout;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concaregh.application.activities.OrderActivity;
import io.icode.concaregh.application.R;
import io.icode.concaregh.application.adapters.ViewPagerAdapter;
import io.icode.concaregh.application.constants.Constants;
import io.icode.concaregh.application.fragments.AdminFragment;
import io.icode.concaregh.application.models.Admin;
import io.icode.concaregh.application.models.Groups;
import io.icode.concaregh.application.models.Users;
import io.icode.concaregh.application.notifications.Token;
import maes.tech.intentanim.CustomIntent;

@SuppressWarnings("ALL")
public class ChatActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;

    RelativeLayout internetConnection;

    CircleImageView profile_image;
    TextView username;


    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    DatabaseReference userRef;

    Users users;

    Admin admin;

    Groups groups;

    //check if internet is available or not on phone
    boolean isConnected = false;

    ProgressDialog progressDialog;

    DatabaseReference adminRef;

    DatabaseReference chatRef;

    DatabaseReference groupRef;

    // variable for duration of snackbar and toast
    private static final int DURATION_LONG = 5000;

    private static final int DURATION_SHORT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // finish activity
                finish();
            }
        });

        internetConnection = findViewById(R.id.no_internet_connection);

        relativeLayout = findViewById(R.id.relativeLayout);

        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("");
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);

        profile_image = findViewById(R.id.profile_image);

        username =  findViewById(R.id.username);

        admin = new Admin();

        users = new Users();

        groups = new Groups();

        groupRef = FirebaseDatabase.getInstance().getReference("Groups");

        profile_image = findViewById(R.id.profile_image);

        username =  findViewById(R.id.username);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // method to display fragments and check for internet connection
        isInternetConnnectionEnabled();

        // method call to update token
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    // Update currentAdmin's token
    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(currentUser.getUid()).setValue(token1);
    }


    // method to check if internet connection is enabled
    private void isInternetConnnectionEnabled(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            //we are connected to a network
            isConnected = true;

            // sets visibility to visible if there is  no internet connection
            internetConnection.setVisibility(View.GONE);

            // get user details
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Users users = dataSnapshot.getValue(Users.class);

                    username.setText("CHAT US NOW");

                    //text if user's imageUrl is equal to default
                    if(currentUser.getPhotoUrl() == null){
                        profile_image.setImageResource(R.drawable.profile_icon);
                    }
                    else{
                        // load user's Image Url
                        Glide.with(getApplicationContext()).load(currentUser.getPhotoUrl()).into(profile_image);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // display error message
                    Toast.makeText(ChatActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
                }
            });

            // getting reference to the views
            final TabLayout tabLayout =  findViewById(R.id.tab_layout);
            final ViewPager viewPager = findViewById(R.id.view_pager);

            // Checks for incoming messages and counts them to be displays together in the chats fragments
            chatRef = FirebaseDatabase.getInstance().getReference(Constants.GROUP_CHAT_REF);


            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


            // adds ChatsFragment and AdminFragment to the viewPager
            viewPagerAdapter.addFragment(new AdminFragment(), getString(R.string.text_admin));

            // adds ChatsFragment and AdminFragment to the viewPager + count of unread messages
            //viewPagerAdapter.addFragment(new AdminFragment(), "("+unreadMessages+") Chats");


            // adds UsersFragment and GroupsFragment to the viewPager
            //viewPagerAdapter.addFragment(new GroupsFragment(),getString(R.string.text_groups));
            //viewPagerAdapter.addFragment(new UsersFragment(), getString(R.string.text_users));
            //Sets Adapter view of the ViewPager
            viewPager.setAdapter(viewPagerAdapter);

            //sets tablayout with viewPager
            tabLayout.setupWithViewPager(viewPager);

        }
        // else condition
        else{

            isConnected = false;

            // sets visibility to visible if there is  no internet connection
            internetConnection.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_order:
                // navigates user to the order Activity
                startActivity(new Intent(ChatActivity.this,OrderActivity.class));
                CustomIntent.customType(ChatActivity.this,"right-to-left");
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // method to set user status to "online" or "offline"
    private void status(String status){

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        userRef.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("online");
    }

    @Override
    protected void onDestroy() {
        super.onPause();
        status("offline");
    }
}
