package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterAtivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etMobile, etDOB, etLocation, etPassword, etConfirmPassword;
    private RadioGroup radioGroupGender;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, addressRef;

    private static final int LOCATION_PICKER_REQUEST_CODE = 100;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_ativity);

        initFirebase();
        initViews();
        setupDatePicker();
        fetchSavedAddress();
        setupLocationPicker();
        setupRegisterButton();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        addressRef = FirebaseDatabase.getInstance().getReference("address");
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etDOB = findViewById(R.id.etDOB);
        etLocation = findViewById(R.id.etLocation);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupDatePicker() {
        etDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterAtivity.this,
                    (view, year, month, dayOfMonth) -> etDOB.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void fetchSavedAddress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e("FetchAddress", "User not logged in");
            return;
        }

        String userId = user.getUid();
        DatabaseReference addressRef = FirebaseDatabase.getInstance()
                .getReference("address")
                .child(userId);

        Log.d("FetchAddress", "Fetching address for user: " + userId);

        addressRef.limitToLast(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String savedAddress = snapshot.getValue(String.class);
                    Log.d("FetchAddress", "Fetched address: " + savedAddress);
                    etLocation.setText(savedAddress);
                }
            } else {
                Log.d("FetchAddress", "No address found for user: " + userId);
            }
        });
    }



    private void setupLocationPicker() {
        etLocation.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterAtivity.this, LocationPickerActivity.class);
            startActivityForResult(intent, LOCATION_PICKER_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String selectedLocation = data.getStringExtra("selected_location");
            etLocation.setText(selectedLocation);
        }
    }

    private void setupRegisterButton() {
        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                registerUser();
            }
        });
    }

    private boolean validateInputs() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            return false;
        }

        if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
            etMobile.setError("Enter a valid 10-digit mobile number");
            return false;
        }

        if (TextUtils.isEmpty(dob)) {
            etDOB.setError("Date of Birth is required");
            return false;
        }

        if (getSelectedGender().isEmpty()) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location is required");
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            etPassword.setError("Password must be at least 6 characters\nwith 1 uppercase, 1 lowercase, 1 digit, 1 special character.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private String getSelectedGender() {
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedGenderId);
            return selectedRadioButton.getText().toString();
        }
        return "";
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String gender = getSelectedGender();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
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
