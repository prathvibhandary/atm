package com.github.prathvib.atmauthentication;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class MainActivity extends AppCompatActivity {


    private CancellationSignal cancellationSignal = null;
    public static int balance =50000;
    public static boolean login =true;

    private BiometricPrompt.AuthenticationCallback authenticationCallback;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void
    onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         //prompt();
    }

    private CancellationSignal getCancellationSignal()
    {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(
                new CancellationSignal.OnCancelListener() {
                    @Override public void onCancel()
                    {
                        notifyUser("Authentication was Cancelled by the user");
                    }
                });
        return cancellationSignal;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private Boolean checkBiometricSupport()
    {
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            notifyUser("Fingerprint authentication has not been enabled in settings");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC)!= PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint Authentication Permission is not enabled");
            return false;
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return true;
        }
        else
            return true;
    }


    private void notifyUser(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume(){
        super.onResume();
        ((TextView) findViewById(R.id.textView2)).setText("XXXXX");
        if (login) {prompt();}
        login=true;

    }

    private void prompt(){
        ((Button) findViewById(R.id.button2)).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ScannedBarcodeActivity.class)));
        ((LinearLayout) findViewById(R.id.layout)).setVisibility(View.INVISIBLE);

        authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
            // here we need to implement two methods
            // onAuthenticationError and
            // onAuthenticationSucceeded If the
            // fingerprint is not recognized by the
            // app it will call onAuthenticationError
            // and show a toast
            @Override
            public void onAuthenticationError(
                    int errorCode, CharSequence errString)
            {
                super.onAuthenticationError(errorCode, errString);
                notifyUser("Authentication Error : " + errString);
            }
            // If the fingerprint is recognized by the
            // app then it will call
            // onAuthenticationSucceeded and show a
            // toast that Authentication has Succeed
            // Here you can also start a new activity
            // after that
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result)
            {
                super.onAuthenticationSucceeded(result);
                ((LinearLayout) findViewById(R.id.layout)).setVisibility(View.VISIBLE);
                notifyUser("Authentication Succeeded");
                // or start a new Activity
            }
        };

        BiometricPrompt biometricPrompt = new BiometricPrompt
                .Builder(getApplicationContext())
                .setTitle("Login to ATM")
                .setSubtitle("Verify Identity")
                .setDescription("Touch Your finger on sensor to login.")
                .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void
                    onClick(DialogInterface dialogInterface, int i)
                    {
                        notifyUser("Authentication Cancelled");
                    }
                }).build();


        biometricPrompt.authenticate(
                getCancellationSignal(),
                getMainExecutor(),
                authenticationCallback);

        checkBiometricSupport();

        ((Button) findViewById(R.id.check_balance)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ((TextView) findViewById(R.id.textView2)).setText(String.valueOf(balance));
            }
        });

    }
}
