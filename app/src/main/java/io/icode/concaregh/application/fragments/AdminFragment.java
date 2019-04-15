package io.icode.concaregh.application.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import io.icode.concaregh.application.R;
import io.icode.concaregh.application.adapters.RecyclerViewAdapterAdmin;
import io.icode.concaregh.application.chatApp.ChatActivity;
import io.icode.concaregh.application.models.Admin;
import io.icode.concaregh.application.notifications.Token;


@SuppressWarnings("ALL")
public class AdminFragment extends Fragment {

    ConstraintLayout mLayout;

    private RecyclerView recyclerView;

    private RecyclerViewAdapterAdmin adapterAdmin;

    private List<Admin> mAdmin;

    FirebaseUser firebaseUser;

    DatabaseReference adminRef;

    ProgressBar progressBar;

    ValueEventListener dbListener;

    ChatActivity applicationContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        applicationContext = (ChatActivity)context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin,container,false);

        mLayout = view.findViewById(R.id.mLayout);

        recyclerView =  view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(applicationContext));

        mAdmin = new ArrayList<>();

        adminRef = FirebaseDatabase.getInstance().getReference("Admin");

        // adapter initialization
        adapterAdmin = new RecyclerViewAdapterAdmin(applicationContext,mAdmin,false);
        // setting adpater to recyclerView
        recyclerView.setAdapter(adapterAdmin);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar = view.findViewById(R.id.progressBar);

        // method call to display admin in recyclerView
        displayAdmin();

        // method call to update token
        updateToken(FirebaseInstanceId.getInstance().getToken());

        // return view
        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
    }

    // message to read the admin from the database
    public  void displayAdmin(){

        // display the progressBar
        progressBar.setVisibility(View.VISIBLE);

         dbListener = adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //clears list
                mAdmin.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Admin admin = snapshot.getValue(Admin.class);

                    assert admin != null;

                    mAdmin.add(admin);

                }

                adapterAdmin.notifyDataSetChanged();

                // dismiss progressBar
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                // dismiss progressBar
                progressBar.setVisibility(View.GONE);

                // display error if it occurs
                Snackbar.make(mLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // remove listener
        adminRef.removeEventListener(dbListener);
    }
}
