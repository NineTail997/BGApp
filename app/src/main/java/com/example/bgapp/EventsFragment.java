package com.example.bgapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventsFragment extends Fragment {

    private View eventsFragmentView;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_events = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private FirebaseUser user;

    private String eventRoomPassword, currentEventName, currentUserID;

    public EventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        eventsFragmentView = inflater.inflate(R.layout.fragment_events, container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserID = user.getUid();
        mRef = FirebaseDatabase.getInstance().getReference().child("Events");

        initializeFields();

        retrieveAndDisplayEvents();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentEventName = parent.getItemAtPosition(position).toString();

                mRef.child(currentEventName).child("Participants").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild(currentUserID)) {
                            if (isAdded()) {
                                accessRoom(currentEventName, "yes");
                            }
                        } else {
                            mRef.child(currentEventName).child("Information").child("password")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                eventRoomPassword = dataSnapshot.getValue().toString();
                                                requestEvent(currentEventName);
                                            } else if (isAdded()) {
                                                accessRoom(currentEventName, "no");
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
            }
        });

        return eventsFragmentView;

    }

    private void initializeFields() {
        listView = (ListView) eventsFragmentView.findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list_of_events);
        listView.setAdapter(arrayAdapter);
    }

    private void requestEvent(final String currentEventName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
        builder.setTitle("Enter  correct password:");

        final EditText eventPassword = new EditText(getContext());
        eventPassword.setHint("password");
        builder.setView(eventPassword);

        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPassword = eventPassword.getText().toString();

                if (enteredPassword.equals(eventRoomPassword)){
                    Toast.makeText(getContext(), "Correct password", Toast.LENGTH_SHORT).show();
                    if (isAdded()) {
                        accessRoom(currentEventName, "no");
                    }
                } else {
                    Toast.makeText(getContext(), "Wrong password", Toast.LENGTH_SHORT).show();
                }
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


    private void accessRoom(String currentEventName, String isEventParticipant) {
        Intent eventChatIntent = new Intent(getContext(), EventInformationActivity.class);
        eventChatIntent.putExtra("eventName", currentEventName);
        eventChatIntent.putExtra("isEventParticipant", isEventParticipant);
        startActivity(eventChatIntent);
    }

    private void retrieveAndDisplayEvents() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    currentEventName = ((DataSnapshot)iterator.next()).getKey();
                    set.add(currentEventName);
                }

                list_of_events.clear();
                list_of_events.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
