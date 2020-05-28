package com.example.bgapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EventInvitationActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private DatabaseReference contactsRef, usersRef, eventsRef, eventRequestsRef, notificationRef;
    private FirebaseAuth mAuth;

    private String currentEventName, isUserEventParticipant, currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_invitation);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        eventsRef = FirebaseDatabase.getInstance().getReference().child("Events");
        eventRequestsRef = FirebaseDatabase.getInstance().getReference().child("Event requests");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        recyclerView = (RecyclerView) findViewById(R.id.event_invite_recycler_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentEventName = getIntent().getExtras().get("eventName").toString();
        isUserEventParticipant = getIntent().getExtras().get("isEventParticipant").toString();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Invite friend to the event");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(contactsRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, inviteFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, inviteFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final inviteFriendViewHolder holder, final int position, @NonNull Contacts model) {
                        final String usersID = getRef(position).getKey();

                        usersRef.child(usersID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    final String profileImage = dataSnapshot.child("image").getValue().toString();
                                    final String profileName = dataSnapshot.child("name").getValue().toString();
                                    final String profileUserID = dataSnapshot.getKey();

                                    holder.userName.setText(profileName);
                                    Glide.with(getApplicationContext())
                                            .load(profileImage)
                                            .placeholder(R.drawable.default_image)
                                            .into(holder.profileImage);

                                    eventsRef.child(currentEventName).child("Participants")
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists() && dataSnapshot.hasChild(profileUserID)) {
                                                        holder.userStatus.setText(" is already " + currentEventName + " event participant");
                                                    } else {
                                                        eventRequestsRef.child(profileUserID)
                                                                .addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        if (dataSnapshot.exists() && dataSnapshot.hasChild(currentEventName)) {
                                                                            holder.userStatus.setText(" is already invited");
                                                                            String inviterID = dataSnapshot.child(currentEventName).child("invited_by").getValue().toString();
                                                                            if (currentUserID.equals(inviterID)) {
                                                                                holder.cancelButton.setVisibility(View.VISIBLE);
                                                                                holder.cancelButton.setText("Cancel invitation");
                                                                            }
                                                                            holder.inviteButton.setVisibility(View.GONE);
                                                                        } else {
                                                                            holder.userStatus.setText(" can be invited");
                                                                            holder.inviteButton.setVisibility(View.VISIBLE);
                                                                            holder.inviteButton.setText("Invite");
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                    holder.inviteButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final String selectedUserID = getRef(position).getKey();
                                            inviteToEvent(selectedUserID);
                                            holder.inviteButton.setVisibility(View.GONE);
                                        }
                                    });

                                    holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final String selectedUserID = getRef(position).getKey();
                                            cancelInvitation(selectedUserID);
                                            holder.cancelButton.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public inviteFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        inviteFriendViewHolder viewHolder = new inviteFriendViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class inviteFriendViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        ImageView profileImage;
        Button inviteButton, cancelButton;

        public inviteFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            inviteButton = itemView.findViewById(R.id.request_accept_button);
            cancelButton = itemView.findViewById(R.id.request_reject_button);
        }
    }

    private void cancelInvitation(final String friendUserID) {
        eventRequestsRef.child(friendUserID).child(currentEventName)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Invitation cancelled", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void inviteToEvent(final String friendUserID) {
        eventRequestsRef.child(friendUserID).child(currentEventName)
                .child("invited_by").setValue(currentUserID)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, String> chatNotificationMap = new HashMap<>();
                                chatNotificationMap.put("from", currentUserID);
                                chatNotificationMap.put("type", "event invitation");
                            notificationRef.child(friendUserID).push()
                                    .setValue(chatNotificationMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Friend successfully invited", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void goBackToEventInformation() {
        Intent eventInformationIntent = new Intent(getApplicationContext(), EventInformationActivity.class);
        eventInformationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        eventInformationIntent.putExtra("eventName", currentEventName);
        eventInformationIntent.putExtra("isEventParticipant", isUserEventParticipant);
        startActivity(eventInformationIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                goBackToEventInformation();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
