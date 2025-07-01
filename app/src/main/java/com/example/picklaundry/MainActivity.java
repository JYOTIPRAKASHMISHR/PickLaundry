package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menu_icon);

        // Open or Close Drawer on Menu Icon Click
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        // Navigation Menu Item Clicks
        // Set icons (optional if already in XML)
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_home).setIcon(R.drawable.ic_home);
        menu.findItem(R.id.my_request).setIcon(R.drawable.ic_request);
        menu.findItem(R.id.nav_orders).setIcon(R.drawable.ic_orders);
        menu.findItem(R.id.nav_contact).setIcon(R.drawable.ic_contact);
        menu.findItem(R.id.setting).setIcon(R.drawable.ic_settings);
        menu.findItem(R.id.nav_profile).setIcon(R.drawable.ic_profile);

// Navigation click listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, Profile.class));
                } else if (id == R.id.nav_contact) {
                    startActivity(new Intent(MainActivity.this, Contact.class));
                } else if (id == R.id.nav_home) {
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                } else if (id == R.id.my_request) {
                    startActivity(new Intent(MainActivity.this, Myrequest.class));
                } else if (id == R.id.nav_orders) {
                    startActivity(new Intent(MainActivity.this, Myorder.class));
                } else if (id == R.id.setting) {
                    startActivity(new Intent(MainActivity.this, Settings.class));
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    // Explicitly Defined Methods for XML Click Events
    public void profile(View view) {
        startActivity(new Intent(this, Profile.class));
    }

    public void contact(View view) {
        startActivity(new Intent(this, Contact.class));
    }

    public void washandiron(View view) {
        startActivity(new Intent(this, Firstwashandiron.class));
    }

    public void washabdfolds(View view) {
        startActivity(new Intent(this, Firstwashandfolds.class));
    }

    public void iron(View view) {
        startActivity(new Intent(this, Firstiron.class));
    }

    public void dryclean(View view) {
        startActivity(new Intent(this, Firstdryclean.class));
    }

    public void shoose(View view) {
        startActivity(new Intent(this, Shose.class));
    }

    public void myorder(View view) {
        startActivity(new Intent(this, Myorder.class));
    }
}
