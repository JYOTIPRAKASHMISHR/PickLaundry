package com.example.picklaundry;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Splashscreen extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        Animation zoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out);

        // Set animation listener to go to next activity when animation ends
        zoomOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Optional: Do something when animation starts
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Go to Secondactivity when animation ends
                Intent intent = new Intent(Splashscreen.this, Secondactivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Not needed
            }
        });

        imageView.startAnimation(zoomOut);
        textView.startAnimation(zoomOut);
    }
}
