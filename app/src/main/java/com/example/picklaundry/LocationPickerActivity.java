package com.example.picklaundry;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.picklaundry.databinding.ActivityLocationPickerBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "LocationPickerActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private ActivityLocationPickerBinding binding;
    private Button btnSaveAddress;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnSaveAddress = findViewById(R.id.btn_save_address);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnSaveAddress.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            } else {
                getAddressFromLatLng(selectedLatLng);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        mMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        });
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15));
                        mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Current Location"));
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        getCurrentLocation();
                    }
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getAddressFromLatLng(LatLng latLng) {
        double lat = latLng.latitude;
        double lng = latLng.longitude;

        boolean isInCuttack = (lat >= 20.45 && lat <= 20.55) && (lng >= 85.80 && lng <= 86.05);
        boolean isInBhubaneswar = (lat >= 20.20 && lat <= 20.40) && (lng >= 85.70 && lng <= 85.90);
        boolean isInBaranga = (lat >= 20.3192 && lat <= 20.3792) && (lng >= 85.8164 && lng <= 85.8764);
        boolean isInTrisulia = (lat >= 20.3910 && lat <= 20.4510) && (lng >= 85.8036 && lng <= 85.8636);

        if (isInCuttack || isInBhubaneswar || isInBaranga || isInTrisulia) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder addressString = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressString.append(address.getAddressLine(i)).append("\n");
                    }
                    Log.d(TAG, "Fetched address: " + addressString.toString());
                    saveAddressToFirebase(addressString.toString());
                } else {
                    Toast.makeText(this, "No address found for this location", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to fetch address. Try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service is only available in Cuttack and Bhubaneswar", Toast.LENGTH_LONG).show();
        }
    }

    private void saveAddressToFirebase(String address) {
        if (selectedLatLng == null) {
            Toast.makeText(this, "Location not selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (address == null || address.isEmpty()) {
            Toast.makeText(this, "Unable to fetch address. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DatabaseReference addressRef = FirebaseDatabase.getInstance()
                .getReference("address")
                .child(userId);

        Log.d("SaveAddress", "Saving address for user: " + userId + ", value: " + address);

        addressRef.push().setValue(address)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Address Saved Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, RegisterAtivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("SaveAddress", "Failed to save address: " + e.getMessage());
                    Toast.makeText(this, "Failed to Save Address", Toast.LENGTH_SHORT).show();
                });
    }
}
