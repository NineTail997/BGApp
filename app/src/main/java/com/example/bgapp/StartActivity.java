package com.example.bgapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class StartActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 150;

    private ImageView imageView;
    private EditText editText, editStatus;
    private TextView textView;

    private Uri uriProfileImage;
    private ProgressBar progressBar;

    private String profileImageUrl;
    private String currentUserID;
    private String currentUserName;

    private FirebaseAuth mAuth;
    private DatabaseReference mRef, notificationRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        user = mAuth.getCurrentUser();
        currentUserName = user.getDisplayName();
        profileImageUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;

        mRef = FirebaseDatabase.getInstance().getReference();
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editTextDisplayName);
        editStatus = (EditText) findViewById(R.id.editTextStatus);
        imageView = (ImageView) findViewById(R.id.imageView);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });

        loadUserInformation();

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });

        findViewById(R.id.buttonMove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.child("Users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("name")) {
                            moveToMain();
                        } else {
                            Toast.makeText(StartActivity.this, "Provide user information first", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.child("Users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("name")) {
                            moveToPlay();
                        } else {
                            Toast.makeText(StartActivity.this, "Provide user information first", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        findViewById(R.id.buttonCollection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.child("Users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("name")) {
                            moveToCollection();
                        } else {
                            Toast.makeText(StartActivity.this, "Provide user information first", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void moveToMain() {
        finish();
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void moveToPlay() {
        finish();
        Intent intent = new Intent(StartActivity.this, GameSupportChooserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void moveToCollection() {
        finish();
        Intent intent = new Intent(StartActivity.this, CollectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUserStatus("online");

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void loadUserInformation() {
        if (user != null) {
            mRef.child("Users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("image") && dataSnapshot.hasChild("name")) {
                        progressBar.setVisibility(View.VISIBLE);
                        mRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                                {
                                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                                    String retrievesStatus = dataSnapshot.child("status").getValue().toString();
                                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                                    textView.setText("\nWelcome " + retrieveUserName);
                                    editText.setVisibility(View.GONE);
                                    editText.setText(retrieveUserName);
                                    editStatus.setText(retrievesStatus);
                                    if (retrieveProfileImage != null) {
                                        Glide.with(getApplicationContext())
                                                .load(retrieveProfileImage)
                                                .placeholder(R.drawable.default_image)
                                                .into(imageView);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        progressBar.setVisibility(View.GONE);
                    } else {
                        editText.setVisibility(View.VISIBLE);
                        textView.setText("Tap on camera to choose profile picture");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            notificationRef.child(currentUserID).removeValue();
        }
    }

    private void saveUserInformation() {

        String displayName = editText.getText().toString();
        String displayStatus = editStatus.getText().toString();

        if (displayName.isEmpty()) {
            editText.setError("Name required");
            editText.requestFocus();
            return;
        }

        if (profileImageUrl == null) {
            Toast.makeText(StartActivity.this, "Please select profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && profileImageUrl != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();

            HashMap<String, Object> profileMap = new HashMap<>();
                profileMap.put("uid", currentUserID);
                profileMap.put("name", displayName);
                profileMap.put("status", displayStatus);
                profileMap.put("image", profileImageUrl);
            mRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(StartActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(StartActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                String message = task.getException().toString();
                                Toast.makeText(StartActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                imageView.setImageBitmap(bitmap);

                uploadImageToFirebaseStorage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebaseStorage() {
        StorageReference profileImageRef =
                FirebaseStorage.getInstance().getReference("profilepics/" + currentUserID + ".jpg");

        if (uriProfileImage != null) {
            progressBar.setVisibility(View.VISIBLE);
            profileImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri getDownloadUrl = urlTask.getResult();

                            profileImageUrl = String.valueOf(getDownloadUrl);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(StartActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuLogout:

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, SignInActivity.class));
                finish();

                break;
        }
        return true;
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd.MM.yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
            onlineStateMap.put("time", saveCurrentTime);
            onlineStateMap.put("date", saveCurrentDate);
            onlineStateMap.put("state", state);

        mRef.child("Users").child(currentUserID).child("user_state")
            .updateChildren(onlineStateMap);
    }
}
