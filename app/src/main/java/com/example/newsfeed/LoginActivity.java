package com.example.newsfeed;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private Button bt1;
    private Button bt2;
    private EditText ed1;
    private EditText ed2;
    private String name;
    private String password;
    private Config config;
    final private OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        config = new Config();

        bt1 = (Button) findViewById(R.id.button1);
        bt2 = (Button) findViewById(R.id.button2);
        ed1 = (EditText) findViewById(R.id.editText1);
        ed2 = (EditText) findViewById(R.id.editText2);

        Log.d("LoginActivity", "onCreate: ");

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name=ed1.getText().toString().trim();
                password=ed2.getText().toString().trim();
                Log.d("LoginActivity", "ed1: "+name);
                //通过okhttp发起post请求
                postLoginRequest(name,password);
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name=ed1.getText().toString().trim();
                password=ed2.getText().toString().trim();
                Log.d("LoginActivity", "ed1: "+name);
                //通过okhttp发起post请求
                postRegisterRequest(name,password);
            }
        });

    }

    private void postLoginRequest(final String name,final String password)  {
        //建立请求表单，添加上传服务器的参数
        RequestBody formBody = new FormBody.Builder()
                .add("name",name)
                .add("password",password)
                .build();
        //发起请求
        final Request request = new Request.Builder()
                .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/check")
                .post(formBody)
                .build();
        //新建一个线程，用于得到服务器响应的参数
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("LoginActivity", "responseData: "+responseData);
                    if (responseData != "NOTFOUND") {
                        JSONObject jsonObject = new JSONObject(responseData);
                        Log.d("LoginActivity", "name: "+jsonObject.getString("name") + ",password:"+jsonObject.getString("password"));
                        if (name.equals(jsonObject.getString("name")) && password.equals(jsonObject.getString("password"))) {
                            Intent intent = new Intent();
                            intent.putExtra("name", name);
                            intent.putExtra("password", password);
                            intent.putExtra("id", jsonObject.getString("id"));
                            Log.d("Login Activity", "run: Login success");

                            intent.putExtra("ed1", name);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                    } else {
                        Log.d("LoginActivity", "run: NOTFOUND");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("LoginActivity", "run: Exception");
                }
            }
        }).start();

    }

    private void postRegisterRequest(final String name,final String password)  {
        //建立请求表单，添加上传服务器的参数
        RequestBody formBody = new FormBody.Builder()
                .add("name",name)
                .add("password",password)
                .build();
        //发起请求
        final Request request = new Request.Builder()
                .url(config.getScheme() + "://" + config.getHost() + ":" +config.getPort().toString() + "/users/add")
                .post(formBody)
                .build();
        //新建一个线程，用于得到服务器响应的参数
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    //回调
                    response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d("LoginActivity", "responseData: "+responseData);
                    if (responseData != "FAILURE" && responseData != "EXISTED") {
                        JSONObject jsonObject = new JSONObject(responseData);
                        Log.d("LoginActivity", "name: "+jsonObject.getString("name") + ",password:"+jsonObject.getString("password"));
                        if (name.equals(jsonObject.getString("name")) && password.equals(jsonObject.getString("password"))) {
                            Intent intent = new Intent();
                            intent.putExtra("name", name);
                            intent.putExtra("password", password);
                            intent.putExtra("id", jsonObject.getString("id"));
                            Log.d("Login Activity", "run: Login SUCCESS");
                            intent.putExtra("ed1", name);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                    } else if(responseData.equals("EXISTED")){
                        Log.d("LoginActivity", "run: username EXISTED");
                    } else {
                        Log.d("LoginActivity", "run: register FAILURE");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("LoginActivity", "run: Exception");
                }
            }
        }).start();

    }

}
