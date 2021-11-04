package com.umls.invertergpskaist.modules;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.umls.invertergpskaist.MainActivity;
import com.umls.invertergpskaist.R;

import org.json.JSONException;
import org.json.JSONObject;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class FirebaseInstanceIDService extends FirebaseMessagingService {
    private class RegisterThread implements Runnable{
        String token;
        public RegisterThread(String token){
            this.token = token;
        }

        public void run(){
            boolean isChecked = false;
            while(!isChecked){
                if (checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    isChecked = true;
                }
                Log.i("olev", isChecked + "");
            }


            TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            try {
                JSONObject params = new JSONObject();
                params.put("token", token);
                params.put("phone", mgr.getSimSerialNumber());
                HttpRequest.request("POST", "/init", params);
            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i("olev", "FirebaseInstanceIDService : " + s);

        new Thread(new RegisterThread(s)).start();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        Log.i("olev", "Receive Message");
        Log.i("olev", remoteMessage.getData().size() + "");
        if(null != remoteMessage && remoteMessage.getData().size() > 0){
            String sender = remoteMessage.getData().get("s_number");
            String contents = remoteMessage.getData().get("data");

            Log.i("olev", "number : " + sender);
            Log.i("olev", "data : " + contents);
            sendToActivity(this, sender, contents);

            // Tray Alarm
            sendNotification(remoteMessage);
        }
    }

    private void sendToActivity(Context context, String sender, String contents){
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("sender", sender);
        intent.putExtra("contents", contents);
        context.startActivity(intent);
    }

    // Tray Alarm
    private void sendNotification(RemoteMessage remoteMessage){
        String sender = remoteMessage.getData().get("s_number");
        String contents = remoteMessage.getData().get("data");

        Log.i("Firebase2", Build.VERSION.SDK_INT + " : " + Build.VERSION_CODES.O);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channel = "채널";
            String channel_nm = "채널명";

            NotificationManager notichannel = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channelMessage = new NotificationChannel(channel, channel_nm,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channelMessage.setDescription("채널에 대한 설명.");
            channelMessage.enableLights(true);
            channelMessage.enableVibration(true);
            channelMessage.setShowBadge(true);
            channelMessage.setVibrationPattern(new long[]{300, 200, 100, 200, 100, 200, 100, 200, 100});
            notichannel.createNotificationChannel(channelMessage);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channel)
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle(sender)
                            .setContentText(contents)
                            .setChannelId(channel)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(9999, notificationBuilder.build());
        }else{
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(sender)
                    .setContentText(contents)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(9999, notificationBuilder.build());
        }
    }
}
