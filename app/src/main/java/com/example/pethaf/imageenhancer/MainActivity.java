package com.example.pethaf.imageenhancer;

import android.app.AlertDialog;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    boolean fuzzyEqHasRun = false;
    boolean regularEqHasRun = false;
    ImageView image;
    ImageView histogram;
    EditText imageText;
    EditText histogramText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

}
