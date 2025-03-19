package com.example.picklaundry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Firstdryclean extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstdryclean);
    }

    public void continue2(View view) {
        Intent intent=new Intent (this,Seconddryclean.class);
        startActivity(intent);

    }
}