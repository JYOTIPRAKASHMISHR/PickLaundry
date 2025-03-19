package com.example.picklaundry;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void home1(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void contact1(View view) {
        Intent intent = new Intent(this, Contact.class);
        startActivity(intent);
    }

    public void profile1(View view) {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    // Method to share the app with other users
    public void share(View view) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain"); // Specify the type of data to share

        // Message to be shared
        String shareMessage = "Check out this amazing app: PickLaundry! Download now: https://play.google.com/store/apps/details?id=com.example.picklaundry";

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "PickLaundry - Best Laundry Service");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        // Start the share intent
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    public void about(View view) {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }
}
