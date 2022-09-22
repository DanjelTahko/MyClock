package com.example.myclock;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Generall setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        TextView clock_source = findViewById(R.id.source);
        TextView clock_text = findViewById(R.id.time_text);

        try {
            // Trying to start constructor with thread
            new Ntp(clock_source, clock_text);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Click listener for button
        findViewById(R.id.button).setOnClickListener(view -> {
            Random rand = new Random();
            // Creates random value between 0-255 and sets that r,g,b value in bg
            int r = rand.nextInt((255) + 1);
            int g = rand.nextInt((255) + 1);
            int b = rand.nextInt((255) + 1);
            View bg_layout = findViewById(R.id.layout);
            bg_layout.setBackgroundColor(Color.rgb(r, g, b));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // onCreateOption has to be boolean!
        // returning super so nobody knows if its true or false.
        // but it has to be here so option menu can exist
        // dont hate the coder, hate java
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Try to open URL from options
        int id = item.getItemId();
        try {
            if (id == R.id.menu_github) {
                Intent browser_git = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DanjelTahko"));
                startActivity(browser_git);
            } else if (id == R.id.menu_website) {
                Intent browser_web = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.danieltahko.com/"));
                startActivity(browser_web);
            }
        } catch (ActivityNotFoundException e) {
            // Toast out message if not able to open URL
            Toast.makeText(this, "Can't open URL, is this Nokia3310?", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }
}