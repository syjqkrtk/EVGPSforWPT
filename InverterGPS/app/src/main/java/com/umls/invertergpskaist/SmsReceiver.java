package com.umls.invertergpskaist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    @Override
    public void onReceive(Context context, Intent intent) {
        // sms가 오면 onReceive() 가 호출된다. 여기에 처리하는 코드 작성하면 된다.
        // Log.d(TAG, "onReceive() 호출됨.");
        Bundle bundle = intent.getExtras();
        // parseSmsMessage() 메서드의 코드들은 SMS문자의 내용을 뽑아내는 정형화된 코드이다.
        // 복잡해 보일 수 있으나 그냥 그대로 가져다 쓰면 된다.
        SmsMessage[] messages = parseSmsMessage(bundle);


        // 문자메세지에서 송신자와 관련된 내용을 뽑아낸다.
        String sender = messages[0].getOriginatingAddress();
        Log.d(TAG, "sender: "+sender);

        // 문자메세지 내용 추출
        String contents = messages[0].getMessageBody().toString();
        Log.d(TAG, "contents: "+contents);

        // 해당 내용을 모두 합쳐서 액티비티로 보낸다.
        sendToActivity(context, sender, contents);

    }

    private void sendToActivity(Context context, String sender, String contents){
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("sender", sender);
        intent.putExtra("contents", contents);
        context.startActivity(intent);
    }

    // 정형화된 코드. 그냥 가져다 쓰면 된다.
    private SmsMessage[] parseSmsMessage(Bundle bundle){

        Object[] objs = (Object[])bundle.get("pdus");
        SmsMessage[] messages = new SmsMessage[objs.length];


        if (bundle != null) {
            for (int i = 0; i < objs.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) objs[i]);
            }
        }

        return messages;
    }
}
