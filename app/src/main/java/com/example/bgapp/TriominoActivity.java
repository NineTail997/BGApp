package com.example.bgapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TriominoActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button addPlayerButton, startGameButton, addZeroPoints, addOnePoint, addTwoPoints, addThreePoints, addFourPoints, addFivePoints, addSixPoints,
            addSevenPoints, addEightPoints, addNinePoints, addTenPoints, addElevenPoints, addTwelvePoints, addThirteenPoints, addFourteenPoints,
            addFifteenPoints, minusOnePoint, minusTwoPoints, minusThreePoints, minusFourPoints, minusFivePoints, nextPlayerButton, setWinnerButton;
    private EditText playerName, winnerAdditionalPoints;
    private TextView currentPlayerNameTextView, currentPlayerScoreTextView, scoreListTextView;
    private CheckBox addFortyPoints, addFiftyPoints, addSixtyPoints;
    private LinearLayout addPointsLinearLayout;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference privateGameRef;

    private String currentUserID, playersList, currentPlayerName, isWinnerFound = "no";
    private int playersCount = 0, currentPlayerNumber = 1, pointsToAdd = 0, additionalPointsToAdd = 0, winnerCurrentQueue = 0, queueDifference = 0;
    private long currentPlayerPoints = 0;

    private List<String> listName = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triomino);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserID = user.getUid();
        privateGameRef = FirebaseDatabase.getInstance().getReference().child("Private games").child(currentUserID);

        initializeFields();

        resetCurrentUserGame();
    }

    @Override
    protected void onStart() {
        super.onStart();

        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newPlayer;
                newPlayer = playerName.getText().toString();
                currentPlayerNameTextView.setVisibility(View.VISIBLE);

                if (newPlayer.isEmpty()) {
                    Toast.makeText(TriominoActivity.this, "Please enter unique player name", Toast.LENGTH_SHORT).show();
                } else {
                    privateGameRef.child("Triomino").child("Players")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild(newPlayer)) {
                                    Toast.makeText(TriominoActivity.this, "You have already player with this name", Toast.LENGTH_SHORT).show();
                                } else {
                                    playersCount = (int) dataSnapshot.getChildrenCount();
                                    playersCount += 1;
                                    HashMap<String, Object> playerCreator = new HashMap<>();
                                        playerCreator.put("queue", playersCount);
                                        playerCreator.put("points", 0);
                                    privateGameRef.child("Triomino").child("Players").child(newPlayer).updateChildren(playerCreator);
                                    playersList += "\n" + Integer.toString(playersCount) + ". " + newPlayer;
                                    listName.add(playersCount - 1, newPlayer);
                                }
                            } else {
                                HashMap<String, Object> playerCreator = new HashMap<>();
                                    playerCreator.put("queue", 1);
                                    playerCreator.put("points", 0);
                                privateGameRef.child("Triomino").child("Players").child(newPlayer).updateChildren(playerCreator);
                                playersList = "1. " + newPlayer;
                                listName.add(0, newPlayer);
                            }
                            playerName.setText("");
                            currentPlayerNameTextView.setText(playersList);
                            if (playersCount > 1) startGameButton.setEnabled(true);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPointsLinearLayout.setVisibility(View.VISIBLE);
                currentPlayerScoreTextView.setVisibility(View.VISIBLE);
                currentPlayerNameTextView.setVisibility(View.VISIBLE);
                nextPlayerButton.setVisibility(View.VISIBLE);
                setWinnerButton.setVisibility(View.VISIBLE);
                nextPlayerButton.setEnabled(false);
                setWinnerButton.setEnabled(false);

                playerName.setVisibility(View.GONE);
                addPlayerButton.setVisibility(View.GONE);
                startGameButton.setVisibility(View.GONE);
                scoreListTextView.setVisibility(View.GONE);
                returnCurrentPlayerNameAndPoints();

                playersList = "";
            }
        });

        addZeroPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 0;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 0pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 0pts = " + Long.toString(currentPlayerPoints) + "pts");
            }
        });

        addOnePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 1;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 1pt + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 1) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 1pt = " + Long.toString(currentPlayerPoints + 1) + "pts");
            }
        });

        addTwoPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 2;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 2pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 2) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 2pts = " + Long.toString(currentPlayerPoints + 2) + "pts");
            }
        });

        addThreePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 3;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 3pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 3) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 3pts = " + Long.toString(currentPlayerPoints + 3) + "pts");
            }
        });

        addFourPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 4;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 4pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 4) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 4pts = " + Long.toString(currentPlayerPoints + 4) + "pts");
            }
        });

        addFivePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 5;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 5pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 5) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 5pts = " + Long.toString(currentPlayerPoints + 5) + "pts");
            }
        });

        addSixPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 6;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 6pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 6) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 6pts = " + Long.toString(currentPlayerPoints + 6) + "pts");
            }
        });

        addSevenPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 7;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 7pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 7) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 7pts = " + Long.toString(currentPlayerPoints + 7) + "pts");
            }
        });

        addEightPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 8;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 8pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 8) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 8pts = " + Long.toString(currentPlayerPoints + 8) + "pts");
            }
        });

        addNinePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 9;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 9pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 9) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 9pts = " + Long.toString(currentPlayerPoints + 9) + "pts");
            }
        });

        addTenPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 10;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 10pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 10) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 10pts = " + Long.toString(currentPlayerPoints + 10) + "pts");
            }
        });

        addElevenPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 11;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 11pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 11) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 11pts = " + Long.toString(currentPlayerPoints + 11) + "pts");
            }
        });

        addTwelvePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 12;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 12pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 12) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 12pts = " + Long.toString(currentPlayerPoints + 12) + "pts");
            }
        });

        addThirteenPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 13;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 13pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 13) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 13pts = " + Long.toString(currentPlayerPoints + 13) + "pts");
            }
        });

        addFourteenPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 14;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 14pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 14) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 14pts = " + Long.toString(currentPlayerPoints + 14) + "pts");
            }
        });

        addFifteenPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = 15;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 15pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd + 15) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + 15pts = " + Long.toString(currentPlayerPoints + 15) + "pts");
            }
        });

        minusOnePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = -1;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts - 1pt + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd - 1) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts - 1pt = " + Long.toString(currentPlayerPoints - 1) + "pts");
            }
        });

        minusTwoPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = -2;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts - 2pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd - 2) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts - 2pts = " + Long.toString(currentPlayerPoints - 2) + "pts");
            }
        });

        minusThreePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = -3;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts - 3pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd - 3) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts - 3pts = " + Long.toString(currentPlayerPoints - 3) + "pts");
            }
        });

        minusFourPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = -4;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts - 4pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd - 4) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts - 4pts = " + Long.toString(currentPlayerPoints - 4) + "pts");
            }
        });

        minusFivePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPlayerButton.setEnabled(true);
                if (isWinnerFound.equals("no")) {
                    setWinnerButton.setEnabled(true);
                }

                pointsToAdd = -5;

                if (addFortyPoints.isChecked() || addFiftyPoints.isChecked() || addSixtyPoints.isChecked()) {
                    currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts - 5pts + " + additionalPointsToAdd + "pts = " + Long.toString(currentPlayerPoints + additionalPointsToAdd - 5) + "pts");
                } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts - 5pts = " + Long.toString(currentPlayerPoints - 5) + "pts");
            }
        });

        addFortyPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addFortyPoints.isChecked()) {
                    addFiftyPoints.setChecked(false);
                    addSixtyPoints.setChecked(false);
                    if (pointsToAdd < 0) {
                        currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts " + Integer.toString(pointsToAdd) + "pts + 40pts = " + Long.toString(currentPlayerPoints + 40 + pointsToAdd) + "pts");
                    } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + " + Integer.toString(pointsToAdd) + "pts + 40pts = " + Long.toString(currentPlayerPoints + 40 + pointsToAdd) + "pts");
                } else {
                    additionalPointsToAdd = 0;
                    if (pointsToAdd < 0) {
                        currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts " + Integer.toString(pointsToAdd) + "pts = " + Long.toString(currentPlayerPoints + pointsToAdd) + "pts");
                    } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + " + Integer.toString(pointsToAdd) + "pts = " + Long.toString(currentPlayerPoints + pointsToAdd) + "pts");
                }
            }
        });

        addFiftyPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addFiftyPoints.isChecked()) {
                    addFortyPoints.setChecked(false);
                    addSixtyPoints.setChecked(false);
                    additionalPointsToAdd = 50;
                    if (pointsToAdd < 0) {
                        currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts " + Integer.toString(pointsToAdd) + "pts + 50pts = " + Long.toString(currentPlayerPoints + 50 + pointsToAdd) + "pts");
                    } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + " + Integer.toString(pointsToAdd) + "pts + 50pts = " + Long.toString(currentPlayerPoints + 50 + pointsToAdd) + "pts");
                } else {
                    additionalPointsToAdd = 0;
                    if (pointsToAdd<0) {
                        currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts " + Integer.toString(pointsToAdd) + "pts = " + Long.toString(currentPlayerPoints + pointsToAdd) + "pts");
                    } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + " + Integer.toString(pointsToAdd) + "pts = " + Long.toString(currentPlayerPoints + pointsToAdd) + "pts");
                }
            }
        });

        addSixtyPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addSixtyPoints.isChecked()) {
                    addFiftyPoints.setChecked(false);
                    addFortyPoints.setChecked(false);
                    additionalPointsToAdd = 60;
                    if (pointsToAdd < 0) {
                        currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts " + Integer.toString(pointsToAdd) + "pts + 60pts = " + Long.toString(currentPlayerPoints + 60 + pointsToAdd) + "pts");
                    } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + " + Integer.toString(pointsToAdd) + "pts + 60pts = " + Long.toString(currentPlayerPoints + 60 + pointsToAdd) + "pts");
                } else {
                    additionalPointsToAdd = 0;
                    if (pointsToAdd<0) {
                        currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts " + Integer.toString(pointsToAdd) + "pts = " + Long.toString(currentPlayerPoints + pointsToAdd) + "pts");
                    } else currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts + " + Integer.toString(pointsToAdd) + "pts = " + Long.toString(currentPlayerPoints + pointsToAdd) + "pts");
                }
            }
        });

        nextPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TriominoActivity.this, Integer.toString(pointsToAdd + additionalPointsToAdd) + " points added for player: " + currentPlayerName, Toast.LENGTH_SHORT).show();
                privateGameRef.child("Triomino").child("Players").child(currentPlayerName).child("points").setValue(pointsToAdd + additionalPointsToAdd);

                pointsToAdd = 0;
                additionalPointsToAdd = 0;

                nextPlayerButton.setEnabled(false);
                setWinnerButton.setEnabled(false);

                addFortyPoints.setChecked(false);
                addFiftyPoints.setChecked(false);
                addSixtyPoints.setChecked(false);

                if (isWinnerFound.equals("no")) {
                    if (currentPlayerNumber == playersCount) {
                        currentPlayerNumber = 1;
                    } else currentPlayerNumber += 1;
                } else {
                    if ((currentPlayerNumber + 1) == playersCount) {
                        nextPlayerButton.setText("Add points");
                        currentPlayerNumber += 1;
                    } else if (currentPlayerNumber == playersCount) {
                        addPointsLinearLayout.setVisibility(View.GONE);
                        setWinnerButton.setEnabled(true);
                        winnerAdditionalPoints.setVisibility(View.VISIBLE);
                        currentPlayerNumber = winnerCurrentQueue;
                    } else currentPlayerNumber += 1;
                }
                returnCurrentPlayerNameAndPoints();
            }
        });

        setWinnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWinnerFound.equals("no")) {
                    isWinnerFound = "yes";
                    winnerCurrentQueue = currentPlayerNumber;
                    setWinnerButton.setEnabled(false);
                    setWinnerButton.setText("Finish game");
                } else {
                    String pointsString = winnerAdditionalPoints.getText().toString();
                    if (pointsString.isEmpty()) {
                        Toast.makeText(TriominoActivity.this, "Please enter points first", Toast.LENGTH_SHORT).show();
                    } else {
                        isWinnerFound = "no";
                        setWinnerButton.setText("Set winner");
                        int pointsInteger = Integer.parseInt(pointsString);
                        Toast.makeText(TriominoActivity.this, pointsString + " points added for player: " + currentPlayerName, Toast.LENGTH_SHORT).show();
                        privateGameRef.child("Triomino").child("Players").child(currentPlayerName).child("points").setValue(currentPlayerPoints + pointsInteger);

                        nextPlayerButton.setText("Next player");
                        nextPlayerButton.setVisibility(View.INVISIBLE);
                        setWinnerButton.setVisibility(View.INVISIBLE);
                        winnerAdditionalPoints.setVisibility(View.GONE);
                        currentPlayerScoreTextView.setVisibility(View.GONE);
                        currentPlayerNameTextView.setVisibility(View.GONE);

                        addPlayerButton.setVisibility(View.VISIBLE);
                        addPlayerButton.setEnabled(false);
                        startGameButton.setVisibility(View.VISIBLE);

                        queueDifference = currentPlayerNumber - 1;

                        privateGameRef.child("Triomino").child("Players").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Iterator iterator = dataSnapshot.getChildren().iterator();

                                while (iterator.hasNext()) {
                                    String playerName = ((DataSnapshot)iterator.next()).getKey();

                                    long i = (long) dataSnapshot.child(playerName).child("queue").getValue();
                                    long points = (long) dataSnapshot.child(playerName).child("points").getValue();

                                    long newQueuePosition = 0;
                                    if ((i - queueDifference) > 0) {
                                        newQueuePosition = i - queueDifference;
                                    } else {
                                        newQueuePosition = playersCount + (i - queueDifference);
                                    }
                                    playersList += Long.toString(newQueuePosition) + ". " + playerName + " has " + Long.toString(points) + "pts\n";
                                    privateGameRef.child("Triomino").child("Players").child(playerName).child("queue").setValue(newQueuePosition);
                                }
                                scoreListTextView.setVisibility(View.VISIBLE);
                                scoreListTextView.setText(playersList);
                                currentPlayerNumber = 1;
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

    private void resetCurrentUserGame() {
        privateGameRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(TriominoActivity.this, "Previous game has been reset", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void returnCurrentPlayerNameAndPoints() {
        privateGameRef.child("Triomino").child("Players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterator iterator = dataSnapshot.getChildren().iterator();

                    while (iterator.hasNext()) {
                        String playerName = ((DataSnapshot)iterator.next()).getKey();

                        long i = (long) dataSnapshot.child(playerName).child("queue").getValue();
                        if (i == currentPlayerNumber) {
                            currentPlayerPoints = (long) dataSnapshot.child(playerName).child("points").getValue();
                            currentPlayerName = playerName;

                            currentPlayerNameTextView.setText(Integer.toString(currentPlayerNumber) + ". " + currentPlayerName);
                            currentPlayerScoreTextView.setText(Long.toString(currentPlayerPoints) + "pts");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        playerName = (EditText) findViewById(R.id.add_player_edit_text);
        winnerAdditionalPoints = (EditText) findViewById(R.id.winner_additional_points);
        currentPlayerNameTextView = (TextView) findViewById(R.id.current_player_name);
        currentPlayerScoreTextView = (TextView) findViewById(R.id.player_score);
        scoreListTextView = (TextView) findViewById(R.id.score_list);
        addPointsLinearLayout = (LinearLayout) findViewById(R.id.add_points) ;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Triomino points counter");

        addPlayerButton = (Button) findViewById(R.id.add_player_button);
        startGameButton = (Button) findViewById(R.id.start_game_button);
        addZeroPoints = (Button) findViewById(R.id.add_0_points);
        addOnePoint = (Button) findViewById(R.id.add_1_point);
        addTwoPoints = (Button) findViewById(R.id.add_2_points);
        addThreePoints = (Button) findViewById(R.id.add_3_points);
        addFourPoints = (Button) findViewById(R.id.add_4_points);
        addFivePoints = (Button) findViewById(R.id.add_5_points);
        addSixPoints = (Button) findViewById(R.id.add_6_points);
        addSevenPoints = (Button) findViewById(R.id.add_7_points);
        addEightPoints = (Button) findViewById(R.id.add_8_points);
        addNinePoints = (Button) findViewById(R.id.add_9_points);
        addTenPoints = (Button) findViewById(R.id.add_10_points);
        addElevenPoints = (Button) findViewById(R.id.add_11_points);
        addTwelvePoints = (Button) findViewById(R.id.add_12_points);
        addThirteenPoints = (Button) findViewById(R.id.add_13_points);
        addFourteenPoints = (Button) findViewById(R.id.add_14_points);
        addFifteenPoints = (Button) findViewById(R.id.add_15_points);
        minusOnePoint = (Button) findViewById(R.id.minus_1_point);
        minusTwoPoints = (Button) findViewById(R.id.minus_2_points);
        minusThreePoints = (Button) findViewById(R.id.minus_3_points);
        minusFourPoints = (Button) findViewById(R.id.minus_4_points);
        minusFivePoints = (Button) findViewById(R.id.minus_5_points);
        nextPlayerButton = (Button) findViewById(R.id.next_player_button);
        setWinnerButton = (Button) findViewById(R.id.set_winner_button);

        addFortyPoints = (CheckBox) findViewById(R.id.plus_40_check_box);
        addFiftyPoints = (CheckBox) findViewById(R.id.plus_50_check_box);
        addSixtyPoints = (CheckBox) findViewById(R.id.plus_60_check_box);

        startGameButton.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(TriominoActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
