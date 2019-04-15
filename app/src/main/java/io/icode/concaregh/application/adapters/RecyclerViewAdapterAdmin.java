package io.icode.concaregh.application.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concaregh.application.R;
import io.icode.concaregh.application.chatApp.MessageActivity;
import io.icode.concaregh.application.models.Admin;
import io.icode.concaregh.application.models.Chats;

public class RecyclerViewAdapterAdmin extends RecyclerView.Adapter<RecyclerViewAdapterAdmin.ViewHolder> {

    private Context mCtx;
    private List<Admin> mAdmin;
    private boolean isChat;

    private String theLastMessage;

    public RecyclerViewAdapterAdmin(Context mCtx, List<Admin> mAdmin, boolean isChat){
        this.mCtx = mCtx;
        this.mAdmin = mAdmin;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_items_admin,parent, false);

        return new RecyclerViewAdapterAdmin.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // gets the positions of the all users
        final Admin admin = mAdmin.get(position);

        // sets username to the text of the textView
        holder.username.setText(admin.getUsername());

        if(admin.getImageUrl() == null){
            // loads the default placeholder into ImageView if ImageUrl is null
            holder.profile_pic.setImageResource(R.mipmap.app_logo_round);
        }
        else{
            // loads user image into the ImageView
            Glide.with(mCtx).load(admin.getImageUrl()).into(holder.profile_pic);
        }

        // calling the lastMessage method
        if(isChat){
            lastMessage(admin.getAdminUid(),holder.last_msg);
        }
        else{
            holder.last_msg.setVisibility(View.GONE);
        }

        // code to check if admin is online
        if(isChat){
            if(admin.getStatus().equals("online")){
                holder.status_online.setVisibility(View.VISIBLE);
                holder.status_offline.setVisibility(View.GONE);
            }
            else{
                holder.status_online.setVisibility(View.GONE);
                holder.status_offline.setVisibility(View.VISIBLE);
            }
        }
        else{
            holder.status_online.setVisibility(View.GONE);
            holder.status_offline.setVisibility(View.GONE);
        }


        // onClickListener for view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // passing adminUid as a string to the MessageActivity
                Intent intent = new Intent(mCtx,MessageActivity.class);
                intent.putExtra("uid",admin.getAdminUid());
                intent.putExtra("username",admin.getUsername());
                intent.putExtra("status",admin.getStatus());
                mCtx.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mAdmin.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profile_pic;
        TextView username;
        TextView last_msg;

        // status online or offline indicators
        CircleImageView status_online;
        CircleImageView status_offline;

        public ViewHolder(View itemView) {
            super(itemView);

            profile_pic = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            status_online = itemView.findViewById(R.id.status_online);
            status_offline = itemView.findViewById(R.id.status_offline);
            last_msg = itemView.findViewById(R.id.last_msg);
        }
    }

    private void lastMessage(final String adminUid, final TextView last_msg){
        theLastMessage = "default";
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference lastMsgRef = FirebaseDatabase.getInstance().getReference("Chats");
        lastMsgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    assert chats != null;
                    if(chats.getReceiver().equals(currentUser.getUid()) && chats.getSender().equals(adminUid)
                            || chats.getReceiver().equals(adminUid) && chats.getSender().equals(currentUser.getUid())){
                        theLastMessage = chats.getMessage();
                    }

                }

                switch (theLastMessage){
                    case "default":
                        last_msg.setText(R.string.no_message);
                        break;

                        default:
                            last_msg.setText(theLastMessage);
                            break;
                }


                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message if one should occur
                Toast.makeText(mCtx, databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }


}
