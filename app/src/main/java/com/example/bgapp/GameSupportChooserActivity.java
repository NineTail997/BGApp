package com.example.bgapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class GameSupportChooserActivity extends AppCompatActivity {

    private final List<String> menuOptionsList = new ArrayList<>();
    private final List<String> menuImagesList = new ArrayList<>();
    private final List<String> menuDescriptionList = new ArrayList<>();


    private Toolbar toolbar;

    private LinearLayoutManager linearLayoutManager;
    private HelperAdapter helperAdapter;
    private RecyclerView menuOptionsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_support_chooser);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Games helpers");

        helperAdapter = new HelperAdapter(menuOptionsList, menuDescriptionList, menuImagesList, getApplicationContext());
        menuOptionsRecyclerView = (RecyclerView) findViewById(R.id.choose_helper_recycler_list);
        linearLayoutManager = new LinearLayoutManager(this);
        menuOptionsRecyclerView.setLayoutManager(linearLayoutManager);
        menuOptionsRecyclerView.setAdapter(helperAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        menuOptionsList.add("Triominos helper");
        menuOptionsList.add("Dice 4");
        menuOptionsList.add("Dice 6");
        menuOptionsList.add("Dice 8");
        menuOptionsList.add("Dice 10");
        menuOptionsList.add("Dice 12");
        menuOptionsList.add("Dice 20");
        menuOptionsList.add("Dice 100");

        menuImagesList.add("triominos");
        menuImagesList.add("d4");
        menuImagesList.add("d6");
        menuImagesList.add("d8");
        menuImagesList.add("d10");
        menuImagesList.add("d12");
        menuImagesList.add("d20");
        menuImagesList.add("d100");

        menuDescriptionList.add("Triominos points counter");
        menuDescriptionList.add("Roll 4-sided dice");
        menuDescriptionList.add("Roll 6-sided dice");
        menuDescriptionList.add("Roll 8-sided dice");
        menuDescriptionList.add("Roll 10-sided dice");
        menuDescriptionList.add("Roll 12-sided dice");
        menuDescriptionList.add("Roll 20-sided dice");
        menuDescriptionList.add("Roll 100-sided dice");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.helper_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.startMenu:
                Intent editIntent = new Intent(GameSupportChooserActivity.this, StartActivity.class);
                startActivity(editIntent);
                break;


            case R.id.menuLogout:
                FirebaseAuth.getInstance().signOut();
                Intent logoutIntent = new Intent(GameSupportChooserActivity.this, SignInActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
                finish();
                FirebaseAuth.getInstance().signOut();
                break;
        }
        return true;
    }
}
