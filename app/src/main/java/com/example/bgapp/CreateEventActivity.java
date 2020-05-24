package com.example.bgapp;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateEventActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText eventName, eventInformation, availableSlots, eventAddress, eventPassword;
    private TextView eventTime, eventDate;
    private Button editEventTime, editEventDate, saveEvent;
    private DatePicker eventDatePicker;
    private TimePicker eventTimePicker;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference usersRef, eventRef;

    private String currentUserName, currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserName = user.getDisplayName();
        currentUserID = user.getUid();
        eventRef = FirebaseDatabase.getInstance().getReference().child("Events");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initializeFields();
    }

    @Override
    protected void onStart() {
        super.onStart();

        editEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventDatePicker.getVisibility() == View.GONE) {
                    eventDatePicker.setVisibility(View.VISIBLE);
                    eventName.setVisibility(View.GONE);
                    eventInformation.setVisibility(View.GONE);
                    eventAddress.setVisibility(View.GONE);
                    eventPassword.setVisibility(View.GONE);
                    eventTime.setVisibility(View.GONE);
                    editEventTime.setVisibility(View.GONE);
                    saveEvent.setVisibility(View.GONE);

                    editEventDate.setText(" Set event date ");
                } else {
                    if (eventDatePicker.getDayOfMonth() < 10) {
                        if ((eventDatePicker.getMonth() + 1) < 10) {
                            eventDate.setText("0" + eventDatePicker.getDayOfMonth() + ".0" + (eventDatePicker.getMonth() + 1) + "." + eventDatePicker.getYear());
                        } else {
                            eventDate.setText("0" + eventDatePicker.getDayOfMonth() + "." + (eventDatePicker.getMonth() + 1) + "." + eventDatePicker.getYear());
                        }
                    } else if ((eventDatePicker.getMonth() + 1) < 10) {
                        eventDate.setText(eventDatePicker.getDayOfMonth() + ".0" + (eventDatePicker.getMonth() + 1) + "." + eventDatePicker.getYear());
                    } else eventDate.setText(eventDatePicker.getDayOfMonth() + "." + (eventDatePicker.getMonth() + 1) + "." + eventDatePicker.getYear());

                    editEventDate.setText(" Edit event date ");
                    eventDatePicker.setVisibility(View.GONE);
                    eventName.setVisibility(View.VISIBLE);
                    eventInformation.setVisibility(View.VISIBLE);
                    eventAddress.setVisibility(View.VISIBLE);
                    eventPassword.setVisibility(View.VISIBLE);
                    eventTime.setVisibility(View.VISIBLE);
                    editEventTime.setVisibility(View.VISIBLE);
                    saveEvent.setVisibility(View.VISIBLE);
                }
            }
        });

        editEventTime.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (eventTimePicker.getVisibility() == View.GONE) {
                    eventTimePicker.setVisibility(View.VISIBLE);
                    eventName.setVisibility(View.GONE);
                    eventInformation.setVisibility(View.GONE);
                    eventAddress.setVisibility(View.GONE);
                    eventPassword.setVisibility(View.GONE);
                    eventDate.setVisibility(View.GONE);
                    editEventDate.setVisibility(View.GONE);
                    saveEvent.setVisibility(View.GONE);

                    editEventTime.setText(" Set event time ");
                } else {
                    if (eventTimePicker.getMinute() < 10) {
                        if (eventTimePicker.getHour() < 10) {
                            eventTime.setText("0" + eventTimePicker.getHour() + ":0" + eventTimePicker.getMinute());
                        } else {
                            eventTime.setText(eventTimePicker.getHour() + ":0" + eventTimePicker.getMinute());
                        }
                    } else if (eventTimePicker.getHour() < 10) {
                        eventTime.setText("0" + eventTimePicker.getHour() + ":" + eventTimePicker.getMinute());
                    } else eventTime.setText(eventTimePicker.getHour() + ":" + eventTimePicker.getMinute());

                    editEventTime.setText(" Edit event time ");
                    eventTimePicker.setVisibility(View.GONE);
                    eventName.setVisibility(View.VISIBLE);
                    eventInformation.setVisibility(View.VISIBLE);
                    eventAddress.setVisibility(View.VISIBLE);
                    eventPassword.setVisibility(View.VISIBLE);
                    eventDate.setVisibility(View.VISIBLE);
                    editEventDate.setVisibility(View.VISIBLE);
                    saveEvent.setVisibility(View.VISIBLE);
                }
            }
        });

        saveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String saveEventName = eventName.getText().toString();
                String saveEventInformation = eventInformation.getText().toString();
                String saveEventAddress = eventAddress.getText().toString();
                String saveEventPassword = eventPassword.getText().toString();
                String saveEventTime = eventTime.getText().toString();
                String saveEventDate = eventDate.getText().toString();
                String saveAvailableSlots = availableSlots.getText().toString();

                if (saveEventName.isEmpty()) {
                    Toast.makeText(CreateEventActivity.this, "You have to enter event name first", Toast.LENGTH_SHORT).show();
                    return;
                } else if (saveEventAddress.isEmpty()) {
                    Toast.makeText(CreateEventActivity.this, "You have to enter event address", Toast.LENGTH_SHORT).show();
                    return;
                } else if (saveEventDate.equals("DD.MM.YYYY")) {
                    Toast.makeText(CreateEventActivity.this, "You have to select event date first", Toast.LENGTH_SHORT).show();
                    return;
                } else if (saveEventTime.equals("HH:MM")) {
                    Toast.makeText(CreateEventActivity.this, "You have to select event time first", Toast.LENGTH_SHORT).show();
                    return;
                } else if (saveAvailableSlots.isEmpty()) {
                    Toast.makeText(CreateEventActivity.this, "You have to enter how many people can participate", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    int slots = Integer.parseInt(saveAvailableSlots);
                    if (slots < 2) {
                        Toast.makeText(CreateEventActivity.this, "Event must have more than one participant", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        HashMap<String, Object> eventMap = new HashMap<>();
                        eventMap.put("creatorID", currentUserID);
                        eventMap.put("slots", saveAvailableSlots);
                        eventMap.put("information", saveEventInformation);
                        eventMap.put("address", saveEventAddress);
                        eventMap.put("password", saveEventPassword);
                        eventMap.put("time", saveEventTime);
                        eventMap.put("date", saveEventDate);
                        eventRef.child(saveEventName).child("Information").updateChildren(eventMap);

                        Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void initializeFields() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Event creator");

        eventName = (EditText) findViewById(R.id.edit_text_event_name);
        eventInformation = (EditText) findViewById(R.id.edit_text_additional_information);
        availableSlots = (EditText) findViewById(R.id.edit_text_available_slots);
        eventAddress = (EditText) findViewById(R.id.edit_text_event_place);
        eventPassword = (EditText) findViewById(R.id.edit_text_event_password);

        eventTime = (TextView) findViewById(R.id.event_time_text_view);
        eventDate = (TextView) findViewById(R.id.event_date_text_view);

        eventTimePicker = (TimePicker) findViewById(R.id.event_time_picker);
        eventDatePicker = (DatePicker) findViewById(R.id.event_date_picker);

        editEventTime = (Button) findViewById(R.id.event_time_picker_button);
        editEventDate = (Button) findViewById(R.id.event_date_picker_button);
        saveEvent = (Button) findViewById(R.id.save_event_button);
    }
}
