package com.example.bgapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private RecyclerView recyclerView;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference usersRef, eventRef, eventMessageKeyRef;

    private String currentEventName, currentUserName, currentUserID, userImageUrl, senderUserID, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_chat);

        currentEventName = getIntent().getExtras().get("eventName").toString();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserName = user.getDisplayName();
        currentUserID = user.getUid();
        eventRef = FirebaseDatabase.getInstance().getReference().child("Events").child(currentEventName).child("Chat");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = (RecyclerView) findViewById(R.id.event_messages_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        initializeFields();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageToDatabase();

                userMessageInput.setText("");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Messages> options =
                new FirebaseRecyclerOptions.Builder<Messages>()
                        .setQuery(eventRef, Messages.class)
                        .build();

        FirebaseRecyclerAdapter<Messages, eventChatViewHolder> adapter =
                new FirebaseRecyclerAdapter<Messages, eventChatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final eventChatViewHolder holder, final int position, @NonNull Messages model) {
                        senderUserID = model.getFrom();
                        usersRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    userImageUrl = dataSnapshot.child("image").getValue().toString();
                                    Glide.with(getApplicationContext())
                                            .load(userImageUrl)
                                            .placeholder(R.drawable.default_image)
                                            .into(holder.profileImage);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        holder.displaySenderName.setText(model.getName() + ":");
                        holder.displayTextMessages.setText(model.getMessage());
                        holder.displayMessageTime.setText(model.getTime() + " " + model.getDate());
                    }

                    @NonNull
                    @Override
                    public eventChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_event_messages_layout, parent, false);
                        eventChatViewHolder viewHolder = new eventChatViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(adapter);

        adapter.startListening();
    }

    public static class eventChatViewHolder extends RecyclerView.ViewHolder {
        TextView displayTextMessages, displayMessageTime, displaySenderName;
        CircleImageView profileImage;

        public eventChatViewHolder(@NonNull View itemView) {
            super(itemView);

            displaySenderName = (TextView) itemView.findViewById(R.id.message_sender_name);
            displayTextMessages = (TextView) itemView.findViewById(R.id.member_message_text);
            displayMessageTime = (TextView) itemView.findViewById(R.id.message_time);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }

    private void initializeFields() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(currentEventName + " - event chat room");
        sendMessageButton = (ImageButton) findViewById(R.id.send_message);
        userMessageInput = (EditText) findViewById(R.id.input_message);
    }

    private void SaveMessageToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = eventRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText( EventChatActivity.this, "Please write message first", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            HashMap<String, Object> eventMessageKey = new HashMap<>();
            eventRef.updateChildren(eventMessageKey);

            eventMessageKeyRef = eventRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name", currentUserName);
                messageInfoMap.put("from", currentUserID);
                messageInfoMap.put("message", message);
                messageInfoMap.put("date", currentDate);
                messageInfoMap.put("time", currentTime);
            eventMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(EventChatActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
