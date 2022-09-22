package com.example.myclock;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Ntp extends AppCompatActivity {

    // Variable init
    private long app_time;
    private long ntp_time;
    private final long delay = 30000;
    private long current_time;
    private long last_time = 0;
    private long diff = 0;
    public SimpleDateFormat date_format = new SimpleDateFormat("HH:mm:ss");

    public Ntp(TextView source, TextView clock) {
        // sets timezone
        date_format.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
        // gets current_time so we can use sleep without busy-wait and show right time
        current_time = app_time = Calendar.getInstance().getTimeInMillis();

        Thread clock_thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // If theres network, get time from NTP
                        if (checkNetwork()) {
                            // if more than 30 sec ago, get new time from NTP
                            if (current_time - last_time > delay) {
                                app_time = Calendar.getInstance().getTimeInMillis();
                                ntp_time = getNtpTime();
                                // updates and takes time difference between ntp and app
                                diff = ntp_time - app_time;
                                System.out.println("TIME DIFF = " + diff);
                                // sets last time to current time so we only update time every 30 sec
                                last_time = current_time;
                            }
                            source.setText("NTP Time");
                            app_time = Calendar.getInstance().getTimeInMillis();
                            // setting current time to right time with help from dif
                            current_time = app_time + diff;
                            clock.setText(date_format.format(current_time));
                        } else {
                            // if no internet connection, get time from app
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
        clock_thread.start();
    }

    public long getNtpTime() {
        // NTP client init
        NTPUDPClient client = new NTPUDPClient();
        long return_time;

        try {
            // returns ip adress for pool.ntp.org
            InetAddress ntp_server = InetAddress.getByName("pool.ntp.org");
            // returns time from ntp server with given ip and port
            TimeInfo time = client.getTime(ntp_server, 123);
            // convert time to long
            return_time = time.getMessage().getTransmitTimeStamp().getTime();
        } catch (IOException e) {
            e.printStackTrace();
            client.close();
            return 0;
        }
        client.close();
        return return_time;
    }

    public boolean checkNetwork() {

        boolean connection = true;
        try {
            // trying to ping to see if theres internet connection
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8");
            if (process.waitFor() == 0) {
                connection = true;
            } else {
                connection = false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
