package com.example.picklaundry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Firstwashandfolds extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstwashandfolds);
    }

    public void continue1(View view) {
        Intent intemt = new Intent(this,Secondwashandfolds.class);
        startActivity(intemt);
    }
}