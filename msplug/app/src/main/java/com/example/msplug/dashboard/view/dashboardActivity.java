package com.example.msplug.dashboard.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.msplug.R;
import com.example.msplug.dashboard.home.homeFragment;
import com.example.msplug.dashboard.messages.messagesFragment;
import com.example.msplug.utils.connectionchecker.ConnectionCheckerApp;
import com.example.msplug.utils.connectionchecker.ConnectivityReceiver;
import com.example.msplug.utils.connectionchecker.InternetConnectionService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class dashboardActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{
    LottieAnimationView networkIndicatorAnim;
    public static final String BroadcastStringForAction = "checkinternet";
    IntentFilter mIntentFilter;
    RelativeLayout noInternetRelLayout;
    private static final int REQ_CODE = 1;
    String frag_to_start;
    String request_type;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorComedyBlue));
        }
        getSupportActionBar().hide();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BroadcastStringForAction);
        Intent service = new Intent(this, InternetConnectionService.class);
        startService(service);
        frag_to_start = getIntent().getStringExtra("frag_to_start");
        request_type = getIntent().getStringExtra("request_type");



        noInternetRelLayout = findViewById(R.id.noInternetRelLayout);
        networkIndicatorAnim = findViewById(R.id.noInternetConnection);
        networkIndicatorAnim.playAnimation();

        //Checks constantly for internet connection
        Handler handler = new Handler();
        Runnable periodicUpdate = new Runnable() {
            @Override
            public void run() {
                ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null && ni.isConnectedOrConnecting()){
                    noInternetRelLayout.setVisibility(View.GONE);
                }
                else{
                    noInternetRelLayout.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(this, 1*500- SystemClock.elapsedRealtime()%500);
            }

        };
        periodicUpdate.run();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //request permission if its not granted already

            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},REQ_CODE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //request permission if its not granted already

            requestPermissions(new String[]{Manifest.permission.CALL_PHONE},REQ_CODE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            //request permission if its not granted already

            requestPermissions(new String[]{Manifest.permission.FOREGROUND_SERVICE},REQ_CODE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
            //request permission if its not granted already
            requestPermissions(new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},REQ_CODE);
        }

        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        if (frag_to_start != null){
            messagesFragment fragment1 = new messagesFragment();
            Bundle bundle = new Bundle();
            bundle.putString("request_type", request_type);
            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            fragment1.setArguments(bundle);
            ft1.replace(R.id.content, fragment1, "");
            ft1.commit();
            navigationView.setSelectedItemId(R.id.action_message);
        }
        else{
            homeFragment fragment1 = new homeFragment();
            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            ft1.replace(R.id.content, fragment1, "");
            ft1.commit();
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.action_home:
                            homeFragment fragment1 = new homeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();
                            return true;

                        case R.id.action_message:
                            messagesFragment fragment2 = new messagesFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;
                    }

                    return false;
                }
            };



    public BroadcastReceiver MyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastStringForAction)) {
                if (intent.getStringExtra("online_status").equals("true")) {
                    noInternetRelLayout.setVisibility(View.GONE);
                } else {
                    noInternetRelLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    };


    @Override
    protected void onRestart() {
        super.onRestart();
        registerReceiver(MyReceiver, mIntentFilter);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            noInternetRelLayout.setVisibility(View.GONE);
        }
        else {
            noInternetRelLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MyReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(MyReceiver, mIntentFilter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

}