package com.example.bgapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

    private DatabaseReference mRef;

    public EventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        eventsFragmentView = inflater.inflate(R.layout.fragment_events, container, false);

        mRef = FirebaseDatabase.getInstance().getReference().child("Events");

        initializeFields();

        retrieveAndDisplayEvents();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentEventName = parent.getItemAtPosition(position).toString();

                Intent eventChatIntent = new Intent(getContext(), EventChatActivity.class);
                eventChatIntent.putExtra("eventName", currentEventName);
                startActivity(eventChatIntent);
            }
        });

        return eventsFragmentView;

    }

    private void initializeFields() {
        listView = (ListView) eventsFragmentView.findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list_of_events);
        listView.setAdapter(arrayAdapter);

    }

    private void retrieveAndDisplayEvents() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    set.add(((DataSnapshot)iterator.next()).getKey());
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
