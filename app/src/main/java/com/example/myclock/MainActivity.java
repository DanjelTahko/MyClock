package com.example.myclock;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public class MainActivity extends AppCompatActivity {

    private NTPUDPClient client = null;
    private int port = 123;
    private InetAddress ntp_host = null;
    private TimeInfo time;

    private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    private TextView clock_source = null;
    private TextView clock_text = null;

    private long app_time;
    private long ntp_time;
    private long current_time = 0;
    private long last_time = 0;
    private long diff = 0;

    public SimpleDateFormat date_format = new SimpleDateFormat("HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(policy);
        date_format.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));

        clock_source = findViewById(R.id.source);
        clock_text = findViewById(R.id.time_text);

        startClock(clock_source, clock_text);

        // Whoopsidoo change background color on click (Only needed one button?? amazing)
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random rand = new Random();
                // Creates random value between 0-255 and sets that r,g,b value in bg
                int r = rand.nextInt((255) + 1);
                int g = rand.nextInt((255) + 1);
                int b = rand.nextInt((255) + 1);
                View bg_layout = findViewById(R.id.layout);
                bg_layout.setBackgroundColor(Color.rgb(r, g, b));
            }
        });
    }

    public void startClock(TextView source, TextView clock) {

        Thread clock_thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (checkNetwork()) {
                            source.setText("NTP Time");
                            app_time = Calendar.getInstance().getTimeInMillis();
                            current_time = app_time + diff;
                            clock.setText(date_format.format(current_time));
                        } else {
                            source.setText("APP Time");
                            app_time = Calendar.getInstance().getTimeInMillis();
                            clock.setText(date_format.format(app_time));
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error with Try set TIME");
                    }
                }
            }
        };

        Thread get_clock_thread = new Thread() {
            @Override
            public void run() {
                long delay = 30000;
                while(true) {
                    if(checkNetwork()) {
                        if (current_time - last_time > delay) {
                            app_time = Calendar.getInstance().getTimeInMillis();
                            ntp_time = getNtpTime();
                            diff = ntp_time - app_time;
                            System.out.println("TIME DIFF = " + diff);
                            last_time = current_time;
                        }
                    }
                }
            }
        };
        clock_thread.start();
        get_clock_thread.start();
    }

    public long getNtpTime() {

        client = new NTPUDPClient();
        long return_time = 0;

        try {
            ntp_host = InetAddress.getByName("pool.ntp.org");
            time = client.getTime(ntp_host, port);
            return_time = time.getMessage().getTransmitTimeStamp().getTime();
        } catch (IOException e) {
            e.printStackTrace();
            client.close();
            return 0;
        }
        client.close();
        return return_time;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
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

    public boolean checkNetwork() {

        boolean CONNECTION = false;

        ConnectivityManager con_manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] net_info = con_manager.getAllNetworkInfo();

        for (int i = 0; i < net_info.length; i++) {
            if ((net_info[i].getTypeName().equalsIgnoreCase("WIFI")) && (net_info[i].isConnected())) {
                CONNECTION = true;
            } else if ((net_info[i].getTypeName().equalsIgnoreCase("MOBILE")) && (net_info[i].isConnected())) {
                CONNECTION = true;
            }
        }
        return CONNECTION;
    }
}