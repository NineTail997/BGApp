package com.example.bgapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class CollectionActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listView;
    private Button addBoardGame;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_games = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private FirebaseUser user;

    private String currentCollectionItem, currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserID = user.getUid();
        mRef = FirebaseDatabase.getInstance().getReference().child("Collections").child(currentUserID);

        initializeFields();

        retrieveAndDisplayCollection();

        addBoardGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBoardGameToCollection();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentCollectionItem = parent.getItemAtPosition(position).toString();

                removeBoardGameToCollection(currentCollectionItem);
            }
        });
    }

    private void initializeFields() {
        addBoardGame = (Button) findViewById(R.id.add_board_game_button);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Your collection");

        listView = (ListView) findViewById(R.id.your_collection_list);
        arrayAdapter = new ArrayAdapter<String>(CollectionActivity.this, android.R.layout.simple_list_item_1, list_of_games);
        listView.setAdapter(arrayAdapter);
    }

    private void retrieveAndDisplayCollection() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    currentCollectionItem = ((DataSnapshot)iterator.next()).getKey();
                    set.add(currentCollectionItem);
                }

                list_of_games.clear();
                list_of_games.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addBoardGameToCollection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CollectionActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter board game title:");

        final EditText boardGameTitle = new EditText(CollectionActivity.this);
        boardGameTitle.setHint("title");
        builder.setView(boardGameTitle);

        builder.setPositiveButton("Add game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredTitle = boardGameTitle.getText().toString();
                mRef.child(enteredTitle).setValue("saved");
                Toast.makeText(CollectionActivity.this, "Collection updated", Toast.LENGTH_SHORT).show();
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

    private void removeBoardGameToCollection(final String currentCollectionItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CollectionActivity.this, R.style.AlertDialog);
        builder.setTitle("Do you want to remove " + currentCollectionItem + " from your collection?");

        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRef.child(currentCollectionItem).removeValue();
                Toast.makeText(CollectionActivity.this, "Game successfully removed", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(CollectionActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}