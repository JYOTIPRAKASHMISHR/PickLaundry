package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterAtivity extends AppCompatActivity {

    private static final String TAG = "RegisterAtivity";

    private TextInputEditText etName, etEmail, etMobile, etDOB, etLocation, etPassword, etConfirmPassword;
    private RadioGroup radioGroupGender;
    private Button btnRegister;
    private Calendar calendar;
    private int year, month, day;
    private String gender = "";

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, addressRef;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_ativity);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        addressRef = FirebaseDatabase.getInstance().getReference("address");

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etDOB = findViewById(R.id.etDOB);
        etLocation = findViewById(R.id.etLocation);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        btnRegister = findViewById(R.id.btnRegister);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        etDOB.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterAtivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) ->
                            etDOB.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear),
                    year, month, day);
            datePickerDialog.show();
        });

        fetchSavedAddress();

        etLocation.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterAtivity.this, LocationPickerActivity.class);
            startActivityForResult(intent, 100);
        });

        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void fetchSavedAddress() {
        addressRef.limitToLast(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    AddressModel addressModel = snapshot.getValue(AddressModel.class);

                    if (addressModel != null) {
                        StringBuilder formattedAddress = new StringBuilder();

                        if (addressModel.buildingName != null)
                            formattedAddress.append(addressModel.buildingName).append(", ");
                        if (addressModel.street != null)
                            formattedAddress.append(addressModel.street).append(", ");
                        if (addressModel.area != null)
                            formattedAddress.append(addressModel.area).append(", ");
                        if (addressModel.city != null)
                            formattedAddress.append(addressModel.city).append(", ");
                        if (addressModel.state != null)
                            formattedAddress.append(addressModel.state).append(", ");
                        if (addressModel.postalCode != null)
                            formattedAddress.append(addressModel.postalCode).append(", ");
                        if (addressModel.country != null)
                            formattedAddress.append(addressModel.country);

                        String finalAddress = formattedAddress.toString().trim();
                        if (finalAddress.endsWith(",")) {
                            finalAddress = finalAddress.substring(0, finalAddress.length() - 1);
                        }

                        etLocation.setText(finalAddress);
                    } else {
                        Toast.makeText(this, "No valid address found", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String selectedLocation = data.getStringExtra("selected_location");
            etLocation.setText(selectedLocation);
        }
    }

    private void validateAndRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedGenderId);
            gender = selectedRadioButton.getText().toString();
        }

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
            etPassword.setError("Password must contain:\n✔ At least 6 characters\n✔ 1 uppercase\n✔ 1 lowercase\n✔ 1 number\n✔ 1 special character");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

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
                                        // delete last address (optional)
                                        addressRef.orderByKey().limitToLast(1)
                                                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot child : snapshot.getChildren()) {
                                                            child.getRef().removeValue();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                    }
                                                });

                                        Toast.makeText(RegisterAtivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(RegisterAtivity.this, LoginAtivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterAtivity.this, "Failed to store data. Try again!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(RegisterAtivity.this, "Authentication Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ✅ Address model used to fetch structured data
    public static class AddressModel {
        public String fullAddress;
        public String buildingName;
        public String street;
        public String area;
        public String city;
        public String state;
        public String country;
        public String postalCode;
        public double latitude;
        public double longitude;

        public AddressModel() {
        }

        public AddressModel(String fullAddress, String buildingName, String street,
                            String area, String city, String state,
                            String country, String postalCode,
                            double latitude, double longitude) {
            this.fullAddress = fullAddress;
            this.buildingName = buildingName;
            this.street = street;
            this.area = area;
            this.city = city;
            this.state = state;
            this.country = country;
            this.postalCode = postalCode;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
