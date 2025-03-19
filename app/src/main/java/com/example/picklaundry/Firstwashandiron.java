package com.example.picklaundry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Firstwashandiron extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstwashandiron);
    }

    public void continue1(View view) {
        Intent intemt = new Intent(this,Secondwashandiron.class);
        startActivity(intemt);
    }
}