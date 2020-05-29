package com.example.bgapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import java.util.Random;

public class DiceRollingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView diceImageView;
    private TextView dicesCountTextView, titleTextView, currentRollsTextView;
    private Button plusOneDiceButton, minusOneDiceButton, rollDicesButton;
    private int dicesCount = 1;
    private int diceSides = 0;

    private String toolbarName, activityDescription, activityImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice_rolling);

        toolbarName = getIntent().getExtras().get("diceName").toString();
        activityDescription = getIntent().getExtras().get("diceDescription").toString();
        activityImage = getIntent().getExtras().get("diceImage").toString();

        diceSides = Integer.parseInt(toolbarName.replaceAll("Dice ", ""));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(toolbarName);

        diceImageView = (ImageView) findViewById(R.id.activity_image);
        titleTextView = (TextView) findViewById(R.id.title_text_view);
        currentRollsTextView = (TextView) findViewById(R.id.current_rolls);
        dicesCountTextView = (TextView) findViewById(R.id.dices_count);
        plusOneDiceButton = (Button) findViewById(R.id.plus_1_dice_button);
        minusOneDiceButton = (Button) findViewById(R.id.minus_1_dice_button);
        rollDicesButton = (Button) findViewById(R.id.roll_button);
    }

    @Override
    protected void onStart() {
        super.onStart();

        titleTextView.setText(activityDescription);

        Glide.with(DiceRollingActivity.this)
                .load(GetImage(activityImage))
                .placeholder(R.drawable.default_image)
                .into(diceImageView);

        plusOneDiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minusOneDiceButton.setEnabled(true);
                if (dicesCount < 5) {
                    dicesCount += 1;
                    dicesCountTextView.setText(Integer.toString(dicesCount) + " dices");
                } else {
                    Toast.makeText(DiceRollingActivity.this, "You can use max 5 dices", Toast.LENGTH_SHORT).show();
                    plusOneDiceButton.setEnabled(false);
                }
            }
        });

        minusOneDiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plusOneDiceButton.setEnabled(true);
                if (dicesCount == 1) {
                    Toast.makeText(DiceRollingActivity.this, "Please use al least 1 dice", Toast.LENGTH_SHORT).show();
                    minusOneDiceButton.setEnabled(false);
                } else {
                    dicesCount -= 1;
                    if (dicesCount == 1) {
                        dicesCountTextView.setText(Integer.toString(dicesCount) + " dice");
                    } else dicesCountTextView.setText(Integer.toString(dicesCount) + " dices");
                }
            }
        });

        rollDicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentRolls = "";
                String endings = "";
                int sum = 0;
                for (int i=0; i<dicesCount; i++) {
                    int random = new Random().nextInt(diceSides) + 1;
                    sum += random;
                    if (i==0) endings = "st";
                    if (i==1) endings = "nd";
                    if (i==2) endings = "rd";
                    if (i>2) endings = "th";
                    currentRolls += Integer.toString(i+1) + endings + " dice: " + Integer.toString(random) + "\n";
                }
                if (dicesCount != 1) currentRolls += "Sum: " + Integer.toString(sum);
                currentRollsTextView.setText(currentRolls);
            }
        });
    }

    public Drawable GetImage(String ImageName) {
        return this.getResources().getDrawable(this.getResources().getIdentifier(ImageName, "drawable", this.getPackageName()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(DiceRollingActivity.this, GameSupportChooserActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
