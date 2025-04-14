package com.example.picklaundry;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Contact extends AppCompatActivity {

    private TextView tvPhone, tvEmail;
    private EditText etMessage;
    private Button btnSend;
    private DatabaseReference databaseReference, userReference;
    private FirebaseAuth auth;
    private String userName, userEmail, userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // Initialize Views
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Complaints");

        if (user != null) {
            userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

            // Fetch User Details
            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userName = snapshot.child("name").getValue(String.class);
                        userEmail = snapshot.child("email").getValue(String.class);
                        userPhone = snapshot.child("mobile").getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Contact.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Phone Click Listener
        tvPhone.setOnClickListener(v -> {
            String phoneNumber = "+9193374 25120";
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        });

        // Email Click Listener
        tvEmail.setOnClickListener(v -> {
            String email = "lipuhota1998@gmail.com";
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + email));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        });

        // Send Button Click Listener
        btnSend.setOnClickListener(v -> submitComplaint());
    }

    private void submitComplaint() {
        String message = etMessage.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter your message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userName == null || userEmail == null || userPhone == null) {
            Toast.makeText(this, "User details not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String complaintId = databaseReference.push().getKey();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create a Complaint object with user details
        Complaint complaint = new Complaint(complaintId, userName, userEmail, userPhone, message, timestamp);

        // Store complaint in Firebase
        databaseReference.child(complaintId).setValue(complaint)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Contact.this, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                    etMessage.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(Contact.this, "Failed to submit complaint", Toast.LENGTH_SHORT).show());
    }

    // Complaint Model Class
    public static class Complaint {
        public String complaintId, name, email, mobile, message, timestamp;

        public Complaint() {
            // Default constructor required for Firebase
        }

        public Complaint(String complaintId, String name, String email, String mobile, String message, String timestamp) {
            this.complaintId = complaintId;
            this.name = name;
            this.email = email;
            this.mobile = mobile;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}
