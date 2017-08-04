package com.example.pethaf.imageenhancer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.w3c.dom.Text;

import java.io.Console;

import static android.R.attr.onClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    boolean newImage = true;
    boolean fuzzyEqHasRun = false;
    boolean regularEqHasRun = false;
    Bitmap originalImage;
    ImageView image;
    ImageView histogram;
    EditText imageText;
    EditText histogramText;
    Button fileBtn;
    Button regHist;
    Button fuzzyHist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileBtn = (Button) findViewById(R.id.fileBtn);
        regHist = (Button) findViewById(R.id.histBtn);
        fuzzyHist = (Button) findViewById(R.id.fuzzBtn);
        fileBtn.setOnClickListener(this);
        regHist.setOnClickListener(this);
        fuzzyHist.setOnClickListener(this);
    }

    @Override
    public final void onClick(View view)
    {
        Integer btnId = ((Button) view).getId();
        if(btnId.equals(fileBtn.getId()))
        {
            Log.i("Info","FileBtn");
        }
        else if(btnId.equals(regHist.getId()))
        {
            Log.i("Info","Regular Histogram");
        }
        else if(btnId.equals((fuzzyHist.getId())))
        {
            Log.i("Info","Fuzzy Histogram");
        }

    }



}
