package com.example.picklaundry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Secondactivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondactivity);
    }

    public void login(View view) {
        Intent intent = new Intent(this,LoginAtivity.class);
        startActivity(intent);
    }

    public void register(View view) {
        Intent intent = new  Intent(this,RegisterAtivity.class);
        startActivity(intent);
    }
}