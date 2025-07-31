package com.example.prevent_screen_overlay;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.MotionEvent;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.prevent_screen_overlay.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    private OverlayDetector overlayDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // 建立 OverlayDetector
        overlayDetector = new OverlayDetector(this);

        // 可以選擇性設定 onPositiveClick 行為
        // 按確定後執行的行為，例如結束 Activity
        overlayDetector.setOnPositiveClick(this::finish);

        // 啟動偵測
        overlayDetector.startDetection();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (overlayDetector != null) {
            overlayDetector.startDetection();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (overlayDetector != null) {
            overlayDetector.stopDetection();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (overlayDetector != null) {
            overlayDetector.stopDetection();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (overlayDetector != null && overlayDetector.dispatchTouchEvent(ev)) {
            return true; // 阻擋觸控事件
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        overlayDetector.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        overlayDetector.onUserLeaveHint();
    }
}