package com.example.bgapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventInformationActivity extends AppCompatActivity {

    private TextView eventName, eventInformation, eventTimeDate, eventAddress, eventTakenSlots;
    private RecyclerView showEventParticipantsList;
    private Button joinEventButton, inviteFriendButton, exitEventButton;

    private DatabaseReference eventRef, usersRef, participantsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String currentUserName, currentUserID, currentUserImage, currentEventName, eventCreatorID, availableEventSlots, isUserEventParticipant;
    private int participantsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_information);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserName = user.getDisplayName();
        currentUserID = user.getUid();
        currentUserImage = user.getPhotoUrl().toString();

        showEventParticipantsList = (RecyclerView) findViewById(R.id.event_participants);
        showEventParticipantsList.setLayoutManager(new LinearLayoutManager(this));

        currentEventName = getIntent().getExtras().get("eventName").toString();
        isUserEventParticipant = getIntent().getExtras().get("isEventParticipant").toString();

        eventName = (TextView) findViewById(R.id.event_name);
        eventInformation = (TextView) findViewById(R.id.event_information);
        eventTimeDate = (TextView) findViewById(R.id.event_time_date);
        eventAddress = (TextView) findViewById(R.id.event_address);
        eventTakenSlots = (TextView) findViewById(R.id.taken_slots);
        joinEventButton = (Button) findViewById(R.id.join_event);
        inviteFriendButton = (Button) findViewById(R.id.invite_friend);
        exitEventButton = (Button) findViewById(R.id.exit_event);

        participantsRef = FirebaseDatabase.getInstance().getReference().child("Events").child(currentEventName).child("Participants");
        eventRef = FirebaseDatabase.getInstance().getReference().child("Events");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        retrieveEventInformation();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(participantsRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, participantsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, participantsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull participantsViewHolder holder, final int position, @NonNull Contacts model) {

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Glide.with(getApplicationContext())
                                .load(model.getImage())
                                .placeholder(R.drawable.default_image)
                                .into(holder.userImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (currentUserID.equals(eventCreatorID)) {
                                    String selectedUserID = getRef(position).getKey();
                                    if (selectedUserID.equals(currentUserID))
                                    {
                                        Toast.makeText(getApplicationContext(), "You can't remove event creator from event", Toast.LENGTH_SHORT).show();
                                    } else removeUserFromEvent(selectedUserID);
                                }
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public participantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        participantsViewHolder viewHolder = new participantsViewHolder(view);
                        return viewHolder;
                    }
                };
        showEventParticipantsList.setAdapter(adapter);
        adapter.startListening();

        inviteFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToInviteFriendActivity();
            }
        });

        joinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentUserID.equals(eventCreatorID)) {
                    HashMap<String, Object> eventParticipant = new HashMap<>();
                        eventParticipant.put("name", currentUserName);
                        eventParticipant.put("status", "Participant");
                        eventParticipant.put("image", currentUserImage);
                    eventRef.child(currentEventName).child("Participants").child(currentUserID).updateChildren(eventParticipant)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    enterEventChatRoom();
                                }
                            }
                        });
                }
            }
        });

        exitEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserEventParticipant.equals("yes")) {
                    if (currentUserID.equals(eventCreatorID)) {
                        deleteEvent();
                    } else {
                        removeUserFromEvent(currentUserID);
                    }
                } else moveToMain();
            }
        });
    }

    private void moveToMain() {
        Intent intent = new Intent(EventInformationActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void deleteEvent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EventInformationActivity.this, R.style.AlertDialog);
        builder.setTitle("Do you want to delete this event?:");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventRef.child(currentEventName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Event successfully deleted", Toast.LENGTH_SHORT).show();
                            moveToMain();
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void removeUserFromEvent(final String selectedUserID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EventInformationActivity.this, R.style.AlertDialog);
        String dialogTitle, buttonTitle;
        final String userID = selectedUserID;
        if (currentUserID.equals(selectedUserID)) {
            dialogTitle = "Do you want to exit this event?";
            buttonTitle = "Exit";
        } else {
            dialogTitle = "Do you want to remove this user from your event?";
            buttonTitle = "Remove";
        }
        builder.setTitle(dialogTitle);

        builder.setPositiveButton(buttonTitle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                participantsRef.child(selectedUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EventInformationActivity.this, "User successfully removed", Toast.LENGTH_SHORT).show();
                            if (!currentUserID.equals(eventCreatorID)) {
                                moveToMain();
                            }
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void goToInviteFriendActivity() {
        Intent eventInvitationIntent = new Intent(getApplicationContext(), EventInvitationActivity.class);
        eventInvitationIntent.putExtra("eventName", currentEventName);
        eventInvitationIntent.putExtra("isEventParticipant", isUserEventParticipant);
        startActivity(eventInvitationIntent);
    }

    private void enterEventChatRoom() {
        Intent eventChatIntent = new Intent(EventInformationActivity.this, EventChatActivity.class);
        eventChatIntent.putExtra("eventName", currentEventName);
        eventChatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(eventChatIntent);
        finish();
    }

    private void retrieveEventInformation() {
        eventRef.child(currentEventName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String setEventInformation = dataSnapshot.child("Information").child("information").getValue().toString();
                    String setEventTime = dataSnapshot.child("Information").child("time").getValue().toString();
                    String setEventDate = dataSnapshot.child("Information").child("date").getValue().toString();
                    String setEventAddress = dataSnapshot.child("Information").child("address").getValue().toString();
                    availableEventSlots = dataSnapshot.child("Information").child("slots").getValue().toString();
                    eventCreatorID = dataSnapshot.child("Information").child("creatorID").getValue().toString();
                    participantsCount = (int) dataSnapshot.child("Participants").getChildrenCount();

                    eventName.setText(currentEventName);
                    eventInformation.setText(setEventInformation);
                    eventTimeDate.setText("Event starts at:   " + setEventTime + "   " + setEventDate);
                    eventAddress.setText("Event will take place at the following address: " + setEventAddress);
                    eventTakenSlots.setText(Integer.toString(participantsCount) + "/" + availableEventSlots + " slots already taken");

                    if (isUserEventParticipant.equals("yes")) {
                        joinEventButton.setText(" Event chat room ");
                    }
                    if (currentUserID.equals(eventCreatorID)) {
                        exitEventButton.setText("     Delete event     ");
                    }
                    if (participantsCount>=Integer.parseInt(availableEventSlots)) {
                        if (!isUserEventParticipant.equals("yes")) {
                            joinEventButton.setVisibility(View.INVISIBLE);
                        }
                        inviteFriendButton.setVisibility(View.INVISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class participantsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView userImage;

        public participantsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
