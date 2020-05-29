package com.example.bgapp;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class HelperAdapter extends RecyclerView.Adapter<HelperAdapter.menuItemViewHolder> {
    private List<String> menuOptionsList, menuDescriptionList, menuImagesList;

    private final Context context;

    public HelperAdapter(List<String> menuOptionsList, List<String> menuDescriptionList, List<String> menuImagesList, Context context) {
        this.menuOptionsList = menuOptionsList;
        this.menuDescriptionList = menuDescriptionList;
        this.menuImagesList = menuImagesList;
        this.context = context;
    }

    public class menuItemViewHolder extends RecyclerView.ViewHolder {
        public TextView gameHelperNameTextView, gameHelperDescriptionTextView;
        public ImageView gameHelperImageView;

        public menuItemViewHolder(@NonNull View itemView) {
            super(itemView);

            gameHelperNameTextView = (TextView) itemView.findViewById(R.id.game_helper_name);
            gameHelperDescriptionTextView = (TextView) itemView.findViewById(R.id.game_helper_description);
            gameHelperImageView = (ImageView) itemView.findViewById(R.id.game_helper_image);
        }
    }

    @NonNull
    @Override
    public menuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.helpers_display_layout, parent, false);

        return new HelperAdapter.menuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull menuItemViewHolder holder, final int position) {
        final String currentHelperName = menuOptionsList.get(position);
        final String currentHelperDescription = menuDescriptionList.get(position);
        final String currentHelperImage = menuImagesList.get(position);

        Glide.with(context)
                .load(GetImage(context, currentHelperImage))
                .placeholder(R.drawable.default_image)
                .into(holder.gameHelperImageView);

        holder.gameHelperNameTextView.setText(currentHelperName);
        holder.gameHelperDescriptionTextView.setText(currentHelperDescription);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    moveToTriominosHelper();
                } else {
                    moveToDiceRolling(currentHelperName, currentHelperDescription, currentHelperImage);
                }
            }
        });
    }


    private void moveToTriominosHelper() {
        Intent intent = new Intent(context, TriominoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    private void moveToDiceRolling(String name, String description, String image) {
        Intent intent = new Intent(context, DiceRollingActivity.class);
        intent.putExtra("diceName", name);
        intent.putExtra("diceDescription", description);
        intent.putExtra("diceImage", image);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return menuOptionsList.size();
    }

    public static Drawable GetImage(Context context, String ImageName) {
        return context.getResources().getDrawable(context.getResources().getIdentifier(ImageName, "drawable", context.getPackageName()));
    }

}
