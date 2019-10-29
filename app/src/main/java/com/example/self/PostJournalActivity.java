package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private Button postButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private EditText titleEditText;
    private EditText thoughtsEditText;
    private TextView postUserName;
    private TextView postDate;
    private ImageView imageView;

    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("Journal");
    private Uri imageUri;
    private String TAG = "POST JOURNAL TAG";
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.postProgressBar);
        titleEditText = findViewById(R.id.post_title);
        thoughtsEditText = findViewById(R.id.post_thought);
        postUserName = findViewById(R.id.post_username);
        postDate = findViewById(R.id.post_date);
        postButton = findViewById(R.id.postButton);
        addPhotoButton = findViewById(R.id.postCameraButton);
        imageView = findViewById(R.id.postImageView);

        postButton.setOnClickListener(this);
        addPhotoButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);

        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUsername();

            postUserName.setText(currentUserName);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null){

                } else {

                }
            }
        };

//        Bundle bundle = getIntent().getExtras();
//
//        if(bundle != null) {
//            String username = bundle.getString("username");
//            String userId = bundle.getString("userId");
//        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.postButton:
                //save journal
                saveJournal();
                break;
            case R.id.postCameraButton:
                //get image from gallery
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
        }
    }

    private void saveJournal() {
        progressBar.setVisibility(View.VISIBLE);
        final String title = titleEditText.getText().toString().trim();
        final String thoughts = thoughtsEditText.getText().toString().trim();


        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && imageUri != null) {

            final StorageReference filepath = storageReference //../journal_image/out_image.jpeg
                    .child("journal_images")
                    .child("my_image_" + Timestamp.now().getSeconds()); //to get a unique image name
            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imagerUrl = uri.toString();
                                    //Todo: Create Journal Object
                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thoughts);
                                    journal.setImageUrl(imagerUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserId(currentUserId);
                                    journal.setUsername(currentUserName);
                                    journal.setCount(count);

                                    //Todo: invoke our collection reference
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                                    finish();

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Log.d(TAG, "onFailure: " + e.getMessage());

                                                }
                                            });
                                    //Todo: and save a journal instance

                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            //data is what we are receiving from intent - the image
            if (data != null) {
                imageUri = data.getData();
                imageView.setImageResource(R.color.startGColor);
                imageView.setImageURI(imageUri);//shows image
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
