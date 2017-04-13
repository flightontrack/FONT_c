package com.flightontrack;

import android.content.Context;
import android.content.Intent;
//import android.content.BroadcastReceiver;
//import android.os.IBinder;
import android.os.BatteryManager;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.BATTERY_SERVICE;
import static com.flightontrack.Const.*;

public class ReceiverHealthCheckAlarm extends WakefulBroadcastReceiver {
    private static final String TAG = "ReceiverHealthCheckAlarm:";
    public static boolean alarmDisable = false;
    public static boolean isRestart = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        isRestart = false;
        if(!MainActivity.isMainActivityExist()){
            Util.appendLog(TAG+ "!!!! MainActivity is killed ...... returning",'d');
            return;
        }

        if(alarmDisable) {
            Util.appendLog(TAG+ "!!!! Alarm Disabled",'d');
            return;
        }

        if(!alarmDisable && MyApplication.fontAppType==APPTYPE.PRIVATE) {
            healthCheckComm(context);

            if (!SvcLocationClock.isInstanceCreated()) {
                Util.appendLog(TAG+ "Restarting : performClick()",'d');
                MainActivity.set_isMultileg(true);
                MainActivity.trackingButton.performClick();
                isRestart = true;
                //getBatteryCapacity(context);
                healthCheckComm(context);
            }

            AlarmManagerCtrl.setAlarm();

            return;
        }
    }

    public void getBatteryCapacity(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager mBatteryManager = (BatteryManager) ctx.getSystemService(Context.BATTERY_SERVICE);
            Long chargeCounter = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
            Long capacity = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

            if (chargeCounter != null && capacity != null) {
                long value = (long) (((float) chargeCounter / (float) capacity) * 100f);
                //return value;
                Util.appendLog(TAG + "BATTERY_PROPERTY_CAPACITY: "+value, 'd');
            }
        }
        //return 0;
    }
    void healthCheckComm(Context ctx) {
        Util.appendLog(TAG+ "healthCheckComm",'d');

        RequestParams requestParams = new RequestParams();
        requestParams.put("rcode", REQUEST_IS_CLOCK_ON);
        requestParams.put("isrestart", isRestart);
        requestParams.put("phonenumber", MainActivity._myPhoneId);
        requestParams.put("deviceid", MainActivity._myDeviceId);
        requestParams.put("isClockOn", SvcLocationClock.isInstanceCreated());
        requestParams.put("flightid", Route.activeFlight==null?FLIGHT_NUMBER_DEFAULT :Route.activeFlight.flightNumber);
        requestParams.put("isdebug", MainActivity.AppProp.pIsDebug);
        requestParams.put("battery", Util.getBattery());

        new AsyncHttpClient().post(Util.getTrackingURL() + ctx.getString(R.string.aspx_communication), requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Response response = new Response(new String(responseBody));
                        String responseText = new String(responseBody);
                        Util.appendLog(TAG + "healthCheckComm OnSuccess response:"+responseText, 'd');

                        if (response.responseAckn != null) {
                            Util.appendLog(TAG + "onSuccess|HealthCheck: "+response.responseAckn,'d');
                        }
                        if (response.responseNotif != null) {
                            Util.appendLog(TAG + "onSuccess|HealthCheck|RESPONSE_TYPE_NOTIF:" +response.responseNotif,'d');
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        Util.appendLog(TAG + "onFailure|HealthCheck: " ,'d');

                    }
                    public void onFinish() {

                    }
                }
        );

    }
}
