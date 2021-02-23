 package com.example.locationtracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;

public class WelcomeActivity extends AppCompatActivity {

    private static final int PICK_FROM_GALLERY = 1;
    Button clicktostart;
    Button creategroup;
    TextView displayname;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private ImageView profileImageView;
    private Uri resultUri;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        profileImageView = findViewById(R.id.profile_image);
        Button imageButton = findViewById(R.id.select_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .start(WelcomeActivity.this);

            }
        });
       /* imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);

            }
        });*/
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Log.d("currentuser", currentUser.getDisplayName());

        getSupportActionBar().setTitle("Welcome : " + currentUser.getDisplayName());
        Button confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(v -> {
            uploadPicturetoFirebase();
            User user = new User();
            user.setEmail(currentUser.getEmail());
            user.setName(currentUser.getDisplayName());
            user.setProfilepic(resultUri.toString());
            insertGroupDB(user);
        });


     /*   b=(Button)findViewById(R.id.btnSelectPhoto);
        viewImage=(ImageView)findViewById(R.id.viewImage);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

      */

    }

    private void uploadPicturetoFirebase() {
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

// Create a reference to "mountains.jpg"
        StorageReference mountainsRef = storageRef.child(currentUser.getEmail() + ".jpg");
        // Get the data from an ImageView as bytes
        profileImageView.setDrawingCacheEnabled(true);
        profileImageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }

    private void signOut() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent loginIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(loginIntent);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
            case R.id.addfriendbutton:
                Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
                startActivity(intent);
                return true;

        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                Glide.with(this)
                        .load(resultUri.toString())
                        .apply(new RequestOptions().fitCenter())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerInside()
                        .into(profileImageView);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void insertGroupDB(User user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String email = currentUser.getEmail();
        email = email.replace(".", ",");
        //preeti,deosthali@gmail,com
        DatabaseReference myRef = database.getReference("user").child(email);

        myRef.push().setValue(user);

    }

}




