package com.example.picklaundry;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

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

        // Initialize views
        tvPhone = findViewById(R.id.phoneText);
        tvEmail = findViewById(R.id.emailText);
        etMessage = findViewById(R.id.messageInput);
        btnSend = findViewById(R.id.sendButton);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Complaints");

        // Set static contact info
        final String supportEmail = "lipuhota1998@gmail.com";
        final String supportPhone = "+91 93374 25120";
        tvEmail.setText(supportEmail);
        tvPhone.setText(supportPhone);

        if (user != null) {
            userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

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
                    Toast.makeText(Contact.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Phone dialer
        tvPhone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + supportPhone.replaceAll(" ", "")));
            startActivity(intent);
        });

        // Email client
        tvEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + supportEmail));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
            startActivity(Intent.createChooser(intent, "Send email via"));
        });

        // Send button click
        btnSend.setOnClickListener(v -> submitComplaint());
    }

    private void submitComplaint() {
        String message = etMessage.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter your message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userName == null || userEmail == null || userPhone == null) {
            Toast.makeText(this, "User information not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String complaintId = databaseReference.push().getKey();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Complaint complaint = new Complaint(complaintId, userName, userEmail, userPhone, message, timestamp);

        databaseReference.child(complaintId).setValue(complaint)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Contact.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                    etMessage.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(Contact.this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }

    // Firebase model class
    public static class Complaint {
        public String complaintId, name, email, mobile, message, timestamp;

        public Complaint() {
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
