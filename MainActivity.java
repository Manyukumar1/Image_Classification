package com.example.imageclassification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.BidiClassifier;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity<viewImage, b> extends AppCompatActivity implements View.OnClickListener {

    int mInputsize = 224;
    String mModelPath = "image_class12.tflite";
    String mLabelPath = "label.txt";
    Classifier classifier;
    ImageView viewImage;
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     //   b = (Button) findViewById(R.id.btnSelectPhoto);
       // viewImage = (ImageView) findViewById(R.id.viewImage);
        try {
            initClassifier();
            initViews();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    void initClassifier () throws IOException {
        classifier = new Classifier(getAssets(), mModelPath, mLabelPath, mInputsize);
    }

    public void initViews ()
    {
        findViewById(R.id.mount).setOnClickListener(this);
        findViewById(R.id.glacier).setOnClickListener(this);
        findViewById(R.id.sea).setOnClickListener(this);
        findViewById(R.id.street).setOnClickListener(this);
        findViewById(R.id.build).setOnClickListener(this);
        findViewById(R.id.forest).setOnClickListener(this);

    }

            public void onClick(View v) {

                Bitmap bitmap = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
                String result = classifier.recognizeImage(bitmap);
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            }

}