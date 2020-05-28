package com.example.bgapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;
    private RecyclerView myEventInvitationsList;

    private DatabaseReference requestsRef, usersRef, contactsRef, eventRequestsRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestsRef = FirebaseDatabase.getInstance().getReference().child("Chat requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        eventRequestsRef = FirebaseDatabase.getInstance().getReference().child("Event requests").child(currentUserID);

        myRequestList = (RecyclerView) requestFragmentView.findViewById(R.id.friend_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        myEventInvitationsList = (RecyclerView) requestFragmentView.findViewById(R.id.event_invitations_list);
        myEventInvitationsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(requestsRef.child(currentUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, requestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, requestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final requestsViewHolder holder, final int position, @NonNull Contacts model) {
                        holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_reject_button).setVisibility(View.VISIBLE);

                        final String listUserID = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received")) {
                                        usersRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    if (isAdded()) {
                                                        final String profileImage = dataSnapshot.child("image").getValue().toString();
                                                        final String profileName = dataSnapshot.child("name").getValue().toString();
                                                        final String profileStatus = dataSnapshot.child("status").getValue().toString();

                                                        holder.userName.setText(profileName);
                                                        holder.userStatus.setText("invited you to friends.");
                                                        Glide.with(RequestFragment.this)
                                                                .load(profileImage)
                                                                .placeholder(R.drawable.default_image)
                                                                .into(holder.userImage);

                                                        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                contactsRef.child(currentUserID).child(listUserID)
                                                                        .child("Contacts").setValue("Saved")
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    contactsRef.child(listUserID).child(currentUserID)
                                                                                            .child("Contacts").setValue("Saved")
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        requestsRef.child(currentUserID).child(listUserID)
                                                                                                                .removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if (task.isSuccessful()) {
                                                                                                                            requestsRef.child(listUserID).child(currentUserID)
                                                                                                                                    .removeValue()
                                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                            if (task.isSuccessful()) {
                                                                                                                                                Toast.makeText(getContext(), "Added new friend", Toast.LENGTH_SHORT).show();
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });

                                                            }
                                                        });

                                                        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                requestsRef.child(currentUserID).child(listUserID)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    requestsRef.child(listUserID).child(currentUserID)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        Toast.makeText(getContext(), "Friend request rejected", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        });

                                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                CharSequence options[] = new CharSequence[] {
                                                                        "Accept",
                                                                        "Reject"
                                                                };
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                builder.setTitle(profileName + " friend request");

                                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        if (which == 0) {
                                                                            contactsRef.child(currentUserID).child(listUserID)
                                                                                    .child("Contacts").setValue("Saved")
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                contactsRef.child(listUserID).child(currentUserID)
                                                                                                        .child("Contacts").setValue("Saved")
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful()) {
                                                                                                                    requestsRef.child(currentUserID).child(listUserID)
                                                                                                                            .removeValue()
                                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                    if (task.isSuccessful()) {
                                                                                                                                        requestsRef.child(listUserID).child(currentUserID)
                                                                                                                                                .removeValue()
                                                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                                    @Override
                                                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                        if (task.isSuccessful()) {
                                                                                                                                                            Toast.makeText(getContext(), "Added new friend", Toast.LENGTH_SHORT).show();
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                });
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                        if (which == 1) {
                                                                            requestsRef.child(currentUserID).child(listUserID)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                requestsRef.child(listUserID).child(currentUserID)
                                                                                                        .removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful()) {
                                                                                                                    Toast.makeText(getContext(), "Friend request rejected", Toast.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });
                                                                builder.show();
                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else if (type.equals("sent")) {
                                        holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.GONE);
                                        holder.rejectButton.setText("Cancel friend request");


                                        usersRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    if (isAdded()) {
                                                        final String profileImage = dataSnapshot.child("image").getValue().toString();
                                                        final String profileName = dataSnapshot.child("name").getValue().toString();
                                                        final String profileStatus = dataSnapshot.child("status").getValue().toString();

                                                        holder.userName.setText(profileName);
                                                        holder.userStatus.setText("You have sent a friend request to " + profileName);
                                                        Glide.with(RequestFragment.this)
                                                                .load(profileImage)
                                                                .placeholder(R.drawable.default_image)
                                                                .into(holder.userImage);

                                                        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                requestsRef.child(currentUserID).child(listUserID)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    requestsRef.child(listUserID).child(currentUserID)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        Toast.makeText(getContext(), "Friend request rejected", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        });

                                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                CharSequence options[] = new CharSequence[] {
                                                                        "Cancel friend request"
                                                                };
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                builder.setTitle("Your friend request to " + profileName);

                                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        if (which == 0) {
                                                                            requestsRef.child(currentUserID).child(listUserID)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                requestsRef.child(listUserID).child(currentUserID)
                                                                                                        .removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful()) {
                                                                                                                    Toast.makeText(getContext(), "You have cancelled friend request", Toast.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });
                                                                builder.show();
                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public requestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        requestsViewHolder holder = new requestsViewHolder(view);
                        return holder;
                    }
                };

        FirebaseRecyclerOptions<Contacts> eventOptions =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(eventRequestsRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, requestsViewHolder> eventAdapter =
                new FirebaseRecyclerAdapter<Contacts, requestsViewHolder>(eventOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull final requestsViewHolder holder, final int position, @NonNull final Contacts model) {
                        final String eventName = getRef(position).getKey();
                        eventRequestsRef.child(eventName).child("invited_by")
                                .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                    String inviterID = dataSnapshot.getValue().toString();
                                    usersRef.child(inviterID).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (isAdded()) {
                                                holder.userName.setText(dataSnapshot.child("name").getValue().toString());
                                                holder.userStatus.setText(" invited you to event:\n " + eventName);
                                                Glide.with(getContext())
                                                        .load(dataSnapshot.child("image").getValue().toString())
                                                        .placeholder(R.drawable.default_image)
                                                        .into(holder.userImage);

                                                holder.acceptButton.setVisibility(View.VISIBLE);
                                                holder.rejectButton.setVisibility(View.VISIBLE);

                                                holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        cancelInvitation(eventName);
                                                        joinEvent(eventName, "no");
                                                    }
                                                });

                                                holder.rejectButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        cancelInvitation(eventName);
                                                        Toast.makeText(getContext(), "Invitation cancelled", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
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

                    @NonNull
                    @Override
                    public requestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        requestsViewHolder viewHolder = new requestsViewHolder(view);
                        return viewHolder;
                    }
                };

        myRequestList.setAdapter(adapter);
        adapter.startListening();

        myEventInvitationsList.setAdapter(eventAdapter);
        eventAdapter.startListening();
    }

    public static class requestsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView userImage;
        Button acceptButton, rejectButton;

        public requestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_button);
            rejectButton = itemView.findViewById(R.id.request_reject_button);
        }
    }

    private void joinEvent(String currentEventName, String isEventParticipant) {
        Intent eventChatIntent = new Intent(getContext(), EventInformationActivity.class);
        eventChatIntent.putExtra("eventName", currentEventName);
        eventChatIntent.putExtra("isEventParticipant", isEventParticipant);
        startActivity(eventChatIntent);
    }

    private void cancelInvitation(String currentEventName) {
        eventRequestsRef.child(currentEventName)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Action confirmed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
