package com.example.bgapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    private String currentUserName, currentUserID, currentUserImage, isTimeValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserName = user.getDisplayName();
        currentUserID = user.getUid();
        currentUserImage = user.getPhotoUrl().toString();
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
                    availableSlots.setVisibility(View.GONE);

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
                    availableSlots.setVisibility(View.VISIBLE);
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
                    availableSlots.setVisibility(View.GONE);

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
                    availableSlots.setVisibility(View.VISIBLE);
                }
            }
        });

        saveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String saveEventName = eventName.getText().toString();
                final String saveEventInformation = eventInformation.getText().toString();
                final String saveEventAddress = eventAddress.getText().toString();
                final String[] saveEventPassword = {eventPassword.getText().toString()};
                final String saveEventTime = eventTime.getText().toString();
                final String saveEventDate = eventDate.getText().toString();
                final String saveAvailableSlots = availableSlots.getText().toString();

                checkEventTime(saveEventDate, saveEventTime);

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
                } else if (isTimeValid == "no") {
                    Toast.makeText(CreateEventActivity.this, "Event can't be created with selected time\nChoose time and date at least 30 min from now", Toast.LENGTH_SHORT).show();
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
                        eventRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists() && dataSnapshot.hasChild(saveEventName)) {
                                    Toast.makeText(CreateEventActivity.this, "There is already event using that name\nPlease type different event name", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (saveEventPassword[0].isEmpty()) saveEventPassword[0] = null;
                                    HashMap<String, Object> eventMap = new HashMap<>();
                                        eventMap.put("creatorID", currentUserID);
                                        eventMap.put("slots", saveAvailableSlots);
                                        eventMap.put("information", saveEventInformation);
                                        eventMap.put("address", saveEventAddress);
                                        eventMap.put("password", saveEventPassword[0]);
                                        eventMap.put("time", saveEventTime);
                                        eventMap.put("date", saveEventDate);
                                    eventRef.child(saveEventName).child("Information").updateChildren(eventMap);

                                    HashMap<String, Object> eventCreator = new HashMap<>();
                                        eventCreator.put("name", currentUserName);
                                        eventCreator.put("status", "Event creator");
                                        eventCreator.put("image", currentUserImage);
                                    eventRef.child(saveEventName).child("Participants").child(currentUserID).updateChildren(eventCreator);

                                    Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                                    moveToMain();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        });
    }

    private void moveToMain() {
        finish();
        Intent intent = new Intent(CreateEventActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void checkEventTime(String date, String time) {
        String currentTime;
        String selectedEventTime = date + " " + time;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 29);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        currentTime = sdf.format(calendar.getTime());

        Date currentDate = null;
        try {
            currentDate = sdf.parse(currentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date selectedDate = null;
        try {
            selectedDate = sdf.parse(selectedEventTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(selectedDate != null && currentDate != null) {
            if (!selectedDate.after(currentDate)) {
                isTimeValid = "no";
            } else isTimeValid = "yes";
        }
    }

    private void initializeFields() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(CreateEventActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
