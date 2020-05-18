package com.example.bgapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class EventChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView scrollView;
    private TextView displayTextMessages;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference userRef, eventRef, eventMessageKeyRef;

    private String currentEventName, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_chat);

        currentEventName = getIntent().getExtras().get("eventName").toString();
        Toast.makeText(EventChatActivity.this, currentEventName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserName = user.getDisplayName();
        eventRef = FirebaseDatabase.getInstance().getReference().child("Events").child(currentEventName);
        
        initializeFields();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageToDatabase();

                userMessageInput.setText("");

                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        eventRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentEventName);
        sendMessageButton = (ImageButton) findViewById(R.id.send_message);
        userMessageInput = (EditText) findViewById(R.id.input_message);
        displayTextMessages = (TextView) findViewById(R.id.event_chat_text_display);
        scrollView = (ScrollView) findViewById(R.id.my_scroll_view);
    }

    private void SaveMessageToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = eventRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText( EventChatActivity.this, "Please write message first", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM yyyy");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            HashMap<String, Object> eventMessageKey = new HashMap<>();
            eventRef.updateChildren(eventMessageKey);

            eventMessageKeyRef = eventRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name", currentUserName);
                messageInfoMap.put("message", message);
                messageInfoMap.put("date", currentDate);
                messageInfoMap.put("time", currentTime);
            eventMessageKeyRef.updateChildren(messageInfoMap);

        }

    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n");

            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
