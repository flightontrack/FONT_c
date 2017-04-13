package com.flightontrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.telephony.SmsManager;
import static com.flightontrack.Const.*;
import android.util.Log;

import static com.flightontrack.Const.APPBOOT_DELAY_MILLISEC;
import static com.flightontrack.Const.FONT_RECEIVER_FILTER;

public class ReceiverBatteryLevel extends BroadcastReceiver {
    private static final String TAG = "ReceiverBatteryLevel:";

    @Override
    public void onReceive(Context context, Intent intent) {
        Util.appendLog(TAG + "onReceive intent: "+intent, 'd');
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = (int) (level / (float)scale);

        Util.setBattery(String.valueOf(level));
        String phoneNo = SMS_RECEIPIENT_PHONE;

        if (intent.getAction().contains("BATTERY_LOW")) {

            String message =    "Ed help !!!"+"\n"+
                    SMS_LOWBATTERY_TEXT+"\n"+
                    "Pilot : "+Util.getUserName()+"\n"+
                    "Aircraft : "+Util.getAcftNum(4)+"\n";
            Util.appendLog(TAG + "BatteryPct low: Level :"+level+" out of "+scale, 'd');

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
        }
        if (intent.getAction().contains("BATTERY_OKAY")) {
            Util.setBattery(String.valueOf(level));
            Util.appendLog(TAG + "Battery Restored Level: "+level, 'd');
        }
        }
    }
