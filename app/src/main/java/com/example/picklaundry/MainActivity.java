package com.example.picklaundry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
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

        // Set Click Listener for Menu Icon
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.openDrawer(GravityCompat.END);
                } else {
                    drawerLayout.closeDrawer(GravityCompat.END);
                }
            }
        });

        // Handle Navigation Menu Clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, Profile.class));
                } else if (id == R.id.nav_contact) {
                    startActivity(new Intent(MainActivity.this, Contact.class));
                }
                else if (id == R.id.nav_home) {
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                }
                else if (id == R.id.my_request) {
                    startActivity(new Intent(MainActivity.this, Myrequest.class));
                }
                else if (id == R.id.setting) {
                    startActivity(new Intent(MainActivity.this, Settings
                            .class));
                }

                // Close drawer after selection
                drawerLayout.closeDrawer(GravityCompat.END);
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