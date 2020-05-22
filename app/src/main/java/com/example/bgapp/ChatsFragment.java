package com.example.bgapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList = (RecyclerView) privateChatsView.findViewById(R.id.chatsList);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, chatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, chatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final chatsViewHolder holder, int position, @NonNull Contacts model) {
                        final String usersID = getRef(position).getKey();

                        usersRef.child(usersID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    final String profileImage = dataSnapshot.child("image").getValue().toString();
                                    final String profileName = dataSnapshot.child("name").getValue().toString();
                                    final String profileStatus = dataSnapshot.child("status").getValue().toString();

                                    if(dataSnapshot.child("user_state").hasChild("state")) {
                                        String state = dataSnapshot.child("user_state").child("state").getValue().toString();
                                        String date = dataSnapshot.child("user_state").child("date").getValue().toString();
                                        String time = dataSnapshot.child("user_state").child("time").getValue().toString();

                                        if(state.equals("online")) {
                                            holder.userStatus.setText("online");
                                        } else if(state.equals("offline")) {
                                            holder.userStatus.setText("Last seen: " + date + " " + time);

                                        }
                                    } else {
                                        holder.userStatus.setText("offline");
                                    }

                                    holder.userName.setText(profileName);
                                    Glide.with(ChatsFragment.this)
                                            .load(profileImage)
                                            .placeholder(R.drawable.default_image)
                                            .into(holder.userImage);

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), PrivateMessageActivity.class);
                                            chatIntent.putExtra("visit_user_id", usersID);
                                            chatIntent.putExtra("visit_user_name", profileName);
                                            chatIntent.putExtra("visit_user_image", profileImage);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        chatsViewHolder viewHolder = new chatsViewHolder(view);
                        return viewHolder;
                    }
                };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class chatsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView userImage;

        public chatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userImage = itemView.findViewById(R.id.users_profile_image);

        }
    }
}
