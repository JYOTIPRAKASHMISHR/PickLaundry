package com.example.picklaundry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Firstiron extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstiron);
    }

    public void continue2(View view) {
        Intent intent=new Intent (this,Secondiron.class);
        startActivity(intent);
    }
}