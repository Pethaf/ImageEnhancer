package com.example.pethaf.imageenhancer;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptC;
import android.support.v8.renderscript.ScriptIntrinsicHistogram;
import android.support.v8.renderscript.Type;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import java.io.File;
import java.io.FileOutputStream;
import static com.example.pethaf.imageenhancer.R.layout.activity_main;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Allocation mOutAllocation2;
    ScriptIntrinsicHistogram mScriptHistogram;
    Allocation mAllocationHist;
    RenderScript rs;
    ScriptC_RgbToHsv rgb2hsv;
    boolean fuzzyEqHasRun = false;
    boolean regularEqHasRun = false;
    ImageView originalImageView;
    ImageView histogramView;
    TextView imageText;
    TextView histogramText;
    Button fileBtn;
    Button regHist;
    Button fuzzyHist;
    Button normalPicBtn;
    FilePickerDialog dialog;
    Bitmap originalImage;
    Bitmap originalImageHistogram;
    Bitmap regHistEq;
    Bitmap fuzzHisEq;
    Bitmap regHistEqHistogram;
    Bitmap fuzzHistEqHistogram;
    double fuzzRunTime;
    double regRunTime;
    Button saveBtn;
    ImageView fullscreen;
    TextView fileNameHeading;
    EditText filenameInput;
    Bitmap Originalimage;
    Bitmap OriginalHistogram;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);
        fileBtn = (Button) findViewById(R.id.fileBtn);
        regHist = (Button) findViewById(R.id.histBtn);
        fuzzyHist = (Button) findViewById(R.id.fuzzBtn);
        normalPicBtn = (Button)findViewById(R.id.originalImg);
        histogramText = (TextView) findViewById(R.id.histogramText);
        fileBtn.setOnClickListener(this);
        regHist.setOnClickListener(this);
        fuzzyHist.setOnClickListener(this);
        originalImageView = (ImageView)findViewById(R.id.originalImage);
        histogramView = (ImageView)findViewById(R.id.histogramView);
        normalPicBtn.setOnClickListener(this);
        originalImageView.setClickable(true);
        originalImageView.setOnClickListener(this);
        histogramView.setClickable(true);
        histogramView.setOnClickListener(this);
        fullscreen = (ImageView)findViewById(R.id.fullScreenImage);
        fullscreen.setClickable(true);
        fullscreen.setOnClickListener(this);
        saveBtn = (Button)findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(this);
        fullscreen.setVisibility(View.GONE);
        saveBtn.setVisibility(View.GONE);
        fileNameHeading = (TextView)findViewById(R.id.filenameHeading);
        fileNameHeading.setVisibility(View.GONE);
        filenameInput = (EditText)findViewById(R.id.fileNameInput);
        filenameInput.setVisibility(View.GONE);
        filenameInput.setVisibility(View.GONE);
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.root = Environment.getExternalStorageDirectory();
        dialog = new FilePickerDialog(MainActivity.this,properties);
        dialog.setTitle("Select a File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {

            @Override
            public void onSelectedFilePaths(String[] files) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                     options.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(files[0], options);
                    if (options.outWidth != -1 && options.outHeight != -1) {

                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        options.inJustDecodeBounds = false;
                        originalImage = BitmapFactory.decodeFile(files[0],options);
                        originalImageView.setImageBitmap(originalImage);
                        fuzzyEqHasRun = false;
                        regularEqHasRun = false;
                        Bitmap hsvImage = makeHSVBitmap(originalImage);
                        int[] hsvHistogram = makeHistogram(hsvImage);
                        int[] Vchannel = new int[hsvHistogram.length/4];
                        int j=2;
                        while(j<= hsvHistogram.length-2) {
                            Vchannel[j/4] = hsvHistogram[j];
                            j+=4;
                        }

                        originalImageHistogram = drawHistogram(Vchannel,
                                histogramView.getWidth(),histogramView.getHeight());
                        histogramView.setImageBitmap(originalImageHistogram);
                        histogramText.setText("Original Image V-channel Histogram");
                }
                else {
                    Toast.makeText(MainActivity.this,"Not image file", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(dialog!=null)
                    {   //Show dialog if the read permission has been granted.
                        dialog.show();
                    }
                }
                else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(MainActivity.this,"Permission is Required for getting list of files", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Bitmap makeHSVBitmap(Bitmap image)
    {
        Bitmap res = image.copy(image.getConfig(), true);
        rs = RenderScript.create(this);
        Allocation allocationA = Allocation.createFromBitmap(rs, image);
        Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());
        rgb2hsv = new ScriptC_RgbToHsv(rs);
        rgb2hsv.forEach_root(allocationA, allocationB);
        allocationB.copyTo(res);
        allocationA.destroy();
        allocationB.destroy();
        rgb2hsv.destroy();
        return res;
    }
    private int[] makeHistogram(Bitmap theImage) {
        mAllocationHist = Allocation.createSized(rs, Element.I32_4(rs), 256);
        mScriptHistogram = ScriptIntrinsicHistogram.create(rs, Element.U8_4(rs));
        mOutAllocation2 = Allocation.createFromBitmap(rs, originalImage);
        mScriptHistogram.setOutput(mAllocationHist);
        mScriptHistogram.forEach(mOutAllocation2);
        int[] hist = new int[256 * 4];
        mAllocationHist.copyTo(hist);
        return hist;
    }
    private Bitmap drawHistogram(int[] values,int width,int height)
    {

        Bitmap histogram = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(histogram);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        int lineDelta = width/values.length;
        int lineStart = 0;
        int sum = 0;
        int max = 0;
        for(int j=0;j< values.length;j++)
        {   if(max < values[j])
            {
                max = values[j];
            }
            sum+= values[j];
        }
        for(int i=0; i < values.length; i++)
        {
            canvas.drawRect(lineStart,(1.0f-(float)values[i]/max)*height,lineStart+lineDelta,height,paint);
            lineStart += lineDelta;
        }
        return histogram;
    }

    private void regHistEq() {
        if(originalImageView.getDrawable() != null) {
            if (regularEqHasRun) {
                originalImageView.setImageBitmap(regHistEq);
                histogramView.setImageBitmap(regHistEqHistogram);
                histogramText.setText("Regular contrast equalization v-channel histogram");
            } else
            {
                regHistEq = originalImage.copy(originalImage.getConfig(),true);
                long start = System.nanoTime();
                int width = originalImage.getWidth();
                int height = originalImage.getHeight();
                Bitmap res = originalImage.copy(originalImage.getConfig(), true);
                RenderScript rs = RenderScript.create(this);
                Allocation allocationA = Allocation.createFromBitmap(rs, res);
                Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());
                ScriptC_RegHistEq histEqScript = new ScriptC_RegHistEq(rs);
                histEqScript.set_size(width * height);
                histEqScript.forEach_root(allocationA, allocationB);
                histEqScript.invoke_createRemapArray();
                histEqScript.forEach_remaptoRGB(allocationB, allocationA);
                allocationA.copyTo(regHistEq);
                long stop = System.nanoTime();
                regRunTime = (double)(stop - start) / 1000000000;
                Bitmap tmp = makeHSVBitmap(regHistEq);
                int[] hsvHistogram = makeHistogram(tmp);
                int[] Vchannel = new int[256];
                int j = 2;
                while (j <= hsvHistogram.length - 2) {
                    Vchannel[j / 4] = hsvHistogram[j];
                    j += 4;
                }
                Bitmap histogram = drawHistogram(Vchannel, histogramView.getWidth(), histogramView.getHeight());
                regHistEqHistogram = histogram;
                regularEqHasRun = true;
                histogramView.setImageBitmap(histogram);
                originalImageView.setImageBitmap(regHistEq);
                histogramText.setText("Regular contrast equalization v-channel histogram");
            }
        }
        else
        {
            Toast.makeText(MainActivity.this,"No image file selected", Toast.LENGTH_SHORT).show();
        }
    }
    private void fuzzyHistEq() {
        if (originalImageView.getDrawable() != null) {
            if (fuzzyEqHasRun) {
                originalImageView.setImageBitmap(fuzzHisEq);
                histogramView.setImageBitmap(fuzzHistEqHistogram);
                histogramText.setText("Fuzzy Contrast Enhancement V-channel histogram");
            } else {

                fuzzHisEq = originalImage.copy(originalImage.getConfig(),true);
                long start = System.nanoTime();
                Bitmap res = originalImage.copy(originalImage.getConfig(), true);
                RenderScript rs = RenderScript.create(this);
                Allocation allocationA = Allocation.createFromBitmap(rs, res);
                Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());
                ScriptC_FuzzHistEq fuzzhistEq = new ScriptC_FuzzHistEq(rs);
                fuzzhistEq.forEach_root(allocationA, allocationB);
                allocationB.copyTo(fuzzHisEq);
                fuzzyEqHasRun = true;
                long stop = System.nanoTime();
                fuzzRunTime = (double)(stop - start) / 1000000000;
                originalImageView.setImageBitmap(fuzzHisEq);
                Bitmap tmp = makeHSVBitmap(fuzzHisEq);
               int[] hsvHistogram = makeHistogram(tmp);
                int[] Vchannel = new int[256];
                int j = 2;
                while (j <= hsvHistogram.length - 2) {
                   Vchannel[j / 4] = hsvHistogram[j];
                   j += 4;
               }
               Bitmap histogram = drawHistogram(Vchannel, histogramView.getWidth(), histogramView.getHeight());
               fuzzHistEqHistogram = histogram;
               histogramView.setImageBitmap(fuzzHistEqHistogram);
                histogramText.setText("Fuzzy Contrast Enhancement V-channel histogram");

            }
        } else {
            Toast.makeText(MainActivity.this,"No image file selected", Toast.LENGTH_SHORT).show();
        }
    }
    public void normalPic()
    {
        originalImageView.setImageBitmap(originalImage);
        histogramView.setImageBitmap(originalImageHistogram);
        histogramText.setText("Original Image V-channel histogram");
    }
    @Override
    public final void onClick(View view) {
         if (view instanceof Button) {
            Integer btnId = ((Button) view).getId();
            if (btnId.equals(fileBtn.getId())) {
                this.pickFile();
            } else if (btnId.equals(regHist.getId())) {
                regHistEq();
            } else if (btnId.equals((fuzzyHist.getId()))) {
                fuzzyHistEq();
            } else if (btnId.equals((normalPicBtn.getId()))) {
                normalPic();
            }
              else if(btnId.equals(saveBtn.getId()))
            {
                if(filenameInput.getVisibility() == View.VISIBLE) {
                    if (filenameInput.getText().toString().equals("Enter Filename") || filenameInput.getText().length() == 0) {
                        Toast.makeText(MainActivity.this, "Invalid Filename", Toast.LENGTH_LONG).show();
                    } else {
                        String filePath = getApplicationContext().getFilesDir().getPath();
                        File myDir = new File(filePath + "/saved_images");
                        myDir.mkdirs();
                        String fname = filenameInput.getText().toString();
                        File file = new File(myDir, fname);
                        FileOutputStream out;
                        if (file.exists()) file.delete();
                        try {
                            out = new FileOutputStream(file);
                            Bitmap tmp = ((BitmapDrawable) fullscreen.getDrawable()).getBitmap();
                            tmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            fileNameHeading.setVisibility(View.GONE);
                            filenameInput.setVisibility(View.GONE);
                            fullscreen.setVisibility(View.GONE);
                            fileBtn.setVisibility(View.VISIBLE);
                            fuzzyHist.setVisibility(View.VISIBLE);
                            regHist.setVisibility(View.VISIBLE);
                            normalPicBtn.setVisibility(View.VISIBLE);
                            histogramView.setVisibility(View.VISIBLE);
                            originalImageView.setVisibility((View.VISIBLE));
                            saveBtn.setVisibility((View.GONE));
                        }
                    }
                }
                else
                {
                    fileNameHeading.setVisibility(View.VISIBLE);
                    filenameInput.setVisibility(View.VISIBLE);
                    filenameInput.setText("Enter Filename");
                }
            }
        } else if (view instanceof ImageView) {
            if (originalImageView.getDrawable() != null) {
                Integer viewId = ((ImageView) view).getId();
                if (viewId.equals(originalImageView.getId())) {
                    fullscreen.setImageBitmap(((BitmapDrawable) originalImageView.getDrawable()).getBitmap());
                    fullscreen.setScaleType(ImageView.ScaleType.FIT_XY);
                    fullscreen.setVisibility(View.VISIBLE);
                    saveBtn.setVisibility(View.VISIBLE);
                    fileBtn.setVisibility(View.GONE);
                    fuzzyHist.setVisibility(View.GONE);
                    regHist.setVisibility(View.GONE);
                    normalPicBtn.setVisibility(View.GONE);
                    histogramView.setVisibility(View.GONE);
                } else if (viewId.equals(histogramView.getId())) {
                    fullscreen.setImageBitmap(((BitmapDrawable) histogramView.getDrawable()).getBitmap());
                    fullscreen.setScaleType(ImageView.ScaleType.FIT_XY);
                    fullscreen.setVisibility(View.VISIBLE);
                    saveBtn.setVisibility(View.VISIBLE);
                    fileBtn.setVisibility(View.GONE);
                    fuzzyHist.setVisibility(View.GONE);
                    regHist.setVisibility(View.GONE);
                    normalPicBtn.setVisibility(View.GONE);
                    histogramView.setVisibility(View.GONE);
                }
                    else if(viewId.equals(fullscreen.getId()))
                {
                   fullscreen.setVisibility(View.GONE);
                    filenameInput.setVisibility(View.GONE);
                    fileNameHeading.setVisibility(View.GONE);
                    saveBtn.setVisibility(View.GONE);
                    fileBtn.setVisibility(View.VISIBLE);
                    fuzzyHist.setVisibility(View.VISIBLE);
                    regHist.setVisibility(View.VISIBLE);
                    normalPicBtn.setVisibility(View.VISIBLE);
                    histogramView.setVisibility(View.VISIBLE);

                }
            }
        }
    }

    public void pickFile()
    {
        dialog.show();
    }


}
