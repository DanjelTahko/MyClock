package com.example.myclock;

import android.widget.TextView;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

public class Clock {

    private static NTPUDPClient client = null;
    private static final int port = 123;
    private static InetAddress ntp_host = null;
    private static TimeInfo time;

    public String getTime() {

        try {
            client = new NTPUDPClient();
            ntp_host = InetAddress.getByName("pool.ntp.org");
            time = client.getTime(ntp_host, port);
            System.out.println("Client TRY is OK");
        } catch (IOException e) {
            e.printStackTrace();
        }

        long return_time = time.getMessage().getTransmitTimeStamp().getTime();
        Date date_time = new Date(return_time);

        String whoop = date_time.toString();
        System.out.println(whoop);

        return whoop;
    }
}
