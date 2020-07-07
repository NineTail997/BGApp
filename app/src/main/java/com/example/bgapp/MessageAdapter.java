package com.example.bgapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.messageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private final Context context;

    public MessageAdapter(List<Messages> userMessagesList, Context context) {
        this.userMessagesList = userMessagesList;
        this.context = context;
    }

    public class messageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;

        public messageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public messageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new messageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final messageViewHolder holder, final int position) {

        String currentUserID = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessagesList.get(position);

        final String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String receiverProfileImage = dataSnapshot.child("image").getValue().toString();
                Glide.with(context)
                        .load(receiverProfileImage)
                        .placeholder(R.drawable.default_image)
                        .into(holder.receiverProfileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text")) {
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.senderMessageText.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(currentUserID)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_private_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage() + "\n" + messages.getDate() + " " + messages.getTime());
            } else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_private_messages_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage() + "\n" + messages.getDate() + " " + messages.getTime());
            }
        }

        if (fromUserID.equals(currentUserID)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete message for me",
                                        "Delete message for everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteSentMessage(position, holder);
                                } else if (which == 1) {
                                    deleteMessageForEveryone(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete message for me",
                                        "Cancel"
                                };
                        //Toast.makeText(holder.itemView.getContext(), userMessagesList.get(position).getTo(), Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    deleteReceivedMessage(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void  deleteSentMessage(final int position, final messageViewHolder holder) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.child("Private messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Message deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void  deleteReceivedMessage(final int position, final messageViewHolder holder) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

        mRef.child("Private messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Message deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void  deleteMessageForEveryone(final int position, final messageViewHolder holder) {
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.child("Private messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mRef.child("Private messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Message deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
