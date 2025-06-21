package com.example.picklaundry;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.picklaundry.databinding.ActivityLocationPickerBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "LocationPickerActivity";

    private GoogleMap mMap;
    private Marker currentMarker;
    private LatLng selectedLatLng;

    private ActivityLocationPickerBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.btnSaveAddress.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            } else {
                getAddressFromLatLng(selectedLatLng);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15));
                        currentMarker = mMap.addMarker(new MarkerOptions()
                                .position(selectedLatLng)
                                .title("Current Location"));
                    }
                });

        mMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            if (currentMarker != null) currentMarker.remove();
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        });
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
                    String fullAddress = address.getAddressLine(0);
                    Log.d(TAG, "Address: " + fullAddress);
                    saveAddressToFirebase(fullAddress);
                } else {
                    Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoder Error: " + e.getMessage());
                Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Service only available in Cuttack, Bhubaneswar, Baranga, Trisulia", Toast.LENGTH_LONG).show();
        }
    }

    private void saveAddressToFirebase(String address) {
        if (address == null || address.isEmpty()) {
            Toast.makeText(this, "Invalid address", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("address");
        addressRef.push().setValue(address)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("selected_location", address);
                    setResult(RESULT_OK, intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save address: " + e.getMessage());
                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
