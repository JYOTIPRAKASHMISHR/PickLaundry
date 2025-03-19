package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterAtivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etMobile, etDOB, etLocation, etPassword, etConfirmPassword;
    private RadioGroup radioGroupGender;
    private Button btnRegister;
    private Calendar calendar;
    private int year, month, day;
    private String gender = "";

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, addressRef;

    // Password Regex Pattern
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_ativity);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        addressRef = FirebaseDatabase.getInstance().getReference("address");

        // Initialize Views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etDOB = findViewById(R.id.etDOB);
        etLocation = findViewById(R.id.etLocation);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        btnRegister = findViewById(R.id.btnRegister);

        // Initialize Calendar
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        // Date Picker Dialog for DOB
        etDOB.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterAtivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) ->
                            etDOB.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear),
                    year, month, day);
            datePickerDialog.show();
        });

        // Retrieve Address from Firebase
        fetchSavedAddress();

        // Location Picker
        etLocation.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterAtivity.this, LocationPickerActivity.class);
            startActivityForResult(intent, 100);
        });

        // Register Button Click Listener
        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    // Fetch the latest address from Firebase and set in etLocation
    private void fetchSavedAddress() {
        addressRef.limitToLast(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String savedAddress = snapshot.getValue(String.class);
                    etLocation.setText(savedAddress);
                }
            } else {
                Toast.makeText(RegisterAtivity.this, "No saved address found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle Location Picker Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String selectedLocation = data.getStringExtra("selected_location");
            etLocation.setText(selectedLocation);
        }
    }

    // Validate Inputs and Register User
    private void validateAndRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Get selected gender
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedGenderId);
            gender = selectedRadioButton.getText().toString();
        }

        // Validations
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            return;
        }

        if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
            etMobile.setError("Enter a valid 10-digit mobile number");
            return;
        }

        if (TextUtils.isEmpty(dob)) {
            etDOB.setError("Date of Birth is required");
            return;
        }

        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location is required");
            return;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            etPassword.setError("Password must contain:\n✔ At least 6 characters\n✔ 1 uppercase letter\n✔ 1 lowercase letter\n✔ 1 number\n✔ 1 special character (@#$%^&+=!)");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Register User in Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        // Store user details in Firebase Database
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", userId);
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("mobile", mobile);
                        userMap.put("dob", dob);
                        userMap.put("gender", gender);
                        userMap.put("location", location);

                        databaseReference.child(userId).setValue(userMap)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(RegisterAtivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(RegisterAtivity.this, LoginAtivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterAtivity.this, "Failed to store data. Try again!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        Toast.makeText(RegisterAtivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
