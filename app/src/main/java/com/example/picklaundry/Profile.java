package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private TextView tvUserName, tvEmail, tvPhone, tvDOB, tvGender, tvUserName1, tvLocation;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog; // Add ProgressDialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Initialize TextViews
        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvDOB = findViewById(R.id.tvDOB);
        tvGender = findViewById(R.id.tvGender);
        tvLocation = findViewById(R.id.tvLocation);
        tvUserName1 = findViewById(R.id.tvUserName1);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Profile...");
        progressDialog.setCancelable(false);

        if (user != null) {
            progressDialog.show(); // Show loading dialog
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            getUserData();
        } else {
            Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show();
            Log.e("ProfileActivity", "User is not logged in.");
        }
    }

    private void getUserData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (progressDialog.isShowing()) progressDialog.dismiss(); // Hide loading dialog

                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("mobile").getValue(String.class);
                    String dob = snapshot.child("dob").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);

                    // Set Data with Null Check
                    tvUserName1.setText(name != null ? name : "N/A");
                    tvUserName.setText(name != null ? name : "N/A");
                    tvEmail.setText(email != null ? email : "N/A");
                    tvPhone.setText(phone != null ? phone : "N/A");
                    tvDOB.setText(dob != null ? dob : "N/A");
                    tvGender.setText(gender != null ? gender : "N/A");
                    tvLocation.setText(location != null ? location : "N/A");
                } else {
                    Toast.makeText(Profile.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    Log.e("ProfileActivity", "No user data found in Firebase.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressDialog.isShowing()) progressDialog.dismiss(); // Hide loading dialog

                Log.e("FirebaseError", "Failed to fetch data", error.toException());
                Toast.makeText(Profile.this, "Failed to load profile!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
