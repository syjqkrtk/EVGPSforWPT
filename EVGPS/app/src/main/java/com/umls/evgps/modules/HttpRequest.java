package com.umls.evgps.modules;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {
    static private String host = "http://2.50.110.255:3000/api/push";

    private static class RequestRunnable implements Runnable {
        Context context;
        JSONObject params;
        String method;
        String api;

        public RequestRunnable(Context context, String method, String api, JSONObject params){
            this.params = params;
            this.method = method;
            this.api = api;
            this.context = context;
        }

        public RequestRunnable(String method, String api, JSONObject params){
            this.params = params;
            this.method = method;
            this.api = api;
        }

        @Override
        public void run() {
            try{
                Log.i("olev", host + api);
                final URL url = new URL(host + api);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("olev_server", "olev_server_key");
                if(!method.equals("GET")){
                    conn.setDoOutput(true);
                }

                if(null != params){
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(params.toString());
                    wr.flush();
                    wr.close();
                }

                if(200 == conn.getResponseCode()){
                    Log.i("olev", "SUCCESS");
                }else{
                    if(null != context){
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "등록되지 않은 전화번호\r\n(InverterGPS 가 설치된 전화번호를 입력하시오)", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    Log.i("olev", "FAIL");
                }
            }catch(IOException ioex){
                Log.i("olev", ioex.getMessage());
                ioex.printStackTrace();
            }
        }
    }

    static public void request(Context context, String method, String api, JSONObject params){
        new Thread(new RequestRunnable(context, method, api, params)).start();
    }

    static public void request(String method, String api, JSONObject params){
        new Thread(new RequestRunnable(method, api, params)).start();
    }
}
