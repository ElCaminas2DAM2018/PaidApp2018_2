package org.ieselcaminas.pmdm.paidapp2018_2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

public class MainActivity extends AppCompatActivity {

    private static final byte[] SALT = new byte[]{-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89};

    private LicenseChecker mChecker;
    private LicenseCheckerCallback mLicenseCheckerCallback;
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoEJwuv4n6itVdYlt0Mwc5SEpWL4vKnhCYmHmI9PnXnc29mUC4gmP18P0I3wGsLMdn0a2MbzmEc6E5lYY4dFsvUKYSs8m7tVct8R/PXY4cY7Y+e09rKggjLeBD+ipYPizxdKrOI0Z2sWHFGECJrkBGaF3OddhEnFMgpPeRvsSpPhl9vUWPl1bqwoTUFGwgmDa6NraLpN4U2HN0DhVewGg8CkeBrjTShMsZzlZFc6MTC4IHTrjwUIhQxahsQdZGJBwggtEQJit3WQjNSw70Wc3M6gZL93WcWWmxjWuGOjd8wnKAFLP2ToRAEjepGw8innFEHSfF6Y0ry5i8xbxxdOGHwIDAQAB";
    private TextView textView;
    private Button checkButton;
    private Handler mHandler;
    private Context context;

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {

        @Override
        public void allow(int reason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // Should allow user access.
            displayResult("Access Allowed");
        }


        @Override
        public void dontAllow(int reason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.

                return;
            }
            if (reason == Policy.RETRY) {
                // If the reason received from the policy is RETRY, it was probably
                // due to a loss of connection with the service, so we should give the
                // user a chance to retry. So show a dialog to retry.
                displayResult("Don't allow access. Please retry connection");
            } else {
                // Otherwise, the user is not licensed to use this app.
                // Your response should always inform the user that the application
                // is not licensed, but your behavior at that point can vary. You might
                // provide the user a limited access version of your app or you can
                // take them to Google Play to purchase the app.
                displayResult("Don't allow access. Please buy the app");
            }
        }

        @Override

        public void applicationError(int errorCode) {
            displayResult(String.format("applicationError %d", errorCode));
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        mLicenseCheckerCallback = new MyLicenseCheckerCallback();

        // Construct the LicenseChecker with a Policy.
        mChecker = new LicenseChecker(
                this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), deviceId)),
                BASE64_PUBLIC_KEY  // Your public licensing key.
        );

        mHandler = new Handler();

        checkButton = findViewById(R.id.checkButton);
        //Call a wrapper method that initiates the license check chec
        checkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                doCheck();
            }
        });


    }

    private void doCheck() {
        checkButton.setEnabled(false);
        setProgressBarIndeterminateVisibility(true);
        textView.setText("Checking License ...");
        mChecker.checkAccess(mLicenseCheckerCallback);
    }

    protected void showMyDialog(final int myReason) {
        mHandler.post(new Runnable() {
            public void run() {
                new AlertDialog.Builder(context).setTitle("No license").setMessage(myReason == Policy.RETRY ? "Please retry" : "Unlicensed app").setPositiveButton(myReason == Policy.RETRY ? "Retry" : "Buy", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1) {
                            doCheck();
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=" + getPackageName()));
                            startActivity(marketIntent);
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create().show();
            }
        });
    }

    private void displayResult(final String result) {
        mHandler.post(new Runnable() {
            public void run() {
                textView.setText(result);
                setProgressBarIndeterminateVisibility(false);
                checkButton.setEnabled(true);
            }
        });
    }
}
