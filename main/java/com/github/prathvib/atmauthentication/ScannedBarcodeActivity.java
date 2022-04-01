package com.github.prathvib.atmauthentication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScannedBarcodeActivity extends AppCompatActivity {


    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_barcode);
       initViews();
    }

    private void initViews() {
        txtBarcodeValue = findViewById(R.id.textView6);
        surfaceView = findViewById(R.id.surfaceView);

        ((Button) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String pin = ((EditText) findViewById(R.id.editTextNumberPassword)).getText().toString();
                if(pin.equals("1234")){
                    int amt = Integer.parseInt(((TextView) findViewById(R.id.editTextNumber)).getText().toString());

                    if(MainActivity.balance>=amt){
                        MainActivity.balance = MainActivity.balance-amt;
                        Toast.makeText(getApplicationContext(), "Please Collect cash from ATM", Toast.LENGTH_SHORT).show();
                        try{
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            URL urlObj = new URL("https://script.google.com/macros/s/AKfycbzC0e0FqsKGMIyn4sHluHV95dgywb7M_EsekMr4udG-O3loOuPP/exec");
                            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                            conn.setDoOutput(true);
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "text/plain;charset=utf-8");
                            conn.setRequestProperty("Accept", "application/json");
                            String paramsString = "XYZATM~`~" +amt ;
                            conn.connect();
                            try(OutputStream os = conn.getOutputStream()) {
                                byte[] input = paramsString.getBytes("utf-8");
                                os.write(input, 0, input.length);
                            }
                            conn.getInputStream();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "erroe", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Cannot Withdraw, Low Balance", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Invalid Pin. Try Again!", Toast.LENGTH_SHORT).show();
                }
                MainActivity.login = false;
                finish();
            }
        });
    }

    private void initialiseDetectorsAndSources() {

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScannedBarcodeActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannedBarcodeActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {

                    if(barcodes.valueAt(0).displayValue.split("~")[0].equals("XYZATM")){
                        txtBarcodeValue.post(new Runnable() {
                            @Override
                            public void run() {


                                cameraSource.release();


                                ViewGroup parent = (ViewGroup)surfaceView.getParent();
                                if (parent != null) {
                                    parent.removeView(surfaceView);
                                }

                            }
                        });
                    }

                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}