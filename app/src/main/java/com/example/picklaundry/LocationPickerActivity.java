package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.picklaundry.databinding.ActivityLocationPickerBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "LocationPickerActivity";
    private GoogleMap mMap;
    private ActivityLocationPickerBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_REQUEST_CODE = 100;
    private LatLng selectedLatLng;
    private DatabaseReference databaseReference;
    private Button btnSaveAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("address");

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnSaveAddress = findViewById(R.id.btn_save_address);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnSaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {Intent intent = new Intent(LocationPickerActivity.this, RegisterAtivity.class);
                saveAddressToFirebase();
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);
        getCurrentLocation();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedLatLng = latLng;
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                Log.d(TAG, "User selected location: " + latLng.latitude + ", " + latLng.longitude);
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> task = fusedLocationClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Your Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15));
                        Log.d(TAG, "Current location retrieved: " + selectedLatLng.latitude + ", " + selectedLatLng.longitude);
                    } else {
                        Log.e(TAG, "Unable to get location");
                        Toast.makeText(LocationPickerActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveAddressToFirebase() {
        if (selectedLatLng == null) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No location selected");
            return;
        }

        String address = getAddressFromLatLng(selectedLatLng);
        if (address == null || address.isEmpty()) {
            Toast.makeText(this, "Unable to fetch address. Try again.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Geocoder returned empty address");
            return;
        }

        String id = databaseReference.push().getKey(); // Generate unique ID
        Log.d(TAG, "Saving Address: " + address + " with ID: " + id);

        databaseReference.child(id).setValue(address)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Address saved successfully in Firebase");
                    Toast.makeText(LocationPickerActivity.this, "Address Saved Successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to Save Address: " + e.getMessage());
                    Toast.makeText(LocationPickerActivity.this, "Failed to Save Address", Toast.LENGTH_SHORT).show();
                });
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                String fullAddress = addresses.get(0).getAddressLine(0);
                Log.d(TAG, "Address found: " + fullAddress);
                return fullAddress;
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder IOException: " + e.getMessage());
        }
        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Log.e(TAG, "Location permission denied");
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
