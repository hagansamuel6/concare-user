package io.icode.concaregh.application.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import io.icode.concaregh.application.R;
import io.icode.concaregh.application.adapters.ViewPagerAdapter;
import io.icode.concaregh.application.fragments.UserSignInFragment;
import io.icode.concaregh.application.fragments.UserSignUpFragment;
import maes.tech.intentanim.CustomIntent;

public class Sign_In_Sign_Up_Activity extends AppCompatActivity {

    // Global variables or views
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;

    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign__in__sign__up_);

        // getting reference
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // getting reference to tabLayout
        tabLayout = findViewById(R.id.tab_layout);
        // getting reference to viewPager
        viewPager = findViewById(R.id.view_pager);

        // method call to setUpViewPager with viewPager
        setupViewPager(viewPager);

        //sets tabLayout with viewPager
        tabLayout.setupWithViewPager(viewPager);

        mAuth = FirebaseAuth.getInstance();

    }

    private void setupViewPager(ViewPager viewPager) {
        // class instantiation of ViewPagerAdapterChat
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        // calling method to add fragments
        viewPagerAdapter.addFragment(new UserSignInFragment(),getString(R.string.text_sign_in));
        viewPagerAdapter.addFragment(new UserSignUpFragment(), getString(R.string.text_sign_up));
        // setting adapter to viewPager
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            // starts home activity
            Intent intent = new Intent(Sign_In_Sign_Up_Activity.this,HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            // adding transition
            CustomIntent.customType(Sign_In_Sign_Up_Activity.this, "fadein-to-fadeout");

            // finish activity
            finish();
        }
    }

}
